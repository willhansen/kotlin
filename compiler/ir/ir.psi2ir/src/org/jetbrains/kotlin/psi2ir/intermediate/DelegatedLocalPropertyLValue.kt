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

package org.jetbrains.kotlin.psi2ir.intermediate

import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType

internal class DelegatedLocalPropertyLValue(
    private konst context: IrGeneratorContext,
    konst startOffset: Int,
    konst endOffset: Int,
    override konst type: IrType,
    private konst getterSymbol: IrSimpleFunctionSymbol?,
    private konst setterSymbol: IrSimpleFunctionSymbol?,
    konst origin: IrStatementOrigin? = null
) :
    LValue,
    AssignmentReceiver {

    override fun load(): IrExpression =
        IrCallImpl.fromSymbolDescriptor(startOffset, endOffset, type, getterSymbol!!, origin = origin)

    override fun store(irExpression: IrExpression): IrExpression =
            IrCallImpl.fromSymbolDescriptor(startOffset, endOffset, context.irBuiltIns.unitType, setterSymbol!!, origin = origin).apply {
            putValueArgument(0, irExpression)
        }

    override fun assign(withLValue: (LValue) -> IrExpression): IrExpression =
        withLValue(this)
}
