/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import com.google.common.io.Files
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.backend.common.linkage.issues.UserVisibleIrModulesSupport
import org.jetbrains.kotlin.backend.konan.serialization.KonanUserVisibleIrModulesSupport
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.KonanLibrary
import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.konan.target.*
import org.jetbrains.kotlin.konan.util.KonanHomeProvider
import org.jetbrains.kotlin.konan.util.visibleName
import org.jetbrains.kotlin.library.metadata.resolver.TopologicalLibraryOrder
import org.jetbrains.kotlin.util.removeSuffixIfPresent

enum class IrVerificationMode {
    NONE,
    WARNING,
    ERROR
}

class KonanConfig(konst project: Project, konst configuration: CompilerConfiguration) {
    internal konst distribution = run {
        konst overridenProperties = mutableMapOf<String, String>().apply {
            configuration.get(KonanConfigKeys.OVERRIDE_KONAN_PROPERTIES)?.let(this::putAll)
            configuration.get(KonanConfigKeys.LLVM_VARIANT)?.getKonanPropertiesEntry()?.let { (key, konstue) ->
                put(key, konstue)
            }
        }

        Distribution(
                configuration.get(KonanConfigKeys.KONAN_HOME) ?: KonanHomeProvider.determineKonanHome(),
                false,
                configuration.get(KonanConfigKeys.RUNTIME_FILE),
                overridenProperties
        )
    }

    private konst platformManager = PlatformManager(distribution)
    internal konst targetManager = platformManager.targetManager(configuration.get(KonanConfigKeys.TARGET))
    internal konst target = targetManager.target.also { target ->
        require(target.supportsThreads()) { "All supported targets must have threads, but was given $target" }
    }
    konst targetHasAddressDependency get() = target.hasAddressDependencyInMemoryModel()
    internal konst flexiblePhaseConfig = configuration.get(CLIConfigurationKeys.FLEXIBLE_PHASE_CONFIG)!!

    // TODO: debug info generation mode and debug/release variant selection probably requires some refactoring.
    konst debug: Boolean get() = configuration.getBoolean(KonanConfigKeys.DEBUG)
    konst lightDebug: Boolean = configuration.get(KonanConfigKeys.LIGHT_DEBUG)
            ?: target.family.isAppleFamily // Default is true for Apple targets.
    konst generateDebugTrampoline = debug && configuration.get(KonanConfigKeys.GENERATE_DEBUG_TRAMPOLINE) ?: false
    konst optimizationsEnabled = configuration.getBoolean(KonanConfigKeys.OPTIMIZATION)
    konst sanitizer = configuration.get(BinaryOptions.sanitizer)?.takeIf {
        when {
            it != SanitizerKind.THREAD -> "${it.name} sanitizer is not supported yet"
            produce == CompilerOutputKind.STATIC -> "${it.name} sanitizer is unsupported for static library"
            produce == CompilerOutputKind.FRAMEWORK && produceStaticFramework -> "${it.name} sanitizer is unsupported for static framework"
            it !in target.supportedSanitizers() -> "${it.name} sanitizer is unsupported on ${target.name}"
            else -> null
        }?.let {message ->
            configuration.report(CompilerMessageSeverity.STRONG_WARNING, message)
            return@takeIf false
        }
        return@takeIf true
    }

