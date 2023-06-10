/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.EffectSystem
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.extensions.internal.CandidateInterceptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.BindingContext.NEW_INFERENCE_CATCH_EXCEPTION_PARAMETER
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver
import org.jetbrains.kotlin.resolve.calls.CallTransformer
import org.jetbrains.kotlin.resolve.calls.KotlinCallResolver
import org.jetbrains.kotlin.resolve.calls.SPECIAL_FUNCTION_NAMES
import org.jetbrains.kotlin.resolve.calls.components.InferenceSession
import org.jetbrains.kotlin.resolve.calls.components.KotlinResolutionCallbacks
import org.jetbrains.kotlin.resolve.calls.components.PostponedArgumentsAnalyzer
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.CallPosition
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency
import org.jetbrains.kotlin.resolve.calls.inference.buildResultingSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.KotlinConstraintSystemCompleter
import org.jetbrains.kotlin.resolve.calls.inference.components.ResultTypeResolver
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.results.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.tasks.DynamicCallableDescriptors
import org.jetbrains.kotlin.resolve.calls.tasks.OldResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategy
import org.jetbrains.kotlin.resolve.calls.util.*
import org.jetbrains.kotlin.resolve.checkers.PassingProgressionAsCollectionCallChecker
import org.jetbrains.kotlin.resolve.checkers.ResolutionWithStubTypesChecker
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.isUnderscoreNamed
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.*
import org.jetbrains.kotlin.resolve.scopes.receivers.*
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.expressions.*
import org.jetbrains.kotlin.types.model.TypeSystemInferenceExtensionContext
import org.jetbrains.kotlin.utils.KotlinExceptionWithAttachments
import org.jetbrains.kotlin.utils.addToStdlib.compactIfPossible
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

