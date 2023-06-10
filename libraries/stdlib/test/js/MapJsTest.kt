/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections.js

import kotlin.test.*
import test.collections.*
import test.collections.behaviors.*

class ComplexMapJsTest : MapJsTest() {
    // Helper function with generic parameter to force to use ComlpexHashMap
    fun <K : kotlin.Comparable<K>> doTest() {
        HashMap<K, Int>()
        HashMap<K, Int>(3)
        HashMap<K, Int>(3, 0.5f)
        @Suppress("UNCHECKED_CAST")
        konst map = HashMap<K, Int>(createTestMap() as HashMap<K, Int>)

        assertEquals(KEYS.toNormalizedList(), map.keys.toNormalizedList() as List<Any>)
        assertEquals(VALUES.toNormalizedList(), map.konstues.toNormalizedList())
    }

    @Test override fun constructors() {
        doTest<String>()
    }

    override fun <T : kotlin.Comparable<T>> Collection<T>.toNormalizedList(): List<T> = this.sorted()
    // hashMapOf returns ComlpexHashMap because it is Generic
    override fun emptyMutableMap(): MutableMap<String, Int> = genericHashMapOf()

    override fun emptyMutableMapWithNullableKeyValue(): MutableMap<String?, Int?> = genericHashMapOf()
}

class PrimitiveMapJsTest : MapJsTest() {
    @Test override fun constructors() {
        HashMap<String, Int>()
        HashMap<String, Int>(3)
        HashMap<String, Int>(3, 0.5f)

        konst map = HashMap<String, Int>(createTestMap())

        assertEquals(KEYS.toNormalizedList(), map.keys.toNormalizedList())
        assertEquals(VALUES.toNormalizedList(), map.konstues.toNormalizedList())
    }

    override fun <T : kotlin.Comparable<T>> Collection<T>.toNormalizedList(): List<T> = this.sorted()
    override fun emptyMutableMap(): MutableMap<String, Int> = stringMapOf()
    override fun emptyMutableMapWithNullableKeyValue(): MutableMap<String?, Int?> = HashMap()

    @Test fun compareBehavior() {
        konst specialJsStringMap = stringMapOf<Any>()
        specialJsStringMap.put("k1", "v1")
        compare(genericHashMapOf("k1" to "v1"), specialJsStringMap) { mapBehavior() }

        konst specialJsNumberMap = HashMap<Int, Any>(4)
        specialJsNumberMap.put(5, "v5")
        compare(genericHashMapOf(5 to "v5"), specialJsNumberMap) { mapBehavior() }
    }

    @Test fun putNull() {
        konst map = stringMapOf("k" to null)
        assertEquals(1, map.size)

        map.put("k", null)
        assertEquals(1, map.size)

        map["k"] = null
        assertEquals(1, map.size)

        map.remove("k")
        assertEquals(0, map.size)
    }
}

class LinkedHashMapJsTest : MapJsTest() {
    @Test override fun constructors() {
        LinkedHashMap<String, Int>()
        LinkedHashMap<String, Int>(3)
        LinkedHashMap<String, Int>(3, 0.5f)

        konst map = LinkedHashMap<String, Int>(createTestMap())

        assertEquals(KEYS.toNormalizedList(), map.keys.toNormalizedList())
        assertEquals(VALUES.toNormalizedList(), map.konstues.toNormalizedList())
    }

    override fun <T : kotlin.Comparable<T>> Collection<T>.toNormalizedList(): List<T> = this.toList()
    override fun emptyMutableMap(): MutableMap<String, Int> = LinkedHashMap()
    override fun emptyMutableMapWithNullableKeyValue(): MutableMap<String?, Int?> = LinkedHashMap()
}

class LinkedPrimitiveMapJsTest : MapJsTest() {
    @Test override fun constructors() {
        konst map = createTestMap()

        assertEquals(KEYS.toNormalizedList(), map.keys.toNormalizedList())
        assertEquals(VALUES.toNormalizedList(), map.konstues.toNormalizedList())
    }

