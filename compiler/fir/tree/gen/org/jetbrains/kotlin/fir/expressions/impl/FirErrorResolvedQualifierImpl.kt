/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.expressions.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirErrorResolvedQualifier
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirImplicitTypeRefImplWithoutSource
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.MutableOrEmptyList
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirErrorResolvedQualifierImpl(
    override konst source: KtSourceElement?,
    override var annotations: MutableOrEmptyList<FirAnnotation>,
    override konst packageFqName: FqName,
    override konst relativeClassFqName: FqName?,
    override konst symbol: FirClassLikeSymbol<*>?,
    override var isNullableLHSForCallableReference: Boolean,
    override konst isFullyQualified: Boolean,
    override var nonFatalDiagnostics: MutableOrEmptyList<ConeDiagnostic>,
    override var typeArguments: MutableOrEmptyList<FirTypeProjection>,
    override konst diagnostic: ConeDiagnostic,
) : FirErrorResolvedQualifier() {
    override var typeRef: FirTypeRef = FirImplicitTypeRefImplWithoutSource
    override konst classId: ClassId? get() = relativeClassFqName?.let {
    ClassId(packageFqName, it, false)
}
    override konst resolvedToCompanionObject: Boolean get() = false

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        typeRef.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
        typeArguments.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirErrorResolvedQualifierImpl {
        typeRef = typeRef.transform(transformer, data)
        transformAnnotations(transformer, data)
        transformTypeArguments(transformer, data)
        return this
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirErrorResolvedQualifierImpl {
        annotations.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformTypeArguments(transformer: FirTransformer<D>, data: D): FirErrorResolvedQualifierImpl {
        typeArguments.transformInplace(transformer, data)
        return this
    }

    override fun replaceTypeRef(newTypeRef: FirTypeRef) {
        typeRef = newTypeRef
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        annotations = newAnnotations.toMutableOrEmpty()
    }

    override fun replaceIsNullableLHSForCallableReference(newIsNullableLHSForCallableReference: Boolean) {
        isNullableLHSForCallableReference = newIsNullableLHSForCallableReference
    }

    override fun replaceResolvedToCompanionObject(newResolvedToCompanionObject: Boolean) {}

    override fun replaceTypeArguments(newTypeArguments: List<FirTypeProjection>) {
        typeArguments = newTypeArguments.toMutableOrEmpty()
    }
}
