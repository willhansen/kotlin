/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.commonizer.CommonizerTarget

interface CirRoot : CirDeclaration {
    konst target: CommonizerTarget

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun create(target: CommonizerTarget): CirRoot = CirRootImpl(target)
    }
}

data class CirRootImpl(
    override konst target: CommonizerTarget
) : CirRoot
