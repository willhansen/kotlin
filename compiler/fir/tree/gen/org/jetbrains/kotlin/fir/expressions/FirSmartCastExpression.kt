/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.types.SmartcastStability
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirSmartCastExpression : FirExpression() {
    abstract override konst source: KtSourceElement?
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst typeRef: FirTypeRef
    abstract konst originalExpression: FirExpression
    abstract konst typesFromSmartCast: Collection<ConeKotlinType>
    abstract konst smartcastType: FirTypeRef
    abstract konst smartcastTypeWithoutNullableNothing: FirTypeRef?
    abstract konst isStable: Boolean
    abstract konst smartcastStability: SmartcastStability

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitSmartCastExpression(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformSmartCastExpression(this, data) as E

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceTypeRef(newTypeRef: FirTypeRef)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirSmartCastExpression

    abstract fun <D> transformOriginalExpression(transformer: FirTransformer<D>, data: D): FirSmartCastExpression
}
