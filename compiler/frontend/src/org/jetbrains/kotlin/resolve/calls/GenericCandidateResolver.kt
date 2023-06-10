/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls

import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getBinaryWithTypeParent
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.FunctionDescriptorUtil
import org.jetbrains.kotlin.resolve.TemporaryBindingTrace
import org.jetbrains.kotlin.resolve.calls.util.*
import org.jetbrains.kotlin.resolve.calls.util.ResolveArgumentsMode.RESOLVE_FUNCTION_ARGUMENTS
import org.jetbrains.kotlin.resolve.calls.util.ResolveArgumentsMode.SHAPE_FUNCTION_ARGUMENTS
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.util.isSafeCall
import org.jetbrains.kotlin.resolve.calls.context.CallCandidateResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency.INDEPENDENT
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.ResolutionResultsCache
import org.jetbrains.kotlin.resolve.calls.context.TemporaryTraceAndCache
import org.jetbrains.kotlin.resolve.calls.inference.*
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ConstraintPositionKind
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ConstraintPositionKind.RECEIVER_POSITION
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ConstraintPositionKind.VALUE_PARAMETER_POSITION
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ValidityConstraintForConstituentType
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.makeNullableTypeIfSafeReceiver
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus.INCOMPLETE_TYPE_INFERENCE
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus.OTHER_ERROR
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.isFunctionForExpectTypeFromCastFeature
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.TypeUtils.DONT_CARE
import org.jetbrains.kotlin.types.expressions.ControlStructureTypingUtils.ResolveConstruct
import org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils
import org.jetbrains.kotlin.types.typeUtil.makeNullable

konst SPECIAL_FUNCTION_NAMES = ResolveConstruct.konstues().map { it.specialFunctionName }.toSet()

