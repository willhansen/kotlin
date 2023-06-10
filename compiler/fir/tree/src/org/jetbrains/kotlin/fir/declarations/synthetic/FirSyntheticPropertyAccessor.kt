/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.synthetic

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.impl.FirEmptyContractDescription
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirPropertyAccessorImpl
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

class FirSyntheticPropertyAccessor(
    konst delegate: FirSimpleFunction,
    override konst isGetter: Boolean,
    override konst propertySymbol: FirPropertySymbol,
) : FirPropertyAccessor() {
    override konst source: KtSourceElement?
        get() = delegate.source

    override konst moduleData: FirModuleData
        get() = delegate.moduleData

    override konst origin: FirDeclarationOrigin
        get() = FirDeclarationOrigin.Synthetic

    override konst returnTypeRef: FirTypeRef
        get() = delegate.returnTypeRef

    override konst status: FirDeclarationStatus
        get() = delegate.status

    override konst dispatchReceiverType: ConeSimpleKotlinType?
        get() = delegate.dispatchReceiverType

    override konst receiverParameter: FirReceiverParameter?
        get() = null

    override konst deprecationsProvider: DeprecationsProvider
        get() = delegate.deprecationsProvider

    override konst konstueParameters: List<FirValueParameter>
        get() = delegate.konstueParameters

    override konst annotations: List<FirAnnotation>
        get() = delegate.annotations

    override konst typeParameters: List<FirTypeParameter>
        get() = emptyList()

    override konst isSetter: Boolean
        get() = !isGetter

    override konst body: FirBlock?
        get() = delegate.body

    override konst attributes: FirDeclarationAttributes
        get() = delegate.attributes

    override konst symbol: FirPropertyAccessorSymbol = FirPropertyAccessorSymbol().apply {
        bind(this@FirSyntheticPropertyAccessor)
    }

    override konst contextReceivers: List<FirContextReceiver>
        get() = emptyList()

    override konst controlFlowGraphReference: FirControlFlowGraphReference? = null

    override konst contractDescription: FirContractDescription = FirEmptyContractDescription

    override konst containerSource: DeserializedContainerSource? get() = null

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        delegate.accept(visitor, data)
        controlFlowGraphReference?.accept(visitor, data)
        contractDescription.accept(visitor, data)
    }

    override fun replaceBody(newBody: FirBlock?) {
        notSupported()
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirPropertyAccessorImpl {
        notSupported()
    }

    override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirPropertyAccessorImpl {
        notSupported()
    }

    override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirPropertyAccessorImpl {
        notSupported()
    }

    override fun <D> transformValueParameters(transformer: FirTransformer<D>, data: D): FirPropertyAccessorImpl {
        notSupported()
    }

    override fun <D> transformContractDescription(transformer: FirTransformer<D>, data: D): FirPropertyAccessorImpl {
        notSupported()
    }

    override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirPropertyAccessorImpl {
        notSupported()
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirPropertyAccessor {
        notSupported()
    }

    override fun <D> transformBody(transformer: FirTransformer<D>, data: D): FirPropertyAccessor {
        notSupported()
    }

    override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirPropertyAccessor {
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

    override fun replaceValueParameters(newValueParameters: List<FirValueParameter>) {
        notSupported()
    }

    override fun replaceContractDescription(newContractDescription: FirContractDescription) {
        notSupported()
    }

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {
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
        error("Mutation of synthetic property accessor isn't supported")
    }
}
