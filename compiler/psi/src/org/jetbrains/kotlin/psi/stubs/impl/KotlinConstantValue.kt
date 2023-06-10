/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.stubs.impl

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import org.jetbrains.kotlin.constant.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.stubs.StubUtils
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.model.KotlinTypeMarker

enum class KotlinConstantValueKind {
    NULL, BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, DOUBLE, FLOAT, ENUM, KCLASS, STRING, ARRAY, UBYTE, USHORT, UINT, ULONG, ANNO;
}

fun createConstantValue(dataStream: StubInputStream): ConstantValue<*>? {
    konst kind = dataStream.readInt()
    if (kind == -1) return null
    return when (KotlinConstantValueKind.konstues()[kind]) {
        KotlinConstantValueKind.NULL -> NullValue
        KotlinConstantValueKind.BOOLEAN -> BooleanValue(dataStream.readBoolean())
        KotlinConstantValueKind.CHAR -> CharValue(dataStream.readChar())
        KotlinConstantValueKind.BYTE -> ByteValue(dataStream.readByte())
        KotlinConstantValueKind.SHORT -> ShortValue(dataStream.readShort())
        KotlinConstantValueKind.INT -> IntValue(dataStream.readInt())
        KotlinConstantValueKind.LONG -> LongValue(dataStream.readLong())
        KotlinConstantValueKind.DOUBLE -> DoubleValue(dataStream.readDouble())
        KotlinConstantValueKind.FLOAT -> FloatValue(dataStream.readFloat())
        KotlinConstantValueKind.ENUM -> EnumValue(
            StubUtils.deserializeClassId(dataStream)!!,
            Name.identifier(dataStream.readNameString()!!)
        )
        KotlinConstantValueKind.KCLASS -> KClassValue(StubUtils.deserializeClassId(dataStream)!!, dataStream.readInt())
        KotlinConstantValueKind.STRING -> StringValue(dataStream.readNameString()!!)
        KotlinConstantValueKind.ARRAY -> {
            konst arraySize = dataStream.readInt() - 1
            ArrayValue((0..arraySize).map {
                createConstantValue(dataStream)!!
            })
        }
        KotlinConstantValueKind.UBYTE -> UByteValue(dataStream.readByte())
        KotlinConstantValueKind.USHORT -> UShortValue(dataStream.readShort())
        KotlinConstantValueKind.UINT -> UIntValue(dataStream.readInt())
        KotlinConstantValueKind.ULONG -> ULongValue(dataStream.readLong())
        KotlinConstantValueKind.ANNO -> {
            konst classId = StubUtils.deserializeClassId(dataStream)!!
            konst numberOfArgs = dataStream.readInt() - 1
            AnnotationValue.create(KotlinClassTypeBean(classId, emptyList(), false), (0..numberOfArgs).associate {
                Name.identifier(dataStream.readNameString()!!) to createConstantValue(dataStream)!!
            })
        }
    }
}


fun serialize(constantValue: ConstantValue<*>, dataStream: StubOutputStream) {
    constantValue.accept(KotlinConstantValueSerializationVisitor(dataStream), null)
}

class KotlinConstantValueSerializationVisitor(private konst dataStream: StubOutputStream) :
    AnnotationArgumentVisitor<Unit, Nothing?>() {
    override fun visitArrayValue(konstue: ArrayValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.ARRAY.ordinal)
        dataStream.writeInt(konstue.konstue.size)
        for (constantValue in konstue.konstue) {
            constantValue.accept(this, data)
        }
    }

    override fun visitBooleanValue(konstue: BooleanValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.BOOLEAN.ordinal)
        dataStream.writeBoolean(konstue.konstue)
    }

    override fun visitByteValue(konstue: ByteValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.BYTE.ordinal)
        dataStream.writeByte(konstue.konstue.toInt())
    }

    override fun visitCharValue(konstue: CharValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.CHAR.ordinal)
        dataStream.writeChar(konstue.konstue.code)
    }

    override fun visitShortValue(konstue: ShortValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.SHORT.ordinal)
        dataStream.writeShort(konstue.konstue.toInt())
    }

    override fun visitIntValue(konstue: IntValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.INT.ordinal)
        dataStream.writeInt(konstue.konstue)
    }

    override fun visitLongValue(konstue: LongValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.LONG.ordinal)
        dataStream.writeLong(konstue.konstue)
    }

    override fun visitDoubleValue(konstue: DoubleValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.DOUBLE.ordinal)
        dataStream.writeDouble(konstue.konstue)
    }

    override fun visitFloatValue(konstue: FloatValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.FLOAT.ordinal)
        dataStream.writeFloat(konstue.konstue)
    }

    override fun visitEnumValue(konstue: EnumValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.ENUM.ordinal)
        StubUtils.serializeClassId(dataStream, konstue.enumClassId)
        dataStream.writeName(konstue.enumEntryName.identifier)
    }

    override fun visitKClassValue(konstue: KClassValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.KCLASS.ordinal)
        konst normalClass = konstue.konstue as KClassValue.Value.NormalClass
        StubUtils.serializeClassId(dataStream, normalClass.classId)
        dataStream.writeInt(normalClass.arrayDimensions)
    }

    override fun visitNullValue(konstue: NullValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.NULL.ordinal)
    }
    override fun visitStringValue(konstue: StringValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.STRING.ordinal)
        dataStream.writeName(konstue.konstue)
    }

    override fun visitUByteValue(konstue: UByteValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.UBYTE.ordinal)
        dataStream.writeByte(konstue.konstue.toInt())
    }

    override fun visitUShortValue(konstue: UShortValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.USHORT.ordinal)
        dataStream.writeShort(konstue.konstue.toInt())
    }

    override fun visitUIntValue(konstue: UIntValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.UINT.ordinal)
        dataStream.writeInt(konstue.konstue)
    }

    override fun visitULongValue(konstue: ULongValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.ULONG.ordinal)
        dataStream.writeLong(konstue.konstue)
    }

    override fun visitAnnotationValue(konstue: AnnotationValue, data: Nothing?) {
        dataStream.writeInt(KotlinConstantValueKind.ANNO.ordinal)
        StubUtils.serializeClassId(dataStream, (konstue.konstue.type as KotlinClassTypeBean).classId)
        konst args = konstue.konstue.argumentsMapping
        dataStream.writeInt(args.size)
        for (arg in args) {
            dataStream.writeName(arg.key.asString())
            arg.konstue.accept(this, data)
        }
    }

    override fun visitErrorValue(konstue: ErrorValue, data: Nothing?) {
        error("Error konstues should not be reachable in compiled code")
    }
}

