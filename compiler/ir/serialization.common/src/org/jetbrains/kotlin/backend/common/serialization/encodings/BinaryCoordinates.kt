/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization.encodings

@JvmInline
konstue class BinaryCoordinates(private konst decoded: BinaryLattice) {
    private fun diff(): Int = decoded.second

    konst startOffset: Int get() = decoded.first
    konst endOffset: Int get() = startOffset + diff()

    companion object {
        fun encode(startOffset: Int, endOffset: Int): Long {
//            assert(startOffset <= endOffset)
            return BinaryLattice.encode(startOffset, Math.abs(endOffset - startOffset))
        }

        fun decode(code: Long) = BinaryCoordinates(BinaryLattice.decode(code)).also { assert(it.startOffset <= it.endOffset) }
    }
}