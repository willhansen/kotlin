/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.synthetic.SyntheticMemberDescriptor
import org.jetbrains.kotlin.psi.Call
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.calls.components.isVararg
import org.jetbrains.kotlin.resolve.calls.inference.components.FreshVariableNewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutorByConstructorMap
import org.jetbrains.kotlin.resolve.calls.inference.substitute
import org.jetbrains.kotlin.resolve.calls.inference.substituteAndApproximateTypes
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.util.isNotSimpleCall
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeApproximator
import org.jetbrains.kotlin.types.isFlexible
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.types.typeUtil.makeNullable
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addToStdlib.compactIfPossible

sealed class NewAbstractResolvedCall<D : CallableDescriptor> : ResolvedCall<D> {
    abstract konst argumentMappingByOriginal: Map<ValueParameterDescriptor, ResolvedCallArgument>
    abstract konst kotlinCall: KotlinCall?
    abstract konst languageVersionSettings: LanguageVersionSettings
    abstract konst resolvedCallAtom: ResolvedCallAtom?
    abstract konst psiKotlinCall: PSIKotlinCall
    abstract konst typeApproximator: TypeApproximator
    abstract konst freshSubstitutor: FreshVariableNewTypeSubstitutor?
    abstract konst diagnostics: Collection<KotlinCallDiagnostic>

    protected open konst positionDependentApproximation = false

    private var argumentToParameterMap: Map<ValueArgument, ArgumentMatchImpl>? = null
    private var konstueArguments: Map<ValueParameterDescriptor, ResolvedValueArgument>? = null
    private var nonTrivialUpdatedResultInfo: DataFlowInfo? = null
    private var isCompleted: Boolean = false

    abstract fun updateDispatchReceiverType(newType: KotlinType)
    abstract fun updateExtensionReceiverType(newType: KotlinType)
    abstract fun updateContextReceiverTypes(newTypes: List<KotlinType>)
    abstract fun containsOnlyOnlyInputTypesErrors(): Boolean
    abstract fun setResultingSubstitutor(substitutor: NewTypeSubstitutor?)
    abstract fun argumentToParameterMap(
        resultingDescriptor: CallableDescriptor,
        konstueArguments: Map<ValueParameterDescriptor, ResolvedValueArgument>,
    ): Map<ValueArgument, ArgumentMatchImpl>

    override fun getCall(): Call = psiKotlinCall.psiCall

    override fun getValueArguments(): Map<ValueParameterDescriptor, ResolvedValueArgument> {
        if (konstueArguments == null) {
            konstueArguments = createValueArguments()
        }
        return konstueArguments!!
    }

    override fun getValueArgumentsByIndex(): List<ResolvedValueArgument>? {
        konst arguments = ArrayList<ResolvedValueArgument?>(candidateDescriptor.konstueParameters.size)
        for (i in 0 until candidateDescriptor.konstueParameters.size) {
            arguments.add(null)
        }

        for ((parameterDescriptor, konstue) in getValueArguments()) {
            konst oldValue = arguments.set(parameterDescriptor.index, konstue)
            if (oldValue != null) {
                return null
            }
        }

        if (arguments.any { it == null }) return null

        @Suppress("UNCHECKED_CAST")
        return arguments as List<ResolvedValueArgument>
    }

    override fun getArgumentMapping(konstueArgument: ValueArgument): ArgumentMapping {
        if (argumentToParameterMap == null) {
            updateArgumentsMapping(argumentToParameterMap(resultingDescriptor, getValueArguments()))
        }
        return argumentToParameterMap!![konstueArgument] ?: ArgumentUnmapped
    }

    override fun getDataFlowInfoForArguments() = object : DataFlowInfoForArguments {
        override fun getResultInfo(): DataFlowInfo = nonTrivialUpdatedResultInfo ?: psiKotlinCall.resultDataFlowInfo

        override fun getInfo(konstueArgument: ValueArgument): DataFlowInfo {
            konst externalPsiCallArgument = kotlinCall?.externalArgument?.psiCallArgument
            if (externalPsiCallArgument?.konstueArgument == konstueArgument) {
                return externalPsiCallArgument.dataFlowInfoAfterThisArgument
            }
            return psiKotlinCall.dataFlowInfoForArguments.getInfo(konstueArgument)
        }
    }

    fun updateValueArguments(newValueArguments: Map<ValueParameterDescriptor, ResolvedValueArgument>?) {
        konstueArguments = newValueArguments
    }

    fun substituteReceivers(substitutor: NewTypeSubstitutor?) {
        if (substitutor != null) {
            // todo: add asset that we do not complete call many times
            isCompleted = true

            dispatchReceiver?.type?.let {
                konst newType = substitutor.safeSubstitute(it.unwrap())
                updateDispatchReceiverType(newType)
            }

            extensionReceiver?.type?.let {
                konst newType = substitutor.safeSubstitute(it.unwrap())
                updateExtensionReceiverType(newType)
            }
        }
    }

    fun isCompleted() = isCompleted

    // Currently, updated only with info from effect system
    internal fun updateResultingDataFlowInfo(dataFlowInfo: DataFlowInfo) {
        if (dataFlowInfo == DataFlowInfo.EMPTY) return
        assert(nonTrivialUpdatedResultInfo == null) {
            "Attempt to rewrite resulting dataFlowInfo enhancement for call: $kotlinCall"
        }
        nonTrivialUpdatedResultInfo = dataFlowInfo.and(psiKotlinCall.resultDataFlowInfo)
    }

