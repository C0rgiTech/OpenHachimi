#pragma once

#include <jni.h>
#include <jvmti.h>
#include <windows.h>
#include <string>

namespace hachimi {

extern HMODULE g_self_module;

void log(const char* format, ...);

namespace jar {
bool extract_embedded(std::wstring& out_path);
}

namespace jvm {
JavaVM* find_vm();
jint attach_instrument(JavaVM* vm, const std::wstring& jar_path);
}

namespace classes {
jobject find_game_class_loader(JavaVM* vm, JNIEnv* env);
jclass load_inject_bootstrap(JNIEnv* env, jobject game_loader,
                             const std::wstring& jar_path);
}

} // namespace hachimi
