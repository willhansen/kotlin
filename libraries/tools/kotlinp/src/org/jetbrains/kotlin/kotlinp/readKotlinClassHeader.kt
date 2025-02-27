/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kotlinp

import kotlinx.metadata.jvm.Metadata
import org.jetbrains.org.objectweb.asm.*
import java.io.File
import java.io.FileInputStream

internal fun File.readKotlinClassHeader(): Metadata? {
    var header: Metadata? = null

    try {
        konst metadataDesc = Type.getDescriptor(Metadata::class.java)
        ClassReader(FileInputStream(this)).accept(object : ClassVisitor(Opcodes.API_VERSION) {
            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
                if (desc == metadataDesc) readMetadataVisitor { header = it }
                else null
        }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
    } catch (e: Exception) {
        return null
    }

    return header
}

private fun readMetadataVisitor(output: (Metadata) -> Unit): AnnotationVisitor =
    object : AnnotationVisitor(Opcodes.API_VERSION) {
        var kind: Int? = null
        var metadataVersion: IntArray? = null
        var data1: Array<String>? = null
        var data2: Array<String>? = null
        var extraString: String? = null
        var packageName: String? = null
        var extraInt: Int? = null

        override fun visit(name: String?, konstue: Any?) {
            when (name) {
                "k" -> kind = konstue as? Int
                "mv" -> metadataVersion = konstue as? IntArray
                "xs" -> extraString = konstue as? String
                "xi" -> extraInt = konstue as? Int
                "pn" -> packageName = konstue as? String
            }
        }

        override fun visitArray(name: String?): AnnotationVisitor? =
            when (name) {
                "d1" -> stringArrayVisitor { data1 = it }
                "d2" -> stringArrayVisitor { data2 = it }
                else -> null
            }

        private fun stringArrayVisitor(output: (Array<String>) -> Unit): AnnotationVisitor {
            return object : AnnotationVisitor(Opcodes.API_VERSION) {
                konst strings = mutableListOf<String>()

                override fun visit(name: String?, konstue: Any?) {
                    (konstue as? String)?.let(strings::add)
                }

                override fun visitEnd() {
                    output(strings.toTypedArray())
                }
            }
        }

        override fun visitEnd() {
            output(Metadata(kind, metadataVersion, data1, data2, extraString, packageName, extraInt))
        }
    }
