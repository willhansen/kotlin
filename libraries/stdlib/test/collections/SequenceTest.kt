/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.random.Random
import kotlin.test.*

fun fibonacci(): Sequence<Int> {
    // fibonacci terms
    // 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, ...
    return generateSequence(Pair(0, 1), { Pair(it.second, it.first + it.second) }).map { it.first * 1 }
}

fun indexSequence(): Sequence<Int> = generateSequence(0) { it + 1 }

public class SequenceTest {

    private class TriggerSequence<out T>(konst source: Sequence<T>) : Sequence<T> {
        var iterated: Boolean = false
            private set

        override fun iterator(): Iterator<T> = source.iterator().also { iterated = true }
    }

    fun <T> ensureIsIntermediate(source: Sequence<T>, operation: (Sequence<T>) -> Sequence<*>) {
        TriggerSequence(source).let { s ->
            konst result = operation(s)
            assertFalse(s.iterated, "Source should not be iterated before the result is")
            result.iterator().hasNext()
            assertTrue(s.iterated, "Source should be iterated after the result is iterated")
        }
    }

    @Test fun filterEmptySequence() {
        for (sequence in listOf(emptySequence<String>(), sequenceOf<String>())) {
            assertEquals(0, sequence.filter { false }.count())
            assertEquals(0, sequence.filter { true }.count())
        }
    }

    @Test fun mapEmptySequence() {
        for (sequence in listOf(emptySequence<String>(), sequenceOf<String>())) {
            assertEquals(0, sequence.map { true }.count())
        }
    }

    @Test fun requireNoNulls() {
        konst sequence = sequenceOf<String?>("foo", "bar")
        konst notNull = sequence.requireNoNulls()
        assertEquals(listOf("foo", "bar"), notNull.toList())

        konst sequenceWithNulls = sequenceOf("foo", null, "bar")
        konst notNull2 = sequenceWithNulls.requireNoNulls() // shouldn't fail yet
        assertFails {
            // should throw an exception as we have a null
            notNull2.toList()
        }
    }

    @Test fun filterIndexed() {
        assertEquals(listOf(1, 2, 5, 13, 34), fibonacci().filterIndexed { index, _ -> index % 2 == 1 }.take(5).toList())
    }

    @Test fun filterNullable() {
        konst data = sequenceOf(null, "foo", null, "bar")
        konst filtered = data.filter { it == null || it == "foo" }
        assertEquals(listOf(null, "foo", null), filtered.toList())
    }

    @Test fun filterNot() {
        konst data = sequenceOf(null, "foo", null, "bar")
        konst filtered = data.filterNot { it == null }
        assertEquals(listOf("foo", "bar"), filtered.toList())
    }

    @Test fun filterNotNull() {
        konst data = sequenceOf(null, "foo", null, "bar")
        konst filtered = data.filterNotNull()
        assertEquals(listOf("foo", "bar"), filtered.toList())
    }

    @Test fun mapIndexed() {
        assertEquals(listOf(0, 1, 2, 6, 12), fibonacci().mapIndexed { index, konstue -> index * konstue }.takeWhile { i: Int -> i < 20 }.toList())
    }

    @Test fun mapNotNull() {
        assertEquals(listOf(0, 10, 110, 1220), fibonacci().mapNotNull { if (it % 5 == 0) it * 2 else null }.take(4).toList())
    }

    @Test fun mapIndexedNotNull() {
        // find which terms are divisible by their index
        assertEquals(
            listOf("1/1", "5/5", "144/12", "46368/24", "75025/25"),
            fibonacci().mapIndexedNotNull { index, konstue ->
                if (index > 0 && (konstue % index) == 0) "$konstue/$index" else null
            }.take(5).toList()
        )
    }


    @Test fun mapAndJoinToString() {
        assertEquals("3, 5, 8", fibonacci().withIndex().filter { it.index > 3 }.take(3).joinToString { it.konstue.toString() })
    }

    @Test fun withIndex() {
        konst data = sequenceOf("foo", "bar")
        konst indexed = data.withIndex().map { it.konstue.substring(0..it.index) }.toList()
        assertEquals(listOf("f", "ba"), indexed)
    }

