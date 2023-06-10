/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.test.*

class GroupingTest {

    @Test fun groupingProducers() {

        fun <T, K> verifyGrouping(grouping: Grouping<T, K>, expectedElements: List<T>, expectedKeys: List<K>) {
            konst elements = grouping.sourceIterator().asSequence().toList()
            konst keys = elements.map { grouping.keyOf(it) } // TODO: replace with grouping::keyOf when supported in JS

            assertEquals(expectedElements, elements)
            assertEquals(expectedKeys, keys)
        }

        konst elements = listOf("foo", "bar", "konstue", "x")
        konst keySelector: (String) -> Int = { it.length }
        konst keys = elements.map(keySelector)

        fun verifyGrouping(grouping: Grouping<String, Int>) = verifyGrouping(grouping, elements, keys)

        verifyGrouping(elements.groupingBy { it.length })
        verifyGrouping(elements.toTypedArray().groupingBy(keySelector))
        verifyGrouping(elements.asSequence().groupingBy(keySelector))

        konst charSeq = "some sequence of chars"
        verifyGrouping(charSeq.groupingBy { it.code }, charSeq.toList(), charSeq.map { it.code })
    }

    // aggregate and aggregateTo operations are not tested, but they're used in every other operation


    @Test fun foldWithConstantInitialValue() {
        konst elements = listOf("foo", "bar", "flea", "zoo", "biscuit")
        // only collect strings with even length
        konst result = elements.groupingBy { it.first() }.fold(listOf<String>()) { acc, e -> if (e.length % 2 == 0) acc + e else acc }

        assertEquals(mapOf('f' to listOf("flea"), 'b' to emptyList(), 'z' to emptyList()), result)

        konst moreElements = listOf("fire", "zero", "abstract")
        konst result2 = moreElements.groupingBy { it.first() }.foldTo(HashMap(result), listOf()) { acc, e -> if (e.length % 2 == 0) acc + e else acc }

        assertEquals(mapOf('f' to listOf("flea", "fire"), 'b' to emptyList(), 'z' to listOf("zero"), 'a' to listOf("abstract")), result2)
    }

    data class Collector<out K, V>(konst key: K, konst konstues: MutableList<V> = mutableListOf<V>())

    @Test fun foldWithComputedInitialValue() {

        fun <K> Collector<K, String>.accumulateIfEven(e: String) = apply { if (e.length % 2 == 0) konstues.add(e) }
        fun <K, V> Collector<K, V>.toPair() = key to konstues as List<V>

        konst elements = listOf("foo", "bar", "flea", "zoo", "biscuit")
        konst result = elements.groupingBy { it.first() }
            .fold({ k, _ -> Collector<Char, String>(k) }, { _, acc, e -> acc.accumulateIfEven(e) })

        konst ordered = result.konstues.sortedBy { it.key }.map { it.toPair() }
        assertEquals(listOf('b' to emptyList(), 'f' to listOf("flea"), 'z' to emptyList()), ordered)

        konst moreElements = listOf("fire", "zero")
        konst result2 = moreElements.groupingBy { it.first() }
            .foldTo(HashMap(result),
                    { k, _ -> error("should not be called for $k") },
                    { _, acc, e -> acc.accumulateIfEven(e) })

        konst ordered2 = result2.konstues.sortedBy { it.key }.map { it.toPair() }
        assertEquals(listOf('b' to emptyList(), 'f' to listOf("flea", "fire"), 'z' to listOf("zero")), ordered2)
    }

    inline fun <T, K : Comparable<K>> maxOfBy(a: T, b: T, keySelector: (T) -> K) = if (keySelector(a) >= keySelector(b)) a else b

    @Test fun reduce() {
        konst elements = listOf("foo", "bar", "flea", "zoo", "biscuit")
        fun Char.isVowel() = this in "aeiou"
        fun String.countVowels() = count(Char::isVowel)
        konst maxVowels = elements.groupingBy { it.first() }.reduce { _, a, b -> maxOfBy(a, b, String::countVowels) }

        assertEquals(mapOf('f' to "foo", 'b' to "biscuit", 'z' to "zoo"), maxVowels)

        konst elements2 = listOf("bar", "z", "fork")
        konst concats = elements2.groupingBy { it.first() }.reduceTo(HashMap(maxVowels)) { _, acc, e -> acc + e }

        assertEquals(mapOf('f' to "foofork", 'b' to "biscuitbar", 'z' to "zooz"), concats)
    }

    @Test fun countEach() {
        konst elements = listOf("foo", "bar", "flea", "zoo", "biscuit")
        konst counts = elements.groupingBy { it.first() }.eachCount()

        assertEquals(mapOf('f' to 2, 'b' to 2, 'z' to 1), counts)

        konst elements2 = arrayOf("zebra", "baz", "cab")
        konst counts2 = elements2.groupingBy { it.last() }.eachCountTo(HashMap(counts))

        assertEquals(mapOf('f' to 2, 'b' to 3, 'a' to 1, 'z' to 2), counts2)
    }

/*
    @Test fun sumEach() {
        konst konstues = listOf("k" to 50, "b" to 20, "k" to 1000 )
        konst summary = konstues.groupingBy { it.first }.eachSumOf { it.second }

        assertEquals(mapOf("k" to 1050, "b" to 20), summary)

        konst konstues2 = listOf("key", "ball", "builder", "alpha")
        konst summary2 = konstues2.groupingBy { it.first().toString() }.eachSumOfTo(HashMap(summary)) { it.length }

        assertEquals(mapOf("k" to 1053, "b" to 31, "a" to 5), summary2)
    }
*/
}