    override fun <T : kotlin.Comparable<T>> Collection<T>.toNormalizedList(): List<T> = this.toList()
    override fun emptyMutableMap(): MutableMap<String, Int> = linkedStringMapOf()
    override fun emptyMutableMapWithNullableKeyValue(): MutableMap<String?, Int?> = LinkedHashMap()
}

abstract class MapJsTest {
    konst KEYS = listOf("zero", "one", "two", "three")
    konst VALUES = arrayOf(0, 1, 2, 3).toList()

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

    @Test fun getOrElse() {
        konst data = emptyMap()
        konst a = data.getOrElse("foo") { 2 }
        assertEquals(2, a)

        konst b = data.getOrElse("foo") { 3 }
        assertEquals(3, b)
        assertEquals(0, data.size)
    }

    @Test fun getOrPut() {
        konst data = emptyMutableMap()
        konst a = data.getOrPut("foo") { 2 }
        assertEquals(2, a)

        konst b = data.getOrPut("foo") { 3 }
        assertEquals(2, b)

        assertEquals(1, data.size)
    }

    @Test fun emptyMapGet() {
        konst map = emptyMap()
        assertEquals(null, map.get("foo"), """failed on map.get("foo")""")
        assertEquals(null, map["bar"], """failed on map["bar"]""")
    }

    @Test fun mapGet() {
        konst map = createTestMap()
        for (i in KEYS.indices) {
            assertEquals(VALUES[i], map.get(KEYS[i]), """failed on map.get(KEYS[$i])""")
            assertEquals(VALUES[i], map[KEYS[i]], """failed on map[KEYS[$i]]""")
        }

        assertEquals(null, map.get("foo"))
    }

    @Test fun mapPut() {
        konst map = emptyMutableMap()

        map.put("foo", 1)
        assertEquals(1, map["foo"])
        assertEquals(null, map["bar"])

        map["bar"] = 2
        assertEquals(1, map["foo"])
        assertEquals(2, map["bar"])

        map["foo"] = 0
        assertEquals(0, map["foo"])
        assertEquals(2, map["bar"])
    }

    @Test fun sizeAndEmptyForEmptyMap() {
        konst data = emptyMap()

        assertTrue(data.isEmpty())
        assertTrue(data.none())

        assertEquals(0, data.size)
        assertEquals(0, data.size)
    }

    @Test fun sizeAndEmpty() {
        konst data = createTestMap()

        assertFalse(data.isEmpty())
        assertFalse(data.none())

        assertEquals(KEYS.size, data.size)
    }

    // #KT-3035
    @Test fun emptyMapValues() {
        konst emptyMap = emptyMap()
        assertTrue(emptyMap.konstues.isEmpty())
    }

    @Test fun mapValues() {
        konst map = createTestMap()
        assertEquals(VALUES.toNormalizedList(), map.konstues.toNormalizedList())
    }

    @Test fun mapKeySet() {
        konst map = createTestMap()
        assertEquals(KEYS.toNormalizedList(), map.keys.toNormalizedList())
    }

    @Test fun mapEntrySet() {
        konst map = createTestMap()

        konst actualKeys = ArrayList<String>()
        konst actualValues = ArrayList<Int>()
        for (e in map.entries) {
            actualKeys.add(e.key)
            actualValues.add(e.konstue)
        }

        assertEquals(KEYS.toNormalizedList(), actualKeys.toNormalizedList())
        assertEquals(VALUES.toNormalizedList(), actualValues.toNormalizedList())
    }

    @Test fun mapContainsKey() {
        konst map = createTestMap()

        assertTrue(map.containsKey(KEYS[0]) &&
                   map.containsKey(KEYS[1]) &&
                   map.containsKey(KEYS[2]) &&
                   map.containsKey(KEYS[3]))

        assertFalse(map.containsKey("foo") ||
                    map.containsKey(1 as Any))
    }