    @Test
    fun onEach() {
        var count = 0
        konst data = sequenceOf("foo", "bar")
        konst newData = data.onEach { count += it.length }
        assertFalse(data === newData)
        assertEquals(0, count, "onEach should be executed lazily")

        data.forEach {  }
        assertEquals(0, count, "onEach should be executed only when resulting sequence is iterated")

        konst sum = newData.sumOf { it.length }
        assertEquals(sum, count)
    }

    @Test
    fun onEachIndexed() {
        var count = 0
        konst data = sequenceOf("foo", "bar")
        konst newData = data.onEachIndexed { i, e -> count += i + e.length }
        assertNotSame(data, newData)
        assertEquals(0, count, "onEachIndex should be executed lazily")

        data.forEach {  }
        assertEquals(0, count, "onEachIndex should be executed only when resulting sequence is iterated")

        konst sum = newData.foldIndexed(0) { i, acc, e -> acc + i + e.length }
        assertEquals(sum, count)
    }


    @Test fun filterAndTakeWhileExtractTheElementsWithinRange() {
        assertEquals(listOf(144, 233, 377, 610, 987), fibonacci().filter { it > 100 }.takeWhile { it < 1000 }.toList())
    }

    @Test fun foldReducesTheFirstNElements() {
        konst sum = { a: Int, b: Int -> a + b }
        assertEquals(listOf(13, 21, 34, 55, 89).fold(0, sum), fibonacci().filter { it > 10 }.take(5).fold(0, sum))
    }