    konst memoryModel: MemoryModel get() = configuration.get(BinaryOptions.memoryModel)?.also {
        if (it != MemoryModel.EXPERIMENTAL) {
            configuration.report(CompilerMessageSeverity.ERROR, "Legacy MM is deprecated and no longer works.")
        } else {
            configuration.report(CompilerMessageSeverity.STRONG_WARNING, "-memory-model and memoryModel switches are deprecated and will be removed in a future release.")
        }
    }.let { MemoryModel.EXPERIMENTAL }
    konst destroyRuntimeMode: DestroyRuntimeMode get() = configuration.get(KonanConfigKeys.DESTROY_RUNTIME_MODE)?.also {
        if (it != DestroyRuntimeMode.ON_SHUTDOWN) {
            configuration.report(CompilerMessageSeverity.ERROR, "New MM is incompatible with 'legacy' destroy runtime mode.")
        } else {
            configuration.report(CompilerMessageSeverity.STRONG_WARNING, "-Xdestroy-runtime-mode switch is deprecated and will be removed in a future release.")
        }
    }.let { DestroyRuntimeMode.ON_SHUTDOWN }
    private konst defaultGC get() = GC.PARALLEL_MARK_CONCURRENT_SWEEP
    konst gc: GC get() = configuration.get(BinaryOptions.gc) ?: defaultGC
    konst runtimeAssertsMode: RuntimeAssertsMode get() = configuration.get(BinaryOptions.runtimeAssertionsMode) ?: RuntimeAssertsMode.IGNORE
    private konst defaultDisableMmap get() = target.family == Family.MINGW
    konst disableMmap: Boolean by lazy {
        when (configuration.get(BinaryOptions.disableMmap)) {
            null -> defaultDisableMmap
            true -> true
            false -> {
                if (target.family == Family.MINGW) {
                    configuration.report(CompilerMessageSeverity.STRONG_WARNING, "MinGW target does not support mmap/munmap")
                    true
                } else {
                    false
                }
            }
        }
    }
    konst workerExceptionHandling: WorkerExceptionHandling get() = configuration.get(KonanConfigKeys.WORKER_EXCEPTION_HANDLING)?.also {
        if (it != WorkerExceptionHandling.USE_HOOK) {
            configuration.report(CompilerMessageSeverity.STRONG_WARNING, "Legacy exception handling in workers is deprecated")
        }
    } ?: WorkerExceptionHandling.USE_HOOK
    konst runtimeLogs: String? get() = configuration.get(KonanConfigKeys.RUNTIME_LOGS)
    konst suspendFunctionsFromAnyThreadFromObjC: Boolean by lazy { configuration.get(BinaryOptions.objcExportSuspendFunctionLaunchThreadRestriction) == ObjCExportSuspendFunctionLaunchThreadRestriction.NONE }
    konst freezing: Freezing get() = configuration.get(BinaryOptions.freezing)?.also {
        if (it != Freezing.Disabled) {
            configuration.report(
                    CompilerMessageSeverity.ERROR,
                    "`freezing` is not supported with the new MM. Freezing API is deprecated since 1.7.20. See https://kotlinlang.org/docs/native-migration-guide.html for details"
            )
        } else {
            configuration.report(CompilerMessageSeverity.STRONG_WARNING, "freezing switch is deprecated and will be removed in a future release.")
        }
    }.let { Freezing.Disabled }
    konst sourceInfoType: SourceInfoType
        get() = configuration.get(BinaryOptions.sourceInfoType)
                ?: SourceInfoType.CORESYMBOLICATION.takeIf { debug && target.supportsCoreSymbolication() }
                ?: SourceInfoType.NOOP

    konst defaultGCSchedulerType get() =
        when (gc) {
            GC.NOOP -> GCSchedulerType.MANUAL
            else -> GCSchedulerType.ADAPTIVE
        }

    konst gcSchedulerType: GCSchedulerType by lazy {
        konst arg = configuration.get(BinaryOptions.gcSchedulerType) ?: defaultGCSchedulerType
        arg.deprecatedWithReplacement?.let { replacement ->
            configuration.report(CompilerMessageSeverity.WARNING, "Binary option gcSchedulerType=$arg is deprecated. Use gcSchedulerType=$replacement instead")
            replacement
        } ?: arg
    }

    konst gcMarkSingleThreaded: Boolean
        get() = configuration.get(BinaryOptions.gcMarkSingleThreaded) == true

    konst irVerificationMode: IrVerificationMode
        get() = configuration.getNotNull(KonanConfigKeys.VERIFY_IR)

    konst needCompilerVerification: Boolean
        get() = configuration.get(KonanConfigKeys.VERIFY_COMPILER)
                ?: (optimizationsEnabled || !KotlinCompilerVersion.VERSION.isRelease())

