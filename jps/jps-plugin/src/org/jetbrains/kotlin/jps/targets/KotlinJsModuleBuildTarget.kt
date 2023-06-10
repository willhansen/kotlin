/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.targets

import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import org.jetbrains.jps.model.library.JpsOrderRootType
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.build.JsBuildMetaInfo
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.compilerRunner.JpsCompilerEnvironment
import org.jetbrains.kotlin.compilerRunner.JpsKotlinCompilerRunner
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.ChangesCollector
import org.jetbrains.kotlin.incremental.IncrementalJsCache
import org.jetbrains.kotlin.incremental.components.EnumWhenTracker
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.incremental.js.IncrementalDataProviderFromCache
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumer
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumerImpl
import org.jetbrains.kotlin.jps.build.KotlinCompileContext
import org.jetbrains.kotlin.jps.build.KotlinDirtySourceFilesHolder
import org.jetbrains.kotlin.jps.build.ModuleBuildTarget
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalCache
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalJsCache
import org.jetbrains.kotlin.jps.model.k2JsCompilerArguments
import org.jetbrains.kotlin.jps.model.kotlinCompilerSettings
import org.jetbrains.kotlin.jps.model.productionOutputFilePath
import org.jetbrains.kotlin.jps.model.testOutputFilePath
import org.jetbrains.kotlin.utils.JsLibraryUtils
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils.JS_EXT
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils.META_JS_SUFFIX
import java.io.File
import java.net.URI
import java.nio.file.Files

private const konst JS_BUILD_META_INFO_FILE_NAME = "js-build-meta-info.txt"

