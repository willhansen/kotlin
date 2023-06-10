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
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousObjectSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirAnonymousObject : FirClass() {
    abstract override konst source: KtSourceElement?
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst typeParameters: List<FirTypeParameterRef>
    abstract override konst status: FirDeclarationStatus
    abstract override konst deprecationsProvider: DeprecationsProvider
    abstract override konst controlFlowGraphReference: FirControlFlowGraphReference?
    abstract override konst classKind: ClassKind
    abstract override konst superTypeRefs: List<FirTypeRef>
    abstract override konst declarations: List<FirDeclaration>
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst scopeProvider: FirScopeProvider
    abstract override konst symbol: FirAnonymousObjectSymbol

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitAnonymousObject(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformAnonymousObject(this, data) as E

    abstract override fun replaceStatus(newStatus: FirDeclarationStatus)

    abstract override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider)

    abstract override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?)

    abstract override fun replaceSuperTypeRefs(newSuperTypeRefs: List<FirTypeRef>)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirAnonymousObject

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirAnonymousObject

    abstract override fun <D> transformSuperTypeRefs(transformer: FirTransformer<D>, data: D): FirAnonymousObject

    abstract override fun <D> transformDeclarations(transformer: FirTransformer<D>, data: D): FirAnonymousObject

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirAnonymousObject
}
