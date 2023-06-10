/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirErrorResolvedQualifier : FirResolvedQualifier(), FirDiagnosticHolder {
    abstract override konst source: KtSourceElement?
    abstract override konst typeRef: FirTypeRef
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst packageFqName: FqName
    abstract override konst relativeClassFqName: FqName?
    abstract override konst classId: ClassId?
    abstract override konst symbol: FirClassLikeSymbol<*>?
    abstract override konst isNullableLHSForCallableReference: Boolean
    abstract override konst resolvedToCompanionObject: Boolean
    abstract override konst isFullyQualified: Boolean
    abstract override konst nonFatalDiagnostics: List<ConeDiagnostic>
    abstract override konst typeArguments: List<FirTypeProjection>
    abstract override konst diagnostic: ConeDiagnostic

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitErrorResolvedQualifier(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformErrorResolvedQualifier(this, data) as E

    abstract override fun replaceTypeRef(newTypeRef: FirTypeRef)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceIsNullableLHSForCallableReference(newIsNullableLHSForCallableReference: Boolean)

    abstract override fun replaceResolvedToCompanionObject(newResolvedToCompanionObject: Boolean)

    abstract override fun replaceTypeArguments(newTypeArguments: List<FirTypeProjection>)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirErrorResolvedQualifier

    abstract override fun <D> transformTypeArguments(transformer: FirTransformer<D>, data: D): FirErrorResolvedQualifier
}
