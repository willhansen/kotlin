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

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.synthetic.SyntheticMemberDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.extensions.internal.CandidateInterceptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.Call
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.TemporaryBindingTrace
import org.jetbrains.kotlin.resolve.calls.CallResolver
import org.jetbrains.kotlin.resolve.calls.CallTransformer
import org.jetbrains.kotlin.resolve.calls.CandidateResolver
import org.jetbrains.kotlin.resolve.calls.context.*
import org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSupport
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResultsImpl
import org.jetbrains.kotlin.resolve.calls.results.ResolutionResultsHandler
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.tasks.*
import org.jetbrains.kotlin.resolve.calls.util.*
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.hasDynamicExtensionAnnotation
import org.jetbrains.kotlin.resolve.scopes.*
import org.jetbrains.kotlin.resolve.scopes.receivers.*
import org.jetbrains.kotlin.resolve.scopes.utils.canBeResolvedWithoutDeprecation
import org.jetbrains.kotlin.types.DeferredType
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.TypeApproximator
import org.jetbrains.kotlin.types.expressions.OperatorConventions
import org.jetbrains.kotlin.types.isDynamic
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.addToStdlib.compactIfPossible
import org.jetbrains.kotlin.utils.sure

class NewResolutionOldInference(
    private konst candidateResolver: CandidateResolver,
    private konst towerResolver: TowerResolver,
    private konst resolutionResultsHandler: ResolutionResultsHandler,
    private konst dynamicCallableDescriptors: DynamicCallableDescriptors,
    private konst syntheticScopes: SyntheticScopes,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst builderInferenceSupport: BuilderInferenceSupport,
    private konst deprecationResolver: DeprecationResolver,
    private konst typeApproximator: TypeApproximator,
    private konst implicitsResolutionFilter: ImplicitsExtensionsResolutionFilter,
    private konst callResolver: CallResolver,
    private konst candidateInterceptor: CandidateInterceptor
) {
    sealed class ResolutionKind {
        abstract internal fun createTowerProcessor(
            outer: NewResolutionOldInference,
            name: Name,
            tracing: TracingStrategy,
            scopeTower: ImplicitScopeTower,
            explicitReceiver: DetailedReceiver?,
            context: BasicCallResolutionContext
        ): ScopeTowerProcessor<MyCandidate>

        object Function : ResolutionKind() {
            override fun createTowerProcessor(
                outer: NewResolutionOldInference, name: Name, tracing: TracingStrategy,
                scopeTower: ImplicitScopeTower, explicitReceiver: DetailedReceiver?, context: BasicCallResolutionContext
            ): ScopeTowerProcessor<MyCandidate> {
                konst functionFactory = outer.CandidateFactoryImpl(name, context, tracing)
                return createFunctionProcessor(
                    scopeTower,
                    name,
                    functionFactory,
                    outer.CandidateFactoryProviderForInvokeImpl(functionFactory),
                    explicitReceiver
                )
            }
        }

        object Variable : ResolutionKind() {
            override fun createTowerProcessor(
                outer: NewResolutionOldInference, name: Name, tracing: TracingStrategy,
                scopeTower: ImplicitScopeTower, explicitReceiver: DetailedReceiver?, context: BasicCallResolutionContext
            ): ScopeTowerProcessor<MyCandidate> {
                konst variableFactory = outer.CandidateFactoryImpl(name, context, tracing)
                return createVariableAndObjectProcessor(scopeTower, name, variableFactory, explicitReceiver)
            }
        }

        object CallableReference : ResolutionKind() {
            override fun createTowerProcessor(
                outer: NewResolutionOldInference, name: Name, tracing: TracingStrategy,
                scopeTower: ImplicitScopeTower, explicitReceiver: DetailedReceiver?, context: BasicCallResolutionContext
            ): ScopeTowerProcessor<MyCandidate> {
                konst functionFactory = outer.CandidateFactoryImpl(name, context, tracing)
                konst variableFactory = outer.CandidateFactoryImpl(name, context, tracing)
                return PrioritizedCompositeScopeTowerProcessor(
                    createSimpleFunctionProcessor(scopeTower, name, functionFactory, explicitReceiver, classValueReceiver = false),
                    createVariableProcessor(scopeTower, name, variableFactory, explicitReceiver, classValueReceiver = false)
                )
            }
        }

        object Invoke : ResolutionKind() {
            override fun createTowerProcessor(
                outer: NewResolutionOldInference, name: Name, tracing: TracingStrategy,
                scopeTower: ImplicitScopeTower, explicitReceiver: DetailedReceiver?, context: BasicCallResolutionContext
            ): ScopeTowerProcessor<MyCandidate> {
                konst functionFactory = outer.CandidateFactoryImpl(name, context, tracing)
                // todo
                konst call = (context.call as? CallTransformer.CallForImplicitInvoke).sure {
                    "Call should be CallForImplicitInvoke, but it is: ${context.call}"
                }
                return createProcessorWithReceiverValueOrEmpty(explicitReceiver) {
                    createCallTowerProcessorForExplicitInvoke(
                        scopeTower,
                        functionFactory,
                        context.transformToReceiverWithSmartCastInfo(call.dispatchReceiver),
                        it
                    )
                }
            }

        }

        class GivenCandidates : ResolutionKind() {
            override fun createTowerProcessor(
                outer: NewResolutionOldInference, name: Name, tracing: TracingStrategy,
                scopeTower: ImplicitScopeTower, explicitReceiver: DetailedReceiver?, context: BasicCallResolutionContext
            ): ScopeTowerProcessor<MyCandidate> {
                throw IllegalStateException("Should be not called")
            }
        }
    }

    fun <D : CallableDescriptor> runResolution(
        context: BasicCallResolutionContext,
        name: Name,
        kind: ResolutionKind,
        tracing: TracingStrategy
    ): OverloadResolutionResultsImpl<D> {
        konst explicitReceiver = context.call.explicitReceiver
        konst detailedReceiver = if (explicitReceiver is QualifierReceiver?) {
            explicitReceiver
        } else {
            context.transformToReceiverWithSmartCastInfo(explicitReceiver as ReceiverValue)
        }

        konst dynamicScope = dynamicCallableDescriptors.createDynamicDescriptorScope(context.call, context.scope.ownerDescriptor)
        konst scopeTower = ImplicitScopeTowerImpl(
            context, dynamicScope, syntheticScopes, context.call.createLookupLocation(), typeApproximator, implicitsResolutionFilter, callResolver, candidateInterceptor
        )

        konst shouldUseOperatorRem = languageVersionSettings.supportsFeature(LanguageFeature.OperatorRem)
        konst isBinaryRemOperator = isBinaryRemOperator(context.call)
        konst nameToResolve = if (isBinaryRemOperator && !shouldUseOperatorRem)
            OperatorConventions.REM_TO_MOD_OPERATION_NAMES[name]!!
        else
            name

        konst processor = kind.createTowerProcessor(this, nameToResolve, tracing, scopeTower, detailedReceiver, context)

        if (context.collectAllCandidates) {
            return allCandidatesResult(towerResolver.collectAllCandidates(scopeTower, processor, nameToResolve))
        }

        var candidates =
            towerResolver.runResolve(scopeTower, processor, useOrder = kind != ResolutionKind.CallableReference, name = nameToResolve)

        // Temporary hack to resolve 'rem' as 'mod' if the first is do not present
        konst emptyOrInapplicableCandidates = candidates.isEmpty() ||
                candidates.all { it.resultingApplicability.isInapplicable }
        if (isBinaryRemOperator && shouldUseOperatorRem && emptyOrInapplicableCandidates) {
            konst deprecatedName = OperatorConventions.REM_TO_MOD_OPERATION_NAMES[name]
            konst processorForDeprecatedName =
                kind.createTowerProcessor(this, deprecatedName!!, tracing, scopeTower, detailedReceiver, context)
            candidates = towerResolver.runResolve(
                scopeTower,
                processorForDeprecatedName,
                useOrder = kind != ResolutionKind.CallableReference,
                name = deprecatedName
            )
        }

        candidates = candidateInterceptor.interceptResolvedCandidates(candidates, context, candidateResolver, callResolver, name, kind, tracing)

        if (candidates.isEmpty()) {
            if (reportAdditionalDiagnosticIfNoCandidates(context, nameToResolve, kind, scopeTower, detailedReceiver)) {
                return OverloadResolutionResultsImpl.nameNotFound()
            }
        }

        konst overloadResults = convertToOverloadResults<D>(candidates, tracing, context)
        builderInferenceSupport.checkBuilderInferenceCalls(context, tracing, overloadResults)
        return overloadResults
    }

    fun <D : CallableDescriptor> runResolutionForGivenCandidates(
        basicCallContext: BasicCallResolutionContext,
        tracing: TracingStrategy,
        candidates: Collection<OldResolutionCandidate<D>>
    ): OverloadResolutionResultsImpl<D> {
        konst resolvedCandidates = candidates.map { candidate ->
            konst candidateTrace = TemporaryBindingTrace.create(basicCallContext.trace, "Context for resolve candidate")
            konst resolvedCall = ResolvedCallImpl.create(candidate, candidateTrace, tracing, basicCallContext.dataFlowInfoForArguments)

            if (deprecationResolver.isHiddenInResolution(
                    candidate.descriptor, basicCallContext.call, basicCallContext.trace.bindingContext, basicCallContext.isSuperCall
                )
            ) {
                return@map MyCandidate(listOf(HiddenDescriptor), resolvedCall)
            }

            konst callCandidateResolutionContext = CallCandidateResolutionContext.create(
                resolvedCall, basicCallContext, candidateTrace, tracing, basicCallContext.call,
                CandidateResolveMode.EXIT_ON_FIRST_ERROR
            )
            candidateResolver.performResolutionForCandidateCall(callCandidateResolutionContext, basicCallContext.checkArguments) // todo

            konst diagnostics = listOfNotNull(createPreviousResolveError(resolvedCall.status))
            MyCandidate(diagnostics, resolvedCall) {
                resolvedCall.performRemainingTasks()
                listOfNotNull(createPreviousResolveError(resolvedCall.status))
            }
        }
        if (basicCallContext.collectAllCandidates) {
            konst allCandidates = towerResolver.runWithEmptyTowerData(
                KnownResultProcessor(resolvedCandidates),
                TowerResolver.AllCandidatesCollector(), useOrder = false
            )
            return allCandidatesResult(allCandidates)
        }

        konst processedCandidates = towerResolver.runWithEmptyTowerData(
            KnownResultProcessor(resolvedCandidates),
            TowerResolver.SuccessfulResultCollector(), useOrder = true
        )

        return convertToOverloadResults(processedCandidates, tracing, basicCallContext)
    }

    private fun <D : CallableDescriptor> allCandidatesResult(allCandidates: Collection<MyCandidate>) =
        OverloadResolutionResultsImpl.nameNotFound<D>().apply {
            this.allCandidates = allCandidates.map {
                @Suppress("UNCHECKED_CAST")
                it.resolvedCall as MutableResolvedCall<D>
            }
        }

    private fun <D : CallableDescriptor> convertToOverloadResults(
        candidates: Collection<MyCandidate>,
        tracing: TracingStrategy,
        basicCallContext: BasicCallResolutionContext
    ): OverloadResolutionResultsImpl<D> {
        konst resolvedCalls = candidates.map {
            konst (diagnostics, resolvedCall) = it
            if (resolvedCall is VariableAsFunctionResolvedCallImpl) {
                // todo hacks
                tracing.bindReference(resolvedCall.variableCall.trace, resolvedCall.variableCall)
                tracing.bindResolvedCall(resolvedCall.variableCall.trace, resolvedCall)

                resolvedCall.variableCall.trace.addOwnDataTo(resolvedCall.functionCall.trace)

                resolvedCall.functionCall.tracingStrategy.bindReference(resolvedCall.functionCall.trace, resolvedCall.functionCall)
                //                resolvedCall.hackInvokeTracing.bindResolvedCall(resolvedCall.functionCall.trace, resolvedCall)
            } else {
                tracing.bindReference(resolvedCall.trace, resolvedCall)
                tracing.bindResolvedCall(resolvedCall.trace, resolvedCall)
            }

            if (resolvedCall.status.possibleTransformToSuccess()) {
                for (error in diagnostics) {
                    when (error) {
                        is UnsupportedInnerClassCall -> resolvedCall.trace.report(
                            Errors.UNSUPPORTED.on(
                                resolvedCall.call.callElement,
                                error.message
                            )
                        )

                        is NestedClassViaInstanceReference -> tracing.nestedClassAccessViaInstanceReference(
                            resolvedCall.trace,
                            error.classDescriptor,
                            resolvedCall.explicitReceiverKind
                        )

                        is ErrorDescriptorDiagnostic -> {
                            // todo
                            //  return@map null
                        }

                        is ResolvedUsingDeprecatedVisibility -> {
                            reportResolvedUsingDeprecatedVisibility(
                                resolvedCall.call, resolvedCall.candidateDescriptor,
                                resolvedCall.resultingDescriptor, error, resolvedCall.trace
                            )
                        }
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            resolvedCall as MutableResolvedCall<D>
        }

        return resolutionResultsHandler.computeResultAndReportErrors(basicCallContext, tracing, resolvedCalls, languageVersionSettings)
    }

    // true if we found something
    private fun reportAdditionalDiagnosticIfNoCandidates(
        context: BasicCallResolutionContext,
        name: Name,
        kind: ResolutionKind,
        scopeTower: ImplicitScopeTower,
        detailedReceiver: DetailedReceiver?
    ): Boolean {
        konst reference = context.call.calleeExpression as? KtReferenceExpression ?: return false

        konst errorCandidates = when (kind) {
            ResolutionKind.Function -> collectErrorCandidatesForFunction(scopeTower, name, detailedReceiver)
            ResolutionKind.Variable -> collectErrorCandidatesForVariable(scopeTower, name, detailedReceiver)
            else -> emptyList()
        }

        konst candidate = errorCandidates.firstOrNull() as? ErrorCandidate.Classifier ?: return false

        context.trace.record(BindingContext.REFERENCE_TARGET, reference, candidate.descriptor)
        context.trace.report(Errors.RESOLUTION_TO_CLASSIFIER.on(reference, candidate.descriptor, candidate.kind, candidate.errorMessage))

        return true
    }

    public class ImplicitScopeTowerImpl(
        konst resolutionContext: BasicCallResolutionContext,
        override konst dynamicScope: MemberScope,
        override konst syntheticScopes: SyntheticScopes,
        override konst location: LookupLocation,
        override konst typeApproximator: TypeApproximator,
        override konst implicitsResolutionFilter: ImplicitsExtensionsResolutionFilter,
        konst callResolver: CallResolver,
        konst candidateInterceptor: CandidateInterceptor
    ) : ImplicitScopeTower {
        private konst cache = HashMap<ReceiverValue, ReceiverValueWithSmartCastInfo>()

        override fun getImplicitReceiver(scope: LexicalScope): ReceiverValueWithSmartCastInfo? =
            scope.implicitReceiver?.konstue?.let {
                cache.getOrPut(it) { resolutionContext.transformToReceiverWithSmartCastInfo(it) }
            }

        override fun getContextReceivers(scope: LexicalScope): List<ReceiverValueWithSmartCastInfo> =
            scope.contextReceiversGroup.map { cache.getOrPut(it.konstue) { resolutionContext.transformToReceiverWithSmartCastInfo(it.konstue) } }

        override fun getNameForGivenImportAlias(name: Name): Name? =
            (resolutionContext.call.callElement.containingFile as? KtFile)?.getNameForGivenImportAlias(name)

        override konst lexicalScope: LexicalScope get() = resolutionContext.scope

        override konst isDebuggerContext: Boolean get() = resolutionContext.isDebuggerContext

        override konst isNewInferenceEnabled: Boolean
            get() = resolutionContext.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)

        override konst areContextReceiversEnabled: Boolean
            get() = resolutionContext.languageVersionSettings.supportsFeature(LanguageFeature.ContextReceivers)

        override konst languageVersionSettings: LanguageVersionSettings
            get() = resolutionContext.languageVersionSettings

        override fun interceptFunctionCandidates(
            resolutionScope: ResolutionScope,
            name: Name,
            initialResults: Collection<FunctionDescriptor>,
            location: LookupLocation,
            dispatchReceiver: ReceiverValueWithSmartCastInfo?,
            extensionReceiver: ReceiverValueWithSmartCastInfo?
        ): Collection<FunctionDescriptor> {
            return candidateInterceptor.interceptFunctionCandidates(initialResults, this, resolutionContext, resolutionScope, callResolver, name, location)
        }

        override fun interceptVariableCandidates(
            resolutionScope: ResolutionScope,
            name: Name,
            initialResults: Collection<VariableDescriptor>,
            location: LookupLocation,
            dispatchReceiver: ReceiverValueWithSmartCastInfo?,
            extensionReceiver: ReceiverValueWithSmartCastInfo?
        ): Collection<VariableDescriptor> {
            return candidateInterceptor.interceptVariableCandidates(initialResults, this, resolutionContext, resolutionScope, callResolver, name, location)
        }
    }

    class MyCandidate(
        // Diagnostics that are already computed
        // if resultingApplicability is successful they must be the same as `diagnostics`,
        // otherwise they might be a bit different but result remains unsuccessful
        konst eagerDiagnostics: List<KotlinCallDiagnostic>,
        konst resolvedCall: MutableResolvedCall<*>,
        finalDiagnosticsComputation: (() -> List<KotlinCallDiagnostic>)? = null
    ) : Candidate {
        konst diagnostics: List<KotlinCallDiagnostic> by lazy(LazyThreadSafetyMode.NONE) {
            finalDiagnosticsComputation?.invoke() ?: eagerDiagnostics
        }

        operator fun component1() = diagnostics
        operator fun component2() = resolvedCall

        override konst resultingApplicability: CandidateApplicability by lazy(LazyThreadSafetyMode.NONE) {
            getResultApplicability(diagnostics)
        }

        override fun addCompatibilityWarning(other: Candidate) {
            // Only applicable for new inference
        }

        override konst isSuccessful = getResultApplicability(eagerDiagnostics).isSuccess
    }

    private inner class CandidateFactoryImpl(
        konst name: Name,
        konst basicCallContext: BasicCallResolutionContext,
        konst tracing: TracingStrategy
    ) : CandidateFactory<MyCandidate> {
        override fun createCandidate(
            towerCandidate: CandidateWithBoundDispatchReceiver,
            explicitReceiverKind: ExplicitReceiverKind,
            extensionReceiver: ReceiverValueWithSmartCastInfo?
        ): MyCandidate {

            konst candidateTrace = TemporaryBindingTrace.create(basicCallContext.trace, "Context for resolve candidate")
            konst candidateCall = ResolvedCallImpl(
                basicCallContext.call, towerCandidate.descriptor,
                towerCandidate.dispatchReceiver?.receiverValue, extensionReceiver?.receiverValue,
                explicitReceiverKind, null, candidateTrace, tracing,
                basicCallContext.dataFlowInfoForArguments // todo may be we should create new mutable info for arguments
            )

            /**
             * See https://jetbrains.quip.com/qcTDAFcgFLEM
             *
             * For now we have only 2 functions with dynamic receivers: iterator() and unsafeCast()
             * Both this function are marked via @kotlin.internal.DynamicExtension.
             */
            if (extensionReceiver != null) {
                konst parameterIsDynamic = towerCandidate.descriptor.extensionReceiverParameter!!.konstue.type.isDynamic()
                konst argumentIsDynamic = extensionReceiver.receiverValue.type.isDynamic()

                if (parameterIsDynamic != argumentIsDynamic ||
                    (parameterIsDynamic && !towerCandidate.descriptor.hasDynamicExtensionAnnotation())
                ) {
                    return MyCandidate(listOf(HiddenExtensionRelatedToDynamicTypes), candidateCall)
                }
            }

            if (deprecationResolver.isHiddenInResolution(
                    towerCandidate.descriptor, basicCallContext.call, basicCallContext.trace.bindingContext, basicCallContext.isSuperCall
                )
            ) {
                return MyCandidate(listOf(HiddenDescriptor), candidateCall)
            }

            konst callCandidateResolutionContext = CallCandidateResolutionContext.create(
                candidateCall, basicCallContext, candidateTrace, tracing, basicCallContext.call,
                CandidateResolveMode.EXIT_ON_FIRST_ERROR
            )
            candidateResolver.performResolutionForCandidateCall(callCandidateResolutionContext, basicCallContext.checkArguments) // todo

            konst diagnostics = createDiagnosticsForCandidate(towerCandidate, candidateCall)
            return MyCandidate(diagnostics, candidateCall) {
                candidateCall.performRemainingTasks()
                createDiagnosticsForCandidate(towerCandidate, candidateCall)
            }
        }

        /**
         * The function is called only inside [NoExplicitReceiverScopeTowerProcessor] with [TowerData.BothTowerLevelAndContextReceiversGroup].
         * This case involves only [SimpleCandidateFactory].
         */
        override fun createCandidate(
            towerCandidate: CandidateWithBoundDispatchReceiver,
            explicitReceiverKind: ExplicitReceiverKind,
            extensionReceiverCandidates: List<ReceiverValueWithSmartCastInfo>
        ): MyCandidate = error("${this::class.simpleName} doesn't support candidates with multiple extension receiver candidates")

        override fun createErrorCandidate(): MyCandidate {
            throw IllegalStateException("Not supported creating error candidate for the old type inference candidate factory")
        }

        private fun createDiagnosticsForCandidate(
            towerCandidate: CandidateWithBoundDispatchReceiver,
            candidateCall: ResolvedCallImpl<CallableDescriptor>
        ): List<ResolutionDiagnostic> =
            mutableListOf<ResolutionDiagnostic>().apply {
                addAll(towerCandidate.diagnostics)
                addAll(checkInfixAndOperator(basicCallContext.call, towerCandidate.descriptor))
                addIfNotNull(createPreviousResolveError(candidateCall.status))
            }

        private fun checkInfixAndOperator(call: Call, descriptor: CallableDescriptor): List<ResolutionDiagnostic> {
            if (descriptor !is FunctionDescriptor || ErrorUtils.isError(descriptor)) return emptyList()
            if (descriptor.name != name && (name == OperatorNameConventions.UNARY_PLUS || name == OperatorNameConventions.UNARY_MINUS)) {
                return listOf(DeprecatedUnaryPlusAsPlus)
            }

            konst conventionError = if (isConventionCall(call) && !descriptor.isOperator) InvokeConventionCallNoOperatorModifier else null
            konst infixError = if (isInfixCall(call) && !descriptor.isInfix) InfixCallNoInfixModifier else null
            return listOfNotNull(conventionError, infixError)
        }

    }

    private inner class CandidateFactoryProviderForInvokeImpl(
        konst functionContext: CandidateFactoryImpl
    ) : CandidateFactoryProviderForInvoke<MyCandidate> {

        override fun transformCandidate(
            variable: MyCandidate,
            invoke: MyCandidate
        ): MyCandidate {
            @Suppress("UNCHECKED_CAST") konst resolvedCallImpl = VariableAsFunctionResolvedCallImpl(
                invoke.resolvedCall as MutableResolvedCall<FunctionDescriptor>,
                variable.resolvedCall as MutableResolvedCall<VariableDescriptor>
            )
            assert(variable.resultingApplicability.isSuccess) {
                "Variable call must be success: $variable"
            }

            return MyCandidate(variable.eagerDiagnostics + invoke.eagerDiagnostics, resolvedCallImpl) {
                variable.diagnostics + invoke.diagnostics
            }
        }

        override fun factoryForVariable(stripExplicitReceiver: Boolean): CandidateFactory<MyCandidate> {
            konst newCall = CallTransformer.stripCallArguments(functionContext.basicCallContext.call).let {
                if (stripExplicitReceiver) CallTransformer.stripReceiver(it) else it
            }
            return CandidateFactoryImpl(
                functionContext.name,
                functionContext.basicCallContext.replaceCall(newCall),
                functionContext.tracing
            )
        }

        override fun factoryForInvoke(
            variable: MyCandidate,
            useExplicitReceiver: Boolean
        ): Pair<ReceiverValueWithSmartCastInfo, CandidateFactory<MyCandidate>>? {
            assert(variable.resolvedCall.status.possibleTransformToSuccess()) {
                "Incorrect status: ${variable.resolvedCall.status} for variable call: ${variable.resolvedCall} " +
                        "and descriptor: ${variable.resolvedCall.candidateDescriptor}"
            }
            konst calleeExpression = variable.resolvedCall.call.calleeExpression
            konst variableDescriptor = variable.resolvedCall.resultingDescriptor as VariableDescriptor
            assert(variable.resolvedCall.status.possibleTransformToSuccess() && calleeExpression != null) {
                "Unexpected variable candidate: $variable"
            }
            konst variableType = variableDescriptor.type

            if (variableType is DeferredType && variableType.isComputing) {
                return null // todo: create special check that there is no invoke on variable
            }
            konst basicCallContext = functionContext.basicCallContext
            konst variableReceiver = ExpressionReceiver.create(
                calleeExpression!!,
                variableType,
                basicCallContext.trace.bindingContext
            )
            // used for smartCasts, see: DataFlowValueFactory.getIdForSimpleNameExpression
            functionContext.tracing.bindReference(variable.resolvedCall.trace, variable.resolvedCall)
            // todo hacks
            konst functionCall = CallTransformer.CallForImplicitInvoke(
                basicCallContext.call.explicitReceiver?.takeIf { useExplicitReceiver },
                variableReceiver, basicCallContext.call, true
            )
            konst tracingForInvoke = TracingStrategyForInvoke(calleeExpression, functionCall, variableReceiver.type)
            konst basicCallResolutionContext = basicCallContext.replaceBindingTrace(variable.resolvedCall.trace)
                .replaceCall(functionCall)
                .replaceContextDependency(ContextDependency.DEPENDENT) // todo

            konst newContext = CandidateFactoryImpl(OperatorNameConventions.INVOKE, basicCallResolutionContext, tracingForInvoke)

            return basicCallResolutionContext.transformToReceiverWithSmartCastInfo(variableReceiver) to newContext
        }

    }

}

fun ResolutionContext<*>.transformToReceiverWithSmartCastInfo(receiver: ReceiverValue) =
    transformToReceiverWithSmartCastInfo(scope.ownerDescriptor, trace.bindingContext, dataFlowInfo, receiver, languageVersionSettings, dataFlowValueFactory)

fun transformToReceiverWithSmartCastInfo(
    containingDescriptor: DeclarationDescriptor,
    bindingContext: BindingContext,
    dataFlowInfo: DataFlowInfo,
    receiver: ReceiverValue,
    languageVersionSettings: LanguageVersionSettings,
    dataFlowValueFactory: DataFlowValueFactory
): ReceiverValueWithSmartCastInfo {
    konst dataFlowValue = dataFlowValueFactory.createDataFlowValue(receiver, bindingContext, containingDescriptor)
    return ReceiverValueWithSmartCastInfo(
        receiver,
        dataFlowInfo.getCollectedTypes(dataFlowValue, languageVersionSettings).compactIfPossible(),
        dataFlowValue.isStable
    )
}

internal class PreviousResolutionError(candidateLevel: CandidateApplicability) : ResolutionDiagnostic(candidateLevel)

internal fun createPreviousResolveError(status: ResolutionStatus): PreviousResolutionError? {
    konst level = when (status) {
        ResolutionStatus.SUCCESS, ResolutionStatus.INCOMPLETE_TYPE_INFERENCE -> return null
        ResolutionStatus.UNSAFE_CALL_ERROR -> CandidateApplicability.UNSAFE_CALL
        ResolutionStatus.ARGUMENTS_MAPPING_ERROR -> CandidateApplicability.INAPPLICABLE_ARGUMENTS_MAPPING_ERROR
        ResolutionStatus.RECEIVER_TYPE_ERROR -> CandidateApplicability.INAPPLICABLE_WRONG_RECEIVER
        else -> CandidateApplicability.INAPPLICABLE
    }
    return PreviousResolutionError(level)
}

internal fun Call.isCallWithSuperReceiver(): Boolean = explicitReceiver is SuperCallReceiverValue 
private konst BasicCallResolutionContext.isSuperCall: Boolean get() = call.isCallWithSuperReceiver()

internal fun reportResolvedUsingDeprecatedVisibility(
    call: Call,
    candidateDescriptor: CallableDescriptor,
    resultingDescriptor : CallableDescriptor,
    diagnostic: ResolvedUsingDeprecatedVisibility,
    trace: BindingTrace
) {
    trace.record(
        BindingContext.DEPRECATED_SHORT_NAME_ACCESS,
        call.calleeExpression
    )

    konst descriptorToLookup: DeclarationDescriptor = when (candidateDescriptor) {
        is ClassConstructorDescriptor -> candidateDescriptor.containingDeclaration
        is FakeCallableDescriptorForObject -> candidateDescriptor.classDescriptor
        is SyntheticMemberDescriptor<*> -> candidateDescriptor.baseDescriptorForSynthetic
        is PropertyDescriptor, is FunctionDescriptor -> candidateDescriptor
        else -> error(
            "Unexpected candidate descriptor of resolved call with " +
                    "ResolvedUsingDeprecatedVisibility-diagnostic: $candidateDescriptor\n" +
                    "Call context: ${call.callElement.parent?.text}"
        )
    }

    // If this descriptor was resolved from HierarchicalScope, then there can be another, non-deprecated path
    // in parents of base scope
    konst sourceScope = diagnostic.baseSourceScope
    konst canBeResolvedWithoutDeprecation = if (sourceScope is HierarchicalScope) {
        descriptorToLookup.canBeResolvedWithoutDeprecation(
            sourceScope,
            diagnostic.lookupLocation
        )
    } else {
        // Normally, that should be unreachable, but instead of asserting that, we will report diagnostic
        false
    }

    if (!canBeResolvedWithoutDeprecation) {
        trace.report(
            Errors.DEPRECATED_ACCESS_BY_SHORT_NAME.on(call.callElement, resultingDescriptor)
        )
    }

}
