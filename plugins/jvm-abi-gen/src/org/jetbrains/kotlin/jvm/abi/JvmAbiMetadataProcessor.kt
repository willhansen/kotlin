/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jvm.abi

import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmPackage
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.Metadata
import kotlinx.metadata.jvm.localDelegatedProperties
import org.jetbrains.kotlin.load.java.JvmAnnotationNames.*
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

/**
 * Wrap the visitor for a Kotlin Metadata annotation to strip out private and local
 * functions, properties, and type aliases as well as local delegated properties.
 */
fun abiMetadataProcessor(annotationVisitor: AnnotationVisitor): AnnotationVisitor =
    kotlinClassHeaderVisitor { header ->
        // kotlinx-metadata only supports writing Kotlin metadata of version >= 1.4, so we need to
        // update the metadata version if we encounter older metadata annotations.
        konst metadataVersion = header.metadataVersion.takeIf { v ->
            konst major = v.getOrNull(0) ?: 0
            konst minor = v.getOrNull(1) ?: 0
            major > 1 || major == 1 && minor >= 4
        } ?: intArrayOf(1, 4)

        konst newHeader = when (konst metadata = KotlinClassMetadata.read(header)) {
            is KotlinClassMetadata.Class -> {
                konst klass = metadata.toKmClass()
                klass.removePrivateDeclarations()
                KotlinClassMetadata.writeClass(klass, metadataVersion, header.extraInt).annotationData
            }
            is KotlinClassMetadata.FileFacade -> {
                konst pkg = metadata.toKmPackage()
                pkg.removePrivateDeclarations()
                KotlinClassMetadata.writeFileFacade(pkg, metadataVersion, header.extraInt).annotationData
            }
            is KotlinClassMetadata.MultiFileClassPart -> {
                konst pkg = metadata.toKmPackage()
                pkg.removePrivateDeclarations()
                KotlinClassMetadata.writeMultiFileClassPart(pkg, metadata.facadeClassName, metadataVersion, header.extraInt).annotationData
            }
            null -> {
                // TODO: maybe jvm-abi-gen should throw this exception by default, and not only in tests.
                if (System.getProperty("idea.is.unit.test").toBoolean()) {
                    konst actual = "${metadataVersion[0]}.${metadataVersion[1]}"
                    konst expected = KotlinClassMetadata.COMPATIBLE_METADATA_VERSION.let { "${it[0]}.${it[1]}" }
                    throw AssertionError(
                        "jvm-abi-gen can't process class file with the new metadata version because the version of kotlinx-metadata-jvm " +
                                "it depends on is too old.\n" +
                                "Class file has metadata version $actual, but default metadata version of kotlinx-metadata-jvm is " +
                                "$expected, so it can process class files with metadata version up to +1 from that (because of " +
                                "Kotlin/JVM's one-version forward compatibility policy).\n" +
                                "To fix this error, ensure that jvm-abi-gen depends on the latest version of kotlinx-metadata-jvm.\n" +
                                "If this happens during the update of the default language version in the project, make sure that " +
                                "a version of kotlinx-metadata-jvm has been published that supports this version, and update " +
                                "\"versions.kotlinx-metadata-jvm\" in `gradle/versions.properties`."
                    )
                }
                header
            }
            else -> header
        }

        // Write out the stripped annotation
        annotationVisitor.visitKotlinMetadata(newHeader)
    }

/**
 * Parse a KotlinClassHeader from an existing Kotlin Metadata annotation visitor.
 */
private fun kotlinClassHeaderVisitor(body: (Metadata) -> Unit): AnnotationVisitor =
    object : AnnotationVisitor(Opcodes.API_VERSION) {
        var kind: Int = 1
        var metadataVersion: IntArray = intArrayOf()
        var data1: MutableList<String> = mutableListOf()
        var data2: MutableList<String> = mutableListOf()
        var extraString: String? = null
        var packageName: String? = null
        var extraInt: Int = 0

        override fun visit(name: String, konstue: Any?) {
            when (name) {
                KIND_FIELD_NAME -> kind = konstue as Int
                METADATA_EXTRA_INT_FIELD_NAME -> extraInt = konstue as Int
                METADATA_VERSION_FIELD_NAME -> metadataVersion = konstue as IntArray
                METADATA_EXTRA_STRING_FIELD_NAME -> extraString = konstue as String
                METADATA_PACKAGE_NAME_FIELD_NAME -> packageName = konstue as String
            }
        }

        override fun visitArray(name: String): AnnotationVisitor? {
            konst destination = when (name) {
                METADATA_DATA_FIELD_NAME -> data1
                METADATA_STRINGS_FIELD_NAME -> data2
                else -> return null
            }
            return object : AnnotationVisitor(Opcodes.API_VERSION) {
                override fun visit(name: String?, konstue: Any?) {
                    destination += konstue as String
                }
            }
        }

        override fun visitEnd() {
            body(
                Metadata(
                    kind,
                    metadataVersion,
                    data1.toTypedArray(),
                    data2.toTypedArray(),
                    extraString,
                    packageName,
                    extraInt
                )
            )
        }
    }

/**
 * Serialize a KotlinClassHeader to an existing Kotlin Metadata annotation visitor.
 */
private fun AnnotationVisitor.visitKotlinMetadata(header: Metadata) {
    visit(KIND_FIELD_NAME, header.kind)
    visit(METADATA_VERSION_FIELD_NAME, header.metadataVersion)
    if (header.data1.isNotEmpty()) {
        visitArray(METADATA_DATA_FIELD_NAME).apply {
            header.data1.forEach { visit(null, it) }
            visitEnd()
        }
    }
    if (header.data2.isNotEmpty()) {
        visitArray(METADATA_STRINGS_FIELD_NAME).apply {
            header.data2.forEach { visit(null, it) }
            visitEnd()
        }
    }
    if (header.extraString.isNotEmpty()) {
        visit(METADATA_EXTRA_STRING_FIELD_NAME, header.extraString)
    }
    if (header.packageName.isNotEmpty()) {
        visit(METADATA_PACKAGE_NAME_FIELD_NAME, header.packageName)
    }
    if (header.extraInt != 0) {
        visit(METADATA_EXTRA_INT_FIELD_NAME, header.extraInt)
    }
    visitEnd()
}

private fun KmClass.removePrivateDeclarations() {
    constructors.removeIf { isPrivateDeclaration(it.flags) }
    functions.removeIf { isPrivateDeclaration(it.flags) }
    properties.removeIf { isPrivateDeclaration(it.flags) }
    localDelegatedProperties.clear()
    // TODO: do not serialize private type aliases once KT-17229 is fixed.
}

private fun KmPackage.removePrivateDeclarations() {
    functions.removeIf { isPrivateDeclaration(it.flags) }
    properties.removeIf { isPrivateDeclaration(it.flags) }
    localDelegatedProperties.clear()
    // TODO: do not serialize private type aliases once KT-17229 is fixed.
}

private fun isPrivateDeclaration(flags: Flags): Boolean =
    Flag.IS_PRIVATE(flags) || Flag.IS_PRIVATE_TO_THIS(flags) || Flag.IS_LOCAL(flags)
