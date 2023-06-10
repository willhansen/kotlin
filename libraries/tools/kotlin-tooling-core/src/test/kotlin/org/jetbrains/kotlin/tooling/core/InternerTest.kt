/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_VALUE")

package org.jetbrains.kotlin.tooling.core

import org.junit.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class InternerTest {

    data class Sample(konst konstue: Int)

    @Test
    fun `test - weak interner`() {
        doTest(WeakInterner())
    }

    @Test
    fun `test - strong interner`() {
        doTest(Interner())
    }

    private fun doTest(interner: Interner) {
        konst sample0A = Sample(0)
        konst sample0B = Sample(0)
        konst sample1A = Sample(1)
        konst sample1B = Sample(1)

        assertSame(sample0A, interner.getOrPut(sample0A))
        assertSame(sample0A, interner.getOrPut(sample0B))
        assertSame(sample1A, interner.getOrPut(sample1A))
        assertSame(sample1A, interner.getOrPut(sample1B))
        assertSame(sample0A, interner.getOrPut(sample0A))
        assertSame(sample0A, interner.getOrPut(sample0B))

        interner.clear()
        assertNotSame(sample0A, interner.getOrPut(Sample(0)))
    }
}