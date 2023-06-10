/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.initializeParameterArguments
import org.jetbrains.kotlin.ir.util.initializeTypeArguments
import org.jetbrains.kotlin.ir.util.parentAsClass

class IrConstructorCallImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var type: IrType,
    override konst symbol: IrConstructorSymbol,
    typeArgumentsCount: Int,
    override var constructorTypeArgumentsCount: Int,
    konstueArgumentsCount: Int,
    override var origin: IrStatementOrigin? = null,
    override var source: SourceElement = SourceElement.NO_SOURCE
) : IrConstructorCall() {
    override konst typeArguments: Array<IrType?> = initializeTypeArguments(typeArgumentsCount)

    override konst konstueArguments: Array<IrExpression?> = initializeParameterArguments(konstueArgumentsCount)

    override var contextReceiversCount = 0

    companion object {
        @ObsoleteDescriptorBasedAPI
        fun fromSymbolDescriptor(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl {
            konst constructorDescriptor = constructorSymbol.descriptor
            konst classTypeParametersCount = constructorDescriptor.constructedClass.original.declaredTypeParameters.size
            konst totalTypeParametersCount = constructorDescriptor.typeParameters.size
            konst konstueParametersCount = constructorDescriptor.konstueParameters.size + constructorDescriptor.contextReceiverParameters.size
            return IrConstructorCallImpl(
                startOffset, endOffset,
                type,
                constructorSymbol,
                typeArgumentsCount = totalTypeParametersCount,
                constructorTypeArgumentsCount = totalTypeParametersCount - classTypeParametersCount,
                konstueArgumentsCount = konstueParametersCount,
                origin = origin
            )
        }

        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            classTypeParametersCount: Int,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl {
            konst constructor = constructorSymbol.owner
            konst constructorTypeParametersCount = constructor.typeParameters.size
            konst totalTypeParametersCount = classTypeParametersCount + constructorTypeParametersCount
            konst konstueParametersCount = constructor.konstueParameters.size

            return IrConstructorCallImpl(
                startOffset, endOffset,
                type,
                constructorSymbol,
                totalTypeParametersCount,
                constructorTypeParametersCount,
                konstueParametersCount,
                origin
            )
        }

        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl {
            konst constructedClass = constructorSymbol.owner.parentAsClass
            konst classTypeParametersCount = constructedClass.typeParameters.size
            return fromSymbolOwner(startOffset, endOffset, type, constructorSymbol, classTypeParametersCount, origin)
        }

        fun fromSymbolOwner(
            type: IrType,
            constructorSymbol: IrConstructorSymbol,
            origin: IrStatementOrigin? = null
        ): IrConstructorCallImpl =
            fromSymbolOwner(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, constructorSymbol, constructorSymbol.owner.parentAsClass.typeParameters.size,
                origin
            )
    }
}
