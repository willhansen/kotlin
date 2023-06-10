/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirAnnotationCall : FirAnnotation(), FirCall, FirResolvable {
    abstract override konst source: KtSourceElement?
    abstract override konst typeRef: FirTypeRef
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst useSiteTarget: AnnotationUseSiteTarget?
    abstract override konst annotationTypeRef: FirTypeRef
    abstract override konst typeArguments: List<FirTypeProjection>
    abstract override konst argumentList: FirArgumentList
    abstract override konst calleeReference: FirReference
    abstract override konst argumentMapping: FirAnnotationArgumentMapping
    abstract konst annotationResolvePhase: FirAnnotationResolvePhase

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitAnnotationCall(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformAnnotationCall(this, data) as E

    abstract override fun replaceTypeRef(newTypeRef: FirTypeRef)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceUseSiteTarget(newUseSiteTarget: AnnotationUseSiteTarget?)

    abstract override fun replaceAnnotationTypeRef(newAnnotationTypeRef: FirTypeRef)

    abstract override fun replaceTypeArguments(newTypeArguments: List<FirTypeProjection>)

    abstract override fun replaceArgumentList(newArgumentList: FirArgumentList)

    abstract override fun replaceCalleeReference(newCalleeReference: FirReference)

    abstract override fun replaceArgumentMapping(newArgumentMapping: FirAnnotationArgumentMapping)

    abstract fun replaceAnnotationResolvePhase(newAnnotationResolvePhase: FirAnnotationResolvePhase)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirAnnotationCall

    abstract override fun <D> transformAnnotationTypeRef(transformer: FirTransformer<D>, data: D): FirAnnotationCall

    abstract override fun <D> transformTypeArguments(transformer: FirTransformer<D>, data: D): FirAnnotationCall

    abstract override fun <D> transformCalleeReference(transformer: FirTransformer<D>, data: D): FirAnnotationCall
}
