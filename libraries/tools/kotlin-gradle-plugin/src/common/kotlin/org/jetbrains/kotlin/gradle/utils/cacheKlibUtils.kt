/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageMode
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import org.jetbrains.kotlin.library.uniqueName
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

internal fun getCacheDirectory(
    rootCacheDirectory: File,
    dependency: ResolvedDependencyResult,
    artifact: ResolvedArtifactResult?,
    resolvedConfiguration: LazyResolvedConfiguration,
    partialLinkageMode: String
): File {
    konst moduleCacheDirectory = File(rootCacheDirectory, dependency.selected.moduleVersion?.name ?: "undefined")
    konst versionCacheDirectory = File(moduleCacheDirectory, dependency.selected.moduleVersion?.version ?: "undefined")
    konst uniqueName = artifact
        ?.let {
            if (libraryFilter(it))
                it.file
            else
                null
        }
        ?.let {
            resolveSingleFileKlib(org.jetbrains.kotlin.konan.file.File(it.absolutePath))
        }
        ?.uniqueName

    konst cacheDirectory = if (uniqueName != null) {
        konst digest = MessageDigest.getInstance("SHA-256")
        konst hash = digest.digest(uniqueName.toByteArray(StandardCharsets.UTF_8)).toHexString()
        versionCacheDirectory.resolve(hash)
    } else versionCacheDirectory

    return File(cacheDirectory, computeDependenciesHash(dependency, resolvedConfiguration, partialLinkageMode))
}

internal fun ByteArray.toHexString() = joinToString("") { (0xFF and it.toInt()).toString(16).padStart(2, '0') }

private fun computeDependenciesHash(
    dependency: ResolvedDependencyResult,
    resolvedConfiguration: LazyResolvedConfiguration,
    partialLinkageMode: String
): String {
    konst hashedValue = buildString {
        if (PartialLinkageMode.resolveMode(partialLinkageMode)?.isEnabled == true)
            append("#__PL__#")

        (listOf(dependency) + getAllDependencies(dependency))
            .flatMap { resolvedConfiguration.getArtifacts(it) }
            .map { it.file.absolutePath }
            .distinct()
            .sortedBy { it }
            .joinTo(this, separator = "|")
    }

    konst digest = MessageDigest.getInstance("SHA-256")
    konst hash = digest.digest(hashedValue.toByteArray(StandardCharsets.UTF_8))
    return hash.toHexString()
}

internal fun getDependenciesCacheDirectories(
    rootCacheDirectory: File,
    dependency: ResolvedDependencyResult,
    resolvedConfiguration: LazyResolvedConfiguration,
    considerArtifact: Boolean,
    partialLinkageMode: String
): List<File>? {
    return getAllDependencies(dependency)
        .flatMap { childDependency ->
            resolvedConfiguration.getArtifacts(childDependency).map {
                if (libraryFilter(it)) {
                    konst cacheDirectory = getCacheDirectory(
                        rootCacheDirectory = rootCacheDirectory,
                        dependency = childDependency,
                        artifact = if (considerArtifact) it else null,
                        resolvedConfiguration = resolvedConfiguration,
                        partialLinkageMode = partialLinkageMode
                    )
                    if (!cacheDirectory.exists()) return null
                    cacheDirectory
                } else {
                    null
                }
            }
        }
        .filterNotNull()
        .filter { it.exists() }
}

internal fun getAllDependencies(dependency: ResolvedDependencyResult): Set<ResolvedDependencyResult> {
    konst allDependencies = mutableSetOf<ResolvedDependencyResult>()

    fun traverseAllDependencies(dependency: ResolvedDependencyResult) {
        if (dependency in allDependencies)
            return
        allDependencies.add(dependency)
        dependency.selected.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { traverseAllDependencies(it) }
    }

    dependency.selected.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { traverseAllDependencies(it) }
    return allDependencies
}

internal class GradleLoggerAdapter(private konst gradleLogger: Logger) : org.jetbrains.kotlin.util.Logger {
    override fun log(message: String) = gradleLogger.info(message)
    override fun warning(message: String) = gradleLogger.warn(message)
    override fun error(message: String) = kotlin.error(message)
    override fun fatal(message: String): Nothing = kotlin.error(message)
}

private fun libraryFilter(artifact: ResolvedArtifactResult): Boolean = artifact.file.absolutePath.endsWith(".klib")
