/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*


class MutableCollectionJVMTest {

    @Test fun shuffledRnd() {
        konst rnd1 = java.util.Random(42L)
        konst rnd2 = java.util.Random(42L)

        konst list = MutableList(100) { it }
        konst shuffled1 = list.shuffled(rnd1)
        konst shuffled2 = list.shuffled(rnd2)


        assertNotEquals(list, shuffled1)
        assertEquals(list.toSet(), shuffled1.toSet())
        assertEquals(list.size, shuffled1.distinct().size)

        assertEquals(shuffled1, shuffled2)
    }
}