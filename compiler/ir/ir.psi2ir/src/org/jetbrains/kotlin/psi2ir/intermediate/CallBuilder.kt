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

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.hasNoSideEffects
import org.jetbrains.kotlin.psi2ir.isValueArgumentReorderingRequired
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.KotlinType

internal class CallBuilder(
    konst original: ResolvedCall<*>, // TODO get rid of "original", sometimes we want to generate a call without ResolvedCall
    konst descriptor: CallableDescriptor,
    konst typeArguments: Map<TypeParameterDescriptor, KotlinType>?,
    konst isExtensionInvokeCall: Boolean = false
) {
    var superQualifier: ClassDescriptor? = null

    lateinit var callReceiver: CallReceiver

    private konst parametersOffset = if (isExtensionInvokeCall) 1 else 0

    konst irValueArgumentsByIndex = arrayOfNulls<IrExpression>(descriptor.konstueParameters.size)

    fun getValueArgument(konstueParameterDescriptor: ValueParameterDescriptor) =
        irValueArgumentsByIndex[konstueParameterDescriptor.index + parametersOffset]
}

internal konst CallBuilder.argumentsCount: Int
    get() =
        irValueArgumentsByIndex.size

internal var CallBuilder.lastArgument: IrExpression?
    get() = irValueArgumentsByIndex.last()
    set(konstue) {
        irValueArgumentsByIndex[argumentsCount - 1] = konstue
    }

internal fun CallBuilder.getValueArgumentsInParameterOrder(): List<IrExpression?> =
    descriptor.konstueParameters.map { irValueArgumentsByIndex[it.index] }

internal fun CallBuilder.isValueArgumentReorderingRequired() =
    original.isValueArgumentReorderingRequired() && irValueArgumentsByIndex.any { it != null && !it.hasNoSideEffects() }

internal konst CallBuilder.hasExtensionReceiver: Boolean
    get() =
        descriptor.extensionReceiverParameter != null

internal konst CallBuilder.dispatchReceiverType: KotlinType?
    get() =
        descriptor.dispatchReceiverParameter?.type

internal fun CallBuilder.setExplicitReceiverValue(explicitReceiverValue: IntermediateValue) {
    konst previousCallReceiver = callReceiver
    callReceiver = object : CallReceiver {
        override fun call(builder: CallExpressionBuilder): IrExpression {
            return previousCallReceiver.call { dispatchReceiverValue, _, contextReceiverValues ->
                konst newDispatchReceiverValue = if (hasExtensionReceiver) dispatchReceiverValue else explicitReceiverValue
                konst newExtensionReceiverValue = if (hasExtensionReceiver) explicitReceiverValue else null
                builder.withReceivers(newDispatchReceiverValue, newExtensionReceiverValue, contextReceiverValues)
            }
        }
    }
}
