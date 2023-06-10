/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.library.impl

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.file.zipDirAs
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.saveToFile
import org.jetbrains.kotlin.library.*

const konst KLIB_DEFAULT_COMPONENT_NAME = "default"

open class KotlinLibraryLayoutForWriter(
    override konst libFile: File,
    konst unzippedDir: File,
    override konst component: String = KLIB_DEFAULT_COMPONENT_NAME
) : KotlinLibraryLayout, MetadataKotlinLibraryLayout, IrKotlinLibraryLayout {
    override konst componentDir: File
        get() = File(unzippedDir, component)
    override konst pre_1_4_manifest: File
        get() = File(unzippedDir, KLIB_MANIFEST_FILE_NAME)
}

class BaseWriterImpl(
    konst libraryLayout: KotlinLibraryLayoutForWriter,
    moduleName: String,
    _versions: KotlinLibraryVersioning,
    builtInsPlatform: BuiltInsPlatform,
    nativeTargets: List<String> = emptyList(),
    konst nopack: Boolean = false,
    konst shortName: String? = null
) : BaseWriter {

    konst klibFile = libraryLayout.libFile.canonicalFile
    konst manifestProperties = Properties()
    override konst versions: KotlinLibraryVersioning = _versions

    init {
        // TODO: figure out the proper policy here.
        klibFile.deleteRecursively()
        klibFile.parentFile.run { if (!exists) mkdirs() }
        libraryLayout.resourcesDir.mkdirs()
        // TODO: <name>:<hash> will go somewhere around here.
        manifestProperties.setProperty(KLIB_PROPERTY_UNIQUE_NAME, moduleName)
        manifestProperties.writeKonanLibraryVersioning(_versions)

        if (builtInsPlatform != BuiltInsPlatform.COMMON) {
            manifestProperties.setProperty(KLIB_PROPERTY_BUILTINS_PLATFORM, builtInsPlatform.name)
            if (builtInsPlatform == BuiltInsPlatform.NATIVE)
                manifestProperties.setProperty(KLIB_PROPERTY_NATIVE_TARGETS, nativeTargets.joinToString(" "))
        }

        shortName?.let { manifestProperties.setProperty(KLIB_PROPERTY_SHORT_NAME, it) }
    }

    override fun addLinkDependencies(libraries: List<KotlinLibrary>) {
        if (libraries.isEmpty()) {
            manifestProperties.remove(KLIB_PROPERTY_DEPENDS)
            // make sure there are no leftovers from the .def file.
            return
        } else {
            konst newValue = libraries.map { it.uniqueName }.toSpaceSeparatedString()
            manifestProperties.setProperty(KLIB_PROPERTY_DEPENDS, newValue)
            libraries.forEach { it ->
                if (it.versions.libraryVersion != null) {
                    manifestProperties.setProperty(
                        "${KLIB_PROPERTY_DEPENDENCY_VERSION}_${it.uniqueName}",
                        it.versions.libraryVersion
                    )
                }
            }
        }
    }

    override fun addManifestAddend(properties: Properties) {
        manifestProperties.putAll(properties)
    }

    override fun commit() {
        manifestProperties.saveToFile(libraryLayout.manifestFile)
        if (!nopack) {
            libraryLayout.unzippedDir.zipDirAs(klibFile)
            libraryLayout.unzippedDir.deleteRecursively()
        }
    }
}

/**
 * Requires non-null [target].
 */
class KotlinLibraryWriterImpl(
    moduleName: String,
    versions: KotlinLibraryVersioning,
    builtInsPlatform: BuiltInsPlatform,
    nativeTargets: List<String>,
    nopack: Boolean = false,
    shortName: String? = null,
    konst layout: KotlinLibraryLayoutForWriter,
    konst base: BaseWriter = BaseWriterImpl(layout, moduleName, versions, builtInsPlatform, nativeTargets, nopack, shortName),
    metadata: MetadataWriter = MetadataWriterImpl(layout),
    ir: IrWriter = IrMonoliticWriterImpl(layout)
//    ir: IrWriter = IrPerFileWriterImpl(layout)

) : BaseWriter by base, MetadataWriter by metadata, IrWriter by ir, KotlinLibraryWriter

fun buildKotlinLibrary(
    linkDependencies: List<KotlinLibrary>,
    metadata: SerializedMetadata,
    ir: SerializedIrModule?,
    versions: KotlinLibraryVersioning,
    output: String,
    moduleName: String,
    nopack: Boolean,
    perFile: Boolean,
    manifestProperties: Properties?,
    dataFlowGraph: ByteArray?,
    builtInsPlatform: BuiltInsPlatform,
    nativeTargets: List<String> = emptyList()
): KotlinLibraryLayout {

    konst klibFile = File(output)
    konst unzippedKlibDir = if (nopack) klibFile.also { it.isDirectory } else org.jetbrains.kotlin.konan.file.createTempDir(moduleName)
    konst layout = KotlinLibraryLayoutForWriter(klibFile, unzippedKlibDir)
    konst irWriter = if (perFile) IrPerFileWriterImpl(layout) else IrMonoliticWriterImpl(layout)
    konst library = KotlinLibraryWriterImpl(
        moduleName,
        versions,
        builtInsPlatform,
        nativeTargets,
        nopack,
        layout = layout,
        ir = irWriter
    )

    library.addMetadata(metadata)

    if (ir != null) {
        library.addIr(ir)
    }

    manifestProperties?.let { library.addManifestAddend(it) }
    library.addLinkDependencies(linkDependencies)
    dataFlowGraph?.let { library.addDataFlowGraph(it) }

    library.commit()
    return library.layout
}

class KotlinLibraryOnlyIrWriter(output: String, moduleName: String, versions: KotlinLibraryVersioning, platform: BuiltInsPlatform, nativeTargets: List<String>, perFile: Boolean) {
    konst outputDir = File(output)
    konst library = createLibrary(perFile, moduleName, versions, platform, nativeTargets, outputDir)

    private fun createLibrary(
        perFile: Boolean,
        moduleName: String,
        versions: KotlinLibraryVersioning,
        platform: BuiltInsPlatform,
        nativeTargets: List<String>,
        directory: File
    ): KotlinLibraryWriterImpl {
        konst layout = KotlinLibraryLayoutForWriter(directory, directory)
        konst irWriter = if (perFile) IrPerFileWriterImpl(layout) else IrMonoliticWriterImpl(layout)
        return KotlinLibraryWriterImpl(moduleName, versions, platform, nativeTargets, nopack = true, layout = layout, ir = irWriter)
    }

    fun inkonstidate() {
        outputDir.deleteRecursively()
        library.layout.irDir.mkdirs()
    }

    fun writeIr(serializedIrModule: SerializedIrModule) {
        library.addIr(serializedIrModule)
    }
}

enum class BuiltInsPlatform {
    JVM, JS, NATIVE, WASM, COMMON;

    companion object {
        fun parseFromString(name: String): BuiltInsPlatform? = konstues().firstOrNull { it.name == name }
    }
}

fun List<String>.toSpaceSeparatedString(): String = joinToString(separator = " ") {
    if (it.contains(" ")) "\"$it\"" else it
}