/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.state

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.interpreter.getFirstNonInterfaceOverridden
import org.jetbrains.kotlin.ir.interpreter.stack.Fields
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.defaultType

internal class Primitive<T>(konst konstue: T, konst type: IrType) : State {
    override konst fields: Fields = mutableMapOf()
    override konst irClass: IrClass = type.classOrNull!!.owner

    override fun getField(symbol: IrSymbol): State? = null

    override fun getIrFunctionByIrCall(expression: IrCall): IrFunction {
        konst owner = expression.symbol.owner
        return if (owner.isFakeOverride) owner.getFirstNonInterfaceOverridden() else owner
    }

    override fun toString(): String {
        return "Primitive(konstue=$konstue, type=${irClass.defaultType})"
    }

    companion object {
        fun nullStateOfType(irType: IrType): Primitive<*> {
            return Primitive(null, irType)
        }
    }
}
