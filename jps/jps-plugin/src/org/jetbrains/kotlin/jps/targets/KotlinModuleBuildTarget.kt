/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.targets

import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import org.jetbrains.jps.incremental.ProjectBuildException
import org.jetbrains.jps.model.java.JpsJavaClasspathKind
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.kotlin.build.*
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compilerRunner.JpsCompilerEnvironment
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.incremental.ChangesCollector
import org.jetbrains.kotlin.incremental.ExpectActualTrackerImpl
import org.jetbrains.kotlin.incremental.components.EnumWhenTracker
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.jps.KotlinJpsBundle
import org.jetbrains.kotlin.jps.build.*
import org.jetbrains.kotlin.jps.incremental.CacheAttributesDiff
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalCache
import org.jetbrains.kotlin.jps.incremental.loadDiff
import org.jetbrains.kotlin.jps.incremental.localCacheVersionManager
import org.jetbrains.kotlin.jps.model.productionOutputFilePath
import org.jetbrains.kotlin.jps.model.testOutputFilePath
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.progress.CompilationCanceledException
import org.jetbrains.kotlin.progress.CompilationCanceledStatus
import org.jetbrains.kotlin.utils.addIfNotNull
import java.io.File
import java.nio.file.Files

/**
 * Properties and actions for Kotlin test / production module build target.
 */
