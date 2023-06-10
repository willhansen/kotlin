/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.ir

import org.jetbrains.kotlin.backend.konan.BinaryType
import org.jetbrains.kotlin.backend.konan.KonanBackendContext
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.computeBinaryType
import org.jetbrains.kotlin.backend.konan.descriptors.getAnnotationStringValue
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.declarations.isSingleFieldValueClass
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

// Generate additional functions for array set and get operators without bounds checking.
internal class FunctionsWithoutBoundCheckGenerator(konst context: KonanBackendContext) {
    private konst symbols = context.ir.symbols

    private fun generateFunction(baseFunction: IrSimpleFunction, delegatingToFunction: IrSimpleFunction?, functionName: Name) =
            context.irFactory.createFunction(
                    baseFunction.startOffset, baseFunction.endOffset,
                    IrDeclarationOrigin.GENERATED_SETTER_GETTER,
                    IrSimpleFunctionSymbolImpl(),
                    functionName,
                    DescriptorVisibilities.PUBLIC,
                    Modality.FINAL,
                    baseFunction.returnType,
                    isInline = false,
                    isExternal = true,
                    isTailrec = false,
                    isSuspend = false,
                    isExpect = false,
                    isFakeOverride = false,
                    isOperator = false,
                    isInfix = false
            ).also { function ->
                function.parent = baseFunction.parent
                function.createDispatchReceiverParameter()
                function.konstueParameters = baseFunction.konstueParameters.map { it.copyTo(function) }
                // Copy annotations.
                konst setWithoutBEAnnotations = (delegatingToFunction ?: baseFunction).annotations.map { annotation ->
                    annotation.deepCopyWithSymbols().also { copy ->
                        if (copy.isAnnotationWithEqualFqName(KonanFqNames.gcUnsafeCall)) {
                            konst konstue = "${annotation.getAnnotationStringValue("callee")}_without_BoundCheck"
                            copy.putValueArgument(0,
                                    IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.stringType, konstue))
                        }
                    }
                }
                function.annotations = setWithoutBEAnnotations
            }

    fun generate() {
        symbols.arrays.forEach { classSymbol ->
            konst underlyingClass = (classSymbol.defaultType.computeBinaryType() as BinaryType.Reference)
                    .types.single().takeIf { classSymbol.owner.isSingleFieldValueClass }
            konst setFunction = classSymbol.owner.functions.single { it.name == OperatorNameConventions.SET }
            konst setDelegatingToFunction = underlyingClass?.functions?.single { it.name == OperatorNameConventions.SET }
            classSymbol.owner.addMember(generateFunction(setFunction, setDelegatingToFunction, KonanNameConventions.setWithoutBoundCheck))

            konst getFunction = classSymbol.owner.functions.single { it.name == OperatorNameConventions.GET }
            konst getDelegatingToFunction = underlyingClass?.functions?.single { it.name == OperatorNameConventions.GET }
            classSymbol.owner.addMember(generateFunction(getFunction, getDelegatingToFunction, KonanNameConventions.getWithoutBoundCheck))
        }
    }
}