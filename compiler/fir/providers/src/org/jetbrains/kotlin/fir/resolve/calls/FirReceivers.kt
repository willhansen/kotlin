/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.copyWithNewSourceKind
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.diagnostics.ConeIntermediateDiagnostic
import org.jetbrains.kotlin.fir.expressions.FirCheckNotNullCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirSmartCastExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.builder.buildInaccessibleReceiverExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildSmartCastExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThisReceiverExpression
import org.jetbrains.kotlin.fir.references.builder.buildImplicitThisReference
import org.jetbrains.kotlin.fir.renderWithType
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.resolve.smartcastScope
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirScriptSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.SmartcastStability
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

interface Receiver

interface ReceiverValue : Receiver {
    konst type: ConeKotlinType

    konst receiverExpression: FirExpression

    fun scope(useSiteSession: FirSession, scopeSession: ScopeSession): FirTypeScope? = type.scope(
        useSiteSession = useSiteSession,
        scopeSession = scopeSession,
        fakeOverrideTypeCalculator = FakeOverrideTypeCalculator.DoNothing,
        requiredMembersPhase = FirResolvePhase.STATUS,
    )
}

// TODO: should inherit just Receiver, not ReceiverValue
abstract class AbstractExplicitReceiver<E : FirExpression> : Receiver {
    abstract konst explicitReceiver: FirExpression
}

abstract class AbstractExplicitReceiverValue<E : FirExpression> : AbstractExplicitReceiver<E>(), ReceiverValue {
    override konst type: ConeKotlinType
        // NB: safe cast is necessary here
        get() = explicitReceiver.typeRef.coneTypeSafe()
            ?: ConeErrorType(ConeIntermediateDiagnostic("No type calculated for: ${explicitReceiver.renderWithType()}")) // TODO: assert here

    override konst receiverExpression: FirExpression
        get() = explicitReceiver
}

open class ExpressionReceiverValue(
    override konst explicitReceiver: FirExpression
) : AbstractExplicitReceiverValue<FirExpression>(), ReceiverValue {
    override fun scope(useSiteSession: FirSession, scopeSession: ScopeSession): FirTypeScope? {
        var receiverExpr: FirExpression? = receiverExpression
        // Unwrap `x!!` to `x` and use the resulted expression to derive receiver type. This is necessary so that smartcast types inside
        // `!!` is handled correctly.
        if (receiverExpr is FirCheckNotNullCall) {
            receiverExpr = receiverExpr.arguments.firstOrNull()
        }

        if (receiverExpr is FirSmartCastExpression) {
            return receiverExpr.smartcastScope(
                useSiteSession,
                scopeSession,
                requiredMembersPhase = FirResolvePhase.STATUS,
            )
        }

        return type.scope(
            useSiteSession,
            scopeSession,
            FakeOverrideTypeCalculator.DoNothing,
            requiredMembersPhase = FirResolvePhase.STATUS,
        )
    }
}

