#include "openhachimi.h"
#include "resource.h"

#include <atomic>
#include <cstdarg>
#include <cstdio>
#include <cwchar>

namespace hachimi {
HMODULE g_self_module = nullptr;

void log(const char* format, ...) {
    char buffer[1024]{};
    va_list args;
    va_start(args, format);
    std::vsnprintf(buffer, sizeof(buffer), format, args);
    va_end(args);
    OutputDebugStringA("[OpenHachimi] ");
    OutputDebugStringA(buffer);
    OutputDebugStringA("\r\n");
}
} // namespace hachimi

namespace {

std::atomic<bool> g_attached{false};
HANDLE g_process_mutex = nullptr;

struct DetachOnExit {
    JavaVM* vm = nullptr;
    ~DetachOnExit() {
        if (vm) vm->DetachCurrentThread();
    }
};

DWORD WINAPI inject_thread(LPVOID) {
    using namespace hachimi;
    log("bootstrap worker started for pid=%lu", GetCurrentProcessId());

    JavaVM* vm = jvm::find_vm();
    if (!vm) return 1;

    JNIEnv* env = nullptr;
    JavaVMAttachArgs args{};
    args.version = JNI_VERSION_1_8;
    args.name = const_cast<char*>("OpenHachimi injector");
    if (vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&env), &args) != JNI_OK || !env) {
        log("AttachCurrentThreadAsDaemon failed");
        return 2;
    }
    DetachOnExit detach{vm};

    std::wstring jar_path;
    if (!jar::extract_embedded(jar_path)) return 3;

    const jint attach_rc = jvm::attach_instrument(vm, jar_path);
    if (attach_rc != JNI_OK) {
        log("Agent_OnAttach returned %d; continuing to class-loader lookup", static_cast<int>(attach_rc));
    }

    jobject game_loader = nullptr;
    for (int attempt = 0; attempt < 30; ++attempt) {
        game_loader = classes::find_game_class_loader(vm, env);
        if (game_loader) break;
        Sleep(2000);
    }
    if (!game_loader) {
        log("Minecraft class loader was not found");
        return 4;
    }

    jclass bootstrap = classes::load_inject_bootstrap(env, game_loader, jar_path);
    if (!bootstrap) {
        env->DeleteLocalRef(game_loader);
        return 5;
    }

    jmethodID start = env->GetStaticMethodID(
            bootstrap, "inject", "(Ljava/lang/ClassLoader;)V");
    if (!start) {
        log("InjectBootstrap.inject(ClassLoader) was not found");
        if (env->ExceptionCheck()) env->ExceptionClear();
        env->DeleteLocalRef(bootstrap);
        env->DeleteLocalRef(game_loader);
        return 6;
    }

    env->CallStaticVoidMethod(bootstrap, start, game_loader);
    if (env->ExceptionCheck()) {
        log("InjectBootstrap.inject failed with a Java exception");
        env->ExceptionDescribe();
        env->ExceptionClear();
        env->DeleteLocalRef(bootstrap);
        env->DeleteLocalRef(game_loader);
        return 7;
    }

    log("InjectBootstrap.inject returned successfully");
    env->DeleteLocalRef(bootstrap);
    env->DeleteLocalRef(game_loader);
    return 0;
}

} // namespace

BOOL APIENTRY DllMain(HMODULE module, DWORD reason, LPVOID) {
    if (reason != DLL_PROCESS_ATTACH) return TRUE;

    wchar_t mutex_name[80]{};
    swprintf_s(mutex_name, L"Local\\OpenHachimiInject_%lu", GetCurrentProcessId());
    g_process_mutex = CreateMutexW(nullptr, FALSE, mutex_name);
    if (!g_process_mutex || GetLastError() == ERROR_ALREADY_EXISTS) {
        if (g_process_mutex) CloseHandle(g_process_mutex);
        g_process_mutex = nullptr;
        return TRUE;
    }

    bool expected = false;
    if (!g_attached.compare_exchange_strong(expected, true)) return TRUE;

    hachimi::g_self_module = module;
    DisableThreadLibraryCalls(module);
    HANDLE thread = CreateThread(nullptr, 0, inject_thread, nullptr, 0, nullptr);
    if (thread) {
        CloseHandle(thread);
    } else {
        hachimi::log("CreateThread failed: %lu", GetLastError());
        g_attached.store(false);
        CloseHandle(g_process_mutex);
        g_process_mutex = nullptr;
    }
    return TRUE;
}
