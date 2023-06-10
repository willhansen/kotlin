/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.symbols.impl.FirBackingFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirBackingField : FirVariable(), FirTypeParametersOwner, FirStatement {
    abstract override konst source: KtSourceElement?
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst returnTypeRef: FirTypeRef
    abstract override konst receiverParameter: FirReceiverParameter?
    abstract override konst deprecationsProvider: DeprecationsProvider
    abstract override konst containerSource: DeserializedContainerSource?
    abstract override konst dispatchReceiverType: ConeSimpleKotlinType?
    abstract override konst contextReceivers: List<FirContextReceiver>
    abstract override konst name: Name
    abstract override konst delegate: FirExpression?
    abstract override konst isVar: Boolean
    abstract override konst isVal: Boolean
    abstract override konst getter: FirPropertyAccessor?
    abstract override konst setter: FirPropertyAccessor?
    abstract override konst backingField: FirBackingField?
    abstract override konst symbol: FirBackingFieldSymbol
    abstract konst propertySymbol: FirPropertySymbol
    abstract override konst initializer: FirExpression?
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst typeParameters: List<FirTypeParameter>
    abstract override konst status: FirDeclarationStatus

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitBackingField(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformBackingField(this, data) as E

    abstract override fun replaceReturnTypeRef(newReturnTypeRef: FirTypeRef)

    abstract override fun replaceReceiverParameter(newReceiverParameter: FirReceiverParameter?)

    abstract override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider)

    abstract override fun replaceContextReceivers(newContextReceivers: List<FirContextReceiver>)

    abstract override fun replaceGetter(newGetter: FirPropertyAccessor?)

    abstract override fun replaceSetter(newSetter: FirPropertyAccessor?)

    abstract override fun replaceInitializer(newInitializer: FirExpression?)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceStatus(newStatus: FirDeclarationStatus)

    abstract override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformDelegate(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformGetter(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformSetter(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformBackingField(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformInitializer(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirBackingField

    abstract override fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirBackingField
}
