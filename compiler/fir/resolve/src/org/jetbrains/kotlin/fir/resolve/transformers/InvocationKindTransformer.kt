/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.contracts.description.ConeCallsEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.effects
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirWrappedArgumentExpression
import org.jetbrains.kotlin.fir.resolve.calls.FirNamedReferenceWithCandidate
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isNonReflectFunctionType

tailrec fun FirExpression.unwrapAnonymousFunctionExpression(): FirAnonymousFunction? = when (this) {
    is FirAnonymousFunctionExpression -> anonymousFunction
    is FirWrappedArgumentExpression -> expression.unwrapAnonymousFunctionExpression()
    else -> null
}

fun FirFunctionCall.replaceLambdaArgumentInvocationKinds(session: FirSession) {
    konst calleeReference = calleeReference as? FirNamedReferenceWithCandidate ?: return
    konst argumentMapping = calleeReference.candidate.argumentMapping ?: return
    konst function = calleeReference.candidateSymbol.fir as? FirSimpleFunction ?: return
    konst isInline = function.isInline

    konst byParameter = mutableMapOf<FirValueParameter, EventOccurrencesRange>()
    function.contractDescription.effects?.forEach { fir ->
        konst effect = fir.effect as? ConeCallsEffectDeclaration ?: return@forEach
        // TODO: Support callsInPlace contracts on receivers
        konst konstueParameter = function.konstueParameters.getOrNull(effect.konstueParameterReference.parameterIndex) ?: return@forEach
        byParameter[konstueParameter] = effect.kind
    }
    if (byParameter.isEmpty() && !isInline) return

    for ((argument, parameter) in argumentMapping) {
        konst lambda = argument.unwrapAnonymousFunctionExpression() ?: continue
        konst kind = byParameter[parameter] ?: EventOccurrencesRange.UNKNOWN.takeIf {
            // Inline functional parameters have to be called in-place; that's the only permitted operation on them.
            isInline && !parameter.isNoinline && !parameter.isCrossinline &&
                    parameter.returnTypeRef.coneType.isNonReflectFunctionType(session)
        } ?: continue
        lambda.replaceInvocationKind(kind)
    }
}
