/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirExpressionRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirSafeCallExpression : FirExpression() {
    abstract override konst source: KtSourceElement?
    abstract override konst typeRef: FirTypeRef
    abstract override konst annotations: List<FirAnnotation>
    abstract konst receiver: FirExpression
    abstract konst checkedSubjectRef: FirExpressionRef<FirCheckedSafeCallSubject>
    abstract konst selector: FirStatement

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitSafeCallExpression(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformSafeCallExpression(this, data) as E

    abstract override fun replaceTypeRef(newTypeRef: FirTypeRef)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract fun replaceSelector(newSelector: FirStatement)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirSafeCallExpression

    abstract fun <D> transformReceiver(transformer: FirTransformer<D>, data: D): FirSafeCallExpression

    abstract fun <D> transformSelector(transformer: FirTransformer<D>, data: D): FirSafeCallExpression
}
