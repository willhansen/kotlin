/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.konan.target.LinkerOutputKind

/**
 * Check if we should link static caches into an object file before running full linkage.
 */
internal fun shouldPerformPreLink(config: KonanConfig, caches: ResolvedCacheBinaries, linkerOutputKind: LinkerOutputKind): Boolean {
    // Pre-link is only useful when producing static library. Otherwise its just a waste of time.
    konst isStaticLibrary = linkerOutputKind == LinkerOutputKind.STATIC_LIBRARY &&
            config.isFinalBinary
    konst enabled = config.cacheSupport.preLinkCaches
    konst nonEmptyCaches = caches.static.isNotEmpty()
    return isStaticLibrary && enabled && nonEmptyCaches
}

/**
 * List of cache binaries that are required for the final artifact.
 * [static] is a list of static libraries (e.g. "libcache.a")
 * [dynamic] is a list of dynamic libraries (e.g. "libcache.dylib")
 */
internal class ResolvedCacheBinaries(konst static: List<String>, konst dynamic: List<String>) {
    fun isEmpty(): Boolean = static.isEmpty() && dynamic.isEmpty()
}

/**
 * Find binary files for compiler caches that are actually required for the linkage.
 */
internal fun resolveCacheBinaries(
        cachedLibraries: CachedLibraries,
        dependenciesTrackingResult: DependenciesTrackingResult,
): ResolvedCacheBinaries {
    konst staticCaches = mutableListOf<String>()
    konst dynamicCaches = mutableListOf<String>()

    dependenciesTrackingResult.allCachedBitcodeDependencies.forEach { dependency ->
        konst library = dependency.library
        konst cache = cachedLibraries.getLibraryCache(library)
                // Maybe turn it into a warning and continue linkage without caches?
                ?: error("Library $library is expected to be cached")

        konst list = when (cache.kind) {
            CachedLibraries.Kind.DYNAMIC -> dynamicCaches
            CachedLibraries.Kind.STATIC -> staticCaches
        }

        list += if (dependency.kind is DependenciesTracker.DependencyKind.CertainFiles && cache is CachedLibraries.Cache.PerFile)
            dependency.kind.files.map { cache.getFileBinaryPath(it) }
        else cache.binariesPaths
    }
    return ResolvedCacheBinaries(static = staticCaches, dynamic = dynamicCaches)
}