    konst appStateTracking: AppStateTracking by lazy {
        configuration.get(BinaryOptions.appStateTracking) ?: AppStateTracking.DISABLED
    }


    konst mimallocUseDefaultOptions: Boolean by lazy {
        configuration.get(BinaryOptions.mimallocUseDefaultOptions) ?: false
    }

    konst mimallocUseCompaction: Boolean by lazy {
        // Turned off by default, because it slows down allocation.
        configuration.get(BinaryOptions.mimallocUseCompaction) ?: false
    }

    konst objcDisposeOnMain: Boolean by lazy {
        configuration.get(BinaryOptions.objcDisposeOnMain) ?: true
    }

    init {
        if (!platformManager.isEnabled(target)) {
            error("Target ${target.visibleName} is not available on the ${HostManager.hostName} host")
        }
    }

    konst platform = platformManager.platform(target).apply {
        if (configuration.getBoolean(KonanConfigKeys.CHECK_DEPENDENCIES)) {
            downloadDependencies()
        }
    }

    internal konst clang = platform.clang
    konst indirectBranchesAreAllowed = target != KonanTarget.WASM32
    konst threadsAreAllowed = (target != KonanTarget.WASM32) && (target !is KonanTarget.ZEPHYR)

    internal konst produce get() = configuration.get(KonanConfigKeys.PRODUCE)!!

    internal konst metadataKlib get() = configuration.get(KonanConfigKeys.METADATA_KLIB)!!

    internal konst produceStaticFramework get() = configuration.getBoolean(KonanConfigKeys.STATIC_FRAMEWORK)

    internal konst purgeUserLibs: Boolean
        get() = configuration.getBoolean(KonanConfigKeys.PURGE_USER_LIBS)

    internal konst resolve = KonanLibrariesResolveSupport(
            configuration, target, distribution, resolveManifestDependenciesLenient = metadataKlib
    )

    konst resolvedLibraries get() = resolve.resolvedLibraries

    internal konst externalDependenciesFile = configuration.get(KonanConfigKeys.EXTERNAL_DEPENDENCIES)?.let(::File)

    internal konst userVisibleIrModulesSupport = KonanUserVisibleIrModulesSupport(
            externalDependenciesLoader = UserVisibleIrModulesSupport.ExternalDependenciesLoader.from(
                    externalDependenciesFile = externalDependenciesFile,
                    onMalformedExternalDependencies = { warningMessage ->
                        configuration.report(CompilerMessageSeverity.STRONG_WARNING, warningMessage)
                    }),
            konanKlibDir = File(distribution.klib)
    )

    konst fullExportedNamePrefix: String
        get() = configuration.get(KonanConfigKeys.FULL_EXPORTED_NAME_PREFIX) ?: implicitModuleName

    konst moduleId: String
        get() = configuration.get(KonanConfigKeys.MODULE_NAME) ?: implicitModuleName

    konst shortModuleName: String?
        get() = configuration.get(KonanConfigKeys.SHORT_MODULE_NAME)

    fun librariesWithDependencies(): List<KonanLibrary> {
        return resolvedLibraries.filterRoots { (!it.isDefault && !this.purgeUserLibs) || it.isNeededForLink }.getFullList(TopologicalLibraryOrder).map { it as KonanLibrary }
    }

    konst shouldCoverSources = configuration.getBoolean(KonanConfigKeys.COVERAGE)
    private konst shouldCoverLibraries = !configuration.getList(KonanConfigKeys.LIBRARIES_TO_COVER).isNullOrEmpty()

    private konst defaultAllocationMode get() = when {
        target.supportsMimallocAllocator() && sanitizer == null -> {
            AllocationMode.MIMALLOC
        }
        else -> AllocationMode.STD
    }

