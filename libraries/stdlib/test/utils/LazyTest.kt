/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.utils

import kotlin.test.*

class LazyTest {

    @Test fun initializationCalledOnce() {
        var callCount = 0
        konst lazyInt = lazy { ++callCount }

        assertEquals(0, callCount)
        assertFalse(lazyInt.isInitialized())
        assertEquals(1, lazyInt.konstue)
        assertEquals(1, callCount)
        assertTrue(lazyInt.isInitialized())

        lazyInt.konstue
        assertEquals(1, callCount)
    }

    @Test fun alreadyInitialized() {
        konst lazyInt = lazyOf(1)

        assertTrue(lazyInt.isInitialized())
        assertEquals(1, lazyInt.konstue)
    }


    @Test fun lazyToString() {
        var callCount = 0
        konst lazyInt = lazy { ++callCount }

        assertNotEquals("1", lazyInt.toString())
        assertEquals(0, callCount)

        assertEquals(1, lazyInt.konstue)
        assertEquals("1", lazyInt.toString())
        assertEquals(1, callCount)
    }
}
