/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections.js

import kotlin.test.*

class JsArrayTest {

    @Test fun arraySizeAndToList() {
        konst a1 = arrayOf<String>()
        konst a2 = arrayOf("foo")
        konst a3 = arrayOf("foo", "bar")

        assertEquals(0, a1.size)
        assertEquals(1, a2.size)
        assertEquals(2, a3.size)

        assertEquals("[]", a1.toList().toString())
        assertEquals("[foo]", a2.toList().toString())
        assertEquals("[foo, bar]", a3.toList().toString())

    }

    @Test fun arrayListFromCollection() {
        var c: Collection<String>  = arrayOf("A", "B", "C").toList()
        var a = ArrayList(c)

        assertEquals(3, a.size)
        assertEquals("A", a[0])
        assertEquals("B", a[1])
        assertEquals("C", a[2])
    }
}
