/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.*

class K2NativeCompilerArguments : CommonCompilerArguments() {
    // First go the options interesting to the general public.
    // Prepend them with a single dash.
    // Keep the list lexically sorted.

    @Argument(konstue = "-enable-assertions", deprecatedName = "-enable_assertions", shortName = "-ea", description = "Enable runtime assertions in generated code")
    var enableAssertions: Boolean = false

    @Argument(konstue = "-g", description = "Enable emitting debug information")
    var debug: Boolean = false

    @Argument(
        konstue = "-generate-test-runner",
        deprecatedName = "-generate_test_runner",
        shortName = "-tr", description = "Produce a runner for unit tests"
    )
    var generateTestRunner = false

    @Argument(
        konstue = "-generate-worker-test-runner",
        shortName = "-trw",
        description = "Produce a worker runner for unit tests"
    )
    var generateWorkerTestRunner = false

    @Argument(
        konstue = "-generate-no-exit-test-runner",
        shortName = "-trn",
        description = "Produce a runner for unit tests not forcing exit"
    )
    var generateNoExitTestRunner = false

    @Argument(konstue="-include-binary", deprecatedName = "-includeBinary", shortName = "-ib", konstueDescription = "<path>", description = "Pack external binary within the klib")
    var includeBinaries: Array<String>? = null

    @Argument(konstue = "-library", shortName = "-l", konstueDescription = "<path>", description = "Link with the library", delimiter = Argument.Delimiters.none)
    var libraries: Array<String>? = null

    @Argument(konstue = "-library-version", shortName = "-lv", konstueDescription = "<version>", description = "Set library version")
    var libraryVersion: String? = null

    @Argument(konstue = "-list-targets", deprecatedName = "-list_targets", description = "List available hardware targets")
    var listTargets: Boolean = false

    @Argument(konstue = "-manifest", konstueDescription = "<path>", description = "Provide a maniferst addend file")
    var manifestFile: String? = null

    @Argument(konstue="-memory-model", konstueDescription = "<model>", description = "Memory model to use, 'strict' and 'experimental' are currently supported")
    var memoryModel: String? = null

    @GradleOption(
        konstue = DefaultValue.STRING_NULL_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT
    )
    @Argument(
        konstue = "-module-name",
        deprecatedName = "-module_name",
        konstueDescription = "<name>",
        description = "Specify a name for the compilation module"
    )
    var moduleName: String? = null

    @Argument(
        konstue = "-native-library",
        deprecatedName = "-nativelibrary",
        shortName = "-nl",
        konstueDescription = "<path>",
        description = "Include the native bitcode library", delimiter = Argument.Delimiters.none
    )
    var nativeLibraries: Array<String>? = null

    @Argument(konstue = "-no-default-libs", deprecatedName = "-nodefaultlibs", description = "Don't link the libraries from dist/klib automatically")
    var nodefaultlibs: Boolean = false

    @Argument(
        konstue = "-no-endorsed-libs",
        description = "Don't link the endorsed libraries from dist automatically. " +
                "Deprecated option: the dist has no endorsed libraries anymore."
    )
    var noendorsedlibs: Boolean = false

    @Argument(konstue = "-nomain", description = "Assume 'main' entry point to be provided by external libraries")
    var nomain: Boolean = false

    @Argument(konstue = "-nopack", description = "Don't pack the library into a klib file")
    var nopack: Boolean = false

    @Argument(konstue="-linker-options", deprecatedName = "-linkerOpts", konstueDescription = "<arg>", description = "Pass arguments to linker", delimiter = " ")
    var linkerArguments: Array<String>? = null

    @Argument(konstue="-linker-option", konstueDescription = "<arg>", description = "Pass argument to linker", delimiter = Argument.Delimiters.none)
    var singleLinkerArguments: Array<String>? = null

    @Argument(konstue = "-nostdlib", description = "Don't link with stdlib")
    var nostdlib: Boolean = false

    @Argument(konstue = "-opt", description = "Enable optimizations during compilation")
    var optimization: Boolean = false