sealed class ImplicitReceiverValue<S : FirBasedSymbol<*>>(
    konst boundSymbol: S,
    type: ConeKotlinType,
    protected konst useSiteSession: FirSession,
    protected konst scopeSession: ScopeSession,
    private konst mutable: Boolean,
    konst contextReceiverNumber: Int = -1,
    private konst inaccessibleReceiver: Boolean = false
) : ReceiverValue {
    final override var type: ConeKotlinType = type
        private set

    abstract konst isContextReceiver: Boolean

    konst originalType: ConeKotlinType = type

    konst expandedType: ConeKotlinType = type.applyIf(type is ConeClassLikeType) { fullyExpandedType(useSiteSession) }

    var implicitScope: FirTypeScope? =
        type.scope(
            useSiteSession,
            scopeSession,
            FakeOverrideTypeCalculator.DoNothing,
            requiredMembersPhase = FirResolvePhase.STATUS
        )
        private set

    override fun scope(useSiteSession: FirSession, scopeSession: ScopeSession): FirTypeScope? = implicitScope

    private konst originalReceiverExpression: FirExpression =
        receiverExpression(boundSymbol, type, contextReceiverNumber, inaccessibleReceiver)
    final override var receiverExpression: FirExpression = originalReceiverExpression
        private set

    @RequiresOptIn
    annotation class ImplicitReceiverInternals

    @Deprecated(level = DeprecationLevel.ERROR, message = "Builder inference should not modify implicit receivers. KT-54708")
    fun updateTypeInBuilderInference(type: ConeKotlinType) {
        this.type = type
        receiverExpression = receiverExpression(boundSymbol, type, contextReceiverNumber, inaccessibleReceiver)
        implicitScope = type.scope(
            useSiteSession = useSiteSession,
            scopeSession = scopeSession,
            fakeOverrideTypeCalculator = FakeOverrideTypeCalculator.DoNothing,
            requiredMembersPhase = FirResolvePhase.STATUS,
        )
    }

    /*
     * Should be called only in ImplicitReceiverStack
     */
    @ImplicitReceiverInternals
    fun updateTypeFromSmartcast(type: ConeKotlinType) {
        if (type == this.type) return
        if (!mutable) throw IllegalStateException("Cannot mutate an immutable ImplicitReceiverValue")
        this.type = type
        receiverExpression = if (type == originalReceiverExpression.typeRef.coneType) {
            originalReceiverExpression
        } else {
            buildSmartCastExpression {
                originalExpression = originalReceiverExpression
                this.source = originalExpression.source?.fakeElement(KtFakeSourceElementKind.SmartCastExpression)
                smartcastType = buildResolvedTypeRef {
                    source = originalReceiverExpression.typeRef.source?.fakeElement(KtFakeSourceElementKind.SmartCastedTypeRef)
                    this.type = type
                }
                typesFromSmartCast = listOf(type)
                smartcastStability = SmartcastStability.STABLE_VALUE
                typeRef = smartcastType.copyWithNewSourceKind(KtFakeSourceElementKind.ImplicitTypeRef)
            }
        }

        implicitScope = type.scope(
            useSiteSession = useSiteSession,
            scopeSession = scopeSession,
            fakeOverrideTypeCalculator = FakeOverrideTypeCalculator.DoNothing,
            requiredMembersPhase = FirResolvePhase.STATUS,
        )
    }

    abstract fun createSnapshot(): ImplicitReceiverValue<S>
}

private fun receiverExpression(
    symbol: FirBasedSymbol<*>,
    type: ConeKotlinType,
    contextReceiverNumber: Int,
    inaccessibleReceiver: Boolean
): FirExpression {
    // NB: we can't use `symbol.fir.source` as the source of `this` receiver. For instance, if this is an implicit receiver for a class,
    // the entire class itself will be set as a source. If combined with an implicit type operation, a certain assertion, like null
    // check assertion, will retrieve source as an assertion message, which is literally the entire class (!).
    konst calleeReference = buildImplicitThisReference {
        boundSymbol = symbol
        this.contextReceiverNumber = contextReceiverNumber
    }
    konst typeRef = type.toFirResolvedTypeRef()
    return when (inaccessibleReceiver) {
        false -> buildThisReceiverExpression {
            this.calleeReference = calleeReference
            this.typeRef = typeRef
            isImplicit = true
        }
        true -> buildInaccessibleReceiverExpression {
            this.calleeReference = calleeReference
            this.typeRef = typeRef
        }
    }
}

class ImplicitDispatchReceiverValue(
    boundSymbol: FirClassSymbol<*>,
    type: ConeKotlinType,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
) : ImplicitReceiverValue<FirClassSymbol<*>>(boundSymbol, type, useSiteSession, scopeSession, mutable) {
    constructor(
        boundSymbol: FirClassSymbol<*>, useSiteSession: FirSession, scopeSession: ScopeSession
    ) : this(
        boundSymbol, boundSymbol.constructType(typeArguments = emptyArray(), isNullable = false),
        useSiteSession, scopeSession
    )

    override fun createSnapshot(): ImplicitReceiverValue<FirClassSymbol<*>> {
        return ImplicitDispatchReceiverValue(boundSymbol, type, useSiteSession, scopeSession, false)
    }

    override konst isContextReceiver: Boolean
        get() = false
}

