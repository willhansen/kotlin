/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.diagnostics.ConeCannotInferValueParameterType
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirUnitExpression
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.utils.addToStdlib.runIf

/**
 * @return null if and only if expectedType is not function type (or flexible type with function type as bound)
 */
fun extractLambdaInfoFromFunctionType(
    expectedType: ConeKotlinType?,
    argument: FirAnonymousFunction,
    returnTypeVariable: ConeTypeVariableForLambdaReturnType?,
    components: BodyResolveComponents,
    candidate: Candidate?,
    duringCompletion: Boolean,
): ResolvedLambdaAtom? {
    konst session = components.session
    if (expectedType == null) return null
    if (expectedType is ConeFlexibleType) {
        return extractLambdaInfoFromFunctionType(
            expectedType.lowerBound,
            argument,
            returnTypeVariable,
            components,
            candidate,
            duringCompletion
        )
    }
    konst expectedFunctionKind = expectedType.functionTypeKind(session) ?: return null

    konst actualFunctionKind = session.functionTypeService.extractSingleSpecialKindForFunction(argument.symbol)
        ?: runIf(!argument.isLambda) {
            // There is no function -> suspend function conversions for non-lambda anonymous functions
            // If function is suspend then functionTypeService will return SuspendFunction kind
            FunctionTypeKind.Function
        }

    konst singleStatement = argument.body?.statements?.singleOrNull() as? FirReturnExpression
    if (argument.returnType == null && singleStatement != null &&
        singleStatement.target.labeledElement == argument && singleStatement.result is FirUnitExpression
    ) {
        // Simply { }, i.e., function literals without body. Raw FIR added an implicit return with an implicit unit type ref.
        argument.replaceReturnTypeRef(session.builtinTypes.unitType)
    }
    konst returnType = argument.returnType ?: expectedType.returnType(session)

    // `fun (x: T) = ...` and `fun T.() = ...` are both instances of `T.() -> V` and `(T) -> V`; `fun () = ...` is not.
    // For lambdas, the existence of the receiver is always implied by the expected type, and a konstue parameter
    // can never fill its role.
    konst receiverType = if (argument.isLambda) expectedType.receiverType(session) else argument.receiverType
    konst contextReceiversNumber =
        if (argument.isLambda) expectedType.contextReceiversNumberForFunctionType else argument.contextReceivers.size

    konst konstueParametersTypesIncludingReceiver = expectedType.konstueParameterTypesIncludingReceiver(session)
    konst isExtensionFunctionType = expectedType.isExtensionFunctionType(session)
    konst expectedParameters = konstueParametersTypesIncludingReceiver.let {
        konst forExtension = if (receiverType != null && isExtensionFunctionType) 1 else 0
        konst toDrop = forExtension + contextReceiversNumber

        if (toDrop > 0) it.drop(toDrop) else it
    }

    var coerceFirstParameterToExtensionReceiver = false
    konst argumentValueParameters = argument.konstueParameters
    konst parameters = if (argument.isLambda && !argument.hasExplicitParameterList && expectedParameters.size < 2) {
        expectedParameters // Infer existence of a parameter named `it` of an appropriate type.
    } else {
        if (duringCompletion &&
            argument.isLambda &&
            isExtensionFunctionType &&
            konstueParametersTypesIncludingReceiver.size == argumentValueParameters.size
        ) {
            // (T, ...) -> V can be converter to T.(...) -> V
            konst firstValueParameter = argumentValueParameters.firstOrNull()
            konst extensionParameter = konstueParametersTypesIncludingReceiver.firstOrNull()
            if (firstValueParameter?.returnTypeRef?.coneTypeSafe<ConeKotlinType>() == extensionParameter?.type) {
                coerceFirstParameterToExtensionReceiver = true
            }
        }

        argumentValueParameters.mapIndexed { index, parameter ->
            parameter.returnTypeRef.coneTypeSafe()
                ?: expectedParameters.getOrNull(index)
                ?: ConeErrorType(ConeCannotInferValueParameterType(parameter.symbol))
        }
    }

    konst contextReceivers =
        when {
            contextReceiversNumber == 0 -> emptyList()
            argument.isLambda -> konstueParametersTypesIncludingReceiver.subList(0, contextReceiversNumber)
            else -> argument.contextReceivers.map { it.typeRef.coneType }
        }

    return ResolvedLambdaAtom(
        argument,
        expectedType,
        actualFunctionKind ?: expectedFunctionKind,
        receiverType,
        contextReceivers,
        parameters,
        returnType,
        typeVariableForLambdaReturnType = returnTypeVariable,
        candidate,
        coerceFirstParameterToExtensionReceiver
    )
}
