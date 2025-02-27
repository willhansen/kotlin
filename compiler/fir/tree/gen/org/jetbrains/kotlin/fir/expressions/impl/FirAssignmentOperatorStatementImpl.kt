/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.expressions.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAssignmentOperatorStatement
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirOperation
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.MutableOrEmptyList
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirAssignmentOperatorStatementImpl(
    override konst source: KtSourceElement?,
    override var annotations: MutableOrEmptyList<FirAnnotation>,
    override konst operation: FirOperation,
    override var leftArgument: FirExpression,
    override var rightArgument: FirExpression,
) : FirAssignmentOperatorStatement() {
    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
        leftArgument.accept(visitor, data)
        rightArgument.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirAssignmentOperatorStatementImpl {
        transformAnnotations(transformer, data)
        transformLeftArgument(transformer, data)
        transformRightArgument(transformer, data)
        return this
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirAssignmentOperatorStatementImpl {
        annotations.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformLeftArgument(transformer: FirTransformer<D>, data: D): FirAssignmentOperatorStatementImpl {
        leftArgument = leftArgument.transform(transformer, data)
        return this
    }

    override fun <D> transformRightArgument(transformer: FirTransformer<D>, data: D): FirAssignmentOperatorStatementImpl {
        rightArgument = rightArgument.transform(transformer, data)
        return this
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        annotations = newAnnotations.toMutableOrEmpty()
    }
}
