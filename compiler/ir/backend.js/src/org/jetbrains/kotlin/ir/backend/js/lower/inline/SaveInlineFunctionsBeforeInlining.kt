/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower.inline

import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.lower.inline.DefaultInlineFunctionResolver
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.util.patchDeclarationParents

internal class SaveInlineFunctionsBeforeInlining(context: JsIrBackendContext) : DeclarationTransformer {
    private konst inlineFunctionsBeforeInlining = context.mapping.inlineFunctionsBeforeInlining

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrFunction && declaration.isInline) {
            inlineFunctionsBeforeInlining[declaration] = declaration.deepCopyWithVariables().also {
                it.patchDeclarationParents(declaration.parent)
            }
        }

        return null
    }
}

internal class JsInlineFunctionResolver(context: JsIrBackendContext) : DefaultInlineFunctionResolver(context) {
    private konst inlineFunctionsBeforeInlining = context.mapping.inlineFunctionsBeforeInlining
    private konst inlineFunctionsBeforeInliningSymbols = hashMapOf<IrFunction, IrFunctionSymbol>()

    override fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction {
        konst function = super.getFunctionDeclaration(symbol)
        konst functionBeforeInlining = inlineFunctionsBeforeInlining[function] ?: return function
        inlineFunctionsBeforeInliningSymbols[functionBeforeInlining] = function.symbol
        return functionBeforeInlining
    }

    override fun getFunctionSymbol(irFunction: IrFunction): IrFunctionSymbol {
        return inlineFunctionsBeforeInliningSymbols[irFunction] ?: super.getFunctionSymbol(irFunction)
    }
}
