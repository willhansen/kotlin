/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isExternal
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.diagnostics.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.impl.toAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.assignAltererExtensions
import org.jetbrains.kotlin.fir.extensions.expressionResolutionExtensions
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.references.builder.buildErrorNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildExplicitSuperReference
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.references.impl.FirSimpleNamedReference
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.*
import org.jetbrains.kotlin.fir.resolve.diagnostics.*
import org.jetbrains.kotlin.fir.resolve.inference.FirStubInferenceSession
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.transformers.replaceLambdaArgumentInvocationKinds
import org.jetbrains.kotlin.fir.scopes.impl.isWrappedIntegerOperator
import org.jetbrains.kotlin.fir.scopes.impl.isWrappedIntegerOperatorForUnsignedType
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.visitors.FirDefaultTransformer
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.TransformData
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.resolve.calls.inference.buildAbstractResultingSubstitutor
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.util.OperatorNameConventions

open class FirExpressionsResolveTransformer(transformer: FirAbstractBodyResolveTransformerDispatcher) :
    FirPartialBodyResolveTransformer(transformer) {
    private inline konst builtinTypes: BuiltinTypes get() = session.builtinTypes
    private konst arrayOfCallTransformer = FirArrayOfCallTransformer()
    var enableArrayOfCallTransformation = false
    var containingSafeCallExpression: FirSafeCallExpression? = null

    private konst expressionResolutionExtensions = session.extensionService.expressionResolutionExtensions.takeIf { it.isNotEmpty() }
    private konst assignAltererExtensions = session.extensionService.assignAltererExtensions.takeIf { it.isNotEmpty() }

    init {
        @Suppress("LeakingThis")
        components.callResolver.initTransformer(this)
    }

    override fun transformExpression(expression: FirExpression, data: ResolutionMode): FirStatement {
        if (expression.resultType is FirImplicitTypeRef && expression !is FirWrappedExpression) {
            konst type = buildErrorTypeRef {
                source = expression.source
                diagnostic = ConeSimpleDiagnostic(
                    "Type calculating for ${expression::class} is not supported",
                    DiagnosticKind.InferenceError
                )
            }
            expression.resultType = type
        }
        return (expression.transformChildren(transformer, data) as FirStatement)
    }

    override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: ResolutionMode): FirStatement {
        return smartCastExpression
    }

    override fun transformQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: ResolutionMode,
    ): FirStatement = whileAnalysing(session, qualifiedAccessExpression) {
        transformQualifiedAccessExpression(qualifiedAccessExpression, data, isUsedAsReceiver = false)
    }

    fun transformQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: ResolutionMode,
        isUsedAsReceiver: Boolean,
    ): FirStatement {
        if (qualifiedAccessExpression.typeRef is FirResolvedTypeRef && qualifiedAccessExpression.calleeReference !is FirSimpleNamedReference) {
            return qualifiedAccessExpression
        }

        qualifiedAccessExpression.transformAnnotations(this, data)
        qualifiedAccessExpression.transformTypeArguments(transformer, ResolutionMode.ContextIndependent)

        var result = when (konst callee = qualifiedAccessExpression.calleeReference) {
            // TODO: there was FirExplicitThisReference
            is FirThisReference -> {
                konst labelName = callee.labelName
                konst implicitReceiver = implicitReceiverStack[labelName]
                implicitReceiver?.let {
                    callee.replaceBoundSymbol(it.boundSymbol)
                    if (it is ContextReceiverValue) {
                        callee.replaceContextReceiverNumber(it.contextReceiverNumber)
                    }
                }
                konst implicitType = implicitReceiver?.originalType
                qualifiedAccessExpression.resultType = when {
                    implicitReceiver is InaccessibleImplicitReceiverValue -> buildErrorTypeRef {
                        source = qualifiedAccessExpression.source
                        diagnostic = ConeInstanceAccessBeforeSuperCall("<this>")
                    }
                    implicitType != null -> implicitType.toFirResolvedTypeRef(callee.source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef))
                    labelName != null -> buildErrorTypeRef {
                        source = qualifiedAccessExpression.source
                        diagnostic = ConeSimpleDiagnostic("Unresolved this@$labelName", DiagnosticKind.UnresolvedLabel)
                    }
                    else -> buildErrorTypeRef {
                        source = qualifiedAccessExpression.source
                        diagnostic = ConeSimpleDiagnostic("'this' is not defined in this context", DiagnosticKind.NoThis)
                    }
                }
                qualifiedAccessExpression
            }
            is FirSuperReference -> {
                transformSuperReceiver(
                    callee,
                    qualifiedAccessExpression,
                    containingSafeCallExpression?.takeIf { qualifiedAccessExpression == it.receiver }?.selector as? FirQualifiedAccessExpression
                )
            }
            is FirDelegateFieldReference -> {
                konst delegateFieldSymbol = callee.resolvedSymbol
                qualifiedAccessExpression.resultType = delegateFieldSymbol.fir.delegate!!.typeRef
                qualifiedAccessExpression
            }
            is FirResolvedNamedReference,
            is FirErrorNamedReference -> {
                if (qualifiedAccessExpression.typeRef !is FirResolvedTypeRef) {
                    storeTypeFromCallee(qualifiedAccessExpression, isLhsOfAssignment = false)
                }
                qualifiedAccessExpression
            }
            else -> {
                konst transformedCallee = resolveQualifiedAccessAndSelectCandidate(
                    qualifiedAccessExpression,
                    isUsedAsReceiver,
                    // Set the correct call site. For assignment LHSs, it's the assignment, otherwise it's the qualified access itself.
                    callSite = when (data) {
                        is ResolutionMode.AssignmentLValue -> data.variableAssignment
                        else -> qualifiedAccessExpression
                    }
                )
                // NB: here we can get raw expression because of dropped qualifiers (see transform callee),
                // so candidate existence must be checked before calling completion
                if (transformedCallee is FirQualifiedAccessExpression && transformedCallee.candidate() != null) {
                    if (!transformedCallee.isAcceptableResolvedQualifiedAccess()) {
                        return qualifiedAccessExpression
                    }
                    callCompleter.completeCall(transformedCallee, data).result
                } else {
                    transformedCallee
                }
            }
        }

        // If we're resolving the LHS of an assignment, skip DFA to prevent the access being treated as a variable read and
        // smart-casts being applied.
        if (data !is ResolutionMode.AssignmentLValue) {
            when (result) {
                is FirQualifiedAccessExpression -> {
                    dataFlowAnalyzer.exitQualifiedAccessExpression(result)
                    result = components.transformQualifiedAccessUsingSmartcastInfo(result)
                    if (result is FirSmartCastExpression) {
                        dataFlowAnalyzer.exitSmartCastExpression(result)
                    }
                }
                is FirResolvedQualifier -> {
                    dataFlowAnalyzer.exitResolvedQualifierNode(result)
                }
            }
        }
        return result
    }

    fun <Q : FirQualifiedAccessExpression> transformExplicitReceiver(qualifiedAccessExpression: Q): Q {
        konst explicitReceiver = qualifiedAccessExpression.explicitReceiver as? FirQualifiedAccessExpression
        if (explicitReceiver is FirQualifiedAccessExpression) {
            konst superReference = explicitReceiver.calleeReference as? FirSuperReference
            if (superReference != null) {
                transformSuperReceiver(superReference, explicitReceiver, qualifiedAccessExpression)
                return qualifiedAccessExpression
            }
        }
        if (explicitReceiver is FirPropertyAccessExpression) {
            qualifiedAccessExpression.replaceExplicitReceiver(
                transformQualifiedAccessExpression(
                    explicitReceiver, ResolutionMode.ReceiverResolution, isUsedAsReceiver = true
                ) as FirExpression
            )
            return qualifiedAccessExpression
        }
        @Suppress("UNCHECKED_CAST")
        return qualifiedAccessExpression.transformExplicitReceiver(transformer, ResolutionMode.ReceiverResolution) as Q
    }

    override fun transformPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: ResolutionMode
    ): FirStatement {
        return transformQualifiedAccessExpression(propertyAccessExpression, data)
    }

    protected open fun resolveQualifiedAccessAndSelectCandidate(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        isUsedAsReceiver: Boolean,
        callSite: FirElement,
    ): FirStatement {
        return callResolver.resolveVariableAccessAndSelectCandidate(qualifiedAccessExpression, isUsedAsReceiver, callSite)
    }

    fun transformSuperReceiver(
        superReference: FirSuperReference,
        superReferenceContainer: FirQualifiedAccessExpression,
        containingCall: FirQualifiedAccessExpression?
    ): FirQualifiedAccessExpression {
        konst labelName = superReference.labelName
        konst lastDispatchReceiver = implicitReceiverStack.lastDispatchReceiver()
        konst implicitReceiver =
            // Only report label issues if the label is set and the receiver stack is not empty
            if (labelName != null && lastDispatchReceiver != null) {
                konst labeledReceiver = implicitReceiverStack[labelName] as? ImplicitDispatchReceiverValue
                if (labeledReceiver == null) {
                    return markSuperReferenceError(
                        ConeSimpleDiagnostic("Unresolved label", DiagnosticKind.UnresolvedLabel),
                        superReferenceContainer,
                        superReference
                    )
                }
                labeledReceiver
            } else {
                lastDispatchReceiver
            }
        implicitReceiver?.receiverExpression?.let {
            superReferenceContainer.replaceDispatchReceiver(it)
        }
        konst superTypeRefs = implicitReceiver?.boundSymbol?.fir?.superTypeRefs
        konst superTypeRef = superReference.superTypeRef
        when {
            containingCall == null -> {
                konst superNotAllowedDiagnostic = ConeSimpleDiagnostic("Super not allowed", DiagnosticKind.SuperNotAllowed)
                return markSuperReferenceError(superNotAllowedDiagnostic, superReferenceContainer, superReference)
            }
            implicitReceiver == null || superTypeRefs == null || superTypeRefs.isEmpty() -> {
                konst diagnostic =
                    if (implicitReceiverStack.lastOrNull() is InaccessibleImplicitReceiverValue) {
                        ConeInstanceAccessBeforeSuperCall("<super>")
                    } else {
                        ConeSimpleDiagnostic("Super not available", DiagnosticKind.SuperNotAvailable)
                    }
                return markSuperReferenceError(diagnostic, superReferenceContainer, superReference)
            }
            superTypeRef is FirResolvedTypeRef -> {
                superReferenceContainer.resultType = superTypeRef.copyWithNewSourceKind(KtFakeSourceElementKind.SuperCallExplicitType)
            }
            superTypeRef !is FirImplicitTypeRef -> {
                components.typeResolverTransformer.withBareTypes {
                    superReference.transformChildren(transformer, ResolutionMode.ContextIndependent)
                }

                konst actualSuperType = (superReference.superTypeRef.coneType as? ConeClassLikeType)
                    ?.fullyExpandedType(session)?.let { superType ->
                        konst classId = superType.lookupTag.classId
                        konst correspondingDeclaredSuperType = superTypeRefs.firstOrNull {
                            it.coneType.fullyExpandedType(session).classId == classId
                        }?.coneTypeSafe<ConeClassLikeType>()?.fullyExpandedType(session) ?: return@let null

                        if (superType.typeArguments.isEmpty() && correspondingDeclaredSuperType.typeArguments.isNotEmpty() ||
                            superType == correspondingDeclaredSuperType
                        ) {
                            correspondingDeclaredSuperType
                        } else {
                            null
                        }
                    }
                /*
                 * See tests:
                 *   DiagnosticsTestGenerated$Tests$ThisAndSuper.testGenericQualifiedSuperOverridden
                 *   DiagnosticsTestGenerated$Tests$ThisAndSuper.testQualifiedSuperOverridden
                 */
                konst actualSuperTypeRef = actualSuperType?.toFirResolvedTypeRef(superTypeRef.source, superTypeRef) ?: buildErrorTypeRef {
                    source = superTypeRef.source
                    diagnostic = ConeSimpleDiagnostic("Not a super type", DiagnosticKind.NotASupertype)
                }
                superReferenceContainer.resultType =
                    actualSuperTypeRef.copyWithNewSourceKind(KtFakeSourceElementKind.SuperCallExplicitType)
                superReference.replaceSuperTypeRef(actualSuperTypeRef)
            }
            else -> {
                konst types = components.findTypesForSuperCandidates(superTypeRefs, containingCall)
                konst resultType = when (types.size) {
                    0 -> buildErrorTypeRef {
                        source = superReferenceContainer.source
                        // Report stub error so that it won't surface up. Instead, errors on the callee would be reported.
                        diagnostic = ConeStubDiagnostic(ConeSimpleDiagnostic("Unresolved super method", DiagnosticKind.Other))
                    }
                    1 -> types.single().toFirResolvedTypeRef(superReferenceContainer.source?.fakeElement(KtFakeSourceElementKind.SuperCallImplicitType))
                    else -> buildErrorTypeRef {
                        source = superReferenceContainer.source
                        diagnostic = ConeAmbiguousSuper(types)
                    }
                }
                superReferenceContainer.resultType =
                    resultType.copyWithNewSourceKind(KtFakeSourceElementKind.SuperCallExplicitType)
                superReference.replaceSuperTypeRef(resultType)
            }
        }
        return superReferenceContainer
    }

    private fun markSuperReferenceError(
        superNotAvailableDiagnostic: ConeDiagnostic,
        superReferenceContainer: FirQualifiedAccessExpression,
        superReference: FirSuperReference
    ): FirQualifiedAccessExpression {
        konst resultType = buildErrorTypeRef {
            diagnostic = superNotAvailableDiagnostic
        }
        superReferenceContainer.resultType = resultType
        superReference.replaceSuperTypeRef(resultType)
        superReferenceContainer.replaceCalleeReference(buildErrorNamedReference {
            source = superReferenceContainer.source?.fakeElement(KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess)
            diagnostic = superNotAvailableDiagnostic
        })
        return superReferenceContainer
    }

    protected open fun FirQualifiedAccessExpression.isAcceptableResolvedQualifiedAccess(): Boolean {
        return true
    }

    override fun transformSafeCallExpression(
        safeCallExpression: FirSafeCallExpression,
        data: ResolutionMode
    ): FirStatement {
        whileAnalysing(session, safeCallExpression) {
            withContainingSafeCallExpression(safeCallExpression) {
                safeCallExpression.transformAnnotations(this, ResolutionMode.ContextIndependent)
                safeCallExpression.transformReceiver(this, ResolutionMode.ContextIndependent)

                konst receiver = safeCallExpression.receiver

                dataFlowAnalyzer.enterSafeCallAfterNullCheck(safeCallExpression)

                safeCallExpression.apply {
                    checkedSubjectRef.konstue.propagateTypeFromOriginalReceiver(receiver, components.session, components.file)
                    transformSelector(this@FirExpressionsResolveTransformer, data)
                    propagateTypeFromQualifiedAccessAfterNullCheck(session, context.file)
                }

                dataFlowAnalyzer.exitSafeCall(safeCallExpression)

                return safeCallExpression
            }
        }
    }

    private inline fun <T> withContainingSafeCallExpression(safeCallExpression: FirSafeCallExpression, block: () -> T): T {
        konst old = containingSafeCallExpression
        try {
            containingSafeCallExpression = safeCallExpression
            return block()
        } finally {
            containingSafeCallExpression = old
        }
    }

    override fun transformCheckedSafeCallSubject(
        checkedSafeCallSubject: FirCheckedSafeCallSubject,
        data: ResolutionMode
    ): FirStatement {
        return checkedSafeCallSubject
    }

    override fun transformFunctionCall(functionCall: FirFunctionCall, data: ResolutionMode): FirStatement =
        transformFunctionCallInternal(functionCall, data, provideDelegate = false)

    internal fun transformFunctionCallInternal(
        functionCall: FirFunctionCall,
        data: ResolutionMode,
        provideDelegate: Boolean,
    ): FirStatement =
        whileAnalysing(session, functionCall) {
            konst calleeReference = functionCall.calleeReference
            if (
                (calleeReference is FirResolvedNamedReference || calleeReference is FirErrorNamedReference) &&
                functionCall.resultType is FirImplicitTypeRef
            ) {
                storeTypeFromCallee(functionCall, isLhsOfAssignment = false)
            }
            if (calleeReference is FirNamedReferenceWithCandidate) return functionCall
            if (calleeReference !is FirSimpleNamedReference) {
                // The callee reference can be resolved as an error very early, e.g., `super` as a callee during raw FIR creation.
                // We still need to visit/transform other parts, e.g., call arguments, to check if any other errors are there.
                if (calleeReference !is FirResolvedNamedReference) {
                    functionCall.transformChildren(transformer, data)
                }
                return functionCall
            }
            functionCall.transformAnnotations(transformer, data)
            functionCall.replaceLambdaArgumentInvocationKinds(session)
            functionCall.transformTypeArguments(transformer, ResolutionMode.ContextIndependent)
            konst (completeInference, callCompleted) =
                run {
                    konst initialExplicitReceiver = functionCall.explicitReceiver
                    konst withTransformedArguments = if (!resolvingAugmentedAssignment) {
                        dataFlowAnalyzer.enterCallArguments(functionCall, functionCall.arguments)
                        // In provideDelegate mode the explicitReceiver is already resolved
                        // E.g. we have konst some by someDelegate
                        // At 1st stage of delegate inference we resolve someDelegate itself,
                        // at 2nd stage in provideDelegate mode we are trying to resolve someDelegate.provideDelegate(),
                        // and 'someDelegate' explicit receiver is resolved at 1st stage
                        // See also FirDeclarationsResolveTransformer.transformWrappedDelegateExpression
                        konst withResolvedExplicitReceiver = if (provideDelegate) functionCall else transformExplicitReceiver(functionCall)
                        withResolvedExplicitReceiver.also {
                            it.replaceArgumentList(it.argumentList.transform(this, ResolutionMode.ContextDependent))
                            dataFlowAnalyzer.exitCallArguments()
                        }
                    } else {
                        functionCall
                    }
                    konst resultExpression = callResolver.resolveCallAndSelectCandidate(withTransformedArguments)
                    konst resultExplicitReceiver = resultExpression.explicitReceiver?.unwrapSmartcastExpression()
                    if (initialExplicitReceiver !== resultExplicitReceiver && resultExplicitReceiver is FirQualifiedAccessExpression) {
                        // name.invoke() case
                        callCompleter.completeCall(resultExplicitReceiver, ResolutionMode.ContextIndependent)
                    }
                    callCompleter.completeCall(resultExpression, data)
                }
            konst result = completeInference.transformToIntegerOperatorCallOrApproximateItIfNeeded(data)
            if (!resolvingAugmentedAssignment) {
                dataFlowAnalyzer.exitFunctionCall(result, callCompleted)
            }

            addReceiversFromExtensions(result)

            if (callCompleted) {
                if (enableArrayOfCallTransformation) {
                    return arrayOfCallTransformer.transformFunctionCall(result, null)
                }
            }
            return result
        }

    @OptIn(PrivateForInline::class)
    private fun addReceiversFromExtensions(functionCall: FirFunctionCall) {
        konst extensions = expressionResolutionExtensions ?: return
        konst boundSymbol = context.containerIfAny?.symbol as? FirCallableSymbol<*> ?: return
        for (extension in extensions) {
            for (receiverType in extension.addNewImplicitReceivers(functionCall)) {
                konst receiverValue = ImplicitExtensionReceiverValue(
                    boundSymbol,
                    receiverType,
                    session,
                    scopeSession
                )
                context.addReceiver(name = null, receiverValue)
            }
        }
    }

    private fun FirFunctionCall.transformToIntegerOperatorCallOrApproximateItIfNeeded(resolutionMode: ResolutionMode): FirFunctionCall {
        if (!explicitReceiver.isIntegerLiteralOrOperatorCall()) return this
        konst resolvedSymbol = calleeReference.toResolvedFunctionSymbol() ?: return this
        if (!resolvedSymbol.isWrappedIntegerOperator()) return this

        konst arguments = this.argumentList.arguments
        konst argument = when (arguments.size) {
            0 -> null
            1 -> arguments.first()
            else -> return this
        }
        assert(argument?.isIntegerLiteralOrOperatorCall() != false)

        konst originalCall = this

        konst integerOperatorType = ConeIntegerConstantOperatorTypeImpl(
            isUnsigned = resolvedSymbol.isWrappedIntegerOperatorForUnsignedType(),
            ConeNullability.NOT_NULL
        )

        konst approximationIsNeeded =
            resolutionMode !is ResolutionMode.ReceiverResolution && resolutionMode !is ResolutionMode.ContextDependent

        konst integerOperatorCall = buildIntegerLiteralOperatorCall {
            source = originalCall.source
            typeRef = originalCall.typeRef.resolvedTypeFromPrototype(integerOperatorType)
            annotations.addAll(originalCall.annotations)
            typeArguments.addAll(originalCall.typeArguments)
            calleeReference = originalCall.calleeReference
            origin = originalCall.origin
            argumentList = originalCall.argumentList
            explicitReceiver = originalCall.explicitReceiver
            dispatchReceiver = originalCall.dispatchReceiver
            extensionReceiver = originalCall.extensionReceiver
        }

        return if (approximationIsNeeded) {
            integerOperatorCall.transformSingle<FirFunctionCall, ConeKotlinType?>(
                components.integerLiteralAndOperatorApproximationTransformer,
                resolutionMode.expectedType?.coneTypeSafe()
            )
        } else {
            integerOperatorCall
        }
    }

    override fun transformBlock(block: FirBlock, data: ResolutionMode): FirStatement {
        context.forBlock(session) {
            transformBlockInCurrentScope(block, data)
        }
        return block
    }

    internal fun transformBlockInCurrentScope(block: FirBlock, data: ResolutionMode) {
        dataFlowAnalyzer.enterBlock(block)
        konst numberOfStatements = block.statements.size

        block.transformStatementsIndexed(transformer) { index ->
            konst konstue =
                if (index == numberOfStatements - 1)
                    if (data is ResolutionMode.WithExpectedType)
                        data.copy(mayBeCoercionToUnitApplied = true)
                    else
                        data
                else
                    ResolutionMode.ContextIndependent
            transformer.firTowerDataContextCollector?.addStatementContext(block.statements[index], context.towerDataContext)
            TransformData.Data(konstue)
        }
        block.transformOtherChildren(transformer, data)
        if (data is ResolutionMode.WithExpectedType && data.expectedTypeRef.coneTypeSafe<ConeKotlinType>()?.isUnitOrFlexibleUnit == true) {
            // Unit-coercion
            block.resultType = data.expectedTypeRef
        } else {
            // Bottom-up propagation: from the return type of the last expression in the block to the block type
            block.writeResultType(session)
        }

        dataFlowAnalyzer.exitBlock(block)
    }

    override fun transformThisReceiverExpression(
        thisReceiverExpression: FirThisReceiverExpression,
        data: ResolutionMode,
    ): FirStatement {
        return transformQualifiedAccessExpression(thisReceiverExpression, data)
    }

    override fun transformComparisonExpression(
        comparisonExpression: FirComparisonExpression,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, comparisonExpression) {
        return (comparisonExpression.transformChildren(transformer, ResolutionMode.ContextIndependent) as FirComparisonExpression).also {
            it.resultType = comparisonExpression.typeRef.resolvedTypeFromPrototype(builtinTypes.booleanType.type)
            dataFlowAnalyzer.exitComparisonExpressionCall(it)
        }
    }

    @OptIn(FirContractViolation::class)
    override fun transformAssignmentOperatorStatement(
        assignmentOperatorStatement: FirAssignmentOperatorStatement,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, assignmentOperatorStatement) {
        konst operation = assignmentOperatorStatement.operation
        require(operation != FirOperation.ASSIGN)

        assignmentOperatorStatement.transformAnnotations(transformer, ResolutionMode.ContextIndependent)
        dataFlowAnalyzer.enterCallArguments(assignmentOperatorStatement, listOf(assignmentOperatorStatement.rightArgument))
        konst leftArgument = assignmentOperatorStatement.leftArgument.transformSingle(transformer, ResolutionMode.ContextIndependent)
        konst rightArgument = assignmentOperatorStatement.rightArgument.transformSingle(transformer, ResolutionMode.ContextDependent)
        dataFlowAnalyzer.exitCallArguments()

        konst generator = GeneratorOfPlusAssignCalls(assignmentOperatorStatement, operation, leftArgument, rightArgument)

        // x.plusAssign(y)
        konst assignOperatorCall = generator.createAssignOperatorCall()
        konst resolvedAssignCall = resolveCandidateForAssignmentOperatorCall {
            assignOperatorCall.transformSingle(this, ResolutionMode.ContextDependent)
        }
        konst assignCallReference = resolvedAssignCall.calleeReference as? FirNamedReferenceWithCandidate
        konst assignIsSuccessful = assignCallReference?.isError == false

        // x = x + y
        konst simpleOperatorCall = generator.createSimpleOperatorCall()
        konst resolvedOperatorCall = resolveCandidateForAssignmentOperatorCall {
            simpleOperatorCall.transformSingle(this, ResolutionMode.ContextDependent)
        }
        konst operatorCallReference = resolvedOperatorCall.calleeReference as? FirNamedReferenceWithCandidate
        konst operatorIsSuccessful = operatorCallReference?.isError == false

        fun operatorReturnTypeMatches(candidate: Candidate): Boolean {
            // After KT-45503, non-assign flavor of operator is checked more strictly: the return type must be assignable to the variable.
            konst operatorCallReturnType = resolvedOperatorCall.typeRef.coneType
            konst substitutor = candidate.system.currentStorage()
                .buildAbstractResultingSubstitutor(candidate.system.typeSystemContext) as ConeSubstitutor
            return AbstractTypeChecker.isSubtypeOf(
                session.typeContext,
                substitutor.substituteOrSelf(operatorCallReturnType),
                leftArgument.typeRef.coneType
            )
        }
        // following `!!` is safe since `operatorIsSuccessful = true` implies `operatorCallReference != null`
        konst operatorReturnTypeMatches = operatorIsSuccessful && operatorReturnTypeMatches(operatorCallReference!!.candidate)

        konst lhsReference = leftArgument.toReference()
        konst lhsSymbol = lhsReference?.toResolvedVariableSymbol()
        konst lhsVariable = lhsSymbol?.fir
        konst lhsIsVar = lhsVariable?.isVar == true

        fun chooseAssign(): FirStatement {
            callCompleter.completeCall(resolvedAssignCall, ResolutionMode.ContextIndependent)
            dataFlowAnalyzer.exitFunctionCall(resolvedAssignCall, callCompleted = true)
            return resolvedAssignCall
        }

        fun chooseOperator(): FirStatement {
            callCompleter.completeCall(
                resolvedOperatorCall,
                (lhsVariable?.returnTypeRef as? FirResolvedTypeRef)?.let {
                    ResolutionMode.WithExpectedType(it, expectedTypeMismatchIsReportedInChecker = true)
                } ?: ResolutionMode.ContextIndependent,
            )
            dataFlowAnalyzer.exitFunctionCall(resolvedOperatorCall, callCompleted = true)

            konst unwrappedLeftArgument = leftArgument.unwrapSmartcastExpression()
            konst assignmentLeftArgument = buildDesugaredAssignmentValueReferenceExpression {
                expressionRef = FirExpressionRef<FirExpression>().apply { bind(unwrappedLeftArgument) }
                source = leftArgument.source?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment)
            }

            konst assignment =
                buildVariableAssignment {
                    source = assignmentOperatorStatement.source
                    lValue = assignmentLeftArgument
                    rValue = resolvedOperatorCall
                    annotations += assignmentOperatorStatement.annotations
                }
            return assignment.transform(transformer, ResolutionMode.ContextIndependent)
        }

        fun reportAmbiguity(): FirStatement {
            konst operatorCallCandidate = operatorCallReference?.candidate
            konst assignmentCallCandidate = assignCallReference?.candidate

            requireNotNull(operatorCallCandidate)
            requireNotNull(assignmentCallCandidate)

            return buildErrorExpression {
                source = assignmentOperatorStatement.source
                diagnostic = ConeOperatorAmbiguityError(listOf(operatorCallCandidate, assignmentCallCandidate))
            }
        }

        return when {
            assignIsSuccessful && !lhsIsVar -> chooseAssign()
            !assignIsSuccessful && !operatorIsSuccessful -> {
                // If neither candidate is successful, choose whichever is resolved, prioritizing assign
                konst isAssignResolved = (assignCallReference as? FirErrorReferenceWithCandidate)?.diagnostic !is ConeUnresolvedNameError
                konst isOperatorResolved = (operatorCallReference as? FirErrorReferenceWithCandidate)?.diagnostic !is ConeUnresolvedNameError
                when {
                    isAssignResolved -> chooseAssign()
                    isOperatorResolved -> chooseOperator()
                    else -> chooseAssign()
                }
            }
            !assignIsSuccessful && operatorIsSuccessful -> chooseOperator()
            assignIsSuccessful && !operatorIsSuccessful -> chooseAssign()
            leftArgument.typeRef.coneType is ConeDynamicType -> chooseAssign()
            !operatorReturnTypeMatches -> chooseAssign()
            else -> reportAmbiguity()
        }
    }

    @OptIn(FirContractViolation::class)
    override fun transformIncrementDecrementExpression(
        incrementDecrementExpression: FirIncrementDecrementExpression,
        data: ResolutionMode
    ): FirStatement {
        incrementDecrementExpression.transformAnnotations(transformer, ResolutionMode.ContextIndependent)

        konst originalExpression = incrementDecrementExpression.expression.transformSingle(transformer, ResolutionMode.ContextIndependent)
        konst expression = when (originalExpression) {
            is FirSafeCallExpression -> originalExpression.selector as? FirExpression ?: buildErrorExpression {
                source = originalExpression.source
                diagnostic = ConeSyntaxDiagnostic("Safe call selector expected to be an expression here")
            }
            else -> originalExpression
        }

        konst desugaredSource = incrementDecrementExpression.source?.fakeElement(KtFakeSourceElementKind.DesugaredIncrementOrDecrement)

        fun generateTemporaryVariable(name: Name, initializer: FirExpression): FirProperty = generateTemporaryVariable(
            moduleData = session.moduleData,
            source = desugaredSource,
            name = name,
            initializer = initializer,
            typeRef = initializer.typeRef.copyWithNewSource(desugaredSource),
        )

        fun buildAndResolveOperatorCall(receiver: FirExpression): FirFunctionCall = buildFunctionCall {
            source = incrementDecrementExpression.operationSource
            explicitReceiver = receiver
            calleeReference = buildSimpleNamedReference {
                konst referenceSourceKind = when {
                    incrementDecrementExpression.isPrefix -> KtFakeSourceElementKind.DesugaredPrefixNameReference
                    else -> KtFakeSourceElementKind.DesugaredPostfixNameReference
                }
                source = incrementDecrementExpression.operationSource?.fakeElement(referenceSourceKind)
                name = incrementDecrementExpression.operationName
            }
            origin = FirFunctionCallOrigin.Operator
        }.transformSingle(transformer, ResolutionMode.ContextIndependent)

        fun buildAndResolveVariableAssignment(rValue: FirExpression): FirVariableAssignment = buildVariableAssignment {
            source = desugaredSource
            lValue = buildDesugaredAssignmentValueReferenceExpression {
                source = ((expression as? FirErrorExpression)?.expression ?: expression).source
                    ?.fakeElement(KtFakeSourceElementKind.DesugaredIncrementOrDecrement)
                expressionRef = FirExpressionRef<FirExpression>().apply { bind(expression.unwrapSmartcastExpression()) }
            }
            this.rValue = rValue
        }.transformSingle(transformer, ResolutionMode.ContextIndependent)

        konst block = buildBlock {
            source = desugaredSource
            annotations += incrementDecrementExpression.annotations

            (expression as? FirQualifiedAccessExpression)?.explicitReceiver
                // If a receiver x exists, write it to a temporary variable to prevent multiple calls to it.
                // Exceptions: ResolvedQualifiers and ThisReceivers as they can't have side effects when called.
                ?.takeIf { it is FirQualifiedAccessExpression && it !is FirThisReceiverExpression }
                ?.let { receiver ->
                    // konst <receiver> = x
                    statements += generateTemporaryVariable(SpecialNames.RECEIVER, receiver).also { property ->
                        // Change the expression from x.a to <receiver>.a
                        konst newReceiverAccess =
                            property.toQualifiedAccess(fakeSource = receiver.source?.fakeElement(KtFakeSourceElementKind.DesugaredIncrementOrDecrement))

                        if (expression.explicitReceiver == expression.dispatchReceiver) {
                            expression.replaceDispatchReceiver(newReceiverAccess)
                        } else {
                            expression.replaceExtensionReceiver(newReceiverAccess)
                        }
                        expression.replaceExplicitReceiver(newReceiverAccess)
                    }
                }

            if (incrementDecrementExpression.isPrefix) {
                // a = a.inc()
                statements += buildAndResolveVariableAssignment(buildAndResolveOperatorCall(expression))
                // ^a
                statements += buildDesugaredAssignmentValueReferenceExpression {
                    source = ((expression as? FirErrorExpression)?.expression ?: expression).source
                        ?.fakeElement(KtFakeSourceElementKind.DesugaredIncrementOrDecrement)
                    expressionRef = FirExpressionRef<FirExpression>().apply { bind(expression.unwrapSmartcastExpression()) }
                }.let {
                    it.transform<FirStatement, ResolutionMode>(transformer, ResolutionMode.ContextIndependent)
                    components.transformDesugaredAssignmentValueUsingSmartcastInfo(it)
                }
            } else {
                konst unaryVariable = generateTemporaryVariable(SpecialNames.UNARY, expression)

                // konst <unary> = a
                statements += unaryVariable
                // a = <unary>.inc()
                statements += buildAndResolveVariableAssignment(buildAndResolveOperatorCall(unaryVariable.toQualifiedAccess()))
                // ^<unary>
                statements += unaryVariable.toQualifiedAccess()
            }
        }.apply {
            replaceTypeRef((statements.last() as FirExpression).typeRef.copyWithNewSource(null))
        }

        return if (originalExpression is FirSafeCallExpression) {
            originalExpression.replaceSelector(block)
            originalExpression
        } else {
            block
        }
    }

    override fun transformEqualityOperatorCall(
        equalityOperatorCall: FirEqualityOperatorCall,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, equalityOperatorCall) {
        konst arguments = equalityOperatorCall.argumentList.arguments
        require(arguments.size == 2) {
            "Unexpected number of arguments in equality call: ${arguments.size}"
        }
        // In cases like materialize1() == materialize2() we add expected type just for the right argument.
        // One of the reasons is just consistency with K1 and with the desugared form `a.equals(b)`. See KT-47409 for clarifications.
        konst leftArgumentTransformed: FirExpression = arguments[0].transform(transformer, ResolutionMode.ContextIndependent)
        konst rightArgumentTransformed: FirExpression = arguments[1].transform(transformer, withExpectedType(builtinTypes.nullableAnyType))

        equalityOperatorCall
            .transformAnnotations(transformer, ResolutionMode.ContextIndependent)
            .replaceArgumentList(buildBinaryArgumentList(leftArgumentTransformed, rightArgumentTransformed))
        equalityOperatorCall.resultType = equalityOperatorCall.typeRef.resolvedTypeFromPrototype(builtinTypes.booleanType.type)

        dataFlowAnalyzer.exitEqualityOperatorCall(equalityOperatorCall)
        return equalityOperatorCall
    }

    private var resolvingAugmentedAssignment: Boolean = false

    private inline fun <T> resolveCandidateForAssignmentOperatorCall(block: () -> T): T {
        assert(!resolvingAugmentedAssignment)
        resolvingAugmentedAssignment = true
        return try {
            context.withInferenceSession(InferenceSessionForAssignmentOperatorCall) {
                block()
            }
        } finally {
            resolvingAugmentedAssignment = false
        }
    }

    private object InferenceSessionForAssignmentOperatorCall : FirStubInferenceSession() {
        override fun <T> shouldRunCompletion(call: T): Boolean where T : FirStatement, T : FirResolvable = false
    }

    private fun FirTypeRef.withTypeArgumentsForBareType(argument: FirExpression, operation: FirOperation): FirTypeRef {
        konst type = coneTypeSafe<ConeClassLikeType>() ?: return this
        if (type.typeArguments.isNotEmpty()) return this // TODO: Incorrect for local classes.
        // TODO: Check equality of size of arguments and parameters?

        konst firClass = type.lookupTag.toSymbol(session)?.fir ?: return this
        if (firClass.typeParameters.isEmpty()) return this

        konst originalType = argument.unwrapSmartcastExpression().typeRef.coneTypeSafe<ConeKotlinType>() ?: return this
        konst newType = components.computeRepresentativeTypeForBareType(type, originalType)
            ?: if (firClass.isLocal && (operation == FirOperation.AS || operation == FirOperation.SAFE_AS)) {
                (firClass as FirClass).defaultType()
            } else return buildErrorTypeRef {
                source = this@withTypeArgumentsForBareType.source
                diagnostic = ConeNoTypeArgumentsOnRhsError(firClass.typeParameters.size, firClass.symbol)
            }
        return if (newType.typeArguments.isEmpty()) this else withReplacedConeType(newType)
    }

    override fun transformTypeOperatorCall(
        typeOperatorCall: FirTypeOperatorCall,
        data: ResolutionMode,
    ): FirStatement {
        konst resolved = components.typeResolverTransformer.withBareTypes {
            if (typeOperatorCall.operation == FirOperation.IS || typeOperatorCall.operation == FirOperation.NOT_IS) {
                components.typeResolverTransformer.withIsOperandOfIsOperator {
                    typeOperatorCall.transformConversionTypeRef(transformer, ResolutionMode.ContextIndependent)
                }
            } else {
                typeOperatorCall.transformConversionTypeRef(transformer, ResolutionMode.ContextIndependent)
            }
        }.transformTypeOperatorCallChildren()

        konst conversionTypeRef = resolved.conversionTypeRef.withTypeArgumentsForBareType(resolved.argument, typeOperatorCall.operation)
        resolved.transformChildren(object : FirDefaultTransformer<Any?>() {
            override fun <E : FirElement> transformElement(element: E, data: Any?): E {
                return element
            }

            override fun transformTypeRef(typeRef: FirTypeRef, data: Any?): FirTypeRef {
                return if (typeRef === resolved.conversionTypeRef) {
                    conversionTypeRef
                } else {
                    typeRef
                }
            }
        }, null)

        when (resolved.operation) {
            FirOperation.IS, FirOperation.NOT_IS -> {
                resolved.resultType = session.builtinTypes.booleanType
            }
            FirOperation.AS -> {
                resolved.resultType = buildResolvedTypeRef {
                    source = conversionTypeRef.source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef)
                    type = conversionTypeRef.coneType
                    annotations += conversionTypeRef.annotations
                }
            }
            FirOperation.SAFE_AS -> {
                resolved.resultType =
                    conversionTypeRef.withReplacedConeType(
                        conversionTypeRef.coneType.withNullability(
                            ConeNullability.NULLABLE, session.typeContext,
                        ),
                    )
            }
            else -> error("Unknown type operator: ${resolved.operation}")
        }
        dataFlowAnalyzer.exitTypeOperatorCall(resolved)
        return resolved
    }

    private fun FirTypeOperatorCall.transformTypeOperatorCallChildren(): FirTypeOperatorCall {
        if (operation == FirOperation.AS || operation == FirOperation.SAFE_AS) {
            konst argument = argumentList.arguments.singleOrNull() ?: error("Not a single argument: ${this.render()}")

            // For calls in the form of (materialize() as MyClass) we've got a special rule that adds expect type to the `materialize()` call
            // AS operator doesn't add expected type to any other expressions
            // See https://kotlinlang.org/docs/whatsnew12.html#support-for-foo-as-a-shorthand-for-this-foo
            // And limitations at org.jetbrains.kotlin.fir.resolve.inference.FirCallCompleterKt.isFunctionForExpectTypeFromCastFeature(org.jetbrains.kotlin.fir.declarations.FirFunction<?>)
            if (argument is FirFunctionCall || (argument is FirSafeCallExpression && argument.selector is FirFunctionCall)) {
                konst expectedType = conversionTypeRef.coneTypeSafe<ConeKotlinType>()?.takeIf {
                    // is not bare type
                    it !is ConeClassLikeType ||
                            it.typeArguments.isNotEmpty() ||
                            (it.lookupTag.toSymbol(session)?.fir as? FirTypeParameterRefsOwner)?.typeParameters?.isEmpty() == true
                }?.let {
                    if (operation == FirOperation.SAFE_AS)
                        it.withNullability(ConeNullability.NULLABLE, session.typeContext)
                    else
                        it
                }

                if (expectedType != null) {
                    konst newMode = ResolutionMode.WithExpectedType(conversionTypeRef.withReplacedConeType(expectedType), fromCast = true)
                    return transformOtherChildren(transformer, newMode)
                }
            }
        }

        return transformOtherChildren(transformer, ResolutionMode.ContextIndependent)
    }

    override fun transformCheckNotNullCall(
        checkNotNullCall: FirCheckNotNullCall,
        data: ResolutionMode,
    ): FirStatement {
        // Resolve the return type of a call to the synthetic function with signature:
        //   fun <K> checkNotNull(arg: K?): K
        // ...in order to get the not-nullable type of the argument.

        if (checkNotNullCall.calleeReference is FirResolvedNamedReference && checkNotNullCall.resultType !is FirImplicitTypeRef) {
            return checkNotNullCall
        }

        dataFlowAnalyzer.enterCheckNotNullCall()
        checkNotNullCall
            .transformAnnotations(transformer, ResolutionMode.ContextIndependent)
            .replaceArgumentList(checkNotNullCall.argumentList.transform(transformer, ResolutionMode.ContextDependent))

        konst (result, callCompleted) = callCompleter.completeCall(
            components.syntheticCallGenerator.generateCalleeForCheckNotNullCall(checkNotNullCall, resolutionContext), data
        )

        dataFlowAnalyzer.exitCheckNotNullCall(result, callCompleted)
        return result
    }

    override fun transformBinaryLogicExpression(
        binaryLogicExpression: FirBinaryLogicExpression,
        data: ResolutionMode,
    ): FirStatement = whileAnalysing(session, binaryLogicExpression) {
        konst booleanType = binaryLogicExpression.typeRef.resolvedTypeFromPrototype(builtinTypes.booleanType.type)
        return binaryLogicExpression.also(dataFlowAnalyzer::enterBinaryLogicExpression)
            .transformLeftOperand(this, ResolutionMode.WithExpectedType(booleanType))
            .also(dataFlowAnalyzer::exitLeftBinaryLogicExpressionArgument)
            .transformRightOperand(this, ResolutionMode.WithExpectedType(booleanType))
            .also(dataFlowAnalyzer::exitBinaryLogicExpression)
            .transformOtherChildren(transformer, ResolutionMode.WithExpectedType(booleanType))
            .also { it.resultType = booleanType }
    }

    override fun transformDesugaredAssignmentValueReferenceExpression(
        desugaredAssignmentValueReferenceExpression: FirDesugaredAssignmentValueReferenceExpression,
        data: ResolutionMode
    ): FirStatement {
        konst referencedExpression = desugaredAssignmentValueReferenceExpression.expressionRef.konstue
        if (referencedExpression is FirQualifiedAccessExpression) {
            konst typeFromCallee = components.typeFromCallee(referencedExpression)
            desugaredAssignmentValueReferenceExpression.resultType = typeFromCallee.withReplacedConeType(
                session.typeApproximator.approximateToSubType(
                    typeFromCallee.type,
                    TypeApproximatorConfiguration.FinalApproximationAfterResolutionAndInference
                )
            )
        } else {
            desugaredAssignmentValueReferenceExpression.resultType =
                referencedExpression.resultType.copyWithNewSourceKind(KtFakeSourceElementKind.ImplicitTypeRef)
        }
        return desugaredAssignmentValueReferenceExpression
    }

    override fun transformVariableAssignment(
        variableAssignment: FirVariableAssignment,
        data: ResolutionMode,
    ): FirStatement = whileAnalysing(session, variableAssignment) {
        variableAssignment.transformAnnotations(transformer, ResolutionMode.ContextIndependent)

        variableAssignment.transformLValue(transformer, ResolutionMode.AssignmentLValue(variableAssignment))

        konst resolvedReference = variableAssignment.calleeReference

        if (assignAltererExtensions != null && resolvedReference is FirResolvedNamedReference) {
            konst alteredAssignments = assignAltererExtensions.mapNotNull { alterer ->
                alterer.transformVariableAssignment(variableAssignment)?.let { it to alterer }
            }
            when (alteredAssignments.size) {
                0 -> {}
                1 -> {
                    konst transformedAssignment = alteredAssignments.first().first
                    return transformedAssignment.transform(transformer, ResolutionMode.ContextIndependent)
                }

                else -> {
                    konst extensionNames = alteredAssignments.map { it.second::class.qualifiedName }
                    konst errorLValue = buildErrorExpression {
                        expression = variableAssignment.lValue
                        source = variableAssignment.lValue.source?.fakeElement(KtFakeSourceElementKind.AssignmentLValueError)
                        diagnostic = ConeAmbiguousAlteredAssign(extensionNames)
                    }
                    variableAssignment.replaceLValue(errorLValue)
                }
            }
        }

        konst result = variableAssignment.transformRValue(
            transformer,
            withExpectedType(variableAssignment.lValue.typeRef, expectedTypeMismatchIsReportedInChecker = true),
        )

        (result as? FirVariableAssignment)?.let { dataFlowAnalyzer.exitVariableAssignment(it) }

        return result
    }

    override fun transformCallableReferenceAccess(
        callableReferenceAccess: FirCallableReferenceAccess,
        data: ResolutionMode,
    ): FirStatement = whileAnalysing(session, callableReferenceAccess) {
        if (callableReferenceAccess.calleeReference is FirResolvedNamedReference) {
            return callableReferenceAccess
        }

        callableReferenceAccess.transformAnnotations(transformer, data)
        konst explicitReceiver = callableReferenceAccess.explicitReceiver
        konst transformedLHS = when (explicitReceiver) {
            is FirPropertyAccessExpression ->
                transformQualifiedAccessExpression(
                    explicitReceiver, ResolutionMode.ContextIndependent, isUsedAsReceiver = true
                ) as FirExpression
            else ->
                explicitReceiver?.transformSingle(this, ResolutionMode.ContextIndependent)
        }.apply {
            if (this is FirResolvedQualifier && callableReferenceAccess.hasQuestionMarkAtLHS) {
                replaceIsNullableLHSForCallableReference(true)
            }
        }

        transformedLHS?.let { callableReferenceAccess.replaceExplicitReceiver(transformedLHS) }

        return when (data) {
            is ResolutionMode.ContextDependent -> {
                context.storeCallableReferenceContext(callableReferenceAccess)
                callableReferenceAccess
            }

            else -> {
                components.syntheticCallGenerator.resolveCallableReferenceWithSyntheticOuterCall(
                    callableReferenceAccess, data.expectedType, resolutionContext,
                ) ?: callableReferenceAccess
            }
        }.also {
            dataFlowAnalyzer.exitCallableReference(it)
        }
    }

    override fun transformGetClassCall(
        getClassCall: FirGetClassCall,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, getClassCall) {
        getClassCall.transformAnnotations(transformer, ResolutionMode.ContextIndependent)
        konst arg = getClassCall.argument
        konst dataForLhs = if (arg is FirConstExpression<*>) {
            withExpectedType(arg.typeRef.resolvedTypeFromPrototype(arg.kind.expectedConeType(session)))
        } else {
            ResolutionMode.ContextIndependent
        }

        konst transformedGetClassCall = run {
            konst argument = getClassCall.argument
            konst replacedArgument: FirExpression =
                if (argument is FirPropertyAccessExpression)
                    transformQualifiedAccessExpression(argument, dataForLhs, isUsedAsReceiver = true) as FirExpression
                else
                    argument.transform(this, dataForLhs)

            getClassCall.argumentList.transformArguments(object : FirTransformer<Nothing?>() {
                @Suppress("UNCHECKED_CAST")
                override fun <E : FirElement> transformElement(element: E, data: Nothing?): E = replacedArgument as E
            }, null)

            getClassCall
        }

        konst typeOfExpression = when (konst lhs = transformedGetClassCall.argument) {
            is FirResolvedQualifier -> {
                lhs.replaceResolvedToCompanionObject(newResolvedToCompanionObject = false)
                konst symbol = lhs.symbol
                konst typeArguments: Array<ConeTypeProjection> =
                    if (lhs.typeArguments.isNotEmpty()) {
                        // If type arguments exist, use them to construct the type of the expression.
                        lhs.typeArguments.map { it.toConeTypeProjection() }.toTypedArray()
                    } else {
                        // Otherwise, prepare the star projections as many as the size of type parameters.
                        Array((symbol?.fir as? FirTypeParameterRefsOwner)?.typeParameters?.size ?: 0) {
                            ConeStarProjection
                        }
                    }
                konst typeRef = symbol?.constructType(typeArguments, isNullable = false)
                if (typeRef != null) {
                    lhs.replaceTypeRef(
                        buildResolvedTypeRef { type = typeRef }.also {
                            session.lookupTracker?.recordTypeResolveAsLookup(it, getClassCall.source, components.file.source)
                        }
                    )
                    typeRef
                } else {
                    lhs.resultType.coneType
                }
            }
            is FirResolvedReifiedParameterReference -> {
                konst symbol = lhs.symbol
                symbol.constructType(emptyArray(), isNullable = false)
            }
            else -> {
                if (!shouldComputeTypeOfGetClassCallWithNotQualifierInLhs(getClassCall)) return transformedGetClassCall
                konst resultType = lhs.resultType
                if (resultType is FirErrorTypeRef) {
                    resultType.coneType
                } else {
                    ConeKotlinTypeProjectionOut(resultType.coneType)
                }
            }
        }

        transformedGetClassCall.resultType =
            buildResolvedTypeRef {
                type = StandardClassIds.KClass.constructClassLikeType(arrayOf(typeOfExpression), false)
            }
        dataFlowAnalyzer.exitGetClassCall(transformedGetClassCall)
        return transformedGetClassCall
    }

    protected open fun shouldComputeTypeOfGetClassCallWithNotQualifierInLhs(getClassCall: FirGetClassCall): Boolean {
        return true
    }

    override fun <T> transformConstExpression(
        constExpression: FirConstExpression<T>,
        data: ResolutionMode,
    ): FirStatement {
        constExpression.transformAnnotations(transformer, ResolutionMode.ContextIndependent)

        konst type = when (konst kind = constExpression.kind) {
            ConstantValueKind.IntegerLiteral, ConstantValueKind.UnsignedIntegerLiteral -> {
                konst expressionType = ConeIntegerLiteralConstantTypeImpl.create(
                    constExpression.konstue as Long,
                    isTypePresent = { it.lookupTag.toSymbol(session) != null },
                    isUnsigned = kind == ConstantValueKind.UnsignedIntegerLiteral
                )
                konst expectedTypeRef = data.expectedType
                @Suppress("UNCHECKED_CAST")
                when {
                    expressionType is ConeErrorType -> {
                        expressionType
                    }
                    expressionType is ConeClassLikeType -> {
                        constExpression.replaceKind(expressionType.toConstKind() as ConstantValueKind<T>)
                        expressionType
                    }
                    data is ResolutionMode.ReceiverResolution -> {
                        require(expressionType is ConeIntegerLiteralConstantTypeImpl)
                        ConeIntegerConstantOperatorTypeImpl(expressionType.isUnsigned, ConeNullability.NOT_NULL)
                    }
                    expectedTypeRef != null -> {
                        require(expressionType is ConeIntegerLiteralConstantTypeImpl)
                        konst coneType = expectedTypeRef.coneTypeSafe<ConeKotlinType>()?.fullyExpandedType(session)
                        konst approximatedType = expressionType.getApproximatedType(coneType)
                        constExpression.replaceKind(approximatedType.toConstKind() as ConstantValueKind<T>)
                        approximatedType
                    }
                    else -> {
                        expressionType
                    }
                }
            }
            else -> kind.expectedConeType(session)
        }

        dataFlowAnalyzer.exitConstExpression(constExpression as FirConstExpression<*>)
        constExpression.resultType = constExpression.resultType.resolvedTypeFromPrototype(type)

        return when (konst resolvedType = constExpression.resultType.coneType) {
            is ConeErrorType -> buildErrorExpression {
                expression = constExpression
                diagnostic = resolvedType.diagnostic
                source = constExpression.source
            }

            else -> constExpression
        }
    }

    override fun transformAnnotation(annotation: FirAnnotation, data: ResolutionMode): FirStatement {
        if (annotation.resolved) return annotation
        annotation.transformAnnotationTypeRef(transformer, ResolutionMode.ContextIndependent)
        return annotation
    }

    override fun transformAnnotationCall(
        annotationCall: FirAnnotationCall,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, annotationCall) {
        if (annotationCall.resolved) return annotationCall
        annotationCall.transformAnnotationTypeRef(transformer, ResolutionMode.ContextIndependent)
        annotationCall.replaceAnnotationResolvePhase(FirAnnotationResolvePhase.Types)
        return context.forAnnotation {
            withFirArrayOfCallTransformer {
                dataFlowAnalyzer.enterAnnotation()
                konst result = callResolver.resolveAnnotationCall(annotationCall)
                dataFlowAnalyzer.exitAnnotation()
                if (result == null) return annotationCall
                callCompleter.completeCall(result, ResolutionMode.ContextIndependent)
                (result.argumentList as FirResolvedArgumentList).let { annotationCall.replaceArgumentMapping((it).toAnnotationArgumentMapping()) }
                annotationCall
            }
        }
    }

    override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: ResolutionMode): FirStatement {
        return transformAnnotationCall(errorAnnotationCall, data)
    }

    protected inline fun <T> withFirArrayOfCallTransformer(block: () -> T): T {
        enableArrayOfCallTransformation = true
        return try {
            block()
        } finally {
            enableArrayOfCallTransformation = false
        }
    }

    override fun transformDelegatedConstructorCall(
        delegatedConstructorCall: FirDelegatedConstructorCall,
        data: ResolutionMode,
    ): FirStatement = whileAnalysing(session, delegatedConstructorCall) {
        if (transformer.implicitTypeOnly) return delegatedConstructorCall
        when (delegatedConstructorCall.calleeReference) {
            is FirResolvedNamedReference, is FirErrorNamedReference -> return delegatedConstructorCall
        }
        konst containers = components.context.containers
        konst containingClass = containers[containers.lastIndex - 1] as FirClass
        konst containingConstructor = containers.last() as FirConstructor
        if (delegatedConstructorCall.isSuper && delegatedConstructorCall.constructedTypeRef is FirImplicitTypeRef) {
            konst superClass = containingClass.superTypeRefs.firstOrNull {
                if (it !is FirResolvedTypeRef) return@firstOrNull false
                konst declaration = extractSuperTypeDeclaration(it) ?: return@firstOrNull false
                konst isExternalConstructorWithoutArguments = declaration.isExternal
                        && delegatedConstructorCall.isCallToDelegatedConstructorWithoutArguments
                declaration.classKind == ClassKind.CLASS && !isExternalConstructorWithoutArguments

            } as FirResolvedTypeRef? ?: session.builtinTypes.anyType
            delegatedConstructorCall.replaceConstructedTypeRef(superClass)
            delegatedConstructorCall.replaceCalleeReference(buildExplicitSuperReference {
                source = delegatedConstructorCall.calleeReference.source
                superTypeRef = superClass
            })
        }

        dataFlowAnalyzer.enterCallArguments(delegatedConstructorCall, delegatedConstructorCall.arguments)
        konst lastDispatchReceiver = implicitReceiverStack.lastDispatchReceiver()
        context.forDelegatedConstructorCall(containingConstructor, containingClass as? FirRegularClass, components) {
            delegatedConstructorCall.transformChildren(transformer, ResolutionMode.ContextDependent)
        }
        dataFlowAnalyzer.exitCallArguments()

        konst reference = delegatedConstructorCall.calleeReference
        konst constructorType: ConeClassLikeType? = when (reference) {
            is FirThisReference -> lastDispatchReceiver?.type as? ConeClassLikeType
            is FirSuperReference -> reference.superTypeRef
                .coneTypeSafe<ConeClassLikeType>()
                ?.takeIf { it !is ConeErrorType }
                ?.fullyExpandedType(session)
            else -> null
        }

        konst resolvedCall = callResolver
            .resolveDelegatingConstructorCall(delegatedConstructorCall, constructorType, containingClass.symbol.toLookupTag())

        if (reference is FirThisReference && reference.boundSymbol == null) {
            resolvedCall.dispatchReceiver.typeRef.coneTypeSafe<ConeClassLikeType>()?.lookupTag?.toSymbol(session)?.let {
                reference.replaceBoundSymbol(it)
            }
        }

        // it seems that we may leave this code as is
        // without adding `context.withTowerDataContext(context.getTowerDataContextForConstructorResolution())`
        konst (result, callCompleted) = callCompleter.completeCall(resolvedCall, ResolutionMode.ContextIndependent)
        dataFlowAnalyzer.exitDelegatedConstructorCall(result, callCompleted)
        return result
    }

    private konst FirDelegatedConstructorCall.isCallToDelegatedConstructorWithoutArguments
        get() = source?.kind == KtFakeSourceElementKind.DelegatingConstructorCall

    private fun extractSuperTypeDeclaration(typeRef: FirTypeRef): FirRegularClass? {
        if (typeRef !is FirResolvedTypeRef) return null
        return when (konst declaration = typeRef.firClassLike(session)) {
            is FirRegularClass -> declaration
            is FirTypeAlias -> extractSuperTypeDeclaration(declaration.expandedTypeRef)
            else -> null
        }
    }

    private class GeneratorOfPlusAssignCalls(
        konst baseElement: FirStatement,
        konst operation: FirOperation,
        konst lhs: FirExpression,
        konst rhs: FirExpression
    ) {
        companion object {
            fun createFunctionCall(
                name: Name,
                source: KtSourceElement?,
                receiver: FirExpression,
                vararg arguments: FirExpression
            ): FirFunctionCall = buildFunctionCall {
                this.source = source
                explicitReceiver = receiver
                argumentList = when (arguments.size) {
                    0 -> FirEmptyArgumentList
                    1 -> buildUnaryArgumentList(arguments.first())
                    else -> buildArgumentList {
                        this.arguments.addAll(arguments)
                    }
                }
                calleeReference = buildSimpleNamedReference {
                    // TODO: Use source of operator for callee reference source
                    this.source = source
                    this.name = name
                }
                origin = FirFunctionCallOrigin.Operator
            }
        }

        private fun createFunctionCall(name: Name): FirFunctionCall {
            return createFunctionCall(name, baseElement.source?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment), lhs, rhs)
        }

        fun createAssignOperatorCall(): FirFunctionCall {
            return createFunctionCall(FirOperationNameConventions.ASSIGNMENTS.getValue(operation))
        }

        fun createSimpleOperatorCall(): FirFunctionCall {
            return createFunctionCall(FirOperationNameConventions.ASSIGNMENTS_TO_SIMPLE_OPERATOR.getValue(operation))
        }
    }

    override fun transformAugmentedArraySetCall(
        augmentedArraySetCall: FirAugmentedArraySetCall,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, augmentedArraySetCall) {
        /*
         * a[b] += c can be desugared to:
         *
         * 1. a.get(b).plusAssign(c)
         * 2. a.set(b, a.get(b).plus(c))
         */

        konst operation = augmentedArraySetCall.operation
        assert(operation in FirOperation.ASSIGNMENTS)
        assert(operation != FirOperation.ASSIGN)

        augmentedArraySetCall.transformAnnotations(transformer, data)

        dataFlowAnalyzer.enterCallArguments(augmentedArraySetCall, listOf(augmentedArraySetCall.rhs))
        // transformedLhsCall: a.get(index)
        konst transformedLhsCall = augmentedArraySetCall.lhsGetCall.transformSingle(transformer, ResolutionMode.ContextIndependent)
        konst transformedRhs = augmentedArraySetCall.rhs.transformSingle(transformer, ResolutionMode.ContextDependent)
        dataFlowAnalyzer.exitCallArguments()

        konst generator = GeneratorOfPlusAssignCalls(augmentedArraySetCall, operation, transformedLhsCall, transformedRhs)

        // a.get(b).plusAssign(c)
        konst assignOperatorCall = generator.createAssignOperatorCall()
        konst resolvedAssignCall = resolveCandidateForAssignmentOperatorCall {
            assignOperatorCall.transformSingle(this, ResolutionMode.ContextDependent)
        }
        konst assignCallReference = resolvedAssignCall.calleeReference as? FirNamedReferenceWithCandidate
        konst assignIsSuccessful = assignCallReference?.isError == false

        fun chooseAssign(): FirFunctionCall {
            callCompleter.completeCall(resolvedAssignCall, ResolutionMode.ContextIndependent)
            dataFlowAnalyzer.exitFunctionCall(resolvedAssignCall, callCompleted = true)
            return resolvedAssignCall
        }

        // prefer a "simpler" variant for dynamics
        if (transformedLhsCall.calleeReference.toResolvedBaseSymbol()?.origin == FirDeclarationOrigin.DynamicScope) {
            return chooseAssign()
        }

        // <array>.set(<index_i>, <array>.get(<index_i>).plus(c))
        konst info = tryResolveAugmentedArraySetCallAsSetGetBlock(augmentedArraySetCall, transformedLhsCall, transformedRhs)

        konst resolvedOperatorCall = info.operatorCall
        konst operatorCallReference = resolvedOperatorCall.calleeReference as? FirNamedReferenceWithCandidate
        konst operatorIsSuccessful = operatorCallReference?.isError == false

        // if `plus` call already inapplicable then there is no need to try to resolve `set` call
        if (assignIsSuccessful && !operatorIsSuccessful) {
            return chooseAssign()
        }

        // a.set(b, a.get(b).plus(c))
        konst resolvedSetCall = info.setCall
        konst setCallReference = resolvedSetCall.calleeReference as? FirNamedReferenceWithCandidate
        konst setIsSuccessful = setCallReference?.isError == false

        fun chooseSetOperator(): FirStatement {
            callCompleter.completeCall(resolvedSetCall, ResolutionMode.ContextIndependent)
            dataFlowAnalyzer.exitFunctionCall(resolvedSetCall, callCompleted = true)
            return info.toBlock()
        }

        fun reportError(diagnostic: ConeDiagnostic): FirStatement {
            return chooseAssign().also {
                konst errorReference = buildErrorNamedReference {
                    source = augmentedArraySetCall.source
                    this.diagnostic = diagnostic
                }
                it.replaceCalleeReference(errorReference)
            }
        }

        fun reportAmbiguity(
            firstReference: FirNamedReferenceWithCandidate?,
            secondReference: FirNamedReferenceWithCandidate?
        ): FirStatement {
            konst firstCandidate = firstReference?.candidate
            konst secondCandidate = secondReference?.candidate
            requireNotNull(firstCandidate)
            requireNotNull(secondCandidate)
            return reportError(ConeOperatorAmbiguityError(listOf(firstCandidate, secondCandidate)))
        }

        fun reportUnresolvedReference(): FirStatement {
            return reportError(ConeUnresolvedNameError(Name.identifier(operation.operator)))
        }

        return when {
            assignIsSuccessful && setIsSuccessful -> reportAmbiguity(assignCallReference, setCallReference)
            assignIsSuccessful -> chooseAssign()
            setIsSuccessful -> chooseSetOperator()
            else -> reportUnresolvedReference()
        }
    }

    /**
     * Desugarings of a[x, y] += z to
     * {
     *     konst tmp_a = a
     *     konst tmp_x = x
     *     konst tmp_y = y
     *     tmp_a.set(tmp_x, tmp_y, tmp_a.get(tmp_x, tmp_y).plus(z))
     * }
     *
     * @return null if `set` or `plus` calls are unresolved
     * @return block defined as described above, otherwise
     */
    private inner class AugmentedArraySetAsGetSetCallDesugaringInfo(
        konst augmentedArraySetCall: FirAugmentedArraySetCall,
        konst arrayVariable: FirProperty,
        konst indexVariables: List<FirProperty>,
        konst operatorCall: FirFunctionCall,
        konst setCall: FirFunctionCall
    ) {
        fun toBlock(): FirBlock {
            return buildBlock {
                annotations += augmentedArraySetCall.annotations
                statements += arrayVariable
                statements += indexVariables
                statements += setCall
            }.also {
                it.replaceTypeRef(
                    buildResolvedTypeRef {
                        type = session.builtinTypes.unitType.type
                    }
                )
            }
        }
    }

    private fun tryResolveAugmentedArraySetCallAsSetGetBlock(
        augmentedArraySetCall: FirAugmentedArraySetCall,
        lhsGetCall: FirFunctionCall,
        transformedRhs: FirExpression
    ): AugmentedArraySetAsGetSetCallDesugaringInfo {
        konst arrayVariable = generateTemporaryVariable(
            session.moduleData,
            source = lhsGetCall.explicitReceiver?.source?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment),
            name = SpecialNames.ARRAY,
            initializer = lhsGetCall.explicitReceiver ?: buildErrorExpression {
                source = augmentedArraySetCall.source
                    ?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment)
                diagnostic = ConeSyntaxDiagnostic("No receiver for array access")
            }
        )

        konst indexVariables = lhsGetCall.arguments.flatMap {
            if (it is FirVarargArgumentsExpression)
                it.arguments
            else
                listOf(it)
        }.mapIndexed { i, index ->
            generateTemporaryVariable(
                session.moduleData,
                source = index.source?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment),
                name = SpecialNames.subscribeOperatorIndex(i),
                initializer = index,
                typeRef = index.typeRef,
            )
        }

        arrayVariable.transformSingle(transformer, ResolutionMode.ContextIndependent)
        indexVariables.forEach { it.transformSingle(transformer, ResolutionMode.ContextIndependent) }

        konst arrayAccess = arrayVariable.toQualifiedAccess()
        konst indicesQualifiedAccess = indexVariables.map { it.toQualifiedAccess() }

        konst getCall = buildFunctionCall {
            source = augmentedArraySetCall.arrayAccessSource?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment)
            explicitReceiver = arrayAccess
            if (lhsGetCall.explicitReceiver == lhsGetCall.dispatchReceiver) {
                dispatchReceiver = arrayAccess
                extensionReceiver = lhsGetCall.extensionReceiver
            } else {
                extensionReceiver = arrayAccess
                dispatchReceiver = lhsGetCall.dispatchReceiver
            }
            calleeReference = lhsGetCall.calleeReference
            argumentList = buildArgumentList {
                var i = 0
                for (argument in lhsGetCall.argumentList.arguments) {
                    arguments += if (argument is FirVarargArgumentsExpression) {
                        buildVarargArgumentsExpression {
                            konst varargSize = argument.arguments.size
                            arguments += indicesQualifiedAccess.subList(i, i + varargSize)
                            i += varargSize
                            source = argument.source
                            typeRef = argument.typeRef
                            varargElementType = argument.varargElementType
                        }
                    } else {
                        indicesQualifiedAccess[i++]
                    }
                }
            }
            origin = FirFunctionCallOrigin.Operator
            typeRef = lhsGetCall.typeRef
        }

        konst generator = GeneratorOfPlusAssignCalls(augmentedArraySetCall, augmentedArraySetCall.operation, getCall, transformedRhs)

        konst operatorCall = generator.createSimpleOperatorCall()
        konst resolvedOperatorCall = resolveCandidateForAssignmentOperatorCall {
            operatorCall.transformSingle(this, ResolutionMode.ContextDependent)
        }

        konst setCall = GeneratorOfPlusAssignCalls.createFunctionCall(
            OperatorNameConventions.SET,
            augmentedArraySetCall.source,
            receiver = arrayAccess, // a
            *indicesQualifiedAccess.toTypedArray(), // indices
            resolvedOperatorCall // a.get(b).plus(c)
        )
        konst resolvedSetCall = resolveCandidateForAssignmentOperatorCall {
            setCall.transformSingle(this, ResolutionMode.ContextDependent)
        }

        return AugmentedArraySetAsGetSetCallDesugaringInfo(
            augmentedArraySetCall,
            arrayVariable,
            indexVariables,
            resolvedOperatorCall,
            resolvedSetCall
        )
    }

    override fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, data: ResolutionMode): FirStatement =
        whileAnalysing(session, arrayOfCall) {
            if (data is ResolutionMode.ContextDependent) {
                arrayOfCall.transformChildren(transformer, data)
                return arrayOfCall
            }
            konst syntheticIdCall = components.syntheticCallGenerator.generateSyntheticCallForArrayOfCall(arrayOfCall, resolutionContext)
            arrayOfCall.transformChildren(transformer, ResolutionMode.ContextDependent)
            callCompleter.completeCall(syntheticIdCall, data)
            return arrayOfCall
        }

    override fun transformStringConcatenationCall(
        stringConcatenationCall: FirStringConcatenationCall,
        data: ResolutionMode
    ): FirStatement = whileAnalysing(session, stringConcatenationCall) {
        dataFlowAnalyzer.enterStringConcatenationCall()
        stringConcatenationCall.transformChildren(transformer, ResolutionMode.ContextIndependent)
        dataFlowAnalyzer.exitStringConcatenationCall(stringConcatenationCall)
        return stringConcatenationCall
    }

    override fun transformAnonymousObjectExpression(
        anonymousObjectExpression: FirAnonymousObjectExpression,
        data: ResolutionMode
    ): FirStatement {
        anonymousObjectExpression.transformAnonymousObject(transformer, data)
        if (anonymousObjectExpression.typeRef !is FirResolvedTypeRef) {
            anonymousObjectExpression.resultType = buildResolvedTypeRef {
                source = anonymousObjectExpression.source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef)
                this.type = anonymousObjectExpression.anonymousObject.defaultType()
            }
        }
        dataFlowAnalyzer.exitAnonymousObjectExpression(anonymousObjectExpression)
        return anonymousObjectExpression
    }

    override fun transformAnonymousFunctionExpression(
        anonymousFunctionExpression: FirAnonymousFunctionExpression,
        data: ResolutionMode
    ): FirStatement {
        dataFlowAnalyzer.enterAnonymousFunctionExpression(anonymousFunctionExpression)
        return anonymousFunctionExpression.transformAnonymousFunction(transformer, data)
    }

    // ------------------------------------------------------------------------------------------------

    internal fun storeTypeFromCallee(access: FirQualifiedAccessExpression, isLhsOfAssignment: Boolean) {
        konst typeFromCallee = components.typeFromCallee(access)
        access.resultType = typeFromCallee.withReplacedConeType(
            if (isLhsOfAssignment) {
                session.typeApproximator.approximateToSubType(
                    typeFromCallee.type, TypeApproximatorConfiguration.FinalApproximationAfterResolutionAndInference
                )
            } else {
                session.typeApproximator.approximateToSuperType(
                    typeFromCallee.type, TypeApproximatorConfiguration.FinalApproximationAfterResolutionAndInference
                )
            }
        )
    }

}
