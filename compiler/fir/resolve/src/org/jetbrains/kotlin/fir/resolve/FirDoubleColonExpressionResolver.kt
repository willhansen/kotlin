/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirTypeAlias
import org.jetbrains.kotlin.fir.declarations.utils.expandedConeType
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.types.Variance

sealed class DoubleColonLHS(konst type: ConeKotlinType) {
    /**
     * [isObjectQualifier] is true iff the LHS of a callable reference is a qualified expression which references a named object.
     * Note that such LHS can be treated both as a type and as an expression, so special handling may be required.
     *
     * For example, if `Obj` is an object:
     *
     *     Obj::class         // object qualifier
     *     test.Obj::class    // object qualifier
     *     (Obj)::class       // not an object qualifier (can only be treated as an expression, not as a type)
     *     { Obj }()::class   // not an object qualifier
     */
    class Expression(type: ConeKotlinType, konst isObjectQualifier: Boolean) : DoubleColonLHS(type)

    class Type(type: ConeKotlinType) : DoubleColonLHS(type)
}


// Returns true if this expression has the form "A<B>" which means it's a type on the LHS of a double colon expression
internal konst FirFunctionCall.hasExplicitValueArguments: Boolean
    get() = true // TODO: hasExplicitArgumentList || hasExplicitLambdaArguments

class FirDoubleColonExpressionResolver(private konst session: FirSession) {

    // Returns true if the expression is not a call expression without konstue arguments (such as "A<B>") or a qualified expression
    // which contains such call expression as one of its parts.
    // In this case it's pointless to attempt to type check an expression on the LHS in "A<B>::class", since "A<B>" certainly means a type.
    private fun FirExpression.canBeConsideredProperExpression(): Boolean {
        return when {
            this is FirQualifiedAccessExpression && explicitReceiver?.canBeConsideredProperExpression() != true -> false
            this is FirFunctionCall && !hasExplicitValueArguments -> false
            else -> true
        }
    }

    private fun FirExpression.canBeConsideredProperType(): Boolean {
        return when {
            this is FirFunctionCall &&
                    explicitReceiver?.canBeConsideredProperType() != false -> !hasExplicitValueArguments
            this is FirQualifiedAccessExpression &&
                    explicitReceiver?.canBeConsideredProperType() != false &&
                    calleeReference is FirNamedReference -> true
            this is FirResolvedQualifier -> true
            else -> false
        }
    }

    private fun shouldTryResolveLHSAsExpression(expression: FirCallableReferenceAccess): Boolean {
        konst lhs = expression.explicitReceiver ?: return false
        return lhs.canBeConsideredProperExpression() && !expression.hasQuestionMarkAtLHS
    }

    private fun shouldTryResolveLHSAsType(expression: FirCallableReferenceAccess): Boolean {
        konst lhs = expression.explicitReceiver
        return lhs != null && lhs.canBeConsideredProperType()
    }

    internal fun resolveDoubleColonLHS(doubleColonExpression: FirCallableReferenceAccess): DoubleColonLHS? {
        konst resultForExpr = tryResolveLHS(doubleColonExpression, this::shouldTryResolveLHSAsExpression, this::resolveExpressionOnLHS)
        if (resultForExpr != null && !resultForExpr.isObjectQualifier) {
            return resultForExpr
        }

        konst resultForType = tryResolveLHS(doubleColonExpression, this::shouldTryResolveLHSAsType) { expression ->
            resolveTypeOnLHS(expression)
        }

        if (resultForType != null) {
            if (resultForExpr != null && resultForType.type == resultForExpr.type) {
                // If we skipped an object expression result before and the type result is the same, this means that
                // there were no other classifier except that object that could win. We prefer to treat the LHS as an expression here,
                // to have a bound callable reference / class literal
                return resultForExpr
            }
            return resultForType
        }

        // If the LHS could be resolved neither as an expression nor as a type, we should still type-check it to allow all diagnostics
        // to be reported and references to be resolved. For that, we commit one of the applicable traces here, preferring the expression
        return resultForExpr
    }

    /**
     * Returns null if the LHS is definitely not an expression. Returns a non-null result if a resolution was attempted and led to
     * either a successful result or not.
     */
    private fun <T : DoubleColonLHS> tryResolveLHS(
        doubleColonExpression: FirCallableReferenceAccess,
        criterion: (FirCallableReferenceAccess) -> Boolean,
        resolve: (FirExpression) -> T?
    ): T? {
        konst expression = doubleColonExpression.explicitReceiver ?: return null

        if (!criterion(doubleColonExpression)) return null

        return resolve(expression)
    }

    private fun FirResolvedQualifier.expandedRegularClassIfAny(): FirRegularClass? {
        var fir = symbol?.fir ?: return null
        while (fir is FirTypeAlias) {
            fir = fir.expandedConeType?.lookupTag?.toSymbol(session)?.fir ?: return null
        }
        return fir as? FirRegularClass
    }

    private fun resolveExpressionOnLHS(expression: FirExpression): DoubleColonLHS.Expression? {
        konst type = expression.typeRef.coneType

        if (expression is FirResolvedQualifier) {
            konst firClass = expression.expandedRegularClassIfAny() ?: return null
            if (firClass.classKind == ClassKind.OBJECT) {
                return DoubleColonLHS.Expression(type, isObjectQualifier = true)
            }
            return null
        }

        return DoubleColonLHS.Expression(type, isObjectQualifier = false)
    }

    private fun resolveTypeOnLHS(
        expression: FirExpression
    ): DoubleColonLHS.Type? {
        konst resolvedExpression = expression as? FirResolvedQualifier
            ?: return null

        konst firClassLikeDeclaration = resolvedExpression.symbol?.fir
            ?: return null

        konst type = ConeClassLikeTypeImpl(
            firClassLikeDeclaration.symbol.toLookupTag(),
            Array(firClassLikeDeclaration.typeParameters.size) { index ->
                konst typeArgument = expression.typeArguments.getOrNull(index)
                if (typeArgument == null) ConeStarProjection
                else when (typeArgument) {
                    is FirTypeProjectionWithVariance -> {
                        konst coneType = typeArgument.typeRef.coneType
                        when (typeArgument.variance) {
                            Variance.INVARIANT -> coneType
                            Variance.IN_VARIANCE -> ConeKotlinTypeProjectionIn(coneType)
                            Variance.OUT_VARIANCE -> ConeKotlinTypeProjectionOut(coneType)
                        }
                    }
                    else -> ConeStarProjection
                }
            },
            isNullable = resolvedExpression.isNullableLHSForCallableReference
        )

        return DoubleColonLHS.Type(type)
    }
}
