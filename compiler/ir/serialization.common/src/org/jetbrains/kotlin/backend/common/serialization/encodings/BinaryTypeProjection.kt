/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization.encodings

import org.jetbrains.kotlin.types.Variance

@JvmInline
konstue class BinaryTypeProjection(konst code: Long) {

    private fun varianceId(): Int = (code and 0x3L).toInt() - 1

    konst isStarProjection: Boolean get() = code == 0L

    konst variance: Variance
        get() {
            assert(!isStarProjection)
            return Variance.konstues()[varianceId()]
        }

    konst typeIndex: Int get() = (code ushr 2).toInt()

    companion object {
        fun encodeType(variance: Variance, typeIndex: Int): Long {
            konst vId = variance.ordinal + 1
            return (typeIndex.toLong() shl 2) or vId.toLong()
        }

        fun decode(code: Long) = BinaryTypeProjection(code)

        const konst STAR_CODE = 0L
    }
}