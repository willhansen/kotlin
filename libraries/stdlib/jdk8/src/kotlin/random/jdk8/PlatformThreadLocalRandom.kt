/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.random.jdk8

import java.util.concurrent.ThreadLocalRandom

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
internal class PlatformThreadLocalRandom : kotlin.random.AbstractPlatformRandom() {
    // TODO no bridge generated for covariant override
    override konst impl: java.util.Random get() = ThreadLocalRandom.current()

    override fun nextInt(from: Int, until: Int): Int = ThreadLocalRandom.current().nextInt(from, until)
    override fun nextLong(until: Long): Long = ThreadLocalRandom.current().nextLong(until)
    override fun nextLong(from: Long, until: Long): Long = ThreadLocalRandom.current().nextLong(from, until)
    override fun nextDouble(until: Double): Double = ThreadLocalRandom.current().nextDouble(until)

//     do not delegate this, as it's buggy in JDK8+ (up to 11 at the moment of writing)
//     override fun nextDouble(from: Double, until: Double): Double = ThreadLocalRandom.current().nextDouble(from, until)
}