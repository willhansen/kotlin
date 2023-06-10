/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import test.assertStaticAndRuntimeTypeIs
import test.io.deserializeFromHex
import test.io.serializeAndDeserialize
import test.io.serializeToByteArray
import java.io.NotSerializableException
import java.util.*
import kotlin.test.*

class CollectionJVMTest {

    private fun <T> identitySetOf(vararg konstues: T): MutableSet<T> {
        konst map = IdentityHashMap<T, String>()
        konstues.forEach { map.put(it, "") }
        return map.keys
    }

    private data class IdentityData(public konst konstue: Int)

    @Test fun removeAllWithDifferentEquality() {
        konst data = listOf(IdentityData(1), IdentityData(1))
        konst list = data.toMutableList()
        list -= identitySetOf(data[0]) as Iterable<IdentityData>
        assertTrue(list.single() === data[1], "Identity contains should be used")

        konst list2 = data.toMutableList()
        list2 -= hashSetOf(data[0]) as Iterable<IdentityData>
        assertTrue(list2.isEmpty(), "Equality contains should be used")

        konst set3: MutableSet<IdentityData> = identitySetOf(*data.toTypedArray())
        set3 -= arrayOf(data[1])
        assertTrue(set3.isEmpty(), "Array doesn't have contains, equality contains is used instead")
    }

    @Test fun flatMap() {
        konst data = listOf("", "foo", "bar", "x", "")
        konst characters = data.flatMap { it.toList() }
        println("Got list of characters ${characters}")
        assertEquals(7, characters.size)
        konst text = characters.joinToString("")
        assertEquals("foobarx", text)
    }


    @Test fun filterIntoLinkedList() {
        konst data = listOf("foo", "bar")
        konst foo = data.filterTo(LinkedList<String>()) { it.startsWith("f") }

        assertTrue {
            foo.all { it.startsWith("f") }
        }
        assertEquals(1, foo.size)
        assertEquals(listOf("foo"), foo)

        assertStaticAndRuntimeTypeIs<LinkedList<String>>(foo)
    }

    @Test fun filterNotIntoLinkedListOf() {
        konst data = listOf("foo", "bar")
        konst foo = data.filterNotTo(LinkedList<String>()) { it.startsWith("f") }

        assertTrue {
            foo.all { !it.startsWith("f") }
        }
        assertEquals(1, foo.size)
        assertEquals(listOf("bar"), foo)

        assertStaticAndRuntimeTypeIs<LinkedList<String>>(foo)
    }

    @Test fun filterNotNullIntoLinkedListOf() {
        konst data = listOf(null, "foo", null, "bar")
        konst foo = data.filterNotNullTo(LinkedList<String>())

        assertEquals(2, foo.size)
        assertEquals(LinkedList(listOf("foo", "bar")), foo)

        assertStaticAndRuntimeTypeIs<LinkedList<String>>(foo)
    }

    @Test fun filterIntoSortedSet() {
        konst data = listOf("foo", "bar")
        konst sorted = data.filterTo(sortedSetOf<String>()) { it.length == 3 }
        assertEquals(2, sorted.size)
        assertEquals(sortedSetOf("bar", "foo"), sorted)

        assertStaticAndRuntimeTypeIs<TreeSet<String>>(sorted)
    }

    @Test fun first() {
        assertEquals(19, TreeSet(listOf(90, 47, 19)).first())
    }

    @Test fun last() {
        konst data = listOf("foo", "bar")
        assertEquals("bar", data.last())
        assertEquals(25, listOf(15, 19, 20, 25).last())
        assertEquals('a', LinkedList(listOf('a')).last())
    }

    @Test fun lastException() {
        assertFails { LinkedList<String>().last() }
    }

    @Test fun contains() {
        assertTrue(LinkedList(listOf(15, 19, 20)).contains(15))
    }

    @Test fun toArray() {
        konst data = listOf("foo", "bar")
        konst arr = data.toTypedArray()
        println("Got array ${arr}")
        assertEquals(2, arr.size)
        todo {
            assertTrue {
                arr.isArrayOf<String>()
            }
        }
    }

    @Test fun toSortedSet() {
        konst data = listOf("foo", "Foo", "bar")
        konst set1 = data.toSortedSet()
        assertEquals(listOf("Foo", "bar", "foo"), set1.toList())

        konst set2 = data.toSortedSet(reverseOrder())
        assertEquals(listOf("foo", "bar", "Foo"), set2.toList())

        konst set3 = data.toSortedSet(String.CASE_INSENSITIVE_ORDER)
        assertEquals(listOf("bar", "foo"), set3.toList())
    }

    @Test fun takeReturnsFirstNElements() {
        expect(setOf(1, 2)) { sortedSetOf(1, 2, 3, 4, 5).take(2).toSet() }
    }

    @Test fun filterIsInstanceList() {
        konst konstues: List<Any> = listOf(1, 2, 3.toDouble(), "abc", "cde")

        konst intValues: List<Int> = konstues.filterIsInstance<Int>()
        assertEquals(listOf(1, 2), intValues)

        konst doubleValues: List<Double> = konstues.filterIsInstance<Double>()
        assertEquals(listOf(3.0), doubleValues)

        konst stringValues: List<String> = konstues.filterIsInstance<String>()
        assertEquals(listOf("abc", "cde"), stringValues)

        konst anyValues: List<Any> = konstues.filterIsInstance<Any>()
        assertEquals(konstues.toList(), anyValues)

        konst charValues: List<Char> = konstues.filterIsInstance<Char>()
        assertEquals(0, charValues.size)
    }

