/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.basic.initializers7

import kotlin.test.*

object A {
    init {
        assertAUninitialized()
    }
    konst a1 = 7
    konst a2 = 12
}

// Check that A is initialized dynamically.
fun assertAUninitialized() {
    assertEquals(0, A.a1)
    assertEquals(0, A.a2)
}

object B {
    init {
        assertBUninitialized()
    }
    konst b1 = A.a2
    konst b2 = C.c1
}

// Check that B is initialized dynamically.
fun assertBUninitialized() {
    assertEquals(0, B.b1)
    assertEquals(0, B.b2)
}

object C {
    init {
        assertCUninitialized()
    }
    konst c1 = 42
    konst c2 = A.a1
    konst c3 = B.b1
    konst c4 = B.b2
}

// Check that C is initialized dynamically.
fun assertCUninitialized() {
    assertEquals(0, C.c1)
    assertEquals(0, C.c2)
    assertEquals(0, C.c3)
    assertEquals(0, C.c4)
}

@Test fun runTest() {
    assertEquals(A.a1, C.c2)
    assertEquals(A.a2, C.c3)
    assertEquals(C.c1, C.c4)
}