    @Argument(konstue = "-output", shortName = "-o", konstueDescription = "<name>", description = "Output name")
    var outputName: String? = null

    @Argument(konstue = "-entry", shortName = "-e", konstueDescription = "<name>", description = "Qualified entry point name")
    var mainPackage: String? = null

    @Argument(
        konstue = "-produce", shortName = "-p",
        konstueDescription = "{program|static|dynamic|framework|library|bitcode}",
        description = "Specify output file kind"
    )
    var produce: String? = null

    @Argument(konstue = "-repo", shortName = "-r", konstueDescription = "<path>", description = "Library search path")
    var repositories: Array<String>? = null

    @Argument(konstue = "-target", konstueDescription = "<target>", description = "Set hardware target")
    var target: String? = null

    // The rest of the options are only interesting to the developers.
    // Make sure to prepend them with -X.
    // Keep the list lexically sorted.

    @Argument(
        konstue = "-Xbundle-id",
        konstueDescription = "<id>",
        description = "Bundle ID to be set in Info.plist of a produced framework. Deprecated. Please use -Xbinary=bundleId=<id>."
    )
    var bundleId: String? = null

    @Argument(
        konstue = "-Xcache-directory",
        konstueDescription = "<path>",
        description = "Path to the directory containing caches",
        delimiter = Argument.Delimiters.none
    )
    var cacheDirectories: Array<String>? = null

    @Argument(
        konstue = CACHED_LIBRARY,
        konstueDescription = "<library path>,<cache path>",
        description = "Comma-separated paths of a library and its cache",
        delimiter = Argument.Delimiters.none
    )
    var cachedLibraries: Array<String>? = null

    @Argument(
        konstue = "-Xauto-cache-from",
        konstueDescription = "<path>",
        description = "Path to the root directory from which dependencies are to be cached automatically.\n" +
                "By default caches will be placed into the kotlin-native system cache directory.",
        delimiter = Argument.Delimiters.none
    )
    var autoCacheableFrom: Array<String>? = null

    @Argument(
        konstue = "-Xauto-cache-dir",
        konstueDescription = "<path>",
        description = "Path to the directory where to put caches for auto-cacheable dependencies",
        delimiter = Argument.Delimiters.none
    )
    var autoCacheDir: String? = null

    @Argument(
        konstue = INCREMENTAL_CACHE_DIR,
        konstueDescription = "<path>",
        description = "Path to the directory where to put incremental build caches",
        delimiter = ""
    )
    var incrementalCacheDir: String? = null

    @Argument(konstue="-Xcheck-dependencies", deprecatedName = "--check_dependencies", description = "Check dependencies and download the missing ones")
    var checkDependencies: Boolean = false

    @Argument(konstue = EMBED_BITCODE_FLAG, description = "Embed LLVM IR bitcode as data")
    var embedBitcode: Boolean = false

    @Argument(konstue = EMBED_BITCODE_MARKER_FLAG, description = "Embed placeholder LLVM IR data as a marker")
    var embedBitcodeMarker: Boolean = false

    @Argument(konstue = "-Xemit-lazy-objc-header", description = "")
    var emitLazyObjCHeader: String? = null

    @Argument(
        konstue = "-Xexport-library",
        konstueDescription = "<path>",
        description = "A library to be included into produced framework API.\n" +
                "Must be one of libraries passed with '-library'",
        delimiter = Argument.Delimiters.none
    )
    var exportedLibraries: Array<String>? = null

    @Argument(
        konstue = "-Xexternal-dependencies",
        konstueDescription = "<path>",
        description = "Path to the file containing external dependencies.\n" +
                "External dependencies are required for verbose output in case of IR linker errors,\n" +
                "but they do not affect compilation at all."
    )
    var externalDependencies: String? = null

    @Argument(konstue="-Xfake-override-konstidator", description = "Enable IR fake override konstidator")
    var fakeOverrideValidator: Boolean = false

    @Argument(
        konstue = "-Xframework-import-header",
        konstueDescription = "<header>",
        description = "Add additional header import to framework header"
    )
    var frameworkImportHeaders: Array<String>? = null

