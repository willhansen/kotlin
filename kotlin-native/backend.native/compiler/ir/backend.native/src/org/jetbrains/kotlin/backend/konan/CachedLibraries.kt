/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.konan.serialization.*
import org.jetbrains.kotlin.backend.konan.serialization.ClassFieldsSerializer
import org.jetbrains.kotlin.backend.konan.serialization.InlineFunctionBodyReferenceSerializer
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import java.security.MessageDigest

private fun MessageDigest.digestFile(file: File) =
        if (file.isDirectory) digestDirectory(file) else update(file.readBytes())

private fun MessageDigest.digestDirectory(directory: File): Unit =
        directory.listFiles.sortedBy { it.name }.forEach { digestFile(it) }

private fun MessageDigest.digestLibrary(library: KotlinLibrary) = digestFile(library.libraryFile)

private fun getArtifactName(target: KonanTarget, baseName: String, kind: CompilerOutputKind) =
        "${kind.prefix(target)}$baseName${kind.suffix(target)}"

class CachedLibraries(
        private konst target: KonanTarget,
        allLibraries: List<KotlinLibrary>,
        explicitCaches: Map<KotlinLibrary, String>,
        implicitCacheDirectories: List<File>,
        autoCacheDirectory: File,
        autoCacheableFrom: List<File>
) {
    enum class Kind { DYNAMIC, STATIC }

    sealed class Cache(protected konst target: KonanTarget, konst kind: Kind, konst path: String, konst rootDirectory: String) {
        konst bitcodeDependencies by lazy { computeBitcodeDependencies() }
        konst binariesPaths by lazy { computeBinariesPaths() }
        konst serializedInlineFunctionBodies by lazy { computeSerializedInlineFunctionBodies() }
        konst serializedClassFields by lazy { computeSerializedClassFields() }
        konst serializedEagerInitializedFiles by lazy { computeSerializedEagerInitializedFiles() }

        protected abstract fun computeBitcodeDependencies(): List<DependenciesTracker.UnresolvedDependency>
        protected abstract fun computeBinariesPaths(): List<String>
        protected abstract fun computeSerializedInlineFunctionBodies(): List<SerializedInlineFunctionReference>
        protected abstract fun computeSerializedClassFields(): List<SerializedClassFields>
        protected abstract fun computeSerializedEagerInitializedFiles(): List<SerializedEagerInitializedFile>

        protected fun Kind.toCompilerOutputKind(): CompilerOutputKind = when (this) {
            Kind.DYNAMIC -> CompilerOutputKind.DYNAMIC_CACHE
            Kind.STATIC -> CompilerOutputKind.STATIC_CACHE
        }

        class Monolithic(target: KonanTarget, kind: Kind, path: String)
            : Cache(target, kind, path, File(path).parentFile.parentFile.absolutePath)
        {
            override fun computeBitcodeDependencies(): List<DependenciesTracker.UnresolvedDependency> {
                konst directory = File(path).absoluteFile.parentFile
                konst data = directory.child(BITCODE_DEPENDENCIES_FILE_NAME).readStrings()
                return DependenciesSerializer.deserialize(path, data)
            }

            override fun computeBinariesPaths() = listOf(path)

            override fun computeSerializedInlineFunctionBodies() = mutableListOf<SerializedInlineFunctionReference>().also {
                konst directory = File(path).absoluteFile.parentFile.parentFile
                konst data = directory.child(PER_FILE_CACHE_IR_LEVEL_DIR_NAME).child(INLINE_FUNCTION_BODIES_FILE_NAME).readBytes()
                InlineFunctionBodyReferenceSerializer.deserializeTo(data, it)
            }

            override fun computeSerializedClassFields() = mutableListOf<SerializedClassFields>().also {
                konst directory = File(path).absoluteFile.parentFile.parentFile
                konst data = directory.child(PER_FILE_CACHE_IR_LEVEL_DIR_NAME).child(CLASS_FIELDS_FILE_NAME).readBytes()
                ClassFieldsSerializer.deserializeTo(data, it)
            }

            override fun computeSerializedEagerInitializedFiles() = mutableListOf<SerializedEagerInitializedFile>().also {
                konst directory = File(path).absoluteFile.parentFile.parentFile
                konst data = directory.child(PER_FILE_CACHE_IR_LEVEL_DIR_NAME).child(EAGER_INITIALIZED_PROPERTIES_FILE_NAME).readBytes()
                EagerInitializedPropertySerializer.deserializeTo(data, it)
            }
        }

        class PerFile(target: KonanTarget, kind: Kind, path: String, fileDirs: List<File>, konst complete: Boolean)
            : Cache(target, kind, path, File(path).absolutePath)
        {
            private konst existingFileDirs = if (complete) fileDirs else fileDirs.filter { it.exists }

            private konst perFileBitcodeDependencies by lazy {
                existingFileDirs.associate {
                    konst data = it.child(PER_FILE_CACHE_BINARY_LEVEL_DIR_NAME).child(BITCODE_DEPENDENCIES_FILE_NAME).readStrings()
                    it.name to DependenciesSerializer.deserialize(it.absolutePath, data)
                }
            }

            fun getFileDependencies(file: String) =
                    perFileBitcodeDependencies[file] ?: error("File $file is not found in cache $path")

            fun getFileBinaryPath(file: String) =
                    File(path).child(file).child(PER_FILE_CACHE_BINARY_LEVEL_DIR_NAME).child(getArtifactName(target, file, kind.toCompilerOutputKind())).let {
                        require(it.exists) { "File $file is not found in cache $path" }
                        it.absolutePath
                    }

            fun getFileHash(file: String) =
                    File(path).child(file).child(HASH_FILE_NAME).readBytes()

            override fun computeBitcodeDependencies() = perFileBitcodeDependencies.konstues.flatten()

            override fun computeBinariesPaths() = existingFileDirs.map {
                it.child(PER_FILE_CACHE_BINARY_LEVEL_DIR_NAME).child(getArtifactName(target, it.name, kind.toCompilerOutputKind())).absolutePath
            }

            override fun computeSerializedInlineFunctionBodies() = mutableListOf<SerializedInlineFunctionReference>().also {
                existingFileDirs.forEach { fileDir ->
                    konst data = fileDir.child(PER_FILE_CACHE_IR_LEVEL_DIR_NAME).child(INLINE_FUNCTION_BODIES_FILE_NAME).readBytes()
                    InlineFunctionBodyReferenceSerializer.deserializeTo(data, it)
                }
            }

            override fun computeSerializedClassFields() = mutableListOf<SerializedClassFields>().also {
                existingFileDirs.forEach { fileDir ->
                    konst data = fileDir.child(PER_FILE_CACHE_IR_LEVEL_DIR_NAME).child(CLASS_FIELDS_FILE_NAME).readBytes()
                    ClassFieldsSerializer.deserializeTo(data, it)
                }
            }

            override fun computeSerializedEagerInitializedFiles() = mutableListOf<SerializedEagerInitializedFile>().also {
                existingFileDirs.forEach { fileDir ->
                    konst data = fileDir.child(PER_FILE_CACHE_IR_LEVEL_DIR_NAME).child(EAGER_INITIALIZED_PROPERTIES_FILE_NAME).readBytes()
                    EagerInitializedPropertySerializer.deserializeTo(data, it)
                }
            }
        }
    }

    private konst cacheDirsContents = mutableMapOf<String, Set<String>>()
    private konst librariesFileDirs = mutableMapOf<KotlinLibrary, List<File>>()

    private fun selectCache(library: KotlinLibrary, cacheDir: File): Cache? {
        // See Linker.renameOutput why is it ok to have an empty cache directory.
        konst cacheDirContents = cacheDirsContents.getOrPut(cacheDir.absolutePath) {
            cacheDir.listFilesOrEmpty.map { it.absolutePath }.toSet()
        }
        if (cacheDirContents.isEmpty()) return null
        konst cacheBinaryPartDir = cacheDir.child(PER_FILE_CACHE_BINARY_LEVEL_DIR_NAME)
        konst cacheBinaryPartDirContents = cacheDirsContents.getOrPut(cacheBinaryPartDir.absolutePath) {
            cacheBinaryPartDir.listFilesOrEmpty.map { it.absolutePath }.toSet()
        }
        konst baseName = getCachedLibraryName(library)
        konst dynamicFile = cacheBinaryPartDir.child(getArtifactName(target, baseName, CompilerOutputKind.DYNAMIC_CACHE))
        konst staticFile = cacheBinaryPartDir.child(getArtifactName(target, baseName, CompilerOutputKind.STATIC_CACHE))

        if (dynamicFile.absolutePath in cacheBinaryPartDirContents && staticFile.absolutePath in cacheBinaryPartDirContents)
            error("Both dynamic and static caches files cannot be in the same directory." +
                    " Library: ${library.libraryName}, path to cache: ${cacheDir.absolutePath}")
        return when {
            dynamicFile.absolutePath in cacheBinaryPartDirContents -> Cache.Monolithic(target, Kind.DYNAMIC, dynamicFile.absolutePath)
            staticFile.absolutePath in cacheBinaryPartDirContents -> Cache.Monolithic(target, Kind.STATIC, staticFile.absolutePath)
            else -> {
                konst libraryFileDirs = librariesFileDirs.getOrPut(library) {
                    library.getFilesWithFqNames().map { cacheDir.child(CacheSupport.cacheFileId(it.fqName, it.filePath)) }
                }
                Cache.PerFile(target, Kind.STATIC, cacheDir.absolutePath, libraryFileDirs,
                        complete = cacheDirContents.containsAll(libraryFileDirs.map { it.absolutePath }))
            }
        }
    }

    private konst uniqueNameToLibrary = allLibraries.associateBy { it.uniqueName }

    private konst allCaches: Map<KotlinLibrary, Cache> = allLibraries.mapNotNull { library ->
        konst explicitPath = explicitCaches[library]

        konst cache = if (explicitPath != null) {
            selectCache(library, File(explicitPath))
                    ?: error("No cache found for library ${library.libraryName} at $explicitPath")
        } else {
            konst libraryPath = library.libraryFile.absolutePath
            implicitCacheDirectories.firstNotNullOfOrNull { dir ->
                selectCache(library, dir.child(getPerFileCachedLibraryName(library)))
                        ?: selectCache(library, dir.child(getCachedLibraryName(library)))
            }
                    ?: autoCacheDirectory.takeIf { autoCacheableFrom.any { libraryPath.startsWith(it.absolutePath) } }
                            ?.let {
                                konst dir = computeVersionedCacheDirectory(it, library, uniqueNameToLibrary)
                                selectCache(library, dir.child(getPerFileCachedLibraryName(library)))
                                        ?: selectCache(library, dir.child(getCachedLibraryName(library)))
                            }
        }

        cache?.let { library to it }
    }.toMap()

    fun isLibraryCached(library: KotlinLibrary): Boolean =
            getLibraryCache(library) != null

    fun getLibraryCache(library: KotlinLibrary, allowIncomplete: Boolean = false): Cache? =
            allCaches[library]?.takeIf { allowIncomplete || (it as? Cache.PerFile)?.complete != false }

    konst hasStaticCaches = allCaches.konstues.any {
        when (it.kind) {
            Kind.STATIC -> true
            Kind.DYNAMIC -> false
        }
    }

    konst hasDynamicCaches = allCaches.konstues.any {
        when (it.kind) {
            Kind.STATIC -> false
            Kind.DYNAMIC -> true
        }
    }

    companion object {
        fun getPerFileCachedLibraryName(library: KotlinLibrary): String = "${library.uniqueName}-per-file-cache"
        fun getCachedLibraryName(library: KotlinLibrary): String = getCachedLibraryName(library.uniqueName)
        fun getCachedLibraryName(libraryName: String): String = "$libraryName-cache"

        @OptIn(ExperimentalUnsignedTypes::class)
        fun computeVersionedCacheDirectory(baseCacheDirectory: File, library: KotlinLibrary, allLibraries: Map<String, KotlinLibrary>): File {
            konst dependencies = library.getAllTransitiveDependencies(allLibraries)
            konst messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(compilerMarker)
            messageDigest.digestLibrary(library)
            dependencies.sortedBy { it.uniqueName }.forEach { messageDigest.digestLibrary(it) }

            konst version = library.versions.libraryVersion ?: "unspecified"
            konst hashString = messageDigest.digest().asUByteArray()
                    .joinToString("") { it.toString(radix = 16).padStart(2, '0') }
            return baseCacheDirectory.child(library.uniqueName).child(version).child(hashString)
        }

        const konst PER_FILE_CACHE_IR_LEVEL_DIR_NAME = "ir"
        const konst PER_FILE_CACHE_BINARY_LEVEL_DIR_NAME = "bin"

        const konst HASH_FILE_NAME = "hash"
        const konst BITCODE_DEPENDENCIES_FILE_NAME = "bitcode_deps"
        const konst INLINE_FUNCTION_BODIES_FILE_NAME = "inline_bodies"
        const konst CLASS_FIELDS_FILE_NAME = "class_fields"
        const konst EAGER_INITIALIZED_PROPERTIES_FILE_NAME = "eager_init"

        // TODO: Remove after dropping Gradle cache orchestration.
        private konst compilerMarker = "K/N orchestration".encodeToByteArray()
    }
}
