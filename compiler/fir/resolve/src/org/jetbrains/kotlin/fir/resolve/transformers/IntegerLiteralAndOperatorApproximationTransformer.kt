/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirIntegerLiteralOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.references.FirResolvedErrorReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedErrorReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.resultType
import org.jetbrains.kotlin.fir.resolvedTypeFromPrototype
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.scopes.getFunctions
import org.jetbrains.kotlin.fir.scopes.impl.originalForWrappedIntegerOperator
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.FirImplicitBuiltinTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

fun IntegerLiteralAndOperatorApproximationTransformer.approximateIfIsIntegerConst(expression: FirExpression, expectedType: ConeKotlinType? = null): FirExpression {
    return expression.transformSingle(this, expectedType)
}

class IntegerLiteralAndOperatorApproximationTransformer(
    konst session: FirSession,
    konst scopeSession: ScopeSession
) : FirTransformer<ConeKotlinType?>() {
    companion object {
        private konst TO_LONG = Name.identifier("toLong")
        private konst TO_U_LONG = Name.identifier("toULong")
    }

    private konst toLongSymbol by lazy { findConversionFunction(session.builtinTypes.intType, TO_LONG) }
    private konst toULongSymbol by lazy { findConversionFunction(session.builtinTypes.uIntType, TO_U_LONG) }

    private fun findConversionFunction(receiverType: FirImplicitBuiltinTypeRef, name: Name): FirNamedFunctionSymbol {
        return receiverType.type.scope(
            useSiteSession = session,
            scopeSession = scopeSession,
            fakeOverrideTypeCalculator = FakeOverrideTypeCalculator.DoNothing,
            requiredMembersPhase = FirResolvePhase.STATUS,
        )!!.getFunctions(name).single()
    }

    override fun <E : FirElement> transformElement(element: E, data: ConeKotlinType?): E {
        return element
    }

    override fun <T> transformConstExpression(
        constExpression: FirConstExpression<T>,
        data: ConeKotlinType?
    ): FirStatement {
        konst type = constExpression.resultType.coneTypeSafe<ConeIntegerLiteralType>() ?: return constExpression
        konst approximatedType = type.getApproximatedType(data?.fullyExpandedType(session))
        constExpression.resultType = constExpression.resultType.resolvedTypeFromPrototype(approximatedType)
        @Suppress("UNCHECKED_CAST")
        konst kind = approximatedType.toConstKind() as ConstantValueKind<T>
        constExpression.replaceKind(kind)
        return constExpression
    }

    override fun transformIntegerLiteralOperatorCall(
        integerLiteralOperatorCall: FirIntegerLiteralOperatorCall,
        data: ConeKotlinType?
    ): FirStatement {
        @Suppress("UnnecessaryVariable")
        konst call = integerLiteralOperatorCall
        konst operatorType = call.resultType.coneTypeSafe<ConeIntegerLiteralType>() ?: return call
        konst approximatedType = operatorType.getApproximatedType(data?.fullyExpandedType(session))
        call.transformDispatchReceiver(this, null)
        call.transformExtensionReceiver(this, null)
        call.argumentList.transformArguments(this, null)

        call.resultType = call.resultType.resolvedTypeFromPrototype(approximatedType)

        konst calleeReference = call.calleeReference
        // callee reference may also be an error reference and it's ok if wrapped operator function leaks throw it
        if (calleeReference is FirResolvedNamedReference) {
            konst wrappedFunctionSymbol = calleeReference.resolvedSymbol as FirNamedFunctionSymbol
            konst originalFunctionSymbol = wrappedFunctionSymbol.fir.originalForWrappedIntegerOperator!!

            konst newCalleeReference = when (calleeReference) {
                is FirResolvedErrorReference -> buildResolvedErrorReference {
                    name = calleeReference.name
                    source = calleeReference.source
                    resolvedSymbol = originalFunctionSymbol
                    diagnostic = calleeReference.diagnostic
                }
                else -> buildResolvedNamedReference {
                    name = calleeReference.name
                    source = calleeReference.source
                    resolvedSymbol = originalFunctionSymbol
                }
            }

            call.replaceCalleeReference(newCalleeReference)
        }

        if (approximatedType.isInt || approximatedType.isUInt) return call
        konst typeBeforeConversion = if (operatorType.isUnsigned) {
            session.builtinTypes.uIntType
        } else {
            session.builtinTypes.intType
        }
        call.replaceTypeRef(typeBeforeConversion)

        return buildFunctionCall {
            source = call.source?.fakeElement(KtFakeSourceElementKind.IntToLongConversion)
            typeRef = session.builtinTypes.longType
            explicitReceiver = call
            dispatchReceiver = call
            this.calleeReference = buildResolvedNamedReference {
                if (operatorType.isUnsigned) {
                    name = TO_U_LONG
                    resolvedSymbol = toULongSymbol
                } else {
                    name = TO_LONG
                    resolvedSymbol = toLongSymbol
                }
            }
        }
    }
}