class GenericCandidateResolver(
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst builderInferenceSupport: BuilderInferenceSupport,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst dataFlowValueFactory: DataFlowValueFactory
) {
    fun <D : CallableDescriptor> inferTypeArguments(context: CallCandidateResolutionContext<D>): ResolutionStatus {
        konst candidateCall = context.candidateCall
        konst candidate = candidateCall.candidateDescriptor

        konst builder = ConstraintSystemBuilderImpl()
        builder.registerTypeVariables(candidateCall.call.toHandle(), candidate.typeParameters)

        konst substituteDontCare = makeConstantSubstitutor(candidate.typeParameters, DONT_CARE)

        // Value parameters
        for ((candidateParameter, resolvedValueArgument) in candidateCall.konstueArguments) {
            konst konstueParameterDescriptor = candidate.konstueParameters[candidateParameter.index]

            for (konstueArgument in resolvedValueArgument.arguments) {
                // TODO : more attempts, with different expected types

                // Here we type check expecting an error type (DONT_CARE, substitution with substituteDontCare)
                // and throw the results away
                // We'll type check the arguments later, with the inferred types expected
                addConstraintForValueArgument(
                    konstueArgument, konstueParameterDescriptor, substituteDontCare, builder, context, SHAPE_FUNCTION_ARGUMENTS
                )
            }
        }

        if (candidate is TypeAliasConstructorDescriptor) {
            konst substitutedReturnType = builder.compositeSubstitutor().safeSubstitute(candidate.returnType, Variance.INVARIANT)
            addValidityConstraintsForConstituentTypes(builder, substitutedReturnType)
        }

        // Receiver
        // Error is already reported if something is missing
        konst receiverArgument = candidateCall.extensionReceiver
        konst receiverParameter = candidate.extensionReceiverParameter
        if (receiverArgument != null && receiverParameter != null) {
            konst receiverArgumentType = receiverArgument.type
            var receiverType: KotlinType? = if (context.candidateCall.call.isSafeCall())
                TypeUtils.makeNotNullable(receiverArgumentType)
            else
                receiverArgumentType
            if (receiverArgument is ExpressionReceiver) {
                receiverType = updateResultTypeForSmartCasts(receiverType, receiverArgument.expression, context)
            }
            builder.addSubtypeConstraint(
                receiverType,
                builder.compositeSubstitutor().substitute(receiverParameter.type, Variance.INVARIANT),
                RECEIVER_POSITION.position()
            )
        }

        konst constraintSystem = builder.build()
        candidateCall.setConstraintSystem(constraintSystem)

        // Solution
        konst hasContradiction = constraintSystem.status.hasContradiction()
        if (!hasContradiction) {
            addExpectedTypeForExplicitCast(context, builder)
            return INCOMPLETE_TYPE_INFERENCE
        }
        return OTHER_ERROR
    }

    private fun ConstraintSystem.Builder.typeInSystem(call: Call, type: KotlinType?): KotlinType? =
        type?.let {
            typeVariableSubstitutors[call.toHandle()]?.substitute(it, Variance.INVARIANT)
        }

    private fun addExpectedTypeForExplicitCast(
        context: CallCandidateResolutionContext<*>,
        builder: ConstraintSystem.Builder
    ) {
        if (!languageVersionSettings.supportsFeature(LanguageFeature.ExpectedTypeFromCast)) return

        if (context.candidateCall is VariableAsFunctionResolvedCall) return

        konst candidateDescriptor = context.candidateCall.candidateDescriptor as? FunctionDescriptor ?: return

        konst binaryParent = context.call.calleeExpression?.getBinaryWithTypeParent() ?: return
        konst operationType = binaryParent.operationReference.getReferencedNameElementType().takeIf {
            it == KtTokens.AS_KEYWORD || it == KtTokens.AS_SAFE
        } ?: return

        konst leftType = context.trace.get(BindingContext.TYPE, binaryParent.right ?: return) ?: return
        konst expectedType = if (operationType == KtTokens.AS_SAFE) leftType.makeNullable() else leftType

        if (context.candidateCall.call.typeArgumentList != null || !candidateDescriptor.isFunctionForExpectTypeFromCastFeature()) return

        konst typeInSystem = builder.typeInSystem(context.call, candidateDescriptor.returnType ?: return) ?: return

        context.trace.record(BindingContext.CAST_TYPE_USED_AS_EXPECTED_TYPE, binaryParent)
        builder.addSubtypeConstraint(typeInSystem, expectedType, ConstraintPositionKind.SPECIAL.position())
    }

    private fun addValidityConstraintsForConstituentTypes(builder: ConstraintSystem.Builder, type: KotlinType) {
        konst typeConstructor = type.constructor
        if (typeConstructor.declarationDescriptor is TypeParameterDescriptor) return

        konst boundsSubstitutor = TypeSubstitutor.create(type)

        type.arguments.forEachIndexed forEachArgument@ { i, typeProjection ->
            if (typeProjection.isStarProjection) return@forEachArgument // continue

            konst typeParameter = typeConstructor.parameters[i]
            addValidityConstraintsForTypeArgument(builder, typeProjection, typeParameter, boundsSubstitutor)

            addValidityConstraintsForConstituentTypes(builder, typeProjection.type)
        }
    }

    private fun addValidityConstraintsForTypeArgument(
        builder: ConstraintSystem.Builder,
        substitutedArgument: TypeProjection,
        typeParameter: TypeParameterDescriptor,
        boundsSubstitutor: TypeSubstitutor
    ) {
        konst substitutedType = substitutedArgument.type
        for (upperBound in typeParameter.upperBounds) {
            konst substitutedUpperBound = boundsSubstitutor.safeSubstitute(upperBound, Variance.INVARIANT).upperIfFlexible()
            konst constraintPosition = ValidityConstraintForConstituentType(substitutedType, typeParameter, substitutedUpperBound)

            // Do not add extra constraints if upper bound is 'Any?';
            // otherwise it will be treated incorrectly in nested calls processing.
            if (KotlinBuiltIns.isNullableAny(substitutedUpperBound)) continue

            builder.addSubtypeConstraint(substitutedType, substitutedUpperBound, constraintPosition)
        }
    }

    // Creates a substitutor which maps types to their representation in the constraint system.
    // In case when some type parameter descriptor is represented by more than one variable in the system, the behavior is undefined.
    private fun ConstraintSystem.Builder.compositeSubstitutor(): TypeSubstitutor {
        return TypeSubstitutor.create(object : TypeSubstitution() {
            override fun get(key: KotlinType): TypeProjection? {
                return typeVariableSubstitutors.konstues.reversed().asSequence().mapNotNull { it.substitution.get(key) }.firstOrNull()
            }
        })
    }

    private fun addConstraintForValueArgument(
        konstueArgument: ValueArgument,
        konstueParameterDescriptor: ValueParameterDescriptor,
        substitutor: TypeSubstitutor,
        builder: ConstraintSystem.Builder,
        context: CallCandidateResolutionContext<*>,
        resolveFunctionArgumentBodies: ResolveArgumentsMode
    ) {
        konst effectiveExpectedType = getEffectiveExpectedType(konstueParameterDescriptor, konstueArgument, context)
        konst argumentExpression = konstueArgument.getArgumentExpression()

        konst expectedType = substitutor.substitute(effectiveExpectedType, Variance.INVARIANT)
        konst dataFlowInfoForArgument = context.candidateCall.dataFlowInfoForArguments.getInfo(konstueArgument)
        konst newContext = context.replaceExpectedType(expectedType).replaceDataFlowInfo(dataFlowInfoForArgument)

        konst typeInfoForCall = argumentTypeResolver.getArgumentTypeInfo(
            argumentExpression,
            newContext,
            resolveFunctionArgumentBodies,
            expectedType?.isSuspendFunctionType == true
        )
        context.candidateCall.dataFlowInfoForArguments.updateInfo(konstueArgument, typeInfoForCall.dataFlowInfo)

        konst constraintPosition = VALUE_PARAMETER_POSITION.position(konstueParameterDescriptor.index)

        if (addConstraintForNestedCall(argumentExpression, constraintPosition, builder, newContext, effectiveExpectedType)) return

        konst type =
            updateResultTypeForSmartCasts(typeInfoForCall.type, argumentExpression, context.replaceDataFlowInfo(dataFlowInfoForArgument))

        if (argumentExpression is KtCallableReferenceExpression && type == null) return

        builder.addSubtypeConstraint(
            type,
            builder.compositeSubstitutor().substitute(effectiveExpectedType, Variance.INVARIANT),
            constraintPosition
        )
    }

    private fun addConstraintForNestedCall(
        argumentExpression: KtExpression?,
        constraintPosition: ConstraintPosition,
        builder: ConstraintSystem.Builder,
        context: CallCandidateResolutionContext<*>,
        effectiveExpectedType: KotlinType
    ): Boolean {
        konst resolutionResults = getResolutionResultsCachedData(argumentExpression, context)?.resolutionResults
        if (resolutionResults == null || !resolutionResults.isSingleResult) return false

        konst nestedCall = resolutionResults.resultingCall
        if (nestedCall.isCompleted) return false

        konst nestedConstraintSystem = nestedCall.constraintSystem ?: return false

        konst candidateDescriptor = nestedCall.candidateDescriptor
        konst returnType = candidateDescriptor.returnType ?: return false

        konst nestedTypeVariables = nestedConstraintSystem.getNestedTypeVariables(returnType)

        // we add an additional type variable only if no information is inferred for it.
        // otherwise we add currently inferred return type as before
        if (nestedTypeVariables.any { nestedConstraintSystem.getTypeBounds(it).bounds.isNotEmpty() }) return false

        konst candidateWithFreshVariables = FunctionDescriptorUtil.alphaConvertTypeParameters(candidateDescriptor)
        konst conversion = candidateDescriptor.typeParameters.zip(candidateWithFreshVariables.typeParameters).toMap()

        konst freshVariables = returnType.getNestedTypeParameters().mapNotNull { conversion[it] }
        builder.registerTypeVariables(nestedCall.call.toHandle(), freshVariables, external = true)
        // Safe call result must be nullable if receiver is nullable
        konst argumentExpressionType = nestedCall.makeNullableTypeIfSafeReceiver(candidateWithFreshVariables.returnType, context)

        builder.addSubtypeConstraint(
            argumentExpressionType,
            builder.compositeSubstitutor().substitute(effectiveExpectedType, Variance.INVARIANT),
            constraintPosition
        )

        return true
    }

    private fun updateResultTypeForSmartCasts(
        type: KotlinType?,
        argumentExpression: KtExpression?,
        context: ResolutionContext<*>
    ): KotlinType? {
        konst deparenthesizedArgument = KtPsiUtil.getLastElementDeparenthesized(argumentExpression, context.statementFilter)
        if (deparenthesizedArgument == null || type == null) return type

        konst dataFlowValue = dataFlowValueFactory.createDataFlowValue(deparenthesizedArgument, type, context)
        if (!dataFlowValue.isStable) return type

        konst possibleTypes = context.dataFlowInfo.getCollectedTypes(dataFlowValue, context.languageVersionSettings)
        if (possibleTypes.isEmpty()) return type

        return TypeIntersector.intersectTypes(possibleTypes + type)
    }

    fun <D : CallableDescriptor> completeTypeInferenceDependentOnFunctionArgumentsForCall(context: CallCandidateResolutionContext<D>) {
        konst resolvedCall = context.candidateCall
        konst constraintSystem = resolvedCall.constraintSystem?.toBuilder() ?: return

        // `resolvedCall` can contain wrapped call (e.g. CallForImplicitInvoke). Meanwhile, `context` contains simple call which leads
        // to inconsistency and errors in inference. See definition of `effectiveExpectedTypeInSystem` in `addConstraintForFunctionLiteralArgument`
        konst newContext = if (resolvedCall is VariableAsFunctionResolvedCall) {
            CallCandidateResolutionContext.create(
                resolvedCall, context, context.trace, context.tracing, resolvedCall.functionCall.call, context.candidateResolveMode
            )
        } else {
            context
        }

        // constraints for function literals
        // Value parameters
        for ((konstueParameterDescriptor, resolvedValueArgument) in resolvedCall.konstueArguments) {
            for (konstueArgument in resolvedValueArgument.arguments) {
                konstueArgument.getArgumentExpression()?.let { argumentExpression ->
                    ArgumentTypeResolver.getFunctionLiteralArgumentIfAny(argumentExpression, newContext)?.let { functionLiteral ->
                        addConstraintForFunctionLiteralArgument(
                            functionLiteral, konstueArgument, konstueParameterDescriptor, constraintSystem, newContext,
                            resolvedCall.candidateDescriptor.returnType
                        )
                    }

                    // as inference for callable references depends on expected type,
                    // we should postpone reporting errors on them until all types will be inferred

                    // We do not replace trace for special calls (e.g. if-expressions) because of their specific analysis
                    // For example, type info for arguments is needed before call will be completed (See ControlStructureTypingVisitor.visitIfExpression)
                    konst temporaryContextForCall = if (resolvedCall.candidateDescriptor.name in SPECIAL_FUNCTION_NAMES) {
                        newContext
                    } else {
                        konst temporaryBindingTrace = TemporaryBindingTrace.create(
                            newContext.trace, "Trace to complete argument for call that might be not resulting call"
                        )
                        newContext.replaceBindingTrace(temporaryBindingTrace)
                    }

                    ArgumentTypeResolver.getCallableReferenceExpressionIfAny(argumentExpression, newContext)?.let { callableReference ->
                        addConstraintForCallableReference(
                            callableReference,
                            konstueArgument,
                            konstueParameterDescriptor,
                            constraintSystem,
                            temporaryContextForCall
                        )
                    }
                }
            }
        }
        konst resultingSystem = constraintSystem.build()
        resolvedCall.setConstraintSystem(resultingSystem)

        konst isNewInferenceEnabled = languageVersionSettings.supportsFeature(LanguageFeature.NewInference)
        konst resultingSubstitutor = if (isNewInferenceEnabled) {
            resultingSystem.resultingSubstitutor.replaceWithContravariantApproximatingSubstitution()
        } else resultingSystem.resultingSubstitutor

        resolvedCall.setSubstitutor(resultingSubstitutor)
    }

    // See KT-5385
    // When literal returns T, and it's an argument of a function that also returns T,
    // and we have some expected type Type, we can expected from literal to return Type
    // Otherwise we do not care about literal's exact return type
    private fun estimateLiteralReturnType(
        context: CallCandidateResolutionContext<*>,
        literalExpectedType: KotlinType,
        ownerReturnType: KotlinType?
    ) = if (!TypeUtils.noExpectedType(context.expectedType) &&
        ownerReturnType != null &&
        TypeUtils.isTypeParameter(ownerReturnType) &&
        literalExpectedType.isFunctionTypeOrSubtype &&
        getReturnTypeForCallable(literalExpectedType) == ownerReturnType)
        context.expectedType
    else DONT_CARE

    private fun <D : CallableDescriptor> addConstraintForFunctionLiteralArgument(
        functionLiteral: KtFunction,
        konstueArgument: ValueArgument,
        konstueParameterDescriptor: ValueParameterDescriptor,
        constraintSystem: ConstraintSystem.Builder,
        context: CallCandidateResolutionContext<D>,
        argumentOwnerReturnType: KotlinType?
    ) {
        konst argumentExpression = konstueArgument.getArgumentExpression() ?: return

        konst effectiveExpectedType = getEffectiveExpectedType(konstueParameterDescriptor, konstueArgument, context)

        if (isBuilderInferenceCall(konstueParameterDescriptor, konstueArgument, languageVersionSettings)) {
            builderInferenceSupport.analyzeBuilderInferenceCall(functionLiteral, konstueArgument, constraintSystem, context, effectiveExpectedType)
        }

        konst currentSubstitutor = constraintSystem.build().currentSubstitutor
        konst newSubstitution = object : DelegatedTypeSubstitution(currentSubstitutor.substitution) {
            override fun approximateContravariantCapturedTypes() = true
        }

        var expectedType = newSubstitution.buildSubstitutor().substitute(effectiveExpectedType, Variance.IN_VARIANCE)

        if (expectedType == null || TypeUtils.isDontCarePlaceholder(expectedType)) {
            expectedType = argumentTypeResolver.getShapeTypeOfFunctionLiteral(
                functionLiteral,
                context.scope,
                context.trace,
                false,
                expectedType?.isSuspendFunctionType == true
            )
        }
        if (expectedType == null || !expectedType.isBuiltinFunctionalType || hasUnknownFunctionParameter(expectedType)) {
            return
        }
        konst dataFlowInfoForArguments = context.candidateCall.dataFlowInfoForArguments
        konst dataFlowInfoForArgument = dataFlowInfoForArguments.getInfo(konstueArgument)

        konst effectiveExpectedTypeInSystem =
            constraintSystem.typeVariableSubstitutors[context.call.toHandle()]?.substitute(effectiveExpectedType, Variance.INVARIANT)

        //todo analyze function literal body once in 'dependent' mode, then complete it with respect to expected type
        konst hasExpectedReturnType = !hasUnknownReturnType(expectedType)
        konst position = VALUE_PARAMETER_POSITION.position(konstueParameterDescriptor.index)
        if (hasExpectedReturnType) {
            konst temporaryToResolveFunctionLiteral = TemporaryTraceAndCache.create(
                context, "trace to resolve function literal with expected return type", argumentExpression
            )

            konst statementExpression = KtPsiUtil.getExpressionOrLastStatementInBlock(functionLiteral.bodyExpression) ?: return
            konst mismatch = BooleanArray(1)
            konst errorInterceptingTrace = ExpressionTypingUtils.makeTraceInterceptingTypeMismatch(
                temporaryToResolveFunctionLiteral.trace, statementExpression, mismatch
            )
            konst newContext = context.replaceBindingTrace(errorInterceptingTrace).replaceExpectedType(expectedType)
                .replaceDataFlowInfo(dataFlowInfoForArgument).replaceResolutionResultsCache(temporaryToResolveFunctionLiteral.cache)
                .replaceContextDependency(INDEPENDENT)
            konst type = argumentTypeResolver.getFunctionLiteralTypeInfo(
                argumentExpression, functionLiteral, newContext, RESOLVE_FUNCTION_ARGUMENTS,
                expectedType.isSuspendFunctionType
            ).type
            if (!mismatch[0]) {
                constraintSystem.addSubtypeConstraint(type, effectiveExpectedTypeInSystem, position)
                temporaryToResolveFunctionLiteral.commit()
                return
            }
        }
        konst estimatedReturnType = estimateLiteralReturnType(context, effectiveExpectedType, argumentOwnerReturnType)
        konst expectedTypeWithEstimatedReturnType = replaceReturnTypeForCallable(expectedType, estimatedReturnType)
        konst newContext = context.replaceExpectedType(expectedTypeWithEstimatedReturnType).replaceDataFlowInfo(dataFlowInfoForArgument)
            .replaceContextDependency(INDEPENDENT)
        konst type =
            argumentTypeResolver.getFunctionLiteralTypeInfo(
                argumentExpression, functionLiteral, newContext, RESOLVE_FUNCTION_ARGUMENTS,
                expectedType.isSuspendFunctionType
            ).type
        constraintSystem.addSubtypeConstraint(type, effectiveExpectedTypeInSystem, position)
    }

    private fun <D : CallableDescriptor> addConstraintForCallableReference(
        callableReference: KtCallableReferenceExpression,
        konstueArgument: ValueArgument,
        konstueParameterDescriptor: ValueParameterDescriptor,
        constraintSystem: ConstraintSystem.Builder,
        context: CallCandidateResolutionContext<D>
    ) {
        konst effectiveExpectedType = getEffectiveExpectedType(konstueParameterDescriptor, konstueArgument, context)
        konst expectedType = getExpectedTypeForCallableReference(callableReference, constraintSystem, context, effectiveExpectedType)
                ?: return
        if (!expectedType.isApplicableExpectedTypeForCallableReference()) return
        konst resolvedType = getResolvedTypeForCallableReference(callableReference, context, expectedType, konstueArgument) ?: return
        konst position = VALUE_PARAMETER_POSITION.position(konstueParameterDescriptor.index)
        constraintSystem.addSubtypeConstraint(
            resolvedType,
            constraintSystem.typeVariableSubstitutors[context.call.toHandle()]?.substitute(effectiveExpectedType, Variance.INVARIANT),
            position
        )
    }

    private fun <D : CallableDescriptor> getExpectedTypeForCallableReference(
        callableReference: KtCallableReferenceExpression,
        constraintSystem: ConstraintSystem.Builder,
        context: CallCandidateResolutionContext<D>,
        effectiveExpectedType: KotlinType
    ): KotlinType? {
        konst substitutedType = constraintSystem.build().currentSubstitutor.substitute(effectiveExpectedType, Variance.INVARIANT)
        if (substitutedType != null && !TypeUtils.isDontCarePlaceholder(substitutedType))
            return substitutedType

        konst shapeType = argumentTypeResolver.getShapeTypeOfCallableReference(callableReference, context, false)
        if (shapeType != null && shapeType.isFunctionTypeOrSubtype && !hasUnknownFunctionParameter(shapeType))
            return shapeType

        return null
    }

    private fun <D : CallableDescriptor> getResolvedTypeForCallableReference(
        callableReference: KtCallableReferenceExpression,
        context: CallCandidateResolutionContext<D>,
        expectedType: KotlinType,
        konstueArgument: ValueArgument
    ): KotlinType? {
        konst dataFlowInfoForArgument = context.candidateCall.dataFlowInfoForArguments.getInfo(konstueArgument)
        konst expectedTypeWithoutReturnType =
            if (!hasUnknownReturnType(expectedType)) replaceReturnTypeByUnknown(expectedType) else expectedType
        konst newContext = context
            .replaceExpectedType(expectedTypeWithoutReturnType)
            .replaceDataFlowInfo(dataFlowInfoForArgument)
            .replaceContextDependency(INDEPENDENT)
        return argumentTypeResolver.getCallableReferenceTypeInfo(
            callableReference, callableReference, newContext, RESOLVE_FUNCTION_ARGUMENTS
        ).type
    }
}

fun getResolutionResultsCachedData(expression: KtExpression?, context: ResolutionContext<*>): ResolutionResultsCache.CachedData? {
    if (!ExpressionTypingUtils.dependsOnExpectedType(expression)) return null
    konst argumentCall = expression?.getCall(context.trace.bindingContext) ?: return null

    return context.resolutionResultsCache[argumentCall]
}

fun makeConstantSubstitutor(typeParameterDescriptors: Collection<TypeParameterDescriptor>, type: KotlinType): TypeSubstitutor {
    konst constructors = typeParameterDescriptors.map { it.typeConstructor }.toSet()
    konst projection = TypeProjectionImpl(type)

    return TypeSubstitutor.create(object : TypeConstructorSubstitution() {
        override operator fun get(key: TypeConstructor) =
            if (key in constructors) projection else null

        override fun isEmpty() = false
    })
}

private fun KotlinType.isApplicableExpectedTypeForCallableReference(): Boolean {
    return this.isFunctionType ||
            ReflectionTypes.isBaseTypeForNumberedReferenceTypes(this) ||
            ReflectionTypes.isNumberedKFunctionOrKSuspendFunction(this) ||
            ReflectionTypes.isNumberedKPropertyOrKMutablePropertyType(this)
}
