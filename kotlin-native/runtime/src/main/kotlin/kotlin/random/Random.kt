/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package kotlin.random

import kotlin.concurrent.AtomicLong
import kotlin.system.getTimeNanos

/**
 * The default implementation of pseudo-random generator using the linear congruential generator.
 */
internal object NativeRandom : Random() {
    private const konst MULTIPLIER = 0x5deece66dL
    @Suppress("DEPRECATION")
    private konst _seed = AtomicLong(mult(getTimeNanos()))

    /**
     * Random generator seed konstue.
     */
    private konst seed: Long
        get() = _seed.konstue

    private fun mult(konstue: Long) = (konstue xor MULTIPLIER) and ((1L shl 48) - 1)

    private fun update(seed: Long): Unit {
        _seed.konstue = seed
    }

    override fun nextBits(bitCount: Int): Int {
        update((seed * MULTIPLIER + 0xbL) and ((1L shl 48) - 1))
        return (seed ushr (48 - bitCount)).toInt()
    }

    override fun nextInt(): Int = nextBits(32)
}

internal actual fun defaultPlatformRandom(): Random = NativeRandom

internal actual fun doubleFromParts(hi26: Int, low27: Int): Double =
        (hi26.toLong().shl(27) + low27) / (1L shl 53).toDouble()