class ImplicitExtensionReceiverValue(
    boundSymbol: FirCallableSymbol<*>,
    type: ConeKotlinType,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
) : ImplicitReceiverValue<FirCallableSymbol<*>>(boundSymbol, type, useSiteSession, scopeSession, mutable) {
    override fun createSnapshot(): ImplicitReceiverValue<FirCallableSymbol<*>> {
        return ImplicitExtensionReceiverValue(boundSymbol, type, useSiteSession, scopeSession, false)
    }

    override konst isContextReceiver: Boolean
        get() = false
}


class InaccessibleImplicitReceiverValue(
    boundSymbol: FirClassSymbol<*>,
    type: ConeKotlinType,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
) : ImplicitReceiverValue<FirClassSymbol<*>>(boundSymbol, type, useSiteSession, scopeSession, mutable, inaccessibleReceiver = true) {
    override fun createSnapshot(): ImplicitReceiverValue<FirClassSymbol<*>> {
        return InaccessibleImplicitReceiverValue(boundSymbol, type, useSiteSession, scopeSession, false)
    }

    override konst isContextReceiver: Boolean
        get() = false
}

sealed class ContextReceiverValue<S : FirBasedSymbol<*>>(
    boundSymbol: S,
    type: ConeKotlinType,
    konst labelName: Name?,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
    contextReceiverNumber: Int,
) : ImplicitReceiverValue<S>(
    boundSymbol, type, useSiteSession, scopeSession, mutable, contextReceiverNumber,
) {
    abstract override fun createSnapshot(): ContextReceiverValue<S>

    override konst isContextReceiver: Boolean
        get() = true
}

class ContextReceiverValueForCallable(
    boundSymbol: FirCallableSymbol<*>,
    type: ConeKotlinType,
    labelName: Name?,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
    contextReceiverNumber: Int,
) : ContextReceiverValue<FirCallableSymbol<*>>(
    boundSymbol, type, labelName, useSiteSession, scopeSession, mutable, contextReceiverNumber
) {
    override fun createSnapshot(): ContextReceiverValue<FirCallableSymbol<*>> =
        ContextReceiverValueForCallable(boundSymbol, type, labelName, useSiteSession, scopeSession, mutable = false, contextReceiverNumber)
}

class ContextReceiverValueForClass(
    boundSymbol: FirClassSymbol<*>,
    type: ConeKotlinType,
    labelName: Name?,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
    contextReceiverNumber: Int,
) : ContextReceiverValue<FirClassSymbol<*>>(
    boundSymbol, type, labelName, useSiteSession, scopeSession, mutable, contextReceiverNumber
) {
    override fun createSnapshot(): ContextReceiverValue<FirClassSymbol<*>> =
        ContextReceiverValueForClass(boundSymbol, type, labelName, useSiteSession, scopeSession, mutable = false, contextReceiverNumber)
}

class ImplicitReceiverValueForScript(
    boundSymbol: FirScriptSymbol,
    type: ConeKotlinType,
    labelName: Name?,
    useSiteSession: FirSession,
    scopeSession: ScopeSession,
    mutable: Boolean = true,
    contextReceiverNumber: Int,
) : ContextReceiverValue<FirScriptSymbol>(
    boundSymbol, type, labelName, useSiteSession, scopeSession, mutable, contextReceiverNumber
) {
    override fun createSnapshot(): ContextReceiverValue<FirScriptSymbol> =
        ImplicitReceiverValueForScript(boundSymbol, type, labelName, useSiteSession, scopeSession, mutable = false, contextReceiverNumber)
}

