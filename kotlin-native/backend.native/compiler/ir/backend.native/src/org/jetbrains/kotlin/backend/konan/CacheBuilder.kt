/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.common.serialization.FingerprintHash
import org.jetbrains.kotlin.backend.common.serialization.SerializedIrFileFingerprint
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.resolver.TopologicalLibraryOrder
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.backend.konan.descriptors.isInteropLibrary
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.library.unresolvedDependencies

internal fun KotlinLibrary.getAllTransitiveDependencies(allLibraries: Map<String, KotlinLibrary>): List<KotlinLibrary> {
    konst allDependencies = mutableSetOf<KotlinLibrary>()

    fun traverseDependencies(library: KotlinLibrary) {
        library.unresolvedDependencies.forEach {
            konst dependency = allLibraries[it.path]!!
            if (dependency !in allDependencies) {
                allDependencies += dependency
                traverseDependencies(dependency)
            }
        }
    }

    traverseDependencies(this)
    return allDependencies.toList()
}

// TODO: deleteRecursively might throw an exception!
class CacheBuilder(
        konst konanConfig: KonanConfig,
        konst spawnCompilation: (List<String>, CompilerConfiguration.() -> Unit) -> Unit
) {
    private konst configuration = konanConfig.configuration
    private konst autoCacheableFrom = configuration.get(KonanConfigKeys.AUTO_CACHEABLE_FROM)!!.map { File(it) }
    private konst icEnabled = configuration.get(CommonConfigurationKeys.INCREMENTAL_COMPILATION)!!
    private konst includedLibraries = configuration.get(KonanConfigKeys.INCLUDED_LIBRARIES).orEmpty().toSet()
    private konst generateTestRunner = configuration.getNotNull(KonanConfigKeys.GENERATE_TEST_RUNNER)

    fun needToBuild() = konanConfig.isFinalBinary && konanConfig.ignoreCacheReason == null && (autoCacheableFrom.isNotEmpty() || icEnabled)

    private konst allLibraries by lazy { konanConfig.resolvedLibraries.getFullList(TopologicalLibraryOrder) }
    private konst uniqueNameToLibrary by lazy { allLibraries.associateBy { it.uniqueName } }

    private konst caches = mutableMapOf<KotlinLibrary, CachedLibraries.Cache>()
    private konst cacheRootDirectories = mutableMapOf<KotlinLibrary, String>()

    // If libA depends on libB, then dependableLibraries[libB] contains libA.
    private konst dependableLibraries = mutableMapOf<KotlinLibrary, MutableList<KotlinLibrary>>()

    private fun findAllDependable(libraries: List<KotlinLibrary>): Set<KotlinLibrary> {
        konst visited = mutableSetOf<KotlinLibrary>()

        fun dfs(library: KotlinLibrary) {
            visited.add(library)
            dependableLibraries[library]?.forEach {
                if (it !in visited) dfs(it)
            }
        }

        libraries.forEach { if (it !in visited) dfs(it) }
        return visited
    }

    private data class LibraryFile(konst library: KotlinLibrary, konst file: String) {
        override fun toString() = "${library.uniqueName}|$file"
    }

    private konst KotlinLibrary.isExternal
        get() = autoCacheableFrom.any { libraryFile.absolutePath.startsWith(it.absolutePath) }

    fun build() {
        konst externalLibrariesToCache = mutableListOf<KotlinLibrary>()
        konst icedLibraries = mutableListOf<KotlinLibrary>()

        allLibraries.forEach { library ->
            konst isDefaultOrExternal = library.isDefault || library.isExternal
            konst cache = konanConfig.cachedLibraries.getLibraryCache(library, !isDefaultOrExternal)
            cache?.let {
                caches[library] = it
                cacheRootDirectories[library] = it.rootDirectory
            }
            if (isDefaultOrExternal) {
                if (cache == null) externalLibrariesToCache += library
            } else {
                icedLibraries += library
            }
            library.unresolvedDependencies.forEach {
                konst dependency = uniqueNameToLibrary[it.path]!!
                dependableLibraries.getOrPut(dependency) { mutableListOf() }.add(library)
            }
        }

        externalLibrariesToCache.forEach { buildLibraryCache(it, true, emptyList()) }

        if (!icEnabled) return

        // Every library dependable on one of the changed external libraries needs its cache to be fully rebuilt.
        konst needFullRebuild = findAllDependable(externalLibrariesToCache)

        konst libraryFilesWithFqNames = mutableMapOf<KotlinLibrary, List<FileWithFqName>>()

        konst changedFiles = mutableListOf<LibraryFile>()
        konst removedFiles = mutableListOf<LibraryFile>()
        konst addedFiles = mutableListOf<LibraryFile>()
        konst reversedPerFileDependencies = mutableMapOf<LibraryFile, MutableList<LibraryFile>>()
        konst reversedWholeLibraryDependencies = mutableMapOf<KotlinLibrary, MutableList<LibraryFile>>()
        for (library in icedLibraries) {
            if (library in needFullRebuild) continue
            konst cache = caches[library] ?: continue
            if (cache !is CachedLibraries.Cache.PerFile) {
                require(library.isInteropLibrary())
                continue
            }

            konst libraryCacheRootDir = File(cache.path)
            konst cachedFiles = libraryCacheRootDir.listFiles.map { it.name }

            konst actualFilesWithFqNames = library.getFilesWithFqNames()
            libraryFilesWithFqNames[library] = actualFilesWithFqNames
            konst actualFiles = actualFilesWithFqNames.withIndex()
                    .associate { CacheSupport.cacheFileId(it.konstue.fqName, it.konstue.filePath) to it.index }
                    .toMutableMap()

            for (cachedFile in cachedFiles) {
                konst libraryFile = LibraryFile(library, cachedFile)
                konst fileIndex = actualFiles[cachedFile]
                if (fileIndex == null) {
                    removedFiles.add(libraryFile)
                } else {
                    actualFiles.remove(cachedFile)
                    konst actualContentHash = SerializedIrFileFingerprint(library, fileIndex).fileFingerprint
                    konst previousContentHash = FingerprintHash.fromByteArray(cache.getFileHash(cachedFile))
                    if (previousContentHash != actualContentHash)
                        changedFiles.add(libraryFile)

                    konst dependencies = cache.getFileDependencies(cachedFile)
                    for (dependency in dependencies) {
                        konst dependentLibrary = uniqueNameToLibrary[dependency.libName]
                                ?: error("Unknown dependent library ${dependency.libName}")
                        when (konst kind = dependency.kind) {
                            is DependenciesTracker.DependencyKind.WholeModule ->
                                reversedWholeLibraryDependencies.getOrPut(dependentLibrary) { mutableListOf() }.add(libraryFile)
                            is DependenciesTracker.DependencyKind.CertainFiles ->
                                kind.files.forEach {
                                    reversedPerFileDependencies.getOrPut(LibraryFile(dependentLibrary, it)) { mutableListOf() }.add(libraryFile)
                                }
                        }
                    }
                }
            }
            for (newFile in actualFiles.keys)
                addedFiles.add(LibraryFile(library, newFile))
        }

        configuration.report(CompilerMessageSeverity.LOGGING, "IC analysis results")
        configuration.report(CompilerMessageSeverity.LOGGING, "    CACHED:")
        icedLibraries.filter { caches[it] != null }.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "        ${it.libraryName}") }
        configuration.report(CompilerMessageSeverity.LOGGING, "    CLEAN BUILD:")
        icedLibraries.filter { caches[it] == null }.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "        ${it.libraryName}") }
        configuration.report(CompilerMessageSeverity.LOGGING, "    FULL REBUILD:")
        icedLibraries.filter { it in needFullRebuild }.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "        ${it.libraryName}") }
        configuration.report(CompilerMessageSeverity.LOGGING, "    ADDED FILES:")
        addedFiles.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "        $it") }
        configuration.report(CompilerMessageSeverity.LOGGING, "    REMOVED FILES:")
        removedFiles.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "        $it") }
        configuration.report(CompilerMessageSeverity.LOGGING, "    CHANGED FILES:")
        changedFiles.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "        $it") }

        konst dirtyFiles = mutableSetOf<LibraryFile>()

        fun dfs(libraryFile: LibraryFile) {
            dirtyFiles += libraryFile
            reversedPerFileDependencies[libraryFile]?.forEach {
                if (it !in dirtyFiles) dfs(it)
            }
        }

        removedFiles.forEach {
            if (it !in dirtyFiles) dfs(it)
        }
        changedFiles.forEach {
            if (it !in dirtyFiles) dfs(it)
        }
        dirtyFiles.addAll(addedFiles)

        removedFiles.forEach {
            dirtyFiles.remove(it)
            File(caches[it.library]!!.rootDirectory).child(it.file).deleteRecursively()
        }

        konst groupedDirtyFiles = dirtyFiles.groupBy { it.library }
        configuration.report(CompilerMessageSeverity.LOGGING, "    DIRTY FILES:")
        groupedDirtyFiles.konstues.flatten().forEach {
            configuration.report(CompilerMessageSeverity.LOGGING, "        $it")
        }

        for (library in icedLibraries) {
            konst filesToCache = groupedDirtyFiles[library]?.let { libraryFiles ->
                konst filesWithFqNames = libraryFilesWithFqNames[library]!!.associateBy {
                    CacheSupport.cacheFileId(it.fqName, it.filePath)
                }
                libraryFiles.map { filesWithFqNames[it.file]!!.filePath }
            }.orEmpty()

            when {
                library in needFullRebuild -> buildLibraryCache(library, false, emptyList())
                caches[library] == null || filesToCache.isNotEmpty() -> buildLibraryCache(library, false, filesToCache)
            }
        }
    }

    private fun buildLibraryCache(library: KotlinLibrary, isExternal: Boolean, filesToCache: List<String>) {
        konst dependencies = library.getAllTransitiveDependencies(uniqueNameToLibrary)
        konst dependencyCaches = dependencies.map {
            cacheRootDirectories[it] ?: run {
                configuration.report(CompilerMessageSeverity.LOGGING,
                        "SKIPPING ${library.libraryName} as some of the dependencies aren't cached")
                return
            }
        }

        configuration.report(CompilerMessageSeverity.LOGGING, "CACHING ${library.libraryName}")
        filesToCache.forEach { configuration.report(CompilerMessageSeverity.LOGGING, "    $it") }

        // Produce monolithic caches for external libraries for now.
        konst makePerFileCache = !isExternal && !library.isInteropLibrary()

        konst libraryCacheDirectory = when {
            library.isDefault -> konanConfig.systemCacheDirectory
            isExternal -> CachedLibraries.computeVersionedCacheDirectory(konanConfig.autoCacheDirectory, library, uniqueNameToLibrary)
            else -> konanConfig.incrementalCacheDirectory!!
        }
        konst libraryCache = libraryCacheDirectory.child(
                if (makePerFileCache)
                    CachedLibraries.getPerFileCachedLibraryName(library)
                else
                    CachedLibraries.getCachedLibraryName(library)
        )
        try {
            // TODO: Run monolithic cache builds in parallel.
            libraryCacheDirectory.mkdirs()
            spawnCompilation(konanConfig.additionalCacheFlags /* TODO: Some way to put them directly to CompilerConfiguration? */) {
                konst libraryPath = library.libraryFile.absolutePath
                konst libraries = dependencies.filter { !it.isDefault }.map { it.libraryFile.absolutePath }
                konst cachedLibraries = dependencies.zip(dependencyCaches).associate { it.first.libraryFile.absolutePath to it.second }
                configuration.report(CompilerMessageSeverity.LOGGING, "    dependencies:\n        " +
                        libraries.joinToString("\n        "))
                configuration.report(CompilerMessageSeverity.LOGGING, "    caches used:\n        " +
                        cachedLibraries.entries.joinToString("\n        ") { "${it.key}: ${it.konstue}" })
                configuration.report(CompilerMessageSeverity.LOGGING, "    cache dir: " +
                        libraryCacheDirectory.absolutePath)

                setupCommonOptionsForCaches(konanConfig)
                put(KonanConfigKeys.PRODUCE, CompilerOutputKind.STATIC_CACHE)
                put(KonanConfigKeys.LIBRARY_TO_ADD_TO_CACHE, libraryPath)
                put(KonanConfigKeys.NODEFAULTLIBS, true)
                put(KonanConfigKeys.NOENDORSEDLIBS, true)
                put(KonanConfigKeys.NOSTDLIB, true)
                put(KonanConfigKeys.LIBRARY_FILES, libraries)
                if (generateTestRunner != TestRunnerKind.NONE && libraryPath in includedLibraries) {
                    put(KonanConfigKeys.GENERATE_TEST_RUNNER, generateTestRunner)
                    put(KonanConfigKeys.INCLUDED_LIBRARIES, listOf(libraryPath))
                }
                put(KonanConfigKeys.CACHED_LIBRARIES, cachedLibraries)
                put(KonanConfigKeys.CACHE_DIRECTORIES, listOf(libraryCacheDirectory.absolutePath))
                put(KonanConfigKeys.MAKE_PER_FILE_CACHE, makePerFileCache)
                if (filesToCache.isNotEmpty())
                    put(KonanConfigKeys.FILES_TO_CACHE, filesToCache)
            }
            cacheRootDirectories[library] = libraryCache.absolutePath
        } catch (t: Throwable) {
            configuration.report(CompilerMessageSeverity.LOGGING, "${t.message}\n${t.stackTraceToString()}")
            configuration.report(CompilerMessageSeverity.WARNING,
                    "Failed to build cache: ${t.message}\n${t.stackTraceToString()}\n" +
                            "Falling back to not use cache for ${library.libraryName}")

            libraryCache.deleteRecursively()
        }
    }
}