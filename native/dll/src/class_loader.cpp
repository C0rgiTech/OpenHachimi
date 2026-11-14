#include "openhachimi.h"

#include <cstring>

namespace hachimi::classes {

namespace {
jobject g_retained_url_loader = nullptr;

bool clear_exception(JNIEnv* env, const char* location) {
    if (!env->ExceptionCheck()) return false;
    log("JNI exception at %s", location);
    env->ExceptionDescribe();
    env->ExceptionClear();
    return true;
}

jvmtiEnv* get_jvmti(JavaVM* vm) {
    jvmtiEnv* result = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&result), JVMTI_VERSION_1_2) != JNI_OK || !result) {
        log("GetEnv(JVMTI_VERSION_1_2) failed");
        return nullptr;
    }
    return result;
}

jobject class_loader_of(JNIEnv* env, jclass cls) {
    jclass class_class = env->FindClass("java/lang/Class");
    if (!class_class) return nullptr;
    jmethodID get_loader = env->GetMethodID(
            class_class, "getClassLoader", "()Ljava/lang/ClassLoader;");
    if (!get_loader) {
        env->DeleteLocalRef(class_class);
        return nullptr;
    }
    jobject loader = env->CallObjectMethod(cls, get_loader);
    env->DeleteLocalRef(class_class);
    if (clear_exception(env, "Class.getClassLoader")) return nullptr;
    return loader;
}
} // namespace

jobject find_game_class_loader(JavaVM* vm, JNIEnv* env) {
    jvmtiEnv* jvmti = get_jvmti(vm);
    if (!jvmti) return nullptr;

    jint count = 0;
    jclass* loaded = nullptr;
    if (jvmti->GetLoadedClasses(&count, &loaded) != JVMTI_ERROR_NONE || !loaded) {
        log("JVMTI GetLoadedClasses failed");
        return nullptr;
    }

    jobject game_loader = nullptr;
    constexpr const char* minecraft_signature = "Lnet/minecraft/client/Minecraft;";
    for (jint i = 0; i < count && !game_loader; ++i) {
        char* signature = nullptr;
        if (jvmti->GetClassSignature(loaded[i], &signature, nullptr) != JVMTI_ERROR_NONE) continue;
        if (signature && std::strcmp(signature, minecraft_signature) == 0) {
            game_loader = class_loader_of(env, loaded[i]);
        }
        if (signature) jvmti->Deallocate(reinterpret_cast<unsigned char*>(signature));
    }

    jvmti->Deallocate(reinterpret_cast<unsigned char*>(loaded));
    if (!game_loader) log("Minecraft class is not loaded yet");
    return game_loader;
}

jclass load_inject_bootstrap(JNIEnv* env, jobject game_loader,
                             const std::wstring& jar_path) {
    jclass file_class = env->FindClass("java/io/File");
    if (!file_class) { clear_exception(env, "FindClass File"); return nullptr; }
    jmethodID file_ctor = env->GetMethodID(file_class, "<init>", "(Ljava/lang/String;)V");
    jstring path = env->NewString(reinterpret_cast<const jchar*>(jar_path.data()),
                                   static_cast<jsize>(jar_path.size()));
    jobject file = env->NewObject(file_class, file_ctor, path);
    env->DeleteLocalRef(path);
    if (clear_exception(env, "new File")) return nullptr;

    jmethodID to_uri = env->GetMethodID(file_class, "toURI", "()Ljava/net/URI;");
    jobject uri = env->CallObjectMethod(file, to_uri);
    if (clear_exception(env, "File.toURI")) return nullptr;

    jclass uri_class = env->FindClass("java/net/URI");
    jmethodID to_url = env->GetMethodID(uri_class, "toURL", "()Ljava/net/URL;");
    jobject url = env->CallObjectMethod(uri, to_url);
    if (clear_exception(env, "URI.toURL")) return nullptr;

    jclass url_class = env->FindClass("java/net/URL");
    jobjectArray urls = env->NewObjectArray(1, url_class, url);
    jclass loader_class = env->FindClass("java/net/URLClassLoader");
    jmethodID loader_ctor = env->GetMethodID(
            loader_class, "<init>", "([Ljava/net/URL;Ljava/lang/ClassLoader;)V");
    jobject url_loader = env->NewObject(loader_class, loader_ctor, urls, game_loader);
    if (clear_exception(env, "new URLClassLoader")) return nullptr;

    // Retain the loader globally because Forge listeners and assets remain in use
    // after this native worker returns.
    g_retained_url_loader = env->NewGlobalRef(url_loader);
    if (!g_retained_url_loader) {
        log("NewGlobalRef(URLClassLoader) failed");
        return nullptr;
    }

    jclass class_loader_class = env->FindClass("java/lang/ClassLoader");
    jmethodID load_class = env->GetMethodID(
            class_loader_class, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    jstring bootstrap_name = env->NewStringUTF("dev.openhachimi.security.InjectBootstrap");
    jobject bootstrap = env->CallObjectMethod(url_loader, load_class, bootstrap_name);
    if (clear_exception(env, "URLClassLoader.loadClass InjectBootstrap")) return nullptr;
    return static_cast<jclass>(bootstrap);
}

} // namespace hachimi::classes
