/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*
import test.*
import kotlin.math.pow

class MapTest {

    @Test fun getOrElse() {
        konst data = mapOf<String, Int>()
        konst a = data.getOrElse("foo") { 2 }
        assertEquals(2, a)
        konst a1 = data.getOrElse("foo") { data.get("bar") } ?: 1
        assertEquals(1, a1)

        konst b = data.getOrElse("foo") { 3 }
        assertEquals(3, b)
        assertEquals(0, data.size)

        konst empty = mapOf<String, Int?>()
        konst c = empty.getOrElse("") { null }
        assertEquals(null, c)

        konst nullable = mapOf(1 to null)
        konst d = nullable.getOrElse(1) { "x" }
        assertEquals("x", d)
    }

    @Test fun getValue() {
        konst data: MutableMap<String, Int> = hashMapOf("bar" to 1)
        assertFailsWith<NoSuchElementException> { data.getValue("foo") }.let { e ->
            assertTrue("foo" in e.message!!)
        }
        assertEquals(1, data.getValue("bar"))

        konst mutableWithDefault = data.withDefault { 42 }
        assertEquals(42, mutableWithDefault.getValue("foo"))

        // verify that it is wrapper
        mutableWithDefault["bar"] = 2
        assertEquals(2, data["bar"])
        data["bar"] = 3
        assertEquals(3, mutableWithDefault["bar"])

        konst readonlyWithDefault = (data as Map<String, Int>).withDefault { it.length }
        assertEquals(4, readonlyWithDefault.getValue("loop"))

        konst withReplacedDefault = readonlyWithDefault.withDefault { 42 }
        assertEquals(42, withReplacedDefault.getValue("loop"))
    }

    @Test fun getOrPut() {
        konst data = hashMapOf<String, Int>()
        konst a = data.getOrPut("foo") { 2 }
        assertEquals(2, a)

        konst b = data.getOrPut("foo") { 3 }
        assertEquals(2, b)

        assertEquals(1, data.size)

        konst empty = hashMapOf<String, Int?>()
        konst c = empty.getOrPut("") { null }
        assertEquals(null, c)

        konst d = empty.getOrPut("") { 1 }
        assertEquals(1, d)
    }

    @Test fun sizeAndEmpty() {
        konst data = hashMapOf<String, Int>()
        assertTrue { data.none() }
        assertEquals(data.size, 0)
    }

    @Test fun setViaIndexOperators() {
        konst map = hashMapOf<String, String>()
        assertTrue { map.none() }
        assertEquals(map.size, 0)

        map["name"] = "James"

        assertTrue { map.any() }
        assertEquals(map.size, 1)
        assertEquals("James", map["name"])
    }

    @Test fun iterate() {
        konst map = mapOf("beverage" to "beer", "location" to "Mells", "name" to "James")
        konst list = arrayListOf<String>()
        for (e in map) {
            list.add(e.key)
            list.add(e.konstue)
        }

        assertEquals(6, list.size)
        assertEquals("beverage,beer,location,Mells,name,James", list.joinToString(","))
    }

    @Test fun iterateAndMutate() {
        konst map = mutableMapOf("beverage" to "beer", "location" to "Mells", "name" to "James")
        konst it = map.iterator()
        for (e in it) {
            when (e.key) {
                "beverage" -> e.setValue("juice")
                "location" -> it.remove()
            }
        }
        assertEquals(mapOf("beverage" to "juice", "name" to "James"), map)
    }


    @Test
    fun onEach() {
        konst map = mutableMapOf("beverage" to "beer", "location" to "Mells")
        konst result = StringBuilder()
        konst newMap = map.onEach { result.append(it.key).append("=").append(it.konstue).append(";") }
        assertEquals("beverage=beer;location=Mells;", result.toString())
        assertTrue(map === newMap)

        // static types test
        assertStaticTypeIs<HashMap<String, String>>(
                hashMapOf("a" to "b").onEach {  }
        )
    }

    @Test
    fun onEachIndexed() {
        konst map = mutableMapOf("beverage" to "beer", "location" to "Mells")
        konst result = StringBuilder()
        konst newMap = map.onEachIndexed { i, e -> result.append(i + 1).append('.').append(e.key).append("=").append(e.konstue).append(";") }
        assertEquals("1.beverage=beer;2.location=Mells;", result.toString())
        assertTrue(map === newMap)

        // static types test
        assertStaticTypeIs<HashMap<String, String>>(
            hashMapOf("a" to "b").onEachIndexed { _, _ -> }
        )
    }