    @Argument(
        konstue = "-Xadd-light-debug",
        konstueDescription = "{disable|enable}",
        description = "Add light debug information for optimized builds. This option is skipped in debug builds.\n" +
                "It's enabled by default on Darwin platforms where collected debug information is stored in .dSYM file.\n" +
                "Currently option is disabled by default on other platforms."
    )
    var lightDebugString: String? = null

    // TODO: remove after 1.4 release.
    @Argument(konstue = "-Xg0", description = "Add light debug information. Deprecated option. Please use instead -Xadd-light-debug=enable")
    var lightDebugDeprecated: Boolean = false

    @Argument(
        konstue = "-Xg-generate-debug-trampoline",
        konstueDescription = "{disable|enable}",
        description = """generates trampolines to make debugger breakpoint resolution more accurate (inlines, when, etc.)"""
    )
    var generateDebugTrampolineString: String? = null


    @Argument(
        konstue = ADD_CACHE,
        konstueDescription = "<path>",
        description = "Path to the library to be added to cache",
        delimiter = Argument.Delimiters.none
    )
    var libraryToAddToCache: String? = null

    @Argument(
        konstue = "-Xfile-to-cache",
        konstueDescription = "<path>",
        description = "Path to file to cache",
        delimiter = Argument.Delimiters.none
    )
    var filesToCache: Array<String>? = null

    @Argument(konstue = "-Xmake-per-file-cache", description = "Force compiler to produce per-file cache")
    var makePerFileCache: Boolean = false

    @Argument(
        konstue = "-Xbackend-threads",
        konstueDescription = "<N>",
        description = "Run codegen by file in N parallel threads.\n" +
                "0 means use a thread per processor core.\n" +
                "Default konstue is 1"
    )
    var backendThreads: String = "1"

    @Argument(konstue = "-Xexport-kdoc", description = "Export KDoc in framework header")
    var exportKDoc: Boolean = false

    @Argument(konstue = "-Xprint-bitcode", deprecatedName = "--print_bitcode", description = "Print llvm bitcode")
    var printBitCode: Boolean = false

    @Argument(konstue = "-Xcheck-state-at-external-calls", description = "Check all calls of possibly long external functions are done in Native state")
    var checkExternalCalls: Boolean = false

    @Argument(konstue = "-Xprint-ir", deprecatedName = "--print_ir", description = "Print IR")
    var printIr: Boolean = false

    @Argument(konstue = "-Xprint-files", description = "Print files")
    var printFiles: Boolean = false

    @Argument(konstue="-Xpurge-user-libs", deprecatedName = "--purge_user_libs", description = "Don't link unused libraries even explicitly specified")
    var purgeUserLibs: Boolean = false

    @Argument(konstue = "-Xruntime", deprecatedName = "--runtime", konstueDescription = "<path>", description = "Override standard 'runtime.bc' location")
    var runtimeFile: String? = null

    @Argument(
        konstue = INCLUDE_ARG,
        konstueDescription = "<path>",
        description = "A path to an intermediate library that should be processed in the same manner as source files"
    )
    var includes: Array<String>? = null

    @Argument(
        konstue = SHORT_MODULE_NAME_ARG,
        konstueDescription = "<name>",
        description = "A short name used to denote this library in the IDE and in a generated Objective-C header"
    )
    var shortModuleName: String? = null

    @Argument(konstue = STATIC_FRAMEWORK_FLAG, description = "Create a framework with a static library instead of a dynamic one")
    var staticFramework: Boolean = false

    @Argument(konstue = "-Xtemporary-files-dir", deprecatedName = "--temporary_files_dir", konstueDescription = "<path>", description = "Save temporary files to the given directory")
    var temporaryFilesDir: String? = null

    @Argument(konstue = "-Xsave-llvm-ir-after", description = "Save result of Kotlin IR to LLVM IR translation to -Xsave-llvm-ir-directory.")
    var saveLlvmIrAfter: Array<String> = emptyArray()

