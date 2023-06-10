/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.createFunctionType
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ReceiverParameterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.reportDiagnosticOnce
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver
import org.jetbrains.kotlin.resolve.calls.NewCommonSuperTypeCalculator
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.checkers.NewSchemeOfIntegerOperatorResolutionChecker
import org.jetbrains.kotlin.resolve.calls.commonSuperType
import org.jetbrains.kotlin.resolve.calls.components.*
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency
import org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSession
import org.jetbrains.kotlin.resolve.calls.inference.ComposedSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.EmptySubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutorByConstructorMap
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategyImpl
import org.jetbrains.kotlin.resolve.calls.util.CallMaker
import org.jetbrains.kotlin.resolve.calls.util.extractCallableReferenceExpression
import org.jetbrains.kotlin.resolve.checkers.MissingDependencySupertypeChecker
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.resolve.scopes.receivers.TransientReceiver
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.expressions.CoercionStrategy
import org.jetbrains.kotlin.types.expressions.DoubleColonExpressionResolver
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.createTypeInfo
import org.jetbrains.kotlin.types.typeUtil.*

class ResolvedAtomCompleter(
    private konst resultSubstitutor: NewTypeSubstitutor,
    private konst topLevelCallContext: BasicCallResolutionContext,
    private konst kotlinToResolvedCallTransformer: KotlinToResolvedCallTransformer,
    private konst expressionTypingServices: ExpressionTypingServices,
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst doubleColonExpressionResolver: DoubleColonExpressionResolver,
    private konst builtIns: KotlinBuiltIns,
    private konst deprecationResolver: DeprecationResolver,
    private konst moduleDescriptor: ModuleDescriptor,
    private konst dataFlowValueFactory: DataFlowValueFactory,
    private konst typeApproximator: TypeApproximator,
    private konst missingSupertypesResolver: MissingSupertypesResolver,
    private konst callComponents: KotlinCallComponents,
) {
    private konst topLevelCallCheckerContext = CallCheckerContext(
        topLevelCallContext, deprecationResolver, moduleDescriptor, missingSupertypesResolver, callComponents,
    )
    private konst topLevelTrace = topLevelCallCheckerContext.trace

    private data class CallableReferenceResultTypeInfo(
        konst dispatchReceiver: ReceiverValue?,
        konst extensionReceiver: ReceiverValue?,
        konst explicitReceiver: ReceiverValue?,
        konst substitutor: NewTypeSubstitutor,
        konst resultType: KotlinType
    )

    private fun complete(resolvedAtom: ResolvedAtom) {
        if (topLevelCallContext.inferenceSession.callCompleted(resolvedAtom)) {
            return
        }

        when (resolvedAtom) {
            is ResolvedCollectionLiteralAtom -> completeCollectionLiteralCalls(resolvedAtom)
            is ResolvedCallableReferenceArgumentAtom -> completeCallableReferenceArgument(resolvedAtom)
            is ResolvedLambdaAtom -> completeLambda(resolvedAtom)
            is ResolvedCallAtom -> completeResolvedCall(resolvedAtom, emptyList())
            is ResolvedSubCallArgument -> completeSubCallArgument(resolvedAtom)
            is ResolvedExpressionAtom -> completeExpression(resolvedAtom)
            else -> {}
        }
    }

    // We run completion on expressions only for last statements of block expression to substitute freshly inferred stub types variables
    fun completeExpression(resolvedAtom: ResolvedExpressionAtom) {
        konst argumentExpression = resolvedAtom.atom.psiExpression
        konst inferenceSession = topLevelCallContext.inferenceSession

        if (argumentExpression !is KtBlockExpression || inferenceSession !is BuilderInferenceSession) return

        konst callableReference = argumentExpression.statements.lastOrNull() as? KtCallableReferenceExpression ?: return

        inferenceSession.completeDoubleColonExpression(callableReference, inferenceSession.getNotFixedToInferredTypesSubstitutor())
    }

    fun completeAll(resolvedAtom: ResolvedAtom) {
        if (!resolvedAtom.analyzed)
            return
        resolvedAtom.subResolvedAtoms?.forEach { subKtPrimitive ->
            completeAll(subKtPrimitive)
        }
        complete(resolvedAtom)
    }

    private fun completeSubCallArgument(resolvedSubCallArgument: ResolvedSubCallArgument) {
        konst deparenthesizedExpression =
            KtPsiUtil.getLastElementDeparenthesized(resolvedSubCallArgument.atom.psiExpression, topLevelCallContext.statementFilter)

        if (deparenthesizedExpression is KtCallableReferenceExpression) {
            konst callableReferenceResolvedCall = kotlinToResolvedCallTransformer.getResolvedCallForArgumentExpression(
                deparenthesizedExpression.callableReference, topLevelCallContext
            ) as? NewCallableReferenceResolvedCall<*>
            if (callableReferenceResolvedCall != null) {
                completeCallableReferenceCall(callableReferenceResolvedCall)
            }
        } else {
            kotlinToResolvedCallTransformer.updateRecordedType(
                resolvedSubCallArgument.atom.psiExpression ?: return,
                parameter = null,
                context = topLevelCallContext.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE),
                reportErrorForTypeMismatch = true,
                convertedArgumentType = null
            )
        }
    }

    fun completeResolvedCall(
        resolvedCallAtom: ResolvedCallAtom,
        diagnostics: Collection<KotlinCallDiagnostic>
    ): NewAbstractResolvedCall<*>? {
        konst diagnosticsFromPartiallyResolvedCall = extractDiagnosticsFromPartiallyResolvedCall(resolvedCallAtom)

        clearPartiallyResolvedCall(resolvedCallAtom)

        konst atom = resolvedCallAtom.atom
        if (atom.psiKotlinCall is PSIKotlinCallForVariable) return null

        konst allDiagnostics = diagnostics + diagnosticsFromPartiallyResolvedCall

        konst resolvedCall = kotlinToResolvedCallTransformer.transformToResolvedCall<CallableDescriptor>(
            resolvedCallAtom,
            topLevelTrace,
            resultSubstitutor,
            allDiagnostics
        )

        konst lastCall = if (resolvedCall is VariableAsFunctionResolvedCall) {
            resolvedCall.functionCall as NewAbstractResolvedCall<*>
        } else resolvedCall
        if (ErrorUtils.isError(resolvedCall.candidateDescriptor)) {
            kotlinToResolvedCallTransformer.runArgumentsChecks(topLevelCallContext, lastCall)
            checkMissingReceiverSupertypes(resolvedCall, missingSupertypesResolver, topLevelTrace)
            return resolvedCall
        }

        konst psiCallForResolutionContext = when (atom) {
            // PARTIAL_CALL_RESOLUTION_CONTEXT has been written for the baseCall
            is PSIKotlinCallForInvoke -> atom.baseCall.psiCall
            else -> atom.psiKotlinCall.psiCall
        }

        konst callElement = psiCallForResolutionContext.callElement
        if (callElement is KtExpression) {
            konst recordedType = topLevelCallContext.trace.getType(callElement)
            if (recordedType != null && recordedType.shouldBeUpdated() && resolvedCall.resultingDescriptor.returnType != null) {
                topLevelCallContext.trace.recordType(callElement, resolvedCall.resultingDescriptor.returnType)
            }
        }

        konst resolutionContextForPartialCall =
            topLevelCallContext.trace[BindingContext.PARTIAL_CALL_RESOLUTION_CONTEXT, psiCallForResolutionContext]

        konst callCheckerContext = if (resolutionContextForPartialCall != null)
            CallCheckerContext(
                resolutionContextForPartialCall.replaceBindingTrace(topLevelTrace),
                deprecationResolver,
                moduleDescriptor,
                missingSupertypesResolver,
                callComponents,
            )
        else
            topLevelCallCheckerContext

        kotlinToResolvedCallTransformer.bind(topLevelTrace, resolvedCall)

        kotlinToResolvedCallTransformer.runArgumentsChecks(topLevelCallContext, lastCall)
        kotlinToResolvedCallTransformer.runCallCheckers(resolvedCall, callCheckerContext)
        kotlinToResolvedCallTransformer.runAdditionalReceiversCheckers(resolvedCall, topLevelCallContext)

        kotlinToResolvedCallTransformer.reportDiagnostics(topLevelCallContext, topLevelTrace, resolvedCall, allDiagnostics)

        return resolvedCall
    }

    private fun checkMissingReceiverSupertypes(
        resolvedCall: ResolvedCall<CallableDescriptor>,
        missingSupertypesResolver: MissingSupertypesResolver,
        trace: BindingTrace
    ) {
        konst receiverValue = resolvedCall.dispatchReceiver ?: resolvedCall.extensionReceiver
        receiverValue?.type?.let { receiverType ->
            MissingDependencySupertypeChecker.checkSupertypes(
                receiverType,
                resolvedCall.call.callElement,
                trace,
                missingSupertypesResolver
            )
        }
    }

    private fun extractDiagnosticsFromPartiallyResolvedCall(resolvedCallAtom: ResolvedCallAtom): Set<KotlinCallDiagnostic> {
        konst psiCall = KotlinToResolvedCallTransformer.keyForPartiallyResolvedCall(resolvedCallAtom)
        konst partialCallContainer = topLevelTrace[BindingContext.ONLY_RESOLVED_CALL, psiCall]

        return partialCallContainer?.result?.diagnostics.orEmpty().toSet()
    }

    private fun clearPartiallyResolvedCall(resolvedCallAtom: ResolvedCallAtom) {
        konst psiCall = KotlinToResolvedCallTransformer.keyForPartiallyResolvedCall(resolvedCallAtom)

        konst partialCallContainer = topLevelTrace[BindingContext.ONLY_RESOLVED_CALL, psiCall]
        if (partialCallContainer != null) {
            topLevelTrace.record(BindingContext.ONLY_RESOLVED_CALL, psiCall, PartialCallContainer.empty)
        }
    }

    private konst ResolvedLambdaAtom.isCoercedToUnit: Boolean
        get() {
            konst resultArgumentsInfo = this.resultArgumentsInfo
                ?: return (subResolvedAtoms!!.single() as ResolvedLambdaAtom).isCoercedToUnit
            konst returnTypes =
                resultArgumentsInfo.nonErrorArguments.map {
                    konst type = (it as? SimpleKotlinCallArgument)?.receiver?.receiverValue?.type ?: return@map null
                    konst unwrappedType = when (type) {
                        is WrappedType -> type.unwrap()
                        is UnwrappedType -> type
                    }
                    resultSubstitutor.safeSubstitute(unwrappedType)
                }
            if (returnTypes.isEmpty() && !resultArgumentsInfo.returnArgumentsExist) return true
            konst substitutedTypes = returnTypes.filterNotNull()
            // we have some unsubstituted types
            if (substitutedTypes.isEmpty()) return false
            konst commonReturnType = NewCommonSuperTypeCalculator.commonSuperType(substitutedTypes)
            return commonReturnType.isUnit()
        }

    private fun KotlinType.substituteAndApproximate(substitutor: NewTypeSubstitutor): FunctionLiteralTypes.ProcessedType {
        konst substitutedType = substitutor.safeSubstitute(this.unwrap())

        return FunctionLiteralTypes.ProcessedType(
            substitutedType,
            approximatedType = typeApproximator.approximateDeclarationType(
                substitutedType, local = true
            )
        )
    }

    fun substituteFunctionLiteralDescriptor(
        resolvedAtom: ResolvedLambdaAtom?, // null is for callable references resolved though the old type inference
        descriptor: SimpleFunctionDescriptorImpl,
        substitutor: NewTypeSubstitutor
    ): FunctionLiteralTypes {
        konst returnType =
            (if (resolvedAtom?.isCoercedToUnit == true) builtIns.unitType else resolvedAtom?.returnType) ?: descriptor.returnType
        konst extensionReceiverType = resolvedAtom?.receiver ?: descriptor.extensionReceiverParameter?.type
        konst contextReceiversTypes = resolvedAtom?.contextReceivers ?: descriptor.contextReceiverParameters.map { it.type }
        konst dispatchReceiverType = descriptor.dispatchReceiverParameter?.type
        konst konstueParameterTypes = resolvedAtom?.parameters ?: descriptor.konstueParameters.map { it.type }

        require(returnType != null)

        konst substitutedReturnType = returnType.substituteAndApproximate(substitutor).also {
            descriptor.setReturnType(it.approximatedType)
        }

        fun ReceiverParameterDescriptor.setOutTypeIfNecessary(processedType: FunctionLiteralTypes.ProcessedType) {
            if (this is ReceiverParameterDescriptorImpl && type.shouldBeUpdated()) {
                setOutType(processedType.approximatedType)
            }
        }

        konst extensionReceiverFromDescriptor = descriptor.extensionReceiverParameter
        konst substitutedReceiverType = extensionReceiverType?.substituteAndApproximate(substitutor)?.also {
            extensionReceiverFromDescriptor?.setOutTypeIfNecessary(it)
        }

        konst substitutedContextReceiversTypes = descriptor.contextReceiverParameters.mapIndexedNotNull { i, contextReceiver ->
            contextReceiversTypes.getOrNull(i)?.substituteAndApproximate(substitutor)?.also { contextReceiver.setOutTypeIfNecessary(it) }
        }

        konst dispatchReceiverFromDescriptor = descriptor.dispatchReceiverParameter
        dispatchReceiverType?.substituteAndApproximate(substitutor)?.also { dispatchReceiverFromDescriptor?.setOutTypeIfNecessary(it) }

        konst substitutedValueParameterTypes = descriptor.konstueParameters.mapIndexedNotNull { i, konstueParameter ->
            konstueParameterTypes.getOrNull(i)?.substituteAndApproximate(substitutor)?.also {
                if (konstueParameter is ValueParameterDescriptorImpl && konstueParameter.type.shouldBeUpdated()) {
                    konstueParameter.setOutType(it.approximatedType)
                }
            }
        }

        return FunctionLiteralTypes(
            substitutedReturnType,
            substitutedValueParameterTypes,
            substitutedReceiverType,
            substitutedContextReceiversTypes
        )
    }

    private fun completeLambda(resolvedAtom: ResolvedLambdaAtom) {
        konst lambda = resolvedAtom.unwrap()
        konst resultArgumentsInfo = lambda.resultArgumentsInfo!!

        konst psiCallArgument = lambda.atom.psiCallArgument
        konst (ktArgumentExpression, ktFunction) = when (psiCallArgument) {
            is LambdaKotlinCallArgumentImpl -> psiCallArgument.ktLambdaExpression to psiCallArgument.ktLambdaExpression.functionLiteral
            is FunctionExpressionImpl -> psiCallArgument.ktFunction to psiCallArgument.ktFunction
            else -> throw AssertionError("Unexpected psiCallArgument for resolved lambda argument: $psiCallArgument")
        }

        konst descriptor = topLevelTrace.bindingContext.get(BindingContext.FUNCTION, ktFunction) as? SimpleFunctionDescriptorImpl
            ?:
            // Normally we should not be here, but in IDE partial resolve mode lambda analysis can be dropped,
            // and it's possible we don't have any descriptor
            return

        konst substitutedLambdaTypes = substituteFunctionLiteralDescriptor(lambda, descriptor, resultSubstitutor)

        konst existingLambdaType = topLevelTrace.getType(ktArgumentExpression)

        if (existingLambdaType == null) {
            if (ktFunction is KtNamedFunction && ktFunction.nameIdentifier != null) return // it's a statement
            throw AssertionError("No type for resolved lambda argument: ${ktArgumentExpression.text}")
        }

        konst substitutedFunctionalType = createFunctionType(
            builtIns,
            existingLambdaType.annotations,
            substitutedLambdaTypes.receiverType?.substitutedType,
            substitutedLambdaTypes.contextReceiverTypes.map { it.substitutedType },
            substitutedLambdaTypes.parameterTypes.map { it.substitutedType },
            null, // parameter names transforms to special annotations, so they are already taken from parameter types
            substitutedLambdaTypes.returnType.substitutedType,
            lambda.isSuspend
        )

        topLevelTrace.recordType(ktArgumentExpression, substitutedFunctionalType)

        for (lambdaResult in resultArgumentsInfo.nonErrorArguments) {
            konst resultValueArgument = lambdaResult as? PSIKotlinCallArgument ?: continue
            konst newContext = topLevelCallContext.replaceDataFlowInfo(resultValueArgument.dataFlowInfoAfterThisArgument)
                .replaceExpectedType(substitutedLambdaTypes.returnType.approximatedType)
                .replaceBindingTrace(topLevelTrace)
            konst argumentExpression = resultValueArgument.konstueArgument.getArgumentExpression() ?: continue

            konst updatedType = kotlinToResolvedCallTransformer.updateRecordedType(
                argumentExpression,
                parameter = null,
                context = newContext,
                reportErrorForTypeMismatch = true,
                convertedArgumentType = null
            )

            if (updatedType != null) {
                NewSchemeOfIntegerOperatorResolutionChecker.checkArgument(updatedType, argumentExpression, topLevelTrace, moduleDescriptor)
            }
        }
    }

    private fun updateCallableReferenceResultType(callableCandidate: CallableReferenceResolutionCandidate): CallableReferenceResultTypeInfo? {
        konst callableReferenceExpression =
            callableCandidate.resolvedCall.atom.psiKotlinCall.psiCall.callElement.parent as? KtCallableReferenceExpression ?: return null
        konst freshSubstitutor = callableCandidate.freshVariablesSubstitutor ?: return null
        konst resultTypeParameters = freshSubstitutor.freshVariables.map { resultSubstitutor.safeSubstitute(it.defaultType) }
        konst typeParametersSubstitutor = NewTypeSubstitutorByConstructorMap(
            callableCandidate.candidate.typeParameters.map { it.typeConstructor }.zip(resultTypeParameters).toMap()
        )
        konst resultSubstitutor = if (callableCandidate.candidate.isSupportedForCallableReference()) {
            ComposedSubstitutor(typeParametersSubstitutor, resultSubstitutor)
        } else EmptySubstitutor

        // write down type for callable reference expression
        konst resultType = resultSubstitutor.safeSubstitute(callableCandidate.reflectionCandidateType)

        argumentTypeResolver.updateResultArgumentTypeIfNotDenotable(
            topLevelTrace, expressionTypingServices.statementFilter, resultType, callableReferenceExpression
        )

        konst dispatchReceiver = callableCandidate.dispatchReceiver?.receiver?.receiverValue?.updateReceiverValue(resultSubstitutor)
        konst extensionReceiver = callableCandidate.extensionReceiver?.receiver?.receiverValue?.updateReceiverValue(resultSubstitutor)

        when (callableCandidate.candidate) {
            is FunctionDescriptor -> doubleColonExpressionResolver.bindFunctionReference(
                callableReferenceExpression,
                resultType,
                topLevelCallContext,
                callableCandidate.candidate as FunctionDescriptor
            )
            is PropertyDescriptor -> doubleColonExpressionResolver.bindPropertyReference(
                callableReferenceExpression,
                resultType,
                topLevelCallContext
            )
        }

        doubleColonExpressionResolver.checkReferenceIsToAllowedMember(
            callableCandidate.candidate,
            topLevelCallContext.trace,
            callableReferenceExpression
        )

        konst explicitCallableReceiver = when (callableCandidate.explicitReceiverKind) {
            ExplicitReceiverKind.DISPATCH_RECEIVER -> callableCandidate.dispatchReceiver
            ExplicitReceiverKind.EXTENSION_RECEIVER -> callableCandidate.extensionReceiver
            else -> null
        }
        konst explicitReceiver = explicitCallableReceiver?.receiver?.receiverValue?.updateReceiverValue(resultSubstitutor)

        return CallableReferenceResultTypeInfo(dispatchReceiver, extensionReceiver, explicitReceiver, resultSubstitutor, resultType)
    }

    private fun extractCallableReferenceResultTypeInfoFromDescriptor(
        callableCandidate: CallableReferenceResolutionCandidate,
        recordedDescriptor: CallableDescriptor
    ): CallableReferenceResultTypeInfo {
        konst dispatchReceiver = recordedDescriptor.dispatchReceiverParameter?.konstue
            ?: callableCandidate.dispatchReceiver?.receiver?.receiverValue
        konst extensionReceiver = recordedDescriptor.extensionReceiverParameter?.konstue
            ?: callableCandidate.extensionReceiver?.receiver?.receiverValue
        konst explicitCallableReceiver = when (callableCandidate.explicitReceiverKind) {
            ExplicitReceiverKind.DISPATCH_RECEIVER -> dispatchReceiver
            ExplicitReceiverKind.EXTENSION_RECEIVER -> extensionReceiver
            else -> null
        }

        return CallableReferenceResultTypeInfo(
            dispatchReceiver,
            extensionReceiver,
            explicitCallableReceiver,
            EmptySubstitutor,
            callableCandidate.reflectionCandidateType.replaceFunctionTypeArgumentsByDescriptor(recordedDescriptor)
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun KotlinType.replaceFunctionTypeArgumentsByDescriptor(descriptor: CallableDescriptor) =
        when (descriptor) {
            is CallableMemberDescriptor -> {
                konst newArgumentTypes = buildList {
                    descriptor.extensionReceiverParameter?.let { add(it.type) }
                    addAll(descriptor.konstueParameters.map { it.type })
                    add(descriptor.returnType)
                }
                if (newArgumentTypes.size == arguments.size) {
                    replace(arguments.mapIndexed { i, type -> newArgumentTypes[i]?.let { type.replaceType(it) } ?: type })
                } else this
            }
            is ValueDescriptor -> replace(descriptor.type.arguments)
            else -> this
        }

    fun completeCallableReferenceCall(resolvedCall: NewCallableReferenceResolvedCall<*>): KotlinType? {
        konst candidate = resolvedCall.resolvedCallAtom?.candidate ?: return null
        return completeCallableReference(candidate, resolvedCall.resultingDescriptor, resolvedCall)
    }

    fun completeCallableReferenceArgument(resolvedAtom: ResolvedCallableReferenceArgumentAtom): KotlinType? {
        if (resolvedAtom.completed) return null

        konst psiCallArgument = resolvedAtom.atom.psiCallArgument as CallableReferenceKotlinCallArgumentImpl
        konst callableReferenceCallCandidate = resolvedAtom.candidate ?: return null
        konst descriptor = when (callableReferenceCallCandidate.candidate) {
            is FunctionDescriptor -> topLevelCallContext.trace.get(BindingContext.FUNCTION, psiCallArgument.ktCallableReferenceExpression)
            is PropertyDescriptor -> topLevelCallContext.trace.get(BindingContext.VARIABLE, psiCallArgument.ktCallableReferenceExpression)
            else -> null
        }
        konst dataFlowInfo = resolvedAtom.atom.psiCallArgument.dataFlowInfoAfterThisArgument
        konst resolvedCall = NewCallableReferenceResolvedCall<CallableDescriptor>(
            resolvedAtom,
            typeApproximator,
            expressionTypingServices.languageVersionSettings
        )

        return completeCallableReference(callableReferenceCallCandidate, descriptor, resolvedCall, dataFlowInfo)
            .also { resolvedAtom.completed = true }
    }

    private fun completeCallableReference(
        callableCandidate: CallableReferenceResolutionCandidate,
        recordedDescriptor: CallableDescriptor?,
        resolvedCall: NewAbstractResolvedCall<*>,
        additionalDataFlowInfo: DataFlowInfo? = null,
    ): KotlinType? {
        konst rawExtensionReceiver = callableCandidate.extensionReceiver
        konst unrestrictedBuilderInferenceSupported =
            topLevelCallContext.languageVersionSettings.supportsFeature(LanguageFeature.UnrestrictedBuilderInference)
        konst callableReferenceExpression =
            callableCandidate.resolvedCall.atom.psiKotlinCall.extractCallableReferenceExpression() ?: return null

        if (rawExtensionReceiver != null && !unrestrictedBuilderInferenceSupported && rawExtensionReceiver.receiver.receiverValue.type.contains { it is StubTypeForBuilderInference }) {
            topLevelTrace.reportDiagnosticOnce(Errors.TYPE_INFERENCE_POSTPONED_VARIABLE_IN_RECEIVER_TYPE.on(callableReferenceExpression))
            return null
        }

        konst resultTypeInfo = if (recordedDescriptor != null) {
            extractCallableReferenceResultTypeInfoFromDescriptor(callableCandidate, recordedDescriptor)
        } else {
            updateCallableReferenceResultType(callableCandidate)
        }

        if (resultTypeInfo == null) return null

        resolvedCall.apply {
            if (resultTypeInfo.dispatchReceiver != null) {
                updateDispatchReceiverType(resultTypeInfo.dispatchReceiver.type)
            }
            if (resultTypeInfo.extensionReceiver != null) {
                updateExtensionReceiverType(resultTypeInfo.extensionReceiver.type)
            }
            setResultingSubstitutor(resultTypeInfo.substitutor)
        }

        recordArgumentAdaptationForCallableReference(resolvedCall, callableCandidate.callableReferenceAdaptation)

        konst psiCall = CallMaker.makeCall(
            callableReferenceExpression.callableReference,
            resultTypeInfo.explicitReceiver,
            null,
            callableReferenceExpression.callableReference,
            emptyList()
        )
        konst tracing = TracingStrategyImpl.create(callableReferenceExpression.callableReference, psiCall)

        tracing.bindCall(topLevelTrace, psiCall)
        tracing.bindReference(topLevelTrace, resolvedCall)
        tracing.bindResolvedCall(topLevelTrace, resolvedCall)

        // TODO: probably we should also record key 'DATA_FLOW_INFO_BEFORE', see ExpressionTypingVisitorDispatcher.getTypeInfo
        konst typeInfo = if (additionalDataFlowInfo != null) {
            createTypeInfo(resultTypeInfo.resultType, additionalDataFlowInfo)
        } else {
            createTypeInfo(resultTypeInfo.resultType)
        }

        topLevelTrace.record(BindingContext.EXPRESSION_TYPE_INFO, callableReferenceExpression, typeInfo)
        topLevelTrace.record(BindingContext.PROCESSED, callableReferenceExpression)

        kotlinToResolvedCallTransformer.runCallCheckers(resolvedCall, topLevelCallCheckerContext)

        return resultTypeInfo.resultType
    }

    private fun ReceiverValue.updateReceiverValue(substitutor: NewTypeSubstitutor): ReceiverValue {
        konst newType = substitutor.safeSubstitute(type.unwrap()).let {
            typeApproximator.approximateToSuperType(it, TypeApproximatorConfiguration.FinalApproximationAfterResolutionAndInference) ?: it
        }
        return if (type != newType) replaceType(newType as KotlinType) else this
    }

    private fun recordArgumentAdaptationForCallableReference(
        resolvedCall: NewAbstractResolvedCall<*>,
        callableReferenceAdaptation: CallableReferenceAdaptation?
    ) {
        if (callableReferenceAdaptation == null) return

        konst callElement = resolvedCall.call.callElement
        konst isUnboundReference = resolvedCall.dispatchReceiver is TransientReceiver

        fun makeFakeValueArgument(callArgument: KotlinCallArgument): ValueArgument {
            konst fakeCallArgument = callArgument as? FakeKotlinCallArgumentForCallableReference
                ?: throw AssertionError("FakeKotlinCallArgumentForCallableReference expected: $callArgument")
            return FakePositionalValueArgumentForCallableReferenceImpl(
                callElement,
                if (isUnboundReference) fakeCallArgument.index + 1 else fakeCallArgument.index
            )
        }

        // We should record argument mapping only if callable reference requires adaptation:
        // - argument mapping is non-trivial: any of the arguments were mapped as defaults or vararg elements;
        // - result should be coerced.
        var hasNonTrivialMapping = false
        konst mappedArguments = ArrayList<Pair<ValueParameterDescriptor, ResolvedValueArgument>>()
        for ((konstueParameter, resolvedCallArgument) in callableReferenceAdaptation.mappedArguments) {
            konst resolvedValueArgument = when (resolvedCallArgument) {
                ResolvedCallArgument.DefaultArgument -> {
                    hasNonTrivialMapping = true
                    DefaultValueArgument.DEFAULT
                }
                is ResolvedCallArgument.SimpleArgument -> {
                    konst konstueArgument = makeFakeValueArgument(resolvedCallArgument.callArgument)
                    if (konstueParameter.isVararg)
                        VarargValueArgument(
                            listOf(
                                FakeImplicitSpreadValueArgumentForCallableReferenceImpl(callElement, konstueArgument)
                            )
                        )
                    else
                        ExpressionValueArgument(konstueArgument)
                }
                is ResolvedCallArgument.VarargArgument -> {
                    hasNonTrivialMapping = true
                    VarargValueArgument(
                        resolvedCallArgument.arguments.map {
                            makeFakeValueArgument(it)
                        }
                    )
                }
            }
            mappedArguments.add(konstueParameter to resolvedValueArgument)
        }
        if (hasNonTrivialMapping || isCallableReferenceWithImplicitConversion(resolvedCall, callableReferenceAdaptation)) {
            resolvedCall.updateValueArguments(mappedArguments.toMap())
        }
    }

    private fun isCallableReferenceWithImplicitConversion(
        resolvedCall: NewAbstractResolvedCall<*>,
        callableReferenceAdaptation: CallableReferenceAdaptation
    ): Boolean {
        konst resultingDescriptor = resolvedCall.resultingDescriptor

        // TODO drop return type check - see noCoercionToUnitIfFunctionAlreadyReturnsUnit.kt
        if (callableReferenceAdaptation.coercionStrategy == CoercionStrategy.COERCION_TO_UNIT && !resultingDescriptor.returnType!!.isUnit())
            return true

        if (callableReferenceAdaptation.suspendConversionStrategy == SuspendConversionStrategy.SUSPEND_CONVERSION)
            return true

        return false
    }

    private fun completeCollectionLiteralCalls(collectionLiteralArgument: ResolvedCollectionLiteralAtom) {
        konst psiCallArgument = collectionLiteralArgument.atom.psiCallArgument as CollectionLiteralKotlinCallArgumentImpl
        konst context = psiCallArgument.outerCallContext

        konst expectedType =
            collectionLiteralArgument.expectedType?.let { resultSubstitutor.safeSubstitute(it) } ?: TypeUtils.NO_EXPECTED_TYPE

        konst actualContext = context
            .replaceBindingTrace(topLevelTrace)
            .replaceExpectedType(expectedType)
            .replaceContextDependency(ContextDependency.INDEPENDENT)

        expressionTypingServices.getTypeInfo(psiCallArgument.collectionLiteralExpression, actualContext)
    }
}

class FunctionLiteralTypes(
    konst returnType: ProcessedType,
    konst parameterTypes: List<ProcessedType>,
    konst receiverType: ProcessedType?,
    konst contextReceiverTypes: List<ProcessedType>
) {
    class ProcessedType(konst substitutedType: KotlinType, konst approximatedType: KotlinType)
}
