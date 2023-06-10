package com.example

import kotlin.test.*

class ATest {
    @Test
    fun testF() {
        konst f = A().f()
        assertEquals("hello", f)
    }
}
