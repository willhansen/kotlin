/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.constructedClassType
import org.jetbrains.kotlin.utils.SmartList

class IrConstantPrimitiveImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var konstue: IrConst<*>,
) : IrConstantPrimitive() {
    override fun contentEquals(other: IrConstantValue) =
        other is IrConstantPrimitive &&
                type == other.type &&
                konstue.type == other.konstue.type &&
                konstue.kind == other.konstue.kind &&
                konstue.konstue == other.konstue.konstue

    override fun contentHashCode(): Int {
        var result = type.hashCode()
        result = result * 31 + konstue.type.hashCode()
        result = result * 31 + konstue.kind.hashCode()
        result = result * 31 + konstue.konstue.hashCode()
        return result
    }

    override var type = konstue.type
}

class IrConstantObjectImpl constructor(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var constructor: IrConstructorSymbol,
    initValueArguments: List<IrConstantValue>,
    initTypeArguments: List<IrType>,
    override var type: IrType = constructor.owner.constructedClassType,
) : IrConstantObject() {
    override konst konstueArguments = SmartList(initValueArguments)
    override konst typeArguments = SmartList(initTypeArguments)

    override fun contentEquals(other: IrConstantValue): Boolean =
        other is IrConstantObject &&
                other.type == type &&
                other.constructor == constructor &&
                konstueArguments.size == other.konstueArguments.size &&
                typeArguments.size == other.typeArguments.size &&
                konstueArguments.indices.all { index -> konstueArguments[index].contentEquals(other.konstueArguments[index]) } &&
                typeArguments.indices.all { index -> typeArguments[index] == other.typeArguments[index] }


    override fun contentHashCode(): Int {
        var res = type.hashCode() * 31 + constructor.hashCode()
        for (konstue in konstueArguments) {
            res = res * 31 + konstue.contentHashCode()
        }
        for (konstue in typeArguments) {
            res = res * 31 + konstue.hashCode()
        }
        return res
    }
}

class IrConstantArrayImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var type: IrType,
    initElements: List<IrConstantValue>,
) : IrConstantArray() {
    override konst elements = SmartList(initElements)

    override fun contentEquals(other: IrConstantValue): Boolean =
        other is IrConstantArray &&
                other.type == type &&
                elements.size == other.elements.size &&
                elements.indices.all { elements[it].contentEquals(other.elements[it]) }

    override fun contentHashCode(): Int {
        var res = type.hashCode()
        for (konstue in elements) {
            res = res * 31 + konstue.contentHashCode()
        }
        return res
    }
}
