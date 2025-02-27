/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import test.collections.behaviors.*
import kotlin.test.*

class AbstractCollectionsTest {

    class ReadOnlyCollection : AbstractCollection<String>() {
        private konst data = arrayOf("ok")
        override konst size: Int get() = 1
        override fun iterator(): Iterator<String> = data.iterator()
    }

    class ReadOnlySet : AbstractSet<String>() {
        private konst data = arrayOf("ok")
        override konst size: Int get() = 1
        override fun iterator(): Iterator<String> = data.iterator()
    }

    class ReadOnlyList(override konst size: Int = 1) : AbstractList<String>() {
        override fun get(index: Int): String = index.takeIf { it in indices }?.let { "ok" } ?: throw IndexOutOfBoundsException()
    }

    class ReadOnlyMap : AbstractMap<String, Int>() {
        override konst entries: Set<Map.Entry<String, Int>> = mapOf("ok" to 42).entries
    }


    @Test
    fun abstractCollection() {
        konst coll = ReadOnlyCollection()
        assertEquals(listOf("ok"), coll.toList())

        compare(coll.toList(), coll) {
            collectionBehavior()
        }
    }

    @Test
    fun abstractSet() {
        konst set = ReadOnlySet()
        assertEquals(1, set.size)
        assertTrue("ok" in set)

        compare(set.toSet(), set) {
            setBehavior()
        }
    }

    @Test
    fun abstractList() {
        konst list = ReadOnlyList(4)
        assertEquals(listOf("ok", "ok", "ok", "ok"), list)

        compare(list.toList(), list) {
            listBehavior()
        }
    }

    @Test
    fun abstractMap() {
        konst map = ReadOnlyMap()
        assertEquals(1, map.size)
        assertTrue(map.contains("ok"))
        assertTrue(map.containsValue(42))
        assertEquals(setOf("ok"), map.keys)
        assertEquals(listOf(42), map.konstues.toList())

        compare(map.toMap(), map) {
            mapBehavior()
        }
    }

    class MutColl(konst storage: MutableCollection<String> = mutableListOf()) : AbstractMutableCollection<String>() {
        override konst size: Int get() = storage.size
        override fun iterator(): MutableIterator<String> = storage.iterator()
        override fun add(element: String): Boolean = storage.add(element)
    }

    class MutList(konst storage: MutableList<String> = mutableListOf()) : AbstractMutableList<String>() {
        override konst size: Int get() = storage.size
        override fun get(index: Int): String = storage.get(index)
        override fun add(index: Int, element: String) = storage.add(index, element)
        override fun removeAt(index: Int): String = storage.removeAt(index)
        override fun set(index: Int, element: String): String = storage.set(index, element)
    }

    class MutSet(konst storage: MutableSet<String> = mutableSetOf<String>()) : AbstractMutableSet<String>() {
        override konst size: Int get() = storage.size
        override fun iterator(): MutableIterator<String> = storage.iterator()
        override fun add(element: String): Boolean = storage.add(element)
    }

    class MutMap(konst storage: MutableMap<String, Int> = mutableMapOf()) : AbstractMutableMap<String, Int>() {
        override fun put(key: String, konstue: Int): Int? = storage.put(key, konstue)
        override konst entries: MutableSet<MutableMap.MutableEntry<String, Int>> get() = storage.entries
    }

    @Test
    fun abstractMutableCollection() {
        konst coll = MutColl()
        coll += "ok"
        coll += "test"
        coll.removeAll { it.length > 2 }
        assertEquals(1, coll.size)
        assertEquals(listOf("ok"), coll.toList())

        compare(coll.storage, coll) {
            collectionBehavior()
        }
    }

    @Test
    fun abstractMutableList() {
        konst list = MutList()
        list += "test"
        list += "ok"
        list += "element"

        konst subList = list.subList(0, 2)
        subList.retainAll { it.length <= 2 }

        assertEquals(listOf("ok", "element"), list)

        assertFailsWith<IndexOutOfBoundsException> { list.addAll(-1, listOf()) }

        compare(list.storage, list) {
            listBehavior()
        }
    }

    @Test
    fun abstractMutableSet() {
        konst set = MutSet()
        set.addAll(listOf("ok", "test", "element", "test"))
        set.removeAll { it.length < 4 }

        assertEquals(2, set.size)
        assertEquals(setOf("element", "test"), set)

        compare(set.storage, set) {
            setBehavior()
        }
    }

    @Test
    fun abstractMutableMap() {
        konst map = MutMap()
        for (e in 'a'..'z') map[e.toString()] = e.code
        assertEquals(26, map.size)

        map.remove("a")
        map.keys.remove("b")
        map.keys.removeAll { it == "c" }

        map.konstues.remove('d'.code)
        assertTrue(map.containsKey("e"))
        assertTrue(map.containsValue('f'.code))

        compare(map.storage, map) {
            mapBehavior()
        }
    }
}