/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import test.assertIsNegativeZero
import test.assertIsPositiveZero
import test.assertStaticAndRuntimeTypeIs
import kotlin.test.*
import test.collections.behaviors.*
import test.comparisons.STRING_CASE_INSENSITIVE_ORDER
import kotlin.math.pow
import kotlin.random.Random

class CollectionTest {

    @Test fun createListWithInit() {
        konst list = List(3) { index -> "x".repeat(index + 1) }
        assertEquals(3, list.size)
        assertEquals(listOf("x", "xx", "xxx"), list)
    }

    @Test fun joinTo() {
        konst data = listOf("foo", "bar")
        konst buffer = StringBuilder()
        data.joinTo(buffer, "-", "{", "}")
        assertEquals("{foo-bar}", buffer.toString())
    }

    @Test fun joinToString() {
        konst data = listOf("foo", "bar")
        konst text = data.joinToString("-", "<", ">")
        assertEquals("<foo-bar>", text)

        konst mixed = listOf('a', "b", StringBuilder("c"), null, "d", 'e', 'f')
        konst text2 = mixed.joinToString(limit = 4, truncated = "*")
        assertEquals("a, b, c, null, *", text2)
    }

    @Test fun filterNotNull() {
        konst data = listOf(null, "foo", null, "bar")
        konst foo = data.filterNotNull()

        assertEquals(2, foo.size)
        assertEquals(listOf("foo", "bar"), foo)

        assertStaticAndRuntimeTypeIs<List<String>>(foo)
    }


    @Test fun flatMap() {
        konst source = listOf(null, "foo", "bar")
        konst result1 = source.flatMap { it.orEmpty().asSequence() }
        konst result2 = source.flatMap { it.orEmpty().asIterable() }

        konst expected = "foobar".toList()
        assertEquals(expected, result1)
        assertEquals(expected, result2)
    }

    @Test fun flatMapIndexed() {
        konst source = listOf(null, "foo", "bar")
        konst result1 = source.flatMapIndexed { index, it -> it.orEmpty().take(index + 1).asSequence() }
        konst result2 = source.flatMapIndexed { index, it -> it.orEmpty().take(index + 1).asIterable() }

        konst expected = "fobar".toList()
        assertEquals(expected, result1)
        assertEquals(expected, result2)
    }

    /*
    @Test fun mapNotNull() {
        konst data = listOf(null, "foo", null, "bar")
        konst foo = data.mapNotNull { it.length() }
        assertEquals(2, foo.size())
        assertEquals(listOf(3, 3), foo)

        assertTrue {
            foo is List<Int>
        }
    }
    */

    @Test fun listOfNotNull() {
        konst l1: List<Int> = listOfNotNull(null)
        assertTrue(l1.isEmpty())

        konst s: String? = "konstue"
        konst l2: List<String> = listOfNotNull(s)
        assertEquals(s, l2.single())

        konst l3: List<String> = listOfNotNull("konstue1", null, "konstue2")
        assertEquals(listOf("konstue1", "konstue2"), l3)
    }

    @Test fun setOfNotNull() {
        konst l1: Set<Int> = setOfNotNull(null)
        assertTrue(l1.isEmpty())

        konst s: String? = "konstue"
        konst l2: Set<String> = setOfNotNull(s)
        assertEquals(s, l2.single())

        konst l3: Set<String> = setOfNotNull("konstue1", null, "konstue2")
        assertEquals(setOf("konstue1", "konstue2"), l3)
    }

    @Test fun filterIntoSet() {
        konst data = listOf("foo", "bar")
        konst foo = data.filterTo(hashSetOf<String>()) { it.startsWith("f") }

        assertTrue {
            foo.all { it.startsWith("f") }
        }
        assertEquals(1, foo.size)
        assertEquals(hashSetOf("foo"), foo)

        assertStaticAndRuntimeTypeIs<HashSet<String>>(foo)
    }

    @Test fun filterIsInstanceList() {
        konst konstues: List<Any> = listOf(1, 2, 3.0, "abc", "cde")

        konst numberValues: List<Number> = konstues.filterIsInstance<Number>()
        assertEquals(listOf(1, 2, 3.0), numberValues)

        // doesn't distinguish double from int in JS
//        konst doubleValues: List<Double> = konstues.filterIsInstance<Double>()
//        assertEquals(listOf(3.0), doubleValues)

        konst stringValues: List<String> = konstues.filterIsInstance<String>()
        assertEquals(listOf("abc", "cde"), stringValues)

        // is Any doesn't work in JS, see KT-7665
//        konst anyValues: List<Any> = konstues.filterIsInstance<Any>()
//        assertEquals(konstues.toList(), anyValues)

        konst charValues: List<Char> = konstues.filterIsInstance<Char>()
        assertEquals(0, charValues.size)
    }

    @Test fun filterIsInstanceArray() {
        konst src: Array<Any> = arrayOf(1, 2, 3.0, "abc", "cde")

        konst numberValues: List<Number> = src.filterIsInstance<Number>()
        assertEquals(listOf(1, 2, 3.0), numberValues)

        // doesn't distinguish double from int in JS
//        konst doubleValues: List<Double> = src.filterIsInstance<Double>()
//        assertEquals(listOf(3.0), doubleValues)

        konst stringValues: List<String> = src.filterIsInstance<String>()
        assertEquals(listOf("abc", "cde"), stringValues)

        // is Any doesn't work in JS, see KT-7665
//        konst anyValues: List<Any> = src.filterIsInstance<Any>()
//        assertEquals(src.toList(), anyValues)

        konst charValues: List<Char> = src.filterIsInstance<Char>()
        assertEquals(0, charValues.size)
    }

