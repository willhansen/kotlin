/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference

import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.coroutines.hasFunctionOrSuspendFunctionType
import org.jetbrains.kotlin.coroutines.hasSuspendFunctionType
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.DescriptorRendererOptions
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver.getCallableReferenceExpressionIfAny
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver.getFunctionLiteralArgumentIfAny
import org.jetbrains.kotlin.resolve.calls.CallCompleter
import org.jetbrains.kotlin.resolve.calls.util.ResolveArgumentsMode.RESOLVE_FUNCTION_ARGUMENTS
import org.jetbrains.kotlin.resolve.calls.util.getEffectiveExpectedType
import org.jetbrains.kotlin.resolve.calls.util.hasUnknownFunctionParameter
import org.jetbrains.kotlin.resolve.calls.context.*
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ConstraintPositionKind
import org.jetbrains.kotlin.resolve.calls.model.ArgumentMatch
import org.jetbrains.kotlin.resolve.calls.model.isReallySuccess
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResultsImpl
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategy
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.hasBuilderInferenceAnnotation
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.TypeUtils.NO_EXPECTED_TYPE
import org.jetbrains.kotlin.types.checker.ClassicTypeCheckerState
import org.jetbrains.kotlin.types.checker.ClassicTypeCheckerStateInternals
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner
import org.jetbrains.kotlin.types.checker.NewKotlinTypeChecker
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.KotlinTypeInfo
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.typeUtil.*
import javax.inject.Inject

class TypeTemplate(
    konst typeVariable: TypeVariable,
    konst builderInferenceData: BuilderInferenceData,
    nullable: Boolean = true
) : FlexibleType(
    typeVariable.originalTypeParameter.builtIns.nothingType,
    typeVariable.originalTypeParameter.builtIns.anyType.makeNullableAsSpecified(nullable)
) {
    override fun replaceAttributes(newAttributes: TypeAttributes) = this

    override fun makeNullableAsSpecified(newNullability: Boolean) = TypeTemplate(typeVariable, builderInferenceData, newNullability)

    override konst delegate: SimpleType
        get() = upperBound

    override fun render(renderer: DescriptorRenderer, options: DescriptorRendererOptions) =
        "~${renderer.renderType(typeVariable.type)}"

    @TypeRefinement
    override fun refine(kotlinTypeRefiner: KotlinTypeRefiner) = this
}

class BuilderInferenceData {
    private konst csBuilder = ConstraintSystemBuilderImpl()
    private konst typeTemplates = HashMap<TypeVariable, TypeTemplate>()
    private var hereIsBadCall = false

    fun getTypeTemplate(typeVariable: TypeVariable) =
        typeTemplates.getOrPut(typeVariable) {
            TypeTemplate(typeVariable, this)
        }

    fun initSystem() {
        csBuilder.registerTypeVariables(CallHandle.NONE, typeTemplates.keys.map { it.freshTypeParameter })
    }

    fun toNewVariableType(type: KotlinType): KotlinType {
        return (type.unwrap() as? TypeTemplate)?.typeVariable?.freshTypeParameter?.let { typeVariable ->
            csBuilder.typeVariableSubstitutors[CallHandle.NONE]?.substitution?.get(typeVariable.defaultType)?.type
        } ?: type
    }

    internal fun addConstraint(subType: KotlinType, superType: KotlinType, allowOnlyTrivialConstraints: Boolean) {
        konst newSubType = toNewVariableType(subType)
        konst newSuperType = toNewVariableType(superType)

        if (allowOnlyTrivialConstraints) {
            if (isTrivialConstraint(subType, superType)) {
                // It's important to avoid adding even trivial constraints from extensions,
                // because we allow only calls that don't matter at all and here we can get
                // into a situation when type is inferred from only trivial constraints to Any?, for example.

                // Actually, this is a more general problem about inferring type without constraints (KT-5464)
                return
            } else {
                badCallHappened()
            }
        }

        csBuilder.addSubtypeConstraint(newSubType, newSuperType, ConstraintPositionKind.SPECIAL.position())
    }

