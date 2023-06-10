/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.synthetic

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirDelegateFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirSyntheticPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

class FirSyntheticProperty(
    override konst moduleData: FirModuleData,
    override konst name: Name,
    override konst isVar: Boolean,
    override konst symbol: FirSyntheticPropertySymbol,
    override konst status: FirDeclarationStatus,
    resolvePhase: FirResolvePhase,
    override konst getter: FirSyntheticPropertyAccessor,
    override konst setter: FirSyntheticPropertyAccessor? = null,
    override konst backingField: FirBackingField? = null,
    override konst deprecationsProvider: DeprecationsProvider = UnresolvedDeprecationProvider
) : FirProperty() {
    init {
        symbol.bind(this)
    }

    init {
        @OptIn(ResolveStateAccess::class)
        this.resolveState = resolvePhase.asResolveState()
    }

    override konst returnTypeRef: FirTypeRef
        get() = getter.returnTypeRef

    override konst dispatchReceiverType: ConeSimpleKotlinType?
        get() = getter.dispatchReceiverType

    override konst source: KtSourceElement?
        get() = null

    override konst origin: FirDeclarationOrigin
        get() = FirDeclarationOrigin.Synthetic

    override konst initializer: FirExpression?
        get() = null

    override konst delegate: FirExpression?
        get() = null

    override konst delegateFieldSymbol: FirDelegateFieldSymbol?
        get() = null

    override konst isLocal: Boolean
        get() = false

    override konst receiverParameter: FirReceiverParameter?
        get() = null

    override konst isVal: Boolean
        get() = !isVar

    override konst annotations: List<FirAnnotation>
        get() = emptyList()

    override konst typeParameters: List<FirTypeParameter>
        get() = emptyList()

    override konst containerSource: DeserializedContainerSource?
        get() = null

    override konst controlFlowGraphReference: FirControlFlowGraphReference? = null

    override konst attributes: FirDeclarationAttributes = FirDeclarationAttributes()

    override konst bodyResolveState: FirPropertyBodyResolveState
        get() = FirPropertyBodyResolveState.EVERYTHING_RESOLVED

    override konst contextReceivers: List<FirContextReceiver>
        get() = emptyList()

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        returnTypeRef.accept(visitor, data)
        status.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformInitializer(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformGetter(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformSetter(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformBackingField(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformDelegate(transformer: FirTransformer<D>, data: D): FirProperty {
        notSupported()
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirSyntheticProperty {
        notSupported()
    }

    override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirProperty {
        notSupported()
    }

    override fun <D> transformContextReceivers(transformer: FirTransformer<D>, data: D): FirProperty {
        notSupported()
    }

    override fun replaceReturnTypeRef(newReturnTypeRef: FirTypeRef) {
        notSupported()
    }

    override fun replaceReceiverParameter(newReceiverParameter: FirReceiverParameter?) {
        notSupported()
    }

    override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider) {
        notSupported()
    }

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {
        notSupported()
    }

    override fun replaceInitializer(newInitializer: FirExpression?) {
        notSupported()
    }

    override fun replaceBodyResolveState(newBodyResolveState: FirPropertyBodyResolveState) {
        notSupported()
    }

    override fun replaceGetter(newGetter: FirPropertyAccessor?) {
        notSupported()
    }

    override fun replaceSetter(newSetter: FirPropertyAccessor?) {
        notSupported()
    }

    override fun replaceContextReceivers(newContextReceivers: List<FirContextReceiver>) {
        notSupported()
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        notSupported()
    }

    override fun replaceStatus(newStatus: FirDeclarationStatus) {
        notSupported()
    }

    private fun notSupported(): Nothing {
        error("Transformation of synthetic property isn't supported")
    }
}
