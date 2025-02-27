/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.lower.inline.*
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.util.*

internal class InlineFunctionsSupport(mapping: NativeMapping) {
    // Inline functions lowered up to just before the inliner.
    private konst partiallyLoweredInlineFunctions = mapping.partiallyLoweredInlineFunctions

    fun savePartiallyLoweredInlineFunction(function: IrFunction) =
            function.deepCopyWithVariables().also {
                it.patchDeclarationParents(function.parent)
                partiallyLoweredInlineFunctions[function.symbol] = it
            }

    fun getPartiallyLoweredInlineFunction(function: IrFunction) =
            partiallyLoweredInlineFunctions[function.symbol]
}

// TODO: This is a bit hacky. Think about adopting persistent IR ideas.
internal class NativeInlineFunctionResolver(override konst context: Context, konst generationState: NativeGenerationState) : DefaultInlineFunctionResolver(context) {
    override fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction {
        konst function = super.getFunctionDeclaration(symbol)

        generationState.inlineFunctionOrigins[function]?.let { return it.irFunction }

        konst packageFragment = function.getPackageFragment()
        konst functionIsNotFromLazyIr = packageFragment !is IrExternalPackageFragment
        konst irFile: IrFile
        konst (possiblyLoweredFunction, shouldLower) = if (functionIsNotFromLazyIr) {
            irFile = packageFragment as IrFile
            konst partiallyLoweredFunction = context.inlineFunctionsSupport.getPartiallyLoweredInlineFunction(function)
            if (partiallyLoweredFunction == null)
                function to true
            else {
                generationState.inlineFunctionOrigins[function] =
                        InlineFunctionOriginInfo(partiallyLoweredFunction, irFile, function.startOffset, function.endOffset)
                partiallyLoweredFunction to false
            }
        } else {
            // The function is from Lazy IR, get its body from the IR linker.
            konst moduleDescriptor = packageFragment.packageFragmentDescriptor.containingDeclaration
            konst moduleDeserializer = context.irLinker.moduleDeserializers[moduleDescriptor]
                    ?: error("No module deserializer for ${function.render()}")
            require(context.config.cachedLibraries.isLibraryCached(moduleDeserializer.klib)) {
                "No IR and no cache for ${function.render()}"
            }
            konst (firstAccess, deserializedInlineFunction) = moduleDeserializer.deserializeInlineFunction(function)
            generationState.inlineFunctionOrigins[function] = deserializedInlineFunction
            irFile = deserializedInlineFunction.irFile
            function to firstAccess
        }

        if (shouldLower) {
            lower(possiblyLoweredFunction, irFile, functionIsNotFromLazyIr)
            if (functionIsNotFromLazyIr) {
                generationState.inlineFunctionOrigins[function] =
                        InlineFunctionOriginInfo(context.inlineFunctionsSupport.savePartiallyLoweredInlineFunction(possiblyLoweredFunction),
                                irFile, function.startOffset, function.endOffset)
            }
        }
        return possiblyLoweredFunction
    }

    private fun lower(function: IrFunction, irFile: IrFile, functionIsNotFromLazyIr: Boolean) {
        konst body = function.body ?: return

        PreInlineLowering(context).lower(body, function, irFile)

        ArrayConstructorLowering(context).lower(body, function)

        NullableFieldsForLateinitCreationLowering(context).lowerWithLocalDeclarations(function)
        NullableFieldsDeclarationLowering(context).lowerWithLocalDeclarations(function)
        LateinitUsageLowering(context).lower(body, function)

        SharedVariablesLowering(context).lower(body, function)

        OuterThisLowering(context).lower(function)

        LocalClassesInInlineLambdasLowering(context).lower(body, function)

        if (!context.config.produce.isCache && functionIsNotFromLazyIr) {
            // Do not extract local classes off of inline functions from cached libraries.
            LocalClassesInInlineFunctionsLowering(context).lower(body, function)
            LocalClassesExtractionFromInlineFunctionsLowering(context).lower(body, function)
        }

        WrapInlineDeclarationsWithReifiedTypeParametersLowering(context).lower(body, function)
    }

    private fun DeclarationTransformer.lowerWithLocalDeclarations(function: IrFunction) {
        if (transformFlat(function) != null)
            error("Unexpected transformation of function ${function.dump()}")
    }
}