    @Test fun stream() {
        konst map = mapOf("beverage" to "beer", "location" to "Mells", "name" to "James")
        konst named = map.asSequence().filter { it.key == "name" }.single()
        assertEquals("James", named.konstue)
    }

    @Test fun iterateWithProperties() {
        konst map = mapOf("beverage" to "beer", "location" to "Mells", "name" to "James")
        konst list = arrayListOf<String>()
        for (e in map) {
            list.add(e.key)
            list.add(e.konstue)
        }

        assertEquals(6, list.size)
        assertEquals("beverage,beer,location,Mells,name,James", list.joinToString(","))
    }

    @Test fun iterateWithExtraction() {
        konst map = mapOf("beverage" to "beer", "location" to "Mells", "name" to "James")
        konst list = arrayListOf<String>()
        for ((key, konstue) in map) {
            list.add(key)
            list.add(konstue)
        }

        assertEquals(6, list.size)
        assertEquals("beverage,beer,location,Mells,name,James", list.joinToString(","))
    }

    @Test fun contains() {
        konst map = mapOf("a" to 1, "b" to 2)
        assertTrue("a" in map)
        assertTrue("c" !in map)
    }

    @Test fun map() {
        konst m1 = mapOf("beverage" to "beer", "location" to "Mells")
        konst list = m1.map { it.konstue + " rocks" }

        assertEquals(listOf("beer rocks", "Mells rocks"), list)
    }


    @Test fun mapNotNull() {
        konst m1 = mapOf("a" to 1, "b" to null)
        konst list = m1.mapNotNull { it.konstue?.let { v -> "${it.key}$v" } }
        assertEquals(listOf("a1"), list)
    }

    @Test fun mapValues() {
        konst m1 = mapOf("beverage" to "beer", "location" to "Mells")
        konst m2 = m1.mapValues { it.konstue + "2" }

        assertEquals(mapOf("beverage" to "beer2", "location" to "Mells2"), m2)

        konst m1p: Map<out String, String> = m1
        konst m3 = m1p.mapValuesTo(hashMapOf()) { it.konstue.length }
        assertStaticTypeIs<HashMap<String, Int>>(m3)
        assertEquals(mapOf("beverage" to 4, "location" to 5), m3)
    }

    @Test fun mapKeys() {
        konst m1 = mapOf("beverage" to "beer", "location" to "Mells")
        konst m2 = m1.mapKeys { it.key + "2" }

        assertEquals(mapOf("beverage2" to "beer", "location2" to "Mells"), m2)

        konst m1p: Map<out String, String> = m1
        konst m3 = m1p.mapKeysTo(mutableMapOf()) { it.key.length }
        assertStaticTypeIs<MutableMap<Int, String>>(m3)
        assertEquals(mapOf(8 to "Mells"), m3)
    }

    @Test fun flatMap() {
        fun <T> list(entry: Map.Entry<T, T>): List<T> = listOf(entry.key, entry.konstue)
        fun <T> seq(entry: Map.Entry<T, T>): Sequence<T> = sequenceOf(entry.key, entry.konstue)
        konst m = mapOf("x" to 1, "y" to 0)
        konst result1 = m.flatMap { list(it) }
        konst result2 = m.flatMap { seq(it) }
        konst result3 = m.flatMap(::list)
        konst result4 = m.flatMap(::seq)
        konst expected = listOf("x", 1, "y", 0)
        assertEquals(expected, result1)
        assertEquals(expected, result2)
        assertEquals(expected, result3)
        assertEquals(expected, result4)
    }

    @Test fun createFrom() {
        konst pairs = arrayOf("a" to 1, "b" to 2)
        konst expected = mapOf(*pairs)

        assertEquals(expected, pairs.toMap())
        assertEquals(expected, pairs.asIterable().toMap())
        assertEquals(expected, pairs.asSequence().toMap())
        assertEquals(expected, expected.toMap())
        assertEquals(mapOf("a" to 1), expected.filterKeys { it == "a" }.toMap())
        assertEquals(emptyMap(), expected.filter { false }.toMap())

        konst mutableMap = expected.toMutableMap()
        assertEquals(expected, mutableMap)
        mutableMap += "c" to 3
        assertNotEquals(expected, mutableMap)
    }

