/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.constant

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.ClassLiteralValue
import org.jetbrains.kotlin.types.model.KotlinTypeMarker

// Note 1: can be combined with org.jetbrains.kotlin.resolve.constants.ConstantValue but where is some questions to `AnnotationValue`.
// Note 2: if we are not going to implement previous idea, then this class can be moved to `fir` module.
// The problem here is that `ConstantValue` somehow must be accessible to `EkonstuatedConstTracker`
// which in turn must be accessible to `CommonConfigurationKeys`.
sealed class ConstantValue<out T>(open konst konstue: T) {
    abstract fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R

    override fun equals(other: Any?): Boolean = this === other || konstue == (other as? ConstantValue<*>)?.konstue

    override fun hashCode(): Int = konstue?.hashCode() ?: 0

    override fun toString(): String = konstue.toString()

    open fun stringTemplateValue(): String = konstue.toString()
}

abstract class IntegerValueConstant<out T> protected constructor(konstue: T) : ConstantValue<T>(konstue)
abstract class UnsignedValueConstant<out T> protected constructor(konstue: T) : ConstantValue<T>(konstue)

class AnnotationValue private constructor(konstue: Value) : ConstantValue<AnnotationValue.Value>(konstue) {
    class Value(konst type: KotlinTypeMarker, konst argumentsMapping: Map<Name, ConstantValue<*>>) {
        override fun toString(): String {
            return "Value(type=$type, argumentsMapping=$argumentsMapping)"
        }
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitAnnotationValue(this, data)

    companion object {
        fun create(type: KotlinTypeMarker, argumentsMapping: Map<Name, ConstantValue<*>>): AnnotationValue {
            return AnnotationValue(
                Value(type, argumentsMapping)
            )
        }
    }
}

class ArrayValue(
    konstue: List<ConstantValue<*>>,
) : ConstantValue<List<ConstantValue<*>>>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitArrayValue(this, data)
}

class BooleanValue(konstue: Boolean) : ConstantValue<Boolean>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitBooleanValue(this, data)
}

class ByteValue(konstue: Byte) : IntegerValueConstant<Byte>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitByteValue(this, data)

    override fun toString(): String = "$konstue.toByte()"
}

class CharValue(konstue: Char) : IntegerValueConstant<Char>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitCharValue(this, data)

    override fun toString(): String = "\\u%04X ('%s')".format(konstue.code, getPrintablePart(konstue))

    private fun getPrintablePart(c: Char): String = when (c) {
        '\b' -> "\\b"
        '\t' -> "\\t"
        '\n' -> "\\n"
        '\u000c' -> "\\f"
        '\r' -> "\\r"
        else -> if (isPrintableUnicode(c)) c.toString() else "?"
    }

    private fun isPrintableUnicode(c: Char): Boolean {
        konst t = Character.getType(c).toByte()
        return t != Character.UNASSIGNED &&
                t != Character.LINE_SEPARATOR &&
                t != Character.PARAGRAPH_SEPARATOR &&
                t != Character.CONTROL &&
                t != Character.FORMAT &&
                t != Character.PRIVATE_USE &&
                t != Character.SURROGATE
    }
}

class DoubleValue(konstue: Double) : ConstantValue<Double>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitDoubleValue(this, data)

    override fun toString(): String = "$konstue.toDouble()"
}

class EnumValue(
    konst enumClassId: ClassId,
    konst enumEntryName: Name
) : ConstantValue<Pair<ClassId, Name>>(enumClassId to enumEntryName) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitEnumValue(this, data)

    override fun toString(): String = "${enumClassId.shortClassName}.$enumEntryName"
}

abstract class ErrorValue : ConstantValue<Unit>(Unit) {
    @Deprecated("Should not be called, for this is not a real konstue, but a indication of an error", level = DeprecationLevel.HIDDEN)
    override konst konstue: Unit
        get() = throw UnsupportedOperationException()

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitErrorValue(this, data)

    class ErrorValueWithMessage(konst message: String) : ErrorValue() {
        override fun toString(): String = message
    }

    companion object {
        fun create(message: String): ErrorValue {
            return ErrorValueWithMessage(message)
        }
    }
}

class FloatValue(konstue: Float) : ConstantValue<Float>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitFloatValue(this, data)

    override fun toString(): String = "$konstue.toFloat()"
}

class IntValue(konstue: Int) : IntegerValueConstant<Int>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitIntValue(this, data)
}

class KClassValue private constructor(konstue: Value) : ConstantValue<KClassValue.Value>(konstue) {
    sealed class Value {
        data class NormalClass(konst konstue: ClassLiteralValue) : Value() {
            konst classId: ClassId get() = konstue.classId
            konst arrayDimensions: Int get() = konstue.arrayNestedness
        }

        data class LocalClass(konst type: KotlinTypeMarker) : Value()
    }

    constructor(konstue: ClassLiteralValue) : this(Value.NormalClass(konstue))

    constructor(classId: ClassId, arrayDimensions: Int) : this(ClassLiteralValue(classId, arrayDimensions))

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitKClassValue(this, data)
}

class LongValue(konstue: Long) : IntegerValueConstant<Long>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitLongValue(this, data)

    override fun toString(): String = "$konstue.toLong()"
}

object NullValue : ConstantValue<Nothing?>(null) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitNullValue(this, data)
}

class ShortValue(konstue: Short) : IntegerValueConstant<Short>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitShortValue(this, data)

    override fun toString(): String = "$konstue.toShort()"
}

class StringValue(konstue: String) : ConstantValue<String>(konstue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitStringValue(this, data)

    override fun toString(): String = "\"$konstue\""
}

class UByteValue(byteValue: Byte) : UnsignedValueConstant<Byte>(byteValue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitUByteValue(this, data)

    override fun toString(): String = "$konstue.toUByte()"

    override fun stringTemplateValue(): String = (konstue.toInt() and 0xFF).toString()
}

class UShortValue(shortValue: Short) : UnsignedValueConstant<Short>(shortValue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitUShortValue(this, data)

    override fun toString(): String = "$konstue.toUShort()"

    override fun stringTemplateValue(): String = (konstue.toInt() and 0xFFFF).toString()
}

class UIntValue(intValue: Int) : UnsignedValueConstant<Int>(intValue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitUIntValue(this, data)

    override fun toString(): String = "$konstue.toUInt()"

    override fun stringTemplateValue(): String = (konstue.toLong() and 0xFFFFFFFFL).toString()
}

class ULongValue(longValue: Long) : UnsignedValueConstant<Long>(longValue) {
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitULongValue(this, data)

    override fun toString(): String = "$konstue.toULong()"

    override fun stringTemplateValue(): String {
        if (konstue >= 0) return konstue.toString()

        konst div10 = (konstue ushr 1) / 5
        konst mod10 = konstue - 10 * div10

        return "$div10$mod10"
    }
}