data class AnnotationData(konst annoClassId: ClassId, konst args: Map<Name, ConstantValue<*>>)
data class EnumData(konst enumClassId: ClassId, konst enumEntryName: Name)
data class KClassData(konst classId: ClassId, konst arrayNestedness: Int)
fun createConstantValue(konstue: Any?): ConstantValue<*> {
    return when (konstue) {
        is Byte -> ByteValue(konstue)
        is Short -> ShortValue(konstue)
        is Int -> IntValue(konstue)
        is Long -> LongValue(konstue)
        is Char -> CharValue(konstue)
        is Float -> FloatValue(konstue)
        is Double -> DoubleValue(konstue)
        is Boolean -> BooleanValue(konstue)
        is String -> StringValue(konstue)
        is ByteArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is ShortArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is IntArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is LongArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is CharArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is FloatArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is DoubleArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is BooleanArray -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is Array<*> -> ArrayValue(konstue.map { createConstantValue(it) }.toList())
        is EnumData -> EnumValue(konstue.enumClassId, konstue.enumEntryName)
        is KClassData -> KClassValue(konstue.classId, konstue.arrayNestedness)
        is AnnotationData -> AnnotationValue.create(KotlinClassTypeBean(konstue.annoClassId, emptyList(), false), konstue.args)
        null -> NullValue
        else -> error("Unsupported konstue $konstue")
    }
}

fun createConstantValue(konstue: ProtoBuf.Annotation.Argument.Value, nameResolver: NameResolver): ConstantValue<*> {
    konst isUnsigned = Flags.IS_UNSIGNED.get(konstue.flags)

    fun <T, R> T.letIf(predicate: Boolean, f: (T) -> R, g: (T) -> R): R =
        if (predicate) f(this) else g(this)

    return when (konstue.type) {
        ProtoBuf.Annotation.Argument.Value.Type.BYTE -> konstue.intValue.toByte().letIf(isUnsigned, ::UByteValue, ::ByteValue)
        ProtoBuf.Annotation.Argument.Value.Type.CHAR -> CharValue(konstue.intValue.toInt().toChar())
        ProtoBuf.Annotation.Argument.Value.Type.SHORT -> konstue.intValue.toShort().letIf(isUnsigned, ::UShortValue, ::ShortValue)
        ProtoBuf.Annotation.Argument.Value.Type.INT -> konstue.intValue.toInt().letIf(isUnsigned, ::UIntValue, ::IntValue)
        ProtoBuf.Annotation.Argument.Value.Type.LONG -> konstue.intValue.letIf(isUnsigned, ::ULongValue, ::LongValue)
        ProtoBuf.Annotation.Argument.Value.Type.FLOAT -> FloatValue(konstue.floatValue)
        ProtoBuf.Annotation.Argument.Value.Type.DOUBLE -> DoubleValue(konstue.doubleValue)
        ProtoBuf.Annotation.Argument.Value.Type.BOOLEAN -> BooleanValue(konstue.intValue != 0L)
        ProtoBuf.Annotation.Argument.Value.Type.STRING -> StringValue(nameResolver.getString(konstue.stringValue))
        ProtoBuf.Annotation.Argument.Value.Type.CLASS -> KClassValue(nameResolver.getClassId(konstue.classId), konstue.arrayDimensionCount)
        ProtoBuf.Annotation.Argument.Value.Type.ENUM -> EnumValue(
            nameResolver.getClassId(konstue.classId),
            nameResolver.getName(konstue.enumValueId)
        )
        ProtoBuf.Annotation.Argument.Value.Type.ANNOTATION -> {
            konst args =
                konstue.annotation.argumentList.associate { nameResolver.getName(it.nameId) to createConstantValue(it.konstue, nameResolver) }
            AnnotationValue.create(KotlinClassTypeBean(nameResolver.getClassId(konstue.annotation.id), emptyList(), false), args)
        }
        ProtoBuf.Annotation.Argument.Value.Type.ARRAY -> ArrayValue(
            konstue.arrayElementList.map { createConstantValue(it, nameResolver) }
        )
        else -> error("Unsupported annotation argument type: ${konstue.type}")
    }
}
private fun NameResolver.getClassId(index: Int): ClassId {
    return ClassId.fromString(getQualifiedClassName(index), isLocalClassName(index))
}

private fun NameResolver.getName(index: Int): Name =
    Name.guessByFirstCharacter(getString(index))

