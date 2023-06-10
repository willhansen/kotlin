/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.declarations.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.declarations.FirAnonymousInitializer
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirResolveState
import org.jetbrains.kotlin.fir.declarations.asResolveState
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousInitializerSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.MutableOrEmptyList
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirAnonymousInitializerImpl(
    override konst source: KtSourceElement?,
    resolvePhase: FirResolvePhase,
    override konst moduleData: FirModuleData,
    override konst origin: FirDeclarationOrigin,
    override konst attributes: FirDeclarationAttributes,
    override var body: FirBlock?,
    override konst symbol: FirAnonymousInitializerSymbol,
    override konst dispatchReceiverType: ConeClassLikeType?,
) : FirAnonymousInitializer() {
    override konst annotations: List<FirAnnotation> get() = emptyList()
    override var controlFlowGraphReference: FirControlFlowGraphReference? = null

    init {
        symbol.bind(this)
        @OptIn(ResolveStateAccess::class)
        resolveState = resolvePhase.asResolveState()
    }

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        controlFlowGraphReference?.accept(visitor, data)
        body?.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirAnonymousInitializerImpl {
        controlFlowGraphReference = controlFlowGraphReference?.transform(transformer, data)
        body = body?.transform(transformer, data)
        return this
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirAnonymousInitializerImpl {
        return this
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {}

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {
        controlFlowGraphReference = newControlFlowGraphReference
    }

    override fun replaceBody(newBody: FirBlock?) {
        body = newBody
    }
}
