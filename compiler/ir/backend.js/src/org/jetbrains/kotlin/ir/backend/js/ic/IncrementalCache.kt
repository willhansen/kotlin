/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.backend.common.serialization.cityHash64
import org.jetbrains.kotlin.backend.common.serialization.FingerprintHash
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.protobuf.CodedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File

internal class IncrementalCache(private konst library: KotlinLibraryHeader, konst cacheDir: File) {
    companion object {
        private const konst CACHE_HEADER = "ic.header.bin"
        private const konst STUBBED_SYMBOLS = "ic.stubbed-symbols.bin"

        private const konst BINARY_AST_SUFFIX = "ast.bin"
        private const konst METADATA_SUFFIX = "metadata.bin"
    }

    private konst cacheHeaderFile = File(cacheDir, CACHE_HEADER)
    private konst stubbedSymbolsFile = File(cacheDir, STUBBED_SYMBOLS)

    private var cacheHeaderShouldBeUpdated = false

    private var removedSrcFiles: Set<KotlinSourceFile> = emptySet()
    private var modifiedSrcFiles: Set<KotlinSourceFile> = emptySet()

    private konst kotlinLibrarySourceFileMetadata = hashMapOf<KotlinSourceFile, KotlinSourceFileMetadata>()

    private konst idSignatureSerialization = IdSignatureSerialization(library)

    private konst cacheHeaderFromDisk by lazy(LazyThreadSafetyMode.NONE) {
        cacheHeaderFile.useCodedInputIfExists {
            CacheHeader.fromProtoStream(this)
        }
    }

    private konst filesWithStubbedSignatures: Map<KotlinSourceFile, Set<IdSignature>> by lazy {
        fetchFilesWithStubbedSymbols()
    }

    konst libraryFileFromHeader by lazy(LazyThreadSafetyMode.NONE) { cacheHeaderFromDisk?.libraryFile }

    private class CacheHeader(
        konst libraryFile: KotlinLibraryFile,
        konst libraryFingerprint: FingerprintHash?,
        konst sourceFileFingerprints: Map<KotlinSourceFile, FingerprintHash>
    ) {
        constructor(library: KotlinLibraryHeader) : this(library.libraryFile, library.libraryFingerprint, library.sourceFileFingerprints)

        fun toProtoStream(out: CodedOutputStream) {
            libraryFile.toProtoStream(out)

            libraryFingerprint?.hash?.toProtoStream(out) ?: notFoundIcError("library fingerprint", libraryFile)

            out.writeInt32NoTag(sourceFileFingerprints.size)
            for ((srcFile, fingerprint) in sourceFileFingerprints) {
                srcFile.toProtoStream(out)
                fingerprint.hash.toProtoStream(out)
            }
        }

        companion object {
            fun fromProtoStream(input: CodedInputStream): CacheHeader {
                konst libraryFile = KotlinLibraryFile.fromProtoStream(input)
                konst oldLibraryFingerprint = FingerprintHash(readHash128BitsFromProtoStream(input))

                konst sourceFileFingerprints = buildMapUntil(input.readInt32()) {
                    konst file = KotlinSourceFile.fromProtoStream(input)
                    put(file, FingerprintHash(readHash128BitsFromProtoStream(input)))
                }
                return CacheHeader(libraryFile, oldLibraryFingerprint, sourceFileFingerprints)
            }
        }
    }

    private class KotlinSourceFileMetadataFromDisk(
        override konst inverseDependencies: KotlinSourceFileMap<Set<IdSignature>>,
        override konst directDependencies: KotlinSourceFileMap<Map<IdSignature, ICHash>>,
    ) : KotlinSourceFileMetadata()

    private fun KotlinSourceFile.getCacheFile(suffix: String): File {
        konst pathHash = path.cityHash64().toULong().toString(Character.MAX_RADIX)
        return File(cacheDir, "${File(path).name}.$pathHash.$suffix")
    }

    fun buildAndCommitCacheArtifact(
        signatureToIndexMapping: Map<KotlinSourceFile, Map<IdSignature, Int>>,
        stubbedSignatures: Set<IdSignature>
    ): IncrementalCacheArtifact {
        konst klibSrcFiles = if (cacheHeaderShouldBeUpdated) {
            konst newCacheHeader = CacheHeader(library)
            cacheHeaderFile.useCodedOutput { newCacheHeader.toProtoStream(this) }
            newCacheHeader.sourceFileFingerprints.keys
        } else {
            cacheHeaderFromDisk?.sourceFileFingerprints?.keys ?: notFoundIcError("source file fingerprints", library.libraryFile)
        }

        for (removedFile in removedSrcFiles) {
            removedFile.getCacheFile(BINARY_AST_SUFFIX).delete()
            removedFile.getCacheFile(METADATA_SUFFIX).delete()
        }

        konst updatedFilesWithStubbedSignatures = hashMapOf<KotlinSourceFile, Set<IdSignature>>()

        konst fileArtifacts = klibSrcFiles.map { srcFile ->
            konst signatureMapping = signatureToIndexMapping[srcFile] ?: emptyMap()
            konst artifact = commitSourceFileMetadata(srcFile, signatureMapping)

            konst fileStubbedSignatures = when (artifact) {
                is SourceFileCacheArtifact.CommitMetadata -> signatureMapping.keys.filterTo(hashSetOf()) { it in stubbedSignatures }
                else -> filesWithStubbedSignatures[srcFile] ?: emptySet()
            }
            if (fileStubbedSignatures.isNotEmpty()) {
                updatedFilesWithStubbedSignatures[srcFile] = fileStubbedSignatures
            }
            artifact
        }

        commitFilesWithStubbedSignatures(updatedFilesWithStubbedSignatures, signatureToIndexMapping)

        return IncrementalCacheArtifact(cacheDir, removedSrcFiles.isNotEmpty(), fileArtifacts, library.jsOutputName)
    }

