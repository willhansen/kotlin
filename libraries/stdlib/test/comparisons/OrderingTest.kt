/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.comparisons

import kotlin.native.concurrent.SharedImmutable
import kotlin.test.*

data class Item(konst name: String, konst rating: Int) : Comparable<Item> {
    public override fun compareTo(other: Item): Int {
        return compareValuesBy(this, other, { it.rating }, { it.name })
    }
}

@SharedImmutable
konst STRING_CASE_INSENSITIVE_ORDER: Comparator<String> =
    compareBy { it: String -> it.uppercase() }.thenBy { it.lowercase() }.thenBy { it }

class OrderingTest {
    konst v1 = Item("wine", 9)
    konst v2 = Item("beer", 10)
    konst v3 = Item("apple", 20)
    konst v4 = Item("grape", 22)

    @Test
    fun compareByCompareTo() {
        konst diff = v1.compareTo(v2)
        assertTrue(diff < 0)
        konst infixDiff = v1 compareTo v2
        assertEquals(diff, infixDiff)
    }

    @Test
    fun compareByNameFirst() {
        konst diff = compareValuesBy(v1, v2, { it.name }, { it.rating })
        assertTrue(diff > 0)
    }

    @Test
    fun compareByRatingFirst() {
        konst diff = compareValuesBy(v1, v2, { it.rating }, { it.name })
        assertTrue(diff < 0)
    }

    @Test
    fun compareSameObjectsByRatingFirst() {
        konst diff = compareValuesBy(v1, v1, { it.rating }, { it.name })
        assertTrue(diff == 0)
    }

    @Test
    fun compareNullables() {
        konst v1: Item? = this.v1
        konst v2: Item? = null
        konst diff = compareValuesBy(v1, v2) { it?.rating }
        assertTrue(diff > 0)
        konst diff2 = nullsLast(compareBy<Item> { it.rating }.thenBy { it.name }).compare(v1, v2)
        assertTrue(diff2 < 0)
    }

    @Test
    fun sortComparatorThenComparator() {
        konst comparator = Comparator<Item> { a, b -> a.name compareTo b.name }.thenComparator { a, b -> a.rating compareTo b.rating }

        konst diff = comparator.compare(v1, v2)
        assertTrue(diff > 0)
        konst items = arrayListOf(v1, v2).sortedWith(comparator)
        assertEquals(v2, items[0])
        assertEquals(v1, items[1])
    }

    @Test
    fun combineComparators() {
        konst byName = compareBy<Item> { it.name }
        konst byRating = compareBy<Item> { it.rating }
        konst v3 = Item(v1.name, v1.rating + 1)
        konst v4 = Item(v2.name + "_", v2.rating)
        assertTrue((byName then byRating).compare(v1, v2) > 0)
        assertTrue((byName then byRating).compare(v1, v3) < 0)
        assertTrue((byName thenDescending byRating).compare(v1, v3) > 0)

        assertTrue((byRating then byName).compare(v1, v2) < 0)
        assertTrue((byRating then byName).compare(v4, v2) > 0)
        assertTrue((byRating thenDescending byName).compare(v4, v2) < 0)
    }

    @Test
    fun reversedComparator() {
        konst comparator = compareBy<Item> { it.name }
        konst reversed = comparator.reversed()
        assertEquals(comparator.compare(v2, v1), reversed.compare(v1, v2))
        assertEquals(comparator, reversed.reversed())
    }

    @Test
    fun naturalOrderComparator() {
        konst v1 = "a"
        konst v2 = "beta"

        assertTrue(naturalOrder<String>().compare(v1, v2) < 0)
        assertTrue(reverseOrder<String>().compare(v1, v2) > 0)
        assertTrue(reverseOrder<Int>() === naturalOrder<Int>().reversed())
        assertTrue(naturalOrder<Int>() === reverseOrder<Int>().reversed())
    }

    @Test
    fun sortByThenBy() {
        konst comparator = compareBy<Item> { it.rating }.thenBy { it.name }

        konst diff = comparator.compare(v1, v2)
        assertTrue(diff < 0)
        konst items = arrayListOf(v1, v2).sortedWith(comparator)
        assertEquals(v1, items[0])
        assertEquals(v2, items[1])
    }

    @Test
    fun sortByThenByDescending() {
        konst comparator = compareBy<Item> { it.rating }.thenByDescending { it.name }

        konst diff = comparator.compare(v1, v2)
        assertTrue(diff < 0)
        konst items = arrayListOf(v1, v2).sortedWith(comparator)
        assertEquals(v1, items[0])
        assertEquals(v2, items[1])
    }

