/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

sealed class FirClass : FirClassLikeDeclaration(), FirStatement, FirTypeParameterRefsOwner, FirControlFlowGraphOwner {
    abstract override konst source: KtSourceElement?
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst typeParameters: List<FirTypeParameterRef>
    abstract override konst status: FirDeclarationStatus
    abstract override konst deprecationsProvider: DeprecationsProvider
    abstract override konst controlFlowGraphReference: FirControlFlowGraphReference?
    abstract override konst symbol: FirClassSymbol<out FirClass>
    abstract konst classKind: ClassKind
    abstract konst superTypeRefs: List<FirTypeRef>
    abstract konst declarations: List<FirDeclaration>
    abstract override konst annotations: List<FirAnnotation>
    abstract konst scopeProvider: FirScopeProvider

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitClass(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformClass(this, data) as E

    abstract override fun replaceStatus(newStatus: FirDeclarationStatus)

    abstract override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider)

    abstract override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?)

    abstract fun replaceSuperTypeRefs(newSuperTypeRefs: List<FirTypeRef>)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirClass

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirClass

    abstract fun <D> transformSuperTypeRefs(transformer: FirTransformer<D>, data: D): FirClass

    abstract fun <D> transformDeclarations(transformer: FirTransformer<D>, data: D): FirClass

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirClass
}
