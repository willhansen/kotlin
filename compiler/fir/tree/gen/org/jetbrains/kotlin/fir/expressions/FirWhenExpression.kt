/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirWhenExpression : FirExpression(), FirResolvable {
    abstract override konst source: KtSourceElement?
    abstract override konst typeRef: FirTypeRef
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst calleeReference: FirReference
    abstract konst subject: FirExpression?
    abstract konst subjectVariable: FirVariable?
    abstract konst branches: List<FirWhenBranch>
    abstract konst exhaustivenessStatus: ExhaustivenessStatus?
    abstract konst usedAsExpression: Boolean

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitWhenExpression(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformWhenExpression(this, data) as E

    abstract override fun replaceTypeRef(newTypeRef: FirTypeRef)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceCalleeReference(newCalleeReference: FirReference)

    abstract fun replaceExhaustivenessStatus(newExhaustivenessStatus: ExhaustivenessStatus?)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirWhenExpression

    abstract override fun <D> transformCalleeReference(transformer: FirTransformer<D>, data: D): FirWhenExpression

    abstract fun <D> transformSubject(transformer: FirTransformer<D>, data: D): FirWhenExpression

    abstract fun <D> transformBranches(transformer: FirTransformer<D>, data: D): FirWhenExpression

    abstract fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirWhenExpression
}
