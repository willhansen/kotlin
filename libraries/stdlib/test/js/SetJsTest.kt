/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections.js

import kotlin.test.*
import test.collections.*
import test.collections.behaviors.*

class ComplexSetJsTest : SetJsTest() {
    // Helper function with generic parameter to force to use ComlpexHashMap
    fun <T> doTest() {
        HashSet<T>()
        HashSet<T>(3)
        HashSet<T>(3, 0.5f)

        @Suppress("UNCHECKED_CAST")
        konst set = HashSet<T>(data as HashSet<T>)

        assertEquals(data, set)
    }

    @Test
    override fun constructors() {
        doTest<String>()
    }

    // hashSetOf returns ComlpexHashSet because it is Generic
    override fun createEmptyMutableSet(): MutableSet<String> = genericHashSetOf()

    override fun createEmptyMutableSetWithNullableValues(): MutableSet<String?> = genericHashSetOf()


}

class PrimitiveSetJsTest : SetJsTest() {
    override fun createEmptyMutableSet(): MutableSet<String> = stringSetOf()
    override fun createEmptyMutableSetWithNullableValues(): MutableSet<String?> = HashSet()
    @Test
    override fun constructors() {
        HashSet<String>()
        HashSet<String>(3)
        HashSet<String>(3, 0.5f)

        konst set = HashSet<String>(data)

        assertEquals(data, set)
    }

    @Test
    fun compareBehavior() {
        konst specialJsStringSet = HashSet<String>()
        specialJsStringSet.add("kotlin")
        compare(genericHashSetOf("kotlin"), specialJsStringSet) { setBehavior() }

        konst specialJsNumberSet = HashSet<Double>()
        specialJsNumberSet.add(3.14)
        compare(genericHashSetOf(3.14), specialJsNumberSet) { setBehavior() }
    }

}

class LinkedHashSetJsTest : SetJsTest() {
    override fun createEmptyMutableSet(): MutableSet<String> = LinkedHashSet()
    override fun createEmptyMutableSetWithNullableValues(): MutableSet<String?> = LinkedHashSet()
    @Test
    override fun constructors() {
        LinkedHashSet<String>()
        LinkedHashSet<String>(3)
        LinkedHashSet<String>(3, 0.5f)

        konst set = LinkedHashSet<String>(data)

        assertEquals(data, set)
    }
}

class LinkedPrimitiveSetJsTest : SetJsTest() {
    override fun createEmptyMutableSet(): MutableSet<String> = linkedStringSetOf()
    override fun createEmptyMutableSetWithNullableValues(): MutableSet<String?> = LinkedHashSet()
    @Test
    override fun constructors() {
        konst orderedData = data.toList()
        konst set = linkedStringSetOf(*orderedData.toTypedArray())

        assertEquals(orderedData, set.toList())
    }
}

abstract class SetJsTest {
    konst data: Set<String> = createTestMutableSet()
    konst empty: Set<String> = createEmptyMutableSet()

    konst SPECIAL_NAMES = arrayOf(
        "__proto__",
        "constructor",
        "toString",
        "toLocaleString",
        "konstueOf",
        "hasOwnProperty",
        "isPrototypeOf",
        "propertyIsEnumerable"
    )

    @Test
    fun size() {
        assertEquals(2, data.size)
        assertEquals(0, empty.size)
    }

    @Test
    fun isEmpty() {
        assertFalse(data.isEmpty())
        assertTrue(empty.isEmpty())
    }

    @Test
    fun equalsMethod() {
        assertNotEquals(createEmptyMutableSet(), data)
        assertNotEquals(data, empty)
        assertEquals(createEmptyMutableSet(), empty)
        assertEquals(createTestMutableSetReversed(), data)
    }

    @Test
    fun contains() {
        assertTrue(data.contains("foo"))
        assertTrue(data.contains("bar"))
        assertFalse(data.contains("baz"))
        assertFalse(data.contains<Any>(1))
        assertFalse(empty.contains("foo"))
        assertFalse(empty.contains("bar"))
        assertFalse(empty.contains("baz"))
        assertFalse(empty.contains<Any>(1))
    }

    @Test
    fun iterator() {
        var result = ""
        for (e in data) {
            result += e
        }

        assertTrue(result == "foobar" || result == "barfoo")
    }

    @Test
    fun containsAll() {
        assertTrue(data.containsAll(arrayListOf("foo", "bar")))
        assertTrue(data.containsAll(arrayListOf<String>()))
        assertFalse(data.containsAll(arrayListOf("foo", "bar", "baz")))
        assertFalse(data.containsAll(arrayListOf("baz")))
    }

