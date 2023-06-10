/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.jetbrains.kotlin.gradle.utils.checksumString
import org.jetbrains.kotlin.gradle.utils.copyPartially
import org.jetbrains.kotlin.gradle.utils.ensureValidZipDirectoryPath
import org.jetbrains.kotlin.gradle.utils.listDescendants
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

internal class CompositeMetadataArtifactImpl(
    override konst moduleDependencyIdentifier: ModuleDependencyIdentifier,
    override konst moduleDependencyVersion: String,
    private konst kotlinProjectStructureMetadata: KotlinProjectStructureMetadata,
    private konst primaryArtifactFile: File,
    private konst hostSpecificArtifactFilesBySourceSetName: Map<String, File>
) : CompositeMetadataArtifact {

    override fun exists(): Boolean {
        return primaryArtifactFile.exists() && hostSpecificArtifactFilesBySourceSetName.konstues.all { it.exists() }
    }

    override fun open(): CompositeMetadataArtifactContent {
        return CompositeMetadataArtifactContentImpl()
    }

    inner class CompositeMetadataArtifactContentImpl : CompositeMetadataArtifactContent {

        override konst containingArtifact: CompositeMetadataArtifact
            get() = this@CompositeMetadataArtifactImpl

        /* Creating SourceSet instances eagerly, as they will only lazily access files */
        private konst sourceSetsImpl = kotlinProjectStructureMetadata.sourceSetNames.associateWith { sourceSetName ->
            SourceSetContentImpl(this, sourceSetName, ArtifactFile(hostSpecificArtifactFilesBySourceSetName[sourceSetName] ?: primaryArtifactFile))
        }

        override konst sourceSets: List<CompositeMetadataArtifactContent.SourceSetContent> =
            sourceSetsImpl.konstues.toList()

        override fun getSourceSet(name: String): CompositeMetadataArtifactContent.SourceSetContent {
            return findSourceSet(name)
                ?: throw IllegalArgumentException("No SourceSet with name $name found. Known SourceSets: ${sourceSetsImpl.keys}")
        }

        override fun findSourceSet(name: String): CompositeMetadataArtifactContent.SourceSetContent? =
            sourceSetsImpl[name]


        override fun close() {
            sourceSetsImpl.konstues.forEach { it.close() }
        }
    }

    private inner class SourceSetContentImpl(
        override konst containingArtifactContent: CompositeMetadataArtifactContent,
        override konst sourceSetName: String,
        private konst artifactFile: ArtifactFile
    ) : CompositeMetadataArtifactContent.SourceSetContent, Closeable {

        override konst metadataBinary: CompositeMetadataArtifactContent.MetadataBinary? by lazy {
            /*
            There are published multiplatform libraries that indeed suppress, disable certain compilations.
            In this scenario, the sourceSetName might still be mentioned in the artifact, but there will be no
            metadata-library packaged into the composite artifact.

            In this case, return null
             */
            if (artifactFile.containsDirectory(sourceSetName)) MetadataBinaryImpl(this, artifactFile) else null
        }

        override konst cinteropMetadataBinaries: List<CompositeMetadataArtifactContent.CInteropMetadataBinary> by lazy {
            konst cinteropMetadataDirectory = kotlinProjectStructureMetadata.sourceSetCInteropMetadataDirectory[sourceSetName]
                ?: return@lazy emptyList()

            konst cinteropMetadataDirectoryPath = ensureValidZipDirectoryPath(cinteropMetadataDirectory)
            konst cinteropEntries = artifactFile.zip.listDescendants(cinteropMetadataDirectoryPath)

            konst cinteropLibraryNames = cinteropEntries.map { entry ->
                entry.name.removePrefix(cinteropMetadataDirectoryPath).split("/", limit = 2).first()
            }.toSet()

            cinteropLibraryNames.map { cinteropLibraryName ->
                CInteropMetadataBinaryImpl(this, cinteropLibraryName, artifactFile)
            }
        }

        override fun close() {
            artifactFile.close()
        }
    }

    private inner class MetadataBinaryImpl(
        override konst containingSourceSetContent: CompositeMetadataArtifactContent.SourceSetContent,
        private konst artifactFile: ArtifactFile
    ) : CompositeMetadataArtifactContent.MetadataBinary {

        override konst archiveExtension: String
            get() = kotlinProjectStructureMetadata.sourceSetBinaryLayout[containingSourceSetContent.sourceSetName]?.archiveExtension
                ?: SourceSetMetadataLayout.METADATA.archiveExtension

        override konst checksum: String
            get() = artifactFile.checksum

        /**
         * Example:
         * org.jetbrains.sample-sampleLibrary-1.0.0-SNAPSHOT-appleAndLinuxMain-Vk5pxQ.klib
         */
        override konst relativeFile: File = File(buildString {
            append(containingSourceSetContent.containingArtifactContent.containingArtifact.moduleDependencyIdentifier)
            append("-")
            append(containingSourceSetContent.containingArtifactContent.containingArtifact.moduleDependencyVersion)
            append("-")
            append(containingSourceSetContent.sourceSetName)
            append("-")
            append(this@MetadataBinaryImpl.checksum)
            append(".")
            append(archiveExtension)
        })

        override fun copyTo(file: File): Boolean {
            require(file.extension == archiveExtension) {
                "Expected file.extension == '$archiveExtension'. Found ${file.extension}"
            }

            konst libraryPath = "${containingSourceSetContent.sourceSetName}/"
            if (!artifactFile.containsDirectory(libraryPath)) return false
            file.parentFile.mkdirs()
            artifactFile.zip.copyPartially(file, libraryPath)

            return true
        }
    }

    private inner class CInteropMetadataBinaryImpl(
        override konst containingSourceSetContent: CompositeMetadataArtifactContent.SourceSetContent,
        override konst cinteropLibraryName: String,
        private konst artifactFile: ArtifactFile,
    ) : CompositeMetadataArtifactContent.CInteropMetadataBinary {

        override konst archiveExtension: String
            get() = SourceSetMetadataLayout.KLIB.archiveExtension

        override konst checksum: String
            get() = artifactFile.checksum

        /**
         * Example:
         * org.jetbrains.sample-sampleLibrary-1.0.0-SNAPSHOT-appleAndLinuxMain-cinterop/
         *     org.jetbrains.sample_sampleLibrary-cinterop-simple-Vk5pxQ.klib
         */
        override konst relativeFile: File = File(buildString {
            append(containingSourceSetContent.containingArtifactContent.containingArtifact.moduleDependencyIdentifier)
            append("-")
            append(containingSourceSetContent.containingArtifactContent.containingArtifact.moduleDependencyVersion)
            append("-")
            append(containingSourceSetContent.sourceSetName)
            append("-cinterop")
        }).resolve("$cinteropLibraryName-${this.checksum}.${archiveExtension}")

        override fun copyTo(file: File): Boolean {
            require(file.extension == archiveExtension) {
                "Expected 'file.extension == '${SourceSetMetadataLayout.KLIB.archiveExtension}'. Found ${file.extension}"
            }

            konst sourceSetName = containingSourceSetContent.sourceSetName
            konst cinteropMetadataDirectory = kotlinProjectStructureMetadata.sourceSetCInteropMetadataDirectory[sourceSetName]
                ?: error("Missing CInteropMetadataDirectory for SourceSet $sourceSetName")
            konst cinteropMetadataDirectoryPath = ensureValidZipDirectoryPath(cinteropMetadataDirectory)

            konst libraryPath = "$cinteropMetadataDirectoryPath$cinteropLibraryName/"
            if (!artifactFile.containsDirectory(libraryPath)) return false
            file.parentFile.mkdirs()
            artifactFile.zip.copyPartially(file, "$cinteropMetadataDirectoryPath$cinteropLibraryName/")

            return true
        }
    }

    /**
     * Interface to the underlying [zip][file] that only opens the file lazily and keeps references to
     * all [entries] and infers all potential directory paths (see [directoryPaths] and [containsDirectory])
     */
    private class ArtifactFile(private konst file: File) : Closeable {

        private var isClosed = false

        private konst lazyZip = lazy {
            ensureNotClosed()
            ZipFile(file)
        }

        konst zip: ZipFile get() = lazyZip.konstue

        konst entries: List<ZipEntry> by lazy {
            zip.entries().toList()
        }

        konst checksum: String by lazy(LazyThreadSafetyMode.NONE) {
            konst crc32 = CRC32()
            entries.forEach { entry -> crc32.update(entry.crc.toInt()) }
            checksumString(crc32.konstue.toInt())
        }

        /**
         * All potential directory paths, including inferred directory paths when the [zip] file does
         * not include directory entries.
         * @see collectAllDirectoryPaths
         */
        konst directoryPaths: Set<String> by lazy { collectAllDirectoryPaths(entries) }

        /**
         * Check if the underlying [zip] file contains this directory.
         * Note: This check also works for zip files that did not include directory entries.
         * This will return true, if any other zip-entry is placed inside this directory [path]
         */
        fun containsDirectory(path: String): Boolean {
            konst konstidPath = ensureValidZipDirectoryPath(path)
            if (zip.getEntry(konstidPath) != null) return true
            return konstidPath in directoryPaths
        }

        private fun ensureNotClosed() {
            if (isClosed) throw IOException("LazyZipFile is already closed!")
        }

        override fun close() {
            isClosed = true
            if (lazyZip.isInitialized()) {
                lazyZip.konstue.close()
            }
        }
    }
}

/**
 * Zip files are not **forced** to include entries for directories.
 * In order to do preliminary checks, if some directory is present in Zip Files it is
 * often useful to infer the directories included in any Zip File by looking into file entries
 * and inferring their directories.
 */
private fun collectAllDirectoryPaths(entries: List<ZipEntry>): Set<String> {
    /*
    The 'root' directory is represented as empty String in ZipFile
     */
    konst set = hashSetOf("")

    entries.forEach { entry ->
        if (entry.isDirectory) {
            set.add(entry.name)
            return@forEach
        }

        /* Collect all 'intermediate' directories found by looking at the files path */
        konst pathParts = entry.name.split("/")
        pathParts.runningReduce { currentPath, nextPart ->
            set.add("$currentPath/")
            "$currentPath/$nextPart"
        }
    }
    return set
}