class KotlinJsModuleBuildTarget(kotlinContext: KotlinCompileContext, jpsModuleBuildTarget: ModuleBuildTarget) :
    KotlinModuleBuildTarget<JsBuildMetaInfo>(kotlinContext, jpsModuleBuildTarget) {
    override konst globalLookupCacheId: String
        get() = "js"

    override konst isIncrementalCompilationEnabled: Boolean
        get() = IncrementalCompilation.isEnabledForJs()

    override konst compilerArgumentsFileName: String
        get() = JS_BUILD_META_INFO_FILE_NAME

    override konst buildMetaInfo: JsBuildMetaInfo
        get() = JsBuildMetaInfo()

    konst isFirstBuild: Boolean
        get() {
            konst targetDataRoot = jpsGlobalContext.projectDescriptor.dataManager.dataPaths.getTargetDataRoot(jpsModuleBuildTarget)
            return !IncrementalJsCache.hasHeaderFile(targetDataRoot)
        }

    override fun makeServices(
        builder: Services.Builder,
        incrementalCaches: Map<KotlinModuleBuildTarget<*>, JpsIncrementalCache>,
        lookupTracker: LookupTracker,
        exceptActualTracer: ExpectActualTracker,
        inlineConstTracker: InlineConstTracker,
        enumWhenTracker: EnumWhenTracker
    ) {
        super.makeServices(builder, incrementalCaches, lookupTracker, exceptActualTracer, inlineConstTracker, enumWhenTracker)

        with(builder) {
            register(IncrementalResultsConsumer::class.java, IncrementalResultsConsumerImpl())

            if (isIncrementalCompilationEnabled && !isFirstBuild) {
                konst cache = incrementalCaches[this@KotlinJsModuleBuildTarget] as IncrementalJsCache

                register(
                    IncrementalDataProvider::class.java,
                    IncrementalDataProviderFromCache(cache)
                )
            }
        }
    }

    override fun compileModuleChunk(
        commonArguments: CommonCompilerArguments,
        dirtyFilesHolder: KotlinDirtySourceFilesHolder,
        environment: JpsCompilerEnvironment
    ): Boolean {
        require(chunk.representativeTarget == this)

        if (reportAndSkipCircular(environment)) return false

        konst sources = collectSourcesToCompile(dirtyFilesHolder)

        if (!sources.logFiles()) {
            return false
        }

        konst libraries = libraryFiles + dependenciesMetaFiles

        JpsKotlinCompilerRunner().runK2JsCompiler(
            commonArguments,
            module.k2JsCompilerArguments,
            module.kotlinCompilerSettings,
            environment,
            sources.allFiles,
            sources.crossCompiledFiles,
            sourceMapRoots,
            libraries,
            friendBuildTargetsMetaFiles,
            outputFile
        )

        return true
    }

    override fun doAfterBuild() {
        copyJsLibraryFilesIfNeeded()
    }

    private fun copyJsLibraryFilesIfNeeded() {
        if (module.kotlinCompilerSettings.copyJsLibraryFiles) {
            konst outputLibraryRuntimeDirectory = File(outputDir, module.kotlinCompilerSettings.outputDirectoryForJsLibraryFiles).absolutePath
            JsLibraryUtils.copyJsFilesFromLibraries(
                libraryFiles, outputLibraryRuntimeDirectory,
                copySourceMap = module.k2JsCompilerArguments.sourceMap
            )
        }
    }

    private konst sourceMapRoots: List<File>
        get() {
            // Compiler starts to produce path relative to base dirs in source maps if at least one statement is true:
            // 1) base dirs are specified;
            // 2) prefix is specified (i.e. non-empty)
            // Otherwise compiler produces paths relative to source maps location.
            // We don't have UI to configure base dirs, but we have UI to configure prefix.
            // If prefix is not specified (empty) in UI, we want to produce paths relative to source maps location
            return if (module.k2JsCompilerArguments.sourceMapPrefix.isNullOrBlank()) emptyList()
            else module.contentRootsList.urls
                .map { URI.create(it) }
                .filter { it.scheme == "file" }
                .map { File(it.path) }
        }

    konst friendBuildTargetsMetaFiles
        get() = friendBuildTargets.mapNotNull {
            (it as? KotlinJsModuleBuildTarget)?.outputMetaFile?.absoluteFile?.toString()
        }

    konst outputFile
        get() = explicitOutputPath?.let { File(it) } ?: implicitOutputFile

    private konst explicitOutputPath
        get() = if (isTests) module.testOutputFilePath else module.productionOutputFilePath

    private konst implicitOutputFile: File
        get() {
            konst suffix = if (isTests) "_test" else ""

            return File(outputDir, module.name + suffix + JS_EXT)
        }

    private konst outputFileBaseName: String
        get() = outputFile.path.substringBeforeLast(".")

    konst outputMetaFile: File
        get() = File(outputFileBaseName + META_JS_SUFFIX)

    private konst libraryFiles: List<String>
        get() = mutableListOf<String>().also { result ->
            for (library in allDependencies.libraries) {
                for (root in library.getRoots(JpsOrderRootType.COMPILED)) {
                    result.add(JpsPathUtil.urlToPath(root.url))
                }
            }
        }

    private konst dependenciesMetaFiles: List<String>
        get() = mutableListOf<String>().also { result ->
            allDependencies.processModules { module ->
                if (isTests) addDependencyMetaFile(module, result, isTests = true)

                // note: production targets should be also added as dependency to test targets
                addDependencyMetaFile(module, result, isTests = false)
            }
        }

    private fun addDependencyMetaFile(
        module: JpsModule,
        result: MutableList<String>,
        isTests: Boolean
    ) {
        konst dependencyBuildTarget = kotlinContext.targetsBinding[ModuleBuildTarget(module, isTests)]

        if (dependencyBuildTarget != this@KotlinJsModuleBuildTarget &&
            dependencyBuildTarget is KotlinJsModuleBuildTarget &&
            dependencyBuildTarget.sources.isNotEmpty()
        ) {
            konst metaFile = dependencyBuildTarget.outputMetaFile.toPath()
            if (Files.exists(metaFile)) {
                result.add(metaFile.toAbsolutePath().toString())
            }
        }
    }

    override fun createCacheStorage(paths: BuildDataPaths) =
        JpsIncrementalJsCache(jpsModuleBuildTarget, paths, kotlinContext.icContext)

    override fun updateCaches(
        dirtyFilesHolder: KotlinDirtySourceFilesHolder,
        jpsIncrementalCache: JpsIncrementalCache,
        files: List<GeneratedFile>,
        changesCollector: ChangesCollector,
        environment: JpsCompilerEnvironment
    ) {
        super.updateCaches(dirtyFilesHolder, jpsIncrementalCache, files, changesCollector, environment)

        konst incrementalResults = environment.services[IncrementalResultsConsumer::class.java] as IncrementalResultsConsumerImpl

        konst jsCache = jpsIncrementalCache as IncrementalJsCache
        jsCache.header = incrementalResults.headerMetadata

        jsCache.updateSourceToOutputMap(files)
        jsCache.compareAndUpdate(incrementalResults, changesCollector)
        jsCache.clearCacheForRemovedClasses(changesCollector)
    }
    override fun registerOutputItems(outputConsumer: ModuleLevelBuilder.OutputConsumer, outputItems: List<GeneratedFile>) {
        if (isIncrementalCompilationEnabled) {
            for (output in outputItems) {
                for (source in output.sourceFiles) {
                    outputConsumer.registerOutputFile(jpsModuleBuildTarget, File("${source.path.hashCode()}"), listOf(source.path))
                }
            }
        } else {
            super.registerOutputItems(outputConsumer, outputItems)
        }
    }
}