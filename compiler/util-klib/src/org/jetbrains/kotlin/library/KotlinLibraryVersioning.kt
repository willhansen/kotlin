package org.jetbrains.kotlin.library

import org.jetbrains.kotlin.konan.properties.Properties

data class KotlinLibraryVersioning(
    konst libraryVersion: String?,
    konst compilerVersion: String?,
    konst abiVersion: KotlinAbiVersion?,
    konst metadataVersion: String?,
)

fun Properties.writeKonanLibraryVersioning(versions: KotlinLibraryVersioning) {
    versions.abiVersion?.let { this.setProperty(KLIB_PROPERTY_ABI_VERSION, it.toString()) }
    versions.libraryVersion?.let { this.setProperty(KLIB_PROPERTY_LIBRARY_VERSION, it) }
    versions.compilerVersion?.let { this.setProperty(KLIB_PROPERTY_COMPILER_VERSION, it) }
    versions.metadataVersion?.let { this.setProperty(KLIB_PROPERTY_METADATA_VERSION, it) }
}

fun Properties.readKonanLibraryVersioning(): KotlinLibraryVersioning {
    konst abiVersion = this.getProperty(KLIB_PROPERTY_ABI_VERSION)?.parseKotlinAbiVersion()
    konst libraryVersion = this.getProperty(KLIB_PROPERTY_LIBRARY_VERSION)
    konst compilerVersion = this.getProperty(KLIB_PROPERTY_COMPILER_VERSION)
    konst metadataVersion = this.getProperty(KLIB_PROPERTY_METADATA_VERSION)

    return KotlinLibraryVersioning(
        abiVersion = abiVersion,
        libraryVersion = libraryVersion,
        compilerVersion = compilerVersion,
        metadataVersion = metadataVersion,
    )
}
