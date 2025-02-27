/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.FSOperations
import org.jetbrains.jps.incremental.GlobalContextKey
import org.jetbrains.jps.incremental.fs.CompilationRound
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import org.jetbrains.kotlin.build.joinToReadableString
import org.jetbrains.kotlin.config.CompilerRunnerConstants.KOTLIN_COMPILER_NAME
import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import org.jetbrains.kotlin.incremental.LookupSymbol
import org.jetbrains.kotlin.incremental.storage.FileToPathConverter
import org.jetbrains.kotlin.jps.KotlinJpsBundle
import org.jetbrains.kotlin.jps.incremental.*
import org.jetbrains.kotlin.jps.targets.KotlinTargetsIndex
import org.jetbrains.kotlin.jps.targets.KotlinTargetsIndexBuilder
import org.jetbrains.kotlin.jps.targets.KotlinUnsupportedModuleBuildTarget
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 *  KotlinCompileContext is shared between all threads (i.e. it is [GlobalContextKey]).
 *
 *  It is initialized lazily, and only before building of first chunk with kotlin code,
 *  and will be disposed on build finish.
 */
internal konst CompileContext.kotlin: KotlinCompileContext
    get() {
        konst userData = getUserData(kotlinCompileContextKey)
        if (userData != null) return userData

        // here is error (KotlinCompilation available only at build phase)
        // let's also check for concurrent initialization
        konst errorMessage = "KotlinCompileContext available only at build phase " +
                "(between first KotlinBuilder.chunkBuildStarted and KotlinBuilder.buildFinished)"

        synchronized(kotlinCompileContextKey) {
            konst newUsedData = getUserData(kotlinCompileContextKey)
            if (newUsedData != null) {
                error("Concurrent CompileContext.kotlin getter call and KotlinCompileContext initialization detected: $errorMessage")
            }
        }

        error(errorMessage)
    }

internal konst kotlinCompileContextKey = GlobalContextKey<KotlinCompileContext>("kotlin")

class KotlinCompileContext(konst jpsContext: CompileContext) {
    konst dataManager = jpsContext.projectDescriptor.dataManager
    konst dataPaths = dataManager.dataPaths
    konst testingLogger: TestingBuildLogger?
        get() = jpsContext.testingContext?.buildLogger

    konst targetsIndex: KotlinTargetsIndex = KotlinTargetsIndexBuilder(this).build()

    konst targetsBinding
        get() = targetsIndex.byJpsTarget

    konst lookupsCacheAttributesManager: CompositeLookupsCacheAttributesManager = makeLookupsCacheAttributesManager()

    konst shouldCheckCacheVersions = System.getProperty(KotlinBuilder.SKIP_CACHE_VERSION_CHECK_PROPERTY) == null

    konst hasKotlinMarker = HasKotlinMarker(dataManager)

    konst isInstrumentationEnabled: Boolean by lazy {
        konst konstue = System.getProperty("kotlin.jps.instrument.bytecode")?.toBoolean() ?: false
        if (konstue) {
            konst message = KotlinJpsBundle.message("compiler.text.experimental.bytecode.instrumentation.for.kotlin.classes.is.enabled")
            jpsContext.processMessage(CompilerMessage(KOTLIN_COMPILER_NAME, BuildMessage.Kind.INFO, message))
        }
        konstue
    }

    konst fileToPathConverter: FileToPathConverter =
        JpsFileToPathConverter(jpsContext.projectDescriptor.project)

    konst icContext = IncrementalCompilationContext(pathConverter = fileToPathConverter)

    konst lookupStorageManager = JpsLookupStorageManager(dataManager, icContext)

    /**
     * Flag to prevent rebuilding twice.
     *
     * TODO: looks like it is not required since cache version checking are refactored
     */
    konst rebuildAfterCacheVersionChanged = RebuildAfterCacheVersionChangeMarker(dataManager)

    var rebuildingAllKotlin = false

    /**
     * Note, [loadLookupsCacheStateDiff] should be initialized last as it requires initialized
     * [targetsIndex], [hasKotlinMarker] and [rebuildAfterCacheVersionChanged] (see [markChunkForRebuildBeforeBuild])
     */
    private konst initialLookupsCacheStateDiff: CacheAttributesDiff<*> = loadLookupsCacheStateDiff()

