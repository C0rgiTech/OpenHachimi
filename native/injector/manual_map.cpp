#include "manual_map.h"

#include <psapi.h>

#include <algorithm>
#include <cstring>
#include <exception>
#include <initializer_list>
#include <cwchar>
#include <string>
#include <sstream>
#include <vector>

#pragma comment(lib, "psapi.lib")

namespace loader {

namespace {
constexpr size_t kMaxMappedImageSize = 512ull * 1024ull * 1024ull;
constexpr size_t kMaxRemoteModules = 65536;
constexpr size_t kMaxImageStringSize = 32768;
constexpr size_t kMaxImportDescriptors = 65536;
constexpr size_t kMaxImportThunks = 1024 * 1024;

std::wstring fmt_err(const wchar_t* where, DWORD err) {
    wchar_t msg[256] = {0};
    FormatMessageW(FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                   nullptr, err, 0, msg, 256, nullptr);
    std::wstringstream ss;
    ss << where << L" failed (" << err << L"): " << msg;
    return ss.str();
}

bool range_within(size_t offset, size_t length, size_t total) {
    return offset <= total && length <= total - offset;
}

bool read_image_string(const BYTE* image, size_t image_size, size_t offset,
                       std::string& result) {
    if (offset >= image_size) return false;
    const size_t available = std::min(image_size - offset, kMaxImageStringSize);
    const auto* end = static_cast<const BYTE*>(
            std::memchr(image + offset, 0, available));
    if (!end) return false;
    result.assign(reinterpret_cast<const char*>(image + offset),
                  static_cast<size_t>(end - (image + offset)));
    return true;
}

bool validate_image(const void* bytes, size_t size,
                    const IMAGE_NT_HEADERS*& nt,
                    const IMAGE_SECTION_HEADER*& sections,
                    std::wstring& err) {
    if (!bytes || size < sizeof(IMAGE_DOS_HEADER)) {
        err = L"DLL payload too small";
        return false;
    }

    const auto* data = static_cast<const BYTE*>(bytes);
    const auto* dos = reinterpret_cast<const IMAGE_DOS_HEADER*>(data);
    if (dos->e_magic != IMAGE_DOS_SIGNATURE) {
        err = L"Bad DOS signature";
        return false;
    }
    if (dos->e_lfanew < static_cast<LONG>(sizeof(IMAGE_DOS_HEADER))) {
        err = L"Invalid PE header offset";
        return false;
    }

    const size_t nt_offset = static_cast<size_t>(dos->e_lfanew);
    constexpr size_t fixed_nt_size = offsetof(IMAGE_NT_HEADERS, OptionalHeader);
    if (!range_within(nt_offset, fixed_nt_size, size)) {
        err = L"Truncated PE file header";
        return false;
    }
    nt = reinterpret_cast<const IMAGE_NT_HEADERS*>(data + nt_offset);
    if (nt->Signature != IMAGE_NT_SIGNATURE) {
        err = L"Bad NT signature";
        return false;
    }
    if (nt->FileHeader.Machine != IMAGE_FILE_MACHINE_AMD64) {
        err = L"DLL is not x64 (only AMD64 supported)";
        return false;
    }
    if (nt->FileHeader.SizeOfOptionalHeader < sizeof(IMAGE_OPTIONAL_HEADER64)) {
        err = L"Truncated PE optional header";
        return false;
    }
    if (!range_within(nt_offset, fixed_nt_size + nt->FileHeader.SizeOfOptionalHeader, size)) {
        err = L"PE optional header is outside the DLL";
        return false;
    }
    if (nt->OptionalHeader.Magic != IMAGE_NT_OPTIONAL_HDR64_MAGIC) {
        err = L"DLL does not contain a PE32+ optional header";
        return false;
    }
    if (nt->OptionalHeader.NumberOfRvaAndSizes > IMAGE_NUMBEROF_DIRECTORY_ENTRIES) {
        err = L"Invalid PE data directory count";
        return false;
    }
    if (nt->FileHeader.NumberOfSections == 0) {
        err = L"DLL has no sections";
        return false;
    }

    const size_t section_offset = nt_offset + fixed_nt_size + nt->FileHeader.SizeOfOptionalHeader;
    const size_t section_bytes = static_cast<size_t>(nt->FileHeader.NumberOfSections)
            * sizeof(IMAGE_SECTION_HEADER);
    if (!range_within(section_offset, section_bytes, size)) {
        err = L"Truncated PE section table";
        return false;
    }
    sections = reinterpret_cast<const IMAGE_SECTION_HEADER*>(data + section_offset);

    const size_t image_size = nt->OptionalHeader.SizeOfImage;
    const size_t headers_size = nt->OptionalHeader.SizeOfHeaders;
    if (image_size == 0 || image_size > kMaxMappedImageSize
            || headers_size == 0 || headers_size > image_size
            || headers_size > size || nt->OptionalHeader.AddressOfEntryPoint == 0
            || nt->OptionalHeader.AddressOfEntryPoint >= image_size) {
        err = L"Invalid PE image or header size";
        return false;
    }
    if (headers_size < section_offset + section_bytes) {
        err = L"PE headers do not contain the complete section table";
        return false;
    }

    for (WORD i = 0; i < nt->FileHeader.NumberOfSections; ++i) {
        const auto& section = sections[i];
        const size_t rva = section.VirtualAddress;
        const size_t extent = std::max<size_t>(section.Misc.VirtualSize,
                                               section.SizeOfRawData);
        if (rva > image_size || extent > image_size - rva) {
            err = L"PE section exceeds the mapped image";
            return false;
        }
        if (section.SizeOfRawData != 0
                && !range_within(section.PointerToRawData, section.SizeOfRawData, size)) {
            err = L"PE section data is outside the DLL";
            return false;
        }
    }

    for (WORD index : {IMAGE_DIRECTORY_ENTRY_IMPORT, IMAGE_DIRECTORY_ENTRY_BASERELOC}) {
        if (nt->OptionalHeader.NumberOfRvaAndSizes <= index) continue;
        const auto& directory = nt->OptionalHeader.DataDirectory[index];
        if (directory.Size != 0
                && !range_within(directory.VirtualAddress, directory.Size, image_size)) {
            err = L"PE data directory exceeds the mapped image";
            return false;
        }
    }
    return true;
}

HMODULE find_remote_module(HANDLE process, const wchar_t* name) {
    std::vector<HMODULE> modules(128);
    for (int attempt = 0; attempt < 8; ++attempt) {
        DWORD needed = 0;
        const DWORD capacity = static_cast<DWORD>(modules.size() * sizeof(HMODULE));
        const BOOL enumerated = EnumProcessModulesEx(
                process, modules.data(), capacity, &needed, LIST_MODULES_ALL);
        if (needed > capacity) {
            const size_t requested = (static_cast<size_t>(needed) + sizeof(HMODULE) - 1)
                    / sizeof(HMODULE);
            if (requested > kMaxRemoteModules) return nullptr;
            modules.resize(requested);
            continue;
        }
        if (!enumerated) return nullptr;
        const size_t count = needed / sizeof(HMODULE);
        for (size_t i = 0; i < count; ++i) {
            wchar_t buf[MAX_PATH]{};
            if (GetModuleBaseNameW(process, modules[i], buf, MAX_PATH)
                    && _wcsicmp(buf, name) == 0) {
                return modules[i];
            }
        }
        return nullptr;
    }
    return nullptr;
}

// Run LoadLibraryW(name) inside the target process and return the resulting
// HMODULE seen by that process, or nullptr on failure.
HMODULE remote_load_library(HANDLE process, const wchar_t* dll_name) {
    SIZE_T sz = (std::wcslen(dll_name) + 1) * sizeof(wchar_t);
    LPVOID arg = VirtualAllocEx(process, nullptr, sz,
            MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
    if (!arg) return nullptr;
    SIZE_T written = 0;
    if (!WriteProcessMemory(process, arg, dll_name, sz, &written) || written != sz) {
        VirtualFreeEx(process, arg, 0, MEM_RELEASE);
        return nullptr;
    }
    FARPROC local_load_library = GetProcAddress(
            GetModuleHandleW(L"kernel32.dll"), "LoadLibraryW");
    HMODULE local_owner = nullptr;
    if (!local_load_library
            || !GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS
                                   | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
                                   reinterpret_cast<LPCWSTR>(local_load_library),
                                   &local_owner)
            || !local_owner) {
        VirtualFreeEx(process, arg, 0, MEM_RELEASE);
        return nullptr;
    }
    wchar_t owner_name[MAX_PATH]{};
    if (!GetModuleBaseNameW(GetCurrentProcess(), local_owner, owner_name, MAX_PATH)) {
        VirtualFreeEx(process, arg, 0, MEM_RELEASE);
        return nullptr;
    }
    HMODULE remote_owner = find_remote_module(process, owner_name);
    if (!remote_owner) {
        VirtualFreeEx(process, arg, 0, MEM_RELEASE);
        return nullptr;
    }
    auto load_lib = reinterpret_cast<LPTHREAD_START_ROUTINE>(
            reinterpret_cast<ULONGLONG>(remote_owner)
            + (reinterpret_cast<ULONGLONG>(local_load_library)
               - reinterpret_cast<ULONGLONG>(local_owner)));
    HANDLE thread = CreateRemoteThread(process, nullptr, 0, load_lib, arg, 0, nullptr);
    if (!thread) {
        VirtualFreeEx(process, arg, 0, MEM_RELEASE);
        return nullptr;
    }
    const DWORD wait = WaitForSingleObject(thread, 10000);
    if (wait != WAIT_OBJECT_0) {
        // LoadLibraryW may still be reading arg. Leave it allocated until the
        // target exits rather than freeing memory still owned by its thread.
        CloseHandle(thread);
        return nullptr;
    }
    DWORD ret = 0;
    const BOOL got_exit_code = GetExitCodeThread(thread, &ret);
    CloseHandle(thread);
    VirtualFreeEx(process, arg, 0, MEM_RELEASE);
    // On x64 GetExitCodeThread returns a DWORD which truncates 64-bit HMODULE.
    // If LoadLibraryW succeeded (ret != 0), resolve the full 64-bit base address via enumeration.
    if (got_exit_code && ret != 0) {
        return find_remote_module(process, dll_name);
    }
    return nullptr;
}

bool apply_relocations(BYTE* image, size_t image_size,
                       const IMAGE_NT_HEADERS* nt, ULONGLONG delta,
                       std::wstring& err) {
    if (delta == 0) return true;
    if (nt->OptionalHeader.NumberOfRvaAndSizes <= IMAGE_DIRECTORY_ENTRY_BASERELOC) {
        err = L"DLL has no base relocation directory";
        return false;
    }
    const auto& dir = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_BASERELOC];
    if (dir.Size == 0) {
        err = L"DLL has no base relocations for the selected address";
        return false;
    }

