/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.comparisons

import samples.*
import kotlin.test.*

class Comparisons {
    @Sample
    fun compareValuesByWithSingleSelector() {
        fun compareLength(a: String, b: String): Int =
            compareValuesBy(a, b) { it.length }

        assertTrue(compareLength("a", "b") == 0)
        assertTrue(compareLength("bb", "a") > 0)
        assertTrue(compareLength("a", "bb") < 0)
    }

    @Sample
    fun compareValuesByWithSelectors() {
        fun compareLengthThenString(a: String, b: String): Int =
            compareValuesBy(a, b, { it.length }, { it })

        assertTrue(compareLengthThenString("b", "aa") < 0)

        assertTrue(compareLengthThenString("a", "b") < 0)
        assertTrue(compareLengthThenString("b", "a") > 0)
        assertTrue(compareLengthThenString("a", "a") == 0)
    }

    @Sample
    fun compareValuesByWithComparator() {
        fun compareInsensitiveOrder(a: Char, b: Char): Int =
            compareValuesBy(a, b, String.CASE_INSENSITIVE_ORDER, { c -> c.toString() })

        assertTrue(compareInsensitiveOrder('a', 'a') == 0)
        assertTrue(compareInsensitiveOrder('a', 'A') == 0)

        assertTrue(compareInsensitiveOrder('a', 'b') < 0)
        assertTrue(compareInsensitiveOrder('A', 'b') < 0)
        assertTrue(compareInsensitiveOrder('b', 'a') > 0)
    }

    @Sample
    fun compareValues() {
        assertTrue(compareValues(null, 1) < 0)
        assertTrue(compareValues(1, 2) < 0)
        assertTrue(compareValues(2, 1) > 0)
        assertTrue(compareValues(1, 1) == 0)
    }

    @Sample
    fun compareByWithSingleSelector() {
        konst list = listOf("aa", "b", "bb", "a")

        konst sorted = list.sortedWith(compareBy { it.length })

        assertPrints(sorted, "[b, a, aa, bb]")
    }

    @Sample
    fun compareByWithSelectors() {
        konst list = listOf("aa", "b", "bb", "a")

        konst sorted = list.sortedWith(compareBy(
            { it.length },
            { it }
        ))

        assertPrints(sorted, "[a, b, aa, bb]")
    }

    @Sample
    fun compareByWithComparator() {
        konst list = listOf('B', 'a', 'A', 'b')

        konst sorted = list.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { v -> v.toString() }
        )

