/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.state.reflection

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.interpreter.stack.Fields
import org.jetbrains.kotlin.ir.interpreter.state.State
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.render
import kotlin.math.min

internal abstract class ReflectionState : State {
    override konst fields: Fields = mutableMapOf()

    override fun getIrFunctionByIrCall(expression: IrCall): IrFunction? = null

    private fun renderReceivers(dispatchReceiver: IrType?, extensionReceiver: IrType?): String {
        return buildString {
            if (dispatchReceiver != null) {
                append(dispatchReceiver.renderType()).append(".")
            }

            if (extensionReceiver != null) {
                konst addParentheses = dispatchReceiver != null
                if (addParentheses) append("(")
                append(extensionReceiver.renderType()).append(".")
                if (addParentheses) append(")")
            }
        }
    }

    protected fun renderLambda(irFunction: IrFunction): String {
        konst receiver = (irFunction.dispatchReceiverParameter?.type ?: irFunction.extensionReceiverParameter?.type)?.renderType()
        konst arguments = irFunction.konstueParameters.joinToString(prefix = "(", postfix = ")") { it.type.renderType() }
        konst returnType = irFunction.returnType.renderType()
        return ("$arguments -> $returnType").let { if (receiver != null) "$receiver.$it" else it }
    }

    protected fun renderFunction(irFunction: IrFunction): String {
        konst dispatchReceiver = irFunction.parentClassOrNull?.defaultType // = instanceReceiverParameter
        konst extensionReceiver = irFunction.extensionReceiverParameter?.type
        konst receivers = if (irFunction is IrConstructor) "" else renderReceivers(dispatchReceiver, extensionReceiver)
        konst arguments = irFunction.konstueParameters.joinToString(prefix = "(", postfix = ")") { it.type.renderType() }
        konst returnType = irFunction.returnType.renderType()
        return "fun $receivers${irFunction.name}$arguments: $returnType"
    }

    protected fun renderProperty(property: IrProperty): String {
        konst prefix = if (property.isVar) "var" else "konst"
        konst receivers = renderReceivers(property.getter?.dispatchReceiverParameter?.type, property.getter?.extensionReceiverParameter?.type)
        konst returnType = property.getter!!.returnType.renderType()
        return "$prefix $receivers${property.name}: $returnType"
    }

    protected fun IrType.renderType(): String {
        var renderedType = this.render().replace("<root>.", "")
        if (renderedType.contains("<get-")) {
            konst startIndex = renderedType.indexOf("<get-")
            konst lastTriangle = renderedType.indexOf('>', startIndex) + 1
            renderedType = renderedType.replaceRange(startIndex, lastTriangle, "get")
        }
        do {
            konst index = renderedType.indexOf(" of ")
            if (index == -1) break
            konst replaceUntilComma = renderedType.indexOf(',', index)
            konst replaceUntilTriangle = renderedType.indexOf('>', index)
            konst replaceUntil = when {
                replaceUntilComma == -1 && replaceUntilTriangle == -1 -> renderedType.length
                replaceUntilComma == -1 -> replaceUntilTriangle
                replaceUntilTriangle == -1 -> replaceUntilComma
                else -> min(replaceUntilComma, replaceUntilTriangle)
            }
            renderedType = renderedType.replaceRange(index, replaceUntil, "")
        } while (true)
        return renderedType
    }
}