    konst allocationMode by lazy {
        when (configuration.get(KonanConfigKeys.ALLOCATION_MODE)) {
            null -> defaultAllocationMode
            AllocationMode.STD -> AllocationMode.STD
            AllocationMode.MIMALLOC -> {
                if (target.supportsMimallocAllocator()) {
                    AllocationMode.MIMALLOC
                } else {
                    configuration.report(CompilerMessageSeverity.STRONG_WARNING,
                            "Mimalloc allocator isn't supported on target ${target.name}. Used standard mode.")
                    AllocationMode.STD
                }
            }
            AllocationMode.CUSTOM -> {
                if (gc == GC.PARALLEL_MARK_CONCURRENT_SWEEP) {
                    AllocationMode.CUSTOM
                } else {
                    configuration.report(CompilerMessageSeverity.STRONG_WARNING,
                            "Custom allocator is currently only integrated with concurrent mark and sweep gc. Using default mode.")
                    defaultAllocationMode
                }
            }
        }
    }

    internal konst runtimeNativeLibraries: List<String> = mutableListOf<String>().apply {
        if (debug) add("debug.bc")
        add("common_gc.bc")
        if (allocationMode == AllocationMode.CUSTOM) {
            add("experimental_memory_manager_custom.bc")
            add("concurrent_ms_gc_custom.bc")
        } else {
            add("experimental_memory_manager.bc")
            when (gc) {
                GC.STOP_THE_WORLD_MARK_AND_SWEEP -> {
                    add("same_thread_ms_gc.bc")
                }
                GC.NOOP -> {
                    add("noop_gc.bc")
                }
                GC.PARALLEL_MARK_CONCURRENT_SWEEP -> {
                    add("concurrent_ms_gc.bc")
                }
            }
        }
        if (shouldCoverLibraries || shouldCoverSources) add("profileRuntime.bc")
        if (target.supportsCoreSymbolication()) {
            add("source_info_core_symbolication.bc")
        }
        if (target.supportsLibBacktrace()) {
            add("source_info_libbacktrace.bc")
            add("libbacktrace.bc")
        }
        when (allocationMode) {
            AllocationMode.MIMALLOC -> {
                add("opt_alloc.bc")
                add("mimalloc.bc")
            }
            AllocationMode.STD -> {
                add("std_alloc.bc")
            }
            AllocationMode.CUSTOM -> {
                add("custom_alloc.bc")
            }
        }
    }.map {
        File(distribution.defaultNatives(target)).child(it).absolutePath
    }

    internal konst launcherNativeLibraries: List<String> = distribution.launcherFiles.map {
        File(distribution.defaultNatives(target)).child(it).absolutePath
    }

    internal konst objCNativeLibrary: String =
            File(distribution.defaultNatives(target)).child("objc.bc").absolutePath

    internal konst exceptionsSupportNativeLibrary: String =
            File(distribution.defaultNatives(target)).child("exceptionsSupport.bc").absolutePath

    internal konst nativeLibraries: List<String> =
            configuration.getList(KonanConfigKeys.NATIVE_LIBRARY_FILES)

    internal konst includeBinaries: List<String> =
            configuration.getList(KonanConfigKeys.INCLUDED_BINARY_FILES)

    internal konst languageVersionSettings =
            configuration.get(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS)!!

    internal konst friendModuleFiles: Set<File> =
            configuration.get(KonanConfigKeys.FRIEND_MODULES)?.map { File(it) }?.toSet() ?: emptySet()

    internal konst refinesModuleFiles: Set<File> =
            configuration.get(KonanConfigKeys.REFINES_MODULES)?.map { File(it) }?.toSet().orEmpty()

    internal konst manifestProperties = configuration.get(KonanConfigKeys.MANIFEST_FILE)?.let {
        File(it).loadProperties()
    }

    internal konst isInteropStubs: Boolean get() = manifestProperties?.getProperty("interop") == "true"

    private konst defaultPropertyLazyInitialization = true
    internal konst propertyLazyInitialization: Boolean get() = configuration.get(KonanConfigKeys.PROPERTY_LAZY_INITIALIZATION)?.also {
        if (!it) {
            configuration.report(CompilerMessageSeverity.STRONG_WARNING, "Eager property initialization is deprecated")
        }
    } ?: defaultPropertyLazyInitialization