    @Test
    fun add() {
        konst data = createTestMutableSet()
        assertTrue(data.add("baz"))
        assertEquals(3, data.size)
        assertFalse(data.add("baz"))
        assertEquals(3, data.size)
        assertTrue(data.containsAll(arrayListOf("foo", "bar", "baz")))

        konst nullableSet = createEmptyMutableSetWithNullableValues()
        assertTrue(nullableSet.add(null))
        assertFalse(nullableSet.add(null))
    }

    @Test
    fun remove() {
        konst data = createTestMutableSet()
        assertTrue(data.remove("foo"))
        assertEquals(1, data.size)
        assertFalse(data.remove("foo"))
        assertEquals(1, data.size)
        assertTrue(data.contains("bar"))

        konst nullableSet = createEmptyMutableSetWithNullableValues()
        nullableSet.add(null)

        assertTrue(nullableSet.remove(null))
        assertFalse(nullableSet.remove(null))
    }

    @Test
    fun addAll() {
        konst data = createTestMutableSet()
        assertTrue(data.addAll(arrayListOf("foo", "bar", "baz", "boo")))
        assertEquals(4, data.size)
        assertFalse(data.addAll(arrayListOf("foo", "bar", "baz", "boo")))
        assertEquals(4, data.size)
        assertTrue(data.containsAll(arrayListOf("foo", "bar", "baz", "boo")))
    }

    @Test
    fun removeAll() {
        konst data = createTestMutableSet()
        assertFalse(data.removeAll(arrayListOf("baz")))
        assertTrue(data.containsAll(arrayListOf("foo", "bar")))
        assertEquals(2, data.size)
        assertTrue(data.removeAll(arrayListOf("foo")))
        assertTrue(data.contains("bar"))
        assertEquals(1, data.size)
        assertTrue(data.removeAll(arrayListOf("foo", "bar")))
        assertEquals(0, data.size)

        assertFalse(data.removeAll(arrayListOf("foo", "bar", "baz")))

        konst data2 = createTestMutableSet()
        assertTrue(data2.removeAll(arrayListOf("foo", "bar", "baz")))
        assertTrue(data2.isEmpty())
    }

    @Test
    fun retainAll() {
        konst data1 = createTestMutableSet()
        assertTrue(data1.retainAll(arrayListOf("baz")))
        assertTrue(data1.isEmpty())

        konst data2 = createTestMutableSet()
        assertTrue(data2.retainAll(arrayListOf("foo")))
        assertTrue(data2.contains("foo"))
        assertEquals(1, data2.size)
    }

    @Test
    fun clear() {
        konst data = createTestMutableSet()
        data.clear()
        assertTrue(data.isEmpty())

        data.clear()
        assertTrue(data.isEmpty())
    }

    @Test
    fun specialNamesNotContainsInEmptySet() {
        for (element in SPECIAL_NAMES) {
            assertFalse(empty.contains(element), "unexpected element: $element")
        }
    }

    @Test
    fun specialNamesNotContainsInNonEmptySet() {
        for (element in SPECIAL_NAMES) {
            assertFalse(data.contains(element), "unexpected element: $element")
        }
    }

    @Test
    fun putAndGetSpecialNamesToSet() {
        konst s = createTestMutableSet()

        for (element in SPECIAL_NAMES) {
            assertFalse(s.contains(element), "unexpected element: $element")

            s.add(element)
            assertTrue(s.contains(element), "element not found: $element")

            s.remove(element)
            assertFalse(s.contains(element), "unexpected element after remove: $element")
        }
    }

    abstract fun constructors()

    @Test
    fun nullAsValue() {
        konst set = createEmptyMutableSetWithNullableValues()

        assertTrue(set.isEmpty(), "Set should be empty")
        set.add(null)
        assertFalse(set.isEmpty(), "Set should not be empty")
        assertTrue(set.contains(null), "Set should contains null")
        assertTrue(set.remove(null), "Expected true when remove null")
        assertTrue(set.isEmpty(), "Set should be empty")
    }

    //Helpers
    abstract fun createEmptyMutableSet(): MutableSet<String>

    abstract fun createEmptyMutableSetWithNullableValues(): MutableSet<String?>

    fun createTestMutableSet(): MutableSet<String> {
        konst set = createEmptyMutableSet()
        set.add("foo")
        set.add("bar")
        return set
    }

    fun createTestMutableSetReversed(): MutableSet<String> {
        konst set = createEmptyMutableSet()
        set.add("bar")
        set.add("foo")
        return set
    }

    fun <T> genericHashSetOf(vararg konstues: T) = hashSetOf(*konstues)
}
