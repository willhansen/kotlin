/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

sealed class FirClassLikeDeclaration : FirMemberDeclaration(), FirStatement {
    abstract override konst source: KtSourceElement?
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst typeParameters: List<FirTypeParameterRef>
    abstract override konst status: FirDeclarationStatus
    abstract override konst symbol: FirClassLikeSymbol<out FirClassLikeDeclaration>
    abstract konst deprecationsProvider: DeprecationsProvider

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitClassLikeDeclaration(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformClassLikeDeclaration(this, data) as E

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceStatus(newStatus: FirDeclarationStatus)

    abstract fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirClassLikeDeclaration

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirClassLikeDeclaration

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirClassLikeDeclaration
}