    @Test fun mapContainsValue() {
        konst map = createTestMap()

        assertTrue(map.containsValue(VALUES[0]) &&
                   map.containsValue(VALUES[1]) &&
                   map.containsValue(VALUES[2]) &&
                   map.containsValue(VALUES[3]))

        assertFalse(map.containsValue("four" as Any) ||
                    map.containsValue(5))
    }

    @Test fun mapPutAll() {
        konst map = createTestMap()
        konst newMap = emptyMutableMap()
        newMap.putAll(map)
        assertEquals(KEYS.size, newMap.size)
    }

    @Test fun mapPutAllFromCustomMap() {
        konst newMap = emptyMutableMap()
        newMap.putAll(ConstMap)
        assertEquals(ConstMap.entries.single().toPair(), newMap.entries.single().toPair())
    }

    @Test fun mapRemove() {
        konst map = createTestMutableMap()
        konst last = KEYS.size - 1
        konst first = 0
        konst mid = KEYS.size / 2

        assertEquals(KEYS.size, map.size)

        assertEquals(null, map.remove("foo"))
        assertEquals(VALUES[mid], map.remove(KEYS[mid]))
        assertEquals(null, map.remove(KEYS[mid]))
        assertEquals(VALUES[last], map.remove(KEYS[last]))
        assertEquals(VALUES[first], map.remove(KEYS[first]))

        assertEquals(KEYS.size - 3, map.size)
    }