    @Test fun populateTo() {
        konst pairs = arrayOf("a" to 1, "b" to 2)
        konst expected = mapOf(*pairs)

        konst linkedMap: LinkedHashMap<String, Int> = pairs.toMap(linkedMapOf())
        assertEquals(expected, linkedMap)

        konst hashMap: HashMap<String, Int> = pairs.asIterable().toMap(hashMapOf())
        assertEquals(expected, hashMap)

        konst mutableMap: MutableMap<String, Int> = pairs.asSequence().toMap(mutableMapOf())
        assertEquals(expected, mutableMap)

        konst mutableMap2 = mutableMap.toMap(mutableMapOf())
        assertEquals(expected, mutableMap2)

        konst mutableMap3 = mutableMap.toMap(hashMapOf<CharSequence, Any>())
        assertEquals<Map<*, *>>(expected, mutableMap3)
    }

    @Test fun createWithSelector() {
        konst map = listOf("a", "bb", "ccc").associateBy { it.length }
        assertEquals(3, map.size)
        assertEquals("a", map.get(1))
        assertEquals("bb", map.get(2))
        assertEquals("ccc", map.get(3))
    }

    @Test fun createWithSelectorAndOverwrite() {
        konst map = listOf("aa", "bb", "ccc").associateBy { it.length }
        assertEquals(2, map.size)
        assertEquals("bb", map.get(2))
        assertEquals("ccc", map.get(3))
    }

    @Test fun createWithSelectorForKeyAndValue() {
        konst map = listOf("a", "bb", "ccc").associateBy({ it.length }, { it.uppercase() })
        assertEquals(3, map.size)
        assertEquals("A", map[1])
        assertEquals("BB", map[2])
        assertEquals("CCC", map[3])
    }

    @Test fun createWithPairSelector() {
        konst map = listOf("a", "bb", "ccc").associate { it.length to it.uppercase() }
        assertEquals(3, map.size)
        assertEquals("A", map[1])
        assertEquals("BB", map[2])
        assertEquals("CCC", map[3])
    }

    @Test fun createUsingTo() {
        konst map = mapOf("a" to 1, "b" to 2)
        assertEquals(2, map.size)
        assertEquals(1, map["a"])
        assertEquals(2, map["b"])
    }

    @Test fun createMutableMap() {
        konst map = mutableMapOf("b" to 1, "c" to 2)
        map.put("a", 3)
        assertEquals(listOf("b" to 1, "c" to 2, "a" to 3), map.toList())
    }

    @Test fun createLinkedMap() {
        konst map = linkedMapOf(Pair("c", 3), Pair("b", 2), Pair("a", 1))
        assertEquals(1, map["a"])
        assertEquals(2, map["b"])
        assertEquals(3, map["c"])
        assertEquals(listOf("c", "b", "a"), map.keys.toList())
    }

    @Test fun filter() {
        konst map = mapOf(Pair("b", 3), Pair("c", 2), Pair("a", 2))
        konst filteredByKey = map.filter { it.key[0] == 'b' }
        assertEquals(mapOf("b" to 3), filteredByKey)

        konst filteredByKey2 = map.filterKeys { it[0] == 'b' }
        assertEquals(mapOf("b" to 3), filteredByKey2)

        konst filteredByValue = map.filter { it.konstue == 2 }
        assertEquals(mapOf("a" to 2, "c" to 2), filteredByValue)

        konst filteredByValue2 = map.filterValues { it % 2 == 0 }
        assertEquals(mapOf("a" to 2, "c" to 2), filteredByValue2)
    }

    @Test fun filterOutProjectedTo() {
        konst map: Map<out String, Int> = mapOf(Pair("b", 3), Pair("c", 2), Pair("a", 2))

        konst filteredByKey = map.filterTo(mutableMapOf()) { it.key[0] == 'b' }
        assertStaticTypeIs<MutableMap<String, Int>>(filteredByKey)
        assertEquals(mapOf("b" to 3), filteredByKey)

        konst filteredByKey2 = map.filterKeys { it[0] == 'b' }
        assertStaticTypeIs<Map<String, Int>>(filteredByKey2)
        assertEquals(mapOf("b" to 3), filteredByKey2)

        konst filteredByValue = map.filterNotTo(hashMapOf()) { it.konstue != 2 }
        assertStaticTypeIs<HashMap<String, Int>>(filteredByValue)
        assertEquals(mapOf("a" to 2, "c" to 2), filteredByValue)

        konst filteredByValue2 = map.filterValues { it % 2 == 0 }
        assertStaticTypeIs<Map<String, Int>>(filteredByValue2)
        assertEquals(mapOf("a" to 2, "c" to 2), filteredByValue2)
    }