class PSICallResolver(
    private konst typeResolver: TypeResolver,
    private konst expressionTypingServices: ExpressionTypingServices,
    private konst doubleColonExpressionResolver: DoubleColonExpressionResolver,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst dynamicCallableDescriptors: DynamicCallableDescriptors,
    private konst syntheticScopes: SyntheticScopes,
    private konst callComponents: KotlinCallComponents,
    private konst kotlinToResolvedCallTransformer: KotlinToResolvedCallTransformer,
    private konst kotlinCallResolver: KotlinCallResolver,
    private konst typeApproximator: TypeApproximator,
    private konst implicitsResolutionFilter: ImplicitsExtensionsResolutionFilter,
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst effectSystem: EffectSystem,
    private konst constantExpressionEkonstuator: ConstantExpressionEkonstuator,
    private konst dataFlowValueFactory: DataFlowValueFactory,
    private konst postponedArgumentsAnalyzer: PostponedArgumentsAnalyzer,
    private konst kotlinConstraintSystemCompleter: KotlinConstraintSystemCompleter,
    private konst deprecationResolver: DeprecationResolver,
    private konst moduleDescriptor: ModuleDescriptor,
    private konst candidateInterceptor: CandidateInterceptor,
    private konst missingSupertypesResolver: MissingSupertypesResolver,
    private konst resultTypeResolver: ResultTypeResolver,
) {
    private konst callCheckersWithAdditionalResolve = listOf(
        PassingProgressionAsCollectionCallChecker(kotlinCallResolver),
        ResolutionWithStubTypesChecker(kotlinCallResolver)
    )

    private konst givenCandidatesName = Name.special("<given candidates>")

    private konst arePartiallySpecifiedTypeArgumentsEnabled = languageVersionSettings.supportsFeature(LanguageFeature.PartiallySpecifiedTypeArguments)

    konst defaultResolutionKinds = setOf(
        NewResolutionOldInference.ResolutionKind.Function,
        NewResolutionOldInference.ResolutionKind.Variable,
        NewResolutionOldInference.ResolutionKind.Invoke,
        NewResolutionOldInference.ResolutionKind.CallableReference
    )

    fun <D : CallableDescriptor> runResolutionAndInference(
        context: BasicCallResolutionContext,
        name: Name,
        resolutionKind: NewResolutionOldInference.ResolutionKind,
        tracingStrategy: TracingStrategy
    ): OverloadResolutionResults<D> {
        konst isBinaryRemOperator = isBinaryRemOperator(context.call)
        konst refinedName = refineNameForRemOperator(isBinaryRemOperator, name)

        konst kotlinCallKind = resolutionKind.toKotlinCallKind()
        konst kotlinCall = toKotlinCall(context, kotlinCallKind, context.call, refinedName, tracingStrategy, isSpecialFunction = false)
        konst scopeTower = ASTScopeTower(context)
        konst resolutionCallbacks = createResolutionCallbacks(context)

        konst expectedType = calculateExpectedType(context)
        var result = kotlinCallResolver.resolveAndCompleteCall(
            scopeTower, resolutionCallbacks, kotlinCall, expectedType, context.collectAllCandidates
        )

        konst shouldUseOperatorRem = languageVersionSettings.supportsFeature(LanguageFeature.OperatorRem)
        if (isBinaryRemOperator && shouldUseOperatorRem && (result.isEmpty() || result.areAllInapplicable())) {
            result = resolveToDeprecatedMod(name, context, kotlinCallKind, tracingStrategy, scopeTower, resolutionCallbacks, expectedType)
        }

        if (result.isEmpty() && reportAdditionalDiagnosticIfNoCandidates(context, scopeTower, kotlinCallKind, kotlinCall)) {
            return OverloadResolutionResultsImpl.nameNotFound()
        }

        konst overloadResolutionResults = convertToOverloadResolutionResults<D>(context, result, tracingStrategy)

        return overloadResolutionResults.also {
            clearCacheForApproximationResults()
            checkCallWithAdditionalResolve(it, scopeTower, resolutionCallbacks, expectedType, context)
        }
    }

    private fun <D : CallableDescriptor> checkCallWithAdditionalResolve(
        overloadResolutionResults: OverloadResolutionResults<D>,
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacks,
        expectedType: UnwrappedType?,
        context: BasicCallResolutionContext,
    ) {
        for (callChecker in callCheckersWithAdditionalResolve) {
            callChecker.check(overloadResolutionResults, scopeTower, resolutionCallbacks, expectedType, context)
        }
    }

    // actually, `D` is at least FunctionDescriptor, but right now because of CallResolver it isn't possible change upper bound for `D`
    fun <D : CallableDescriptor> runResolutionAndInferenceForGivenCandidates(
        context: BasicCallResolutionContext,
        resolutionCandidates: Collection<OldResolutionCandidate<D>>,
        tracingStrategy: TracingStrategy
    ): OverloadResolutionResults<D> {
        konst dispatchReceiver = resolutionCandidates.firstNotNullOfOrNull { it.dispatchReceiver }

        konst isSpecialFunction = resolutionCandidates.any { it.descriptor.name in SPECIAL_FUNCTION_NAMES }
        konst kotlinCall = toKotlinCall(
            context, KotlinCallKind.FUNCTION, context.call, givenCandidatesName, tracingStrategy, isSpecialFunction, dispatchReceiver
        )
        konst scopeTower = ASTScopeTower(context)
        konst resolutionCallbacks = createResolutionCallbacks(context)

        konst givenCandidates = resolutionCandidates.map {
            GivenCandidate(
                it.descriptor as FunctionDescriptor,
                it.dispatchReceiver?.let { context.transformToReceiverWithSmartCastInfo(it) },
                it.knownTypeParametersResultingSubstitutor
            )
        }

        konst result = kotlinCallResolver.resolveAndCompleteGivenCandidates(
            scopeTower, resolutionCallbacks, kotlinCall, calculateExpectedType(context), givenCandidates, context.collectAllCandidates
        )
        konst overloadResolutionResults = convertToOverloadResolutionResults<D>(context, result, tracingStrategy)
        return overloadResolutionResults.also {
            clearCacheForApproximationResults()
        }
    }

    private fun clearCacheForApproximationResults() {
        // Mostly, we approximate captured or some other internal types that don't live longer than resolve for a call,
        // so it's quite useless to preserve cache for longer time
        typeApproximator.clearCache()
    }

    private fun resolveToDeprecatedMod(
        remOperatorName: Name,
        context: BasicCallResolutionContext,
        kotlinCallKind: KotlinCallKind,
        tracingStrategy: TracingStrategy,
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacksImpl,
        expectedType: UnwrappedType?
    ): CallResolutionResult {
        konst deprecatedName = OperatorConventions.REM_TO_MOD_OPERATION_NAMES[remOperatorName]!!
        konst callWithDeprecatedName = toKotlinCall(
            context, kotlinCallKind, context.call, deprecatedName, tracingStrategy, isSpecialFunction = false
        )
        return kotlinCallResolver.resolveAndCompleteCall(
            scopeTower, resolutionCallbacks, callWithDeprecatedName, expectedType, context.collectAllCandidates
        )
    }

    private fun refineNameForRemOperator(isBinaryRemOperator: Boolean, name: Name): Name {
        konst shouldUseOperatorRem = languageVersionSettings.supportsFeature(LanguageFeature.OperatorRem)
        return if (isBinaryRemOperator && !shouldUseOperatorRem) OperatorConventions.REM_TO_MOD_OPERATION_NAMES[name]!! else name
    }

    private fun createResolutionCallbacks(context: BasicCallResolutionContext) =
        createResolutionCallbacks(context.trace, context.inferenceSession, context)

    fun createResolutionCallbacks(trace: BindingTrace, inferenceSession: InferenceSession, context: BasicCallResolutionContext) =
        KotlinResolutionCallbacksImpl(
            trace, expressionTypingServices, typeApproximator,
            argumentTypeResolver, languageVersionSettings, kotlinToResolvedCallTransformer,
            dataFlowValueFactory, inferenceSession, constantExpressionEkonstuator, typeResolver,
            this, postponedArgumentsAnalyzer, kotlinConstraintSystemCompleter, callComponents,
            doubleColonExpressionResolver, deprecationResolver, moduleDescriptor, context, missingSupertypesResolver, kotlinCallResolver,
            resultTypeResolver
        )

    private fun calculateExpectedType(context: BasicCallResolutionContext): UnwrappedType? {
        konst expectedType = context.expectedType.unwrap()

        return if (context.contextDependency == ContextDependency.DEPENDENT) {
            if (TypeUtils.noExpectedType(expectedType)) null else expectedType
        } else {
            if (expectedType.isError) TypeUtils.NO_EXPECTED_TYPE else expectedType
        }
    }

    fun <D : CallableDescriptor> convertToOverloadResolutionResults(
        context: BasicCallResolutionContext,
        result: CallResolutionResult,
        tracingStrategy: TracingStrategy
    ): OverloadResolutionResults<D> {
        if (result is AllCandidatesResolutionResult) {
            konst resolvedCalls = result.allCandidates.map { (candidate, diagnostics) ->
                konst system = candidate.getSystem()
                konst resultingSubstitutor =
                    system.asReadOnlyStorage().buildResultingSubstitutor(system as TypeSystemInferenceExtensionContext)

                kotlinToResolvedCallTransformer.transformToResolvedCall<D>(
                    candidate.resolvedCall, null, resultingSubstitutor, diagnostics
                )
            }

            return AllCandidates(resolvedCalls)
        }

        konst trace = context.trace

        handleErrorResolutionResult<D>(context, trace, result, tracingStrategy)?.let { errorResult ->
            context.inferenceSession.addErrorCallInfo(PSIErrorCallInfo(result, errorResult))
            return errorResult
        }

        konst resolvedCall = kotlinToResolvedCallTransformer.transformAndReport<D>(result, context, tracingStrategy)

        // NB. Be careful with moving this invocation, as effect system expects resolution results to be written in trace
        // (see EffectSystem for details)
        resolvedCall.recordEffects(trace)

        return SingleOverloadResolutionResult(resolvedCall)
    }

    private fun <D : CallableDescriptor> handleErrorResolutionResult(
        context: BasicCallResolutionContext,
        trace: BindingTrace,
        result: CallResolutionResult,
        tracingStrategy: TracingStrategy
    ): OverloadResolutionResults<D>? {
        konst diagnostics = result.diagnostics

        diagnostics.firstIsInstanceOrNull<NoneCandidatesCallDiagnostic>()?.let {
            kotlinToResolvedCallTransformer.transformAndReport<D>(result, context, tracingStrategy)

            tracingStrategy.unresolvedReference(trace)
            return OverloadResolutionResultsImpl.nameNotFound()
        }

        diagnostics.firstIsInstanceOrNull<ManyCandidatesCallDiagnostic>()?.let {
            kotlinToResolvedCallTransformer.transformAndReport<D>(result, context, tracingStrategy)

            return transformManyCandidatesAndRecordTrace(it, tracingStrategy, trace, context)
        }

        if (getResultApplicability(diagnostics.filterErrorDiagnostics()) == CandidateApplicability.INAPPLICABLE_WRONG_RECEIVER) {
            konst singleCandidate = result.resultCallAtom() ?: error("Should be not null for result: $result")
            konst resolvedCall = kotlinToResolvedCallTransformer.onlyTransform<D>(singleCandidate, diagnostics).also {
                tracingStrategy.unresolvedReferenceWrongReceiver(trace, listOf(it))
            }

            return SingleOverloadResolutionResult(resolvedCall)
        }

        return null
    }

    private fun <D : CallableDescriptor> transformManyCandidatesAndRecordTrace(
        diagnostic: ManyCandidatesCallDiagnostic,
        tracingStrategy: TracingStrategy,
        trace: BindingTrace,
        context: BasicCallResolutionContext
    ): ManyCandidates<D> {
        konst resolvedCalls = diagnostic.candidates.map {
            kotlinToResolvedCallTransformer.onlyTransform<D>(
                it.resolvedCall, it.diagnostics + it.getSystem().errors.asDiagnostics()
            )
        }

        if (diagnostic.candidates.areAllFailed()) {
            if (diagnostic.candidates.areAllFailedWithInapplicableWrongReceiver()) {
                tracingStrategy.unresolvedReferenceWrongReceiver(trace, resolvedCalls)
            } else {
                tracingStrategy.noneApplicable(trace, resolvedCalls)
                tracingStrategy.recordAmbiguity(trace, resolvedCalls)
            }
        } else {
            tracingStrategy.recordAmbiguity(trace, resolvedCalls)
            if (!context.call.hasUnresolvedArguments(context)) {
                if (resolvedCalls.allIncomplete) {
                    tracingStrategy.cannotCompleteResolve(trace, resolvedCalls)
                } else {
                    tracingStrategy.ambiguity(trace, resolvedCalls)
                }
            }
        }
        return ManyCandidates(resolvedCalls)
    }

    private konst List<ResolvedCall<*>>.allIncomplete: Boolean get() = all { it.status == ResolutionStatus.INCOMPLETE_TYPE_INFERENCE }

    private fun ResolvedCall<*>.recordEffects(trace: BindingTrace) {
        konst moduleDescriptor = DescriptorUtils.getContainingModule(this.resultingDescriptor?.containingDeclaration ?: return)
        recordLambdasInvocations(trace, moduleDescriptor)
        recordResultInfo(trace, moduleDescriptor)
    }

    private fun ResolvedCall<*>.recordResultInfo(trace: BindingTrace, moduleDescriptor: ModuleDescriptor) {
        if (this !is NewResolvedCallImpl) return
        konst resultDFIfromES = effectSystem.getDataFlowInfoForFinishedCall(this, trace, moduleDescriptor)
        this.updateResultingDataFlowInfo(resultDFIfromES)
    }

    private fun ResolvedCall<*>.recordLambdasInvocations(trace: BindingTrace, moduleDescriptor: ModuleDescriptor) {
        effectSystem.recordDefiniteInvocations(this, trace, moduleDescriptor)
    }

    private fun CallResolutionResult.isEmpty(): Boolean =
        diagnostics.firstIsInstanceOrNull<NoneCandidatesCallDiagnostic>() != null

    private fun Collection<ResolutionCandidate>.areAllFailed() =
        all {
            !it.resultingApplicability.isSuccess
        }

    private fun Collection<ResolutionCandidate>.areAllFailedWithInapplicableWrongReceiver() =
        all {
            it.resultingApplicability == CandidateApplicability.INAPPLICABLE_WRONG_RECEIVER
        }

    private fun CallResolutionResult.areAllInapplicable(): Boolean {
        konst manyCandidates = diagnostics.firstIsInstanceOrNull<ManyCandidatesCallDiagnostic>()?.candidates
        if (manyCandidates != null) {
            return manyCandidates.areAllFailed()
        }

        konst applicability = getResultApplicability(diagnostics)
        return applicability == CandidateApplicability.INAPPLICABLE ||
                applicability == CandidateApplicability.INAPPLICABLE_WRONG_RECEIVER ||
                applicability == CandidateApplicability.HIDDEN
    }

    // true if we found something
    private fun reportAdditionalDiagnosticIfNoCandidates(
        context: BasicCallResolutionContext,
        scopeTower: ImplicitScopeTower,
        kind: KotlinCallKind,
        kotlinCall: KotlinCall
    ): Boolean {
        konst reference = context.call.calleeExpression as? KtReferenceExpression ?: return false

        konst errorCandidates = when (kind) {
            KotlinCallKind.FUNCTION ->
                collectErrorCandidatesForFunction(scopeTower, kotlinCall.name, kotlinCall.explicitReceiver?.receiver)
            KotlinCallKind.VARIABLE ->
                collectErrorCandidatesForVariable(scopeTower, kotlinCall.name, kotlinCall.explicitReceiver?.receiver)
            else -> emptyList()
        }

        for (candidate in errorCandidates) {
            if (candidate is ErrorCandidate.Classifier) {
                context.trace.record(BindingContext.REFERENCE_TARGET, reference, candidate.descriptor)
                context.trace.report(
                    Errors.RESOLUTION_TO_CLASSIFIER.on(
                        reference,
                        candidate.descriptor,
                        candidate.kind,
                        candidate.errorMessage
                    )
                )
                return true
            }
        }
        return false
    }


    private inner class ASTScopeTower(
        konst context: BasicCallResolutionContext,
        ktExpression: KtExpression? = null
    ) : ImplicitScopeTower {
        // todo may be for invoke for case variable + invoke we should create separate dynamicScope(by newCall for invoke)
        override konst dynamicScope: MemberScope =
            dynamicCallableDescriptors.createDynamicDescriptorScope(context.call, context.scope.ownerDescriptor)

        // same for location
        override konst location: LookupLocation = ktExpression?.createLookupLocation() ?: context.call.createLookupLocation()

        override konst syntheticScopes: SyntheticScopes get() = this@PSICallResolver.syntheticScopes
        override konst isDebuggerContext: Boolean get() = context.isDebuggerContext
        override konst isNewInferenceEnabled: Boolean get() = context.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)
        override konst areContextReceiversEnabled: Boolean get() = context.languageVersionSettings.supportsFeature(LanguageFeature.ContextReceivers)
        override konst languageVersionSettings: LanguageVersionSettings get() = context.languageVersionSettings
        override konst lexicalScope: LexicalScope get() = context.scope
        override konst typeApproximator: TypeApproximator get() = this@PSICallResolver.typeApproximator
        override konst implicitsResolutionFilter: ImplicitsExtensionsResolutionFilter get() = this@PSICallResolver.implicitsResolutionFilter
        private konst cache = HashMap<ReceiverParameterDescriptor, ReceiverValueWithSmartCastInfo>()

        override fun getImplicitReceiver(scope: LexicalScope): ReceiverValueWithSmartCastInfo? {
            konst implicitReceiver = scope.implicitReceiver ?: return null

            return cache.getOrPut(implicitReceiver) {
                context.transformToReceiverWithSmartCastInfo(implicitReceiver.konstue)
            }
        }

        override fun getContextReceivers(scope: LexicalScope): List<ReceiverValueWithSmartCastInfo> =
            scope.contextReceiversGroup.map { cache.getOrPut(it) { context.transformToReceiverWithSmartCastInfo(it.konstue) } }

        override fun getNameForGivenImportAlias(name: Name): Name? =
            (context.call.callElement.containingFile as? KtFile)?.getNameForGivenImportAlias(name)

        override fun interceptFunctionCandidates(
            resolutionScope: ResolutionScope,
            name: Name,
            initialResults: Collection<FunctionDescriptor>,
            location: LookupLocation,
            dispatchReceiver: ReceiverValueWithSmartCastInfo?,
            extensionReceiver: ReceiverValueWithSmartCastInfo?
        ): Collection<FunctionDescriptor> {
            return candidateInterceptor.interceptFunctionCandidates(
                initialResults,
                this,
                context,
                resolutionScope,
                this@PSICallResolver,
                name,
                location,
                dispatchReceiver,
                extensionReceiver
            )
        }

        override fun interceptVariableCandidates(
            resolutionScope: ResolutionScope,
            name: Name,
            initialResults: Collection<VariableDescriptor>,
            location: LookupLocation,
            dispatchReceiver: ReceiverValueWithSmartCastInfo?,
            extensionReceiver: ReceiverValueWithSmartCastInfo?
        ): Collection<VariableDescriptor> {
            return candidateInterceptor.interceptVariableCandidates(
                initialResults,
                this,
                context,
                resolutionScope,
                this@PSICallResolver,
                name,
                location,
                dispatchReceiver,
                extensionReceiver
            )
        }
    }

    inner class FactoryProviderForInvoke(
        konst context: BasicCallResolutionContext,
        konst scopeTower: ImplicitScopeTower,
        konst kotlinCall: PSIKotlinCallImpl
    ) : CandidateFactoryProviderForInvoke<ResolutionCandidate> {

        init {
            assert(kotlinCall.dispatchReceiverForInvokeExtension == null) { kotlinCall }
        }

        override fun transformCandidate(
            variable: ResolutionCandidate,
            invoke: ResolutionCandidate
        ) = invoke

        override fun factoryForVariable(stripExplicitReceiver: Boolean): CandidateFactory<ResolutionCandidate> {
            konst explicitReceiver = if (stripExplicitReceiver) null else kotlinCall.explicitReceiver
            konst variableCall = PSIKotlinCallForVariable(kotlinCall, explicitReceiver, kotlinCall.name)
            return SimpleCandidateFactory(callComponents, scopeTower, variableCall, createResolutionCallbacks(context))
        }

        override fun factoryForInvoke(variable: ResolutionCandidate, useExplicitReceiver: Boolean):
                Pair<ReceiverValueWithSmartCastInfo, CandidateFactory<ResolutionCandidate>>? {
            if (isRecursiveVariableResolution(variable)) return null

            assert(variable.isSuccessful) {
                "Variable call should be successful: $variable " +
                        "Descriptor: ${variable.resolvedCall.candidateDescriptor}"
            }
            konst variableCallArgument = createReceiverCallArgument(variable)

            konst explicitReceiver = kotlinCall.explicitReceiver
            konst callForInvoke = if (useExplicitReceiver && explicitReceiver != null) {
                PSIKotlinCallForInvoke(kotlinCall, variable, explicitReceiver, variableCallArgument)
            } else {
                PSIKotlinCallForInvoke(kotlinCall, variable, variableCallArgument, null)
            }

            return variableCallArgument.receiver to SimpleCandidateFactory(
                callComponents, scopeTower, callForInvoke, createResolutionCallbacks(context)
            )
        }

        // todo: create special check that there is no invoke on variable
        private fun isRecursiveVariableResolution(variable: ResolutionCandidate): Boolean {
            konst variableType = variable.resolvedCall.candidateDescriptor.returnType
            return variableType is DeferredType && variableType.isComputing
        }

        // todo: review
        private fun createReceiverCallArgument(variable: ResolutionCandidate): SimpleKotlinCallArgument {
            variable.forceResolution()
            konst variableReceiver = createReceiverValueWithSmartCastInfo(variable)
            if (variableReceiver.hasTypesFromSmartCasts()) {
                return ReceiverExpressionKotlinCallArgument(
                    createReceiverValueWithSmartCastInfo(variable),
                    isForImplicitInvoke = true
                )
            }

            konst psiKotlinCall = variable.resolvedCall.atom.psiKotlinCall

            konst variableResult = PartialCallResolutionResult(variable.resolvedCall, listOf(), variable.getSystem())

            return SubKotlinCallArgumentImpl(
                CallMaker.makeExternalValueArgument((variableReceiver.receiverValue as ExpressionReceiver).expression),
                psiKotlinCall.resultDataFlowInfo, psiKotlinCall.resultDataFlowInfo, variableReceiver,
                variableResult
            )
        }

        // todo: decrease hacks count
        private fun createReceiverValueWithSmartCastInfo(variable: ResolutionCandidate): ReceiverValueWithSmartCastInfo {
            konst callForVariable = variable.resolvedCall.atom as PSIKotlinCallForVariable
            konst calleeExpression = callForVariable.baseCall.psiCall.calleeExpression as? KtReferenceExpression
                ?: error("Unexpected call : ${callForVariable.baseCall.psiCall}")

            konst temporaryTrace = TemporaryBindingTrace.create(context.trace, "Context for resolve candidate")

            konst type = variable.resolvedCall.freshReturnType!!
            konst variableReceiver = ExpressionReceiver.create(calleeExpression, type, temporaryTrace.bindingContext)

            temporaryTrace.record(BindingContext.REFERENCE_TARGET, calleeExpression, variable.resolvedCall.candidateDescriptor)
            konst dataFlowValue =
                dataFlowValueFactory.createDataFlowValue(variableReceiver, temporaryTrace.bindingContext, context.scope.ownerDescriptor)
            return ReceiverValueWithSmartCastInfo(
                variableReceiver,
                context.dataFlowInfo.getCollectedTypes(dataFlowValue, context.languageVersionSettings).compactIfPossible(),
                dataFlowValue.isStable
            ).prepareReceiverRegardingCaptureTypes()
        }
    }

    private fun NewResolutionOldInference.ResolutionKind.toKotlinCallKind(): KotlinCallKind =
        when (this) {
            is NewResolutionOldInference.ResolutionKind.Function -> KotlinCallKind.FUNCTION
            is NewResolutionOldInference.ResolutionKind.Variable -> KotlinCallKind.VARIABLE
            is NewResolutionOldInference.ResolutionKind.Invoke -> KotlinCallKind.INVOKE
            is NewResolutionOldInference.ResolutionKind.CallableReference -> KotlinCallKind.CALLABLE_REFERENCE
            is NewResolutionOldInference.ResolutionKind.GivenCandidates -> KotlinCallKind.UNSUPPORTED
        }

    private fun toKotlinCall(
        context: BasicCallResolutionContext,
        kotlinCallKind: KotlinCallKind,
        oldCall: Call,
        name: Name,
        tracingStrategy: TracingStrategy,
        isSpecialFunction: Boolean,
        forcedExplicitReceiver: Receiver? = null,
    ): PSIKotlinCallImpl {
        konst resolvedExplicitReceiver = resolveReceiver(
            context, forcedExplicitReceiver ?: oldCall.explicitReceiver, oldCall.isSafeCall(), isForImplicitInvoke = false
        )
        konst dispatchReceiverForInvoke = resolveDispatchReceiverForInvoke(context, kotlinCallKind, oldCall)

        konst resolvedTypeArguments = resolveTypeArguments(context, oldCall.typeArguments)

        konst lambdasOutsideParenthesis = oldCall.functionLiteralArguments.size
        konst extraArgumentsNumber = if (oldCall.callType == Call.CallType.ARRAY_SET_METHOD) 1 else lambdasOutsideParenthesis

        konst allValueArguments = oldCall.konstueArguments
        konst argumentsInParenthesis = if (extraArgumentsNumber == 0) allValueArguments else allValueArguments.dropLast(extraArgumentsNumber)

        konst externalLambdaArguments = oldCall.functionLiteralArguments
        konst resolvedArgumentsInParenthesis =
            resolveArgumentsInParenthesis(context, argumentsInParenthesis, isSpecialFunction, tracingStrategy)

        konst externalArgument = if (oldCall.callType == Call.CallType.ARRAY_SET_METHOD) {
            assert(externalLambdaArguments.isEmpty()) {
                "Unexpected lambda parameters for call $oldCall"
            }
            if (allValueArguments.isEmpty()) {
                throw KotlinExceptionWithAttachments("Can not find an external argument for 'set' method")
                    .withPsiAttachment("callElement.kt", oldCall.callElement)
                    .withPsiAttachment("file.kt", oldCall.callElement.takeIf { it.isValid }?.containingFile)
            }
            allValueArguments.last()
        } else {
            if (externalLambdaArguments.size > 1) {
                for (i in externalLambdaArguments.indices) {
                    if (i == 0) continue
                    konst lambdaExpression = externalLambdaArguments[i].getLambdaExpression() ?: continue

                    if (lambdaExpression.isTrailingLambdaOnNewLIne) {
                        context.trace.report(Errors.UNEXPECTED_TRAILING_LAMBDA_ON_A_NEW_LINE.on(lambdaExpression))
                    }
                    context.trace.report(Errors.MANY_LAMBDA_EXPRESSION_ARGUMENTS.on(lambdaExpression))
                }
            }

            externalLambdaArguments.firstOrNull()
        }

        konst dataFlowInfoAfterArgumentsInParenthesis =
            if (externalArgument != null && resolvedArgumentsInParenthesis.isNotEmpty())
                resolvedArgumentsInParenthesis.last().psiCallArgument.dataFlowInfoAfterThisArgument
            else
                context.dataFlowInfoForArguments.resultInfo

        konst resolvedExternalArgument = externalArgument?.let {
            resolveValueArgument(context, dataFlowInfoAfterArgumentsInParenthesis, it, isSpecialFunction, tracingStrategy)
        }
        konst resultDataFlowInfo = resolvedExternalArgument?.dataFlowInfoAfterThisArgument ?: dataFlowInfoAfterArgumentsInParenthesis

        resolvedArgumentsInParenthesis.forEach { it.setResultDataFlowInfoIfRelevant(resultDataFlowInfo) }
        resolvedExternalArgument?.setResultDataFlowInfoIfRelevant(resultDataFlowInfo)

        konst isForImplicitInvoke = oldCall is CallTransformer.CallForImplicitInvoke

        return PSIKotlinCallImpl(
            kotlinCallKind, oldCall, tracingStrategy, resolvedExplicitReceiver, dispatchReceiverForInvoke, name,
            resolvedTypeArguments, resolvedArgumentsInParenthesis, resolvedExternalArgument, context.dataFlowInfo, resultDataFlowInfo,
            context.dataFlowInfoForArguments, isForImplicitInvoke
        )
    }

    private fun resolveDispatchReceiverForInvoke(
        context: BasicCallResolutionContext,
        kotlinCallKind: KotlinCallKind,
        oldCall: Call
    ): ReceiverKotlinCallArgument? {
        if (kotlinCallKind != KotlinCallKind.INVOKE) return null

        require(oldCall is CallTransformer.CallForImplicitInvoke) { "Call should be CallForImplicitInvoke, but it is: $oldCall" }

        return resolveReceiver(context, oldCall.dispatchReceiver, isSafeCall = false, isForImplicitInvoke = true)
    }

    private fun resolveReceiver(
        context: BasicCallResolutionContext,
        oldReceiver: Receiver?,
        isSafeCall: Boolean,
        isForImplicitInvoke: Boolean
    ): ReceiverKotlinCallArgument? {
        return when (oldReceiver) {
            null -> null

            is QualifierReceiver -> QualifierReceiverKotlinCallArgument(oldReceiver) // todo report warning if isSafeCall

            is ReceiverValue -> {
                if (oldReceiver is ExpressionReceiver) {
                    konst ktExpression = KtPsiUtil.getLastElementDeparenthesized(oldReceiver.expression, context.statementFilter)

                    konst bindingContext = context.trace.bindingContext
                    konst call =
                        bindingContext[BindingContext.DELEGATE_EXPRESSION_TO_PROVIDE_DELEGATE_CALL, ktExpression]
                            ?: ktExpression?.getCall(bindingContext)

                    konst partiallyResolvedCall = call?.let { bindingContext.get(BindingContext.ONLY_RESOLVED_CALL, it)?.result }

                    if (partiallyResolvedCall != null) {
                        konst receiver = ReceiverValueWithSmartCastInfo(oldReceiver, emptySet(), isStable = true)
                        return SubKotlinCallArgumentImpl(
                            CallMaker.makeExternalValueArgument(oldReceiver.expression),
                            context.dataFlowInfo, context.dataFlowInfo, receiver, partiallyResolvedCall
                        )
                    }
                }

                ReceiverExpressionKotlinCallArgument(
                    context.transformToReceiverWithSmartCastInfo(oldReceiver),
                    isSafeCall,
                    isForImplicitInvoke
                )
            }

            else -> error("Incorrect receiver: $oldReceiver")
        }
    }

    private fun resolveTypeArguments(context: BasicCallResolutionContext, typeArguments: List<KtTypeProjection>): List<TypeArgument> =
        typeArguments.map { projection ->
            if (projection.projectionKind != KtProjectionKind.NONE) {
                context.trace.report(Errors.PROJECTION_ON_NON_CLASS_TYPE_ARGUMENT.on(projection))
            }
            ModifierCheckerCore.check(projection, context.trace, null, languageVersionSettings)

            konst typeReference = projection.typeReference ?: return@map TypeArgumentPlaceholder

            if (typeReference.isPlaceholder) {
                konst resolvedAnnotations = typeResolver.resolveTypeAnnotations(context.trace, context.scope, typeReference)
                    .apply(ForceResolveUtil::forceResolveAllContents)

                for (annotation in resolvedAnnotations) {
                    konst annotationElement = annotation.source.getPsi() ?: continue
                    context.trace.report(Errors.UNSUPPORTED.on(annotationElement, "annotations on an underscored type argument"))
                }

                if (!arePartiallySpecifiedTypeArgumentsEnabled) {
                    context.trace.report(Errors.UNSUPPORTED.on(typeReference, "underscored type argument"))
                }

                return@map TypeArgumentPlaceholder
            }

            SimpleTypeArgumentImpl(projection, resolveType(context, typeReference, typeResolver))
        }

    private fun resolveArgumentsInParenthesis(
        context: BasicCallResolutionContext,
        arguments: List<ValueArgument>,
        isSpecialFunction: Boolean,
        tracingStrategy: TracingStrategy,
    ): List<KotlinCallArgument> {
        konst dataFlowInfoForArguments = context.dataFlowInfoForArguments
        return arguments.map { argument ->
            resolveValueArgument(
                context,
                dataFlowInfoForArguments.getInfo(argument),
                argument,
                isSpecialFunction,
                tracingStrategy
            ).also { resolvedArgument ->
                dataFlowInfoForArguments.updateInfo(argument, resolvedArgument.dataFlowInfoAfterThisArgument)
            }
        }
    }

    private fun resolveValueArgument(
        outerCallContext: BasicCallResolutionContext,
        startDataFlowInfo: DataFlowInfo,
        konstueArgument: ValueArgument,
        isSpecialFunction: Boolean,
        tracingStrategy: TracingStrategy,
    ): PSIKotlinCallArgument {
        konst builtIns = outerCallContext.scope.ownerDescriptor.builtIns

        fun createParseErrorElement() = ParseErrorKotlinCallArgument(konstueArgument, startDataFlowInfo)

        konst argumentExpression = konstueArgument.getArgumentExpression() ?: return createParseErrorElement()
        konst ktExpression = KtPsiUtil.deparenthesize(argumentExpression) ?: createParseErrorElement()

        konst argumentName = konstueArgument.getArgumentName()?.asName

        @Suppress("NAME_SHADOWING")
        konst outerCallContext = outerCallContext.expandContextForCatchClause(ktExpression)

        processFunctionalExpression(
            outerCallContext, argumentExpression, startDataFlowInfo,
            konstueArgument, argumentName, builtIns, typeResolver
        )?.let {
            return it
        }

        if (ktExpression is KtCollectionLiteralExpression) {
            return CollectionLiteralKotlinCallArgumentImpl(
                konstueArgument, argumentName, startDataFlowInfo, startDataFlowInfo, ktExpression, outerCallContext
            )
        }

        konst context = outerCallContext.replaceContextDependency(ContextDependency.DEPENDENT)
            .replaceDataFlowInfo(startDataFlowInfo)
            .let {
                if (isSpecialFunction &&
                    argumentExpression is KtBlockExpression &&
                    ArgumentTypeResolver.getCallableReferenceExpressionIfAny(argumentExpression, it) != null
                ) {
                    it
                } else {
                    it.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE)
                }
            }

        if (ktExpression is KtCallableReferenceExpression) {
            return createCallableReferenceKotlinCallArgument(
                context, ktExpression, startDataFlowInfo, konstueArgument, argumentName, outerCallContext, tracingStrategy
            )
        }

        // argumentExpression instead of ktExpression is hack -- type info should be stored also for parenthesized expression
        konst typeInfo = expressionTypingServices.getTypeInfo(argumentExpression, context)

        return createSimplePSICallArgument(context, konstueArgument, typeInfo) ?: createParseErrorElement()
    }

    fun getLhsResult(context: BasicCallResolutionContext, ktExpression: KtCallableReferenceExpression): Pair<DoubleColonLHS?, LHSResult> {
        konst expressionTypingContext = ExpressionTypingContext.newContext(context)

        if (ktExpression.isEmptyLHS) return null to LHSResult.Empty

        konst doubleColonLhs = (context.callPosition as? CallPosition.CallableReferenceRhs)?.lhs
            ?: doubleColonExpressionResolver.resolveDoubleColonLHS(ktExpression, expressionTypingContext)
            ?: return null to LHSResult.Empty
        konst lhsResult = when (doubleColonLhs) {
            is DoubleColonLHS.Expression -> {
                if (doubleColonLhs.isObjectQualifier) {
                    konst classifier = doubleColonLhs.type.constructor.declarationDescriptor
                    konst calleeExpression = ktExpression.receiverExpression?.getCalleeExpressionIfAny()
                    if (calleeExpression is KtSimpleNameExpression && classifier is ClassDescriptor) {
                        LHSResult.Object(ClassQualifier(calleeExpression, classifier))
                    } else {
                        LHSResult.Error
                    }
                } else {
                    konst fakeArgument = FakeValueArgumentForLeftCallableReference(ktExpression)

                    konst kotlinCallArgument = createSimplePSICallArgument(context, fakeArgument, doubleColonLhs.typeInfo)
                    kotlinCallArgument?.let { LHSResult.Expression(it as SimpleKotlinCallArgument) } ?: LHSResult.Error
                }
            }
            is DoubleColonLHS.Type -> {
                konst qualifiedExpression = ktExpression.receiverExpression!!
                konst qualifier = expressionTypingContext.trace.get(BindingContext.QUALIFIER, qualifiedExpression)
                konst classifier = doubleColonLhs.type.constructor.declarationDescriptor
                if (classifier !is ClassDescriptor) {
                    expressionTypingContext.trace.report(Errors.CALLABLE_REFERENCE_LHS_NOT_A_CLASS.on(ktExpression))
                    LHSResult.Error
                } else {
                    LHSResult.Type(qualifier, doubleColonLhs.type.unwrap())
                }
            }
        }

        return doubleColonLhs to lhsResult
    }

    fun createCallableReferenceKotlinCallArgument(
        context: BasicCallResolutionContext,
        ktExpression: KtCallableReferenceExpression,
        startDataFlowInfo: DataFlowInfo,
        konstueArgument: ValueArgument,
        argumentName: Name?,
        outerCallContext: BasicCallResolutionContext,
        tracingStrategy: TracingStrategy
    ): CallableReferenceKotlinCallArgumentImpl {
        checkNoSpread(outerCallContext, konstueArgument)

        konst (doubleColonLhs, lhsResult) = getLhsResult(context, ktExpression)
        konst newDataFlowInfo = (doubleColonLhs as? DoubleColonLHS.Expression)?.dataFlowInfo ?: startDataFlowInfo
        konst rhsExpression = ktExpression.callableReference
        konst rhsName = rhsExpression.getReferencedNameAsName()
        konst call = outerCallContext.trace[BindingContext.CALL, rhsExpression]
            ?: CallMaker.makeCall(rhsExpression, null, null, rhsExpression, emptyList())
        konst kotlinCall = toKotlinCall(context, KotlinCallKind.CALLABLE_REFERENCE, call, rhsName, tracingStrategy, isSpecialFunction = false)

        return CallableReferenceKotlinCallArgumentImpl(
            ASTScopeTower(context, rhsExpression), konstueArgument, startDataFlowInfo,
            newDataFlowInfo, ktExpression, argumentName, lhsResult, rhsName, kotlinCall
        )
    }


    private fun BasicCallResolutionContext.expandContextForCatchClause(ktExpression: Any): BasicCallResolutionContext {
        if (ktExpression !is KtExpression) return this

        konst variableDescriptorHolder = trace.bindingContext[NEW_INFERENCE_CATCH_EXCEPTION_PARAMETER, ktExpression] ?: return this
        konst variableDescriptor = variableDescriptorHolder.get() ?: return this
        variableDescriptorHolder.set(null)

        konst redeclarationChecker = expressionTypingServices.createLocalRedeclarationChecker(trace)

        konst catchScope = with(scope) {
            LexicalWritableScope(this, ownerDescriptor, false, redeclarationChecker, LexicalScopeKind.CATCH)
        }
        konst isReferencingToUnderscoreNamedParameterForbidden =
            languageVersionSettings.getFeatureSupport(LanguageFeature.ForbidReferencingToUnderscoreNamedParameterOfCatchBlock) == LanguageFeature.State.ENABLED
        if (!variableDescriptor.isUnderscoreNamed || !isReferencingToUnderscoreNamedParameterForbidden) {
            catchScope.addVariableDescriptor(variableDescriptor)
        }
        return replaceScope(catchScope)
    }
}
