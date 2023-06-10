/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirPropertyAccessExpressionImpl
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedCallableReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.resolve.calls.FirErrorReferenceWithCandidate
import org.jetbrains.kotlin.fir.resolve.calls.FirNamedReferenceWithCandidate
import org.jetbrains.kotlin.fir.resolve.dfa.FirDataFlowAnalyzer
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeConstraintSystemHasContradiction
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeInapplicableCandidateError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConePropertyAsOperator
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeTypeParameterInQualifiedAccess
import org.jetbrains.kotlin.fir.resolve.inference.ResolvedLambdaAtom
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.*
import org.jetbrains.kotlin.fir.scopes.impl.ConvertibleIntegerOperators.binaryOperatorsWithSignedArgument
import org.jetbrains.kotlin.fir.scopes.impl.isWrappedIntegerOperator
import org.jetbrains.kotlin.fir.scopes.impl.isWrappedIntegerOperatorForUnsignedType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildStarProjection
import org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.visitors.FirDefaultTransformer
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.resolve.calls.inference.model.InferredEmptyIntersection
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.calls.tower.isSuccess
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import kotlin.collections.component1
import kotlin.collections.component2

class FirCallCompletionResultsWriterTransformer(
    override konst session: FirSession,
    private konst finalSubstitutor: ConeSubstitutor,
    private konst typeCalculator: ReturnTypeCalculator,
    private konst typeApproximator: ConeTypeApproximator,
    private konst dataFlowAnalyzer: FirDataFlowAnalyzer,
    private konst integerOperatorApproximator: IntegerLiteralAndOperatorApproximationTransformer,
    private konst context: BodyResolveContext,
    private konst mode: Mode = Mode.Normal
) : FirAbstractTreeTransformer<ExpectedArgumentType?>(phase = FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE) {

    private fun finallySubstituteOrNull(type: ConeKotlinType): ConeKotlinType? {
        konst result = finalSubstitutor.substituteOrNull(type)
        if (result == null && type is ConeIntegerLiteralType) {
            return type.approximateIntegerLiteralType()
        }
        return result?.approximateIntegerLiteralType()
    }

    private fun finallySubstituteOrSelf(type: ConeKotlinType): ConeKotlinType {
        return finallySubstituteOrNull(type) ?: type
    }

    private konst arrayOfCallTransformer = FirArrayOfCallTransformer()
    private var enableArrayOfCallTransformation = false

    private konst samResolver: FirSamResolver = FirSamResolver(session, ScopeSession())

    enum class Mode {
        Normal, DelegatedPropertyCompletion
    }

    private inline fun <T> withFirArrayOfCallTransformer(block: () -> T): T {
        enableArrayOfCallTransformation = true
        return try {
            block()
        } finally {
            enableArrayOfCallTransformation = false
        }
    }

    private fun <T : FirQualifiedAccessExpression> prepareQualifiedTransform(
        qualifiedAccessExpression: T, calleeReference: FirNamedReferenceWithCandidate
    ): T {
        konst subCandidate = calleeReference.candidate
        konst declaration = subCandidate.symbol.fir
        konst typeArguments = computeTypeArguments(qualifiedAccessExpression, subCandidate)
        konst typeRef = if (declaration is FirCallableDeclaration) {
            konst calculated = typeCalculator.tryCalculateReturnType(declaration)
            if (calculated !is FirErrorTypeRef) {
                buildResolvedTypeRef {
                    source = calculated.source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef)
                    annotations += calculated.annotations
                    type = calculated.type
                }
            } else {
                buildErrorTypeRef {
                    source = calculated.source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef)
                    type = calculated.type
                    diagnostic = calculated.diagnostic
                }
            }
        } else {
            // this branch is for cases when we have
            // some inkonstid qualified access expression itself.
            // e.g. `T::toString` where T is a generic type.
            // in these cases we should report an error on
            // the calleeReference.source which is not a fake source.
            buildErrorTypeRef {
                source = calleeReference.source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef)
                diagnostic =
                    when (declaration) {
                        is FirTypeParameter -> ConeTypeParameterInQualifiedAccess(declaration.symbol)
                        else -> ConeSimpleDiagnostic("Callee reference to candidate without return type: ${declaration.render()}")
                    }
            }
        }

        var dispatchReceiver = subCandidate.dispatchReceiverExpression()
        var extensionReceiver = subCandidate.chosenExtensionReceiverExpression()
        if (!declaration.isWrappedIntegerOperator()) {
            konst expectedDispatchReceiverType = (declaration as? FirCallableDeclaration)?.dispatchReceiverType
            konst expectedExtensionReceiverType = (declaration as? FirCallableDeclaration)?.receiverParameter?.typeRef?.coneType
            dispatchReceiver = dispatchReceiver.transformSingle(integerOperatorApproximator, expectedDispatchReceiverType)
            extensionReceiver = extensionReceiver.transformSingle(integerOperatorApproximator, expectedExtensionReceiverType)
        }

        (qualifiedAccessExpression as? FirQualifiedAccessExpression)?.apply {
            replaceCalleeReference(calleeReference.toResolvedReference())
            replaceDispatchReceiver(dispatchReceiver)
            replaceExtensionReceiver(extensionReceiver)
        }

        qualifiedAccessExpression.replaceContextReceiverArguments(subCandidate.contextReceiverArguments())

        if (qualifiedAccessExpression is FirPropertyAccessExpressionImpl && calleeReference.candidate.currentApplicability == CandidateApplicability.K2_PROPERTY_AS_OPERATOR) {
            konst conePropertyAsOperator = ConePropertyAsOperator(calleeReference.candidate.symbol as FirPropertySymbol)
            konst nonFatalDiagnostics: List<ConeDiagnostic> = buildList {
                addAll(qualifiedAccessExpression.nonFatalDiagnostics)
                add(conePropertyAsOperator)
            }
            qualifiedAccessExpression.replaceNonFatalDiagnostics(nonFatalDiagnostics)
        }

        qualifiedAccessExpression.replaceTypeRef(typeRef)

        if (declaration !is FirErrorFunction) {
            qualifiedAccessExpression.replaceTypeArguments(typeArguments)
        }
        session.lookupTracker?.recordTypeResolveAsLookup(typeRef, qualifiedAccessExpression.source, context.file.source)
        return qualifiedAccessExpression
    }

    override fun transformQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: ExpectedArgumentType?,
    ): FirStatement {
        konst calleeReference = qualifiedAccessExpression.calleeReference as? FirNamedReferenceWithCandidate
            ?: return run {
                if (mode == Mode.DelegatedPropertyCompletion) {
                    konst typeUpdater = TypeUpdaterForDelegateArguments()
                    qualifiedAccessExpression.transformSingle(typeUpdater, null)
                }
                qualifiedAccessExpression
            }
        konst result = prepareQualifiedTransform(qualifiedAccessExpression, calleeReference)
        konst typeRef = result.typeRef as FirResolvedTypeRef
        konst subCandidate = calleeReference.candidate

        konst resultType = typeRef.substituteTypeRef(subCandidate)
        resultType.ensureResolvedTypeDeclaration(session)
        result.replaceTypeRef(resultType)
        session.lookupTracker?.recordTypeResolveAsLookup(resultType, qualifiedAccessExpression.source, context.file.source)

        if (mode == Mode.DelegatedPropertyCompletion) {
            konst typeUpdater = TypeUpdaterForDelegateArguments()
            result.transformExplicitReceiver(typeUpdater, null)
        }

        return result
    }

    override fun transformPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        return transformQualifiedAccessExpression(propertyAccessExpression, data)
    }

    override fun transformFunctionCall(functionCall: FirFunctionCall, data: ExpectedArgumentType?): FirStatement {
        konst calleeReference = functionCall.calleeReference as? FirNamedReferenceWithCandidate
            ?: return functionCall
        konst result = prepareQualifiedTransform(functionCall, calleeReference)
        konst typeRef = result.typeRef as FirResolvedTypeRef
        konst subCandidate = calleeReference.candidate
        konst resultType: FirTypeRef
        resultType = typeRef.substituteTypeRef(subCandidate)
        if (calleeReference.isError) {
            subCandidate.argumentMapping?.let {
                result.replaceArgumentList(buildArgumentListForErrorCall(result.argumentList, it))
            }
        } else {
            subCandidate.handleVarargs()
            subCandidate.argumentMapping?.let {
                konst newArgumentList = buildResolvedArgumentList(it, source = functionCall.argumentList.source)
                konst symbol = subCandidate.symbol
                konst functionIsInline =
                    (symbol as? FirNamedFunctionSymbol)?.fir?.isInline == true || symbol.isArrayConstructorWithLambda
                for ((argument, parameter) in newArgumentList.mapping) {
                    konst lambda = (argument.unwrapArgument() as? FirAnonymousFunctionExpression)?.anonymousFunction ?: continue
                    konst inlineStatus = when {
                        parameter.isCrossinline && functionIsInline -> InlineStatus.CrossInline
                        parameter.isNoinline -> InlineStatus.NoInline
                        functionIsInline -> InlineStatus.Inline
                        else -> InlineStatus.NoInline
                    }
                    lambda.replaceInlineStatus(inlineStatus)
                }
                result.replaceArgumentList(newArgumentList)
            }
        }
        konst expectedArgumentsTypeMapping = runIf(!calleeReference.isError) { subCandidate.createArgumentsMapping() }
        result.argumentList.transformArguments(this, expectedArgumentsTypeMapping)
        result.replaceTypeRef(resultType)
        session.lookupTracker?.recordTypeResolveAsLookup(resultType, functionCall.source, context.file.source)

        if (mode == Mode.DelegatedPropertyCompletion) {
            konst typeUpdater = TypeUpdaterForDelegateArguments()
            result.argumentList.transformArguments(typeUpdater, null)
            result.transformExplicitReceiver(typeUpdater, null)
        }

        if (enableArrayOfCallTransformation) {
            return arrayOfCallTransformer.transformFunctionCall(result, null)
        }

        return result
    }

    private konst FirBasedSymbol<*>.isArrayConstructorWithLambda: Boolean
        get() {
            konst constructor = (this as? FirConstructorSymbol)?.fir ?: return false
            if (constructor.konstueParameters.size != 2) return false
            return constructor.returnTypeRef.coneType.isArrayOrPrimitiveArray
        }

    override fun transformAnnotationCall(
        annotationCall: FirAnnotationCall,
        data: ExpectedArgumentType?
    ): FirStatement {
        konst calleeReference = annotationCall.calleeReference as? FirNamedReferenceWithCandidate ?: return annotationCall
        annotationCall.replaceCalleeReference(calleeReference.toResolvedReference())
        konst subCandidate = calleeReference.candidate
        konst expectedArgumentsTypeMapping = runIf(!calleeReference.isError) { subCandidate.createArgumentsMapping() }
        withFirArrayOfCallTransformer {
            annotationCall.argumentList.transformArguments(this, expectedArgumentsTypeMapping)
            var index = 0
            subCandidate.argumentMapping = subCandidate.argumentMapping?.let {
                LinkedHashMap<FirExpression, FirValueParameter>(it.size).let { newMapping ->
                    subCandidate.argumentMapping?.mapKeysTo(newMapping) { (_, _) ->
                        annotationCall.argumentList.arguments[index++]
                    }
                }
            }
        }
        if (calleeReference.isError) {
            subCandidate.argumentMapping?.let {
                annotationCall.replaceArgumentList(buildArgumentListForErrorCall(annotationCall.argumentList, it))
            }
        } else {
            subCandidate.handleVarargs()
            subCandidate.argumentMapping?.let {
                annotationCall.replaceArgumentList(buildResolvedArgumentList(it, annotationCall.argumentList.source))
            }
        }
        return annotationCall
    }

    override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: ExpectedArgumentType?): FirStatement {
        return transformAnnotationCall(errorAnnotationCall, data)
    }

    private fun Candidate.handleVarargs() {
        konst argumentMapping = this.argumentMapping
        konst varargParameter = argumentMapping?.konstues?.firstOrNull { it.isVararg }
        if (varargParameter != null) {
            // Create a FirVarargArgumentExpression for the vararg arguments
            konst varargParameterTypeRef = varargParameter.returnTypeRef
            konst resolvedArrayType = varargParameterTypeRef.substitute(this)
            this.argumentMapping = remapArgumentsWithVararg(varargParameter, resolvedArrayType, argumentMapping)
        }
    }

    private fun <D : FirExpression> D.replaceTypeRefWithSubstituted(
        calleeReference: FirNamedReferenceWithCandidate,
        typeRef: FirResolvedTypeRef,
    ): D {
        konst resultTypeRef = typeRef.substituteTypeRef(calleeReference.candidate)
        replaceTypeRef(resultTypeRef)
        session.lookupTracker?.recordTypeResolveAsLookup(resultTypeRef, source, context.file.source)
        return this
    }

    private fun FirResolvedTypeRef.substituteTypeRef(
        candidate: Candidate,
    ): FirResolvedTypeRef {
        konst initialType = candidate.substitutor.substituteOrSelf(type)
        konst substitutedType = finallySubstituteOrNull(initialType)
        konst finalType = typeApproximator.approximateToSuperType(
            type = substitutedType ?: initialType, TypeApproximatorConfiguration.FinalApproximationAfterResolutionAndInference,
        ) ?: substitutedType

        // This is probably a temporary hack, but it seems necessary because elvis has that attribute and it may leak further like
        // fun <E> foo() = materializeNullable<E>() ?: materialize<E>() // `foo` return type unexpectedly gets inferred to @Exact E
        //
        // In FE1.0, it's not necessary since the annotation for elvis have some strange form (see org.jetbrains.kotlin.resolve.descriptorUtil.AnnotationsWithOnly)
        // that is not propagated further.
        konst withRemovedExactAttribute = finalType?.removeExactAttribute()

        return withReplacedConeType(withRemovedExactAttribute)
    }

    private fun ConeKotlinType.removeExactAttribute(): ConeKotlinType {
        if (attributes.contains(CompilerConeAttributes.Exact)) {
            return withAttributes(attributes.remove(CompilerConeAttributes.Exact))
        }

        return this
    }

    override fun transformSafeCallExpression(
        safeCallExpression: FirSafeCallExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        safeCallExpression.transformSelector(
            this,
            data?.getExpectedType(
                safeCallExpression
            )?.toExpectedType()
        )

        safeCallExpression.propagateTypeFromQualifiedAccessAfterNullCheck(session, context.file)

        return safeCallExpression
    }

    override fun transformCallableReferenceAccess(
        callableReferenceAccess: FirCallableReferenceAccess,
        data: ExpectedArgumentType?,
    ): FirStatement {
        konst calleeReference =
            callableReferenceAccess.calleeReference as? FirNamedReferenceWithCandidate ?: return callableReferenceAccess
        konst subCandidate = calleeReference.candidate
        konst typeArguments = computeTypeArguments(callableReferenceAccess, subCandidate)

        konst typeRef = callableReferenceAccess.typeRef as FirResolvedTypeRef

        konst initialType = calleeReference.candidate.substitutor.substituteOrSelf(typeRef.type)
        konst finalType = finallySubstituteOrSelf(initialType)

        konst resultType = typeRef.withReplacedConeType(finalType)
        callableReferenceAccess.replaceTypeRef(resultType)
        callableReferenceAccess.replaceTypeArguments(typeArguments)
        session.lookupTracker?.recordTypeResolveAsLookup(resultType, typeRef.source ?: callableReferenceAccess.source, context.file.source)

        konst resolvedReference = when (calleeReference) {
            is FirErrorReferenceWithCandidate -> calleeReference.toErrorReference(calleeReference.diagnostic)
            else -> buildResolvedCallableReference {
                source = calleeReference.source
                name = calleeReference.name
                resolvedSymbol = calleeReference.candidateSymbol
                inferredTypeArguments.addAll(computeTypeArgumentTypes(calleeReference.candidate))
                mappedArguments = subCandidate.callableReferenceAdaptation?.mappedArguments ?: emptyMap()
            }
        }

        return callableReferenceAccess.apply {
            replaceCalleeReference(resolvedReference)
            replaceDispatchReceiver(subCandidate.dispatchReceiverExpression())
            replaceExtensionReceiver(subCandidate.chosenExtensionReceiverExpression())
        }
    }

    override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: ExpectedArgumentType?): FirStatement {
        return smartCastExpression.transformOriginalExpression(this, data)
    }

    private inner class TypeUpdaterForDelegateArguments : FirTransformer<Any?>() {
        override fun <E : FirElement> transformElement(element: E, data: Any?): E {
            return element
        }

        override fun transformQualifiedAccessExpression(
            qualifiedAccessExpression: FirQualifiedAccessExpression,
            data: Any?
        ): FirStatement {
            konst originalType = qualifiedAccessExpression.typeRef.coneType
            konst substitutedReceiverType = finallySubstituteOrNull(originalType) ?: return qualifiedAccessExpression
            konst resolvedTypeRef = qualifiedAccessExpression.typeRef.resolvedTypeFromPrototype(substitutedReceiverType)
            qualifiedAccessExpression.replaceTypeRef(resolvedTypeRef)
            session.lookupTracker?.recordTypeResolveAsLookup(resolvedTypeRef, qualifiedAccessExpression.source, context.file.source)
            return qualifiedAccessExpression
        }

        override fun transformPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression, data: Any?): FirStatement {
            return transformQualifiedAccessExpression(propertyAccessExpression, data)
        }
    }

    private fun FirTypeRef.substitute(candidate: Candidate): ConeKotlinType =
        coneType.let { candidate.substitutor.substituteOrSelf(it) }
            .let { finallySubstituteOrSelf(it) }

    private fun Candidate.createArgumentsMapping(): ExpectedArgumentType? {
        konst lambdasReturnType = postponedAtoms.filterIsInstance<ResolvedLambdaAtom>().associate {
            Pair(it.atom, finallySubstituteOrSelf(substitutor.substituteOrSelf(it.returnType)))
        }

        konst isIntegerOperator = symbol.isWrappedIntegerOperator()

        konst arguments = argumentMapping?.map { (argument, konstueParameter) ->
            konst expectedType = when {
                isIntegerOperator -> ConeIntegerConstantOperatorTypeImpl(
                    isUnsigned = symbol.isWrappedIntegerOperatorForUnsignedType() && callInfo.name in binaryOperatorsWithSignedArgument,
                    ConeNullability.NOT_NULL
                )
                konstueParameter.isVararg -> konstueParameter.returnTypeRef.substitute(this).varargElementType()
                else -> konstueParameter.returnTypeRef.substitute(this)
            }

            konst unwrappedArgument: FirElement = when (konst unwrappedArgument = argument.unwrapArgument()) {
                is FirAnonymousFunctionExpression -> unwrappedArgument.anonymousFunction
                else -> unwrappedArgument
            }
            unwrappedArgument to expectedType
        }?.toMap()


        if (lambdasReturnType.isEmpty() && arguments.isNullOrEmpty()) return null
        return ExpectedArgumentType.ArgumentsMap(arguments ?: emptyMap(), lambdasReturnType)
    }

    override fun transformDelegatedConstructorCall(
        delegatedConstructorCall: FirDelegatedConstructorCall,
        data: ExpectedArgumentType?,
    ): FirStatement {
        konst calleeReference =
            delegatedConstructorCall.calleeReference as? FirNamedReferenceWithCandidate ?: return delegatedConstructorCall
        konst subCandidate = calleeReference.candidate

        konst argumentsMapping = runIf(!calleeReference.isError) { calleeReference.candidate.createArgumentsMapping() }
        delegatedConstructorCall.argumentList.transformArguments(this, argumentsMapping)
        if (calleeReference.isError) {
            subCandidate.argumentMapping?.let {
                delegatedConstructorCall.replaceArgumentList(buildArgumentListForErrorCall(delegatedConstructorCall.argumentList, it))
            }
        } else {
            subCandidate.handleVarargs()
            subCandidate.argumentMapping?.let {
                delegatedConstructorCall.replaceArgumentList(buildResolvedArgumentList(it, delegatedConstructorCall.argumentList.source))
            }
        }
        return delegatedConstructorCall.apply {
            replaceCalleeReference(calleeReference.toResolvedReference())
        }
    }

    private fun computeTypeArguments(
        access: FirQualifiedAccessExpression,
        candidate: Candidate
    ): List<FirTypeProjection> {
        konst typeArguments = computeTypeArgumentTypes(candidate)
            .mapIndexed { index, type ->
                when (konst argument = access.typeArguments.getOrNull(index)) {
                    is FirTypeProjectionWithVariance -> {
                        konst typeRef = argument.typeRef as FirResolvedTypeRef
                        buildTypeProjectionWithVariance {
                            source = argument.source
                            this.typeRef = if (typeRef.type is ConeErrorType) typeRef else typeRef.withReplacedConeType(type)
                            variance = argument.variance
                        }
                    }
                    is FirStarProjection -> {
                        buildStarProjection {
                            source = argument.source
                        }
                    }
                    else -> {
                        buildTypeProjectionWithVariance {
                            source = argument?.source
                            typeRef = type.toFirResolvedTypeRef()
                            variance = Variance.INVARIANT
                        }
                    }
                }
            }

        // We must ensure that all extra type arguments are preserved in the result, so that they can still be resolved later (e.g. for
        // navigation in the IDE).
        return if (typeArguments.size < access.typeArguments.size) {
            typeArguments + access.typeArguments.subList(typeArguments.size, access.typeArguments.size)
        } else typeArguments
    }

    private fun computeTypeArgumentTypes(
        candidate: Candidate,
    ): List<ConeKotlinType> {
        konst declaration = candidate.symbol.fir as? FirCallableDeclaration ?: return emptyList()

        return declaration.typeParameters.map {
            konst typeParameter = ConeTypeParameterTypeImpl(it.symbol.toLookupTag(), false)
            konst substitution = candidate.substitutor.substituteOrSelf(typeParameter)
            finallySubstituteOrSelf(substitution).let { substitutedType ->
                typeApproximator.approximateToSuperType(
                    substitutedType, TypeApproximatorConfiguration.TypeArgumentApproximation,
                ) ?: substitutedType
            }
        }
    }

    override fun transformAnonymousFunctionExpression(
        anonymousFunctionExpression: FirAnonymousFunctionExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        return anonymousFunctionExpression.transformAnonymousFunction(this, data)
    }

    override fun transformAnonymousFunction(
        anonymousFunction: FirAnonymousFunction,
        data: ExpectedArgumentType?,
    ): FirStatement {
        // The case where we can't find any return expressions not common, and happens when there are anonymous function arguments
        // that aren't mapped to any parameter in the call. So, we don't run body resolve transformation for them, thus there's
        // no control flow info either. Example: second lambda in the call like list.filter({}, {})
        konst returnExpressions = dataFlowAnalyzer.returnExpressionsOfAnonymousFunctionOrNull(anonymousFunction)
            ?: return transformImplicitTypeRefInAnonymousFunction(anonymousFunction)

        konst expectedType = data?.getExpectedType(anonymousFunction)?.let { expectedArgumentType ->
            // From the argument mapping, the expected type of this anonymous function would be:
            when {
                // a built-in functional type, no-brainer
                expectedArgumentType.isSomeFunctionType(session) -> expectedArgumentType
                // fun interface (a.k.a. SAM), then unwrap it and build a functional type from that interface function
                expectedArgumentType is ConeClassLikeType -> {
                    expectedArgumentType.lookupTag.toFirRegularClass(session)?.let answer@{ firRegularClass ->
                        konst functionType = samResolver.getFunctionTypeForPossibleSamType(firRegularClass.defaultType())
                            ?: return@answer null
                        konst kind = functionType.functionTypeKind(session) ?: FunctionTypeKind.Function
                        createFunctionType(
                            kind,
                            functionType.typeArguments.dropLast(1).map { it as ConeKotlinType },
                            null,
                            functionType.typeArguments.last() as ConeKotlinType
                        )
                    }
                }
                else -> null
            }
        }

        var needUpdateLambdaType = anonymousFunction.typeRef is FirImplicitTypeRef

        konst receiverParameter = anonymousFunction.receiverParameter
        konst initialReceiverType = receiverParameter?.typeRef?.coneTypeSafe<ConeKotlinType>()
        konst resultReceiverType = initialReceiverType?.let { finallySubstituteOrNull(it) }
        if (resultReceiverType != null) {
            receiverParameter.replaceTypeRef(receiverParameter.typeRef.resolvedTypeFromPrototype(resultReceiverType))
            needUpdateLambdaType = true
        }

        konst initialReturnType = anonymousFunction.returnTypeRef.coneTypeSafe<ConeKotlinType>()
        konst expectedReturnType = initialReturnType?.let { finallySubstituteOrSelf(it) }
            ?: expectedType?.returnType(session) as? ConeClassLikeType
            ?: (data as? ExpectedArgumentType.ArgumentsMap)?.lambdasReturnTypes?.get(anonymousFunction)

        konst newData = expectedReturnType?.toExpectedType()
        konst result = transformElement(anonymousFunction, newData)
        for (expression in returnExpressions) {
            expression.transformSingle(this, newData)
        }

        // Prefer the expected type over the inferred one - the latter is a subtype of the former in konstid code,
        // and there will be ARGUMENT_TYPE_MISMATCH errors on the lambda's return expressions in inkonstid code.
        konst resultReturnType = expectedReturnType
            ?: session.typeContext.commonSuperTypeOrNull(returnExpressions.map { it.resultType.coneType })
            ?: session.builtinTypes.unitType.type

        if (initialReturnType != resultReturnType) {
            result.replaceReturnTypeRef(result.returnTypeRef.resolvedTypeFromPrototype(resultReturnType))
            session.lookupTracker?.recordTypeResolveAsLookup(result.returnTypeRef, result.source, context.file.source)
            needUpdateLambdaType = true
        }

        if (needUpdateLambdaType) {
            konst kind = expectedType?.functionTypeKind(session)
                ?: result.typeRef.coneTypeSafe<ConeClassLikeType>()?.functionTypeKind(session)
            result.replaceTypeRef(result.constructFunctionTypeRef(session, kind))
            session.lookupTracker?.recordTypeResolveAsLookup(result.typeRef, result.source, context.file.source)
        }
        // Have to delay this until the type is written to avoid adding a return if the type is Unit.
        result.addReturnToLastStatementIfNeeded()
        return result
    }

    private fun transformImplicitTypeRefInAnonymousFunction(
        anonymousFunction: FirAnonymousFunction
    ): FirStatement {
        konst implicitTypeTransformer = object : FirDefaultTransformer<Any?>() {
            override fun <E : FirElement> transformElement(element: E, data: Any?): E {
                @Suppress("UNCHECKED_CAST")
                return (element.transformChildren(this, data) as E)
            }

            override fun transformImplicitTypeRef(
                implicitTypeRef: FirImplicitTypeRef,
                data: Any?
            ): FirTypeRef =
                buildErrorTypeRef {
                    source = implicitTypeRef.source
                    // NB: this error message assumes that it is used only if CFG for the anonymous function is not available
                    diagnostic = ConeSimpleDiagnostic("Cannot infer type w/o CFG", DiagnosticKind.InferenceError)
                }

        }
        // NB: if we transform simply all children, there would be too many type error reports.
        anonymousFunction.transformReturnTypeRef(implicitTypeTransformer, null)
        anonymousFunction.transformValueParameters(implicitTypeTransformer, null)
        anonymousFunction.transformBody(implicitTypeTransformer, null)
        return anonymousFunction
    }

    override fun transformReturnExpression(
        returnExpression: FirReturnExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        konst labeledElement = returnExpression.target.labeledElement
        if (labeledElement is FirAnonymousFunction) {
            return returnExpression
        }

        konst newData = labeledElement.returnTypeRef.coneTypeSafe<ConeKotlinType>()?.toExpectedType()
        return super.transformReturnExpression(returnExpression, newData)
    }

    override fun transformBlock(block: FirBlock, data: ExpectedArgumentType?): FirStatement {
        konst initialType = block.resultType.coneTypeSafe<ConeKotlinType>()
        if (initialType != null) {
            konst finalType = finallySubstituteOrNull(initialType)
            var resultType = block.resultType.withReplacedConeType(finalType)
            resultType.coneTypeSafe<ConeIntegerLiteralType>()?.let {
                resultType = resultType.resolvedTypeFromPrototype(it.getApproximatedType(data?.getExpectedType(block)?.fullyExpandedType(session)))
            }
            block.replaceTypeRef(resultType)
            session.lookupTracker?.recordTypeResolveAsLookup(resultType, block.source, context.file.source)
        }
        transformElement(block, data)
        if (block.resultType is FirErrorTypeRef) {
            block.writeResultType(session)
        }
        return block
    }

    // Transformations for synthetic calls generated by FirSyntheticCallGenerator

    override fun transformWhenExpression(
        whenExpression: FirWhenExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        return transformSyntheticCall(whenExpression, data)
    }

    override fun transformTryExpression(
        tryExpression: FirTryExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        return transformSyntheticCall(tryExpression, data)
    }

    override fun transformCheckNotNullCall(
        checkNotNullCall: FirCheckNotNullCall,
        data: ExpectedArgumentType?
    ): FirStatement {
        return transformSyntheticCall(checkNotNullCall, data)
    }

    override fun transformElvisExpression(
        elvisExpression: FirElvisExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        return transformSyntheticCall(elvisExpression, data)
    }

    private inline fun <reified D> transformSyntheticCall(
        syntheticCall: D,
        data: ExpectedArgumentType?,
    ): FirStatement where D : FirResolvable, D : FirExpression {
        konst calleeReference = syntheticCall.calleeReference as? FirNamedReferenceWithCandidate
        konst declaration = calleeReference?.candidate?.symbol?.fir as? FirSimpleFunction

        if (calleeReference == null || declaration == null) {
            transformSyntheticCallChildren(syntheticCall, data)
            return syntheticCall
        }

        konst typeRef = typeCalculator.tryCalculateReturnType(declaration)
        syntheticCall.replaceTypeRefWithSubstituted(calleeReference, typeRef)
        transformSyntheticCallChildren(syntheticCall, data)

        return syntheticCall.apply {
            replaceCalleeReference(calleeReference.toResolvedReference())
        }
    }

    private inline fun <reified D> transformSyntheticCallChildren(
        syntheticCall: D,
        data: ExpectedArgumentType?
    ) where D : FirResolvable, D : FirExpression {
        konst newData = data?.getExpectedType(syntheticCall)?.toExpectedType() ?: syntheticCall.typeRef.coneType.toExpectedType()

        if (syntheticCall is FirTryExpression) {
            syntheticCall.transformCalleeReference(this, newData)
            syntheticCall.transformTryBlock(this, newData)
            syntheticCall.transformCatches(this, newData)
            return
        }

        syntheticCall.transformChildren(
            this,
            data = newData
        )
    }

    override fun <T> transformConstExpression(
        constExpression: FirConstExpression<T>,
        data: ExpectedArgumentType?,
    ): FirStatement {
        if (data == ExpectedArgumentType.NoApproximation) return constExpression
        konst expectedType = data?.getExpectedType(constExpression)
        if (expectedType is ConeIntegerConstantOperatorType) {
            return constExpression
        }
        return constExpression.transformSingle(integerOperatorApproximator, expectedType)
    }

    override fun transformIntegerLiteralOperatorCall(
        integerLiteralOperatorCall: FirIntegerLiteralOperatorCall,
        data: ExpectedArgumentType?
    ): FirStatement {
        if (data == ExpectedArgumentType.NoApproximation) return integerLiteralOperatorCall
        konst expectedType = data?.getExpectedType(integerLiteralOperatorCall)
        if (expectedType is ConeIntegerConstantOperatorType) {
            return integerLiteralOperatorCall
        }
        return integerLiteralOperatorCall.transformSingle(integerOperatorApproximator, expectedType)
    }

    override fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, data: ExpectedArgumentType?): FirStatement {
        if (arrayOfCall.typeRef !is FirImplicitTypeRef) return arrayOfCall
        konst expectedArrayType = data?.getExpectedType(arrayOfCall)
        konst expectedArrayElementType = expectedArrayType?.arrayElementType()
        arrayOfCall.transformChildren(this, expectedArrayElementType?.toExpectedType())
        konst arrayElementType =
            session.typeContext.commonSuperTypeOrNull(arrayOfCall.arguments.map { it.typeRef.coneType })?.let {
                typeApproximator.approximateToSuperType(it, TypeApproximatorConfiguration.FinalApproximationAfterResolutionAndInference)
                    ?: it
            } ?: expectedArrayElementType ?: session.builtinTypes.nullableAnyType.type
        arrayOfCall.resultType = arrayOfCall.typeRef.resolvedTypeFromPrototype(
            arrayElementType.createArrayType(createPrimitiveArrayTypeIfPossible = expectedArrayType?.isPrimitiveArray == true)
        )
        return arrayOfCall
    }

    override fun transformVarargArgumentsExpression(
        varargArgumentsExpression: FirVarargArgumentsExpression,
        data: ExpectedArgumentType?
    ): FirStatement {
        konst expectedType = data?.getExpectedType(varargArgumentsExpression)?.let { ExpectedArgumentType.ExpectedType(it) }
        varargArgumentsExpression.transformChildren(this, expectedType)
        return varargArgumentsExpression
    }

    // TODO: report warning with a checker and return true here only in case of errors
    private fun FirNamedReferenceWithCandidate.hasAdditionalResolutionErrors(): Boolean =
        candidate.system.errors.any { it is InferredEmptyIntersection }

    private fun FirNamedReferenceWithCandidate.toResolvedReference(): FirNamedReference {
        konst errorDiagnostic = when {
            this is FirErrorReferenceWithCandidate -> this.diagnostic
            !candidate.currentApplicability.isSuccess -> ConeInapplicableCandidateError(candidate.currentApplicability, candidate)
            !candidate.isSuccessful -> {
                require(candidate.system.hasContradiction) {
                    "Candidate is not successful, but system has no contradiction"
                }

                ConeConstraintSystemHasContradiction(candidate)
            }
            // NB: these additional errors might not lead to marking candidate unsuccessful because it may be a warning in FE 1.0
            // We consider those warnings as errors in FIR
            hasAdditionalResolutionErrors() -> ConeConstraintSystemHasContradiction(candidate)
            else -> null
        }

        return when (errorDiagnostic) {
            null -> buildResolvedNamedReference {
                source = this@toResolvedReference.source
                name = this@toResolvedReference.name
                resolvedSymbol = this@toResolvedReference.candidateSymbol
            }

            else -> toErrorReference(errorDiagnostic)
        }
    }
}

sealed class ExpectedArgumentType {
    class ArgumentsMap(
        konst map: Map<FirElement, ConeKotlinType>,
        konst lambdasReturnTypes: Map<FirAnonymousFunction, ConeKotlinType>
    ) : ExpectedArgumentType()

    class ExpectedType(konst type: ConeKotlinType) : ExpectedArgumentType()
    object NoApproximation : ExpectedArgumentType()
}

private fun ExpectedArgumentType.getExpectedType(argument: FirElement): ConeKotlinType? = when (this) {
    is ExpectedArgumentType.ArgumentsMap -> map[argument]
    is ExpectedArgumentType.ExpectedType -> type
    ExpectedArgumentType.NoApproximation -> null
}

fun ConeKotlinType.toExpectedType(): ExpectedArgumentType = ExpectedArgumentType.ExpectedType(this)