#include "openhachimi.h"

#include <cwchar>
#include <vector>

namespace hachimi::jvm {

namespace {
using AgentOnAttach = jint (JNICALL*)(JavaVM*, char*, void*);

bool find_instrument_dll(std::wstring& result) {
    HMODULE java_dll = GetModuleHandleW(L"java.dll");
    if (!java_dll) {
        log("java.dll is not loaded in the target process");
        return false;
    }

    wchar_t path[MAX_PATH]{};
    const DWORD length = GetModuleFileNameW(java_dll, path, MAX_PATH);
    if (length == 0 || length >= MAX_PATH) {
        log("GetModuleFileName(java.dll) failed: %lu", GetLastError());
        return false;
    }

    wchar_t* slash = wcsrchr(path, L'\\');
    if (!slash) return false;
    slash[1] = L'\0';
    result.assign(path);
    result.append(L"instrument.dll");
    return true;
}
} // namespace

JavaVM* find_vm() {
    HMODULE jvm_dll = GetModuleHandleW(L"jvm.dll");
    if (!jvm_dll) {
        log("jvm.dll is not loaded in the target process");
        return nullptr;
    }

    using GetCreatedJavaVMs = jint (JNICALL*)(JavaVM**, jsize, jsize*);
    auto get_vms = reinterpret_cast<GetCreatedJavaVMs>(
            GetProcAddress(jvm_dll, "JNI_GetCreatedJavaVMs"));
    if (!get_vms) {
        log("JNI_GetCreatedJavaVMs was not found");
        return nullptr;
    }

    JavaVM* vm = nullptr;
    jsize count = 0;
    const jint result = get_vms(&vm, 1, &count);
    if (result != JNI_OK || count < 1 || !vm) {
        log("JNI_GetCreatedJavaVMs failed: rc=%d count=%d", result, count);
        return nullptr;
    }
    return vm;
}

jint attach_instrument(JavaVM* vm, const std::wstring& jar_path) {
    std::wstring instrument_path;
    if (!find_instrument_dll(instrument_path)) return -1;

    HMODULE instrument = LoadLibraryW(instrument_path.c_str());
    if (!instrument) {
        log("Loading instrument.dll failed: %lu", GetLastError());
        return -1;
    }

    auto on_attach = reinterpret_cast<AgentOnAttach>(
            GetProcAddress(instrument, "Agent_OnAttach"));
    if (!on_attach) {
        log("Agent_OnAttach was not exported by instrument.dll");
        return -1;
    }

    const int needed = WideCharToMultiByte(CP_ACP, 0, jar_path.c_str(), -1,
                                           nullptr, 0, nullptr, nullptr);
    if (needed <= 0) {
        log("Could not encode the embedded jar path");
        return -1;
    }
    std::vector<char> options(static_cast<size_t>(needed));
    if (!WideCharToMultiByte(CP_ACP, 0, jar_path.c_str(), -1,
                             options.data(), needed, nullptr, nullptr)) {
        log("Encoding the embedded jar path failed: %lu", GetLastError());
        return -1;
    }

    const jint result = on_attach(vm, options.data(), nullptr);
    log("Agent_OnAttach returned %d", result);
    return result;
}

} // namespace hachimi::jvm
