#include <cstdlib>
#include <algorithm>
#include <cstddef>
#include <cwctype>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <iterator>
#include <string>
#include <utility>
#include <vector>

#include <windows.h>
#include <tlhelp32.h>

#include "manual_map.h"

namespace {

struct JavaProcess {
    DWORD pid;
    std::wstring executable;
    std::wstring window_title;
};

BOOL CALLBACK collect_window_title(HWND window, LPARAM parameter) {
    auto* context = reinterpret_cast<std::pair<DWORD, std::wstring*>*>(parameter);
    DWORD window_pid = 0;
    GetWindowThreadProcessId(window, &window_pid);
    if (window_pid != context->first || !IsWindowVisible(window)) return TRUE;

    const int length = GetWindowTextLengthW(window);
    if (length <= 0) return TRUE;
    std::wstring title(static_cast<size_t>(length) + 1, L'\0');
    const int copied = GetWindowTextW(window, title.data(), length + 1);
    if (copied > 0) {
        title.resize(static_cast<size_t>(copied));
        *context->second = std::move(title);
        return FALSE;
    }
    return TRUE;
}

std::wstring get_window_title(DWORD pid) {
    std::wstring title;
    std::pair<DWORD, std::wstring*> context{pid, &title};
    EnumWindows(collect_window_title, reinterpret_cast<LPARAM>(&context));
    return title;
}

std::vector<JavaProcess> find_java_processes() {
    std::vector<JavaProcess> java_processes;
    HANDLE snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (snapshot == INVALID_HANDLE_VALUE) return java_processes;

    PROCESSENTRY32W entry{};
    entry.dwSize = sizeof(entry);
    if (Process32FirstW(snapshot, &entry)) {
        do {
            const bool is_java = _wcsicmp(entry.szExeFile, L"java.exe") == 0
                    || _wcsicmp(entry.szExeFile, L"javaw.exe") == 0;
            if (is_java) {
                java_processes.push_back(JavaProcess{
                        entry.th32ProcessID,
                        entry.szExeFile,
                        get_window_title(entry.th32ProcessID)});
            }
        } while (Process32NextW(snapshot, &entry));
    }
    CloseHandle(snapshot);

    auto title_priority = [](const JavaProcess& process) {
        std::wstring title = process.window_title;
        std::transform(title.begin(), title.end(), title.begin(),
                       [](wchar_t c) { return static_cast<wchar_t>(std::towlower(c)); });
        if (title.find(L"minecraft") != std::wstring::npos
                || title.find(L"forge") != std::wstring::npos) return 0;
        return process.window_title.empty() ? 2 : 1;
    };
    std::stable_sort(java_processes.begin(), java_processes.end(),
            [&](const JavaProcess& left, const JavaProcess& right) {
                return title_priority(left) < title_priority(right);
            });
    return java_processes;
}

void set_target_window_title(DWORD pid, const std::wstring& title) {
    std::wstring text = L"OpenHachimi Injector - ";
    if (title.empty()) {
        text += L"PID " + std::to_wstring(pid);
    } else {
        text += title.substr(0, 120);
    }
    SetConsoleTitleW(text.c_str());
}

void wait_for_exit() {
    std::wcout << L"\nPress Enter to close this window..." << std::flush;
    std::wstring ignored;
    std::getline(std::wcin, ignored);
}

int interactive_error(int code, const std::wstring& message) {
    std::wcerr << message << L"\n";
    wait_for_exit();
    return code;
}

} // namespace

int wmain(int argc, wchar_t** argv) {
    SetConsoleTitleW(L"OpenHachimi Injector");
    const bool interactive = argc == 1;
    DWORD pid = 0;
    std::wstring selected_title;
    std::filesystem::path dll_path;
    const std::filesystem::path executable_path = std::filesystem::absolute(argv[0]);

    if (interactive) {
        std::wcout << L"OpenHachimi injector\n"
                   << L"Java processes and their window titles:\n\n";
        const std::vector<JavaProcess> java_processes = find_java_processes();
        if (java_processes.empty()) {
            return interactive_error(2, L"No java.exe or javaw.exe process was found. Start Minecraft first.");
        }

        for (size_t i = 0; i < java_processes.size(); ++i) {
            const JavaProcess& process = java_processes[i];
            std::wcout << L"[" << (i + 1) << L"] PID " << process.pid
                       << L" | " << process.executable << L" | ";
            if (process.window_title.empty()) {
                std::wcout << L"(no window title)";
            } else {
                std::wcout << process.window_title;
            }
            std::wcout << L"\n";
        }
        std::wcout << L"\nEnter the number or PID to inject: " << std::flush;
        std::wstring selection;
        std::getline(std::wcin, selection);
        wchar_t* end = nullptr;
        const unsigned long selected = std::wcstoul(selection.c_str(), &end, 10);
        if (selection.empty() || end == selection.c_str() || *end != L'\0') {
            return interactive_error(3, L"Invalid selection.");
        }
        auto selected_process = java_processes.end();
        if (selected >= 1 && selected <= java_processes.size()) {
            selected_process = java_processes.begin() + static_cast<ptrdiff_t>(selected - 1);
        } else {
            pid = static_cast<DWORD>(selected);
            selected_process = std::find_if(java_processes.begin(), java_processes.end(),
                    [pid](const JavaProcess& process) { return process.pid == pid; });
            if (selected_process == java_processes.end()) {
                return interactive_error(3, L"That PID is not in the listed Java processes.");
            }
        }
        pid = selected_process->pid;
        selected_title = selected_process->window_title;
        set_target_window_title(pid, selected_title);
        dll_path = executable_path.parent_path() / L"OpenHachimi.dll";
    } else {
        wchar_t* end = nullptr;
        const unsigned long parsed = std::wcstoul(argv[1], &end, 10);
        if (argv[1][0] == L'\0' || end == argv[1] || *end != L'\0' || parsed == 0) {
            std::wcerr << L"usage: OpenHachimiInject.exe <pid> [dll_path]\n";
            return 2;
        }
        pid = static_cast<DWORD>(parsed);
        dll_path = argc >= 3 ? std::filesystem::path(argv[2])
                             : executable_path.parent_path() / L"OpenHachimi.dll";
        selected_title = get_window_title(pid);
        set_target_window_title(pid, selected_title);
    }

    std::ifstream file(dll_path, std::ios::binary);
    if (!file) {
        const std::wstring message = L"Cannot open DLL: " + dll_path.wstring();
        if (interactive) return interactive_error(4, message);
        std::wcerr << message << L"\n";
        return 4;
    }

    std::vector<char> bytes(
            (std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    if (bytes.size() < sizeof(IMAGE_DOS_HEADER)) {
        return interactive ? interactive_error(5, L"The DLL file is too small.") : 5;
    }

    std::wcout << L"Mapping " << dll_path.wstring() << L" (" << bytes.size()
               << L" bytes) into PID " << pid << L"...\n";
    const std::wstring error = loader::inject_in_memory(pid, bytes.data(), bytes.size());
    if (!error.empty()) {
        if (interactive) return interactive_error(6, L"Injection failed: " + error);
        std::wcerr << L"Injection failed: " << error << L"\n";
        return 6;
    }

    std::wcout << L"DLL mapped. Bootstrap continues inside Minecraft.\n";
    if (interactive) wait_for_exit();
    return 0;
}