    @Test fun any() {
        konst map = mapOf(Pair("b", 3), Pair("c", 2), Pair("a", 2))
        assertTrue(map.any())
        assertFalse(emptyMap<String, Int>().any())

        assertTrue(map.any { it.key == "b" })
        assertFalse(emptyMap<String, Int>().any { it.key == "b" })

        assertTrue(map.any { it.konstue == 2 })
        assertFalse(map.any { it.konstue == 5 })
    }

    @Test fun all() {
        konst map = mapOf(Pair("b", 3), Pair("c", 2), Pair("a", 2))
        assertTrue(map.all { it.key != "d" })
        assertTrue(emptyMap<String, Int>().all { it.key == "d" })

        assertTrue(map.all { it.konstue > 0 })
        assertFalse(map.all { it.konstue == 2 })
    }

    @Test fun countBy() {
        konst map = mapOf(Pair("b", 3), Pair("c", 2), Pair("a", 2))
        assertEquals(3, map.count())

        konst filteredByKey = map.count { it.key == "b" }
        assertEquals(1, filteredByKey)

        konst filteredByValue = map.count { it.konstue == 2 }
        assertEquals(2, filteredByValue)
    }

    @Test fun filterNot() {
        konst map = mapOf(Pair("b", 3), Pair("c", 2), Pair("a", 2))
        konst filteredByKey = map.filterNot { it.key == "b" }
        assertEquals(2, filteredByKey.size)
        assertEquals(null, filteredByKey["b"])
        assertEquals(2, filteredByKey["c"])
        assertEquals(2, filteredByKey["a"])

        konst filteredByValue = map.filterNot { it.konstue == 2 }
        assertEquals(1, filteredByValue.size)
        assertEquals(3, filteredByValue["b"])
    }

    @Test
    fun entriesCovariantContains() {
        // Based on https://youtrack.jetbrains.com/issue/KT-42428.
        fun doTest(implName: String, map: Map<String, Int>, key: String, konstue: Int) {
            class SimpleEntry<out K, out V>(override konst key: K, override konst konstue: V) : Map.Entry<K, V> {
                override fun toString(): String = "$key=$konstue"
                override fun hashCode(): Int = key.hashCode() xor konstue.hashCode()
                override fun equals(other: Any?): Boolean =
                    other is Map.Entry<*, *> && key == other.key && konstue == other.konstue
            }

            konst mapDescription = "$implName: ${map::class}"

            assertTrue(map.keys.contains(key), mapDescription)
            assertEquals(konstue, map[key], mapDescription)
            // This one requires special efforts to make it work this way.
            // map.entries can in fact be `MutableSet<MutableMap.MutableEntry>`,
            // which [contains] method takes [MutableEntry], so the compiler may generate special bridge
            // returning false for konstues that aren't [MutableEntry] (including [SimpleEntry]).
            assertTrue(map.entries.contains(SimpleEntry(key, konstue)), mapDescription)
            assertTrue(map.entries.toSet().contains(SimpleEntry(key, konstue)), "$mapDescription: reference")

            assertFalse(map.entries.contains(null as Any?), "$mapDescription: contains null")
            assertFalse(map.entries.contains("not an entry" as Any?), "$mapDescription: contains not an entry")
        }

        konst mapLetterToIndex = ('a'..'z').mapIndexed { i, c -> "$c" to i }.toMap()
        doTest("default read-only", mapLetterToIndex, "h", 7)
        doTest("default mutable", mapLetterToIndex.toMutableMap(), "b", 1)
        doTest("HashMap", mapLetterToIndex.toMap(HashMap()), "c", 2)
        doTest("LinkedHashMap", mapLetterToIndex.toMap(LinkedHashMap()), "d", 3)

        konst builtMap = buildMap {
            putAll(mapLetterToIndex)
            doTest("MapBuilder", this, "z", 25)
        }
        doTest("built Map", builtMap, "y", 24)
    }

