/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import kotlin.test.Test
import kotlin.test.assertEquals

class Tests {
    @Test
    fun test1() {
        assertEquals("hello", process.env.hello)
    }

    @Test
    fun test2() {
        assertEquals("foo", foo.foo())
    }

    @Test
    fun test3() {
        assertEquals(process.title, "KGP_CUSTOM_NODEJS_TITLE")
    }
}

external konst process: dynamic