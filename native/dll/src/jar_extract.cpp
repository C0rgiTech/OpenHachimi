#include "openhachimi.h"
#include "resource.h"

#include <filesystem>

namespace hachimi::jar {

namespace {
const void* find_rcdata(HMODULE image, WORD resource_id, DWORD& size) {
    size = 0;
    auto* base = reinterpret_cast<BYTE*>(image);
    auto* dos = reinterpret_cast<PIMAGE_DOS_HEADER>(base);
    if (!base || dos->e_magic != IMAGE_DOS_SIGNATURE) return nullptr;
    auto* nt = reinterpret_cast<PIMAGE_NT_HEADERS>(base + dos->e_lfanew);
    if (nt->Signature != IMAGE_NT_SIGNATURE) return nullptr;

    const auto& directory = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_RESOURCE];
    if (directory.Size == 0) return nullptr;
    BYTE* root_base = base + directory.VirtualAddress;
    auto* root = reinterpret_cast<PIMAGE_RESOURCE_DIRECTORY>(root_base);

    auto find_id = [](PIMAGE_RESOURCE_DIRECTORY dir, WORD wanted)
            -> PIMAGE_RESOURCE_DIRECTORY_ENTRY {
        auto* entries = reinterpret_cast<PIMAGE_RESOURCE_DIRECTORY_ENTRY>(dir + 1);
        const WORD total = dir->NumberOfNamedEntries + dir->NumberOfIdEntries;
        for (WORD i = dir->NumberOfNamedEntries; i < total; ++i) {
            if (!entries[i].NameIsString && entries[i].Id == wanted) return &entries[i];
        }
        return nullptr;
    };

    auto* type = find_id(root, 10);
    if (!type || !type->DataIsDirectory) return nullptr;
    auto* names = reinterpret_cast<PIMAGE_RESOURCE_DIRECTORY>(root_base + type->OffsetToDirectory);
    auto* name = find_id(names, resource_id);
    if (!name || !name->DataIsDirectory) return nullptr;
    auto* languages = reinterpret_cast<PIMAGE_RESOURCE_DIRECTORY>(root_base + name->OffsetToDirectory);
    if (languages->NumberOfNamedEntries + languages->NumberOfIdEntries == 0) return nullptr;
    auto* language = reinterpret_cast<PIMAGE_RESOURCE_DIRECTORY_ENTRY>(languages + 1);
    auto* data = reinterpret_cast<PIMAGE_RESOURCE_DATA_ENTRY>(root_base + language->OffsetToData);
    size = data->Size;
    return base + data->OffsetToData;
}
} // namespace

bool extract_embedded(std::wstring& out_path) {
    DWORD size = 0;
    const void* bytes = find_rcdata(hachimi::g_self_module, IDR_HACHIMI_JAR, size);
    if (!bytes || size == 0) {
        log("The embedded OpenHachimi jar resource was not found");
        return false;
    }

    wchar_t temp[MAX_PATH]{};
    const DWORD temp_length = GetTempPathW(MAX_PATH, temp);
    if (temp_length == 0 || temp_length >= MAX_PATH) {
        log("GetTempPathW failed: %lu", GetLastError());
        return false;
    }

    std::filesystem::path directory = std::filesystem::path(temp) / L"OpenHachimi";
    std::error_code error;
    std::filesystem::create_directories(directory, error);
    if (error) {
        log("Could not create the OpenHachimi temp directory");
        return false;
    }

    const std::filesystem::path jar_path = directory /
            (L"openhachimi-" + std::to_wstring(GetCurrentProcessId()) + L".jar");
    HANDLE file = CreateFileW(jar_path.c_str(), GENERIC_WRITE, FILE_SHARE_READ, nullptr,
                              CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, nullptr);
    if (file == INVALID_HANDLE_VALUE) {
        log("Creating the extracted jar failed: %lu", GetLastError());
        return false;
    }

    DWORD written = 0;
    const BOOL ok = WriteFile(file, bytes, size, &written, nullptr);
    const DWORD write_error = GetLastError();
    CloseHandle(file);
    if (!ok || written != size) {
        DeleteFileW(jar_path.c_str());
        log("Writing the extracted jar failed: %lu", write_error);
        return false;
    }

    // URLClassLoader reads resources from this jar after bootstrap returns, so it
    // must remain available for the lifetime of the game process.
    out_path = jar_path.wstring();
    log("Embedded jar extracted (%lu bytes)", size);
    return true;
}

} // namespace hachimi::jar
