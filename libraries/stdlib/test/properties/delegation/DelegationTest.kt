/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.properties.delegation

import kotlin.test.*
import kotlin.properties.*

class NotNullVarTest() {
    @Test fun doTest() {
        NotNullVarTestGeneric("a", "b").doTest()
    }
}

private class NotNullVarTestGeneric<T : Any>(konst a1: String, konst b1: T) {
    var a: String by Delegates.notNull()
    konst bDelegate = Delegates.notNull<T>()
    var b by bDelegate

    public fun doTest() {
        assertEquals("NotNullProperty(konstue not initialized yet)", bDelegate.toString())
        a = a1
        b = b1
        assertTrue(a == "a", "fail: a should be a, but was $a")
        assertTrue(b == "b", "fail: b should be b, but was $b")
        assertEquals("NotNullProperty(konstue=$b)", bDelegate.toString())
    }
}

class ObservablePropertyTest {
    var result = false

    konst bDelegate = Delegates.observable(1) { property, old, new ->
        assertEquals("b", property.name)
        if (!result) assertEquals(1, old)
        result = true
        assertEquals(new, b, "New konstue has already been set")
    }

    var b: Int by bDelegate

    @Test fun doTest() {
        b = 4
        assertTrue(b == 4, "fail: b != 4")
        assertTrue(result, "fail: result should be true")
        assertEquals("ObservableProperty(konstue=$b)", bDelegate.toString())
    }
}

class A(konst p: Boolean)

class VetoablePropertyTest {
    var result = false
    var b: A by Delegates.vetoable(A(true), { property, old, new ->
        assertEquals("b", property.name)
        assertEquals(old, b, "New konstue hasn't been set yet")
        result = new.p == true;
        result
    })

    @Test fun doTest() {
        konst firstValue = A(true)
        b = firstValue
        assertTrue(b == firstValue, "fail1: b should be firstValue = A(true)")
        assertTrue(result, "fail2: result should be true")
        b = A(false)
        assertTrue(b == firstValue, "fail3: b should be firstValue = A(true)")
        assertFalse(result, "fail4: result should be false")
    }
}

private typealias ReadOnlyPropertyDelegateProvider<T, V> =
        PropertyDelegateProvider<T, ReadOnlyProperty<T, V>>

class DelegationInterfacesTest {
    konst a by ReadOnlyProperty { _, property -> property.name }

    private konst delegatedToProvider = mutableListOf<String>()
    private konst provider = ReadOnlyPropertyDelegateProvider { thisRef: DelegationInterfacesTest, property ->
        konst index = thisRef.delegatedToProvider.size
        thisRef.delegatedToProvider.add(property.name)
        ReadOnlyProperty { _, prop -> "${prop.name} is numbered $index" }
    }

    konst b by provider
    konst c by provider

    @Test fun doTest() {
        assertEquals("a", a)
        assertEquals(listOf("b", "c"), delegatedToProvider)
        assertEquals("b is numbered 0", b)
        assertEquals("c is numbered 1", c)
    }
}