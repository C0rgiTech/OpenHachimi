cmake_minimum_required(VERSION 3.20)
cmake_policy(SET CMP0091 NEW)
project(OpenHachimiInject LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")

add_executable(OpenHachimiInject
    main.cpp
    manual_map.cpp
)

target_include_directories(OpenHachimiInject PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}")
target_compile_definitions(OpenHachimiInject PRIVATE
    UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX
)
target_link_libraries(OpenHachimiInject PRIVATE psapi)
set_target_properties(OpenHachimiInject PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/bin"
    RUNTIME_OUTPUT_DIRECTORY_RELEASE "${CMAKE_CURRENT_BINARY_DIR}/bin"
)
