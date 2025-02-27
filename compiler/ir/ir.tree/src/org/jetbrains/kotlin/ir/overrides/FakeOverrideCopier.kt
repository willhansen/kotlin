/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.overrides

import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.linkage.partial.IrUnimplementedOverridesStrategy
import org.jetbrains.kotlin.ir.util.*

class FakeOverrideCopier(
    private konst symbolRemapper: SymbolRemapper,
    private konst typeRemapper: TypeRemapper,
    private konst symbolRenamer: SymbolRenamer,
    private konst makeExternal: Boolean,
    private konst parent: IrClass,
    private konst unimplementedOverridesStrategy: IrUnimplementedOverridesStrategy
) : DeepCopyIrTreeWithSymbols(symbolRemapper, typeRemapper, symbolRenamer) {

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrSimpleFunction {
        konst customization = unimplementedOverridesStrategy.computeCustomization(declaration, parent)

        return declaration.factory.createFunctionWithLateBinding(
            declaration.startOffset, declaration.endOffset,
            customization.origin ?: IrDeclarationOrigin.FAKE_OVERRIDE,
            symbolRenamer.getFunctionName(declaration.symbol),
            declaration.visibility,
            customization.modality ?: declaration.modality,
            declaration.returnType,
            isInline = declaration.isInline,
            isExternal = makeExternal,
            isTailrec = declaration.isTailrec,
            isSuspend = declaration.isSuspend,
            isExpect = declaration.isExpect,
            isOperator = declaration.isOperator,
            isInfix = declaration.isInfix
        ).apply {
            transformAnnotations(declaration)
            copyTypeParametersFrom(declaration)
            typeRemapper.withinScope(this) {
                // This is the more correct way to produce dispatch receiver for a fake override,
                // but some lowerings still expect the below behavior as produced by the current psi2ir.
                /*
                    konst superDispatchReceiver = declaration.dispatchReceiverParameter!!
                    konst dispatchReceiverSymbol = IrValueParameterSymbolImpl(WrappedReceiverParameterDescriptor())
                    konst dispatchReceiverType = destinationClass.defaultType
                    dispatchReceiverParameter = IrValueParameterImpl(
                        superDispatchReceiver.startOffset,
                        superDispatchReceiver.endOffset,
                        superDispatchReceiver.origin,
                        dispatchReceiverSymbol,
                        superDispatchReceiver.name,
                        superDispatchReceiver.index,
                        dispatchReceiverType,
                        null,
                        superDispatchReceiver.isCrossinline,
                        superDispatchReceiver.isNoinline
                    )
                    */
                // Should fake override's receiver be the current class is an open question.
                dispatchReceiverParameter = declaration.dispatchReceiverParameter?.transform()
                extensionReceiverParameter = declaration.extensionReceiverParameter?.transform()
                returnType = typeRemapper.remapType(declaration.returnType)
                konstueParameters = declaration.konstueParameters.transform()
            }
        }
    }

    override fun visitProperty(declaration: IrProperty): IrProperty {
        konst customization = unimplementedOverridesStrategy.computeCustomization(declaration, parent)

        return declaration.factory.createPropertyWithLateBinding(
            declaration.startOffset, declaration.endOffset,
            customization.origin ?: IrDeclarationOrigin.FAKE_OVERRIDE,
            declaration.name,
            declaration.visibility,
            customization.modality ?: declaration.modality,
            isVar = declaration.isVar,
            isConst = declaration.isConst,
            isLateinit = declaration.isLateinit,
            isDelegated = declaration.isDelegated,
            isExpect = declaration.isExpect,
            isExternal = makeExternal
        ).apply {
            transformAnnotations(declaration)
            this.getter = declaration.getter?.transform()
            this.setter = declaration.setter?.transform()
        }
    }

    override fun visitValueParameter(declaration: IrValueParameter): IrValueParameter =
        declaration.factory.createValueParameter(
            declaration.startOffset, declaration.endOffset,
            mapDeclarationOrigin(declaration.origin),
            symbolRemapper.getDeclaredValueParameter(declaration.symbol),
            symbolRenamer.getValueParameterName(declaration.symbol),
            declaration.index,
            declaration.type.remapType(),
            declaration.varargElementType?.remapType(),
            declaration.isCrossinline,
            declaration.isNoinline,
            declaration.isHidden,
            declaration.isAssignable
        ).apply {
            transformAnnotations(declaration)
            // Don't set the default konstue for fake overrides.
        }
}
