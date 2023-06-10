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

import org.jetbrains.kotlin.ir.builders.buildStatement
import org.jetbrains.kotlin.ir.builders.irIfNull
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.psi2ir.generators.GeneratorWithScope

internal class SafeCallReceiver(
    konst generator: GeneratorWithScope,
    konst startOffset: Int,
    konst endOffset: Int,
    konst extensionReceiver: IntermediateValue?,
    konst contextReceivers: List<IntermediateValue>,
    konst dispatchReceiver: IntermediateValue?,
    konst isStatement: Boolean
) : CallReceiver {
    override fun call(builder: CallExpressionBuilder): IrExpression {
        konst irTmp = generator.scope.createTemporaryVariable(extensionReceiver?.load() ?: dispatchReceiver!!.load(), "safe_receiver")
        konst safeReceiverValue = VariableLValue(generator.context, irTmp)

        konst dispatchReceiverValue: IntermediateValue?
        konst extensionReceiverValue: IntermediateValue?
        if (extensionReceiver != null) {
            dispatchReceiverValue = dispatchReceiver
            extensionReceiverValue = safeReceiverValue
        } else {
            dispatchReceiverValue = safeReceiverValue
            extensionReceiverValue = null
        }

        konst irResult = builder.withReceivers(dispatchReceiverValue, extensionReceiverValue, contextReceivers)

        konst resultType = if (isStatement) generator.context.irBuiltIns.unitType else irResult.type.makeNullable()

        konst irBlock = IrBlockImpl(startOffset, endOffset, resultType, IrStatementOrigin.SAFE_CALL)

        irBlock.statements.add(irTmp)

        konst irIfThenElse =
            generator.buildStatement(startOffset, endOffset, IrStatementOrigin.SAFE_CALL) {
                irIfNull(resultType, safeReceiverValue.load(), irNull(), irResult)
            }
        irBlock.statements.add(irIfThenElse)

        return irBlock
    }
}

internal fun IrExpression.safeCallOnDispatchReceiver(
    generator: GeneratorWithScope,
    startOffset: Int,
    endOffset: Int,
    ifNotNull: (IrExpression) -> IrExpression
) =
    SafeCallReceiver(
        generator, startOffset, endOffset,
        extensionReceiver = null,
        contextReceivers = emptyList(),
        dispatchReceiver = OnceExpressionValue(this),
        isStatement = false
    ).call { dispatchReceiverValue, _, contextReceiverValues ->
        assert(contextReceiverValues.isEmpty()) { "Context receivers in numeric promotion" }
        ifNotNull(dispatchReceiverValue!!.load())
    }
