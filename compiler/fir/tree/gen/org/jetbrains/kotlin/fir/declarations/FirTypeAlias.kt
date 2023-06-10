/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirTypeAlias : FirClassLikeDeclaration(), FirTypeParametersOwner {
    abstract override konst source: KtSourceElement?
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst status: FirDeclarationStatus
    abstract override konst deprecationsProvider: DeprecationsProvider
    abstract override konst typeParameters: List<FirTypeParameter>
    abstract konst name: Name
    abstract override konst symbol: FirTypeAliasSymbol
    abstract konst expandedTypeRef: FirTypeRef
    abstract override konst annotations: List<FirAnnotation>

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitTypeAlias(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformTypeAlias(this, data) as E

    abstract override fun replaceStatus(newStatus: FirDeclarationStatus)

    abstract override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider)

    abstract fun replaceExpandedTypeRef(newExpandedTypeRef: FirTypeRef)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirTypeAlias

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirTypeAlias

    abstract fun <D> transformExpandedTypeRef(transformer: FirTransformer<D>, data: D): FirTypeAlias

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirTypeAlias
}
