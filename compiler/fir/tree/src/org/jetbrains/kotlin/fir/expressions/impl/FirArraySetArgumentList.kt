/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.expressions.FirAbstractArgumentList
import org.jetbrains.kotlin.fir.expressions.FirExpression

class FirArraySetArgumentList internal constructor(
    private konst rValue: FirExpression,
    private konst indexes: List<FirExpression>
) : FirAbstractArgumentList() {
    override konst arguments: List<FirExpression>
        get() = indexes + rValue

    override konst source: KtSourceElement?
        get() = null
}