    size_t offset = dir.VirtualAddress;
    size_t remaining = dir.Size;
    while (remaining != 0) {
        if (remaining < sizeof(IMAGE_BASE_RELOCATION)) {
            err = L"Truncated base relocation block";
            return false;
        }
        IMAGE_BASE_RELOCATION block{};
        std::memcpy(&block, image + offset, sizeof(block));
        if (block.SizeOfBlock < sizeof(IMAGE_BASE_RELOCATION)
                || block.SizeOfBlock > remaining
                || (block.SizeOfBlock - sizeof(IMAGE_BASE_RELOCATION)) % sizeof(WORD) != 0) {
            err = L"Invalid base relocation block size";
            return false;
        }
        const size_t count = (block.SizeOfBlock - sizeof(IMAGE_BASE_RELOCATION)) / sizeof(WORD);
        const size_t entries_offset = offset + sizeof(IMAGE_BASE_RELOCATION);
        for (size_t i = 0; i < count; ++i) {
            WORD entry = 0;
            std::memcpy(&entry, image + entries_offset + i * sizeof(WORD), sizeof(entry));
            WORD type = entry >> 12;
            WORD off = entry & 0x0FFF;
            const size_t target = static_cast<size_t>(block.VirtualAddress) + off;
            if (type == IMAGE_REL_BASED_DIR64) {
                if (!range_within(target, sizeof(ULONGLONG), image_size)) {
                    err = L"64-bit relocation target exceeds the mapped image";
                    return false;
                }
                ULONGLONG value = 0;
                std::memcpy(&value, image + target, sizeof(value));
                value += delta;
                std::memcpy(image + target, &value, sizeof(value));
            } else if (type == IMAGE_REL_BASED_HIGHLOW) {
                if (!range_within(target, sizeof(DWORD), image_size)) {
                    err = L"32-bit relocation target exceeds the mapped image";
                    return false;
                }
                DWORD value = 0;
                std::memcpy(&value, image + target, sizeof(value));
                value += static_cast<DWORD>(delta);
                std::memcpy(image + target, &value, sizeof(value));
            }
            // IMAGE_REL_BASED_ABSOLUTE (0) is a padding entry; ignore.
        }
        offset += block.SizeOfBlock;
        remaining -= block.SizeOfBlock;
    }
    return true;
}

bool resolve_imports(HANDLE process, BYTE* local_image, size_t image_size,
                     const IMAGE_NT_HEADERS* nt, std::wstring& err) {
    if (nt->OptionalHeader.NumberOfRvaAndSizes <= IMAGE_DIRECTORY_ENTRY_IMPORT)
        return true;
    const auto& dir = nt->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT];
    if (dir.Size == 0) return true;