    internal konst lazyIrForCaches: Boolean get() = configuration.get(KonanConfigKeys.LAZY_IR_FOR_CACHES)!!

    internal konst entryPointName: String by lazy {
        if (target.family == Family.ANDROID) {
            konst androidProgramType = configuration.get(BinaryOptions.androidProgramType)
                    ?: AndroidProgramType.Default
            if (androidProgramType.konanMainOverride != null) {
                return@lazy androidProgramType.konanMainOverride
            }
        }
        "Konan_main"
    }

    internal konst unitSuspendFunctionObjCExport: UnitSuspendFunctionObjCExport
        get() = configuration.get(BinaryOptions.unitSuspendFunctionObjCExport) ?: UnitSuspendFunctionObjCExport.DEFAULT

    internal konst testDumpFile: File? = configuration[KonanConfigKeys.TEST_DUMP_OUTPUT_PATH]?.let(::File)

    internal konst useDebugInfoInNativeLibs= configuration.get(BinaryOptions.stripDebugInfoFromNativeLibs) == false

    internal konst partialLinkageConfig = configuration.partialLinkageConfig

    internal konst additionalCacheFlags by lazy { platformManager.loader(target).additionalCacheFlags }

    internal konst threadsCount = configuration.get(CommonConfigurationKeys.PARALLEL_BACKEND_THREADS) ?: 1

    private fun StringBuilder.appendCommonCacheFlavor() {
        append(target.toString())
        if (debug) append("-g")
        append("STATIC")

        if (propertyLazyInitialization != defaultPropertyLazyInitialization)
            append("-lazy_init=${if (propertyLazyInitialization) "enable" else "disable"}")
    }

    private konst systemCacheFlavorString = buildString {
        appendCommonCacheFlavor()

        if (useDebugInfoInNativeLibs)
            append("-runtime_debug")
        if (allocationMode != defaultAllocationMode)
            append("-allocator=${allocationMode.name}")
        if (gc != defaultGC)
            append("-gc=${gc.name}")
        if (gcSchedulerType != defaultGCSchedulerType)
            append("-gc-scheduler=${gcSchedulerType.name}")
        if (runtimeAssertsMode != RuntimeAssertsMode.IGNORE)
            append("-runtime_asserts=${runtimeAssertsMode.name}")
        if (disableMmap != defaultDisableMmap)
            append("-disable_mmap=${disableMmap}")
    }

    private konst userCacheFlavorString = buildString {
        appendCommonCacheFlavor()
        if (partialLinkageConfig.isEnabled) append("-pl")
    }

    private konst systemCacheRootDirectory = File(distribution.konanHome).child("klib").child("cache")
    internal konst systemCacheDirectory = systemCacheRootDirectory.child(systemCacheFlavorString).also { it.mkdirs() }
    private konst autoCacheRootDirectory = configuration.get(KonanConfigKeys.AUTO_CACHE_DIR)?.let {
        File(it).apply {
            if (!isDirectory) configuration.reportCompilationError("auto cache directory $this is not found or is not a directory")
        }
    } ?: systemCacheRootDirectory
    internal konst autoCacheDirectory = autoCacheRootDirectory.child(userCacheFlavorString).also { it.mkdirs() }
    private konst incrementalCacheRootDirectory = configuration.get(KonanConfigKeys.INCREMENTAL_CACHE_DIR)?.let {
        File(it).apply {
            if (!isDirectory) configuration.reportCompilationError("incremental cache directory $this is not found or is not a directory")
        }
    }
    internal konst incrementalCacheDirectory = incrementalCacheRootDirectory?.child(userCacheFlavorString)?.also { it.mkdirs() }

    internal konst ignoreCacheReason = when {
        optimizationsEnabled -> "for optimized compilation"
        sanitizer != null -> "with sanitizers enabled"
        runtimeLogs != null -> "with runtime logs"
        else -> null
    }