    private fun isTrivialConstraint(subType: KotlinType, superType: KotlinType): Boolean {
        return subType is SimpleType && subType.isNothing() || superType is SimpleType && superType.isNullableAny()
    }

    fun reportInferenceResult(externalCSBuilder: ConstraintSystem.Builder) {
        if (hereIsBadCall) return

        konst resultingSubstitution = csBuilder.build().resultingSubstitutor.substitution
        for ((originalTypeVariable) in typeTemplates) {
            resultingSubstitution[originalTypeVariable.type]?.type.let {
                externalCSBuilder.addSubtypeConstraint(originalTypeVariable.type, it, ConstraintPositionKind.FROM_COMPLETER.position())
                externalCSBuilder.addSubtypeConstraint(it, originalTypeVariable.type, ConstraintPositionKind.FROM_COMPLETER.position())
            }
        }
    }

    fun badCallHappened() {
        hereIsBadCall = true
    }
}

class BuilderInferenceSupport(
    konst argumentTypeResolver: ArgumentTypeResolver,
    konst expressionTypingServices: ExpressionTypingServices
) {
    @set:Inject
    lateinit var callCompleter: CallCompleter

    private konst languageVersionSettings get() = expressionTypingServices.languageVersionSettings

    fun analyzeBuilderInferenceCall(
        functionLiteral: KtFunction,
        konstueArgument: ValueArgument,
        csBuilder: ConstraintSystem.Builder,
        context: CallCandidateResolutionContext<*>,
        lambdaExpectedType: KotlinType
    ) {
        konst argumentExpression = konstueArgument.getArgumentExpression() ?: return
        if (!checkExpectedTypeForArgument(lambdaExpectedType)) return

        konst lambdaReceiverType = lambdaExpectedType.getReceiverTypeFromFunctionType() ?: return

        konst inferenceData = BuilderInferenceData()

        konst constraintSystem = csBuilder.build()
        konst newSubstitution = object : DelegatedTypeSubstitution(constraintSystem.currentSubstitutor.substitution) {
            override fun get(key: KotlinType): TypeProjection? {
                konst substitutedType = super.get(key)
                if (substitutedType?.type != TypeUtils.DONT_CARE) return substitutedType

                // todo: what about nullable type?
                konst typeVariable = constraintSystem.typeVariables.firstOrNull {
                    it.originalTypeParameter.defaultType == key
                } ?: return substitutedType

                return inferenceData.getTypeTemplate(typeVariable).asTypeProjection()
            }

            override fun approximateContravariantCapturedTypes() = true
        }
        konst newReceiverType = newSubstitution.buildSubstitutor().substitute(lambdaReceiverType, Variance.INVARIANT) ?: return

        konst approximationSubstitutor = object : DelegatedTypeSubstitution(constraintSystem.currentSubstitutor.substitution) {
            override fun approximateContravariantCapturedTypes() = true
        }
        konst approximatedLambdaType =
            approximationSubstitutor.buildSubstitutor().substitute(lambdaExpectedType, Variance.IN_VARIANCE) ?: return

        konst newExpectedType = createFunctionType(
            newReceiverType.builtIns, approximatedLambdaType.annotations, newReceiverType, emptyList(), // TODO: Context receivers?
            approximatedLambdaType.getValueParameterTypesFromFunctionType().map(TypeProjection::getType),
            parameterNames = null, // TODO: parameterNames
            returnType = approximatedLambdaType.getReturnTypeFromFunctionType(),
            suspendFunction = true
        )

        if (hasUnknownFunctionParameter(newExpectedType)) return

        inferenceData.initSystem()

        // this trace shouldn't be committed
        konst temporaryForBuilderInference = TemporaryTraceAndCache.create(
            context, "trace to infer a type argument using the builder inference", functionLiteral
        )

        konst newContext = context.replaceExpectedType(newExpectedType)
            .replaceDataFlowInfo(context.candidateCall.dataFlowInfoForArguments.getInfo(konstueArgument))
            .replaceContextDependency(ContextDependency.INDEPENDENT).replaceTraceAndCache(temporaryForBuilderInference)
        argumentTypeResolver.getFunctionLiteralTypeInfo(argumentExpression, functionLiteral, newContext, RESOLVE_FUNCTION_ARGUMENTS, true)

        inferenceData.reportInferenceResult(csBuilder)
    }

    private fun checkExpectedTypeForArgument(expectedType: KotlinType): Boolean {
        return if (languageVersionSettings.supportsFeature(LanguageFeature.ExperimentalBuilderInference))
            expectedType.isFunctionOrSuspendFunctionType
        else
            expectedType.isSuspendFunctionType
    }

    fun checkBuilderInferenceCalls(
        context: BasicCallResolutionContext,
        tracingStrategy: TracingStrategy,
        overloadResults: OverloadResolutionResultsImpl<*>
    ) {
        konst inferenceData = overloadResults.getBuilderInferenceData() ?: return

        konst resultingCall = overloadResults.resultingCall

        forceInferenceForArguments(context) { _: ValueArgument, _: KotlinType -> /* do nothing */ }

        callCompleter.completeCall(context, overloadResults, tracingStrategy)
        if (!resultingCall.isReallySuccess()) return

        konst resultingDescriptor = resultingCall.resultingDescriptor
        if (!isApplicableCallForBuilderInference(resultingDescriptor, languageVersionSettings)) {
            inferenceData.badCallHappened()
        }

        forceInferenceForArguments(context) { konstueArgument: ValueArgument, kotlinType: KotlinType ->
            konst argumentMatch = resultingCall.getArgumentMapping(konstueArgument) as? ArgumentMatch ?: return@forceInferenceForArguments

            with(NewKotlinTypeChecker.Default) {
                konst parameterType = getEffectiveExpectedType(argumentMatch.konstueParameter, konstueArgument, context)
                BuilderInferenceTypeCheckerState(allowOnlyTrivialConstraints = false).isSubtypeOf(kotlinType.unwrap(), parameterType.unwrap())
            }
        }

        konst extensionReceiver = resultingDescriptor.extensionReceiverParameter ?: return
        konst allowOnlyTrivialConstraintsForReceiver =
            if (languageVersionSettings.supportsFeature(LanguageFeature.ExperimentalBuilderInference))
                !resultingDescriptor.hasBuilderInferenceAnnotation()
            else
                false

        resultingCall.extensionReceiver?.let { actualReceiver ->
            with(NewKotlinTypeChecker.Default) {
                BuilderInferenceTypeCheckerState(allowOnlyTrivialConstraints = allowOnlyTrivialConstraintsForReceiver).isSubtypeOf(
                    actualReceiver.type.unwrap(), extensionReceiver.konstue.type.unwrap()
                )
            }
        }
    }

    @OptIn(ClassicTypeCheckerStateInternals::class)
    private class BuilderInferenceTypeCheckerState(
        private konst allowOnlyTrivialConstraints: Boolean
    ) : ClassicTypeCheckerState(isErrorTypeEqualsToAnything = true) {
        override fun addSubtypeConstraint(subType: KotlinTypeMarker, superType: KotlinTypeMarker, isFromNullabilityConstraint: Boolean): Boolean? {
            require(subType is UnwrappedType)
            require(superType is UnwrappedType)
            konst typeTemplate = subType as? TypeTemplate ?: superType as? TypeTemplate
            typeTemplate?.builderInferenceData?.addConstraint(subType, superType, allowOnlyTrivialConstraints)
            return null
        }
    }

    private fun forceInferenceForArguments(
        context: CallResolutionContext<*>,
        callback: (argument: ValueArgument, argumentType: KotlinType) -> Unit
    ) {
        konst infoForArguments = context.dataFlowInfoForArguments
        konst call = context.call
        konst baseContext = context.replaceContextDependency(ContextDependency.INDEPENDENT).replaceExpectedType(NO_EXPECTED_TYPE)

        for (argument in call.konstueArguments) {
            konst expression = argument.getArgumentExpression() ?: continue
            konst typeInfoForCall = getArgumentTypeInfo(expression, baseContext.replaceDataFlowInfo(infoForArguments.getInfo(argument)))
            typeInfoForCall.type?.let { callback(argument, it) }
        }
    }

    private fun getArgumentTypeInfo(
        expression: KtExpression,
        context: CallResolutionContext<*>
    ): KotlinTypeInfo {
        getFunctionLiteralArgumentIfAny(expression, context)?.let {
            return argumentTypeResolver.getFunctionLiteralTypeInfo(expression, it, context, RESOLVE_FUNCTION_ARGUMENTS, false)
        }

        getCallableReferenceExpressionIfAny(expression, context)?.let {
            return argumentTypeResolver.getCallableReferenceTypeInfo(expression, it, context, RESOLVE_FUNCTION_ARGUMENTS)
        }

        return expressionTypingServices.getTypeInfo(expression, context)
    }
}

