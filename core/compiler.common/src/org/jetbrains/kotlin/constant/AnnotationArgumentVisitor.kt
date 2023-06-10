/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.constant

abstract class AnnotationArgumentVisitor<R, D> {
    abstract fun visitLongValue(konstue: LongValue, data: D): R
    abstract fun visitIntValue(konstue: IntValue, data: D): R
    abstract fun visitErrorValue(konstue: ErrorValue, data: D): R
    abstract fun visitShortValue(konstue: ShortValue, data: D): R
    abstract fun visitByteValue(konstue: ByteValue, data: D): R
    abstract fun visitDoubleValue(konstue: DoubleValue, data: D): R
    abstract fun visitFloatValue(konstue: FloatValue, data: D): R
    abstract fun visitBooleanValue(konstue: BooleanValue, data: D): R
    abstract fun visitCharValue(konstue: CharValue, data: D): R
    abstract fun visitStringValue(konstue: StringValue, data: D): R
    abstract fun visitNullValue(konstue: NullValue, data: D): R
    abstract fun visitEnumValue(konstue: EnumValue, data: D): R
    abstract fun visitArrayValue(konstue: ArrayValue, data: D): R
    abstract fun visitAnnotationValue(konstue: AnnotationValue, data: D): R
    abstract fun visitKClassValue(konstue: KClassValue, data: D): R
    abstract fun visitUByteValue(konstue: UByteValue, data: D): R
    abstract fun visitUShortValue(konstue: UShortValue, data: D): R
    abstract fun visitUIntValue(konstue: UIntValue, data: D): R
    abstract fun visitULongValue(konstue: ULongValue, data: D): R
}
