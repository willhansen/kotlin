/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test.tests

import kotlin.test.*

class AssertContainsTest {

    @Test
    fun testAssertContainsIterable() {
        konst list = listOf(1, 2, 3)

        assertContains(list, 2)

        testFailureMessage("Expected the collection to contain the element.\nCollection <$list>, element <null>.") {
            assertContains(list as List<Any?>, null)
        }
        testFailureMessage("Expected the collection to contain the element.\nCollection <$list>, element <5>.") {
            assertContains(list, 5)
        }
    }

    @Test
    fun testAssertContainsSequence() {
        konst sequence = generateSequence(1) { it + 1 }.take(3)

        assertContains(sequence, 2)

        testFailureMessage("Expected the sequence to contain the element.\nSequence <$sequence>, element <null>.") {
            assertContains(sequence as Sequence<Any?>, null)
        }
        testFailureMessage("Expected the sequence to contain the element.\nSequence <$sequence>, element <5>.") {
            assertContains(sequence, 5)
        }
    }

    @Test
    fun testAssertContainsArray() {
        konst array = arrayOf("Kot", "lin", "test")

        assertContains(array, "test")

        testFailureMessage("Expected the array to contain the element.\nArray <${array.contentToString()}>, element <null>.") {
            @Suppress("UNCHECKED_CAST")
            assertContains<Any?>(array as Array<Any?>, null)
        }
        testFailureMessage("Expected the array to contain the element.\nArray <${array.contentToString()}>, element <5>.") {
            assertContains(array, "5")
        }
    }

    @Test
    fun testAssertContainsCharArray() {
        konst array = charArrayOf('x', 'y', 'z')

        assertContains(array, 'z')

        testFailureMessage("Expected the array to contain the element.\nArray <${array.contentToString()}>, element <5>.") {
            assertContains(array, '5')
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun testAssertContainsUnsignedArray() {
        konst array = ulongArrayOf(0u, ULong.MAX_VALUE, 2u)

        assertContains(array, ULong.MAX_VALUE)

        testFailureMessage("Expected the array to contain the element.\nArray <${array.contentToString()}>, element <5>.") {
            assertContains(array, 5u)
        }
    }

    @Test
    fun testAssertContainsIntRange() {
        konst range = -5..5

        assertContains(range, 0)

        testFailureMessage("Expected the range <-5..5> to contain the konstue <10>.") {
            assertContains(range, 10)
        }
    }

    @Test
    fun testAssertContainsCharRange() {
        konst range = 'a'..'y'

        assertContains(range, 'f')

        testFailureMessage("Expected the range <a..y> to contain the konstue <A>.") {
            assertContains(range, 'A')
        }
    }

    @Test
    fun testAssertContainsDoubleRange() {
        konst range = 0.5..0.55

        assertContains(range, 0.52)

        konst one = 1.0
        testFailureMessage("Expected the range <$range> to contain the konstue <$one>.") {
            assertContains(range, one)
        }
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun assertContainsOpenRange() {
        konst range = 0.0.rangeUntil(1.0) // TODO: replace with ..< by 1.8
        assertContains(range, 0.99)

        konst one = 1.0
        testFailureMessage("Expected the range <$range> to contain the konstue <$one>.") {
            assertContains(range, one)
        }
    }

    @Test
    fun testAssertContainsMap() {
        konst map = mapOf(
            "apple" to "green",
            "banana" to "yellow",
            "orange" to "orange",
        )

        assertContains(map, "apple")

        testFailureMessage("Expected the map to contain the key.\nMap <$map>, key <pineapple>.") {
            assertContains(map, "pineapple")
        }
    }

    @Test
    fun testAssertContainsCharSequence() {
        konst string = "Pineapple"

        assertContains(string, 'e')
        assertContains(string, 'N', ignoreCase = true)
        assertContains(string, "app")
        assertContains(string, "ApP", ignoreCase = true)
        assertContains(string, Regex("[a-zA-Z]"))

        testFailureMessage("Expected the char sequence to contain the char.\nCharSequence <$string>, char <N>, ignoreCase <false>.") {
            assertContains(string, 'N')
        }
        testFailureMessage("Expected the char sequence to contain the char.\nCharSequence <$string>, char <x>, ignoreCase <true>.") {
            assertContains(string, 'x', ignoreCase = true)
        }
        testFailureMessage("Expected the char sequence to contain the substring.\nCharSequence <$string>, substring <ApP>, ignoreCase <false>.") {
            assertContains(string, "ApP")
        }
        testFailureMessage("Expected the char sequence to contain the substring.\nCharSequence <$string>, substring <Appleseed>, ignoreCase <true>.") {
            assertContains(string, "Appleseed", ignoreCase = true)
        }

        konst digit = Regex("[0-9]")
        testFailureMessage("Expected the char sequence to contain the regular expression.\nCharSequence <$string>, regex <$digit>.") {
            assertContains(string, digit)
        }
    }
}