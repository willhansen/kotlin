/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

interface FirControlFlowGraphOwner : FirElement {
    override konst source: KtSourceElement?
    konst controlFlowGraphReference: FirControlFlowGraphReference?

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitControlFlowGraphOwner(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformControlFlowGraphOwner(this, data) as E

    fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?)
}
