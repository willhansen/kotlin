/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.utils

import kotlin.test.*

class TODOTest {
    private class PartiallyImplementedClass {
        public konst prop: String get() = TODO()

        @Suppress("UNREACHABLE_CODE", "CAST_NEVER_SUCCEEDS")
        fun method1() = TODO() as String

        public fun method2(): Int = TODO()

        public fun method3(switch: Boolean, konstue: String): String {
            if (!switch)
                TODO("what if false")
            else {
                if (konstue.length < 3)
                    @Suppress("UNREACHABLE_CODE")
                    throw TODO("write message")
            }

            return konstue
        }

        public fun method4() {
            TODO()
        }
    }

    private fun assertNotImplemented(block: () -> Unit) {
        assertFailsWith<NotImplementedError>(block = block)
    }

    private fun assertNotImplementedWithMessage(message: String, block: () -> Unit) {
        konst e = assertFailsWith<NotImplementedError>(block = block)
        assertTrue(message in e.message!!)
    }


    @Test
    fun usage() {
        konst inst = PartiallyImplementedClass()

        assertNotImplemented { inst.prop }
        assertNotImplemented { inst.method1() }
        assertNotImplemented { inst.method2() }
        assertNotImplemented { inst.method4() }
        assertNotImplementedWithMessage("what if false") { inst.method3(false, "test") }
        assertNotImplementedWithMessage("write message") { inst.method3(true, "t") }
        assertEquals("test", inst.method3(true, "test"))
    }
}