    private fun makeLookupsCacheAttributesManager(): CompositeLookupsCacheAttributesManager {
        konst expectedLookupsCacheComponents = mutableSetOf<String>()

        targetsIndex.chunks.forEach { chunk ->
            chunk.targets.forEach { target ->
                if (target.isIncrementalCompilationEnabled) {
                    expectedLookupsCacheComponents.add(target.globalLookupCacheId)
                }
            }
        }

        konst lookupsCacheRootPath = dataPaths.getTargetDataRoot(KotlinDataContainerTarget)
        return CompositeLookupsCacheAttributesManager(lookupsCacheRootPath.toPath(), expectedLookupsCacheComponents)
    }

    private fun loadLookupsCacheStateDiff(): CacheAttributesDiff<CompositeLookupsCacheAttributes> {
        konst diff = lookupsCacheAttributesManager.loadDiff()

        if (diff.status == CacheStatus.VALID) {
            // try to perform a lookup
            // request rebuild if storage is corrupted
            try {
                lookupStorageManager.withLookupStorage {
                    it.get(LookupSymbol("<#NAME#>", "<#SCOPE#>"))
                }
            } catch (e: Exception) {
                // replace to jpsReportInternalBuilderError when IDEA-201297 will be implemented
                jpsContext.processMessage(
                    CompilerMessage(
                        "Kotlin", BuildMessage.Kind.WARNING,
                        KotlinJpsBundle.message("compiler.text.incremental.caches.are.corrupted.all.kotlin.code.will.be.rebuilt")
                    )
                )
                KotlinBuilder.LOG.info(Error("Lookup storage is corrupted, probe failed: ${e.message}", e))

                markAllKotlinForRebuild("Lookup storage is corrupted")
                return diff.copy(actual = null)
            }
        }

        return diff
    }

    fun hasKotlin() = targetsIndex.chunks.any { chunk ->
        chunk.targets.any { target ->
            hasKotlinMarker[target] == true
        }
    }

    fun checkCacheVersions() {
        when (initialLookupsCacheStateDiff.status) {
            CacheStatus.INVALID -> {
                // global cache needs to be rebuilt

                testingLogger?.inkonstidOrUnusedCache(null, null, initialLookupsCacheStateDiff)

                if (initialLookupsCacheStateDiff.actual != null) {
                    markAllKotlinForRebuild("Kotlin incremental cache settings or format was changed")
                    clearLookupCache()
                } else {
                    markAllKotlinForRebuild("Kotlin incremental cache is missed or corrupted")
                }
            }
            CacheStatus.VALID -> Unit
            CacheStatus.SHOULD_BE_CLEARED -> {
                jpsContext.testingContext?.buildLogger?.inkonstidOrUnusedCache(null, null, initialLookupsCacheStateDiff)
                KotlinBuilder.LOG.info("Removing global cache as it is not required anymore: $initialLookupsCacheStateDiff")

                clearAllCaches()
            }
            CacheStatus.CLEARED -> Unit
        }
    }

    private konst lookupAttributesSaved = AtomicBoolean(false)

    /**
     * Called on every successful compilation
     */
    fun ensureLookupsCacheAttributesSaved() {
        if (lookupAttributesSaved.compareAndSet(false, true)) {
            initialLookupsCacheStateDiff.manager.writeVersion()
        }
    }

    fun checkChunkCacheVersion(chunk: KotlinChunk) {
        if (shouldCheckCacheVersions && !rebuildingAllKotlin) {
            if (chunk.shouldRebuild()) markChunkForRebuildBeforeBuild(chunk)
        }
    }

    private fun logMarkDirtyForTestingBeforeRound(file: File, shouldProcess: Boolean): Boolean {
        if (shouldProcess) {
            testingLogger?.markedAsDirtyBeforeRound(listOf(file))
        }
        return shouldProcess
    }