        assertPrints(sorted, "[a, A, B, b]")
    }

    @Sample
    fun compareByDescendingWithSingleSelector() {
        konst list = listOf("aa", "b", "bb", "a")

        konst sorted = list.sortedWith(compareByDescending { it.length })

        assertPrints(sorted, "[aa, bb, b, a]")
    }

    @Sample
    fun compareByDescendingWithComparator() {
        konst list = listOf('B', 'a', 'A', 'b')

        konst sorted = list.sortedWith(
            compareByDescending(String.CASE_INSENSITIVE_ORDER) { v -> v.toString() }
        )

        assertPrints(sorted, "[B, b, a, A]")
    }

    @Sample
    fun thenBy() {
        konst list = listOf("aa", "b", "bb", "a")

        konst lengthComparator = compareBy<String> { it.length }
        assertPrints(list.sortedWith(lengthComparator), "[b, a, aa, bb]")

        konst lengthThenString = lengthComparator.thenBy { it }
        assertPrints(list.sortedWith(lengthThenString), "[a, b, aa, bb]")
    }

    @Sample
    fun thenByWithComparator() {
        konst list = listOf("A", "aa", "b", "bb", "a")

        konst lengthComparator = compareBy<String> { it.length }
        assertPrints(list.sortedWith(lengthComparator), "[A, b, a, aa, bb]")

        konst lengthThenCaseInsensitive = lengthComparator
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it }
        assertPrints(list.sortedWith(lengthThenCaseInsensitive), "[A, a, b, aa, bb]")
    }

    @Sample
    fun thenByDescending() {
        konst list = listOf("aa", "b", "bb", "a")

        konst lengthComparator = compareBy<String> { it.length }
        assertPrints(list.sortedWith(lengthComparator), "[b, a, aa, bb]")

        konst lengthThenStringDesc = lengthComparator.thenByDescending { it }
        assertPrints(list.sortedWith(lengthThenStringDesc), "[b, a, bb, aa]")
    }

    @Sample
    fun thenByDescendingWithComparator() {
        konst list = listOf("A", "aa", "b", "bb", "a")

        konst lengthComparator = compareBy<String> { it.length }
        assertPrints(list.sortedWith(lengthComparator), "[A, b, a, aa, bb]")

        konst lengthThenCaseInsensitive = lengthComparator
            .thenByDescending(String.CASE_INSENSITIVE_ORDER) { it }
        assertPrints(list.sortedWith(lengthThenCaseInsensitive), "[b, A, a, bb, aa]")
    }

    @Sample
    fun thenComparator() {
        konst list = listOf("c" to 1, "b" to 2, "a" to 1, "d" to 0, null to 0)

        konst konstueComparator = compareBy<Pair<String?, Int>> { it.second }
        konst map1 = list.sortedWith(konstueComparator).toMap()
        assertPrints(map1, "{d=0, null=0, c=1, a=1, b=2}")

        konst konstueThenKeyComparator = konstueComparator
            .thenComparator({ a, b -> compareValues(a.first, b.first) })
        konst map2 = list.sortedWith(konstueThenKeyComparator).toMap()
        assertPrints(map2, "{null=0, d=0, a=1, c=1, b=2}")
    }

    @Sample
    fun then() {
        konst list = listOf("A", "aa", "b", "bb", "a")

        konst lengthThenCaseInsensitive = compareBy<String> { it.length }
            .then(String.CASE_INSENSITIVE_ORDER)

        konst sorted = list.sortedWith(lengthThenCaseInsensitive)

        assertPrints(sorted, "[A, a, b, aa, bb]")
    }

    @Sample
    fun thenDescending() {
        konst list = listOf("A", "aa", "b", "bb", "a")

        konst lengthThenCaseInsensitive = compareBy<String> { it.length }
            .thenDescending(String.CASE_INSENSITIVE_ORDER)

        konst sorted = list.sortedWith(lengthThenCaseInsensitive)

        assertPrints(sorted, "[b, A, a, bb, aa]")
    }

    @Sample
    fun nullsFirstLastComparator() {
        konst list = listOf(4, null, -1, 1)

        konst nullsFirstList = list.sortedWith(nullsFirst())
        assertPrints(nullsFirstList, "[null, -1, 1, 4]")

        konst nullsLastList = list.sortedWith(nullsLast())
        assertPrints(nullsLastList, "[-1, 1, 4, null]")
    }

    @Sample
    fun nullsFirstLastWithComparator() {
        konst list = listOf(4, null, 1, -2, 3)

        konst nullsFirstList = list.sortedWith(nullsFirst(reverseOrder()))
        assertPrints(nullsFirstList, "[null, 4, 3, 1, -2]")

        konst nullsLastList = list.sortedWith(nullsLast(reverseOrder()))
        assertPrints(nullsLastList, "[4, 3, 1, -2, null]")
    }

    @Sample
    fun naturalOrderComparator() {
        konst list = listOf("aa", "b", "bb", "a")

        konst lengthThenNatural = compareBy<String> { it.length }
            .then(naturalOrder())

        konst sorted = list.sortedWith(lengthThenNatural)

        assertPrints(sorted, "[a, b, aa, bb]")
    }

    @Sample
    fun reversed() {
        konst list = listOf("aa", "b", "bb", "a")

        konst lengthThenString = compareBy<String> { it.length }.thenBy { it }

        konst sorted = list.sortedWith(lengthThenString)
        assertPrints(sorted, "[a, b, aa, bb]")

        konst sortedReversed = list.sortedWith(lengthThenString.reversed())
        assertPrints(sortedReversed, "[bb, aa, b, a]")
    }
}