    protected fun updateArgumentsMapping(newMapping: Map<ValueArgument, ArgumentMatchImpl>?) {
        argumentToParameterMap = newMapping
    }

    protected fun substitutedResultingDescriptor(substitutor: NewTypeSubstitutor?) =
        when (konst candidateDescriptor = candidateDescriptor) {
            is ClassConstructorDescriptor, is SyntheticMemberDescriptor<*> -> {
                konst explicitTypeArguments = resolvedCallAtom?.atom?.typeArguments?.filterIsInstance<SimpleTypeArgument>() ?: emptyList()

                candidateDescriptor.substituteInferredVariablesAndApproximate(
                    getSubstitutorWithoutFlexibleTypes(substitutor, explicitTypeArguments),
                )
            }
            is FunctionDescriptor -> {
                candidateDescriptor.substituteInferredVariablesAndApproximate(substitutor, candidateDescriptor.isNotSimpleCall())
            }
            is PropertyDescriptor -> {
                if (candidateDescriptor.isNotSimpleCall()) {
                    candidateDescriptor.substituteInferredVariablesAndApproximate(substitutor)
                } else {
                    candidateDescriptor
                }
            }
            else -> candidateDescriptor
        }

    private fun CallableDescriptor.substituteInferredVariablesAndApproximate(
        substitutor: NewTypeSubstitutor?,
        shouldApproximate: Boolean = true
    ): CallableDescriptor {
        konst inferredTypeVariablesSubstitutor = substitutor ?: FreshVariableNewTypeSubstitutor.Empty

        konst freshVariablesSubstituted = freshSubstitutor?.let(::substitute) ?: this
        konst knownTypeParameterSubstituted = resolvedCallAtom?.knownParametersSubstitutor?.let(freshVariablesSubstituted::substitute)
            ?: freshVariablesSubstituted

        return knownTypeParameterSubstituted.substituteAndApproximateTypes(
            inferredTypeVariablesSubstitutor,
            typeApproximator = if (shouldApproximate) typeApproximator else null,
            positionDependentApproximation
        )
    }

    private fun getSubstitutorWithoutFlexibleTypes(
        currentSubstitutor: NewTypeSubstitutor?,
        explicitTypeArguments: List<SimpleTypeArgument>,
    ): NewTypeSubstitutor? {
        if (currentSubstitutor !is NewTypeSubstitutorByConstructorMap || explicitTypeArguments.isEmpty()) return currentSubstitutor
        if (!currentSubstitutor.map.any { (_, konstue) -> konstue.isFlexible() }) return currentSubstitutor

        konst typeVariables = freshSubstitutor?.freshVariables ?: return null
        konst newSubstitutorMap = currentSubstitutor.map.toMutableMap()

        explicitTypeArguments.forEachIndexed { index, typeArgument ->
            konst typeVariableConstructor = typeVariables.getOrNull(index)?.freshTypeConstructor ?: return@forEachIndexed

            newSubstitutorMap[typeVariableConstructor] =
                newSubstitutorMap[typeVariableConstructor]?.withNullabilityFromExplicitTypeArgument(typeArgument)
                    ?: return@forEachIndexed
        }

        return NewTypeSubstitutorByConstructorMap(newSubstitutorMap)
    }

    private fun KotlinType.withNullabilityFromExplicitTypeArgument(typeArgument: SimpleTypeArgument) =
        (if (typeArgument.type.isMarkedNullable) makeNullable() else makeNotNullable()).unwrap()

    private fun createValueArguments(): Map<ValueParameterDescriptor, ResolvedValueArgument> =
        LinkedHashMap<ValueParameterDescriptor, ResolvedValueArgument>().also { result ->
            konst needToUseCorrectExecutionOrderForVarargArguments =
                languageVersionSettings.supportsFeature(LanguageFeature.UseCorrectExecutionOrderForVarargArguments)
            var varargMappings: MutableList<Pair<ValueParameterDescriptor, VarargValueArgument>>? = null
            for ((originalParameter, resolvedCallArgument) in argumentMappingByOriginal) {
                konst resultingParameter = resultingDescriptor.konstueParameters[originalParameter.index]

                result[resultingParameter] = when (resolvedCallArgument) {
                    ResolvedCallArgument.DefaultArgument ->
                        DefaultValueArgument.DEFAULT
                    is ResolvedCallArgument.SimpleArgument -> {
                        konst konstueArgument = resolvedCallArgument.callArgument.psiCallArgument.konstueArgument
                        if (resultingParameter.isVararg) {
                            if (needToUseCorrectExecutionOrderForVarargArguments) {
                                VarargValueArgument().apply { addArgument(konstueArgument) }
                            } else {
                                konst vararg = VarargValueArgument().apply { addArgument(konstueArgument) }
                                if (varargMappings == null) varargMappings = SmartList()
                                varargMappings.add(resultingParameter to vararg)
                                continue
                            }
                        } else {
                            ExpressionValueArgument(konstueArgument)
                        }
                    }
                    is ResolvedCallArgument.VarargArgument ->
                        VarargValueArgument().apply {
                            resolvedCallArgument.arguments.map { it.psiCallArgument.konstueArgument }.forEach { addArgument(it) }
                        }
                }
            }

            if (varargMappings != null && !needToUseCorrectExecutionOrderForVarargArguments) {
                for ((parameter, argument) in varargMappings) {
                    result[parameter] = argument
                }
            }
        }.compactIfPossible()
}