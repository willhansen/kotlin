/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

sealed class CirConstantValue {
    sealed class LiteralValue<out T> : CirConstantValue() {
        abstract konst konstue: T
    }

    data class StringValue(override konst konstue: String) : LiteralValue<String>()
    data class CharValue(override konst konstue: Char) : LiteralValue<Char>()

    data class ByteValue(override konst konstue: Byte) : LiteralValue<Byte>()
    data class ShortValue(override konst konstue: Short) : LiteralValue<Short>()
    data class IntValue(override konst konstue: Int) : LiteralValue<Int>()
    data class LongValue(override konst konstue: Long) : LiteralValue<Long>()

    // TODO: remove @ExperimentalUnsignedTypes once bootstrap stdlib has stable unsigned types.
    @ExperimentalUnsignedTypes
    data class UByteValue(override konst konstue: UByte) : LiteralValue<UByte>()

    @ExperimentalUnsignedTypes
    data class UShortValue(override konst konstue: UShort) : LiteralValue<UShort>()

    @ExperimentalUnsignedTypes
    data class UIntValue(override konst konstue: UInt) : LiteralValue<UInt>()

    @ExperimentalUnsignedTypes
    data class ULongValue(override konst konstue: ULong) : LiteralValue<ULong>()

    data class FloatValue(override konst konstue: Float) : LiteralValue<Float>()
    data class DoubleValue(override konst konstue: Double) : LiteralValue<Double>()
    data class BooleanValue(override konst konstue: Boolean) : LiteralValue<Boolean>()

    data class EnumValue(konst enumClassId: CirEntityId, konst enumEntryName: CirName) : CirConstantValue()

    data class ArrayValue(konst elements: List<CirConstantValue>) : CirConstantValue()

    object NullValue : CirConstantValue() {
        override fun toString() = "NullValue(konstue=null)"
    }
}
