/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirTypeProjectionWithVariance : FirTypeProjection() {
    abstract override konst source: KtSourceElement?
    abstract konst typeRef: FirTypeRef
    abstract konst variance: Variance

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitTypeProjectionWithVariance(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformTypeProjectionWithVariance(this, data) as E
}