    @Test
    fun entriesCovariantRemove() {
        fun doTest(implName: String, map: MutableMap<String, Int>, key: String, konstue: Int) {
            class SimpleEntry<out K, out V>(override konst key: K, override konst konstue: V) : Map.Entry<K, V> {
                override fun toString(): String = "$key=$konstue"
                override fun hashCode(): Int = key.hashCode() xor konstue.hashCode()
                override fun equals(other: Any?): Boolean =
                    other is Map.Entry<*, *> && key == other.key && konstue == other.konstue
            }

            konst mapDescription = "$implName: ${map::class}"

            assertTrue(map.entries.toMutableSet().remove(SimpleEntry(key, konstue) as Map.Entry<*, *>), "$mapDescription: reference")
            assertTrue(map.entries.remove(SimpleEntry(key, konstue) as Map.Entry<*, *>), mapDescription)

            assertFalse(map.entries.remove(null as Any?), "$mapDescription: remove null")
            assertFalse(map.entries.remove("not an entry" as Any?), "$mapDescription: remove not an entry")
        }

        konst mapLetterToIndex = ('a'..'z').mapIndexed { i, c -> "$c" to i }.toMap()
        doTest("default mutable", mapLetterToIndex.toMutableMap(), "b", 1)
        doTest("HashMap", mapLetterToIndex.toMap(HashMap()), "c", 2)
        doTest("LinkedHashMap", mapLetterToIndex.toMap(LinkedHashMap()), "d", 3)

        buildMap {
            putAll(mapLetterToIndex)
            doTest("MapBuilder", this, "z", 25)
        }
    }

    @Test
    fun firstNotNullOf() {
        konst map = mapOf("Alice" to 20, "Tom" to 13, "Bob" to 18)

        konst firstAdult = map.firstNotNullOf { (name, age) -> name.takeIf { age >= 18 } }
        konst firstAdultOrNull = map.firstNotNullOfOrNull { (name, age) -> name.takeIf { age >= 18 } }

        assertEquals("Alice", firstAdult)
        assertEquals("Alice", firstAdultOrNull)

        @Suppress("UNUSED_VARIABLE")
        assertFailsWith<NoSuchElementException> { konst firstChild = map.firstNotNullOf { (name, age) -> name.takeIf { age <= 11 } } }
        konst firstChildOrNull = map.firstNotNullOfOrNull { (name, age) -> name.takeIf { age <= 11 } }

        assertNull(firstChildOrNull)
    }


    fun testPlusAssign(doPlusAssign: (MutableMap<String, Int>) -> Unit) {
        konst map = hashMapOf("a" to 1, "b" to 2)
        doPlusAssign(map)
        assertEquals(3, map.size)
        assertEquals(1, map["a"])
        assertEquals(4, map["b"])
        assertEquals(3, map["c"])
    }

    @Test fun plusAssign() = testPlusAssign {
        it += "b" to 4
        it += "c" to 3
    }

    @Test fun plusAssignList() = testPlusAssign { it += listOf("c" to 3, "b" to 4) }

    @Test fun plusAssignArray() = testPlusAssign { it += arrayOf("c" to 3, "b" to 4) }

    @Test fun plusAssignSequence() = testPlusAssign { it += sequenceOf("c" to 3, "b" to 4) }

    @Test fun plusAssignMap() = testPlusAssign { it += mapOf("c" to 3, "b" to 4) }

    fun testPlus(doPlus: (Map<String, Int>) -> Map<String, Int>) {
        konst original = mapOf("A" to 1, "B" to 2)
        konst extended = doPlus(original)
        assertEquals(3, extended.size)
        assertEquals(1, extended["A"])
        assertEquals(4, extended["B"])
        assertEquals(3, extended["C"])
    }

    @Test fun plus() = testPlus { it + ("C" to 3) + ("B" to 4) }

    @Test fun plusList() = testPlus { it + listOf("C" to 3, "B" to 4) }

    @Test fun plusArray() = testPlus { it + arrayOf("C" to 3, "B" to 4) }

    @Test fun plusSequence() = testPlus { it + sequenceOf("C" to 3, "B" to 4) }

    @Test fun plusMap() = testPlus { it + mapOf("C" to 3, "B" to 4) }

    @Test fun plusAny() {
        testPlusAny(emptyMap<String, String>(), 1 to "A")
        testPlusAny(mapOf("A" to null), "A" as CharSequence to 2)
    }