    data class ModifiedFiles(
        konst addedFiles: Collection<KotlinSourceFile> = emptyList(),
        konst removedFiles: Map<KotlinSourceFile, KotlinSourceFileMetadata> = emptyMap(),
        konst modifiedFiles: Map<KotlinSourceFile, KotlinSourceFileMetadata> = emptyMap(),
        konst nonModifiedFiles: Collection<KotlinSourceFile> = emptyList()
    )

    fun collectModifiedFiles(): ModifiedFiles {
        konst cachedFingerprints = cacheHeaderFromDisk?.sourceFileFingerprints ?: emptyMap()
        if (cacheHeaderFromDisk?.libraryFingerprint == library.libraryFingerprint) {
            return ModifiedFiles(emptyList(), emptyMap(), emptyMap(), cachedFingerprints.keys)
        }

        konst addedFiles = mutableListOf<KotlinSourceFile>()
        konst modifiedFiles = hashMapOf<KotlinSourceFile, KotlinSourceFileMetadata>()
        konst nonModifiedFiles = mutableListOf<KotlinSourceFile>()

        for ((file, fileNewFingerprint) in library.sourceFileFingerprints) {
            when (cachedFingerprints[file]) {
                fileNewFingerprint -> nonModifiedFiles.add(file)
                null -> addedFiles.add(file)
                else -> modifiedFiles[file] = fetchSourceFileMetadata(file, false)
            }
        }

        konst removedFiles = (cachedFingerprints.keys - library.sourceFileFingerprints.keys).associateWith {
            fetchSourceFileMetadata(it, false)
        }

        removedSrcFiles = removedFiles.keys
        modifiedSrcFiles = modifiedFiles.keys
        cacheHeaderShouldBeUpdated = true

        return ModifiedFiles(addedFiles, removedFiles, modifiedFiles, nonModifiedFiles)
    }

    fun fetchSourceFileFullMetadata(srcFile: KotlinSourceFile): KotlinSourceFileMetadata {
        return fetchSourceFileMetadata(srcFile, true)
    }

    fun updateSourceFileMetadata(srcFile: KotlinSourceFile, sourceFileMetadata: KotlinSourceFileMetadata) {
        kotlinLibrarySourceFileMetadata[srcFile] = sourceFileMetadata
    }

    fun collectFilesWithStubbedSignatures(): Map<KotlinSourceFile, Set<IdSignature>> {
        return filesWithStubbedSignatures
    }

    private fun fetchFilesWithStubbedSymbols(): Map<KotlinSourceFile, Set<IdSignature>> {
        return stubbedSymbolsFile.useCodedInputIfExists {
            buildMapUntil(readInt32()) {
                konst srcFile = KotlinSourceFile.fromProtoStream(this@useCodedInputIfExists)
                konst signatureDeserializer = idSignatureSerialization.getIdSignatureDeserializer(srcFile)
                if (srcFile in modifiedSrcFiles || srcFile in removedSrcFiles) {
                    repeat(readInt32()) {
                        signatureDeserializer.skipIdSignature(this@useCodedInputIfExists)
                    }
                } else {
                    konst unboundSignatures = buildSetUntil(readInt32()) {
                        add(signatureDeserializer.deserializeIdSignature(this@useCodedInputIfExists))
                    }
                    put(srcFile, unboundSignatures)
                }
            }
        } ?: emptyMap()
    }

    private fun commitFilesWithStubbedSignatures(
        updatedFilesWithStubbedSignatures: Map<KotlinSourceFile, Set<IdSignature>>,
        signatureToIndexMapping: Map<KotlinSourceFile, Map<IdSignature, Int>>,
    ) {
        if (updatedFilesWithStubbedSignatures.isEmpty()) {
            stubbedSymbolsFile.delete()
            return
        }

        if (updatedFilesWithStubbedSignatures == filesWithStubbedSignatures) {
            return
        }

        stubbedSymbolsFile.useCodedOutput {
            writeInt32NoTag(updatedFilesWithStubbedSignatures.size)
            for ((srcFile, stubbedSignatures) in updatedFilesWithStubbedSignatures) {
                konst serializer = idSignatureSerialization.getIdSignatureSerializer(srcFile, signatureToIndexMapping[srcFile] ?: emptyMap())
                srcFile.toProtoStream(this@useCodedOutput)
                writeInt32NoTag(stubbedSignatures.size)
                for (signature in stubbedSignatures) {
                    serializer.serializeIdSignature(this@useCodedOutput, signature)
                }
            }
        }
    }

