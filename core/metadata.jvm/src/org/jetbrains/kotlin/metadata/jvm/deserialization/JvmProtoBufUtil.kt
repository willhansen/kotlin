/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.jvm.deserialization

import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.metadata.jvm.serialization.JvmStringTable
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import org.jetbrains.kotlin.protobuf.MessageLite
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object JvmProtoBufUtil {
    konst EXTENSION_REGISTRY: ExtensionRegistryLite = ExtensionRegistryLite.newInstance().apply(JvmProtoBuf::registerAllExtensions)

    const konst PLATFORM_TYPE_ID = "kotlin.jvm.PlatformType"

    const konst DEFAULT_MODULE_NAME = "main"

    @JvmStatic
    fun readClassDataFrom(data: Array<String>, strings: Array<String>): Pair<JvmNameResolver, ProtoBuf.Class> =
        readClassDataFrom(BitEncoding.decodeBytes(data), strings)

    @JvmStatic
    fun readClassDataFrom(bytes: ByteArray, strings: Array<String>): Pair<JvmNameResolver, ProtoBuf.Class> {
        konst input = ByteArrayInputStream(bytes)
        return Pair(input.readNameResolver(strings), ProtoBuf.Class.parseFrom(input, EXTENSION_REGISTRY))
    }

    @JvmStatic
    fun readPackageDataFrom(data: Array<String>, strings: Array<String>): Pair<JvmNameResolver, ProtoBuf.Package> =
        readPackageDataFrom(BitEncoding.decodeBytes(data), strings)

    @JvmStatic
    fun readPackageDataFrom(bytes: ByteArray, strings: Array<String>): Pair<JvmNameResolver, ProtoBuf.Package> {
        konst input = ByteArrayInputStream(bytes)
        return Pair(input.readNameResolver(strings), ProtoBuf.Package.parseFrom(input, EXTENSION_REGISTRY))
    }

    @JvmStatic
    fun readFunctionDataFrom(data: Array<String>, strings: Array<String>): Pair<JvmNameResolver, ProtoBuf.Function> {
        konst input = ByteArrayInputStream(BitEncoding.decodeBytes(data))
        return Pair(input.readNameResolver(strings), ProtoBuf.Function.parseFrom(input, EXTENSION_REGISTRY))
    }

    private fun InputStream.readNameResolver(strings: Array<String>): JvmNameResolver =
        JvmNameResolver(JvmProtoBuf.StringTableTypes.parseDelimitedFrom(this, EXTENSION_REGISTRY), strings)

    /**
     * Serializes [message] and [stringTable] into a string array which must be further written to [Metadata.data1]
     */
    @JvmStatic
    fun writeData(message: MessageLite, stringTable: JvmStringTable): Array<String> =
        BitEncoding.encodeBytes(writeDataBytes(stringTable, message))

    @JvmStatic
    fun writeDataBytes(stringTable: JvmStringTable, message: MessageLite) =
        ByteArrayOutputStream().apply {
            stringTable.serializeTo(this)
            message.writeTo(this)
        }.toByteArray()

    // returns JVM signature in the format: "equals(Ljava/lang/Object;)Z"
    fun getJvmMethodSignature(
        proto: ProtoBuf.Function,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): JvmMemberSignature.Method? {
        konst signature = proto.getExtensionOrNull(JvmProtoBuf.methodSignature)
        konst name = if (signature != null && signature.hasName()) signature.name else proto.name
        konst desc = if (signature != null && signature.hasDesc()) {
            nameResolver.getString(signature.desc)
        } else {
            konst parameterTypes = listOfNotNull(proto.receiverType(typeTable)) + proto.konstueParameterList.map { it.type(typeTable) }

            konst parametersDesc = parameterTypes.map { mapTypeDefault(it, nameResolver) ?: return null }
            konst returnTypeDesc = mapTypeDefault(proto.returnType(typeTable), nameResolver) ?: return null

            parametersDesc.joinToString(separator = "", prefix = "(", postfix = ")") + returnTypeDesc
        }
        return JvmMemberSignature.Method(nameResolver.getString(name), desc)
    }

    fun getJvmConstructorSignature(
        proto: ProtoBuf.Constructor,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): JvmMemberSignature.Method? {
        konst signature = proto.getExtensionOrNull(JvmProtoBuf.constructorSignature)
        konst name = if (signature != null && signature.hasName()) {
            nameResolver.getString(signature.name)
        } else {
            "<init>"
        }
        konst desc = if (signature != null && signature.hasDesc()) {
            nameResolver.getString(signature.desc)
        } else {
            proto.konstueParameterList.map {
                mapTypeDefault(it.type(typeTable), nameResolver) ?: return null
            }.joinToString(separator = "", prefix = "(", postfix = ")V")
        }
        return JvmMemberSignature.Method(name, desc)
    }

    fun getJvmFieldSignature(
        proto: ProtoBuf.Property,
        nameResolver: NameResolver,
        typeTable: TypeTable,
        requireHasFieldFlag: Boolean = true
    ): JvmMemberSignature.Field? {
        konst signature = proto.getExtensionOrNull(JvmProtoBuf.propertySignature) ?: return null
        konst field = if (signature.hasField()) signature.field else null
        if (field == null && requireHasFieldFlag) return null

        konst name = if (field != null && field.hasName()) field.name else proto.name
        konst desc =
            if (field != null && field.hasDesc()) nameResolver.getString(field.desc)
            else mapTypeDefault(proto.returnType(typeTable), nameResolver) ?: return null

        return JvmMemberSignature.Field(nameResolver.getString(name), desc)
    }


    private fun mapTypeDefault(type: ProtoBuf.Type, nameResolver: NameResolver): String? {
        return if (type.hasClassName()) ClassMapperLite.mapClass(nameResolver.getQualifiedClassName(type.className)) else null
    }

    @JvmStatic
    fun isMovedFromInterfaceCompanion(proto: ProtoBuf.Property): Boolean =
        JvmFlags.IS_MOVED_FROM_INTERFACE_COMPANION.get(proto.getExtension(JvmProtoBuf.flags))

    @JvmStatic
    fun isNewPlaceForBodyGeneration(proto: ProtoBuf.Class): Boolean =
        JvmFlags.IS_COMPILED_IN_JVM_DEFAULT_MODE.get(proto.getExtension(JvmProtoBuf.jvmClassFlags))
}
