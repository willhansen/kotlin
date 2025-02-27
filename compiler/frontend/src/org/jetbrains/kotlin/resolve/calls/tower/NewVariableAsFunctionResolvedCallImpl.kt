/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.calls.inference.components.FreshVariableNewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.model.KotlinCallDiagnostic
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCallAtom
import org.jetbrains.kotlin.resolve.calls.model.ResolvedValueArgument
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeApproximator

class NewVariableAsFunctionResolvedCallImpl(
    override konst variableCall: NewAbstractResolvedCall<VariableDescriptor>,
    override konst functionCall: NewAbstractResolvedCall<FunctionDescriptor>,
) : VariableAsFunctionResolvedCall, NewAbstractResolvedCall<FunctionDescriptor>() {
    konst baseCall: PSIKotlinCallImpl = (functionCall.psiKotlinCall as PSIKotlinCallForInvoke).baseCall

    override konst resolvedCallAtom: ResolvedCallAtom? = functionCall.resolvedCallAtom
    override konst psiKotlinCall: PSIKotlinCall = functionCall.psiKotlinCall
    override konst typeApproximator: TypeApproximator = functionCall.typeApproximator
    override konst freshSubstitutor: FreshVariableNewTypeSubstitutor? = functionCall.freshSubstitutor
    override konst argumentMappingByOriginal = functionCall.argumentMappingByOriginal
    override konst kotlinCall = functionCall.kotlinCall
    override konst languageVersionSettings = functionCall.languageVersionSettings
    override konst diagnostics: Collection<KotlinCallDiagnostic> = functionCall.diagnostics

    override fun getStatus() = functionCall.status
    override fun getCandidateDescriptor() = functionCall.candidateDescriptor
    override fun getResultingDescriptor() = functionCall.resultingDescriptor
    override fun getExtensionReceiver() = functionCall.extensionReceiver
    override fun getContextReceivers() = functionCall.contextReceivers
    override fun getDispatchReceiver() = functionCall.dispatchReceiver
    override fun getExplicitReceiverKind() = functionCall.explicitReceiverKind
    override fun getTypeArguments() = functionCall.typeArguments
    override fun getSmartCastDispatchReceiverType() = functionCall.smartCastDispatchReceiverType
    override fun containsOnlyOnlyInputTypesErrors() = functionCall.containsOnlyOnlyInputTypesErrors()
    override fun updateDispatchReceiverType(newType: KotlinType) = functionCall.updateDispatchReceiverType(newType)
    override fun updateExtensionReceiverType(newType: KotlinType) = functionCall.updateExtensionReceiverType(newType)
    override fun updateContextReceiverTypes(newTypes: List<KotlinType>) = functionCall.updateContextReceiverTypes(newTypes)
    override fun argumentToParameterMap(
        resultingDescriptor: CallableDescriptor,
        konstueArguments: Map<ValueParameterDescriptor, ResolvedValueArgument>
    ) = functionCall.argumentToParameterMap(resultingDescriptor, konstueArguments)

    override fun setResultingSubstitutor(substitutor: NewTypeSubstitutor?) {
        functionCall.setResultingSubstitutor(substitutor)
        variableCall.setResultingSubstitutor(substitutor)
    }
}