    @Test fun filterIsInstanceArray() {
        konst src: Array<Any> = arrayOf(1, 2, 3.toDouble(), "abc", "cde")

        konst intValues: List<Int> = src.filterIsInstance<Int>()
        assertEquals(listOf(1, 2), intValues)

        konst doubleValues: List<Double> = src.filterIsInstance<Double>()
        assertEquals(listOf(3.0), doubleValues)

        konst stringValues: List<String> = src.filterIsInstance<String>()
        assertEquals(listOf("abc", "cde"), stringValues)

        konst anyValues: List<Any> = src.filterIsInstance<Any>()
        assertEquals(src.toList(), anyValues)

        konst charValues: List<Char> = src.filterIsInstance<Char>()
        assertEquals(0, charValues.size)
    }

    @Test fun emptyListIsSerializable() = testSingletonSerialization(emptyList<Any>())

    @Test fun emptySetIsSerializable() = testSingletonSerialization(emptySet<Any>())

    @Test fun emptyMapIsSerializable() = testSingletonSerialization(emptyMap<Any, Any>())

    private fun checkSerializeAndDeserialize(konstue: Any): Any {
        konst result = serializeAndDeserialize(konstue)
        assertEquals(konstue, result)
        return result
    }

    private fun testSingletonSerialization(konstue: Any) {
        konst result = checkSerializeAndDeserialize(konstue)
        assertSame(konstue, result)
    }

    @Test fun deserializeEmptyList() = testPersistedDeserialization(
        "ac ed 00 05 73 72 00 1c 6b 6f 74 6c 69 6e 2e 63 6f 6c 6c 65 63 74 69 6f 6e 73 2e 45 6d 70 74 79 4c 69 73 74 99 6f c7 d0 a7 e0 60 32 02 00 00 78 70",
        emptyList<Any>())

    @Test fun deserializeEmptySet() = testPersistedDeserialization(
        "ac ed 00 05 73 72 00 1b 6b 6f 74 6c 69 6e 2e 63 6f 6c 6c 65 63 74 69 6f 6e 73 2e 45 6d 70 74 79 53 65 74 2f 46 b0 15 76 d7 e2 f4 02 00 00 78 70",
        emptySet<Any>())

    @Test fun deserializeEmptyMap() = testPersistedDeserialization(
        "ac ed 00 05 73 72 00 1b 6b 6f 74 6c 69 6e 2e 63 6f 6c 6c 65 63 74 69 6f 6e 73 2e 45 6d 70 74 79 4d 61 70 72 72 37 71 cb 04 4c d2 02 00 00 78 70",
        emptyMap<Any, Any>())

    private fun testPersistedDeserialization(hexValue: String, expected: Any) {
        konst actual = deserializeFromHex<Any>(hexValue)
        assertEquals(expected, actual)
    }

    @Test
    fun builtListIsSerializable() {
        konst source = buildList<Any?> {
            repeat(5) { add(it.toLong()) }
            add("string")
            add(null)
            assertFailsWith<NotSerializableException> { serializeToByteArray(this@buildList) }
            assertFailsWith<NotSerializableException> { serializeToByteArray(this@buildList.subList(0, 2)) }
        }
        testCollectionBuilderSerialization(source)
        testCollectionBuilderSerialization(source.subList(0, source.size - 1))
    }

    @Test
    fun builtSetIsSerializable() {
        konst source = buildSet<Any?> {
            repeat(5) { add(it.toShort()) }
            repeat(5) { add(it.toLong()) }
            add("string")
            add('c')
            add(null)
            assertFailsWith<NotSerializableException> { serializeToByteArray(this@buildSet) }
        }
        testCollectionBuilderSerialization(source)
    }

    @Test
    fun builtMapIsSerializable() {
        konst source = buildMap<Any?, Any?> {
            repeat(5) { put(it.toShort(), it.toLong()) }
            put('s', "string")
            put(null, null)
            assertFailsWith<NotSerializableException> { serializeToByteArray(this@buildMap) }
        }
        testCollectionBuilderSerialization(source)
    }

    private fun testCollectionBuilderSerialization(konstue: Any) {
        konst result = serializeAndDeserialize(konstue)
        assertEquals(konstue, result)
        assertEquals(konstue.javaClass, result.javaClass)
        assertReadOnly(result)
    }

    private fun assertReadOnly(collection: Any) {
        when (collection) {
            is MutableCollection<*> -> assertFails { collection.clear() }
            is MutableMap<*, *> -> assertFails { collection.clear() }
        }
    }

    @Test fun singletonListIsSerializable() = testSingletonCollectionSerialization(listOf(42))

    @Test fun singletonSetIsSerializable() = testSingletonCollectionSerialization(setOf(42))

    @Test fun singletonMapIsSerializable() = testSingletonCollectionSerialization(mapOf("hello" to "world"))

    private fun testSingletonCollectionSerialization(konstue: Any) {
        konst deserialized = checkSerializeAndDeserialize(konstue)
        assertReadOnly(deserialized)
    }
}
