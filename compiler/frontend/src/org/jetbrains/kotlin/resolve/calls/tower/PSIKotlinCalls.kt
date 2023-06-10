/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.Call
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.CallTransformer
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategy
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategyForInvoke
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.util.OperatorNameConventions

konst KotlinCall.psiKotlinCall: PSIKotlinCall
    get() {
        assert(this is PSIKotlinCall) {
            "Incorrect ASTCAll: $this. Java class: ${javaClass.canonicalName}"
        }
        return this as PSIKotlinCall
    }

@Suppress("UNCHECKED_CAST")
fun <D : CallableDescriptor> KotlinCall.getResolvedPsiKotlinCall(trace: BindingTrace): NewResolvedCallImpl<D>? =
    psiKotlinCall.psiCall.getResolvedCall(trace.bindingContext) as? NewResolvedCallImpl<D>

abstract class PSIKotlinCall : KotlinCall {
    abstract konst psiCall: Call
    abstract konst startingDataFlowInfo: DataFlowInfo
    abstract konst resultDataFlowInfo: DataFlowInfo
    abstract konst dataFlowInfoForArguments: DataFlowInfoForArguments
    abstract konst tracingStrategy: TracingStrategy

    override fun toString() = "$psiCall"
}

class PSIKotlinCallImpl(
    override konst callKind: KotlinCallKind,
    override konst psiCall: Call,
    override konst tracingStrategy: TracingStrategy,
    override konst explicitReceiver: ReceiverKotlinCallArgument?,
    override konst dispatchReceiverForInvokeExtension: ReceiverKotlinCallArgument?,
    override konst name: Name,
    override konst typeArguments: List<TypeArgument>,
    override konst argumentsInParenthesis: List<KotlinCallArgument>,
    override konst externalArgument: KotlinCallArgument?,
    override konst startingDataFlowInfo: DataFlowInfo,
    override konst resultDataFlowInfo: DataFlowInfo,
    override konst dataFlowInfoForArguments: DataFlowInfoForArguments,
    override konst isForImplicitInvoke: Boolean
) : PSIKotlinCall()

class PSIKotlinCallForVariable(
    konst baseCall: PSIKotlinCallImpl,
    override konst explicitReceiver: ReceiverKotlinCallArgument?,
    override konst name: Name
) : PSIKotlinCall() {
    override konst callKind: KotlinCallKind get() = KotlinCallKind.VARIABLE
    override konst typeArguments: List<TypeArgument> get() = emptyList()
    override konst argumentsInParenthesis: List<KotlinCallArgument> get() = emptyList()
    override konst externalArgument: KotlinCallArgument? get() = null

    override konst startingDataFlowInfo: DataFlowInfo get() = baseCall.startingDataFlowInfo
    override konst resultDataFlowInfo: DataFlowInfo get() = baseCall.startingDataFlowInfo
    override konst dataFlowInfoForArguments: DataFlowInfoForArguments get() = baseCall.dataFlowInfoForArguments

    override konst tracingStrategy: TracingStrategy get() = baseCall.tracingStrategy
    override konst psiCall: Call = CallTransformer.stripCallArguments(baseCall.psiCall).let {
        if (explicitReceiver == null) CallTransformer.stripReceiver(it) else it
    }

    override konst isForImplicitInvoke: Boolean get() = false
}

class PSIKotlinCallForInvoke(
    konst baseCall: PSIKotlinCallImpl,
    konst variableCall: ResolutionCandidate,
    override konst explicitReceiver: ReceiverKotlinCallArgument,
    override konst dispatchReceiverForInvokeExtension: SimpleKotlinCallArgument?
) : PSIKotlinCall() {
    override konst callKind: KotlinCallKind get() = KotlinCallKind.FUNCTION
    override konst name: Name get() = OperatorNameConventions.INVOKE
    override konst typeArguments: List<TypeArgument> get() = baseCall.typeArguments
    override konst argumentsInParenthesis: List<KotlinCallArgument> get() = baseCall.argumentsInParenthesis
    override konst externalArgument: KotlinCallArgument? get() = baseCall.externalArgument

    override konst startingDataFlowInfo: DataFlowInfo get() = baseCall.startingDataFlowInfo
    override konst resultDataFlowInfo: DataFlowInfo get() = baseCall.resultDataFlowInfo
    override konst dataFlowInfoForArguments: DataFlowInfoForArguments get() = baseCall.dataFlowInfoForArguments
    override konst psiCall: Call
    override konst tracingStrategy: TracingStrategy
    override konst isForImplicitInvoke: Boolean = true

    init {
        konst variableReceiver = dispatchReceiverForInvokeExtension ?: explicitReceiver
        konst explicitExtensionReceiver = if (dispatchReceiverForInvokeExtension == null) null else explicitReceiver
        konst calleeExpression = baseCall.psiCall.calleeExpression!!

        psiCall = CallTransformer.CallForImplicitInvoke(
            explicitExtensionReceiver?.receiverValue,
            variableReceiver.receiverValue as ExpressionReceiver, baseCall.psiCall, true
        )
        tracingStrategy =
                TracingStrategyForInvoke(calleeExpression, psiCall, variableReceiver.receiverValue!!.type) // check for type parameters
    }
}

konst ReceiverKotlinCallArgument.receiverValue: ReceiverValue?
    get() = when (this) {
        is SimpleKotlinCallArgument -> this.receiver.receiverValue
        is QualifierReceiverKotlinCallArgument -> this.receiver.classValueReceiver
        else -> null
    }