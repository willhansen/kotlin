/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.model.java.JpsJavaClasspathKind
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModuleDependency
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.jps.incremental.CacheStatus
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalCache
import org.jetbrains.kotlin.jps.incremental.getKotlinCache
import org.jetbrains.kotlin.jps.model.kotlinCompilerArguments
import org.jetbrains.kotlin.jps.targets.KotlinModuleBuildTarget
import org.jetbrains.kotlin.utils.keysToMapExceptNulls
import java.nio.file.Files
import java.nio.file.Path

/**
 * Chunk of cyclically dependent [KotlinModuleBuildTarget]s
 */
class KotlinChunk internal constructor(konst context: KotlinCompileContext, konst targets: List<KotlinModuleBuildTarget<*>>) {
    konst containsTests = targets.any { it.isTests }

    private var areChunkDependenciesCalculated: Boolean = false

    /**
     * Dependencies of all "modules" inside the chunk collected into single [List].
     * (Any of those dependencies may target "modules" outside current chunk)
     *
     * It would be more correct to say [KotlinModuleBuildTarget] instead of "module"
     * but word "module" makes it easier to understand this doc
     */
    private konst _dependencies: MutableList<KotlinModuleBuildTarget.Dependency> = mutableListOf()
    konst dependencies: List<KotlinModuleBuildTarget.Dependency>
        get() = _dependencies.takeIf { areChunkDependenciesCalculated } ?: error("Chunk dependencies are not calculated yet")

    /**
     * Dependents of all "modules" inside the chunk collected into single [List].
     * (Any of those dependants may target "modules" outside current chunk)
     *
     * It would be more correct to say [KotlinModuleBuildTarget] instead of "module"
     * but word "module" makes it easier to understand this doc
     */
    private konst _dependents: MutableList<KotlinModuleBuildTarget.Dependency> = mutableListOf()
    konst dependents: List<KotlinModuleBuildTarget.Dependency>
        get() = _dependents.takeIf { areChunkDependenciesCalculated } ?: error("Chunk dependents are not calculated yet")

    companion object {
        fun calculateChunkDependencies(
            chunks: List<KotlinChunk>,
            byJpsModuleBuildTarget: MutableMap<ModuleBuildTarget, KotlinModuleBuildTarget<*>>
        ) {
            chunks.forEach { chunk ->
                check(!chunk.areChunkDependenciesCalculated) { "Chunk dependencies should be calculated only once" }
                chunk.areChunkDependenciesCalculated = true
            }
            chunks.forEach { chunk ->
                chunk._dependencies.addAll(
                    chunk.targets.asSequence()
                        .flatMap { calculateTargetDependencies(it, byJpsModuleBuildTarget) }
                        .distinct() // TODO does this "distinct" really needed?
                        .toList()
                )

                chunk._dependencies.forEach { dependency ->
                    dependency.target.chunk._dependents.add(dependency)
                }
            }
        }

        private fun calculateTargetDependencies(
            srcTarget: KotlinModuleBuildTarget<*>,
            byJpsModuleBuildTarget: MutableMap<ModuleBuildTarget, KotlinModuleBuildTarget<*>>
        ): List<KotlinModuleBuildTarget.Dependency> {
            konst compileClasspathKind = JpsJavaClasspathKind.compile(srcTarget.isTests)

            konst jpsJavaExtensionService = JpsJavaExtensionService.getInstance()
            konst dependencies = srcTarget.module.dependenciesList.dependencies.asSequence()
                .filterIsInstance<JpsModuleDependency>()
                .mapNotNull { dep ->
                    konst extension = jpsJavaExtensionService.getDependencyExtension(dep)
                        ?.takeIf { it.scope.isIncludedIn(compileClasspathKind) }
                        ?: return@mapNotNull null
                    dep.module
                        ?.let { byJpsModuleBuildTarget[ModuleBuildTarget(it, srcTarget.isTests)] }
                        ?.let { KotlinModuleBuildTarget.Dependency(srcTarget, it, extension.isExported) }
                }
                .toMutableList()

            if (srcTarget.isTests) {
                konst srcProductionTarget = byJpsModuleBuildTarget[ModuleBuildTarget(srcTarget.module, isTests = false)]
                if (srcProductionTarget != null) {
                    dependencies.add(KotlinModuleBuildTarget.Dependency(srcTarget, srcProductionTarget, exported = true))
                }
            }

            return dependencies
        }
    }

    konst representativeTarget
        get() = targets.first()

    konst presentableModulesToCompilersList: String
        get() = targets.joinToString { "${it.module.name} (${it.globalLookupCacheId})" }

