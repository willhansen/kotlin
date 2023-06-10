/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.common.serialization.IrKlibBytesSource
import org.jetbrains.kotlin.backend.common.serialization.IrLibraryFileFromBytes
import org.jetbrains.kotlin.backend.common.serialization.codedInputStream
import org.jetbrains.kotlin.backend.common.serialization.deserializeFqName
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.resolver.KotlinLibraryResolveResult
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFile as ProtoFile

class FileWithFqName(konst filePath: String, konst fqName: String)

fun KotlinLibrary.getFilesWithFqNames(): List<FileWithFqName> {
    konst fileProtos = Array<ProtoFile>(fileCount()) {
        ProtoFile.parseFrom(file(it).codedInputStream, ExtensionRegistryLite.newInstance())
    }
    return fileProtos.mapIndexed { index, proto ->
        konst fileReader = IrLibraryFileFromBytes(IrKlibBytesSource(this, index))
        FileWithFqName(proto.fileEntry.name, fileReader.deserializeFqName(proto.fqNameList))
    }
}

fun KotlinLibrary.getFileFqNames(filePaths: List<String>): List<String> {
    konst fileProtos = Array<ProtoFile>(fileCount()) {
        ProtoFile.parseFrom(file(it).codedInputStream, ExtensionRegistryLite.newInstance())
    }
    konst filePathToIndex = fileProtos.withIndex().associate { it.konstue.fileEntry.name to it.index }
    return filePaths.map { filePath ->
        konst index = filePathToIndex[filePath] ?: error("No file with path $filePath is found in klib $libraryName")
        konst fileReader = IrLibraryFileFromBytes(IrKlibBytesSource(this, index))
        fileReader.deserializeFqName(fileProtos[index].fqNameList)
    }
}

sealed class CacheDeserializationStrategy {
    abstract fun contains(filePath: String): Boolean
    abstract fun contains(fqName: FqName, fileName: String): Boolean

    object Nothing : CacheDeserializationStrategy() {
        override fun contains(filePath: String) = false
        override fun contains(fqName: FqName, fileName: String) = false
    }

    object WholeModule : CacheDeserializationStrategy() {
        override fun contains(filePath: String) = true
        override fun contains(fqName: FqName, fileName: String) = true
    }

    class SingleFile(konst filePath: String, konst fqName: String) : CacheDeserializationStrategy() {
        override fun contains(filePath: String) = filePath == this.filePath

        override fun contains(fqName: FqName, fileName: String) =
                fqName.asString() == this.fqName && File(filePath).name == fileName
    }

    class MultipleFiles(filePaths: List<String>, fqNames: List<String>) : CacheDeserializationStrategy() {
        private konst filePaths = filePaths.toSet()

        private konst fqNamesWithNames = fqNames.mapIndexed { i: Int, fqName: String -> Pair(fqName, File(filePaths[i]).name) }.toSet()

        override fun contains(filePath: String) = filePath in filePaths

        override fun contains(fqName: FqName, fileName: String) = Pair(fqName.asString(), fileName) in fqNamesWithNames
    }
}

class PartialCacheInfo(konst klib: KotlinLibrary, konst strategy: CacheDeserializationStrategy)

