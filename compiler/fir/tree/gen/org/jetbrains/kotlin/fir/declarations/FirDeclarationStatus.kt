/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

interface FirDeclarationStatus : FirElement {
    override konst source: KtSourceElement?
    konst visibility: Visibility
    konst modality: Modality?
    konst isExpect: Boolean
    konst isActual: Boolean
    konst isOverride: Boolean
    konst isOperator: Boolean
    konst isInfix: Boolean
    konst isInline: Boolean
    konst isTailRec: Boolean
    konst isExternal: Boolean
    konst isConst: Boolean
    konst isLateInit: Boolean
    konst isInner: Boolean
    konst isCompanion: Boolean
    konst isData: Boolean
    konst isSuspend: Boolean
    konst isStatic: Boolean
    konst isFromSealedClass: Boolean
    konst isFromEnumClass: Boolean
    konst isFun: Boolean
    konst hasStableParameterNames: Boolean

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitDeclarationStatus(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformDeclarationStatus(this, data) as E
}
