cmake_minimum_required(VERSION 3.20)
cmake_policy(SET CMP0091 NEW)
project(OpenHachimiNative LANGUAGES CXX RC)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")

if(NOT DEFINED ENV{JAVA_HOME})
    message(FATAL_ERROR "JAVA_HOME must point to a JDK so jni.h and jvmti.h can be found")
endif()
if(NOT JAR_SOURCE OR NOT EXISTS "${JAR_SOURCE}")
    message(FATAL_ERROR "JAR_SOURCE must point to the built OpenHachimi jar")
endif()

set(JDK_INCLUDE "$ENV{JAVA_HOME}/include")
include_directories("${JDK_INCLUDE}" "${JDK_INCLUDE}/win32")

file(TO_CMAKE_PATH "${JAR_SOURCE}" JAR_SOURCE_RC)
configure_file(res/payload.rc.in "${CMAKE_CURRENT_BINARY_DIR}/payload.rc" @ONLY)

add_library(OpenHachimi SHARED
    src/main.cpp
    src/jvm_attach.cpp
    src/jar_extract.cpp
    src/class_loader.cpp
    "${CMAKE_CURRENT_BINARY_DIR}/payload.rc"
)

target_include_directories(OpenHachimi PRIVATE
    "${CMAKE_CURRENT_SOURCE_DIR}/src"
    "${CMAKE_CURRENT_SOURCE_DIR}/res"
    "${CMAKE_CURRENT_BINARY_DIR}"
)
target_compile_definitions(OpenHachimi PRIVATE WIN32_LEAN_AND_MEAN NOMINMAX)
target_link_libraries(OpenHachimi PRIVATE advapi32)

set_target_properties(OpenHachimi PROPERTIES
    OUTPUT_NAME "OpenHachimi"
    PREFIX ""
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/bin"
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/bin"
    RUNTIME_OUTPUT_DIRECTORY_RELEASE "${CMAKE_CURRENT_BINARY_DIR}/bin"
    LIBRARY_OUTPUT_DIRECTORY_RELEASE "${CMAKE_CURRENT_BINARY_DIR}/bin"
)