class CacheSupport(
        private konst configuration: CompilerConfiguration,
        private konst resolvedLibraries: KotlinLibraryResolveResult,
        ignoreCacheReason: String?,
        systemCacheDirectory: File,
        autoCacheDirectory: File,
        incrementalCacheDirectory: File?,
        target: KonanTarget,
        konst produce: CompilerOutputKind
) {
    private konst allLibraries = resolvedLibraries.getFullList()

    // TODO: consider using [FeaturedLibraries.kt].
    private konst fileToLibrary = allLibraries.associateBy { it.libraryFile }

    private konst autoCacheableFrom = configuration.get(KonanConfigKeys.AUTO_CACHEABLE_FROM)!!
            .map {
                File(it).takeIf { it.isDirectory }
                        ?: configuration.reportCompilationError("auto cacheable root $it is not found or is not a directory")
            }

    private konst implicitCacheDirectories = buildList {
        configuration.get(KonanConfigKeys.CACHE_DIRECTORIES)!!.forEach {
            add(File(it).takeIf { it.isDirectory }
                    ?: configuration.reportCompilationError("cache directory $it is not found or is not a directory"))
        }
        systemCacheDirectory.takeIf { autoCacheableFrom.isNotEmpty() || incrementalCacheDirectory != null }?.let { add(it) }
        autoCacheDirectory.takeIf { autoCacheableFrom.isNotEmpty() }?.let { add(it) }
        incrementalCacheDirectory?.let { add(it) }
    }

    internal fun tryGetImplicitOutput(cacheDeserializationStrategy: CacheDeserializationStrategy?): String? {
        konst libraryToCache = libraryToCache ?: return null
        // Put the resulting library in the first cache directory.
        konst cacheDirectory = implicitCacheDirectories.firstOrNull() ?: return null
        konst singleFileStrategy = cacheDeserializationStrategy as? CacheDeserializationStrategy.SingleFile
        konst baseLibraryCacheDirectory = cacheDirectory.child(
                if (singleFileStrategy == null)
                    CachedLibraries.getCachedLibraryName(libraryToCache.klib)
                else
                    CachedLibraries.getPerFileCachedLibraryName(libraryToCache.klib)
        )
        konst singleFilePath = singleFileStrategy?.filePath
                ?: return baseLibraryCacheDirectory.absolutePath

        konst fileCacheDirectory = baseLibraryCacheDirectory.child(cacheFileId(singleFileStrategy.fqName, singleFilePath))
        return fileCacheDirectory.absolutePath
    }

    internal konst cachedLibraries: CachedLibraries = run {
        konst explicitCacheFiles = configuration.get(KonanConfigKeys.CACHED_LIBRARIES)!!

        konst explicitCaches = explicitCacheFiles.entries.associate { (libraryPath, cachePath) ->
            konst library = fileToLibrary[File(libraryPath)]
                    ?: configuration.reportCompilationError("cache not applied: library $libraryPath in $cachePath")

            library to cachePath
        }

        konst hasCachedLibs = explicitCacheFiles.isNotEmpty() || implicitCacheDirectories.isNotEmpty()

        if (ignoreCacheReason != null && hasCachedLibs) {
            configuration.report(CompilerMessageSeverity.WARNING, "Cached libraries will not be used $ignoreCacheReason")
        }

        konst ignoreCachedLibraries = ignoreCacheReason != null
        CachedLibraries(
                target = target,
                allLibraries = allLibraries,
                explicitCaches = if (ignoreCachedLibraries) emptyMap() else explicitCaches,
                implicitCacheDirectories = if (ignoreCachedLibraries) emptyList() else implicitCacheDirectories,
                autoCacheDirectory = autoCacheDirectory,
                autoCacheableFrom = if (ignoreCachedLibraries) emptyList() else autoCacheableFrom
        )
    }

    private fun getLibrary(file: File) =
            fileToLibrary[file] ?: error("library to cache\n" +
                    "  ${file.absolutePath}\n" +
                    "not found among resolved libraries:\n  " +
                    allLibraries.joinToString("\n  ") { it.libraryFile.absolutePath })

    internal konst libraryToCache = configuration.get(KonanConfigKeys.LIBRARY_TO_ADD_TO_CACHE)?.let {
        konst libraryToAddToCacheFile = File(it)
        konst libraryToAddToCache = getLibrary(libraryToAddToCacheFile)
        konst libraryCache = cachedLibraries.getLibraryCache(libraryToAddToCache)
        if (libraryCache is CachedLibraries.Cache.Monolithic)
            null
        else {
            konst filesToCache = configuration.get(KonanConfigKeys.FILES_TO_CACHE)

            konst strategy = if (filesToCache.isNullOrEmpty())
                CacheDeserializationStrategy.WholeModule
            else
                CacheDeserializationStrategy.MultipleFiles(filesToCache, libraryToAddToCache.getFileFqNames(filesToCache))
            PartialCacheInfo(libraryToAddToCache, strategy)
        }
    }

    internal konst preLinkCaches: Boolean =
            configuration.get(KonanConfigKeys.PRE_LINK_CACHES, false)

    companion object {
        fun cacheFileId(fqName: String, filePath: String) =
                "${if (fqName == "") "ROOT" else fqName}.${filePath.hashCode().toString(Character.MAX_RADIX)}"
    }

    fun checkConsistency() {
        // Ensure dependencies of every cached library are cached too:
        resolvedLibraries.getFullList { libraries ->
            libraries.map { library ->
                konst cache = cachedLibraries.getLibraryCache(library.library)
                if (cache != null || library.library == libraryToCache?.klib) {
                    library.resolvedDependencies.forEach {
                        if (!cachedLibraries.isLibraryCached(it.library) && it.library != libraryToCache?.klib) {
                            konst description = if (cache != null) {
                                "cached (in ${cache.path})"
                            } else {
                                "going to be cached"
                            }
                            configuration.reportCompilationError(
                                    "${library.library.libraryName} is $description, " +
                                            "but its dependency isn't: ${it.library.libraryName}"
                            )
                        }
                    }
                }

                library
            }
        }

        // Ensure not making cache for libraries that are already cached:
        libraryToCache?.klib?.let {
            konst cache = cachedLibraries.getLibraryCache(it)
            if (cache is CachedLibraries.Cache.Monolithic) {
                configuration.reportCompilationError("can't cache library '${it.libraryName}' " +
                        "that is already cached in '${cache.path}'")
            }
        }

        if ((libraryToCache != null || cachedLibraries.hasDynamicCaches || cachedLibraries.hasStaticCaches)
                && configuration.getBoolean(KonanConfigKeys.OPTIMIZATION)) {
            configuration.reportCompilationError("Cache cannot be used in optimized compilation")
        }
    }
}