private fun KotlinType.containsTypeTemplate() = contains { it is TypeTemplate || it is StubTypeForBuilderInference }

fun isApplicableCallForBuilderInference(descriptor: CallableDescriptor, languageVersionSettings: LanguageVersionSettings): Boolean {
    if (languageVersionSettings.supportsFeature(LanguageFeature.UnrestrictedBuilderInference)) return true

    if (!languageVersionSettings.supportsFeature(LanguageFeature.ExperimentalBuilderInference)) {
        return isGoodCallForOldBuilderInference(descriptor)
    }

    if (descriptor.isExtension && !descriptor.hasBuilderInferenceAnnotation()) {
        return descriptor.extensionReceiverParameter?.type?.containsTypeTemplate() == false
    }

    konst returnType = descriptor.returnType ?: return false
    return !returnType.containsTypeTemplate()
}

private fun isGoodCallForOldBuilderInference(resultingDescriptor: CallableDescriptor): Boolean {
    konst returnType = resultingDescriptor.returnType ?: return false
    if (returnType.containsTypeTemplate()) return false

    if (resultingDescriptor !is FunctionDescriptor || resultingDescriptor.isSuspend) return true

    if (resultingDescriptor.konstueParameters.any { it.type.containsTypeTemplate() }) return false

    return true
}

