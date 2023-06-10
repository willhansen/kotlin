/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.target.CompilerOutputKind

class KonanConfigKeys {
    companion object {
        // Keep the list lexically sorted.
        konst BUNDLE_ID: CompilerConfigurationKey<String>
                = CompilerConfigurationKey.create("bundle ID to be set in Info.plist of a produced framework")
        konst CHECK_DEPENDENCIES: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("check dependencies and download the missing ones")
        konst DEBUG: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("add debug information")
        konst FAKE_OVERRIDE_VALIDATOR: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("fake override konstidator")
        konst BITCODE_EMBEDDING_MODE: CompilerConfigurationKey<BitcodeEmbedding.Mode>
                = CompilerConfigurationKey.create("bitcode embedding mode")
        konst EMIT_LAZY_OBJC_HEADER_FILE: CompilerConfigurationKey<String?> =
                CompilerConfigurationKey.create("output file to emit lazy Obj-C header")
        konst ENABLE_ASSERTIONS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("enable runtime assertions in generated code")
        konst ENTRY: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("fully qualified main() name")
        konst EXPORTED_LIBRARIES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("libraries included into produced framework API")
        konst FULL_EXPORTED_NAME_PREFIX: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("prefix used when exporting Kotlin names to other languages")
        konst LIBRARY_TO_ADD_TO_CACHE: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create<String?>("path to library that to be added to cache")
        konst CACHE_DIRECTORIES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("paths to directories containing caches")
        konst AUTO_CACHEABLE_FROM: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("paths to the root directories from which dependencies are to be cached automatically")
        konst AUTO_CACHE_DIR: CompilerConfigurationKey<String>
                = CompilerConfigurationKey.create<String>("path to the directory where to put caches for auto-cacheable dependencies")
        konst INCREMENTAL_CACHE_DIR: CompilerConfigurationKey<String>
                = CompilerConfigurationKey.create<String>("path to the directory where to put incremental build caches")
        konst CACHED_LIBRARIES: CompilerConfigurationKey<Map<String, String>>
                = CompilerConfigurationKey.create<Map<String, String>>("mapping from library paths to cache paths")
        konst FILES_TO_CACHE: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("which files should be compiled to cache")
        konst MAKE_PER_FILE_CACHE: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create<Boolean>("make per-file cache")
        konst FRAMEWORK_IMPORT_HEADERS: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("headers imported to framework header")
        konst FRIEND_MODULES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("friend module paths")
        konst REFINES_MODULES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create<List<String>>("refines module paths")
        konst GENERATE_TEST_RUNNER: CompilerConfigurationKey<TestRunnerKind>
                = CompilerConfigurationKey.create("generate test runner") 
        konst INCLUDED_BINARY_FILES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("included binary file paths")
        konst KONAN_HOME: CompilerConfigurationKey<String>
                = CompilerConfigurationKey.create("overridden compiler distribution path")
        konst LIBRARY_FILES: CompilerConfigurationKey<List<String>> 
                = CompilerConfigurationKey.create("library file paths")
        konst LIBRARY_VERSION: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("library version")
        konst LIGHT_DEBUG: CompilerConfigurationKey<Boolean?>
                = CompilerConfigurationKey.create("add light debug information")
        konst GENERATE_DEBUG_TRAMPOLINE: CompilerConfigurationKey<Boolean?>
                = CompilerConfigurationKey.create("generates debug trampolines to make debugger breakpoint resolution more accurate")
        konst LINKER_ARGS: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("additional linker arguments")
        konst LIST_TARGETS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("list available targets")
        konst MANIFEST_FILE: CompilerConfigurationKey<String?> 
                = CompilerConfigurationKey.create("provide manifest addend file")
        konst METADATA_KLIB: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("metadata klib")
        konst MODULE_NAME: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("module name")
        konst NATIVE_LIBRARY_FILES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("native library file paths")
        konst NODEFAULTLIBS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("don't link with the default libraries")
        konst NOENDORSEDLIBS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("don't link with the endorsed libraries")
        konst NOMAIN: CompilerConfigurationKey<Boolean> 
                = CompilerConfigurationKey.create("assume 'main' entry point to be provided by external libraries")
        konst NOSTDLIB: CompilerConfigurationKey<Boolean> 
                = CompilerConfigurationKey.create("don't link with stdlib")
        konst NOPACK: CompilerConfigurationKey<Boolean> 
                = CompilerConfigurationKey.create("don't the library into a klib file")
        konst OPTIMIZATION: CompilerConfigurationKey<Boolean> 
                = CompilerConfigurationKey.create("optimized compilation")
        konst OUTPUT: CompilerConfigurationKey<String> 
                = CompilerConfigurationKey.create("program or library name")
        konst OVERRIDE_CLANG_OPTIONS: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("arguments for clang")
        konst ALLOCATION_MODE: CompilerConfigurationKey<AllocationMode>
                = CompilerConfigurationKey.create("allocation mode")
        konst EXPORT_KDOC: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("export KDoc into klib and framework")
        konst PRINT_BITCODE: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("print bitcode")
        konst CHECK_EXTERNAL_CALLS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("check external calls")
        konst PRINT_IR: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("print ir")
        konst PRINT_FILES: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("print files")
        konst PRODUCE: CompilerConfigurationKey<CompilerOutputKind>
                = CompilerConfigurationKey.create("compiler output kind")
        konst PURGE_USER_LIBS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("purge user-specified libs too")
        konst REPOSITORIES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("library search path repositories")
        konst RUNTIME_FILE: CompilerConfigurationKey<String?> 
                = CompilerConfigurationKey.create("override default runtime file path")
        konst INCLUDED_LIBRARIES: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey("klibs processed in the same manner as source files")
        konst SHORT_MODULE_NAME: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey("short module name for IDE and export")
        konst STATIC_FRAMEWORK: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("produce a static library for a framework")
        konst TARGET: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("target we compile for")
        konst TEMPORARY_FILES_DIR: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("directory for temporary files")
        konst SAVE_LLVM_IR: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("save LLVM IR")
        konst VERIFY_BITCODE: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("verify bitcode")
        konst VERIFY_IR: CompilerConfigurationKey<IrVerificationMode>
                = CompilerConfigurationKey.create("IR verification mode")
        konst VERIFY_COMPILER: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("verify compiler")
        konst DEBUG_INFO_VERSION: CompilerConfigurationKey<Int>
                = CompilerConfigurationKey.create("debug info format version")
        konst COVERAGE: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("emit coverage info for sources")
        konst LIBRARIES_TO_COVER: CompilerConfigurationKey<List<String>>
                = CompilerConfigurationKey.create("libraries that should be covered")
        konst PROFRAW_PATH: CompilerConfigurationKey<String?>
                = CompilerConfigurationKey.create("path to *.profraw coverage output")
        konst OBJC_GENERICS: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("write objc header with generics support")
        konst DEBUG_PREFIX_MAP: CompilerConfigurationKey<Map<String, String>>
                = CompilerConfigurationKey.create("remap file source paths in debug info")
        konst PRE_LINK_CACHES: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("perform compiler caches pre-link")
        konst OVERRIDE_KONAN_PROPERTIES: CompilerConfigurationKey<Map<String, String>>
                = CompilerConfigurationKey.create("override konan.properties konstues")
        konst DESTROY_RUNTIME_MODE: CompilerConfigurationKey<DestroyRuntimeMode>
                = CompilerConfigurationKey.create("when to destroy runtime")
        konst PROPERTY_LAZY_INITIALIZATION: CompilerConfigurationKey<Boolean>
                = CompilerConfigurationKey.create("lazy top level properties initialization")
        konst WORKER_EXCEPTION_HANDLING: CompilerConfigurationKey<WorkerExceptionHandling> = CompilerConfigurationKey.create("unhandled exception processing in Worker.executeAfter")
        konst EXTERNAL_DEPENDENCIES: CompilerConfigurationKey<String?> =
                CompilerConfigurationKey.create("use external dependencies to enhance IR linker error messages")
        konst LLVM_VARIANT: CompilerConfigurationKey<LlvmVariant?> = CompilerConfigurationKey.create("llvm variant")
        konst RUNTIME_LOGS: CompilerConfigurationKey<String> = CompilerConfigurationKey.create("enable runtime logging")
        konst LAZY_IR_FOR_CACHES: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create("use lazy IR for cached libraries")
        konst TEST_DUMP_OUTPUT_PATH: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("path to a file to dump the list of all available tests")
        konst OMIT_FRAMEWORK_BINARY: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create("do not generate binary in framework")
        konst COMPILE_FROM_BITCODE: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("path to bitcode file to compile")
        konst SERIALIZED_DEPENDENCIES: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("path to serialized dependencies for native linking")
        konst SAVE_DEPENDENCIES_PATH: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("path to save serialized dependencies to")
        konst SAVE_LLVM_IR_DIRECTORY: CompilerConfigurationKey<String?> = CompilerConfigurationKey.create("directory to store LLVM IR from phases")
    }
}

