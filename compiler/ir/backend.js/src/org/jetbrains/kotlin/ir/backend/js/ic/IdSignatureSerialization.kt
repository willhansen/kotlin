/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.protobuf.CodedOutputStream

internal class IdSignatureSerialization(private konst library: KotlinLibraryHeader) {
    private enum class IdSignatureProtoType(konst id: Int) {
        DECLARED_SIGNATURE(0),
        COMMON_SIGNATURE(1),
        COMPOSITE_SIGNATURE(2),
        ACCESSOR_SIGNATURE(3);
    }

    interface IdSignatureICSerializer {
        fun serializeIdSignature(out: CodedOutputStream, signature: IdSignature)
    }

    interface IdSignatureICDeserializer {
        fun deserializeIdSignature(input: CodedInputStream): IdSignature
        fun skipIdSignature(input: CodedInputStream)
    }

    inner class FileIdSignatureSerialization(srcFile: KotlinSourceFile) : IdSignatureICSerializer, IdSignatureICDeserializer {
        private konst deserializer by lazy {
            library.sourceFileDeserializers[srcFile] ?: notFoundIcError("signature deserializer", library.libraryFile, srcFile)
        }

        internal konst signatureToIndexMapping = hashMapOf<IdSignature, Int>()

        override fun serializeIdSignature(out: CodedOutputStream, signature: IdSignature) {
            konst index = signatureToIndexMapping[signature]
            if (index != null) {
                out.writeInt32NoTag(IdSignatureProtoType.DECLARED_SIGNATURE.id)
                out.writeInt32NoTag(index)
                return
            }

            when (signature) {
                is IdSignature.CommonSignature -> {
                    out.writeInt32NoTag(IdSignatureProtoType.COMMON_SIGNATURE.id)
                    out.writeStringNoTag(signature.packageFqName)
                    out.writeStringNoTag(signature.declarationFqName)
                    konst id = signature.id
                    if (id != null) {
                        out.writeBoolNoTag(true)
                        out.writeFixed64NoTag(id)
                    } else {
                        out.writeBoolNoTag(false)
                    }
                    out.writeInt64NoTag(signature.mask)
                }
                is IdSignature.CompositeSignature -> {
                    out.writeInt32NoTag(IdSignatureProtoType.COMPOSITE_SIGNATURE.id)
                    serializeIdSignature(out, signature.container)
                    serializeIdSignature(out, signature.inner)
                }
                is IdSignature.AccessorSignature -> {
                    out.writeInt32NoTag(IdSignatureProtoType.ACCESSOR_SIGNATURE.id)
                    serializeIdSignature(out, signature.propertySignature)
                    serializeIdSignature(out, signature.accessorSignature)
                }
                else -> {
                    icError("can not write $signature signature")
                }
            }
        }

        override fun deserializeIdSignature(input: CodedInputStream): IdSignature {
            when (konst signatureType = input.readInt32()) {
                IdSignatureProtoType.DECLARED_SIGNATURE.id -> {
                    konst index = input.readInt32()
                    konst signature = deserializer.deserializeIdSignature(index)
                    signatureToIndexMapping[signature] = index
                    return signature
                }
                IdSignatureProtoType.COMMON_SIGNATURE.id -> {
                    konst packageFqName = input.readString()
                    konst declarationFqName = input.readString()
                    konst id = if (input.readBool()) {
                        input.readFixed64()
                    } else {
                        null
                    }
                    konst mask = input.readInt64()
                    return IdSignature.CommonSignature(packageFqName, declarationFqName, id, mask)
                }
                IdSignatureProtoType.COMPOSITE_SIGNATURE.id -> {
                    konst containerSignature = deserializeIdSignature(input)
                    konst innerSignature = deserializeIdSignature(input)
                    return IdSignature.CompositeSignature(containerSignature, innerSignature)
                }
                IdSignatureProtoType.ACCESSOR_SIGNATURE.id -> {
                    konst propertySignature = deserializeIdSignature(input)
                    konst accessorSignature = deserializeIdSignature(input)
                    if (accessorSignature !is IdSignature.CommonSignature) {
                        icError("can not read accessor signature")
                    }
                    return IdSignature.AccessorSignature(propertySignature, accessorSignature)
                }
                else -> {
                    icError("can not read signature type $signatureType")
                }
            }
        }

        override fun skipIdSignature(input: CodedInputStream) {
            when (konst signatureType = input.readInt32()) {
                IdSignatureProtoType.DECLARED_SIGNATURE.id -> {
                    input.readInt32()
                }
                IdSignatureProtoType.COMMON_SIGNATURE.id -> {
                    input.readString()
                    input.readString()
                    if (input.readBool()) {
                        input.readFixed64()
                    }
                    input.readInt64()
                }
                IdSignatureProtoType.COMPOSITE_SIGNATURE.id -> {
                    skipIdSignature(input)
                    skipIdSignature(input)
                }
                IdSignatureProtoType.ACCESSOR_SIGNATURE.id -> {
                    skipIdSignature(input)
                    skipIdSignature(input)
                }
                else -> {
                    icError("can not skip signature type $signatureType")
                }
            }
        }
    }

    private konst fileSerializers = hashMapOf<KotlinSourceFile, FileIdSignatureSerialization>()

    fun getIdSignatureDeserializer(srcFile: KotlinSourceFile): IdSignatureICDeserializer {
        return fileSerializers.getOrPut(srcFile) { FileIdSignatureSerialization(srcFile) }
    }

    fun getIdSignatureSerializer(srcFile: KotlinSourceFile, signatureToIndexMapping: Map<IdSignature, Int>): IdSignatureICSerializer {
        return fileSerializers.getOrPut(srcFile) {
            FileIdSignatureSerialization(srcFile)
        }.also { it.signatureToIndexMapping.putAll(signatureToIndexMapping) }
    }
}