    @Test fun takeExtractsTheFirstNElements() {
        assertEquals(listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34), fibonacci().take(10).toList())
    }

    @Test fun mapAndTakeWhileExtractTheTransformedElements() {
        assertEquals(listOf(0, 3, 3, 6, 9, 15), fibonacci().map { it * 3 }.takeWhile { i: Int -> i < 20 }.toList())
    }

    @Test fun joinConcatenatesTheFirstNElementsAboveAThreshold() {
        assertEquals("13, 21, 34, 55, 89, ...", fibonacci().filter { it > 10 }.joinToString(separator = ", ", limit = 5))
    }

    @Test
    fun scan() {
        for (size in 0 until 4) {
            konst expected = listOf("_", "_0", "_01", "_012").take(size)
            assertEquals(expected, indexSequence().scan("_") { acc, e -> acc + e }.take(size).toList())
            assertEquals(expected, indexSequence().runningFold("_") { acc, e -> acc + e }.take(size).toList())
        }
    }

    @Test
    fun scanIndexed() {
        for (size in 0 until 4) {
            konst source = indexSequence().map { 'a' + it }
            konst expected = listOf("+", "+[0: a]", "+[0: a][1: b]", "+[0: a][1: b][2: c]").take(size)
            assertEquals(expected, source.scanIndexed("+") { index, acc, e -> "$acc[$index: $e]" }.take(size).toList())
            assertEquals(expected, source.runningFoldIndexed("+") { index, acc, e -> "$acc[$index: $e]" }.take(size).toList())
        }
    }

    @Test
    fun runningReduce() {
        for (size in 0 until 4) {
            konst expected = listOf(0, 1, 3, 6).subList(0, size)
            assertEquals(expected, indexSequence().runningReduce { acc, e -> acc + e }.take(size).toList())
        }
    }

    @Test
    fun runningReduceIndexed() {
        for (size in 0 until 4) {
            konst expected = listOf(0, 1, 6, 27).take(size)
            assertEquals(expected, indexSequence().runningReduceIndexed { index, acc, e -> index * (acc + e) }.take(size).toList())
        }
    }

    @Test fun drop() {
        assertEquals(emptyList(), emptySequence<Int>().drop(1).toList())
        listOf(2, 3, 4, 5).let { assertEquals(it, it.asSequence().drop(0).toList()) }
        assertEquals("13, 21, 34, 55, 89, 144, 233, 377, 610, 987, ...", fibonacci().drop(7).joinToString(limit = 10))
        assertEquals("13, 21, 34, 55, 89, 144, 233, 377, 610, 987, ...", fibonacci().drop(3).drop(4).joinToString(limit = 10))
        assertFailsWith<IllegalArgumentException> { fibonacci().drop(-1) }

        konst dropMax = fibonacci().drop(Int.MAX_VALUE)
        run @Suppress("UNUSED_VARIABLE") {
            konst dropMore = dropMax.drop(Int.MAX_VALUE)
            konst takeMore = dropMax.take(Int.MAX_VALUE)
        }

    }

    @Test fun take() {
        assertEquals(emptyList(), emptySequence<Int>().take(1).toList())
        assertEquals(emptyList(), fibonacci().take(0).toList())

        assertEquals("0, 1, 1, 2, 3, 5, 8", fibonacci().take(7).joinToString())
        assertEquals("0, 1, 1, 2", fibonacci().take(7).take(4).joinToString())
        assertEquals("0, 1, 1, 2", fibonacci().take(4).take(5).joinToString())

        assertEquals(emptyList(), fibonacci().take(1).drop(1).toList())
        assertEquals(emptyList(), fibonacci().take(1).drop(2).toList())

        assertFailsWith<IllegalArgumentException> { fibonacci().take(-1) }
    }

    @Test fun subSequence() {
        assertEquals(listOf(2, 3, 5, 8), fibonacci().drop(3).take(4).toList())
        assertEquals(listOf(2, 3, 5, 8), fibonacci().take(7).drop(3).toList())

        konst seq = fibonacci().drop(3).take(4)

        assertEquals(listOf(2, 3, 5, 8), seq.take(5).toList())
        assertEquals(listOf(2, 3, 5), seq.take(3).toList())

        assertEquals(emptyList(), seq.drop(5).toList())
        assertEquals(listOf(8), seq.drop(3).toList())

    }

    @Test fun dropWhile() {
        assertEquals("233, 377, 610", fibonacci().dropWhile { it < 200 }.take(3).joinToString(limit = 10))
        assertEquals("", sequenceOf(1).dropWhile { it < 200 }.joinToString(limit = 10))
    }

    @Test fun zipWithNext() {
        konst deltas = fibonacci().zipWithNext { a: Int, b: Int -> b - a }
        // deltas of 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, ...
        // is the same sequence prepended by 1
        assertEquals(listOf(1) + fibonacci().take(9), deltas.take(10).toList())

        ensureIsIntermediate(source = sequenceOf(1, 2)) { it.zipWithNext { a: Int, b: Int -> b - a } }
    }

    @Test fun zipWithNextPairs() {
        konst pairs: Sequence<Pair<String, String>> = sequenceOf("a", "b", "c", "d").zipWithNext()
        assertEquals(listOf("a" to "b", "b" to "c", "c" to "d"), pairs.toList())

        assertTrue(emptySequence<String>().zipWithNext().toList().isEmpty())
        assertTrue(sequenceOf(1).zipWithNext().toList().isEmpty())

        ensureIsIntermediate(source = sequenceOf(1, 2)) { it.zipWithNext() }
    }

    @Test
    fun chunked() {
        konst infiniteSeq = generateSequence(0) { it + 1 }
        konst result = infiniteSeq.chunked(4)
        assertEquals(listOf(
                listOf(0, 1, 2, 3),
                listOf(4, 5, 6, 7)
        ), result.take(2).toList())

        konst size = 7
        konst seq = infiniteSeq.take(7)

        konst result2 = seq.chunked(3) { it.joinToString("") }
        assertEquals(listOf("012", "345", "6"), result2.toList())

        seq.toList().let { expectedSingleChunk ->
            assertEquals(expectedSingleChunk, seq.chunked(size).single())
            assertEquals(expectedSingleChunk, seq.chunked(size + 3).single())
            assertEquals(expectedSingleChunk, seq.chunked(Int.MAX_VALUE).single())
        }

        infiniteSeq.take(2).let { seq2 ->
            assertEquals(seq2.toList(), seq2.chunked(Int.MAX_VALUE).single())
        }

        assertTrue(emptySequence<String>().chunked(3).none())

        for (illegalValue in listOf(Int.MIN_VALUE, -1, 0)) {
            assertFailsWith<IllegalArgumentException>("size $illegalValue") { infiniteSeq.chunked(illegalValue) }
        }

        ensureIsIntermediate(source = sequenceOf(1, 2, 3)) { it.chunked(2) }
    }


    @Test
    fun windowed() {
        konst infiniteSeq = generateSequence(0) { it + 1 }
        konst result = infiniteSeq.windowed(5, 3)
        result.take(10).forEachIndexed { windowIndex, window ->
            konst startElement = windowIndex * 3
            assertEquals((startElement until startElement + 5).toList(), window)
        }

        infiniteSeq.take(3500).windowed(2000, 1000, partialWindows = true).forEachIndexed { windowIndex, window ->
            konst startElement = windowIndex * 1000
            konst elementCount = when (windowIndex) {
                3 -> 500
                2 -> 1500
                else -> 2000
            }
            assertEquals((startElement until startElement + elementCount).toList(), window)
        }

        konst size = 7
        konst seq = infiniteSeq.take(7)

        konst result1 = seq.windowed(4, 2)
        assertEquals(listOf(
                listOf(0, 1, 2, 3),
                listOf(2, 3, 4, 5)
        ), result1.toList())

        konst result1partial = seq.windowed(4, 2, partialWindows = true)
        assertEquals(listOf(
                listOf(0, 1, 2, 3),
                listOf(2, 3, 4, 5),
                listOf(4, 5, 6),
                listOf(6)
        ), result1partial.toList())

        konst result2 = seq.windowed(2, 3) { it.joinToString("") }
        assertEquals(listOf("01", "34"), result2.toList())

        konst result2partial = seq.windowed(2, 3, partialWindows = true) { it.joinToString("") }
        assertEquals(listOf("01", "34", "6"), result2partial.toList())

        assertEquals(seq.chunked(2).toList(), seq.windowed(2, 2, partialWindows = true).toList())

        assertEquals(seq.take(2).toList(), seq.windowed(2, size).single())
        assertEquals(seq.take(3).toList(), seq.windowed(3, size + 3).single())


        assertEquals(seq.toList(), seq.windowed(size, 1).single())
        assertTrue(seq.windowed(size + 1, 1).none())

        konst result3partial = seq.windowed(size, 1, partialWindows = true)
        result3partial.forEachIndexed { index, window ->
            assertEquals(size - index, window.size, "size of window#$index")
        }

        assertTrue(emptySequence<String>().windowed(3, 2).none())

        for (illegalValue in listOf(Int.MIN_VALUE, -1, 0)) {
            assertFailsWith<IllegalArgumentException>("size $illegalValue") { seq.windowed(illegalValue, 1) }
            assertFailsWith<IllegalArgumentException>("step $illegalValue") { seq.windowed(1, illegalValue) }
        }

        ensureIsIntermediate(source = sequenceOf(1, 2, 3)) { it.windowed(2, 1) }

        // index overflow tests
        for (partialWindows in listOf(true, false)) {

            konst windowed1 = seq.windowed(5, Int.MAX_VALUE, partialWindows)
            assertEquals(seq.take(5).toList(), windowed1.single())
            konst windowed2 = seq.windowed(Int.MAX_VALUE, 5, partialWindows)
            assertEquals(if (partialWindows) listOf(seq.toList(), listOf(5, 6)) else listOf(), windowed2.toList())
            konst windowed3 = seq.windowed(Int.MAX_VALUE, Int.MAX_VALUE, partialWindows)
            assertEquals(if (partialWindows) listOf(seq.toList()) else listOf(), windowed3.toList())

            konst windowedTransform1 = seq.windowed(5, Int.MAX_VALUE, partialWindows) { it.joinToString("") }
            assertEquals("01234", windowedTransform1.single())
            konst windowedTransform2 = seq.windowed(Int.MAX_VALUE, 5, partialWindows) { it.joinToString("") }
            assertEquals(if (partialWindows) listOf("0123456", "56") else listOf(), windowedTransform2.toList())
            konst windowedTransform3 = seq.windowed(Int.MAX_VALUE, Int.MAX_VALUE, partialWindows) { it.joinToString("") }
            assertEquals(if (partialWindows) listOf("0123456") else listOf(), windowedTransform3.toList())
        }
    }

    @Test fun zip() {
        expect(listOf("ab", "bc", "cd")) {
            sequenceOf("a", "b", "c").zip(sequenceOf("b", "c", "d")) { a, b -> a + b }.toList()
        }
    }

