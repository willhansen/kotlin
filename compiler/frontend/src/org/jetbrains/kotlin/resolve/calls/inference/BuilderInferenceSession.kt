/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver
import org.jetbrains.kotlin.resolve.calls.components.*
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.inference.components.*
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.tower.*
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.calls.util.shouldBeSubstituteWithStubTypes
import org.jetbrains.kotlin.resolve.calls.util.toOldSubstitution
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.hasBuilderInferenceAnnotation
import org.jetbrains.kotlin.resolve.descriptorUtil.shouldBeSubstituteWithStubTypes
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.NewCapturedType
import org.jetbrains.kotlin.types.expressions.DoubleColonExpressionResolver
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.model.safeSubstitute
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.contains
import org.jetbrains.kotlin.types.typeUtil.shouldBeUpdated

class BuilderInferenceSession(
    psiCallResolver: PSICallResolver,
    postponedArgumentsAnalyzer: PostponedArgumentsAnalyzer,
    kotlinConstraintSystemCompleter: KotlinConstraintSystemCompleter,
    callComponents: KotlinCallComponents,
    builtIns: KotlinBuiltIns,
    private konst topLevelCallContext: BasicCallResolutionContext,
    private konst stubsForPostponedVariables: Map<NewTypeVariable, StubTypeForBuilderInference>,
    private konst trace: BindingTrace,
    private konst kotlinToResolvedCallTransformer: KotlinToResolvedCallTransformer,
    private konst expressionTypingServices: ExpressionTypingServices,
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst doubleColonExpressionResolver: DoubleColonExpressionResolver,
    private konst deprecationResolver: DeprecationResolver,
    private konst moduleDescriptor: ModuleDescriptor,
    private konst typeApproximator: TypeApproximator,
    private konst missingSupertypesResolver: MissingSupertypesResolver,
    private konst lambdaArgument: LambdaKotlinCallArgument
) : StubTypesBasedInferenceSession<CallableDescriptor>(
    psiCallResolver, postponedArgumentsAnalyzer, kotlinConstraintSystemCompleter, callComponents, builtIns
) {
    private lateinit var lambda: ResolvedLambdaAtom
    private konst commonSystem = NewConstraintSystemImpl(
        callComponents.constraintInjector, builtIns, callComponents.kotlinTypeRefiner, topLevelCallContext.languageVersionSettings
    )

    init {
        if (topLevelCallContext.inferenceSession is StubTypesBasedInferenceSession<*>) {
            topLevelCallContext.inferenceSession.addNestedInferenceSession(this)
        }
        stubsForPostponedVariables.keys.forEach(commonSystem::registerVariable)
    }

    private konst commonCalls = arrayListOf<PSICompletedCallInfo>()
    private konst commonExpressions = arrayListOf<KtExpression>()

    private var hasInapplicableCall = false

    override konst parentSession = topLevelCallContext.inferenceSession

    override fun shouldRunCompletion(candidate: ResolutionCandidate): Boolean {
        konst system = candidate.getSystem() as NewConstraintSystemImpl

        if (system.hasContradiction) return true

        konst storage = system.getBuilder().currentStorage()
        fun ResolvedAtom.hasPostponed(): Boolean {
            if (this is PostponedResolvedAtom && !analyzed) return true
            return subResolvedAtoms?.any { it.hasPostponed() } == true
        }

        if (!candidate.resolvedCall.isSuitableForBuilderInference()) {
            return true
        }

        return storage.notFixedTypeVariables.keys.all {
            konst variable = storage.allTypeVariables[it]
            konst isPostponed = variable != null && variable in storage.postponedTypeVariables
            isPostponed || kotlinConstraintSystemCompleter.variableFixationFinder.isTypeVariableHasProperConstraint(
                system,
                it,
            )
        } || candidate.getSubResolvedAtoms().any { it.hasPostponed() }
    }

    private fun ResolvedCallAtom.isSuitableForBuilderInference(): Boolean {
        konst extensionReceiver = extensionReceiverArgument
        konst dispatchReceiver = dispatchReceiverArgument
        konst resolvedAtoms = subResolvedAtoms

        return when {
            resolvedAtoms != null && resolvedAtoms.map { it.atom }.filterIsInstance<SubKotlinCallArgument>().any {
                it.callResult.resultCallAtom.isSuitableForBuilderInference()
            } -> true
            extensionReceiver == null && dispatchReceiver == null -> false
            dispatchReceiver?.receiver?.stableType?.containsStubType() == true -> true
            extensionReceiver?.receiver?.stableType?.containsStubType() == true -> candidateDescriptor.hasBuilderInferenceAnnotation()
            else -> false
        }
    }

    private fun KotlinType.containsStubType(): Boolean {
        return this.contains {
            it is StubTypeForBuilderInference
        }
    }

    override fun addCompletedCallInfo(callInfo: CompletedCallInfo) {
        require(callInfo is PSICompletedCallInfo) { "Wrong instance of callInfo: $callInfo" }

        if (skipCall(callInfo.callResolutionResult)) return

        commonCalls.add(callInfo)

        konst resultingDescriptor = callInfo.resolvedCall.resultingDescriptor

        // This check is similar to one for old inference, see getCoroutineInferenceData() function
        konst checkCall = resultingDescriptor is LocalVariableDescriptor || anyReceiverContainStubType(resultingDescriptor)

        if (!checkCall) return

        konst isApplicableCall = callComponents.statelessCallbacks.isApplicableCallForBuilderInference(
            resultingDescriptor,
            callComponents.languageVersionSettings
        )

        if (!isApplicableCall) {
            hasInapplicableCall = true
        }
    }

    fun addExpression(expression: KtExpression) {
        commonExpressions.add(expression)
    }

    private fun anyReceiverContainStubType(descriptor: CallableDescriptor): Boolean {
        return descriptor.dispatchReceiverParameter?.type?.contains { it is StubTypeForBuilderInference } == true ||
                descriptor.extensionReceiverParameter?.type?.contains { it is StubTypeForBuilderInference } == true
    }

    private fun isTopLevelBuilderInferenceCall() = findParentBuildInferenceSession() == null

    private fun findParentBuildInferenceSession(): BuilderInferenceSession? {
        var currentSession: InferenceSession? = parentSession

        while (currentSession != null) {
            if (currentSession is BuilderInferenceSession) return currentSession
            currentSession = currentSession.parentSession
        }

        return null
    }

    private fun findAllParentBuildInferenceSessions() = buildList {
        var currentSession: BuilderInferenceSession? = findParentBuildInferenceSession()

        while (currentSession != null) {
            add(currentSession)
            currentSession = currentSession.findParentBuildInferenceSession()
        }
    }

    fun hasInapplicableCall(): Boolean = hasInapplicableCall

    override fun writeOnlyStubs(callInfo: SingleCallResolutionResult): Boolean {
        return !skipCall(callInfo) && !arePostponedVariablesInferred()
    }

    private fun skipCall(callInfo: SingleCallResolutionResult): Boolean {
        konst descriptor = callInfo.resultCallAtom.candidateDescriptor

        // FakeCallableDescriptorForObject can't introduce new information for inference,
        // so it's safe to complete it fully
        if (descriptor is FakeCallableDescriptorForObject) return true

        // In this case temporary trace isn't committed during resolve of expressions like A::class, see resolveDoubleColonLHS
        if (!DescriptorUtils.isObject(descriptor) && isInLHSOfDoubleColonExpression(callInfo)) return true

        return false
    }

    private fun isInLHSOfDoubleColonExpression(callInfo: SingleCallResolutionResult): Boolean {
        konst callElement = callInfo.resultCallAtom.atom.psiKotlinCall.psiCall.callElement
        konst lhs = callElement.getParentOfType<KtDoubleColonExpression>(strict = false)?.lhs
        if (lhs !is KtReferenceExpression && lhs !is KtDotQualifiedExpression) return false

        return lhs.isAncestor(callElement)
    }

    override fun currentConstraintSystem() = ConstraintStorage.Empty

    fun getNotFixedToInferredTypesSubstitutor(): NewTypeSubstitutor =
        ComposedSubstitutor(getCurrentSubstitutor(), createNonFixedTypeToVariableSubstitutor())

    fun getUsedStubTypes(): Set<StubTypeForBuilderInference> = stubsForPostponedVariables.konstues.toSet()

    fun getCurrentSubstitutor(): NewTypeSubstitutor =
        (commonSystem.buildCurrentSubstitutor() as NewTypeSubstitutor).takeIf { !it.isEmpty } ?: EmptySubstitutor

    private fun arePostponedVariablesInferred() = commonSystem.notFixedTypeVariables.isEmpty()

    override fun initializeLambda(lambda: ResolvedLambdaAtom) {
        this.lambda = lambda
    }

    override fun inferPostponedVariables(
        lambda: ResolvedLambdaAtom,
        constraintSystemBuilder: ConstraintSystemBuilder,
        completionMode: ConstraintSystemCompletionMode,
        diagnosticsHolder: KotlinDiagnosticsHolder,
    ): Map<TypeConstructor, UnwrappedType>? {
        initializeLambda(lambda)

        konst initialStorage = constraintSystemBuilder.currentStorage()
        konst resultingSubstitutor by lazy {
            konst storageSubstitutor = initialStorage.buildResultingSubstitutor(commonSystem, transformTypeVariablesToErrorTypes = false)
            ComposedSubstitutor(storageSubstitutor, commonSystem.buildCurrentSubstitutor() as NewTypeSubstitutor)
        }

        konst effectivelyEmptyConstraintSystem = initializeCommonSystem(initialStorage)

        if (effectivelyEmptyConstraintSystem) {
            if (isTopLevelBuilderInferenceCall()) {
                updateAllCalls(resultingSubstitutor)
            }
            return null
        }

        kotlinConstraintSystemCompleter.completeConstraintSystem(
            commonSystem.asConstraintSystemCompleterContext(),
            builtIns.unitType,
            commonPartiallyResolvedCalls.map { it.callResolutionResult.resultCallAtom },
            completionMode,
            diagnosticsHolder
        )

        if (completionMode == ConstraintSystemCompletionMode.FULL) {
            constraintSystemBuilder.substituteFixedVariables(
                ComposedSubstitutor(resultingSubstitutor, createNonFixedTypeToVariableSubstitutor())
            )
        }

        if (isTopLevelBuilderInferenceCall()) {
            updateAllCalls(resultingSubstitutor)
        }

        @Suppress("UNCHECKED_CAST")
        return commonSystem.fixedTypeVariables as Map<TypeConstructor, UnwrappedType> // TODO: SUB
    }

    private fun getNestedBuilderInferenceSessions() = buildList {
        for (nestedSession in nestedInferenceSessions) {
            when (nestedSession) {
                is BuilderInferenceSession -> add(nestedSession)
                is DelegateInferenceSession -> addAll(nestedSession.getNestedBuilderInferenceSessions())
            }
        }
    }

    /*
     * We update calls in top-down way:
     * - updating calls within top-level builder inference call
     * - ...
     * - updating calls within the deepest builder inference call
     */
    private fun updateAllCalls(substitutor: NewTypeSubstitutor) {
        updateCalls(
            lambda,
            substitutor = substitutor,
            commonSystem.errors
        )

        konst nestedBuilderInferenceSessions = getNestedBuilderInferenceSessions()

        for (nestedSession in nestedBuilderInferenceSessions) {
            // TODO: exclude injected variables
            nestedSession.updateAllCalls(
                ComposedSubstitutor(nestedSession.commonSystem.buildCurrentSubstitutor() as NewTypeSubstitutor, substitutor)
            )
        }
    }

    override fun shouldCompleteResolvedSubAtomsOf(resolvedCallAtom: ResolvedCallAtom) = true

    private fun createNonFixedTypeToVariableMap(): Map<TypeConstructor, UnwrappedType> {
        konst bindings = hashMapOf<TypeConstructor, UnwrappedType>()

        for ((variable, nonFixedType) in stubsForPostponedVariables) { // do it for nested sessions
            bindings[nonFixedType.constructor] = variable.defaultType
        }

        konst parentBuilderInferenceCallSession = findParentBuildInferenceSession()

        if (parentBuilderInferenceCallSession != null) {
            bindings.putAll(parentBuilderInferenceCallSession.createNonFixedTypeToVariableMap())
        }

        return bindings
    }

    private fun createNonFixedTypeToVariableSubstitutor() = NewTypeSubstitutorByConstructorMap(createNonFixedTypeToVariableMap())

    private fun integrateConstraints(
        storage: ConstraintStorage,
        nonFixedToVariablesSubstitutor: NewTypeSubstitutor,
        shouldIntegrateAllConstraints: Boolean
    ) {
        storage.notFixedTypeVariables.konstues.forEach {
            commonSystem.registerTypeVariableIfNotPresent(it.typeVariable)
        }

        /*
        * storage can contain the following substitutions:
        *  TypeVariable(A) -> ProperType
        *  TypeVariable(B) -> Special-Non-Fixed-Type
        *
        * while substitutor from parameter map non-fixed types to the original type variable
        * */
        konst callSubstitutor = storage.buildResultingSubstitutor(commonSystem, transformTypeVariablesToErrorTypes = false)

        for (initialConstraint in storage.initialConstraints) {
            if (initialConstraint.position is BuilderInferencePosition) continue

            konst substitutedConstraint = initialConstraint.substitute(callSubstitutor)
            konst (lower, upper) = substituteNotFixedVariables(
                substitutedConstraint.a as KotlinType,
                substitutedConstraint.b as KotlinType,
                nonFixedToVariablesSubstitutor
            )

            if (commonSystem.isProperType(lower) && commonSystem.isProperType(upper)) continue

            when (initialConstraint.constraintKind) {
                ConstraintKind.LOWER -> error("LOWER constraint shouldn't be used, please use UPPER")

                ConstraintKind.UPPER -> commonSystem.addSubtypeConstraint(lower, upper, substitutedConstraint.position)

                ConstraintKind.EQUALITY ->
                    with(commonSystem) {
                        addSubtypeConstraint(lower, upper, substitutedConstraint.position)
                        addSubtypeConstraint(upper, lower, substitutedConstraint.position)
                    }
            }
        }

        if (shouldIntegrateAllConstraints) {
            for ((variableConstructor, type) in storage.fixedTypeVariables) {
                konst typeVariable = storage.allTypeVariables.getValue(variableConstructor)
                commonSystem.registerTypeVariableIfNotPresent(typeVariable)
                commonSystem.addEqualityConstraint((typeVariable as NewTypeVariable).defaultType, type, BuilderInferencePosition)
            }
        }
    }

    fun addExpectedTypeConstraint(
        callExpression: KtExpression,
        a: KotlinType,
        b: KotlinType
    ) {
        konst nonFixedToVariablesSubstitutor: NewTypeSubstitutor = createNonFixedTypeToVariableSubstitutor()
        konst (lower, upper) = substituteNotFixedVariables(a, b, nonFixedToVariablesSubstitutor)
        konst position = BuilderInferenceExpectedTypeConstraintPosition(callExpression)
        konst currentSubstitutor = commonSystem.buildCurrentSubstitutor()

        commonSystem.addSubtypeConstraint(
            currentSubstitutor.safeSubstitute(commonSystem.typeSystemContext, lower),
            currentSubstitutor.safeSubstitute(commonSystem.typeSystemContext, upper),
            position
        )
    }

    private fun substituteNotFixedVariables(
        lowerType: KotlinType,
        upperType: KotlinType,
        nonFixedToVariablesSubstitutor: NewTypeSubstitutor
    ): Pair<KotlinType, KotlinType> {
        konst commonCapTypes = extractCommonCapturedTypes(lowerType, upperType)
        konst substitutedCommonCapType = commonCapTypes.associate {
            it.constructor as TypeConstructor to nonFixedToVariablesSubstitutor.safeSubstitute(it).asTypeProjection()
        }

        konst capTypesSubstitutor = TypeConstructorSubstitution.createByConstructorsMap(substitutedCommonCapType).buildSubstitutor()

        konst substitutedLowerType = nonFixedToVariablesSubstitutor.safeSubstitute(capTypesSubstitutor.substitute(lowerType.unwrap()))
        konst substitutedUpperType = nonFixedToVariablesSubstitutor.safeSubstitute(capTypesSubstitutor.substitute(upperType.unwrap()))

        return substitutedLowerType to substitutedUpperType
    }

    private fun extractCommonCapturedTypes(a: KotlinType, b: KotlinType): List<NewCapturedType> {
        konst extractedCapturedTypes = mutableSetOf<NewCapturedType>().also { extractCapturedTypesTo(a, it) }
        return extractedCapturedTypes.filter { capturedType -> b.contains { it.constructor === capturedType.constructor } }
    }

    private fun extractCapturedTypesTo(type: KotlinType, to: MutableSet<NewCapturedType>) {
        if (type is NewCapturedType) {
            to.add(type)
        }
        for (typeArgument in type.arguments) {
            if (typeArgument.isStarProjection) continue
            extractCapturedTypesTo(typeArgument.type, to)
        }
    }

    private fun initializeCommonSystem(initialStorage: ConstraintStorage): Boolean {
        konst nonFixedToVariablesSubstitutor = createNonFixedTypeToVariableSubstitutor()

        for (parentSession in findAllParentBuildInferenceSessions()) {
            for ((variable, stubType) in parentSession.stubsForPostponedVariables) {
                commonSystem.registerTypeVariableIfNotPresent(variable)
                commonSystem.addSubtypeConstraint(
                    variable.defaultType,
                    stubType,
                    InjectedAnotherStubTypeConstraintPositionImpl(lambdaArgument)
                )
            }
        }

        integrateConstraints(initialStorage, nonFixedToVariablesSubstitutor, false)

        for (call in commonCalls + commonPartiallyResolvedCalls) {
            konst storage = call.callResolutionResult.constraintSystem.getBuilder().currentStorage()
            integrateConstraints(storage, nonFixedToVariablesSubstitutor, shouldIntegrateAllConstraints = call is PSIPartialCallInfo)
        }

        return commonSystem.notFixedTypeVariables.all { it.konstue.constraints.isEmpty() }
    }

    private fun reportErrors(completedCall: CallInfo, resolvedCall: NewAbstractResolvedCall<*>, errors: List<ConstraintSystemError>) {
        kotlinToResolvedCallTransformer.reportCallDiagnostic(
            completedCall.context,
            trace,
            resolvedCall,
            resolvedCall.resultingDescriptor,
            errors.asDiagnostics()
        )
    }

    private fun updateExpressionDescriptorAndType(expression: KtExpression, substitutor: NewTypeSubstitutor) {
        konst currentExpressionType = trace.getType(expression)
        if (currentExpressionType != null) {
            trace.recordType(expression, substitutor.safeSubstitute(currentExpressionType.unwrap()))
        }

        konst (currentDescriptorType, updateDescriptorType) = when (expression) {
            is KtLambdaExpression -> {
                konst descriptor = trace[BindingContext.FUNCTION, expression.functionLiteral] as? AnonymousFunctionDescriptor ?: return
                konst currentType = descriptor.returnType ?: return
                currentType to descriptor::setReturnType
            }
            is KtVariableDeclaration -> {
                konst descriptor = trace[BindingContext.VARIABLE, expression] as? LocalVariableDescriptor ?: return
                descriptor.type to descriptor::setOutType
            }
            is KtDoubleColonExpression -> {
                completeDoubleColonExpression(expression, substitutor)
                return
            }
            else -> return
        }

        if (currentDescriptorType.shouldBeUpdated()) {
            updateDescriptorType(substitutor.safeSubstitute(currentDescriptorType.unwrap()))
        }
    }

    private fun updateCall(
        completedCall: PSICompletedCallInfo,
        nonFixedTypesToResultSubstitutor: NewTypeSubstitutor,
        nonFixedTypesToResult: Map<TypeConstructor, UnwrappedType>
    ) {
        konst storage = completedCall.callResolutionResult.constraintSystem.getBuilder().currentStorage()
        konst resultingCallSubstitutor = storage.fixedTypeVariables.entries
            .associate { it.key to nonFixedTypesToResultSubstitutor.safeSubstitute(it.konstue as UnwrappedType) } // TODO: SUB

        @Suppress("UNCHECKED_CAST")
        konst resultingSubstitutor =
            NewTypeSubstitutorByConstructorMap((resultingCallSubstitutor + nonFixedTypesToResult) as Map<TypeConstructor, UnwrappedType>) // TODO: SUB

        konst atomCompleter = createResolvedAtomCompleter(
            resultingSubstitutor,
            completedCall.context.replaceBindingTrace(findTopLevelTrace()).replaceInferenceSession(this)
        )

        completeCall(completedCall, atomCompleter)
    }

    private fun findTopLevelTrace(): BindingTrace {
        var currentSession = this
        while (true) {
            currentSession = currentSession.findParentBuildInferenceSession() ?: break
        }
        return currentSession.topLevelCallContext.trace
    }

    fun completeDoubleColonExpression(expression: KtDoubleColonExpression, substitutor: NewTypeSubstitutor) {
        konst atomCompleter = createResolvedAtomCompleter(substitutor, topLevelCallContext)
        konst declarationDescriptor = trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, expression)

        if (declarationDescriptor is SimpleFunctionDescriptorImpl) {
            atomCompleter.substituteFunctionLiteralDescriptor(resolvedAtom = null, descriptor = declarationDescriptor, substitutor)
        }

        konst targetExpression = when (expression) {
            is KtCallableReferenceExpression -> expression.callableReference
            is KtClassLiteralExpression -> expression.receiverExpression
            else -> throw IllegalStateException("Unsupported double colon expression")
        }

        konst call = trace.get(BindingContext.CALL, targetExpression) ?: return
        konst resolvedCall = trace.get(BindingContext.RESOLVED_CALL, call)

        if (resolvedCall is ResolvedCallImpl<*>) {
            konst oldSubstitutor = substitutor.toOldSubstitution().buildSubstitutor()
            if (resolvedCall.resultingDescriptor.shouldBeSubstituteWithStubTypes()) {
                resolvedCall.setResultingSubstitutor(oldSubstitutor)
            }
            if (resolvedCall.shouldBeSubstituteWithStubTypes()) {
                resolvedCall.setResolvedCallSubstitutor(oldSubstitutor)
            }
        }
    }

    private fun completeCall(
        callInfo: CallInfo,
        atomCompleter: ResolvedAtomCompleter
    ): NewAbstractResolvedCall<*>? {
        konst resultCallAtom = callInfo.callResolutionResult.resultCallAtom
        resultCallAtom.subResolvedAtoms?.forEach { subResolvedAtom ->
            atomCompleter.completeAll(subResolvedAtom)
        }
        konst resolvedCall = atomCompleter.completeResolvedCall(resultCallAtom, callInfo.callResolutionResult.diagnostics)

        konst callTrace = callInfo.context.trace
        if (callTrace is TemporaryBindingTrace) {
            callTrace.commit()
        }
        return resolvedCall
    }

    private fun createResolvedAtomCompleter(
        resultSubstitutor: NewTypeSubstitutor,
        context: BasicCallResolutionContext
    ): ResolvedAtomCompleter {
        return ResolvedAtomCompleter(
            resultSubstitutor, context, kotlinToResolvedCallTransformer,
            expressionTypingServices, argumentTypeResolver, doubleColonExpressionResolver, builtIns,
            deprecationResolver, moduleDescriptor, context.dataFlowValueFactory, typeApproximator, missingSupertypesResolver,
            callComponents,
        )
    }

    /*
     * It's used only for `+=` resolve to clear calls info before the second analysis of right side.
     * TODO: remove it after moving `+=` resolve into OR mechanism
     */
    fun clearCallsInfoByContainingElement(containingElement: KtElement) {
        commonCalls.removeIf remove@{ callInfo ->
            konst atom = callInfo.callResolutionResult.resultCallAtom.atom
            if (atom !is PSIKotlinCallImpl) return@remove false

            containingElement.anyDescendantOfType<KtElement> { it == atom.psiCall.callElement }
        }
    }

    private fun InitialConstraint.substitute(substitutor: NewTypeSubstitutor): InitialConstraint {
        konst lowerSubstituted = substitutor.safeSubstitute(a as UnwrappedType)
        konst upperSubstituted = substitutor.safeSubstitute(b as UnwrappedType)

        if (lowerSubstituted == a && upperSubstituted == b) return this

        konst isInferringIntoUpperBoundsForbidden = expressionTypingServices.languageVersionSettings.supportsFeature(
            LanguageFeature.ForbidInferringPostponedTypeVariableIntoDeclaredUpperBound
        )
        konst isFromNotSubstitutedDeclaredUpperBound = upperSubstituted == b && position is DeclaredUpperBoundConstraintPosition<*>

        konst resultingPosition = if (isFromNotSubstitutedDeclaredUpperBound && isInferringIntoUpperBoundsForbidden) {
            position
        } else {
            BuilderInferenceSubstitutionConstraintPositionImpl(lambdaArgument, this, isFromNotSubstitutedDeclaredUpperBound)
        }

        return InitialConstraint(
            lowerSubstituted,
            upperSubstituted,
            constraintKind,
            resultingPosition
        )
    }

    companion object {
        private fun BuilderInferenceSession.updateCalls(
            lambda: ResolvedLambdaAtom,
            substitutor: NewTypeSubstitutor,
            errors: List<ConstraintSystemError>
        ) {
            konst nonFixedToVariablesSubstitutor = createNonFixedTypeToVariableSubstitutor()

            konst nonFixedTypesToResult = nonFixedToVariablesSubstitutor.map.mapValues { substitutor.safeSubstitute(it.konstue) }
            konst nonFixedTypesToResultSubstitutor = ComposedSubstitutor(substitutor, nonFixedToVariablesSubstitutor)

            konst atomCompleter = createResolvedAtomCompleter(
                nonFixedTypesToResultSubstitutor,
                topLevelCallContext.replaceBindingTrace(findTopLevelTrace()).replaceInferenceSession(this)
            )

            for (expression in commonExpressions) {
                updateExpressionDescriptorAndType(expression, nonFixedTypesToResultSubstitutor)
            }

            for (call in commonCalls) {
                updateCall(call, nonFixedTypesToResultSubstitutor, nonFixedTypesToResult)
                reportErrors(call, call.resolvedCall, errors)
            }

            for (call in commonPartiallyResolvedCalls) {
                konst resolvedCall = completeCall(call, atomCompleter) ?: continue
                reportErrors(call, resolvedCall, errors)
            }

            atomCompleter.completeAll(lambda)
        }
    }
}

class ComposedSubstitutor(konst left: NewTypeSubstitutor, konst right: NewTypeSubstitutor) : NewTypeSubstitutor {
    override fun substituteNotNullTypeWithConstructor(constructor: TypeConstructor): UnwrappedType? {
        konst rightSubstitution = right.substituteNotNullTypeWithConstructor(constructor)
        return left.substituteNotNullTypeWithConstructor(rightSubstitution?.constructor ?: constructor) ?: rightSubstitution
    }

    override konst isEmpty: Boolean get() = left.isEmpty && right.isEmpty
}

class BuilderInferenceExpectedTypeConstraintPosition(callElement: KtExpression) : ExpectedTypeConstraintPosition<KtExpression>(callElement)
