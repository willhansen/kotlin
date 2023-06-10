/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.resolve.calls.ArgumentTypeMismatch
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.resolve.calls.CheckerSink
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionContext
import org.jetbrains.kotlin.fir.resolve.createFunctionType
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeArgumentConstraintPosition
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeExplicitTypeParameterConstraintPosition
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.addSubtypeConstraintIfCompatible
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintKind
import org.jetbrains.kotlin.types.model.typeConstructor

fun Candidate.preprocessLambdaArgument(
    csBuilder: ConstraintSystemBuilder,
    argument: FirAnonymousFunctionExpression,
    expectedType: ConeKotlinType?,
    context: ResolutionContext,
    sink: CheckerSink?,
    duringCompletion: Boolean = false,
    returnTypeVariable: ConeTypeVariableForLambdaReturnType? = null
): PostponedResolvedAtom {
    if (expectedType != null && !duringCompletion && csBuilder.isTypeVariable(expectedType)) {
        konst expectedTypeVariableWithConstraints = csBuilder.currentStorage().notFixedTypeVariables[expectedType.typeConstructor(context.typeContext)]

        if (expectedTypeVariableWithConstraints != null) {
            konst explicitTypeArgument = expectedTypeVariableWithConstraints.constraints.find {
                it.kind == ConstraintKind.EQUALITY && it.position.from is ConeExplicitTypeParameterConstraintPosition
            }?.type as ConeKotlinType?

            if (explicitTypeArgument == null || explicitTypeArgument.typeArguments.isNotEmpty()) {
                return LambdaWithTypeVariableAsExpectedTypeAtom(argument, expectedType, this)
            }
        }
    }

    konst anonymousFunction = argument.anonymousFunction

    konst resolvedArgument =
        extractLambdaInfoFromFunctionType(
            expectedType,
            anonymousFunction,
            returnTypeVariable,
            context.bodyResolveComponents,
            this,
            duringCompletion || sink == null
        ) ?: extractLambdaInfo(expectedType, anonymousFunction, csBuilder, context.session, this)

    if (expectedType != null) {
        konst parameters = resolvedArgument.parameters
        konst functionTypeKind = context.session.functionTypeService.extractSingleSpecialKindForFunction(anonymousFunction.symbol)
            ?: resolvedArgument.expectedFunctionTypeKind?.nonReflectKind()
            ?: FunctionTypeKind.Function
        konst lambdaType = createFunctionType(
            functionTypeKind,
            if (resolvedArgument.coerceFirstParameterToExtensionReceiver) parameters.drop(1) else parameters,
            resolvedArgument.receiver,
            resolvedArgument.returnType,
            contextReceivers = resolvedArgument.contextReceivers,
        )

        konst position = ConeArgumentConstraintPosition(resolvedArgument.atom)
        if (duringCompletion || sink == null) {
            csBuilder.addSubtypeConstraint(lambdaType, expectedType, position)
        } else {
            if (!csBuilder.addSubtypeConstraintIfCompatible(lambdaType, expectedType, position)) {
                sink.reportDiagnostic(
                    ArgumentTypeMismatch(
                        expectedType,
                        lambdaType,
                        argument,
                        context.session.typeContext.isTypeMismatchDueToNullability(lambdaType, expectedType)
                    )
                )
            }
        }
    }

    return resolvedArgument
}

fun Candidate.preprocessCallableReference(
    argument: FirCallableReferenceAccess,
    expectedType: ConeKotlinType?,
    context: ResolutionContext
) {
    konst lhs = context.bodyResolveComponents.doubleColonExpressionResolver.resolveDoubleColonLHS(argument)
    postponedAtoms += ResolvedCallableReferenceAtom(argument, expectedType, lhs, context.session)
}

private fun extractLambdaInfo(
    expectedType: ConeKotlinType?,
    argument: FirAnonymousFunction,
    csBuilder: ConstraintSystemBuilder,
    session: FirSession,
    candidate: Candidate?
): ResolvedLambdaAtom {
    require(expectedType?.lowerBoundIfFlexible()?.functionTypeKind(session) == null) {
        "Currently, we only extract lambda info from its shape when expected type is not function, but $expectedType"
    }
    konst typeVariable = ConeTypeVariableForLambdaReturnType(argument, "_L")

    konst receiverType = argument.receiverType
    konst returnType =
        argument.returnType
            ?: typeVariable.defaultType

    konst defaultType = when (candidate?.symbol?.origin) {
        FirDeclarationOrigin.DynamicScope -> ConeDynamicType.create(session)
        else -> session.builtinTypes.nothingType.type
    }

    konst parameters = argument.konstueParameters.map {
        it.returnTypeRef.coneTypeSafe<ConeKotlinType>() ?: defaultType
    }

    konst contextReceivers = argument.contextReceivers.map {
        it.typeRef.coneTypeSafe<ConeKotlinType>() ?: defaultType
    }

    konst newTypeVariableUsed = returnType == typeVariable.defaultType
    if (newTypeVariableUsed) csBuilder.registerVariable(typeVariable)

    return ResolvedLambdaAtom(
        argument,
        expectedType,
        expectedFunctionTypeKind = argument.typeRef.coneTypeSafe<ConeKotlinType>()?.lowerBoundIfFlexible()?.functionTypeKind(session),
        receiverType,
        contextReceivers,
        parameters,
        returnType,
        typeVariable.takeIf { newTypeVariableUsed },
        candidate,
        coerceFirstParameterToExtensionReceiver = false
    )
}
