/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirErrorFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirErrorFunction : FirFunction(), FirDiagnosticHolder {
    abstract override konst source: KtSourceElement?
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst typeParameters: List<FirTypeParameterRef>
    abstract override konst status: FirDeclarationStatus
    abstract override konst returnTypeRef: FirTypeRef
    abstract override konst receiverParameter: FirReceiverParameter?
    abstract override konst deprecationsProvider: DeprecationsProvider
    abstract override konst containerSource: DeserializedContainerSource?
    abstract override konst dispatchReceiverType: ConeSimpleKotlinType?
    abstract override konst contextReceivers: List<FirContextReceiver>
    abstract override konst controlFlowGraphReference: FirControlFlowGraphReference?
    abstract override konst konstueParameters: List<FirValueParameter>
    abstract override konst body: FirBlock?
    abstract override konst diagnostic: ConeDiagnostic
    abstract override konst symbol: FirErrorFunctionSymbol

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitErrorFunction(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformErrorFunction(this, data) as E

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceStatus(newStatus: FirDeclarationStatus)

    abstract override fun replaceReturnTypeRef(newReturnTypeRef: FirTypeRef)

    abstract override fun replaceReceiverParameter(newReceiverParameter: FirReceiverParameter?)

    abstract override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider)

    abstract override fun replaceContextReceivers(newContextReceivers: List<FirContextReceiver>)

    abstract override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?)

    abstract override fun replaceValueParameters(newValueParameters: List<FirValueParameter>)

    abstract override fun replaceBody(newBody: FirBlock?)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirErrorFunction

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirErrorFunction

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirErrorFunction

    abstract override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirErrorFunction

    abstract override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirErrorFunction

    abstract override fun <D> transformValueParameters(transformer: FirTransformer<D>, data: D): FirErrorFunction

    abstract override fun <D> transformBody(transformer: FirTransformer<D>, data: D): FirErrorFunction
}