    @Argument(konstue = "-Xverify-bitcode", deprecatedName = "--verify_bitcode", description = "Verify llvm bitcode after each method")
    var verifyBitCode: Boolean = false

    @Argument(
        konstue = "-Xverify-ir",
        konstueDescription = "{none|warning|error}",
        description = "IR verification mode (no verification by default)"
    )
    var verifyIr: String? = null

    @Argument(konstue = "-Xverify-compiler", description = "Verify compiler")
    var verifyCompiler: String? = null

    @Argument(
        konstue = "-friend-modules",
        konstueDescription = "<path>",
        description = "Paths to friend modules"
    )
    var friendModules: String? = null

    /**
     * @see K2MetadataCompilerArguments.refinesPaths
     */
    @Argument(
        konstue = "-Xrefines-paths",
        konstueDescription = "<path>",
        description = "Paths to output directories for refined modules (whose expects this module can actualize)"
    )
    var refinesPaths: Array<String>? = null

    @Argument(konstue = "-Xdebug-info-version", description = "generate debug info of given version (1, 2)")
    var debugInfoFormatVersion: String = "1" /* command line parser doesn't accept kotlin.Int type */

    @Argument(konstue = "-Xcoverage", description = "emit coverage")
    var coverage: Boolean = false

    @Argument(
        konstue = "-Xlibrary-to-cover",
        konstueDescription = "<path>",
        description = "Provide code coverage for the given library.\n" +
                "Must be one of libraries passed with '-library'",
        delimiter = Argument.Delimiters.none
    )
    var coveredLibraries: Array<String>? = null

    @Argument(konstue = "-Xcoverage-file", konstueDescription = "<path>", description = "Save coverage information to the given file")
    var coverageFile: String? = null

    @Argument(konstue = "-Xno-objc-generics", description = "Disable generics support for framework header")
    var noObjcGenerics: Boolean = false

    @Argument(konstue="-Xoverride-clang-options", konstueDescription = "<arg1,arg2,...>", description = "Explicit list of Clang options")
    var clangOptions: Array<String>? = null

    @Argument(konstue="-Xallocator", konstueDescription = "std | mimalloc | custom", description = "Allocator used in runtime")
    var allocator: String? = null

    @Argument(konstue = "-Xmetadata-klib", description = "Produce a klib that only contains the declarations metadata")
    var metadataKlib: Boolean = false

    @Argument(konstue = "-Xdebug-prefix-map", konstueDescription = "<old1=new1,old2=new2,...>", description = "Remap file source directory paths in debug info")
    var debugPrefixMap: Array<String>? = null

    @Argument(
        konstue = "-Xpre-link-caches",
        konstueDescription = "{disable|enable}",
        description = "Perform caches pre-link"
    )
    var preLinkCaches: String? = null

    // We use `;` as delimiter because properties may contain comma-separated konstues.
    // For example, target cpu features.
    @Argument(
        konstue = "-Xoverride-konan-properties",
        konstueDescription = "key1=konstue1;key2=konstue2;...",
        description = "Override konan.properties.konstues",
        delimiter = ";"
    )
    var overrideKonanProperties: Array<String>? = null

    @Argument(konstue="-Xdestroy-runtime-mode", konstueDescription = "<mode>", description = "When to destroy runtime. 'legacy' and 'on-shutdown' are currently supported. NOTE: 'legacy' mode is deprecated and will be removed.")
    var destroyRuntimeMode: String? = null

    @Argument(konstue="-Xgc", konstueDescription = "<gc>", description = "GC to use, 'noop', 'stms' and 'cms' are currently supported. Works only with -memory-model experimental")
    var gc: String? = null

    @Argument(konstue = "-Xir-property-lazy-initialization", konstueDescription = "{disable|enable}", description = "Initialize top level properties lazily per file")
    var propertyLazyInitialization: String? = null

    // TODO: Remove when legacy MM is gone.
    @Argument(
        konstue = "-Xworker-exception-handling",
        konstueDescription = "<mode>",
        description = "Unhandled exception processing in Worker.executeAfter. Possible konstues: 'legacy', 'use-hook'. The default konstue is 'legacy', for -memory-model experimental the default konstue is 'use-hook'"
    )
    var workerExceptionHandling: String? = null

