/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.FirImplementationDetail

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirImplicitInvokeCall : FirFunctionCall() {
    abstract override konst typeRef: FirTypeRef
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst contextReceiverArguments: List<FirExpression>
    abstract override konst typeArguments: List<FirTypeProjection>
    abstract override konst explicitReceiver: FirExpression?
    abstract override konst dispatchReceiver: FirExpression
    abstract override konst extensionReceiver: FirExpression
    abstract override konst source: KtSourceElement?
    abstract override konst argumentList: FirArgumentList
    abstract override konst calleeReference: FirNamedReference
    abstract override konst origin: FirFunctionCallOrigin

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitImplicitInvokeCall(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformImplicitInvokeCall(this, data) as E

    abstract override fun replaceTypeRef(newTypeRef: FirTypeRef)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceContextReceiverArguments(newContextReceiverArguments: List<FirExpression>)

    abstract override fun replaceTypeArguments(newTypeArguments: List<FirTypeProjection>)

    abstract override fun replaceExplicitReceiver(newExplicitReceiver: FirExpression?)

    abstract override fun replaceDispatchReceiver(newDispatchReceiver: FirExpression)

    abstract override fun replaceExtensionReceiver(newExtensionReceiver: FirExpression)

    @FirImplementationDetail
    abstract override fun replaceSource(newSource: KtSourceElement?)

    abstract override fun replaceArgumentList(newArgumentList: FirArgumentList)

    abstract override fun replaceCalleeReference(newCalleeReference: FirNamedReference)

    abstract override fun replaceCalleeReference(newCalleeReference: FirReference)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirImplicitInvokeCall

    abstract override fun <D> transformTypeArguments(transformer: FirTransformer<D>, data: D): FirImplicitInvokeCall

    abstract override fun <D> transformExplicitReceiver(transformer: FirTransformer<D>, data: D): FirImplicitInvokeCall

    abstract override fun <D> transformCalleeReference(transformer: FirTransformer<D>, data: D): FirImplicitInvokeCall
}