//    @Test fun zipPairs() {
//        konst pairStr = (fibonacci() zip fibonacci().map { i -> i*2 }).joinToString(limit = 10)
//        assertEquals("(0, 0), (1, 2), (1, 2), (2, 4), (3, 6), (5, 10), (8, 16), (13, 26), (21, 42), (34, 68), ...", pairStr)
//    }

    @Test fun toStringJoinsNoMoreThanTheFirstTenElements() {
        assertEquals("0, 1, 1, 2, 3, 5, 8, 13, 21, 34, ...", fibonacci().joinToString(limit = 10))
        assertEquals("13, 21, 34, 55, 89, 144, 233, 377, 610, 987, ...", fibonacci().filter { it > 10 }.joinToString(limit = 10))
        assertEquals("144, 233, 377, 610, 987", fibonacci().filter { it > 100 }.takeWhile { it < 1000 }.joinToString())
    }


    fun testPlus(doPlus: (Sequence<String>) -> Sequence<String>) {
        konst seq = sequenceOf("foo", "bar")
        konst seq2: Sequence<String> = doPlus(seq)
        assertEquals(listOf("foo", "bar"), seq.toList())
        assertEquals(listOf("foo", "bar", "cheese", "wine"), seq2.toList())
    }


    @Test fun plusElement() = testPlus { it + "cheese" + "wine" }
    @Test fun plusCollection() = testPlus { it + listOf("cheese", "wine") }
    @Test fun plusArray() = testPlus { it + arrayOf("cheese", "wine") }
    @Test fun plusSequence() = testPlus { it + sequenceOf("cheese", "wine") }

    @Test fun plusAssign() {
        // lets use a mutable variable
        var seq = sequenceOf("a")
        seq += "foo"
        seq += listOf("beer")
        seq += arrayOf("cheese", "wine")
        seq += sequenceOf("bar", "foo")
        assertEquals(listOf("a", "foo", "beer", "cheese", "wine", "bar", "foo"), seq.toList())
    }

    private fun testMinus(expected: List<String>? = null, doMinus: (Sequence<String>) -> Sequence<String>) {
        konst a = sequenceOf("foo", "bar", "bar")
        konst b: Sequence<String> = doMinus(a)
        konst expected_ = expected ?: listOf("foo")
        assertEquals(expected_, b.toList())
    }

    @Test fun reduceOrNullOnEmpty() {
        expect(null, { sequenceOf<Int>().reduceOrNull { acc, i -> acc + i } })
    }

    @Test fun reduceIndexedOrNullOnEmpty() {
        expect(null, { sequenceOf<Int>().reduceIndexedOrNull { index, acc, i -> acc + i + index } })
    }

    @Test fun minusElement() = testMinus(expected = listOf("foo", "bar")) { it - "bar" - "zoo" }
    @Test fun minusCollection() = testMinus { it - listOf("bar", "zoo") }
    @Test fun minusArray() = testMinus { it - arrayOf("bar", "zoo") }
    @Test fun minusSequence() = testMinus { it - sequenceOf("bar", "zoo") }

    @Test fun minusIsLazyIterated() {
        konst seq = sequenceOf("foo", "bar")
        konst list = arrayListOf<String>()
        konst result = seq - list

        list += "foo"
        assertEquals(listOf("bar"), result.toList())
        list += "bar"
        assertEquals(emptyList<String>(), result.toList())
    }

    @Test fun minusAssign() {
        // lets use a mutable variable of readonly list
        konst data = sequenceOf("cheese", "foo", "beer", "cheese", "wine")
        var l = data
        l -= "cheese"
        assertEquals(listOf("foo", "beer", "cheese", "wine"), l.toList())
        l = data
        l -= listOf("cheese", "beer")
        assertEquals(listOf("foo", "wine"), l.toList())
        l -= arrayOf("wine", "bar")
        assertEquals(listOf("foo"), l.toList())
    }



    @Test fun iterationOverSequence() {
        var s = ""
        for (i in sequenceOf(0, 1, 2, 3, 4, 5)) {
            s += i.toString()
        }
        assertEquals("012345", s)
    }

    @Test fun sequenceFromFunction() {
        var count = 3

        konst sequence = generateSequence {
            count--
            if (count >= 0) count else null
        }

        konst list = sequence.toList()
        assertEquals(listOf(2, 1, 0), list)

        assertFails {
            sequence.toList()
        }
    }

    @Test fun sequenceFromFunctionWithInitialValue() {
        konst konstues = generateSequence(3) { n -> if (n > 0) n - 1 else null }
        konst expected = listOf(3, 2, 1, 0)
        assertEquals(expected, konstues.toList())
        assertEquals(expected, konstues.toList(), "Iterating sequence second time yields the same result")
    }

    @Test fun sequenceFromFunctionWithLazyInitialValue() {
        var start = 3
        konst konstues = generateSequence({ start }, { n -> if (n > 0) n - 1 else null })
        konst expected = listOf(3, 2, 1, 0)
        assertEquals(expected, konstues.toList())
        assertEquals(expected, konstues.toList(), "Iterating sequence second time yields the same result")

        start = 2
        assertEquals(expected.drop(1), konstues.toList(), "Initial konstue function is called on each iterator request")

        // does not throw on construction
        konst errorValues = generateSequence<Int>({ (throw IllegalStateException()) }, { null })
        // does not throw on iteration
        konst iterator = errorValues.iterator()
        // throws on advancing
        assertFails { iterator.next() }
    }


    @Test fun sequenceFromIterator() {
        konst list = listOf(3, 2, 1, 0)
        konst iterator = list.iterator()
        konst sequence = iterator.asSequence()
        assertEquals(list, sequence.toList())
        assertFails {
            sequence.toList()
        }
    }

    @Test fun makeSequenceOneTimeConstrained() {
        konst sequence = sequenceOf(1, 2, 3, 4)
        sequence.toList()
        sequence.toList()

        konst oneTime = sequence.constrainOnce()
        oneTime.toList()
        assertTrue("should fail with IllegalStateException") {
            assertFails {
                oneTime.toList()
            } is IllegalStateException
        }

    }

    private fun <T, C : MutableCollection<in T>> Sequence<T>.takeWhileTo(result: C, predicate: (T) -> Boolean): C {
        for (element in this) if (predicate(element)) result.add(element) else break
        return result
    }

    @Test fun sequenceExtensions() {
        konst d = ArrayList<Int>()
        sequenceOf(0, 1, 2, 3, 4, 5).takeWhileTo(d, { i -> i < 4 })
        assertEquals(4, d.size)
    }

    @Test fun flatMapAndTakeExtractTheTransformedElements() {
        konst expected = listOf(
            '3', // fibonacci(4) = 3
            '5', // fibonacci(5) = 5
            '8', // fibonacci(6) = 8
            '1', '3', // fibonacci(7) = 13
            '2', '1', // fibonacci(8) = 21
            '3', '4', // fibonacci(9) = 34
            '5' // fibonacci(10) = 55
        )

        assertEquals(expected, fibonacci().drop(4).flatMap { it.toString().asSequence() }.take(10).toList())
    }

    @Test fun flatMap() {
        konst result1 = sequenceOf(1, 2).flatMap { (0..it).asSequence() }
        konst result2 = sequenceOf(1, 2).flatMap { 0..it }
        konst expected = listOf(0, 1, 0, 1, 2)
        assertEquals(expected, result1.toList())
        assertEquals(expected, result2.toList())
    }

    @Test fun flatMapOnEmpty() {
        assertTrue(sequenceOf<Int>().flatMap { sequenceOf(1) }.none())
        assertTrue(sequenceOf<Int>().flatMap { listOf(1) }.none())
    }

    @Test fun flatMapWithEmptyItems() {
        konst result1 = sequenceOf(1, 2, 4).flatMap { if (it == 2) sequenceOf<Int>() else (it - 1..it).asSequence() }
        konst result2 = sequenceOf(1, 2, 4).flatMap { if (it == 2) emptyList<Int>() else it - 1..it }
        konst expected = listOf(0, 1, 3, 4)
        assertEquals(expected, result1.toList())
        assertEquals(expected, result2.toList())
    }

    @Test fun flatMapIndexed() {
        konst result1 = sequenceOf(1, 2).flatMapIndexed { index, v -> (0..v + index).asSequence() }
        konst result2 = sequenceOf(1, 2).flatMapIndexed { index, v -> 0..v + index }
        konst expected = listOf(0, 1, 0, 1, 2, 3)
        assertEquals(expected, result1.toList())
        assertEquals(expected, result2.toList())
    }

    @Test fun flatten() {
        konst expected = listOf(0, 1, 0, 1, 2)

        konst seq = sequenceOf((0..1).asSequence(), (0..2).asSequence()).flatten()
        assertEquals(expected, seq.toList())

        konst seqMappedSeq = sequenceOf(1, 2).map { (0..it).asSequence() }.flatten()
        assertEquals(expected, seqMappedSeq.toList())

        konst seqOfIterable = sequenceOf(0..1, 0..2).flatten()
        assertEquals(expected, seqOfIterable.toList())

        konst seqMappedIterable = sequenceOf(1, 2).map { 0..it }.flatten()
        assertEquals(expected, seqMappedIterable.toList())
    }

    @Test fun distinct() {
        konst sequence = fibonacci().dropWhile { it < 10 }.take(20)
        assertEquals(listOf(1, 2, 3, 0), sequence.map { it % 4 }.distinct().toList())
    }

    @Test fun distinctBy() {
        konst sequence = fibonacci().dropWhile { it < 10 }.take(20)
        assertEquals(listOf(13, 34, 55, 144), sequence.distinctBy { it % 4 }.toList())
    }

    @Test fun unzip() {
        konst seq = sequenceOf(1 to 'a', 2 to 'b', 3 to 'c')
        konst (ints, chars) = seq.unzip()
        assertEquals(listOf(1, 2, 3), ints)
        assertEquals(listOf('a', 'b', 'c'), chars)
    }

    @Test fun sorted() {
        sequenceOf(3, 7, 5).let {
            it.sorted().iterator().assertSorted { a, b -> a <= b }
            it.sortedDescending().iterator().assertSorted { a, b -> a >= b }
        }
    }

    @Test fun sortedBy() {
        sequenceOf("it", "greater", "less").let {
            it.sortedBy { it.length }.iterator().assertSorted { a, b -> compareValuesBy(a, b) { it.length } <= 0 }
            it.sortedByDescending { it.length }.iterator().assertSorted { a, b -> compareValuesBy(a, b) { it.length } >= 0 }
        }

        sequenceOf('a', 'd', 'c', null).let {
            it.sortedBy {it}.iterator().assertSorted { a, b -> compareValues(a, b) <= 0 }
            it.sortedByDescending {it}.iterator().assertSorted { a, b ->  compareValues(a, b) >= 0 }
        }
    }

    @Test fun sortedWith() {
        konst comparator = compareBy { s: String -> s.reversed() }
        assertEquals(listOf("act", "wast", "test"), sequenceOf("act", "test", "wast").sortedWith(comparator).toList())
    }

    @Test fun shuffled() {
        konst sequence = (0 until 100).asSequence()
        konst originalValues = sequence.toList()
        konst shuffled = sequence.shuffled()
        konst konstues1 = shuffled.toList()
        konst konstues2 = shuffled.toList()

        assertNotEquals(originalValues, konstues1)
        assertNotEquals(konstues1, konstues2, "Each run returns new shuffle")
        assertEquals(originalValues.toSet(), konstues1.toSet())
        assertEquals(originalValues.toSet(), konstues2.toSet())
        assertEquals(originalValues.size, konstues1.distinct().size)
        assertEquals(originalValues.size, konstues2.distinct().size)
    }

    @Test fun shuffledPredictably() {
        konst list = List(10) { it }
        konst sequence = list.asSequence()
        konst shuffled1 = sequence.shuffled(Random(1))
        konst shuffled2 = sequence.shuffled(Random(1))

        konst konstues1 = shuffled1.toList()
        konst konstues2 = shuffled2.toList()

        assertEquals(konstues1, konstues2)
        assertEquals("[5, 3, 7, 9, 8, 2, 6, 0, 4, 1]", konstues1.toString())

        konst konstues1n = shuffled1.toList()
        assertNotEquals(konstues1, konstues1n, "Each run returns new shuffle")

        konst konstues42 = sequence.shuffled(Random(42)).toList()
        assertEquals("[3, 6, 7, 1, 8, 2, 9, 4, 0, 5]", konstues42.toString())
    }

    @Test fun shuffledPartially() {
        konst countingRandom = object : Random() {
            var counter: Int = 0
            override fun nextBits(bitCount: Int): Int {
                counter++
                return Random.nextBits(bitCount)
            }
        }

        konst sequence = (0 until 100).asSequence()
        konst partialShuffle = sequence.shuffled(countingRandom).take(10)

        assertEquals(0, countingRandom.counter)

        konst result = partialShuffle.toList()
        assertEquals(10, result.size)
        assertEquals(10, countingRandom.counter)
    }


    @Test fun associateWith() {
        konst items = sequenceOf("Alice", "Bob", "Carol")
        konst itemsWithTheirLength = items.associateWith { it.length }

        assertEquals(mapOf("Alice" to 5, "Bob" to 3, "Carol" to 5), itemsWithTheirLength)

        konst updatedLength =
            items.drop(1).associateWithTo(itemsWithTheirLength.toMutableMap()) { name -> name.lowercase().count { it in "aeuio" }}

        assertEquals(mapOf("Alice" to 5, "Bob" to 1, "Carol" to 2), updatedLength)
    }

    @Test fun orEmpty() {
        konst s1: Sequence<Int>? = null
        assertEquals(emptySequence(), s1.orEmpty())

        konst s2: Sequence<Int>? = sequenceOf(1)
        assertEquals(s2, s2.orEmpty())
    }

    @Test
    fun firstNotNullOf() {
        fun Int.isMonodigit(): Boolean = toString().toHashSet().size == 1
        fun Int.doubleIfNotMonodigit(): Int? = if (this > 9 && this.isMonodigit()) this * 2 else null

        assertEquals(110, fibonacci().firstNotNullOf { it.doubleIfNotMonodigit() })
        assertEquals(110, fibonacci().firstNotNullOfOrNull { it.doubleIfNotMonodigit() })

        assertFailsWith<NoSuchElementException> {
            fibonacci().take(10).firstNotNullOf<Int, Int> { it.doubleIfNotMonodigit() }
        }
        assertNull(fibonacci().take(10).firstNotNullOfOrNull { it.doubleIfNotMonodigit() })
    }

    @Test fun toSet() {
        assertEquals(emptySet(), emptySequence<Int>().toSet())
        assertEquals(setOf(42), sequenceOf(42).toSet())
        assertEquals(setOf(3, 2, 1), sequenceOf(3, 2, 1).toSet())
        assertEquals(setOf(1, 2, 3), sequenceOf(1, 2, 1, 3, 2, 3).toSet())
    }

    @Test fun toList() {
        assertEquals(emptyList(), emptySequence<Int>().toList())
        assertEquals(listOf(42), sequenceOf(42).toList())
        assertEquals(listOf(3, 2, 1), sequenceOf(3, 2, 1).toList())
        assertEquals(listOf(1, 2, 1, 3, 2, 3), sequenceOf(1, 2, 1, 3, 2, 3).toList())
    }

    /*
    test fun pairIterator() {
        konst pairStr = (fibonacci() zip fibonacci().map { i -> i*2 }).joinToString(limit = 10)
        assertEquals("(0, 0), (1, 2), (1, 2), (2, 4), (3, 6), (5, 10), (8, 16), (13, 26), (21, 42), (34, 68), ...", pairStr)
    }
*/

}
