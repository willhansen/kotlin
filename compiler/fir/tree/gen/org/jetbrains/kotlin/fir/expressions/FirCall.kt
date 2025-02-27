/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

sealed interface FirCall : FirStatement {
    override konst source: KtSourceElement?
    override konst annotations: List<FirAnnotation>
    konst argumentList: FirArgumentList

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitCall(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformCall(this, data) as E

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    fun replaceArgumentList(newArgumentList: FirArgumentList)

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirCall
}