    const size_t descriptor_count = dir.Size / sizeof(IMAGE_IMPORT_DESCRIPTOR);
    if (descriptor_count > kMaxImportDescriptors) {
        err = L"Import descriptor table is too large";
        return false;
    }
    bool terminated = false;
    for (size_t descriptor_index = 0; descriptor_index < descriptor_count; ++descriptor_index) {
        const size_t descriptor_offset = static_cast<size_t>(dir.VirtualAddress)
                + descriptor_index * sizeof(IMAGE_IMPORT_DESCRIPTOR);
        IMAGE_IMPORT_DESCRIPTOR desc{};
        std::memcpy(&desc, local_image + descriptor_offset, sizeof(desc));
        if (desc.Name == 0) {
            terminated = true;
            break;
        }

        std::string dll_name_ansi;
        if (!read_image_string(local_image, image_size, desc.Name, dll_name_ansi)) {
            err = L"Invalid imported DLL name";
            return false;
        }
        wchar_t dll_name_w[MAX_PATH] = {0};
        if (MultiByteToWideChar(CP_ACP, 0, dll_name_ansi.c_str(), -1,
                                dll_name_w, MAX_PATH) == 0) {
            err = L"Could not convert imported DLL name";
            return false;
        }

        HMODULE remote_mod = find_remote_module(process, dll_name_w);
        if (!remote_mod) {
            remote_mod = remote_load_library(process, dll_name_w);
        }
        if (!remote_mod) {
            std::wstringstream ss;
            ss << L"Remote LoadLibrary failed for dependency " << dll_name_w;
            err = ss.str();
            return false;
        }

        // Resolve exports against the loader's local dependency copy, then
        // translate from the actual owner module (which may differ when an
        // export is forwarded) to that module's base in the target process.
        HMODULE local_mod = GetModuleHandleA(dll_name_ansi.c_str());
        bool local_mod_loaded = false;
        if (!local_mod) {
            local_mod = LoadLibraryA(dll_name_ansi.c_str());
            local_mod_loaded = local_mod != nullptr;
        }
        if (!local_mod) {
            std::wstringstream ss;
            ss << L"Local LoadLibrary failed for dependency " << dll_name_w;
            err = ss.str();
            return false;
        }

        const DWORD thunk_rva = desc.OriginalFirstThunk ? desc.OriginalFirstThunk : desc.FirstThunk;
        bool thunk_terminated = false;
        for (size_t thunk_index = 0; thunk_index < kMaxImportThunks; ++thunk_index) {
            const size_t thunk_offset = static_cast<size_t>(thunk_rva)
                    + thunk_index * sizeof(IMAGE_THUNK_DATA);
            const size_t iat_offset = static_cast<size_t>(desc.FirstThunk)
                    + thunk_index * sizeof(IMAGE_THUNK_DATA);
            if (!range_within(thunk_offset, sizeof(IMAGE_THUNK_DATA), image_size)
                    || !range_within(iat_offset, sizeof(IMAGE_THUNK_DATA), image_size)) {
                err = L"Import thunk exceeds the mapped image";
                if (local_mod_loaded) FreeLibrary(local_mod);
                return false;
            }
            IMAGE_THUNK_DATA thunk{};
            std::memcpy(&thunk, local_image + thunk_offset, sizeof(thunk));
            if (thunk.u1.AddressOfData == 0) {
                thunk_terminated = true;
                break;
            }

            FARPROC local_fn = nullptr;
            if (IMAGE_SNAP_BY_ORDINAL(thunk.u1.Ordinal)) {
                local_fn = GetProcAddress(local_mod,
                        reinterpret_cast<LPCSTR>(IMAGE_ORDINAL(thunk.u1.Ordinal)));
            } else {
                const size_t import_name_offset = static_cast<size_t>(thunk.u1.AddressOfData);
                std::string import_name;
                if (!range_within(import_name_offset, sizeof(WORD), image_size)
                        || !read_image_string(local_image, image_size,
                                              import_name_offset + sizeof(WORD), import_name)) {
                    err = L"Invalid import name entry";
                    if (local_mod_loaded) FreeLibrary(local_mod);
                    return false;
                }
                local_fn = GetProcAddress(local_mod, import_name.c_str());
            }

            if (!local_fn) {
                err = L"Could not resolve an imported function";
                if (local_mod_loaded) FreeLibrary(local_mod);
                return false;
            }

            HMODULE local_owner = nullptr;
            if (!GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS
                                    | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
                                    reinterpret_cast<LPCWSTR>(local_fn), &local_owner)
                    || !local_owner) {
                err = L"Could not locate the owner of an imported function";
                if (local_mod_loaded) FreeLibrary(local_mod);
                return false;
            }
            wchar_t owner_name[MAX_PATH]{};
            if (!GetModuleBaseNameW(GetCurrentProcess(), local_owner,
                                    owner_name, MAX_PATH)) {
                err = L"Could not name the owner of an imported function";
                if (local_mod_loaded) FreeLibrary(local_mod);
                return false;
            }
            HMODULE remote_owner = find_remote_module(process, owner_name);
            if (!remote_owner) remote_owner = remote_load_library(process, owner_name);
            if (!remote_owner) {
                err = L"Could not locate the remote owner of an imported function";
                if (local_mod_loaded) FreeLibrary(local_mod);
                return false;
            }

            ULONGLONG remote_fn = reinterpret_cast<ULONGLONG>(remote_owner)
                    + (reinterpret_cast<ULONGLONG>(local_fn)
                       - reinterpret_cast<ULONGLONG>(local_owner));
            IMAGE_THUNK_DATA iat{};
            iat.u1.Function = remote_fn;
            std::memcpy(local_image + iat_offset, &iat, sizeof(iat));
        }
        if (local_mod_loaded) FreeLibrary(local_mod);
        if (!thunk_terminated) {
            err = L"Import thunk table has no terminator";
            return false;
        }
    }
    if (!terminated) {
        err = L"Import descriptor table has no terminator";
        return false;
    }
    return true;
}

