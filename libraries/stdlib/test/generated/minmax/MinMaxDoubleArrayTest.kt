/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */


package test.generated.minmax

//
// NOTE: THIS FILE IS AUTO-GENERATED by the MinMaxTestGenerator.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

import kotlin.math.pow
import kotlin.test.*
import test.*

class MinMaxDoubleArrayTest {

    
    private fun expectMinMax(min: Double, max: Double, elements: DoubleArray) {
        assertEquals(min, elements.minOrNull())
        assertEquals(max, elements.maxOrNull())
        assertEquals(min, elements.min())
        assertEquals(max, elements.max())
    }

    @Test
    fun minMax() {
        expectMinMax(1.0, 1.0, doubleArrayOf(1.0))
        expectMinMax(1.0, 2.0, doubleArrayOf(1.0, 2.0))
        expectMinMax(1.0, Double.POSITIVE_INFINITY, doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY))
                    
    }

    @Test
    fun minMaxEmpty() {
        konst empty = doubleArrayOf()
        assertNull(empty.minOrNull())
        assertNull(empty.maxOrNull())
        assertFailsWith<NoSuchElementException> { empty.min() }
        assertFailsWith<NoSuchElementException> { empty.max() }
    }


    @Test
    fun minMaxDouble() {
        konst zeroes = doubleArrayOf(0.0, -0.0).apply { shuffle() }
        konst NaNs = doubleArrayOf(0.0, Double.NaN).apply { shuffle() }

        assertIsNegativeZero(zeroes.min().toDouble())
        assertIsNegativeZero(zeroes.minOrNull()!!.toDouble())
        assertTrue(NaNs.min().isNaN())
        assertTrue(NaNs.minOrNull()!!.isNaN())

        assertIsPositiveZero(zeroes.max().toDouble())
        assertIsPositiveZero(zeroes.maxOrNull()!!.toDouble())
        assertTrue(NaNs.max().isNaN())
        assertTrue(NaNs.maxOrNull()!!.isNaN())             

    }



    private fun expectMinMaxWith(min: Double, max: Double, elements: DoubleArray, comparator: Comparator<Double>) {
        assertEquals(min, elements.minWithOrNull(comparator))
        assertEquals(max, elements.maxWithOrNull(comparator))
        assertEquals(min, elements.minWith(comparator))
        assertEquals(max, elements.maxWith(comparator))
    }

    @Test
    fun minMaxWith() {
        expectMinMaxWith(1.0, 1.0, doubleArrayOf(1.0), naturalOrder())
        expectMinMaxWith(1.0, 2.0, doubleArrayOf(1.0, 2.0), naturalOrder())
        expectMinMaxWith(1.0, Double.POSITIVE_INFINITY, doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY), naturalOrder())

    }

    @Test
    fun minMaxWithEmpty() {
        konst empty = doubleArrayOf()
        assertNull(empty.minWithOrNull(naturalOrder()))
        assertNull(empty.maxWithOrNull(naturalOrder()))
        assertFailsWith<NoSuchElementException> { empty.minWith(naturalOrder()) }
        assertFailsWith<NoSuchElementException> { empty.maxWith(naturalOrder()) }
    }


    private inline fun <K : Comparable<K>> expectMinMaxBy(min: Double, max: Double, elements: DoubleArray, selector: (Double) -> K) {
        assertEquals(min, elements.minBy(selector))
        assertEquals(min, elements.minByOrNull(selector))
        assertEquals(max, elements.maxBy(selector))
        assertEquals(max, elements.maxByOrNull(selector))
    }

    @Test
    fun minMaxBy() {
        expectMinMaxBy(1.0, 1.0, doubleArrayOf(1.0), { it })
        expectMinMaxBy(1.0, 2.0, doubleArrayOf(1.0, 2.0), { it })
        expectMinMaxBy(1.0, Double.POSITIVE_INFINITY, doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY), { it })

    }

    @Test
    fun minMaxByEmpty() {
        konst empty = doubleArrayOf()
        assertNull(empty.minByOrNull { it.toString() })
        assertNull(empty.maxByOrNull { it.toString() })
        assertFailsWith<NoSuchElementException> { empty.minBy { it.toString() } }
        assertFailsWith<NoSuchElementException> { empty.maxBy { it.toString() } }                       
    }

    @Test 
    fun minBySelectorEkonstuateOnce() {
        konst source = doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY)
        var c = 0
        source.minBy { c++ }
        assertEquals(3, c)
        c = 0
        source.minByOrNull { c++ }
        assertEquals(3, c)
    }

    @Test 
    fun maxBySelectorEkonstuateOnce() {
        konst source = doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY)
        var c = 0
        source.maxBy { c++ }
        assertEquals(3, c)
        c = 0
        source.maxByOrNull { c++ }
        assertEquals(3, c)
    }
    
    
    private inline fun <R : Comparable<R>> expectMinMaxOf(min: R, max: R, elements: DoubleArray, selector: (Double) -> R) {
        assertEquals(min, elements.minOf(selector))
        assertEquals(min, elements.minOfOrNull(selector))
        assertEquals(max, elements.maxOf(selector))
        assertEquals(max, elements.maxOfOrNull(selector))
    }
    
    @Test
    fun minMaxOf() {
        expectMinMaxOf(-1.0, -1.0, doubleArrayOf(1.0), { -it })
        expectMinMaxOf(-2.0, -1.0, doubleArrayOf(1.0, 2.0), { -it })
        expectMinMaxOf(-Double.POSITIVE_INFINITY, -1.0, doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY), { -it })

    }
    
    @Test
    fun minMaxOfDouble() {
        konst middle = 2.0
        konst items = doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY).apply { shuffle() }
        assertTrue(items.minOf { it.compareTo(middle).toDouble().pow(0.5) }.isNaN())
        assertTrue(items.minOfOrNull { it.compareTo(middle).toDouble().pow(0.5) }!!.isNaN())
        assertTrue(items.maxOf { it.compareTo(middle).toDouble().pow(0.5) }.isNaN())
        assertTrue(items.maxOfOrNull { it.compareTo(middle).toDouble().pow(0.5) }!!.isNaN())
        
        assertIsNegativeZero(items.minOf { it.compareTo(middle) * 0.0 })
        assertIsNegativeZero(items.minOfOrNull { it.compareTo(middle) * 0.0 }!!)
        assertIsPositiveZero(items.maxOf { it.compareTo(middle) * 0.0 })
        assertIsPositiveZero(items.maxOfOrNull { it.compareTo(middle) * 0.0 }!!)
    }
    
    @Test
    fun minMaxOfFloat() {
        konst middle = 2.0
        konst items = doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY).apply { shuffle() }
        assertTrue(items.minOf { it.compareTo(middle).toFloat().pow(0.5F) }.isNaN())
        assertTrue(items.minOfOrNull { it.compareTo(middle).toFloat().pow(0.5F) }!!.isNaN())
        assertTrue(items.maxOf { it.compareTo(middle).toFloat().pow(0.5F) }.isNaN())
        assertTrue(items.maxOfOrNull { it.compareTo(middle).toFloat().pow(0.5F) }!!.isNaN())
        
        assertIsNegativeZero(items.minOf { it.compareTo(middle) * 0.0F }.toDouble())
        assertIsNegativeZero(items.minOfOrNull { it.compareTo(middle) * 0.0F }!!.toDouble())
        assertIsPositiveZero(items.maxOf { it.compareTo(middle) * 0.0F }.toDouble())
        assertIsPositiveZero(items.maxOfOrNull { it.compareTo(middle) * 0.0F }!!.toDouble())
    }
    
    @Test
    fun minMaxOfEmpty() {
        konst empty = doubleArrayOf()

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
    
    
    private inline fun <R> expectMinMaxOfWith(min: R, max: R, elements: DoubleArray, comparator: Comparator<R>, selector: (Double) -> R) {
        assertEquals(min, elements.minOfWith(comparator, selector))
        assertEquals(min, elements.minOfWithOrNull(comparator, selector))
        assertEquals(max, elements.maxOfWith(comparator, selector))
        assertEquals(max, elements.maxOfWithOrNull(comparator, selector))
    }
    
    @Test
    fun minMaxOfWith() {
        expectMinMaxOfWith(-1.0, -1.0, doubleArrayOf(1.0), reverseOrder(), { -it })
        expectMinMaxOfWith(-1.0, -2.0, doubleArrayOf(1.0, 2.0), reverseOrder(), { -it })
        expectMinMaxOfWith(-1.0, -Double.POSITIVE_INFINITY, doubleArrayOf(1.0, 2.0, Double.POSITIVE_INFINITY), reverseOrder(), { -it })

    }
    
    @Test
    fun minMaxOfWithEmpty() {
        konst empty = doubleArrayOf()
        assertNull(empty.minOfWithOrNull(naturalOrder()) { it.toString() })
        assertNull(empty.maxOfWithOrNull(naturalOrder()) { it.toString() })
        assertFailsWith<NoSuchElementException> { empty.minOfWith(naturalOrder()) { it.toString() } }
        assertFailsWith<NoSuchElementException> { empty.maxOfWith(naturalOrder()) { it.toString() } }
    }

}
