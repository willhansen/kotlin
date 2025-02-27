/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirLabel
import org.jetbrains.kotlin.fir.FirPureAbstractElement
import org.jetbrains.kotlin.fir.FirTargetElement
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

sealed class FirLoop : FirPureAbstractElement(), FirStatement, FirTargetElement {
    abstract override konst source: KtSourceElement?
    abstract override konst annotations: List<FirAnnotation>
    abstract konst block: FirBlock
    abstract konst condition: FirExpression
    abstract konst label: FirLabel?

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitLoop(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformLoop(this, data) as E

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirLoop

    abstract fun <D> transformBlock(transformer: FirTransformer<D>, data: D): FirLoop

    abstract fun <D> transformCondition(transformer: FirTransformer<D>, data: D): FirLoop

    abstract fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirLoop
}