DWORD section_protection(DWORD characteristics) {
    bool x = (characteristics & IMAGE_SCN_MEM_EXECUTE) != 0;
    bool r = (characteristics & IMAGE_SCN_MEM_READ) != 0;
    bool w = (characteristics & IMAGE_SCN_MEM_WRITE) != 0;
    if (x && r && w) return PAGE_EXECUTE_READWRITE;
    if (x && r) return PAGE_EXECUTE_READ;
    if (x) return PAGE_EXECUTE;
    if (r && w) return PAGE_READWRITE;
    if (r) return PAGE_READONLY;
    return PAGE_NOACCESS;
}

} // namespace

std::wstring inject_in_memory(DWORD pid, const void* dll_bytes, size_t dll_size) {
    const IMAGE_NT_HEADERS* nt = nullptr;
    const IMAGE_SECTION_HEADER* sections = nullptr;
    std::wstring validation_error;
    if (!validate_image(dll_bytes, dll_size, nt, sections, validation_error))
        return validation_error;

    const SIZE_T image_size = static_cast<SIZE_T>(nt->OptionalHeader.SizeOfImage);
    std::vector<BYTE> local_image;
    try {
        local_image.assign(image_size, 0);
    } catch (const std::exception&) {
        return L"Could not allocate the local PE image";
    }
    std::memcpy(local_image.data(), dll_bytes, nt->OptionalHeader.SizeOfHeaders);

    for (WORD i = 0; i < nt->FileHeader.NumberOfSections; ++i) {
        const auto& section = sections[i];
        if (section.SizeOfRawData == 0) continue;
        std::memcpy(local_image.data() + section.VirtualAddress,
                    static_cast<const BYTE*>(dll_bytes) + section.PointerToRawData,
                    section.SizeOfRawData);
    }

    HANDLE process = OpenProcess(
            PROCESS_CREATE_THREAD | PROCESS_QUERY_INFORMATION |
            PROCESS_VM_OPERATION | PROCESS_VM_WRITE | PROCESS_VM_READ,
            FALSE, pid);
    if (!process) return fmt_err(L"OpenProcess", GetLastError());

    LPVOID remote_image = VirtualAllocEx(process, nullptr, image_size,
            MEM_COMMIT | MEM_RESERVE, PAGE_EXECUTE_READWRITE);
    if (!remote_image) {
        DWORD err = GetLastError();
        CloseHandle(process);
        return fmt_err(L"VirtualAllocEx (image)", err);
    }

    ULONGLONG delta = reinterpret_cast<ULONGLONG>(remote_image) -
            nt->OptionalHeader.ImageBase;
    std::wstring relocation_error;
    if (!apply_relocations(local_image.data(), image_size, nt, delta,
                           relocation_error)) {
        VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
        CloseHandle(process);
        return relocation_error;
    }

    std::wstring imp_err;
    if (!resolve_imports(process, local_image.data(), image_size, nt, imp_err)) {
        VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
        CloseHandle(process);
        return imp_err;
    }

    SIZE_T written = 0;
    if (!WriteProcessMemory(process, remote_image, local_image.data(),
                            image_size, &written) || written != image_size) {
        DWORD err = GetLastError();
        VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
        CloseHandle(process);
        return fmt_err(L"WriteProcessMemory (image)", err);
    }

    // Tighten section permissions to match the original PE characteristics.
    for (WORD i = 0; i < nt->FileHeader.NumberOfSections; ++i) {
        const auto& section = sections[i];
        const SIZE_T section_size = std::max<SIZE_T>(section.Misc.VirtualSize,
                                                     section.SizeOfRawData);
        if (section_size == 0) continue;
        DWORD prot = section_protection(section.Characteristics);
        DWORD old = 0;
        if (!VirtualProtectEx(process,
                static_cast<BYTE*>(remote_image) + section.VirtualAddress,
                section_size, prot, &old)) {
            DWORD err = GetLastError();
            VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
            CloseHandle(process);
            return fmt_err(L"VirtualProtectEx (section)", err);
        }
    }

    // CreateRemoteThread can only deliver one pointer-sized arg, so we drop a
    // tiny x64 trampoline that calls
    //   DllMain(hModule = remote_image,
    //           fdwReason = DLL_PROCESS_ATTACH,
    //           lpvReserved = NULL)
    // through the standard MSVC ABI before returning.
    BYTE shellcode[] = {
        0x48, 0xB9, 0,0,0,0,0,0,0,0,  // mov rcx, imm64 (hModule)
        0xBA, 0x01, 0x00, 0x00, 0x00, // mov edx, 1     (DLL_PROCESS_ATTACH)
        0x4D, 0x31, 0xC0,             // xor r8, r8     (lpvReserved)
        0x48, 0xB8, 0,0,0,0,0,0,0,0,  // mov rax, imm64 (entry point)
        0x48, 0x83, 0xEC, 0x28,       // sub rsp, 0x28  (16 + shadow space)
        0xFF, 0xD0,                   // call rax
        0x48, 0x83, 0xC4, 0x28,       // add rsp, 0x28
        0xC3                          // ret
    };
    ULONGLONG hmod = reinterpret_cast<ULONGLONG>(remote_image);
    ULONGLONG entry = hmod + nt->OptionalHeader.AddressOfEntryPoint;
    std::memcpy(shellcode + 2, &hmod, sizeof hmod);
    std::memcpy(shellcode + 20, &entry, sizeof entry);

    LPVOID remote_sc = VirtualAllocEx(process, nullptr, sizeof shellcode,
            MEM_COMMIT | MEM_RESERVE, PAGE_EXECUTE_READWRITE);
    if (!remote_sc) {
        DWORD err = GetLastError();
        VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
        CloseHandle(process);
        return fmt_err(L"VirtualAllocEx (shellcode)", err);
    }
    if (!WriteProcessMemory(process, remote_sc, shellcode, sizeof shellcode, &written)
            || written != sizeof shellcode) {
        DWORD err = GetLastError();
        VirtualFreeEx(process, remote_sc, 0, MEM_RELEASE);
        VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
        CloseHandle(process);
        return fmt_err(L"WriteProcessMemory (shellcode)", err);
    }

    HANDLE thread = CreateRemoteThread(process, nullptr, 0,
            reinterpret_cast<LPTHREAD_START_ROUTINE>(remote_sc), nullptr, 0, nullptr);
    if (!thread) {
        DWORD err = GetLastError();
        VirtualFreeEx(process, remote_sc, 0, MEM_RELEASE);
        VirtualFreeEx(process, remote_image, 0, MEM_RELEASE);
        CloseHandle(process);
        return fmt_err(L"CreateRemoteThread", err);
    }
    const DWORD wait = WaitForSingleObject(thread, 30000);
    if (wait != WAIT_OBJECT_0) {
        // The initializer may still be executing both remote allocations.
        // Keep them alive until the target exits instead of causing a use-after-free.
        CloseHandle(thread);
        CloseHandle(process);
        return L"Timed out waiting for the target initializer; its memory was kept allocated";
    }
    CloseHandle(thread);

    VirtualFreeEx(process, remote_sc, 0, MEM_RELEASE);
    // Leave remote_image allocated - the DLL stays mapped in the target's
    // address space for the lifetime of the process.
    CloseHandle(process);
    return L"";
}

} // namespace loader
