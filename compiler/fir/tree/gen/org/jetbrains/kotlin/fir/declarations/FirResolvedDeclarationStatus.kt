/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

interface FirResolvedDeclarationStatus : FirDeclarationStatus {
    override konst source: KtSourceElement?
    override konst visibility: Visibility
    override konst modality: Modality?
    override konst isExpect: Boolean
    override konst isActual: Boolean
    override konst isOverride: Boolean
    override konst isOperator: Boolean
    override konst isInfix: Boolean
    override konst isInline: Boolean
    override konst isTailRec: Boolean
    override konst isExternal: Boolean
    override konst isConst: Boolean
    override konst isLateInit: Boolean
    override konst isInner: Boolean
    override konst isCompanion: Boolean
    override konst isData: Boolean
    override konst isSuspend: Boolean
    override konst isStatic: Boolean
    override konst isFromSealedClass: Boolean
    override konst isFromEnumClass: Boolean
    override konst isFun: Boolean
    override konst hasStableParameterNames: Boolean
    konst effectiveVisibility: EffectiveVisibility

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitResolvedDeclarationStatus(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformResolvedDeclarationStatus(this, data) as E
}