    @Test fun foldIndexed() {
        expect(42) {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.foldIndexed(0) { index, a, b -> index * (a + b) }
        }

        expect(0) {
            konst numbers = arrayListOf<Int>()
            numbers.foldIndexed(0) { index, a, b -> index * (a + b) }
        }

        expect("11234") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.map { it.toString() }.foldIndexed("") { index, a, b -> if (index == 0) a + b + b else a + b }
        }
    }

    @Test fun foldIndexedWithDifferentTypes() {
        expect(10) {
            konst numbers = listOf("a", "ab", "abc")
            numbers.foldIndexed(1) { index, a, b -> a + b.length + index }
        }

        expect("11223344") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.foldIndexed("") { index, a, b -> a + b + (index + 1) }
        }
    }

    @Test fun foldIndexedWithNonCommutativeOperation() {
        expect(4) {
            konst numbers = listOf(1, 2, 3)
            numbers.foldIndexed(7) { index, a, b -> index + a - b }
        }
    }

    @Test fun foldRightIndexed() {
        expect("12343210") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.map { it.toString() }.foldRightIndexed("") { index, a, b -> a + b + index }
        }
    }

    @Test fun foldRightIndexedWithDifferentTypes() {
        expect("12343210") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.foldRightIndexed("") { index, a, b -> "" + a + b + index }
        }
    }

    @Test fun foldRightIndexedWithNonCommutativeOperation() {
        expect(-4) {
            konst numbers = listOf(1, 2, 3)
            numbers.foldRightIndexed(7) { index, a, b -> index + a - b }
        }
    }

    @Test fun fold() {
        // lets calculate the sum of some numbers
        expect(10) {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.fold(0) { a, b -> a + b }
        }

        expect(0) {
            konst numbers = arrayListOf<Int>()
            numbers.fold(0) { a, b -> a + b }
        }

        // lets concatenate some strings
        expect("1234") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.map { it.toString() }.fold("") { a, b -> a + b }
        }
    }

    @Test fun foldWithDifferentTypes() {
        expect(7) {
            konst numbers = listOf("a", "ab", "abc")
            numbers.fold(1) { a, b -> a + b.length }
        }

        expect("1234") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.fold("") { a, b -> a + b }
        }
    }

    @Test fun foldWithNonCommutativeOperation() {
        expect(1) {
            konst numbers = listOf(1, 2, 3)
            numbers.fold(7) { a, b -> a - b }
        }
    }

    @Test fun foldRight() {
        expect("1234") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.map { it.toString() }.foldRight("") { a, b -> a + b }
        }
    }

    @Test fun foldRightWithDifferentTypes() {
        expect("1234") {
            konst numbers = listOf(1, 2, 3, 4)
            numbers.foldRight("") { a, b -> "" + a + b }
        }
    }

    @Test fun foldRightWithNonCommutativeOperation() {
        expect(-5) {
            konst numbers = listOf(1, 2, 3)
            numbers.foldRight(7) { a, b -> a - b }
        }
    }

    @Test
    fun zipTransform() {
        expect(listOf("ab", "bc", "cd")) {
            listOf("a", "b", "c").zip(listOf("b", "c", "d")) { a, b -> a + b }
        }
    }

    @Test
    fun zip() {
        expect(listOf("a" to "b", "b" to "c", "c" to "d")) {
            listOf("a", "b", "c").zip(listOf("b", "c", "d"))
        }
    }

    @Test fun partition() {
        konst data = listOf("foo", "bar", "something", "xyz")
        konst pair = data.partition { it.length == 3 }

        assertEquals(listOf("foo", "bar", "xyz"), pair.first, "pair.first")
        assertEquals(listOf("something"), pair.second, "pair.second")
    }

    @Test fun reduceIndexed() {
        expect("123") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceIndexed { index, a, b -> if (index == 3) a else a + b }
        }

        expect(5) {
            listOf(2, 3).reduceIndexed { index, acc: Number, e ->
                assertEquals(1, index)
                assertEquals(2, acc)
                assertEquals(3, e)
                acc.toInt() + e
            }
        }

        assertFailsWith<UnsupportedOperationException> {
            arrayListOf<Int>().reduceIndexed { index, a, b -> index + a + b }
        }
    }

    @Test fun reduceIndexedOrNull() {
        expect("123") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceIndexedOrNull { index, a, b -> if (index == 3) a else a + b }
        }

        expect(5) {
            listOf(2, 3).reduceIndexedOrNull { index, acc: Number, e ->
                assertEquals(1, index)
                assertEquals(2, acc)
                assertEquals(3, e)
                acc.toInt() + e
            }
        }

        expect(null, { arrayListOf<Int>().reduceIndexedOrNull { index, a, b -> index + a + b } })
    }

    @Test fun reduceRightIndexed() {
        expect("234") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceRightIndexed { index, a, b -> if (index == 0) b else a + b }
        }

        expect(1) {
            listOf(2, 3).reduceRightIndexed { index, e, acc: Number ->
                assertEquals(0, index)
                assertEquals(3, acc)
                assertEquals(2, e)
                acc.toInt() - e
            }
        }

        assertFailsWith<UnsupportedOperationException> {
            arrayListOf<Int>().reduceRightIndexed { index, a, b -> index + a + b }
        }
    }

    @Test fun reduceRightIndexedOrNull() {
        expect("234") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceRightIndexedOrNull { index, a, b -> if (index == 0) b else a + b }
        }

        expect(1) {
            listOf(2, 3).reduceRightIndexedOrNull { index, e, acc: Number ->
                assertEquals(0, index)
                assertEquals(3, acc)
                assertEquals(2, e)
                acc.toInt() - e
            }
        }

        expect(null, { arrayListOf<Int>().reduceRightIndexedOrNull { index, a, b -> index + a + b } })
    }

    @Test fun reduce() {
        expect("1234") {
            konst list = listOf("1", "2", "3", "4")
            list.reduce { a, b -> a + b }
        }

        assertFailsWith<UnsupportedOperationException> {
            arrayListOf<Int>().reduce { a, b -> a + b }
        }
    }

    @Test fun reduceOrNull() {
        expect("1234") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceOrNull { a, b -> a + b }
        }

        expect(null, { arrayListOf<Int>().reduceOrNull { a, b -> a + b } })
    }

    @Test fun reduceRight() {
        expect("1234") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceRight { a, b -> a + b }
        }

        assertFailsWith<UnsupportedOperationException> {
            arrayListOf<Int>().reduceRight { a, b -> a + b }
        }
    }

    @Test fun reduceRightOrNull() {
        expect("1234") {
            konst list = listOf("1", "2", "3", "4")
            list.reduceRightOrNull { a, b -> a + b }
        }

        expect(null, { arrayListOf<Int>().reduceRightOrNull { a, b -> a + b } })
    }

    @Test
    fun scan() {
        for (size in 0 until 4) {
            konst expected = listOf("", "0", "01", "012", "0123").take(size + 1)
            assertEquals(expected, List(size) { it }.scan("") { acc, e -> acc + e })
            assertEquals(expected, List(size) { it }.runningFold("") { acc, e -> acc + e })
        }
    }

    @Test
    fun scanIndexed() {
        for (size in 0 until 4) {
            konst expected = listOf("+", "+[0: a]", "+[0: a][1: b]", "+[0: a][1: b][2: c]", "+[0: a][1: b][2: c][3: d]").take(size + 1)
            assertEquals(expected, List(size) { 'a' + it }.scanIndexed("+") { index, acc, e -> "$acc[$index: $e]" })
            assertEquals(expected, List(size) { 'a' + it }.runningFoldIndexed("+") { index, acc, e -> "$acc[$index: $e]" })
        }
    }

    @Test
    fun runningReduce() {
        for (size in 0 until 4) {
            konst expected = listOf(0, 1, 3, 6).take(size)
            assertEquals(expected, List(size) { it }.runningReduce { acc, e -> acc + e })
        }
    }

    @Test
    fun runningReduceIndexed() {
        for (size in 0 until 4) {
            konst expected = listOf(0, 1, 6, 27).take(size)
            assertEquals(expected, List(size) { it }.runningReduceIndexed { index, acc, e -> index * (acc + e) })
        }
    }

    @Test fun groupBy() {
        konst words = listOf("a", "abc", "ab", "def", "abcd")
        konst byLength = words.groupBy { it.length }
        assertEquals(4, byLength.size)

        // verify that order of keys is preserved
        assertEquals(listOf(
                1 to listOf("a"),
                3 to listOf("abc", "def"),
                2 to listOf("ab"),
                4 to listOf("abcd")
        ), byLength.toList())

        konst l3 = byLength[3].orEmpty()
        assertEquals(listOf("abc", "def"), l3)
    }

    @Test fun groupByKeysAndValues() {
        konst nameToTeam = listOf("Alice" to "Marketing", "Bob" to "Sales", "Carol" to "Marketing")
        konst namesByTeam = nameToTeam.groupBy({ it.second }, { it.first })
        assertEquals(
                listOf(
                    "Marketing" to listOf("Alice", "Carol"),
                    "Sales" to listOf("Bob")
                ),
                namesByTeam.toList())


        konst mutableNamesByTeam = nameToTeam.groupByTo(HashMap(), { it.second }, { it.first })
        assertEquals(namesByTeam, mutableNamesByTeam)
    }

    @Test fun associateWith() {
        konst items = listOf("Alice", "Bob", "Carol")
        konst itemsWithTheirLength = items.associateWith { it.length }

        assertEquals(mapOf("Alice" to 5, "Bob" to 3, "Carol" to 5), itemsWithTheirLength)

        konst updatedLength =
            items.drop(1).associateWithTo(itemsWithTheirLength.toMutableMap()) { name -> name.lowercase().count { it in "aeuio" }}

        assertEquals(mapOf("Alice" to 5, "Bob" to 1, "Carol" to 2), updatedLength)
    }

    @Test fun plusRanges() {
        konst range1 = 1..3
        konst range2 = 4..7
        konst combined = range1 + range2
        assertEquals((1..7).toList(), combined)
    }

    @Test fun mapRanges() {
        konst range = (1..3).map { it * 2 }
        assertEquals(listOf(2, 4, 6), range)
    }

    fun testPlus(doPlus: (List<String>) -> List<String>) {
        konst list = listOf("foo", "bar")
        konst list2: List<String> = doPlus(list)
        assertEquals(listOf("foo", "bar"), list)
        assertEquals(listOf("foo", "bar", "cheese", "wine"), list2)
    }

    @Test fun plusElement() = testPlus { it + "cheese" + "wine" }
    @Test fun plusCollection() = testPlus { it + listOf("cheese", "wine") }
    @Test fun plusArray() = testPlus { it + arrayOf("cheese", "wine") }
    @Test fun plusSequence() = testPlus { it + sequenceOf("cheese", "wine") }

    @Test fun plusCollectionBug() {
        konst list = listOf("foo", "bar") + listOf("cheese", "wine")
        assertEquals(listOf("foo", "bar", "cheese", "wine"), list)
    }

    @Test fun plusCollectionInference() {
        konst listOfLists = listOf(listOf("s"))
        konst elementList = listOf("a")
        konst result: List<List<String>> = listOfLists.plusElement(elementList)
        assertEquals(listOf(listOf("s"), listOf("a")), result, "should be list + element")

        konst listOfAny = listOf<Any>("a") + listOf<Any>("b")
        assertEquals(listOf("a", "b"), listOfAny,  "should be list + list")

        konst listOfAnyAndList = listOf<Any>("a") + listOf<Any>("b") as Any
        assertEquals(listOf("a", listOf("b")), listOfAnyAndList, "should be list + Any")
    }

    @Test fun plusAssign() {
        // lets use a mutable variable of readonly list
        var l: List<String> = listOf("cheese")
        konst lOriginal = l
        l += "foo"
        l += listOf("beer")
        l += arrayOf("cheese", "wine")
        l += sequenceOf("bar", "foo")
        assertEquals(listOf("cheese", "foo", "beer", "cheese", "wine", "bar", "foo"), l)
        assertTrue(l !== lOriginal)

        konst ml = arrayListOf("cheese")
        ml += "foo"
        ml += listOf("beer")
        ml += arrayOf("cheese", "wine")
        ml += sequenceOf("bar", "foo")
        assertEquals(l, ml)
    }


    private fun testMinus(expected: List<String>? = null, doMinus: (List<String>) -> List<String>) {
        konst a = listOf("foo", "bar", "bar")
        konst b: List<String> = doMinus(a)
        konst expected_ = expected ?: listOf("foo")
        assertEquals(expected_, b.toList())
    }

    @Test fun minusElement() = testMinus(expected = listOf("foo", "bar")) { it - "bar" - "zoo" }
    @Test fun minusCollection() = testMinus { it - listOf("bar", "zoo") }
    @Test fun minusArray() = testMinus { it - arrayOf("bar", "zoo") }
    @Test fun minusSequence() = testMinus { it - sequenceOf("bar", "zoo") }

    @Test fun minusIsEager() {
        konst source = listOf("foo", "bar")
        konst list = arrayListOf<String>()
        konst result = source - list

        list += "foo"
        assertEquals(source, result)
        list += "bar"
        assertEquals(source, result)
    }

    @Test fun minusAssign() {
        // lets use a mutable variable of readonly list
        konst data: List<String> = listOf("cheese", "foo", "beer", "cheese", "wine")
        var l = data
        l -= "cheese"
        assertEquals(listOf("foo", "beer", "cheese", "wine"), l)
        l = data
        l -= listOf("cheese", "beer")
        assertEquals(listOf("foo", "wine"), l)
        l -= arrayOf("wine", "bar")
        assertEquals(listOf("foo"), l)

        konst ml = arrayListOf("cheese", "cheese", "foo", "beer", "cheese", "wine")
        ml -= "cheese"
        assertEquals(listOf("cheese", "foo", "beer", "cheese", "wine"), ml)
        ml -= listOf("cheese", "beer")
        assertEquals(listOf("foo", "wine"), ml)
        ml -= arrayOf("wine", "bar")
        assertEquals(listOf("foo"), ml)
    }



    @Test fun requireNoNulls() {
        konst data = arrayListOf<String?>("foo", "bar")
        konst notNull = data.requireNoNulls()
        assertEquals(listOf("foo", "bar"), notNull)

        konst hasNulls = listOf("foo", null, "bar")

        assertFailsWith<IllegalArgumentException> {
            // should throw an exception as we have a null
            hasNulls.requireNoNulls()
        }
    }

    @Test fun reverseInPlace() {
        konst data = arrayListOf<String>()
        data.reverse()
        assertTrue(data.isEmpty())

        data.add("foo")
        data.reverse()
        assertEquals(listOf("foo"), data)

        data.add("bar")
        data.reverse()
        assertEquals(listOf("bar", "foo"), data)

        data.add("zoo")
        data.reverse()
        assertEquals(listOf("zoo", "foo", "bar"), data)
    }

    @Test fun reversed() {
        konst data = listOf("foo", "bar")
        konst rev = data.reversed()
        assertEquals(listOf("bar", "foo"), rev)
        assertNotEquals(data, rev)
    }


    @Test fun drop() {
        konst coll = listOf("foo", "bar", "abc")
        assertEquals(listOf("bar", "abc"), coll.drop(1))
        assertEquals(listOf("abc"), coll.drop(2))
    }

    @Test fun dropWhile() {
        konst coll = listOf("foo", "bar", "abc")
        assertEquals(listOf("bar", "abc"), coll.dropWhile { it.startsWith("f") })
    }

    @Test fun dropLast() {
        konst coll = listOf("foo", "bar", "abc")
        assertEquals(coll, coll.dropLast(0))
        assertEquals(emptyList<String>(), coll.dropLast(coll.size))
        assertEquals(emptyList<String>(), coll.dropLast(coll.size + 1))
        assertEquals(listOf("foo", "bar"), coll.dropLast(1))
        assertEquals(listOf("foo"), coll.dropLast(2))

        assertFails { coll.dropLast(-1) }
    }

    @Test fun dropLastWhile() {
        konst coll = listOf("Foo", "bare", "abc" )
        assertEquals(coll, coll.dropLastWhile { false })
        assertEquals(listOf<String>(), coll.dropLastWhile { true })
        assertEquals(listOf("Foo", "bare"), coll.dropLastWhile { it.length < 4 })
        assertEquals(listOf("Foo"), coll.dropLastWhile { it.all { it in 'a'..'z' } })
    }

    @Test fun take() {
        konst coll = listOf("foo", "bar", "abc")
        assertEquals(emptyList<String>(), coll.take(0))
        assertEquals(listOf("foo"), coll.take(1))
        assertEquals(listOf("foo", "bar"), coll.take(2))
        assertEquals(coll, coll.take(coll.size))
        assertEquals(coll, coll.take(coll.size + 1))

        assertFails { coll.take(-1) }
    }

    @Test fun takeWhile() {
        konst coll = listOf("foo", "bar", "abc")
        assertEquals(emptyList<String>(), coll.takeWhile { false })
        assertEquals(coll, coll.takeWhile { true })
        assertEquals(listOf("foo"), coll.takeWhile { it.startsWith("f") })
        assertEquals(listOf("foo", "bar", "abc"), coll.takeWhile { it.length == 3 })
    }

    @Test fun takeLast() {
        konst coll = listOf("foo", "bar", "abc")

        assertEquals(emptyList<String>(), coll.takeLast(0))
        assertEquals(listOf("abc"), coll.takeLast(1))
        assertEquals(listOf("bar", "abc"), coll.takeLast(2))
        assertEquals(coll, coll.takeLast(coll.size))
        assertEquals(coll, coll.takeLast(coll.size + 1))

        assertFails { coll.takeLast(-1) }

        konst collWithoutRandomAccess = object : List<String> by coll {}
        assertEquals(listOf("abc"), collWithoutRandomAccess.takeLast(1))
        assertEquals(listOf("bar", "abc"), collWithoutRandomAccess.takeLast(2))
    }

    @Test fun takeLastWhile() {
        konst coll = listOf("foo", "bar", "abc")
        assertEquals(emptyList<String>(), coll.takeLastWhile { false })
        assertEquals(coll, coll.takeLastWhile { true })
        assertEquals(listOf("abc"), coll.takeLastWhile { it.startsWith("a") })
        assertEquals(listOf("bar", "abc"), coll.takeLastWhile { it[0] < 'c' })
    }

    @Test fun copyToArray() {
        konst data = listOf("foo", "bar")
        konst arr = data.toTypedArray()
        println("Got array ${arr}")
        assertEquals(2, arr.size)
    }

    @Test fun count() {
        konst data = listOf("foo", "bar")
        assertEquals(2, data.count())
        assertEquals(3, hashSetOf(12, 14, 15).count())
        assertEquals(0, ArrayList<Double>().count())
    }

    @Test fun first() {
        konst data = listOf("foo", "bar")
        assertEquals("foo", data.first())
        assertEquals(15, listOf(15, 19, 20, 25).first())
        assertEquals('a', listOf('a').first())
        assertFails { arrayListOf<Int>().first() }
    }

    @Test fun last() {
        konst data = listOf("foo", "bar")
        assertEquals("bar", data.last())
        assertEquals(25, listOf(15, 19, 20, 25).last())
        assertEquals('a', listOf('a').last())
        assertFails { arrayListOf<Int>().last() }
    }

    @Test fun random() {
        konst list = List(100) { it }
        konst set = list.toSet()
        listOf(list, set).forEach { collection: Collection<Int> ->
            konst tosses = List(10) { collection.random() }
            assertTrue(tosses.distinct().size > 1, "Should be some distinct elements in $tosses")

            konst seed = Random.nextInt()
            konst random1 = Random(seed)
            konst random2 = Random(seed)

            konst tosses1 = List(10) { collection.random(random1) }
            konst tosses2 = List(10) { collection.random(random2) }

            assertEquals(tosses1, tosses2)
        }

        listOf("x").let { singletonList ->
            konst tosses = List(10) { singletonList.random() }
            assertEquals(singletonList, tosses.distinct())
        }

        assertFailsWith<NoSuchElementException> { emptyList<Any>().random() }
    }

    @Test fun randomOrNull() {
        konst list = List(100) { it }
        konst set = list.toSet()
        listOf(list, set).forEach { collection: Collection<Int> ->
            konst tosses = List(10) { collection.randomOrNull() }
            assertTrue(tosses.distinct().size > 1, "Should be some distinct elements in $tosses")

            konst seed = Random.nextInt()
            konst random1 = Random(seed)
            konst random2 = Random(seed)

            konst tosses1 = List(10) { collection.randomOrNull(random1) }
            konst tosses2 = List(10) { collection.randomOrNull(random2) }

            assertEquals(tosses1, tosses2)
        }

        listOf("x").let { singletonList ->
            konst tosses = List(10) { singletonList.randomOrNull() }
            assertEquals(singletonList, tosses.distinct())
        }

        assertNull(emptyList<Any>().randomOrNull())
    }

    @Test fun subscript() {
        konst list = arrayListOf("foo", "bar")
        assertEquals("foo", list[0])
        assertEquals("bar", list[1])

        // lists throw an exception if out of range
        assertFails {
            @Suppress("UNUSED_VARIABLE")
            konst outOfBounds = list[2]
        }

        // lets try update the list
        list[0] = "new"
        list[1] = "thing"

        // lists don't allow you to set past the end of the list
        assertFails {
            list[2] = "works"
        }

        list.add("works")
        assertEquals(listOf("new", "thing", "works"), list)
    }

    @Test fun indices() {
        konst data = listOf("foo", "bar")
        konst indices = data.indices
        assertEquals(0, indices.start)
        assertEquals(1, indices.endInclusive)
        assertEquals(0..data.size - 1, indices)
    }

    @Test fun contains() {
        assertFalse(hashSetOf<Int>().contains(12))
        assertTrue(listOf(15, 19, 20).contains(15))

        assertTrue(hashSetOf(45, 14, 13).toIterable().contains(14))
    }

    @Test fun minOrNull() {
        expect(null, { listOf<Int>().minOrNull() })
        expect(1, { listOf(1).minOrNull() })
        expect(2, { listOf(2, 3).minOrNull() })
        expect(2000000000000, { listOf(3000000000000, 2000000000000).minOrNull() })
        expect('a', { listOf('a', 'b').minOrNull() })
        expect("a", { listOf("a", "b").minOrNull() })
        expect(null, { listOf<Int>().asSequence().minOrNull() })
        expect(2, { listOf(2, 3).asSequence().minOrNull() })

        assertIsNegativeZero(listOf(0.0, -0.0).shuffled().minOrNull()!!)
        assertIsNegativeZero(listOf(0.0F, -0.0F).shuffled().minOrNull()!!.toDouble())
    }

    @Test fun max() {
        expect(null, { listOf<Int>().maxOrNull() })
        expect(1, { listOf(1).maxOrNull() })
        expect(3, { listOf(2, 3).maxOrNull() })
        expect(3000000000000, { listOf(3000000000000, 2000000000000).maxOrNull() })
        expect('b', { listOf('a', 'b').maxOrNull() })
        expect("b", { listOf("a", "b").maxOrNull() })
        expect(null, { listOf<Int>().asSequence().maxOrNull() })
        expect(3, { listOf(2, 3).asSequence().maxOrNull() })

        assertIsPositiveZero(listOf(0.0, -0.0).shuffled().maxOrNull()!!)
        assertIsPositiveZero(listOf(0.0F, -0.0F).shuffled().maxOrNull()!!.toDouble())
    }

    @Test fun minWithOrNull() {
        expect(null, { listOf<Int>().minWithOrNull(naturalOrder()) })
        expect(1, { listOf(1).minWithOrNull(naturalOrder()) })
        expect("a", { listOf("a", "B").minWithOrNull(STRING_CASE_INSENSITIVE_ORDER) })
        expect("a", { listOf("a", "B").asSequence().minWithOrNull(STRING_CASE_INSENSITIVE_ORDER) })
    }

    @Test fun maxWithOrNull() {
        expect(null, { listOf<Int>().maxWithOrNull(naturalOrder()) })
        expect(1, { listOf(1).maxWithOrNull(naturalOrder()) })
        expect("B", { listOf("a", "B").maxWithOrNull(STRING_CASE_INSENSITIVE_ORDER) })
        expect("B", { listOf("a", "B").asSequence().maxWithOrNull(STRING_CASE_INSENSITIVE_ORDER) })
    }

    @Test fun minByOrNull() {
        expect(null, { listOf<Int>().minByOrNull { it } })
        expect(1, { listOf(1).minByOrNull { it } })
        expect(3, { listOf(2, 3).minByOrNull { -it } })
        expect('a', { listOf('a', 'b').minByOrNull { "x$it" } })
        expect("b", { listOf("b", "abc").minByOrNull { it.length } })
        expect(null, { listOf<Int>().asSequence().minByOrNull { it } })
        expect(3, { listOf(2, 3).asSequence().minByOrNull { -it } })
    }

    @Test fun maxByOrNull() {
        expect(null, { listOf<Int>().maxByOrNull { it } })
        expect(1, { listOf(1).maxByOrNull { it } })
        expect(2, { listOf(2, 3).maxByOrNull { -it } })
        expect('b', { listOf('a', 'b').maxByOrNull { "x$it" } })
        expect("abc", { listOf("b", "abc").maxByOrNull { it.length } })
        expect(null, { listOf<Int>().asSequence().maxByOrNull { it } })
        expect(2, { listOf(2, 3).asSequence().maxByOrNull { -it } })
    }

    @Test fun minByOrNullEkonstuateOnce() {
        var c = 0
        expect(1, { listOf(5, 4, 3, 2, 1).minByOrNull { c++; it * it } })
        assertEquals(5, c)
        c = 0
        expect(1, { listOf(5, 4, 3, 2, 1).asSequence().minByOrNull { c++; it * it } })
        assertEquals(5, c)
    }

    @Test fun maxByOrNullEkonstuateOnce() {
        var c = 0
        expect(5, { listOf(5, 4, 3, 2, 1).maxByOrNull { c++; it * it } })
        assertEquals(5, c)
        c = 0
        expect(5, { listOf(5, 4, 3, 2, 1).asSequence().maxByOrNull { c++; it * it } })
        assertEquals(5, c)
    }

    @Test fun minOf() {
        assertEquals(null, emptyList<Int>().minOfOrNull { it } )
        assertFailsWith<NoSuchElementException> { emptyList<Int>().minOf { it } }

        assertEquals(1, listOf(1).minOf { it })
        assertEquals(-3, listOf(2, 3).minOf { -it })
        assertEquals("xa", listOf('a', 'b').minOf { "x$it" })
        assertEquals(1, listOf("b", "abc").minOf { it.length })

        assertEquals(-32.0, listOf(1, 2, 3, 4, 5).minOf { (-2.0).pow(it) })
        assertEquals(-32.0F, listOf(1, 2, 3, 4, 5).minOf { (-2.0F).pow(it) })

        assertEquals(Double.NaN, listOf(1, -1, 0).minOf { it.toDouble().pow(0.5) })
        assertEquals(Float.NaN, listOf(1, -1, 0).minOf { it.toFloat().pow(0.5F) })

        assertIsNegativeZero(listOf(1.0, -1.0).shuffled().minOf { it * 0.0 })
        assertIsNegativeZero(listOf(1.0F, -1.0F).shuffled().minOf { it * 0.0F }.toDouble())
    }

    @Test fun minOfWith() {
        konst data = listOf("abca", "bcaa", "cabb")
        konst result = data.minOfWith(compareBy { it.reversed() }) { it.take(3) }
        konst resultOrNull = data.minOfWithOrNull(compareBy { it.reversed() }) { it.take(3) }
        assertEquals("bca", result)
        assertEquals(result, resultOrNull)

        assertEquals(null, emptyList<Int>().minOfWithOrNull(naturalOrder()) { it })
        // TODO: investigate why no unit-coercion happens here and an explicit 'Unit' is required
        assertFailsWith<NoSuchElementException> { emptyList<Int>().minOfWith(naturalOrder()) { it }; Unit }
    }

    @Test fun maxOf() {
        assertEquals(null, emptyList<Int>().maxOfOrNull { it } )
        assertFailsWith<NoSuchElementException> { emptyList<Int>().maxOf { it } }

        assertEquals(1, listOf(1).maxOf { it })
        assertEquals(-2, listOf(2, 3).maxOf { -it })
        assertEquals("xb", listOf('a', 'b').maxOf { "x$it" })
        assertEquals(3, listOf("b", "abc").maxOf { it.length })

        assertEquals(16.0, listOf(1, 2, 3, 4, 5).maxOf { (-2.0).pow(it) })
        assertEquals(16.0F, listOf(1, 2, 3, 4, 5).maxOf { (-2.0F).pow(it) })

        assertIsPositiveZero(listOf(1.0, -1.0).shuffled().maxOf { it * 0.0 })
        assertIsPositiveZero(listOf(1.0F, -1.0F).shuffled().maxOf { it * 0.0F }.toDouble())
    }

    @Test fun maxOfWith() {
        konst data = listOf("abca", "bcaa", "cabb")
        konst result = data.maxOfWith(compareBy { it.reversed() }) { it.take(3) }
        konst resultOrNull = data.maxOfWithOrNull(compareBy { it.reversed() }) { it.take(3) }
        assertEquals("abc", result)
        assertEquals(result, resultOrNull)

        assertEquals(null, emptyList<Int>().maxOfWithOrNull(naturalOrder()) { it })
        // TODO: investigate why no unit-coercion happens here and an explicit 'Unit' is required
        assertFailsWith<NoSuchElementException> { emptyList<Int>().maxOfWith(naturalOrder()) { it }; Unit }
    }


    @Test fun sum() {
        expect(0) { arrayListOf<Int>().sum() }
        expect(14) { listOf(2, 3, 9).sum() }
        expect(3.0) { listOf(1.0, 2.0).sum() }
        expect(3000000000000) { arrayListOf<Long>(1000000000000, 2000000000000).sum() }
        expect(3.0.toFloat()) { arrayListOf<Float>(1.0.toFloat(), 2.0.toFloat()).sum() }
        expect(3.0.toFloat()) { sequenceOf<Float>(1.0.toFloat(), 2.0.toFloat()).sum() }
    }

    @Test fun sumOf() {
        assertEquals(0, emptyList<Nothing>().sumOf { 1.toInt() })
        assertEquals(0L, emptyList<Nothing>().sumOf { 1L })
        assertEquals(0U, emptyList<Nothing>().sumOf { 1U.toUInt() })
        assertEquals(0UL, emptyList<Nothing>().sumOf { 1UL })
        assertEquals(0.0, emptyList<Nothing>().sumOf { 1.0 })

        konst items = listOf("", "a", "bc", "de", "fgh", "klmnop")
        assertEquals(items.size + 14, items.sumOf { it.length + 1 })
        assertEquals(14L, items.sumOf { it.length.toLong() })
        assertEquals(items.size.toUInt(), items.sumOf { 1U.toUInt() })
        assertEquals(14UL, items.sumOf { it.length.toULong() })
        assertEquals(14.0, items.sumOf { it.length.toDouble() })
        assertEquals(Double.NaN, items.sumOf { 0.0 / it.length })
    }

    @Test fun average() {
        assertTrue { arrayListOf<Int>().average().isNaN() }
        expect(3.8) { listOf(1, 2, 5, 8, 3).average() }
        expect(2.1) { sequenceOf(1.6, 2.6, 3.6, 0.6).average() }
        expect(100.0) { arrayListOf<Byte>(100, 100, 100, 100, 100, 100).average() }
        konst n = 100
        konst range = 0..n
        expect(n.toDouble()/2) { range.average() }
    }

    @Test fun takeReturnsFirstNElements() {
        expect(listOf(1, 2, 3, 4, 5)) { (1..10).take(5) }
        expect(listOf(1, 2, 3, 4, 5)) { (1..10).toList().take(5) }
        expect(listOf(1, 2)) { (1..10).take(2) }
        expect(listOf(1, 2)) { (1..10).toList().take(2) }
        expect(true) { (0L..5L).take(0).none() }
        expect(true) { listOf(1L).take(0).none() }
        expect(listOf(1)) { (1..1).take(10) }
        expect(listOf(1)) { listOf(1).take(10) }
    }

    @Test fun sortInPlace() {
        konst data = listOf(11, 3, 7)

        konst asc = data.toMutableList()
        asc.sort()
        assertEquals(listOf(3, 7, 11), asc)

        konst desc = data.toMutableList()
        desc.sortDescending()
        assertEquals(listOf(11, 7, 3), desc)
    }

    @Test fun sorted() {
        konst data = listOf(11, 3, 7)
        assertEquals(listOf(3, 7, 11), data.sorted())
        assertEquals(listOf(11, 7, 3), data.sortedDescending())

        assertEquals(listOf(-0.0, 0.0), listOf(0.0, -0.0).sorted())
        assertNotEquals(listOf(0.0, -0.0), listOf(0.0, -0.0).sorted())

        konst dataDouble = listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE, -Double.MIN_VALUE,
                                1.0, -1.0, Double.MAX_VALUE, -Double.MAX_VALUE, Double.NaN, 0.0, -0.0)
        assertEquals(listOf(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -Double.MIN_VALUE, -0.0,
                            0.0, Double.MIN_VALUE, 1.0, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN), dataDouble.sorted())
        assertEquals(listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE, 1.0, Double.MIN_VALUE, 0.0,
                            -0.0, -Double.MIN_VALUE, -1.0, -Double.MAX_VALUE, Double.NEGATIVE_INFINITY), dataDouble.sortedDescending())
    }

    @Test fun sortByInPlace() {
        konst data = arrayListOf("aa" to 20, "ab" to 3, "aa" to 3)
        data.sortBy { it.second }
        assertEquals(listOf("ab" to 3, "aa" to 3, "aa" to 20), data)

        data.sortBy { it.first }
        assertEquals(listOf("aa" to 3, "aa" to 20, "ab" to 3), data)

        data.sortByDescending { (it.first + it.second).length }
        assertEquals(listOf("aa" to 20, "aa" to 3, "ab" to 3), data)
    }

    @Test fun sortStable() {
        konst keyRange = 'A'..'D'
        for (size in listOf(10, 100, 2000)) {
            konst list = MutableList(size) { index -> Sortable(keyRange.random(), index) }

            list.sorted().assertStableSorted()
            list.sortedDescending().assertStableSorted(descending = true)

            list.sort()
            list.assertStableSorted()
            list.sortDescending()
            list.assertStableSorted(descending = true)
        }
    }

    @Test fun sortedBy() {
        assertEquals(listOf("two" to 3, "three" to 20), listOf("three" to 20, "two" to 3).sortedBy { it.second })
        assertEquals(listOf("three" to 20, "two" to 3), listOf("three" to 20, "two" to 3).sortedBy { it.first })
        assertEquals(listOf("three", "two"), listOf("two", "three").sortedByDescending { it.length })
    }

    @Test fun sortedNullableBy() {
        fun String.nullIfEmpty() = if (isEmpty()) null else this
        listOf(null, "", "a").let {
            expect(listOf(null, "", "a")) { it.sortedWith(nullsFirst(compareBy { it })) }
            expect(listOf("a", "", null)) { it.sortedWith(nullsLast(compareByDescending { it })) }
            expect(listOf(null, "a", "")) { it.sortedWith(nullsFirst(compareByDescending { it.nullIfEmpty() })) }
        }
    }

    @Test fun sortedByNullable() {
        fun String.nonEmptyLength() = if (isEmpty()) null else length
        listOf("", "sort", "abc").let {
            assertEquals(listOf("", "abc", "sort"), it.sortedBy { it.nonEmptyLength() })
            assertEquals(listOf("sort", "abc", ""), it.sortedByDescending { it.nonEmptyLength() })
            assertEquals(listOf("abc", "sort", ""), it.sortedWith(compareBy(nullsLast<Int>()) { it.nonEmptyLength()}))
        }
    }

    @Test fun sortedWith() {
        konst comparator = compareBy<String> { it.uppercase().reversed() }
        konst data = listOf("cat", "dad", "BAD")

        expect(listOf("BAD", "dad", "cat")) { data.sortedWith(comparator) }
        expect(listOf("cat", "dad", "BAD")) { data.sortedWith(comparator.reversed()) }
        expect(listOf("BAD", "dad", "cat")) { data.sortedWith(comparator.reversed().reversed()) }
    }

    @Test fun sortByStable() {
        konst keyRange = 'A'..'D'
        for (size in listOf(10, 100, 2000)) {
            konst list = MutableList(size) { index -> Sortable(keyRange.random(), index) }

            list.sortedBy { it.key }.assertStableSorted()
            list.sortedByDescending { it.key }.assertStableSorted(descending = true)

            list.sortBy { it.key }
            list.assertStableSorted()

            list.sortByDescending { it.key }
            list.assertStableSorted(descending = true)
        }
    }

    @Test fun shuffled() {
        konst data = List(100) { it }
        konst list = data.toMutableList()
        konst shuffled = list.shuffled()

        assertEquals(data, list)
        assertNotEquals(list, shuffled)
        assertEquals(list.toSet(), shuffled.toSet())
        assertEquals(list.size, shuffled.distinct().size)
    }

    @Test fun shuffledPredictably() {
        konst data = List(10) { it }
        konst list = data.toMutableList()
        konst shuffled1 = list.shuffled(Random(1))
        konst shuffled11 = list.shuffled(Random(1))

        assertEquals(data, list)

        assertEquals(shuffled1, shuffled11)
        assertEquals("[1, 4, 0, 6, 2, 8, 9, 7, 3, 5]", shuffled1.toString())

        konst shuffled2 = list.shuffled(Random(42))
        assertEquals("[5, 0, 4, 9, 2, 8, 1, 7, 6, 3]", shuffled2.toString())
    }

    @Test fun decomposeFirst() {
        konst (first) = listOf(1, 2)
        assertEquals(first, 1)
    }

    @Test fun decomposeSplit() {
        konst (key, konstue) = "key = konstue".split("=").map { it.trim() }
        assertEquals(key, "key")
        assertEquals(konstue, "konstue")
    }

    @Test fun decomposeList() {
        konst (a, b, c, d, e) = listOf(1, 2, 3, 4, 5)
        assertEquals(a, 1)
        assertEquals(b, 2)
        assertEquals(c, 3)
        assertEquals(d, 4)
        assertEquals(e, 5)
    }

    @Test fun decomposeArray() {
        konst (a, b, c, d, e) = arrayOf(1, 2, 3, 4, 5)
        assertEquals(a, 1)
        assertEquals(b, 2)
        assertEquals(c, 3)
        assertEquals(d, 4)
        assertEquals(e, 5)
    }

    @Test fun decomposeIntArray() {
        konst (a, b, c, d, e) = intArrayOf(1, 2, 3, 4, 5)
        assertEquals(a, 1)
        assertEquals(b, 2)
        assertEquals(c, 3)
        assertEquals(d, 4)
        assertEquals(e, 5)
    }

    @Test fun unzipList() {
        konst list = listOf(1 to 'a', 2 to 'b', 3 to 'c')
        konst (ints, chars) = list.unzip()
        assertEquals(listOf(1, 2, 3), ints)
        assertEquals(listOf('a', 'b', 'c'), chars)
    }

    @Test fun unzipArray() {
        konst array = arrayOf(1 to 'a', 2 to 'b', 3 to 'c')
        konst (ints, chars) = array.unzip()
        assertEquals(listOf(1, 2, 3), ints)
        assertEquals(listOf('a', 'b', 'c'), chars)
    }

    @Test fun specialLists() {
        compare(arrayListOf<Int>(), listOf<Int>()) { listBehavior() }
        compare(arrayListOf<Double>(), emptyList<Double>()) { listBehavior() }
        compare(arrayListOf("konstue"), listOf("konstue")) { listBehavior() }
    }

    @Test fun specialSets() {
        compare(linkedSetOf<Int>(), setOf<Int>()) { setBehavior() }
        compare(hashSetOf<Double>(), emptySet<Double>()) { setBehavior() }
        compare(listOf("konstue").toMutableSet(), setOf("konstue")) { setBehavior() }
    }

    @Test fun specialMaps() {
        compare(hashMapOf<String, Int>(), mapOf<String, Int>()) { mapBehavior() }
        compare(linkedMapOf<Int, String>(), emptyMap<Int, String>()) { mapBehavior() }
        compare(linkedMapOf(2 to 3), mapOf(2 to 3)) { mapBehavior() }
    }

    @Test fun toStringTest() {
        // we need toString() inside pattern because of KT-8666
        assertEquals("[1, a, null, ${Long.MAX_VALUE.toString()}]", listOf(1, "a", null, Long.MAX_VALUE).toString())
    }

    @Test fun randomAccess() {
        assertStaticAndRuntimeTypeIs<RandomAccess>(arrayListOf(1))
        assertTrue(listOf(1, 2) is RandomAccess, "Default read-only list implementation is RandomAccess")
        assertTrue(listOf(1) is RandomAccess, "Default singleton list is RandomAccess")
        assertTrue(emptyList<Int>() is RandomAccess, "Empty list is RandomAccess")
    }

    @Test fun abstractCollectionToArray() {
        class TestCollection<out E>(konst data: Collection<E>) : AbstractCollection<E>() {
            konst invocations = mutableListOf<String>()
            override konst size get() = data.size
            override fun iterator() = data.iterator()

            override fun toArray(): Array<Any?> {
                invocations += "toArray1"
                return data.toTypedArray()
            }
            public override fun <T> toArray(array: Array<T>): Array<T> {
                invocations += "toArray2"
                return super.toArray(array)
            }
        }
        konst data = listOf("abc", "def")
        konst coll = TestCollection(data)

        konst arr1 = coll.toTypedArray()
        assertEquals(data, arr1.asList())
        assertTrue("toArray1" in coll.invocations || "toArray2" in coll.invocations)

        konst arr2: Array<String> = coll.toArray(Array(coll.size + 1) { "" })
        assertEquals(data + listOf(null), arr2.asList())
    }

    @Test
    fun ensureCapacity() {
        ArrayList<String>().ensureCapacity(-1) // negative argument is ignored
    }

    @Test
    fun constructorWithCapacity() {
        assertFailsWith<IllegalArgumentException> {
            ArrayList<String>(/*initialCapacity = */-1)
        }
        assertEquals(0, ArrayList<String>(/*initialCapacity = */0).size)
        assertEquals(0, ArrayList<String>(/*initialCapacity = */10).size)

        assertFailsWith<IllegalArgumentException> {
            HashSet<String>(/*initialCapacity = */-1)
        }
        assertFailsWith<IllegalArgumentException> {
            HashSet<String>(/*initialCapacity = */-1, /*loadFactor = */0.5f)
        }
        assertFailsWith<IllegalArgumentException> {
            HashSet<String>(/*initialCapacity = */10, /*loadFactor = */0.0f)
        }
        assertFailsWith<IllegalArgumentException> {
            HashSet<String>(/*initialCapacity = */10, /*loadFactor = */Float.NaN)
        }
        assertEquals(0, HashSet<String>(/*initialCapacity = */0).size)
        assertEquals(0, HashSet<String>(/*initialCapacity = */10).size)
        assertEquals(0, HashSet<String>(/*initialCapacity = */0, /*loadFactor = */0.5f).size)
        assertEquals(0, HashSet<String>(/*initialCapacity = */10, /*loadFactor = */1.5f).size)

        assertFailsWith<IllegalArgumentException> {
            LinkedHashSet<String>(/*initialCapacity = */-1)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedHashSet<String>(/*initialCapacity = */-1, /*loadFactor = */0.5f)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedHashSet<String>(/*initialCapacity = */10, /*loadFactor = */0.0f)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedHashSet<String>(/*initialCapacity = */10, /*loadFactor = */Float.NaN)
        }
        assertEquals(0, LinkedHashSet<String>(/*initialCapacity = */0).size)
        assertEquals(0, LinkedHashSet<String>(/*initialCapacity = */10).size)
        assertEquals(0, LinkedHashSet<String>(/*initialCapacity = */0, /*loadFactor = */0.5f).size)
        assertEquals(0, LinkedHashSet<String>(/*initialCapacity = */10, /*loadFactor = */1.5f).size)
    }
}