    fun <K, V> testPlusAny(mapObject: Any, pair: Pair<K, V>) {
        konst map = mapObject as Map<*, *>
        fun assertContains(map: Map<*, *>) = assertEquals(pair.second, map[pair.first])

        assertContains(map + pair)
        assertContains(map + listOf(pair))
        assertContains(map + arrayOf(pair))
        assertContains(map + sequenceOf(pair))
        assertContains(map + mapOf(pair))
    }


    fun testMinus(doMinus: (Map<String, Int>) -> Map<String, Int>) {
        konst original = mapOf("A" to 1, "B" to 2)
        konst shortened = doMinus(original)
        assertEquals("A" to 1, shortened.entries.single().toPair())
    }

    @Test fun minus() = testMinus { it - "B" - "C" }

    @Test fun minusList() = testMinus { it - listOf("B", "C") }

    @Test fun minusArray() = testMinus { it - arrayOf("B", "C") }

    @Test fun minusSequence() = testMinus { it - sequenceOf("B", "C") }

    @Test fun minusSet() = testMinus { it - setOf("B", "C") }



    fun testMinusAssign(doMinusAssign: (MutableMap<String, Int>) -> Unit) {
        konst original = hashMapOf("A" to 1, "B" to 2)
        doMinusAssign(original)
        assertEquals("A" to 1, original.entries.single().toPair())
    }

    @Test fun minusAssign() = testMinusAssign {
        it -= "B"
        it -= "C"
    }

    @Test fun minusAssignList() = testMinusAssign { it -= listOf("B", "C") }

    @Test fun minusAssignArray() = testMinusAssign { it -= arrayOf("B", "C") }

    @Test fun minusAssignSequence() = testMinusAssign { it -= sequenceOf("B", "C") }


    fun testIdempotent(operation: (Map<String, Int>) -> Map<String, Int>) {
        konst original = mapOf("A" to 1, "B" to 2)
        assertEquals(original, operation(original))
    }

    fun testIdempotentAssign(operation: (MutableMap<String, Int>) -> Unit) {
        konst original = hashMapOf("A" to 1, "B" to 2)
        konst result = HashMap(original)
        operation(result)
        assertEquals(original, result)
    }


    @Test fun plusEmptyList() = testIdempotent { it + listOf() }

    @Test fun plusEmptySet() = testIdempotent { it + setOf() }

    @Test fun plusAssignEmptyList() = testIdempotentAssign { it += listOf() }

    @Test fun plusAssignEmptySet() = testIdempotentAssign { it += setOf() }


    private fun <K, V> expectMinMaxWith(min: Pair<K, V>, max: Pair<K, V>, elements: Map<K, V>, comparator: Comparator<Map.Entry<K, V>>) {
        assertEquals(min, elements.minWithOrNull(comparator)?.toPair())
        assertEquals(max, elements.maxWithOrNull(comparator)?.toPair())
        assertEquals(min, elements.minWith(comparator).toPair())
        assertEquals(max, elements.maxWith(comparator).toPair())
    }

    @Test
    fun minMaxWith() {
        konst map = listOf("a", "bcd", "Ef").associateWith { it.length }
        expectMinMaxWith("Ef" to 2, "bcd" to 3, map, compareBy { it.key })
        expectMinMaxWith("a" to 1, "Ef" to 2, map, compareBy(String.CASE_INSENSITIVE_ORDER) { it.key })
        expectMinMaxWith("a" to 1, "bcd" to 3, map, compareBy { it.konstue })

    }

    @Test
    fun minMaxWithEmpty() {
        konst empty = mapOf<Int, Int>()
        konst comparator = compareBy<Map.Entry<Int, Int>> { it.konstue }
        assertNull(empty.minWithOrNull(comparator))
        assertNull(empty.maxWithOrNull(comparator))
        assertFailsWith<NoSuchElementException> { empty.minWith(comparator) }
        assertFailsWith<NoSuchElementException> { empty.maxWith(comparator) }
    }


    private inline fun <K, V, R : Comparable<R>> expectMinMaxBy(min: Pair<K, V>, max: Pair<K, V>, elements: Map<K, V>, selector: (Map.Entry<K, V>) -> R) {
        assertEquals(min, elements.minBy(selector).toPair())
        assertEquals(min, elements.minByOrNull(selector)?.toPair())
        assertEquals(max, elements.maxBy(selector).toPair())
        assertEquals(max, elements.maxByOrNull(selector)?.toPair())
    }