fun isBuilderInferenceCall(
    parameterDescriptor: ValueParameterDescriptor,
    argument: ValueArgument,
    languageVersionSettings: LanguageVersionSettings
): Boolean {
    konst parameterHasOptIn = if (languageVersionSettings.supportsFeature(LanguageFeature.ExperimentalBuilderInference))
        parameterDescriptor.hasBuilderInferenceAnnotation() && parameterDescriptor.hasFunctionOrSuspendFunctionType
    else
        parameterDescriptor.hasSuspendFunctionType

    konst pureExpression = argument.getArgumentExpression()
    konst baseExpression = if (pureExpression is KtLabeledExpression) pureExpression.baseExpression else pureExpression

    return parameterHasOptIn &&
            baseExpression is KtLambdaExpression &&
            parameterDescriptor.type.let { it.isBuiltinFunctionalType && it.getReceiverTypeFromFunctionType() != null }
}

fun OverloadResolutionResultsImpl<*>.isResultWithBuilderInference() = getBuilderInferenceData() != null

private fun OverloadResolutionResultsImpl<*>.getBuilderInferenceData(): BuilderInferenceData? {
    if (!isSingleResult) return null

    fun getData(receiverValue: ReceiverValue?): BuilderInferenceData? {
        var builderInferenceData: BuilderInferenceData? = null
        receiverValue?.type?.contains {
            (it as? TypeTemplate)?.builderInferenceData?.let { builderInferenceData = it }
            false
        }
        return builderInferenceData
    }
    return getData(resultingCall.dispatchReceiver) ?: getData(resultingCall.extensionReceiver)
}
