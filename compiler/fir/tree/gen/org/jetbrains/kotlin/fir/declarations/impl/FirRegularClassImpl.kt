/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.declarations.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.declarations.DeprecationsProvider
import org.jetbrains.kotlin.fir.declarations.FirContextReceiver
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirResolveState
import org.jetbrains.kotlin.fir.declarations.FirTypeParameterRef
import org.jetbrains.kotlin.fir.declarations.asResolveState
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.MutableOrEmptyList
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirRegularClassImpl(
    override konst source: KtSourceElement?,
    resolvePhase: FirResolvePhase,
    override konst moduleData: FirModuleData,
    override konst origin: FirDeclarationOrigin,
    override konst attributes: FirDeclarationAttributes,
    override konst typeParameters: MutableList<FirTypeParameterRef>,
    override var status: FirDeclarationStatus,
    override var deprecationsProvider: DeprecationsProvider,
    override konst classKind: ClassKind,
    override konst declarations: MutableList<FirDeclaration>,
    override var annotations: MutableOrEmptyList<FirAnnotation>,
    override konst scopeProvider: FirScopeProvider,
    override konst name: Name,
    override konst symbol: FirRegularClassSymbol,
    override var companionObjectSymbol: FirRegularClassSymbol?,
    override konst superTypeRefs: MutableList<FirTypeRef>,
    override var contextReceivers: MutableOrEmptyList<FirContextReceiver>,
) : FirRegularClass() {
    override var controlFlowGraphReference: FirControlFlowGraphReference? = null
    override konst hasLazyNestedClassifiers: Boolean get() = false

    init {
        symbol.bind(this)
        @OptIn(ResolveStateAccess::class)
        resolveState = resolvePhase.asResolveState()
    }

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        typeParameters.forEach { it.accept(visitor, data) }
        status.accept(visitor, data)
        controlFlowGraphReference?.accept(visitor, data)
        declarations.forEach { it.accept(visitor, data) }
        annotations.forEach { it.accept(visitor, data) }
        superTypeRefs.forEach { it.accept(visitor, data) }
        contextReceivers.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirRegularClassImpl {
        transformTypeParameters(transformer, data)
        transformStatus(transformer, data)
        controlFlowGraphReference = controlFlowGraphReference?.transform(transformer, data)
        transformDeclarations(transformer, data)
        transformAnnotations(transformer, data)
        transformSuperTypeRefs(transformer, data)
        contextReceivers.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirRegularClassImpl {
        typeParameters.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirRegularClassImpl {
        status = status.transform(transformer, data)
        return this
    }

    override fun <D> transformDeclarations(transformer: FirTransformer<D>, data: D): FirRegularClassImpl {
        declarations.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirRegularClassImpl {
        annotations.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformSuperTypeRefs(transformer: FirTransformer<D>, data: D): FirRegularClassImpl {
        superTypeRefs.transformInplace(transformer, data)
        return this
    }

    override fun replaceStatus(newStatus: FirDeclarationStatus) {
        status = newStatus
    }

    override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider) {
        deprecationsProvider = newDeprecationsProvider
    }

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {
        controlFlowGraphReference = newControlFlowGraphReference
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        annotations = newAnnotations.toMutableOrEmpty()
    }

    override fun replaceCompanionObjectSymbol(newCompanionObjectSymbol: FirRegularClassSymbol?) {
        companionObjectSymbol = newCompanionObjectSymbol
    }

    override fun replaceSuperTypeRefs(newSuperTypeRefs: List<FirTypeRef>) {
        superTypeRefs.clear()
        superTypeRefs.addAll(newSuperTypeRefs)
    }
}