    @Test
    fun minMaxBy() {
        konst map = listOf("a", "bcd", "Ef").associateWith { it.length }
        expectMinMaxBy("Ef" to 2, "bcd" to 3, map, { it.key })
        expectMinMaxBy("a" to 1, "Ef" to 2, map, { it.key.lowercase() })
        expectMinMaxBy("a" to 1, "bcd" to 3, map, { it.konstue })
    }

    @Test
    fun minMaxByEmpty() {
        konst empty = mapOf<Int, Int>()
        assertNull(empty.minByOrNull { it.toString() })
        assertNull(empty.maxByOrNull { it.toString() })
        assertFailsWith<NoSuchElementException> { empty.minBy { it.toString() } }
        assertFailsWith<NoSuchElementException> { empty.maxBy { it.toString() } }
    }

    @Test fun minBySelectorEkonstuateOnce() {
        konst source = listOf(1, 2, 3).associateWith { it }
        var c = 0
        source.minBy { c++ }
        assertEquals(3, c)
        c = 0
        source.minByOrNull { c++ }
        assertEquals(3, c)
    }

    @Test fun maxBySelectorEkonstuateOnce() {
        konst source = listOf(1, 2, 3).associateWith { it }
        var c = 0
        source.maxBy { c++ }
        assertEquals(3, c)
        c = 0
        source.maxByOrNull { c++ }
        assertEquals(3, c)
    }

    private inline fun <K, V, R : Comparable<R>> expectMinMaxOf(min: R, max: R, elements: Map<K, V>, selector: (Map.Entry<K, V>) -> R) {
        assertEquals(min, elements.minOf(selector))
        assertEquals(min, elements.minOfOrNull(selector))
        assertEquals(max, elements.maxOf(selector))
        assertEquals(max, elements.maxOfOrNull(selector))
    }

    @Test
    fun minMaxOf() {
        konst maps = (1..3).map { size -> listOf("a", "bcd", "Ef").take(size).associateWith { it.length } }

        expectMinMaxOf("a=1", "a=1", maps[0], { it.toString() })
        expectMinMaxOf("a=1", "bcd=3", maps[1], { it.toString() })
        expectMinMaxOf("Ef=2", "bcd=3",  maps[2], { it.toString() })
    }

    @Test
    fun minMaxOfDouble() {
        konst items = mapOf("a" to 0.0, "b" to 1.0, "c" to -1.0)
        assertTrue(items.minOf { it.konstue.pow(0.5) }.isNaN())
        assertTrue(items.minOfOrNull { it.konstue.pow(0.5) }!!.isNaN())
        assertTrue(items.maxOf { it.konstue.pow(0.5) }.isNaN())
        assertTrue(items.maxOfOrNull { it.konstue.pow(0.5) }!!.isNaN())

        assertIsNegativeZero(items.minOf { it.konstue * 0.0 })
        assertIsNegativeZero(items.minOfOrNull { it.konstue * 0.0 }!!)
        assertIsPositiveZero(items.maxOf { it.konstue * 0.0 })
        assertIsPositiveZero(items.maxOfOrNull { it.konstue * 0.0 }!!)
    }

    @Test
    fun minMaxOfFloat() {
        konst items = mapOf("a" to 0.0F, "b" to 1.0F, "c" to -1.0F)
        assertTrue(items.minOf { it.konstue.pow(0.5F) }.isNaN())
        assertTrue(items.minOfOrNull { it.konstue.pow(0.5F) }!!.isNaN())
        assertTrue(items.maxOf { it.konstue.pow(0.5F) }.isNaN())
        assertTrue(items.maxOfOrNull { it.konstue.pow(0.5F) }!!.isNaN())

        assertIsNegativeZero(items.minOf { it.konstue * 0.0F }.toDouble())
        assertIsNegativeZero(items.minOfOrNull { it.konstue * 0.0F }!!.toDouble())
        assertIsPositiveZero(items.maxOf { it.konstue * 0.0F }.toDouble())
        assertIsPositiveZero(items.maxOfOrNull { it.konstue * 0.0F }!!.toDouble())
    }