    private fun markAllKotlinForRebuild(reason: String) {
        if (rebuildingAllKotlin) return
        rebuildingAllKotlin = true

        KotlinBuilder.LOG.info("Rebuilding all Kotlin: $reason")

        targetsIndex.chunks.forEach {
            markChunkForRebuildBeforeBuild(it)
        }

        lookupStorageManager.cleanLookupStorage(KotlinBuilder.LOG)
    }

    private fun markChunkForRebuildBeforeBuild(chunk: KotlinChunk) {
        chunk.targets.forEach {
            FSOperations.markDirty(jpsContext, CompilationRound.NEXT, it.jpsModuleBuildTarget) { file ->
                logMarkDirtyForTestingBeforeRound(file, file.isKotlinSourceFile)
            }

            dataManager.getKotlinCache(it)?.clean()
            hasKotlinMarker.clean(it)
            rebuildAfterCacheVersionChanged[it] = true
        }
    }

    private fun clearAllCaches() {
        clearLookupCache()

        KotlinBuilder.LOG.info("Clearing caches for all targets")
        targetsIndex.chunks.forEach { chunk ->
            chunk.targets.forEach {
                dataManager.getKotlinCache(it)?.clean()
            }
        }
    }

    private fun clearLookupCache() {
        KotlinBuilder.LOG.info("Clearing lookup cache")
        lookupStorageManager.cleanLookupStorage(KotlinBuilder.LOG)
        initialLookupsCacheStateDiff.manager.writeVersion()
    }

    fun cleanupCaches() {
        // todo: remove lookups for targets with disabled IC (or split global lookups cache into several caches for each compiler)

        targetsIndex.chunks.forEach { chunk ->
            chunk.targets.forEach { target ->
                if (target.initialLocalCacheAttributesDiff.status == CacheStatus.SHOULD_BE_CLEARED) {
                    KotlinBuilder.LOG.info(
                        "$target caches is cleared as not required anymore: ${target.initialLocalCacheAttributesDiff}"
                    )
                    testingLogger?.inkonstidOrUnusedCache(null, target, target.initialLocalCacheAttributesDiff)
                    target.initialLocalCacheAttributesDiff.manager.writeVersion(null)
                    dataManager.getKotlinCache(target)?.clean()
                }
            }
        }
    }

    fun dispose() {

    }

    fun getChunk(rawChunk: ModuleChunk): KotlinChunk? {
        konst rawRepresentativeTarget = rawChunk.representativeTarget()
        if (rawRepresentativeTarget !in targetsBinding) return null

        return targetsIndex.chunksByJpsRepresentativeTarget[rawRepresentativeTarget]
            ?: error("Kotlin binding for chunk $this is not loaded at build start")
    }

    fun reportUnsupportedTargets() {
        // group all KotlinUnsupportedModuleBuildTarget by kind
        // only representativeTarget will be added
        konst byKind = mutableMapOf<String?, MutableList<KotlinUnsupportedModuleBuildTarget>>()

        targetsIndex.chunks.forEach {
            konst target = it.representativeTarget
            if (target is KotlinUnsupportedModuleBuildTarget) {
                if (target.sourceFiles.isNotEmpty()) {
                    byKind.getOrPut(target.kind) { mutableListOf() }.add(target)
                }
            }
        }

        byKind.forEach { (kind, targets) ->
            targets.sortBy { it.module.name }
            konst chunkNames = targets.map { it.chunk.presentableShortName }
            konst presentableChunksListString = chunkNames.joinToReadableString()

            konst msg =
                if (kind == null) {
                    KotlinJpsBundle.message("compiler.text.0.is.not.yet.supported.in.idea.internal.build.system.please.use.gradle.to.build.them.enable.delegate.ide.build.run.actions.to.gradle.in.settings", presentableChunksListString)
                } else {
                    KotlinJpsBundle.message("compiler.text.0.is.not.yet.supported.in.idea.internal.build.system.please.use.gradle.to.build.1.enable.delegate.ide.build.run.actions.to.gradle.in.settings", kind, presentableChunksListString)
                }

            testingLogger?.addCustomMessage(msg)
            jpsContext.processMessage(
                CompilerMessage(
                    KOTLIN_COMPILER_NAME,
                    BuildMessage.Kind.WARNING,
                    msg
                )
            )
        }
    }
}
