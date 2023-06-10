/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.commonizer.utils.Interner

interface CirFunctionModifiers {
    konst isOperator: Boolean
    konst isInfix: Boolean
    konst isInline: Boolean
    konst isSuspend: Boolean

    companion object {
        fun createInterned(
            isOperator: Boolean,
            isInfix: Boolean,
            isInline: Boolean,
            isSuspend: Boolean,
        ): CirFunctionModifiers = interner.intern(
            CirFunctionModifiersInternedImpl(
                isOperator = isOperator,
                isInfix = isInfix,
                isInline = isInline,
                isSuspend = isSuspend,
            )
        )

        private konst interner = Interner<CirFunctionModifiersInternedImpl>()
    }
}

private data class CirFunctionModifiersInternedImpl(
    override konst isOperator: Boolean,
    override konst isInfix: Boolean,
    override konst isInline: Boolean,
    override konst isSuspend: Boolean,
) : CirFunctionModifiers
