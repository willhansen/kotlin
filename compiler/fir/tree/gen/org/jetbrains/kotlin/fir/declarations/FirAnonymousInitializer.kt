/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousInitializerSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirAnonymousInitializer : FirDeclaration(), FirControlFlowGraphOwner {
    abstract override konst source: KtSourceElement?
    abstract override konst annotations: List<FirAnnotation>
    abstract override konst moduleData: FirModuleData
    abstract override konst origin: FirDeclarationOrigin
    abstract override konst attributes: FirDeclarationAttributes
    abstract override konst controlFlowGraphReference: FirControlFlowGraphReference?
    abstract konst body: FirBlock?
    abstract override konst symbol: FirAnonymousInitializerSymbol
    abstract konst dispatchReceiverType: ConeClassLikeType?

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitAnonymousInitializer(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformAnonymousInitializer(this, data) as E

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?)

    abstract fun replaceBody(newBody: FirBlock?)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirAnonymousInitializer
}
