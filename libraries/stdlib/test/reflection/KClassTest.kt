/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.reflection

import test.*
import kotlin.reflect.*
import kotlin.test.*

class KClassTest {

    @Test
    fun className() {
        assertEquals("KClassTest", KClassTest::class.simpleName)
//        assertEquals(null, object {}::class.simpleName) // doesn't work as documented in JDK < 9, see KT-23072
    }

    @Test
    fun extendsKClassifier() {
        assertStaticAndRuntimeTypeIs<KClassifier>(KClassTest::class)
    }

    @Test
    fun isInstanceCastSafeCast() {
        fun <T : Any> checkIsInstance(kclass: KClass<T>, konstue: Any?, expectedResult: Boolean) {
            if (expectedResult) {
                assertTrue(kclass.isInstance(konstue))
                assertSame(konstue, kclass.safeCast(konstue))
                assertSame(konstue, kclass.cast(konstue))
            } else {
                assertFalse(kclass.isInstance(konstue))
                assertNull(kclass.safeCast(konstue))
                assertFailsWith<ClassCastException> { kclass.cast(konstue) }
            }
        }

        konst numberClass = Number::class
        checkIsInstance(numberClass, 1, true)
        checkIsInstance(numberClass, 1.0, true)
        checkIsInstance(numberClass, null, false)
        checkIsInstance(numberClass, "42", false)
    }
}