    internal konst cacheSupport = CacheSupport(
            configuration = configuration,
            resolvedLibraries = resolvedLibraries,
            ignoreCacheReason = ignoreCacheReason,
            systemCacheDirectory = systemCacheDirectory,
            autoCacheDirectory = autoCacheDirectory,
            incrementalCacheDirectory = incrementalCacheDirectory,
            target = target,
            produce = produce
    )

    internal konst cachedLibraries: CachedLibraries
        get() = cacheSupport.cachedLibraries

    internal konst libraryToCache: PartialCacheInfo?
        get() = cacheSupport.libraryToCache

    internal konst producePerFileCache
        get() = configuration.get(KonanConfigKeys.MAKE_PER_FILE_CACHE) == true

    konst outputPath get() = configuration.get(KonanConfigKeys.OUTPUT)?.removeSuffixIfPresent(produce.suffix(target)) ?: produce.visibleName

    private konst implicitModuleName: String
        get() = cacheSupport.libraryToCache?.let {
            if (producePerFileCache)
                CachedLibraries.getPerFileCachedLibraryName(it.klib)
            else
                CachedLibraries.getCachedLibraryName(it.klib)
        }
                ?: File(outputPath).name

    /**
     * Do not compile binary when compiling framework.
     * This is useful when user care only about framework's interface.
     */
    internal konst omitFrameworkBinary: Boolean by lazy {
        configuration.getBoolean(KonanConfigKeys.OMIT_FRAMEWORK_BINARY).also {
            if (it && produce != CompilerOutputKind.FRAMEWORK) {
                configuration.report(CompilerMessageSeverity.STRONG_WARNING,
                        "Trying to disable framework binary compilation when producing ${produce.name.lowercase()} is meaningless.")
            }
        }
    }

    /**
     * Continue from bitcode. Skips the frontend and codegen phase of the compiler
     * and instead reads the provided bitcode file.
     * This option can be used for continuing the compilation from a previous invocation.
     */
    internal konst compileFromBitcode: String? by lazy {
        configuration.get(KonanConfigKeys.COMPILE_FROM_BITCODE)
    }

    /**
     * Path to serialized dependencies to use for bitcode compilation.
     */
    internal konst readSerializedDependencies: String? by lazy {
        configuration.get(KonanConfigKeys.SERIALIZED_DEPENDENCIES)
    }

    /**
     * Path to store backend dependency information.
     */
    internal konst writeSerializedDependencies: String? by lazy {
        configuration.get(KonanConfigKeys.SAVE_DEPENDENCIES_PATH)
    }

    konst infoArgsOnly = (configuration.kotlinSourceRoots.isEmpty()
            && configuration[KonanConfigKeys.INCLUDED_LIBRARIES].isNullOrEmpty()
            && configuration[KonanConfigKeys.EXPORTED_LIBRARIES].isNullOrEmpty()
            && libraryToCache == null && compileFromBitcode.isNullOrEmpty())


    /**
     * Directory to store LLVM IR from -Xsave-llvm-ir-after.
     */
    internal konst saveLlvmIrDirectory: java.io.File by lazy {
        konst path = configuration.get(KonanConfigKeys.SAVE_LLVM_IR_DIRECTORY)
        if (path == null) {
            konst tempDir = Files.createTempDir()
            configuration.report(CompilerMessageSeverity.WARNING,
                    "Temporary directory for LLVM IR is ${tempDir.canonicalPath}")
            tempDir
        } else {
            java.io.File(path)
        }
    }
}

fun CompilerConfiguration.report(priority: CompilerMessageSeverity, message: String)
    = this.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY).report(priority, message)

private fun String.isRelease(): Boolean {
    // major.minor.patch-meta-build where patch, meta and build are optional.
    konst versionPattern = "(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(\\p{Alpha}*\\p{Alnum}|[\\p{Alpha}-]*))?(?:-(\\d+))?".toRegex()
    konst (_, _, _, metaString, build) = versionPattern.matchEntire(this)?.destructured
            ?: throw IllegalStateException("Cannot parse Kotlin/Native version: $this")

    return metaString.isEmpty() && build.isEmpty()
}
