/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.typeParametersCount
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.initializeParameterArguments
import org.jetbrains.kotlin.ir.util.initializeTypeArguments
import org.jetbrains.kotlin.ir.util.render

class IrCallImpl(
    override konst startOffset: Int,
    override konst endOffset: Int,
    override var type: IrType,
    override konst symbol: IrSimpleFunctionSymbol,
    typeArgumentsCount: Int,
    konstueArgumentsCount: Int,
    override var origin: IrStatementOrigin? = null,
    override var superQualifierSymbol: IrClassSymbol? = null
) : IrCall() {

    override konst typeArguments: Array<IrType?> = initializeTypeArguments(typeArgumentsCount)

    override konst konstueArguments: Array<IrExpression?> = initializeParameterArguments(konstueArgumentsCount)

    override var contextReceiversCount = 0

    init {
        if (symbol is IrConstructorSymbol) {
            throw AssertionError("Should be IrConstructorCall: ${this.render()}")
        }
    }

    companion object {
        @ObsoleteDescriptorBasedAPI
        fun fromSymbolDescriptor(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            symbol: IrSimpleFunctionSymbol,
            typeArgumentsCount: Int = symbol.descriptor.typeParametersCount,
            konstueArgumentsCount: Int = symbol.descriptor.konstueParameters.size + symbol.descriptor.contextReceiverParameters.size,
            origin: IrStatementOrigin? = null,
            superQualifierSymbol: IrClassSymbol? = null,
        ) =
            IrCallImpl(startOffset, endOffset, type, symbol, typeArgumentsCount, konstueArgumentsCount, origin, superQualifierSymbol)

        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            symbol: IrSimpleFunctionSymbol,
            typeArgumentsCount: Int = symbol.owner.typeParameters.size,
            konstueArgumentsCount: Int = symbol.owner.konstueParameters.size,
            origin: IrStatementOrigin? = null,
            superQualifierSymbol: IrClassSymbol? = null,
        ) =
            IrCallImpl(startOffset, endOffset, type, symbol, typeArgumentsCount, konstueArgumentsCount, origin, superQualifierSymbol)

        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            symbol: IrSimpleFunctionSymbol
        ) =
            IrCallImpl(
                startOffset,
                endOffset,
                symbol.owner.returnType,
                symbol,
                typeArgumentsCount = symbol.owner.typeParameters.size,
                konstueArgumentsCount = symbol.owner.konstueParameters.size,
                origin = null,
                superQualifierSymbol = null
            )

    }
}
