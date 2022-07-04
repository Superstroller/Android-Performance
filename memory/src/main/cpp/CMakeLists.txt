
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("oom")

add_compile_options(-Wall -Wextra -Wexit-time-destructors -D_FILE_OFFSET_BITS=64 -D_7ZIP_ST)
#liblog
add_compile_options(-DLIBLOG_LOG_TAG=1006 -DSNET_EVENT_LOG_TAG=1397638484 -Oz)

set(XHOOK_BASE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../xhook)

include_directories(${CMAKE_SOURCE_DIR}/include
        ${CMAKE_SOURCE_DIR}/lzma
        ${XHOOK_BASE_DIR}/src/main/cpp/include)

#设置生成的so动态库最后输出到jniLibs目录
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

LINK_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/${ANDROID_ABI})

set(LZMA_SOURCES lzma/7zAlloc.c lzma/7zArcIn.c lzma/7zBuf2.c lzma/7zBuf.c lzma/7zCrc.c lzma/7zCrcOpt.c
        lzma/7zDec.c lzma/7zFile.c lzma/7zStream.c lzma/Aes.c lzma/AesOpt.c lzma/Alloc.c lzma/Bcj2.c
        lzma/Bra86.c lzma/Bra.c lzma/BraIA64.c lzma/CpuArch.c lzma/Delta.c lzma/LzFind.c
        lzma/Lzma2Dec.c lzma/Lzma2Enc.c lzma/Lzma86Dec.c lzma/Lzma86Enc.c lzma/LzmaDec.c lzma/LzmaEnc.c
        lzma/LzmaLib.c lzma/Ppmd7.c lzma/Ppmd7Dec.c lzma/Ppmd7Enc.c lzma/Sha256.c lzma/Sort.c
        lzma/Xz.c lzma/XzCrc64.c lzma/XzCrc64Opt.c lzma/XzDec.c lzma/XzEnc.c lzma/XzIn.c)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             oom

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
            heapdump_bridge.cpp hprof_strip.cpp hprof_dump.cpp elf_reader.cpp kwai_dlfcn.cpp async_safe_log.cpp ndk_port.cpp prop_info.cpp ${LZMA_SOURCES})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       oom

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} xhook)