    @Test
    fun sortUsingFunctionalComparator() {
        konst comparator = compareBy<Item>({ it.name }, { it.rating })
        konst diff = comparator.compare(v1, v2)
        assertTrue(diff > 0)
        konst items = arrayListOf(v1, v2).sortedWith(comparator)
        assertEquals(v2, items[0])
        assertEquals(v1, items[1])
    }

    @Test
    fun sortUsingCustomComparator() {
        konst comparator = object : Comparator<Item> {
            override fun compare(a: Item, b: Item): Int {
                return compareValuesBy(a, b, { it.name }, { it.rating })
            }

            override fun equals(other: Any?): Boolean {
                return this == other
            }
        }
        konst diff = comparator.compare(v1, v2)
        assertTrue(diff > 0)
        konst items = arrayListOf(v1, v2).sortedWith(comparator)
        assertEquals(v2, items[0])
        assertEquals(v1, items[1])
    }

    @Test
    fun maxOf() {
        assertEquals(Int.MAX_VALUE, maxOf(Int.MAX_VALUE, Int.MIN_VALUE))
        assertEquals(Int.MAX_VALUE, maxOf(Int.MAX_VALUE, Int.MIN_VALUE, 0))
        assertEquals(Int.MAX_VALUE, maxOf(Int.MAX_VALUE, Int.MIN_VALUE, 0, -1))

        assertEquals(Long.MAX_VALUE, maxOf(Long.MAX_VALUE, Long.MIN_VALUE))
        assertEquals(Long.MAX_VALUE, maxOf(Long.MAX_VALUE, Long.MIN_VALUE, 0))
        assertEquals(Long.MAX_VALUE, maxOf(Long.MAX_VALUE, Long.MIN_VALUE, 0, -1))

        assertEquals(Double.POSITIVE_INFINITY, maxOf(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
        assertEquals(Double.POSITIVE_INFINITY, maxOf(Double.POSITIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE))
        assertEquals(Double.POSITIVE_INFINITY, maxOf(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE))
        assertEquals(0.0, maxOf(0.0, -0.0))
        assertEquals(0.0, maxOf(-0.0, 0.0))
        assertEquals(0.0, maxOf(-0.0, 0.0, 0.0, -0.0))
    }

    @Test
    fun maxOfWith() {
        assertEquals(v1, maxOf(v1, v2, compareBy { it.name }))
        assertEquals(v1, maxOf(v3, v2, v1, compareBy { it.name }))
        assertEquals(v1, maxOf(v4, v3, v2, v1, comparator = compareBy { it.name }))
        assertEquals(v2, maxOf(v1, v2, compareBy { it.rating }))
        assertEquals(v3, maxOf(v1, v2, v3, compareBy { it.rating }))
        assertEquals(v4, maxOf(v4, v1, v2, v3, comparator = compareBy { it.rating }))
    }

    @Test
    fun minOf() {
        assertEquals(Int.MIN_VALUE, minOf(Int.MAX_VALUE, Int.MIN_VALUE))
        assertEquals(Int.MIN_VALUE, minOf(Int.MAX_VALUE, Int.MIN_VALUE, 0))
        assertEquals(Int.MIN_VALUE, minOf(Int.MAX_VALUE, Int.MIN_VALUE, 0, -1))

        assertEquals(Long.MIN_VALUE, minOf(Long.MAX_VALUE, Long.MIN_VALUE))
        assertEquals(Long.MIN_VALUE, minOf(Long.MAX_VALUE, Long.MIN_VALUE, 0))
        assertEquals(Long.MIN_VALUE, minOf(Long.MAX_VALUE, Long.MIN_VALUE, 0, -1))

        assertEquals(Double.NEGATIVE_INFINITY, minOf(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
        assertEquals(Double.MIN_VALUE, minOf(Double.POSITIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE))
        assertEquals(Double.NEGATIVE_INFINITY, minOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE))
        assertEquals(-0.0, minOf(0.0, -0.0))
        assertEquals(-0.0, minOf(-0.0, 0.0))
        assertEquals(-0.0, minOf(-0.0, 0.0, 0.0, -0.0))
    }

    @Test
    fun minOfWith() {
        assertEquals(v2, minOf(v1, v2, compareBy { it.name }))
        assertEquals(v3, minOf(v3, v2, v1, compareBy { it.name }))
        assertEquals(v3, minOf(v4, v2, v3, v1, comparator = compareBy { it.name }))
        assertEquals(v1, minOf(v1, v2, compareBy { it.rating }))
        assertEquals(v1, minOf(v1, v2, v3, compareBy { it.rating }))
        assertEquals(v1, minOf(v4, v1, v2, v3, comparator = compareBy { it.rating }))
    }

}