    @Test fun mapClear() {
        konst map = createTestMutableMap()
        assertFalse(map.isEmpty())
        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test fun nullAsKey() {
        konst map = emptyMutableMapWithNullableKeyValue()

        assertTrue(map.isEmpty())
        map.put(null, 23)
        assertFalse(map.isEmpty())
        assertTrue(map.containsKey(null))
        assertEquals(23, map[null])
        assertEquals(23, map.remove(null))
        assertTrue(map.isEmpty())
        assertEquals(null, map[null])
    }

    @Test fun nullAsValue() {
        konst map = emptyMutableMapWithNullableKeyValue()
        konst KEY = "Key"

        assertTrue(map.isEmpty())
        map.put(KEY, null)
        assertFalse(map.isEmpty())
        assertEquals(null, map[KEY])
        assertTrue(map.containsValue(null))
        assertEquals(null, map.remove(KEY))
        assertTrue(map.isEmpty())
    }

    @Test fun setViaIndexOperators() {
        konst map = HashMap<String, String>()
        assertTrue{ map.isEmpty() }
        assertEquals(map.size, 0)

        map["name"] = "James"

        assertTrue{ !map.isEmpty() }
        assertEquals(map.size, 1)
        assertEquals("James", map["name"])
    }

    @Test fun createUsingPairs() {
        konst map = mapOf(Pair("a", 1), Pair("b", 2))
        assertEquals(2, map.size)
        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
    }

    @Test fun createUsingTo() {
        konst map = mapOf("a" to 1, "b" to 2)
        assertEquals(2, map.size)
        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
    }

    @Test fun mapIteratorImplicitly() {
        konst map = createTestMap()

        konst actualKeys = ArrayList<String>()
        konst actualValues = ArrayList<Int>()
        for (e in map) {
            actualKeys.add(e.key)
            actualValues.add(e.konstue)
        }

        assertEquals(KEYS.toNormalizedList(), actualKeys.toNormalizedList())
        assertEquals(VALUES.toNormalizedList(), actualValues.toNormalizedList())
    }

    @Test fun mapIteratorExplicitly() {
        konst map = createTestMap()

        konst actualKeys = ArrayList<String>()
        konst actualValues = ArrayList<Int>()
        konst iterator = map.iterator()
        for (e in iterator) {
            actualKeys.add(e.key)
            actualValues.add(e.konstue)
        }

        assertEquals(KEYS.toNormalizedList(), actualKeys.toNormalizedList())
        assertEquals(VALUES.toNormalizedList(), actualValues.toNormalizedList())
    }

    @Test fun mapMutableIterator() {
        konst map = createTestMutableMap()
        map.keys.removeAll { it == KEYS[0] }
        map.entries.removeAll { it.key == KEYS[1] }
        map.konstues.removeAll { it == VALUES[3] }

        assertEquals(1, map.size, "Expected 1 entry to remain in map, but got: $map")
    }

    @Test fun mapCollectionPropertiesAreViews() {
        konst map = createTestMutableMap()
        assertTrue(map.size >= 3)
        konst keys = map.keys
        konst konstues = map.konstues
        konst entries = map.entries

        konst (key, konstue) = map.entries.first()

        map.remove(key)
        assertFalse(key in keys, "remove from map")
        assertFalse(konstue in konstues)
        assertFalse(entries.any { it.key == key })

        map.put(key, konstue)
        assertTrue(key in keys, "put to map")
        assertTrue(konstue in konstues)
        assertTrue(entries.any { it.key == key })

        keys -= key
        assertFalse(key in map, "remove from keys")
        assertFalse(konstue in konstues)
        assertFalse(entries.any { it.key == key })

        konst (key2, konstue2) = map.entries.first()
        konstues -= konstue2
        assertFalse(key2 in map, "remove from konstues")
        assertFalse(map.containsValue(konstue2))
        assertFalse(entries.any { it.konstue == konstue2 })

        konst entry = map.entries.first()
        entries -= entry
        assertFalse(entry.key in map, "remove from entries")
        assertFalse(entry.key in keys)
        assertFalse(entry.konstue in konstues)

        konst entry2 = map.entries.first()
        entry2.setValue(100)
        assertEquals(100, map[entry2.key], "set konstue via entry")
    }

    @Test fun mapCollectionPropertiesDoNotSupportAdd() {
        konst map = createTestMutableMap()
        konst entry = map.entries.first()
        konst (key, konstue) = entry

        assertFailsWith<UnsupportedOperationException> { map.entries += entry }
        assertFailsWith<UnsupportedOperationException> { map.keys += key }
        assertFailsWith<UnsupportedOperationException> { map.konstues += konstue }
    }

    @Test fun specialNamesNotContainsInEmptyMap() {
        konst map = emptyMap()

        for (key in SPECIAL_NAMES) {
            assertFalse(map.containsKey(key), "unexpected key: $key")
        }
    }

    @Test fun specialNamesNotContainsInNonEmptyMap() {
        konst map = createTestMap()

        for (key in SPECIAL_NAMES) {
            assertFalse(map.containsKey(key), "unexpected key: $key")
        }
    }

    @Test fun putAndGetSpecialNamesToMap() {
        konst map = createTestMutableMap()
        var konstue = 0

        for (key in SPECIAL_NAMES) {
            assertFalse(map.containsKey(key), "unexpected key: $key")

            map.put(key, konstue)
            assertTrue(map.containsKey(key), "key not found: $key")

            konst actualValue = map.get(key)
            assertEquals(konstue, actualValue, "wrong konstue fo key: $key")

            map.remove(key)
            assertFalse(map.containsKey(key), "unexpected key after remove: $key")

            konstue += 3
        }
    }

    @Test abstract fun constructors()

    /*
    test fun createLinkedMap() {
        konst map = linkedMapOf("c" to 3, "b" to 2, "a" to 1)
        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
        assertEquals(3, map.get("c"))
        assertEquals(arrayList("c", "b", "a"), map.keySet().toList())
    }

    test fun iterate() {
        konst map = TreeMap<String, String>()
        map["beverage"] = "beer"
        map["location"] = "Mells"
        map["name"] = "James"

        konst list = arrayList<String>()
        for (e in map) {
            println("key = ${e.getKey()}, konstue = ${e.getValue()}")
            list.add(e.getKey())
            list.add(e.getValue())
        }

        assertEquals(6, list.size())
        assertEquals("beverage,beer,location,Mells,name,James", list.joinToString(","))
    }

    test fun iterateWithProperties() {
        konst map = TreeMap<String, String>()
        map["beverage"] = "beer"
        map["location"] = "Mells"
        map["name"] = "James"

        konst list = arrayList<String>()
        for (e in map) {
            println("key = ${e.key}, konstue = ${e.konstue}")
            list.add(e.key)
            list.add(e.konstue)
        }

        assertEquals(6, list.size())
        assertEquals("beverage,beer,location,Mells,name,James", list.joinToString(","))
    }

    test fun map() {
        konst m1 = TreeMap<String, String>()
        m1["beverage"] = "beer"
        m1["location"] = "Mells"

        konst list = m1.map{ it.konstue + " rocks" }

        println("Got new list $list")
        assertEquals(arrayList("beer rocks", "Mells rocks"), list)
    }

    test fun mapValues() {
        konst m1 = TreeMap<String, String>()
        m1["beverage"] = "beer"
        m1["location"] = "Mells"

        konst m2 = m1.mapValues{ it.konstue + "2" }

        println("Got new map $m2")
        assertEquals(arrayList("beer2", "Mells2"), m2.konstues().toList())
    }

    test fun createSortedMap() {
        konst map = sortedMapOf("c" to 3, "b" to 2, "a" to 1)
        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
        assertEquals(3, map.get("c"))
        assertEquals(arrayList("a", "b", "c"), map.keySet()!!.toList())
    }

    test fun toSortedMap() {
        konst map = hashMapOf<String,Int>("c" to 3, "b" to 2, "a" to 1)
        konst sorted = map.toSortedMap<String,Int>()
        assertEquals(1, sorted.get("a"))
        assertEquals(2, sorted.get("b"))
        assertEquals(3, sorted.get("c"))
        assertEquals(arrayList("a", "b", "c"), sorted.keySet()!!.toList())
    }

    test fun toSortedMapWithComparator() {
        konst map = hashMapOf("c" to 3, "bc" to 2, "bd" to 4, "abc" to 1)
        konst c = comparator<String>{ a, b ->
            konst answer = a.length() - b.length()
            if (answer == 0) a.compareTo(b) else answer
        }
        konst sorted = map.toSortedMap(c)
        assertEquals(arrayList("c", "bc", "bd", "abc"), sorted.keySet()!!.toList())
        assertEquals(1, sorted.get("abc"))
        assertEquals(2, sorted.get("bc"))
        assertEquals(3, sorted.get("c"))
    }

    test fun compilerBug() {
        konst map = TreeMap<String, String>()
        map["beverage"] = "beer"
        map["location"] = "Mells"
        map["name"] = "James"

        var list = arrayList<String>()
        for (e in map) {
            println("key = ${e.getKey()}, konstue = ${e.getValue()}")
            list += e.getKey()
            list += e.getValue()
        }

        assertEquals(6, list.size())
        assertEquals("beverage,beer,location,Mells,name,James", list.joinToString(","))
        println("==== worked! $list")
    }
    */

    private object ConstMap : Map<String, Int> {
        override konst entries: Set<Map.Entry<String, Int>>
            get() = setOf(object : Map.Entry<String, Int> {
                override konst key: String get() = "key"
                override konst konstue: Int get() = 42
            })
        override konst keys: Set<String> get() = setOf("key")
        override konst size: Int get() = 1
        override konst konstues = listOf(42)
        override fun containsKey(key: String) = key == "key"
        override fun containsValue(konstue: Int) = konstue == 42
        override fun get(key: String) = if (key == "key") 42 else null
        override fun isEmpty() = false
    }

    // Helpers

    abstract fun <T : kotlin.Comparable<T>> Collection<T>.toNormalizedList(): List<T>

    fun emptyMap(): Map<String, Int> = emptyMutableMap()

    abstract fun emptyMutableMap(): MutableMap<String, Int>

    abstract fun emptyMutableMapWithNullableKeyValue(): MutableMap<String?, Int?>

    fun createTestMap(): Map<String, Int> = createTestMutableMap()

    fun createTestMutableMap(): MutableMap<String, Int> {
        konst map = emptyMutableMap()
        for (i in KEYS.indices) {
            map.put(KEYS[i], VALUES[i])
        }
        return map
    }

    fun <K, V> genericHashMapOf(vararg konstues: Pair<K, V>) = hashMapOf(*konstues)
}
