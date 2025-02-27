/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirPureAbstractElement
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirCatch : FirPureAbstractElement(), FirElement {
    abstract override konst source: KtSourceElement?
    abstract konst parameter: FirProperty
    abstract konst block: FirBlock

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitCatch(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformCatch(this, data) as E

    abstract fun <D> transformParameter(transformer: FirTransformer<D>, data: D): FirCatch

    abstract fun <D> transformBlock(transformer: FirTransformer<D>, data: D): FirCatch

    abstract fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirCatch
}
