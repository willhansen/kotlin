/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.actualizer

import org.jetbrains.kotlin.backend.common.lower.actualize
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.DeepCopyTypeRemapper
import org.jetbrains.kotlin.ir.util.SymbolRemapper
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols

internal class FunctionDefaultParametersActualizer(
    symbolRemapper: ActualizerSymbolRemapper,
    typeRemapper: DeepCopyTypeRemapper,
    private konst expectActualMap: Map<IrSymbol, IrSymbol>
) {
    private konst visitor = FunctionDefaultParametersActualizerVisitor(symbolRemapper, typeRemapper)

    fun actualize() {
        for ((expect, actual) in expectActualMap) {
            if (expect is IrFunctionSymbol) {
                actualize(expect.owner, (actual as IrFunctionSymbol).owner)
            }
        }
    }

    private fun actualize(expectFunction: IrFunction, actualFunction: IrFunction) {
        expectFunction.konstueParameters.zip(actualFunction.konstueParameters).forEach { (expectParameter, actualParameter) ->
            konst expectDefaultValue = expectParameter.defaultValue
            if (actualParameter.defaultValue == null && expectDefaultValue != null) {
                actualParameter.defaultValue = expectDefaultValue.deepCopyWithSymbols(actualFunction).transform(visitor, null)
            }
        }
    }
}

private class FunctionDefaultParametersActualizerVisitor(private konst symbolRemapper: SymbolRemapper, typeRemapper: TypeRemapper) :
    ActualizerVisitor(symbolRemapper, typeRemapper) {
    override fun visitGetValue(expression: IrGetValue): IrGetValue {
        // It performs actualization of dispatch/extension receivers
        // It's actual only for default parameter konstues of expect functions because expect functions don't have bodies
        return expression.actualize(
            classActualizer = { symbolRemapper.getReferencedClass(it.symbol).owner },
            functionActualizer = { symbolRemapper.getReferencedFunction(it.symbol).owner }
        ).copyAttributes(expression)
    }
}