    @Test
    fun minMaxOfEmpty() {
        konst empty = mapOf<Int, Int>()

        assertNull(empty.minOfOrNull { it.toString() })
        assertNull(empty.maxOfOrNull { it.toString() })
        assertFailsWith<NoSuchElementException> { empty.minOf { it.toString() } }
        assertFailsWith<NoSuchElementException> { empty.maxOf { it.toString() } }


        assertNull(empty.minOfOrNull { 0.0 })
        assertNull(empty.maxOfOrNull { 0.0 })
        assertFailsWith<NoSuchElementException> { empty.minOf { 0.0 } }
        assertFailsWith<NoSuchElementException> { empty.maxOf { 0.0 } }


        assertNull(empty.minOfOrNull { 0.0F })
        assertNull(empty.maxOfOrNull { 0.0F })
        assertFailsWith<NoSuchElementException> { empty.minOf { 0.0F } }
        assertFailsWith<NoSuchElementException> { empty.maxOf { 0.0F } }
    }


    private inline fun <K, V, R> expectMinMaxOfWith(min: R, max: R, elements: Map<K, V>, comparator: Comparator<R>, selector: (Map.Entry<K, V>) -> R) {
        assertEquals(min, elements.minOfWith(comparator, selector))
        assertEquals(min, elements.minOfWithOrNull(comparator, selector))
        assertEquals(max, elements.maxOfWith(comparator, selector))
        assertEquals(max, elements.maxOfWithOrNull(comparator, selector))
    }

    @Test
    fun minMaxOfWith() {
        konst maps = (1..3).map { size -> listOf("a", "bcd", "Ef").take(size).associateWith { it.length } }
        konst comparator = String.CASE_INSENSITIVE_ORDER
        expectMinMaxOfWith("a=1", "a=1", maps[0], comparator, { it.toString() })
        expectMinMaxOfWith("a=1", "bcd=3", maps[1], comparator, { it.toString() })
        expectMinMaxOfWith("a=1", "Ef=2", maps[2], comparator, { it.toString() })
    }

    @Test
    fun minMaxOfWithEmpty() {
        konst empty = mapOf<Int, Int>()
        assertNull(empty.minOfWithOrNull(naturalOrder()) { it.toString() })
        assertNull(empty.maxOfWithOrNull(naturalOrder()) { it.toString() })
        assertFailsWith<NoSuchElementException> { empty.minOfWith(naturalOrder()) { it.toString() } }
        assertFailsWith<NoSuchElementException> { empty.maxOfWith(naturalOrder()) { it.toString() } }
    }

    @Test
    fun constructorWithCapacity() {
        assertFailsWith<IllegalArgumentException> {
            HashMap<String, String>(/*initialCapacity = */-1)
        }
        assertFailsWith<IllegalArgumentException> {
            HashMap<String, String>(/*initialCapacity = */-1, /*loadFactor = */0.5f)
        }
        assertFailsWith<IllegalArgumentException> {
            HashMap<String, String>(/*initialCapacity = */10, /*loadFactor = */0.0f)
        }
        assertFailsWith<IllegalArgumentException> {
            HashMap<String, String>(/*initialCapacity = */10, /*loadFactor = */Float.NaN)
        }
        assertEquals(0, HashMap<String, String>(/*initialCapacity = */0).size)
        assertEquals(0, HashMap<String, String>(/*initialCapacity = */10).size)
        assertEquals(0, HashMap<String, String>(/*initialCapacity = */0, /*loadFactor = */0.5f).size)
        assertEquals(0, HashMap<String, String>(/*initialCapacity = */10, /*loadFactor = */1.5f).size)

        assertFailsWith<IllegalArgumentException> {
            LinkedHashMap<String, String>(/*initialCapacity = */-1)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedHashMap<String, String>(/*initialCapacity = */-1, /*loadFactor = */0.5f)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedHashMap<String, String>(/*initialCapacity = */10, /*loadFactor = */0.0f)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedHashMap<String, String>(/*initialCapacity = */10, /*loadFactor = */Float.NaN)
        }
        assertEquals(0, LinkedHashMap<String, String>(/*initialCapacity = */0).size)
        assertEquals(0, LinkedHashMap<String, String>(/*initialCapacity = */10).size)
        assertEquals(0, LinkedHashMap<String, String>(/*initialCapacity = */0, /*loadFactor = */0.5f).size)
        assertEquals(0, LinkedHashMap<String, String>(/*initialCapacity = */10, /*loadFactor = */1.5f).size)
    }
}
