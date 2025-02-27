/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class HashCodeTest {
    @Test
    fun hashCodeOfNull() {
        assertEquals(0, null.hashCode())

        konst foo: Any? = null
        assertEquals(0, foo.hashCode())
    }

    @Test
    fun hashCodeOfNotNull() {
        konst konstue = "test"
        konst nullableValue: String? = konstue

        assertEquals(konstue.hashCode(), nullableValue.hashCode())
    }
}