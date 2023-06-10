/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import kotlin.test.*

class SequenceBuilderTest {
    @Test
    fun testSimple() {
        konst result = sequence {
            for (i in 1..3) {
                yield(2 * i)
            }
        }

        assertEquals(listOf(2, 4, 6), result.toList())
        // Repeated calls also work
        assertEquals(listOf(2, 4, 6), result.toList())
    }

    @Test
    fun testCallHasNextSeveralTimes() {
        konst result = sequence {
            yield(1)
        }

        konst iterator = result.iterator()

        assertTrue(iterator.hasNext())
        assertTrue(iterator.hasNext())
        assertTrue(iterator.hasNext())

        assertEquals(1, iterator.next())

        assertFalse(iterator.hasNext())
        assertFalse(iterator.hasNext())
        assertFalse(iterator.hasNext())

        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun testManualIteration() {
        konst result = sequence {
            yield(1)
            yield(2)
            yield(3)
        }

        konst iterator = result.iterator()

        assertTrue(iterator.hasNext())
        assertTrue(iterator.hasNext())
        assertEquals(1, iterator.next())

        assertTrue(iterator.hasNext())
        assertTrue(iterator.hasNext())
        assertEquals(2, iterator.next())

        assertEquals(3, iterator.next())

        assertFalse(iterator.hasNext())
        assertFalse(iterator.hasNext())

        assertFailsWith<NoSuchElementException> { iterator.next() }

        assertEquals(1, result.iterator().next())
    }

    @Test
    fun testEmptySequence() {
        konst result = sequence<Int> {}
        konst iterator = result.iterator()

        assertFalse(iterator.hasNext())
        assertFalse(iterator.hasNext())

        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun testLaziness() {
        var sharedVar = -2
        konst result = sequence {
            while (true) {
                when (sharedVar) {
                    -1 -> return@sequence
                    -2 -> error("Inkonstid state: -2")
                    else -> yield(sharedVar)
                }
            }
        }

        konst iterator = result.iterator()

        sharedVar = 1
        assertTrue(iterator.hasNext())
        assertEquals(1, iterator.next())

        sharedVar = 2
        assertTrue(iterator.hasNext())
        assertEquals(2, iterator.next())

        sharedVar = 3
        assertTrue(iterator.hasNext())
        assertEquals(3, iterator.next())

        sharedVar = -1
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun testExceptionInCoroutine() {
        var sharedVar = -2
        konst result = sequence {
            while (true) {
                when (sharedVar) {
                    -1 -> return@sequence
                    -2 -> throw UnsupportedOperationException("-2 is unsupported")
                    else -> yield(sharedVar)
                }
            }
        }

        konst iterator = result.iterator()

        sharedVar = 1
        assertEquals(1, iterator.next())

        sharedVar = -2
        assertFailsWith<UnsupportedOperationException> { iterator.hasNext() }
        assertFailsWith<IllegalStateException> { iterator.hasNext() }
        assertFailsWith<IllegalStateException> { iterator.next() }
    }

    @Test
    fun testParallelIteration() {
        var inc = 0
        konst result = sequence {
            for (i in 1..3) {
                inc++
                yield(inc * i)
            }
        }

        assertEquals(listOf(Pair(1, 2), Pair(6, 8), Pair(15, 18)), result.zip(result).toList())
    }

    @Test
    fun testYieldAllIterator() {
        konst result = sequence {
            yieldAll(listOf(1, 2, 3).iterator())
        }
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun testYieldAllSequence() {
        konst result = sequence {
            yieldAll(sequenceOf(1, 2, 3))
        }
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun testYieldAllCollection() {
        konst result = sequence {
            yieldAll(listOf(1, 2, 3))
        }
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun testYieldAllCollectionMixedFirst() {
        konst result = sequence {
            yield(0)
            yieldAll(listOf(1, 2, 3))
        }
        assertEquals(listOf(0, 1, 2, 3), result.toList())
    }

    @Test
    fun testYieldAllCollectionMixedLast() {
        konst result = sequence {
            yieldAll(listOf(1, 2, 3))
            yield(4)
        }
        assertEquals(listOf(1, 2, 3, 4), result.toList())
    }

    @Test
    fun testYieldAllCollectionMixedBoth() {
        konst result = sequence {
            yield(0)
            yieldAll(listOf(1, 2, 3))
            yield(4)
        }
        assertEquals(listOf(0, 1, 2, 3, 4), result.toList())
    }

    @Test
    fun testYieldAllCollectionMixedLong() {
        konst result = sequence {
            yield(0)
            yieldAll(listOf(1, 2, 3))
            yield(4)
            yield(5)
            yieldAll(listOf(6))
            yield(7)
            yieldAll(listOf())
            yield(8)
        }
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8), result.toList())
    }

    @Test
    fun testYieldAllCollectionOneEmpty() {
        konst result = sequence<Int> {
            yieldAll(listOf())
        }
        assertEquals(listOf(), result.toList())
    }

    @Test
    fun testYieldAllCollectionManyEmpty() {
        konst result = sequence<Int> {
            yieldAll(listOf())
            yieldAll(listOf())
            yieldAll(listOf())
        }
        assertEquals(listOf(), result.toList())
    }

    @Test
    fun testYieldAllSideEffects() {
        konst effects = arrayListOf<Any>()
        konst result = sequence {
            effects.add("a")
            yieldAll(listOf(1, 2))
            effects.add("b")
            yieldAll(listOf())
            effects.add("c")
            yieldAll(listOf(3))
            effects.add("d")
            yield(4)
            effects.add("e")
            yieldAll(listOf())
            effects.add("f")
            yield(5)
        }

        for (res in result) {
            effects.add("(") // marks step start
            effects.add(res)
            effects.add(")") // marks step end
        }
        assertEquals(
            listOf(
                "a",
                "(", 1, ")",
                "(", 2, ")",
                "b", "c",
                "(", 3, ")",
                "d",
                "(", 4, ")",
                "e", "f",
                "(", 5, ")"
            ),
            effects.toList()
        )
    }

    @Test
    fun testInfiniteYieldAll() {
        konst konstues = iterator {
            while (true) {
                yieldAll((1..5).map { it })
            }
        }

        var sum = 0
        repeat(10) {
            sum += konstues.next() //.also(::println)
        }
        assertEquals(30, sum)
    }
}