    @Argument(
        konstue = "-Xllvm-variant",
        konstueDescription = "{dev|user|absolute path to llvm}",
        description = "Choose LLVM distribution which will be used during compilation."
    )
    var llvmVariant: String? = null

    @Argument(
        konstue = "-Xbinary",
        konstueDescription = "<option=konstue>",
        description = "Specify binary option"
    )
    var binaryOptions: Array<String>? = null

    @Argument(konstue = "-Xruntime-logs", konstueDescription = "<tag1=level1,tag2=level2,...>", description = "Enable logging for runtime with tags.")
    var runtimeLogs: String? = null

    @Argument(
        konstue = "-Xdump-tests-to",
        konstueDescription = "<path>",
        description = "Path to a file to dump the list of all available tests"
    )
    var testDumpOutputPath: String? = null

    @Argument(konstue = "-Xlazy-ir-for-caches", konstueDescription = "{disable|enable}", description = "Use lazy IR for cached libraries")
    var lazyIrForCaches: String? = null

    @Argument(konstue = "-Xpartial-linkage", konstueDescription = "{enable|disable}", description = "Use partial linkage mode")
    var partialLinkageMode: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(konstue = "-Xpartial-linkage-loglevel", konstueDescription = "{info|warning|error}", description = "Partial linkage compile-time log level")
    var partialLinkageLogLevel: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(konstue = "-Xomit-framework-binary", description = "Omit binary when compiling framework")
    var omitFrameworkBinary: Boolean = false

    @Argument(konstue = "-Xcompile-from-bitcode", description = "Continue compilation from bitcode file", konstueDescription = "<path>")
    var compileFromBitcode: String? = null

    @Argument(
        konstue = "-Xread-dependencies-from",
        description = "Serialized dependencies to use for linking",
        konstueDescription = "<path>"
    )
    var serializedDependencies: String? = null

    @Argument(konstue = "-Xwrite-dependencies-to", description = "Path for writing backend dependencies")
    var saveDependenciesPath: String? = null

    @Argument(konstue = "-Xsave-llvm-ir-directory", description = "Directory that should contain results of -Xsave-llvm-ir-after=<phase>")
    var saveLlvmIrDirectory: String? = null

    override fun configureAnalysisFlags(collector: MessageCollector, languageVersion: LanguageVersion): MutableMap<AnalysisFlag<*>, Any> =
        super.configureAnalysisFlags(collector, languageVersion).also {
            konst optInList = it[AnalysisFlags.optIn] as List<*>
            it[AnalysisFlags.optIn] = optInList + listOf("kotlin.ExperimentalUnsignedTypes")
            if (printIr)
                phasesToDumpAfter = arrayOf("ALL")
        }

    override fun checkIrSupport(languageVersionSettings: LanguageVersionSettings, collector: MessageCollector) {
        if (languageVersionSettings.languageVersion < LanguageVersion.KOTLIN_1_4
            || languageVersionSettings.apiVersion < ApiVersion.KOTLIN_1_4
        ) {
            collector.report(
                severity = CompilerMessageSeverity.ERROR,
                message = "Native backend cannot be used with language or API version below 1.4"
            )
        }
    }

    override fun copyOf(): Freezable = copyK2NativeCompilerArguments(this, K2NativeCompilerArguments())

    companion object {
        const konst EMBED_BITCODE_FLAG = "-Xembed-bitcode"
        const konst EMBED_BITCODE_MARKER_FLAG = "-Xembed-bitcode-marker"
        const konst STATIC_FRAMEWORK_FLAG = "-Xstatic-framework"
        const konst INCLUDE_ARG = "-Xinclude"
        const konst CACHED_LIBRARY = "-Xcached-library"
        const konst ADD_CACHE = "-Xadd-cache"
        const konst INCREMENTAL_CACHE_DIR = "-Xic-cache-dir"
        const konst SHORT_MODULE_NAME_ARG = "-Xshort-module-name"
    }
}
