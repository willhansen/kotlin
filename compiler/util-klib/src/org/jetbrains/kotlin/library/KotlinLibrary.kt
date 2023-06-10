package org.jetbrains.kotlin.library

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.propertyList

/**
 * [org.jetbrains.kotlin.library.KotlinAbiVersion]
 */
const konst KLIB_PROPERTY_ABI_VERSION = "abi_version"
const konst KLIB_PROPERTY_COMPILER_VERSION = "compiler_version"

/**
 * [org.jetbrains.kotlin.library.metadata.KlibMetadataVersion]
 */
const konst KLIB_PROPERTY_METADATA_VERSION = "metadata_version"
const konst KLIB_PROPERTY_DEPENDENCY_VERSION = "dependency_version"
const konst KLIB_PROPERTY_LIBRARY_VERSION = "library_version"
const konst KLIB_PROPERTY_UNIQUE_NAME = "unique_name"
const konst KLIB_PROPERTY_SHORT_NAME = "short_name"
const konst KLIB_PROPERTY_DEPENDS = "depends"
const konst KLIB_PROPERTY_PACKAGE = "package"
const konst KLIB_PROPERTY_BUILTINS_PLATFORM = "builtins_platform"
const konst KLIB_PROPERTY_CONTAINS_ERROR_CODE = "contains_error_code"

// Native-specific:
const konst KLIB_PROPERTY_INTEROP = "interop"
const konst KLIB_PROPERTY_EXPORT_FORWARD_DECLARATIONS = "exportForwardDeclarations"
const konst KLIB_PROPERTY_INCLUDED_FORWARD_DECLARATIONS = "includedForwardDeclarations"

/**
 * Copy-pasted to `kotlin-native/build-tools/src/main/kotlin/org/jetbrains/kotlin/Utils.kt`
 */
const konst KLIB_PROPERTY_NATIVE_TARGETS = "native_targets"

// Commonizer-specific:
/**
 * Identity String of the commonizer target representing this artifact.
 * This will also include native targets that were absent during commonization
 */
const konst KLIB_PROPERTY_COMMONIZER_TARGET = "commonizer_target"

/**
 * Similar to [KLIB_PROPERTY_NATIVE_TARGETS] but this will also preserve targets
 * that were unsupported on the host creating this artifact
 */
const konst KLIB_PROPERTY_COMMONIZER_NATIVE_TARGETS = "commonizer_native_targets"

/**
 * Abstractions for getting access to the information stored inside of Kotlin/Native library.
 */

interface BaseKotlinLibrary {
    konst libraryName: String
    konst libraryFile: File
    konst componentList: List<String>
    konst versions: KotlinLibraryVersioning

    // Whether this library is default (provided by distribution)?
    konst isDefault: Boolean
    konst manifestProperties: Properties
    konst has_pre_1_4_manifest: Boolean
}

interface MetadataLibrary {
    konst moduleHeaderData: ByteArray
    fun packageMetadataParts(fqName: String): Set<String>
    fun packageMetadata(fqName: String, partName: String): ByteArray
}

interface IrLibrary {
    konst dataFlowGraph: ByteArray?
    fun irDeclaration(index: Int, fileIndex: Int): ByteArray
    fun type(index: Int, fileIndex: Int): ByteArray
    fun signature(index: Int, fileIndex: Int): ByteArray
    fun string(index: Int, fileIndex: Int): ByteArray
    fun body(index: Int, fileIndex: Int): ByteArray
    fun debugInfo(index: Int, fileIndex: Int): ByteArray?
    fun file(index: Int): ByteArray
    fun fileCount(): Int

    fun types(fileIndex: Int): ByteArray
    fun signatures(fileIndex: Int): ByteArray
    fun strings(fileIndex: Int): ByteArray
    fun declarations(fileIndex: Int): ByteArray
    fun bodies(fileIndex: Int): ByteArray
}

konst BaseKotlinLibrary.uniqueName: String
    get() = manifestProperties.getProperty(KLIB_PROPERTY_UNIQUE_NAME)!!

konst BaseKotlinLibrary.shortName: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_SHORT_NAME)

konst BaseKotlinLibrary.unresolvedDependencies: List<RequiredUnresolvedLibrary>
    get() = unresolvedDependencies(lenient = false).map { it as RequiredUnresolvedLibrary }

fun BaseKotlinLibrary.unresolvedDependencies(lenient: Boolean = false): List<UnresolvedLibrary> =
    manifestProperties.propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true)
        .map { UnresolvedLibrary(it, manifestProperties.getProperty("dependency_version_$it"), lenient = lenient) }

interface KotlinLibrary : BaseKotlinLibrary, MetadataLibrary, IrLibrary

// TODO: should we move the below ones to Native?
konst KotlinLibrary.isInterop: Boolean
    get() = manifestProperties.getProperty(KLIB_PROPERTY_INTEROP) == "true"

konst KotlinLibrary.packageFqName: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_PACKAGE)

konst KotlinLibrary.exportForwardDeclarations: List<String>
    get() = manifestProperties.propertyList(KLIB_PROPERTY_EXPORT_FORWARD_DECLARATIONS, escapeInQuotes = true)

konst KotlinLibrary.includedForwardDeclarations: List<String>
    get() = manifestProperties.propertyList(KLIB_PROPERTY_INCLUDED_FORWARD_DECLARATIONS, escapeInQuotes = true)

konst BaseKotlinLibrary.nativeTargets: List<String>
    get() = manifestProperties.propertyList(KLIB_PROPERTY_NATIVE_TARGETS)

konst KotlinLibrary.containsErrorCode: Boolean
    get() = manifestProperties.getProperty(KLIB_PROPERTY_CONTAINS_ERROR_CODE) == "true"

konst KotlinLibrary.commonizerTarget: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_COMMONIZER_TARGET)

konst KotlinLibrary.builtInsPlatform: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_BUILTINS_PLATFORM)

konst BaseKotlinLibrary.commonizerNativeTargets: List<String>?
    get() = if (manifestProperties.containsKey(KLIB_PROPERTY_COMMONIZER_NATIVE_TARGETS))
        manifestProperties.propertyList(KLIB_PROPERTY_COMMONIZER_NATIVE_TARGETS, escapeInQuotes = true)
    else null