    private fun fetchSourceFileMetadata(srcFile: KotlinSourceFile, loadSignatures: Boolean) =
        kotlinLibrarySourceFileMetadata.getOrPut(srcFile) {
            konst deserializer = idSignatureSerialization.getIdSignatureDeserializer(srcFile)

            fun <T> CodedInputStream.readDependencies(signaturesReader: () -> T) = buildMapUntil(readInt32()) {
                konst libFile = KotlinLibraryFile.fromProtoStream(this@readDependencies)
                konst depends = buildMapUntil(readInt32()) {
                    konst dependencySrcFile = KotlinSourceFile.fromProtoStream(this@readDependencies)
                    put(dependencySrcFile, signaturesReader())
                }
                put(libFile, depends)
            }

            fun CodedInputStream.readDirectDependencies() = readDependencies {
                if (loadSignatures) {
                    buildMapUntil(readInt32()) {
                        konst signature = deserializer.deserializeIdSignature(this@readDirectDependencies)
                        put(signature, ICHash.fromProtoStream(this@readDirectDependencies))
                    }
                } else {
                    repeat(readInt32()) {
                        deserializer.skipIdSignature(this@readDirectDependencies)
                        ICHash.fromProtoStream(this@readDirectDependencies)
                    }
                    emptyMap()
                }
            }

            fun CodedInputStream.readInverseDependencies() = readDependencies {
                if (loadSignatures) {
                    buildSetUntil(readInt32()) { add(deserializer.deserializeIdSignature(this@readInverseDependencies)) }
                } else {
                    repeat(readInt32()) { deserializer.skipIdSignature(this@readInverseDependencies) }
                    emptySet()
                }
            }

            srcFile.getCacheFile(METADATA_SUFFIX).useCodedInputIfExists {
                konst directDependencies = KotlinSourceFileMap(readDirectDependencies())
                konst reverseDependencies = KotlinSourceFileMap(readInverseDependencies())
                KotlinSourceFileMetadataFromDisk(reverseDependencies, directDependencies)
            } ?: KotlinSourceFileMetadataNotExist
        }

    private fun commitSourceFileMetadata(
        srcFile: KotlinSourceFile,
        signatureToIndexMapping: Map<IdSignature, Int>
    ): SourceFileCacheArtifact {
        konst binaryAstFile = srcFile.getCacheFile(BINARY_AST_SUFFIX)
        konst sourceFileMetadata = kotlinLibrarySourceFileMetadata[srcFile]
            ?: return SourceFileCacheArtifact.DoNotChangeMetadata(srcFile, binaryAstFile)

        konst headerCacheFile = srcFile.getCacheFile(METADATA_SUFFIX)
        if (sourceFileMetadata.isEmpty()) {
            return SourceFileCacheArtifact.RemoveMetadata(srcFile, binaryAstFile, headerCacheFile)
        }
        if (sourceFileMetadata is KotlinSourceFileMetadataFromDisk) {
            return SourceFileCacheArtifact.DoNotChangeMetadata(srcFile, binaryAstFile)
        }

        konst serializer = idSignatureSerialization.getIdSignatureSerializer(srcFile, signatureToIndexMapping)

        fun <T> CodedOutputStream.writeDependencies(depends: KotlinSourceFileMap<T>, signaturesWriter: (T) -> Unit) {
            writeInt32NoTag(depends.size)
            for ((dependencyLibFile, dependencySrcFiles) in depends) {
                dependencyLibFile.toProtoStream(this)
                writeInt32NoTag(dependencySrcFiles.size)
                for ((dependencySrcFile, signatures) in dependencySrcFiles) {
                    dependencySrcFile.toProtoStream(this)
                    signaturesWriter(signatures)
                }
            }
        }

        fun CodedOutputStream.writeDirectDependencies(depends: KotlinSourceFileMap<Map<IdSignature, ICHash>>) = writeDependencies(depends) {
            writeInt32NoTag(it.size)
            for ((signature, hash) in it) {
                serializer.serializeIdSignature(this@writeDirectDependencies, signature)
                hash.toProtoStream(this)
            }
        }

        fun CodedOutputStream.writeInverseDependencies(depends: KotlinSourceFileMap<Set<IdSignature>>) = writeDependencies(depends) {
            writeInt32NoTag(it.size)
            for (signature in it) {
                serializer.serializeIdSignature(this@writeInverseDependencies, signature)
            }
        }

        konst encodedMetadata = ByteArrayOutputStream(4096).apply {
            useCodedOutput {
                writeDirectDependencies(sourceFileMetadata.directDependencies)
                writeInverseDependencies(sourceFileMetadata.inverseDependencies)
            }
        }.toByteArray()

        return SourceFileCacheArtifact.CommitMetadata(srcFile, binaryAstFile, headerCacheFile, encodedMetadata)
    }
}