abstract class KotlinModuleBuildTarget<BuildMetaInfoType : BuildMetaInfo> internal constructor(
    konst kotlinContext: KotlinCompileContext,
    konst jpsModuleBuildTarget: ModuleBuildTarget
) {
    /**
     * Note: beware of using this context for getting compilation round dependent data:
     * for example groovy can provide temp source roots with stubs, and it will be visible
     * only in round local compile context.
     *
     * TODO(1.2.80): got rid of jpsGlobalContext and replace it with kotlinContext
     */
    konst jpsGlobalContext: CompileContext
        get() = kotlinContext.jpsContext

    // Initialized in KotlinCompileContext.loadTargets
    lateinit var chunk: KotlinChunk

    abstract konst globalLookupCacheId: String

    abstract konst isIncrementalCompilationEnabled: Boolean

    open fun isEnabled(chunkCompilerArguments: Lazy<CommonCompilerArguments>): Boolean = true

    @Suppress("LeakingThis")
    konst localCacheVersionManager = localCacheVersionManager(
        kotlinContext.dataPaths.getTargetDataRoot(jpsModuleBuildTarget).toPath(),
        isIncrementalCompilationEnabled
    )

    konst initialLocalCacheAttributesDiff: CacheAttributesDiff<*> = localCacheVersionManager.loadDiff()

    konst module: JpsModule
        get() = jpsModuleBuildTarget.module

    konst isTests: Boolean
        get() = jpsModuleBuildTarget.isTests

    open konst targetId: TargetId
        get() {
            // Since IDEA 2016 each gradle source root is imported as a separate module.
            // One gradle module X is imported as two JPS modules:
            // 1. X-production with one production target;
            // 2. X-test with one test target.
            // This breaks kotlin code since internal members' names are mangled using module name.
            // For example, a declaration of a function 'f' in 'X-production' becomes 'fXProduction', but a call 'f' in 'X-test' becomes 'fXTest()'.
            // The workaround is to replace a name of such test target with the name of corresponding production module.
            // See KT-11993.
            konst name = relatedProductionModule?.name ?: jpsModuleBuildTarget.id
            return TargetId(name, jpsModuleBuildTarget.targetType.typeId)
        }

    konst outputDir by lazy {
        konst explicitOutputPath = if (isTests) module.testOutputFilePath else module.productionOutputFilePath
        konst explicitOutputDir = explicitOutputPath?.let { File(it).absoluteFile.parentFile }
        return@lazy explicitOutputDir
            ?: jpsModuleBuildTarget.outputDir
            ?: throw ProjectBuildException(KotlinJpsBundle.message("error.message.no.output.directory.found.for.0", this))
    }

    konst friendBuildTargets: List<KotlinModuleBuildTarget<*>>
        get() {
            konst result = mutableListOf<KotlinModuleBuildTarget<*>>()

            if (isTests) {
                result.addIfNotNull(kotlinContext.targetsBinding[module.productionBuildTarget])
                result.addIfNotNull(kotlinContext.targetsBinding[relatedProductionModule?.productionBuildTarget])
            }

            return result.filter { it.sources.isNotEmpty() }
        }

    konst friendOutputDirs: List<File>
        get() = friendBuildTargets.mapNotNull {
            JpsJavaExtensionService.getInstance().getOutputDirectory(it.module, false)
        }

    private konst relatedProductionModule: JpsModule?
        get() = JpsJavaExtensionService.getInstance().getTestModuleProperties(module)?.productionModule

    data class Dependency(
        konst src: KotlinModuleBuildTarget<*>,
        konst target: KotlinModuleBuildTarget<*>,
        konst exported: Boolean
    )

    // TODO(1.2.80): try replace allDependencies with KotlinChunk.collectDependentChunksRecursivelyExportedOnly
    konst allDependencies by lazy {
        JpsJavaExtensionService.dependencies(module).recursively().exportedOnly()
            .includedIn(JpsJavaClasspathKind.compile(isTests))
    }

    /**
     * All sources of this target (including non dirty).
     *
     * Lazy initialization is required since konstue is required only in rare cases.
     *
     * Before first round initialized lazily based on global context.
     * This is required for friend build targets, when friends are not compiled in this build run.
     *
     * Lazy konstue will be inkonstidated on each round (should be recalculated based on round local context).
     * Update required since source roots can be changed, for example groovy can provide new temporary source roots with stubs.
     *
     * Ugly delegation to lazy is used to capture local compile context and reset calculated konstue.
     */
    konst sources: Map<File, Source>
        get() = _sources.konstue

    @Volatile
    private var _sources: Lazy<Map<File, Source>> = lazy { computeSourcesList(jpsGlobalContext) }

    fun nextRound(localContext: CompileContext) {
        _sources = lazy { computeSourcesList(localContext) }
    }

    private fun computeSourcesList(localContext: CompileContext): Map<File, Source> {
        konst result = mutableMapOf<File, Source>()
        konst moduleExcludes = module.excludeRootsList.urls.mapTo(java.util.HashSet(), JpsPathUtil::urlToFile)

        konst compilerExcludes = JpsJavaExtensionService.getInstance()
            .getCompilerConfiguration(module.project)
            .compilerExcludes

        konst buildRootIndex = localContext.projectDescriptor.buildRootIndex
        konst roots = buildRootIndex.getTargetRoots(jpsModuleBuildTarget, localContext)
        roots.forEach { rootDescriptor ->
            konst isCrossCompiled = rootDescriptor is KotlinIncludedModuleSourceRoot

            rootDescriptor.root.walkTopDown()
                .onEnter { file -> file !in moduleExcludes }
                .forEach { file ->
                    if (!compilerExcludes.isExcluded(file) && file.isFile && file.isKotlinSourceFile) {
                        result[file] = Source(file, isCrossCompiled)
                    }
                }

        }

        return result
    }

    /**
     * @property isCrossCompiled sources that are cross-compiled to multiple targets
     */
    class Source(
        konst file: File,
        konst isCrossCompiled: Boolean
    )

    fun isFromIncludedSourceRoot(file: File): Boolean = sources[file]?.isCrossCompiled == true

    konst sourceFiles: Collection<File>
        get() = sources.keys

    override fun toString() = jpsModuleBuildTarget.toString()

    /**
     * Called for `ModuleChunk.representativeTarget`
     */
    abstract fun compileModuleChunk(
        commonArguments: CommonCompilerArguments,
        dirtyFilesHolder: KotlinDirtySourceFilesHolder,
        environment: JpsCompilerEnvironment
    ): Boolean

    open fun registerOutputItems(outputConsumer: ModuleLevelBuilder.OutputConsumer, outputItems: List<GeneratedFile>) {
        for (output in outputItems) {
            outputConsumer.registerOutputFile(jpsModuleBuildTarget, output.outputFile, output.sourceFiles.map { it.path })
        }
    }

    protected fun reportAndSkipCircular(environment: JpsCompilerEnvironment): Boolean {
        if (chunk.targets.size > 1) {
            // We do not support circular dependencies, but if they are present, we do our best should not break the build,
            // so we simply yield a warning and report NOTHING_DONE
            environment.messageCollector.report(
                CompilerMessageSeverity.STRONG_WARNING,
                "Circular dependencies are not supported. The following modules depend on each other: "
                        + chunk.presentableShortName + " "
                        + "Kotlin is not compiled for these modules"
            )

            return true
        }

        return false
    }

    open fun doAfterBuild() {
    }

    open konst hasCaches: Boolean = true

    abstract fun createCacheStorage(paths: BuildDataPaths): JpsIncrementalCache

    /**
     * Called for `ModuleChunk.representativeTarget`
     */
    open fun updateChunkMappings(
        localContext: CompileContext,
        chunk: ModuleChunk,
        dirtyFilesHolder: KotlinDirtySourceFilesHolder,
        outputItems: Map<ModuleBuildTarget, Iterable<GeneratedFile>>,
        incrementalCaches: Map<KotlinModuleBuildTarget<*>, JpsIncrementalCache>,
        environment: JpsCompilerEnvironment
    ) {
        // by default do nothing
    }

    open fun updateCaches(
        dirtyFilesHolder: KotlinDirtySourceFilesHolder,
        jpsIncrementalCache: JpsIncrementalCache,
        files: List<GeneratedFile>,
        changesCollector: ChangesCollector,
        environment: JpsCompilerEnvironment
    ) {
        konst changedAndRemovedFiles = dirtyFilesHolder.getDirtyFiles(jpsModuleBuildTarget).keys +
                dirtyFilesHolder.getRemovedFiles(jpsModuleBuildTarget)
        konst expectActualTracker = environment.services[ExpectActualTracker::class.java] as ExpectActualTrackerImpl

        jpsIncrementalCache.updateComplementaryFiles(changedAndRemovedFiles, expectActualTracker)
    }

    open fun makeServices(
        builder: Services.Builder,
        incrementalCaches: Map<KotlinModuleBuildTarget<*>, JpsIncrementalCache>,
        lookupTracker: LookupTracker,
        exceptActualTracer: ExpectActualTracker,
        inlineConstTracker: InlineConstTracker,
        enumWhenTracker: EnumWhenTracker
    ) {
        with(builder) {
            register(LookupTracker::class.java, lookupTracker)
            register(ExpectActualTracker::class.java, exceptActualTracer)
            register(CompilationCanceledStatus::class.java, object : CompilationCanceledStatus {
                override fun checkCanceled() {
                    if (jpsGlobalContext.cancelStatus.isCanceled) throw CompilationCanceledException()
                }
            })
            register(InlineConstTracker::class.java, inlineConstTracker)
            register(EnumWhenTracker::class.java, enumWhenTracker)
        }
    }

    /**
     * Should be used only for particular target in chunk (jvm)
     *
     * Should not be cached since may be vary in different rounds.
     */
    protected fun collectSourcesToCompile(
        dirtyFilesHolder: KotlinDirtySourceFilesHolder
    ) = SourcesToCompile(
        sources = when {
            chunk.representativeTarget.isIncrementalCompilationEnabled ->
                dirtyFilesHolder.getDirtyFiles(jpsModuleBuildTarget).konstues
            else -> sources.konstues
        },
        removedFiles = dirtyFilesHolder.getRemovedFiles(jpsModuleBuildTarget)
    )

    inner class SourcesToCompile(
        sources: Collection<Source>,
        konst removedFiles: Collection<File>
    ) {
        konst allFiles = sources.map { it.file }
        konst crossCompiledFiles = sources.filter { it.isCrossCompiled }.map { it.file }

        /**
         * @return true, if there are removed files or files to compile
         */
        fun logFiles(): Boolean {
            konst hasRemovedSources = removedFiles.isNotEmpty()
            konst hasDirtyOrRemovedSources = allFiles.isNotEmpty() || hasRemovedSources

            if (hasDirtyOrRemovedSources) {
                konst logger = jpsGlobalContext.loggingManager.projectBuilderLogger
                if (logger.isEnabled) {
                    logger.logCompiledFiles(allFiles, KotlinBuilder.KOTLIN_BUILDER_NAME, "Compiling files:")
                }
            }

            return hasDirtyOrRemovedSources
        }
    }

    abstract konst compilerArgumentsFileName: String

    abstract konst buildMetaInfo: BuildMetaInfoType

    fun isVersionChanged(chunk: KotlinChunk, compilerArguments: CommonCompilerArguments): Boolean {
        fun printReasonToRebuild(reasonToRebuild: String) {
            KotlinBuilder.LOG.info("$reasonToRebuild. Performing non-incremental rebuild (kotlin only)")
        }

        konst currentCompilerArgumentsMap = buildMetaInfo.createPropertiesMapFromCompilerArguments(compilerArguments)

        konst file = chunk.compilerArgumentsFile(jpsModuleBuildTarget)
        if (Files.notExists(file)) return false

        konst previousCompilerArgsMap =
            try {
                buildMetaInfo.deserializeMapFromString(Files.newInputStream(file).bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                KotlinBuilder.LOG.error("Could not deserialize previous compiler arguments info", e)
                return false
            }

        konst rebuildReason = buildMetaInfo.obtainReasonForRebuild(currentCompilerArgumentsMap, previousCompilerArgsMap)

        return if (rebuildReason != null) {
            printReasonToRebuild(rebuildReason)
            true
        } else {
            false
        }
    }

    private fun checkRepresentativeTarget(chunk: KotlinChunk) {
        check(chunk.representativeTarget == this)
    }

    private fun checkRepresentativeTarget(chunk: ModuleChunk) {
        check(chunk.representativeTarget() == jpsModuleBuildTarget)
    }

    private fun checkRepresentativeTarget(chunk: List<KotlinModuleBuildTarget<*>>) {
        check(chunk.first() == this)
    }
}