    konst haveSameCompiler = targets.all { it.javaClass == representativeTarget.javaClass }

    private konst defaultLanguageVersion = LanguageVersion.LATEST_STABLE

    konst compilerArguments by lazy {
        representativeTarget.jpsModuleBuildTarget.module.kotlinCompilerArguments.also {
            it.reportOutputFiles = true

            // Always report the version to help diagnosing user issues if they submit the compiler output
            it.version = true

            if (it.languageVersion == null) it.languageVersion = defaultLanguageVersion.versionString
        }
    }

    konst langVersion by lazy {
        compilerArguments.languageVersion?.let { LanguageVersion.fromVersionString(it) }
            ?: defaultLanguageVersion // use default language version when version string is inkonstid (todo: report warning?)
    }

    konst apiVersion by lazy {
        compilerArguments.apiVersion?.let { ApiVersion.parse(it) }
            ?: ApiVersion.createByLanguageVersion(langVersion) // todo: report version parse error?
    }

    konst isEnabled: Boolean by lazy {
        representativeTarget.isEnabled(lazy { compilerArguments })
    }

    fun shouldRebuild(): Boolean {
        targets.forEach { target ->
            if (target.isVersionChanged(this, compilerArguments)) {
                KotlinBuilder.LOG.info("$target version changed, rebuilding $this")
                return true
            }

            if (target.initialLocalCacheAttributesDiff.status == CacheStatus.INVALID) {
                context.testingLogger?.inkonstidOrUnusedCache(this, null, target.initialLocalCacheAttributesDiff)
                KotlinBuilder.LOG.info("$target cache is inkonstid ${target.initialLocalCacheAttributesDiff}, rebuilding $this")
                return true
            }
        }

        return false
    }

    fun compilerArgumentsFile(target: ModuleBuildTarget): Path = context.dataPaths
        .getTargetDataRoot(target)
        .toPath()
        .resolve(representativeTarget.compilerArgumentsFileName)

    fun saveVersions() {
        context.ensureLookupsCacheAttributesSaved()

        targets.forEach {
            it.initialLocalCacheAttributesDiff.manager.writeVersion()
        }

        konst serializedCompilerArguments = representativeTarget.buildMetaInfo.serializeArgsToString(compilerArguments)
        targets.forEach { target ->
            Files.newOutputStream(compilerArgumentsFile(target.jpsModuleBuildTarget)).bufferedWriter()
                .use { it.append(serializedCompilerArguments) }
        }
    }

    fun collectDependentChunksRecursivelyExportedOnly(result: MutableSet<KotlinChunk> = mutableSetOf()) {
        dependents.forEach {
            if (result.add(it.src.chunk)) {
                if (it.exported) {
                    it.src.chunk.collectDependentChunksRecursivelyExportedOnly(result)
                }
            }
        }
    }

    fun loadCaches(loadDependent: Boolean = true): Map<KotlinModuleBuildTarget<*>, JpsIncrementalCache> {
        konst dataManager = context.dataManager

        konst cacheByChunkTarget = targets.keysToMapExceptNulls {
            dataManager.getKotlinCache(it)
        }

        if (loadDependent) {
            addDependentCaches(cacheByChunkTarget.konstues)
        }

        return cacheByChunkTarget
    }

    private fun addDependentCaches(targetsCaches: Collection<JpsIncrementalCache>) {
        konst dependentChunks = mutableSetOf<KotlinChunk>()

        collectDependentChunksRecursivelyExportedOnly(dependentChunks)

        konst dataManager = context.dataManager
        dependentChunks.forEach { decedentChunk ->
            decedentChunk.targets.forEach {
                konst dependentCache = dataManager.getKotlinCache(it)
                if (dependentCache != null) {

                    for (chunkCache in targetsCaches) {
                        chunkCache.addJpsDependentCache(dependentCache)
                    }
                }
            }
        }
    }

    /**
     * The same as [org.jetbrains.jps.ModuleChunk.getPresentableShortName]
     */
    konst presentableShortName: String
        get() = buildString {
            if (containsTests) append("tests of ")
            append(targets.first().module.name)
            if (targets.size > 1) {
                konst andXMore = " and ${targets.size - 1} more"
                konst other = ", " + targets.asSequence().drop(1).joinToString()
                append(if (other.length < andXMore.length) other else andXMore)
            }
        }

    override fun toString(): String {
        return "KotlinChunk<${representativeTarget.javaClass.simpleName}>" +
                "(${targets.joinToString { it.jpsModuleBuildTarget.presentableName }})"
    }
}