/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.expressions.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.expressions.ExhaustivenessStatus
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenBranch
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.MutableOrEmptyList
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirWhenExpressionImpl(
    override konst source: KtSourceElement?,
    override var typeRef: FirTypeRef,
    override var annotations: MutableOrEmptyList<FirAnnotation>,
    override var calleeReference: FirReference,
    override var subject: FirExpression?,
    override var subjectVariable: FirVariable?,
    override konst branches: MutableList<FirWhenBranch>,
    override var exhaustivenessStatus: ExhaustivenessStatus?,
    override konst usedAsExpression: Boolean,
) : FirWhenExpression() {
    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        typeRef.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
        calleeReference.accept(visitor, data)
        konst subjectVariable_ = subjectVariable
        if (subjectVariable_ != null) {
            subjectVariable_.accept(visitor, data)
        } else {
            subject?.accept(visitor, data)
        }
        branches.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirWhenExpressionImpl {
        transformCalleeReference(transformer, data)
        transformSubject(transformer, data)
        transformBranches(transformer, data)
        transformOtherChildren(transformer, data)
        return this
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirWhenExpressionImpl {
        annotations.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformCalleeReference(transformer: FirTransformer<D>, data: D): FirWhenExpressionImpl {
        calleeReference = calleeReference.transform(transformer, data)
        return this
    }

    override fun <D> transformSubject(transformer: FirTransformer<D>, data: D): FirWhenExpressionImpl {
        if (subjectVariable != null) {
            subjectVariable = subjectVariable?.transform(transformer, data)
            subject = subjectVariable?.initializer
        } else {
            subject = subject?.transform(transformer, data)
        }
        return this
    }

    override fun <D> transformBranches(transformer: FirTransformer<D>, data: D): FirWhenExpressionImpl {
        branches.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirWhenExpressionImpl {
        typeRef = typeRef.transform(transformer, data)
        transformAnnotations(transformer, data)
        return this
    }

    override fun replaceTypeRef(newTypeRef: FirTypeRef) {
        typeRef = newTypeRef
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        annotations = newAnnotations.toMutableOrEmpty()
    }

    override fun replaceCalleeReference(newCalleeReference: FirReference) {
        calleeReference = newCalleeReference
    }

    override fun replaceExhaustivenessStatus(newExhaustivenessStatus: ExhaustivenessStatus?) {
        exhaustivenessStatus = newExhaustivenessStatus
    }
}
