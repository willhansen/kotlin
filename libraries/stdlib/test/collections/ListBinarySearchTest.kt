/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections.binarySearch

import kotlin.test.*

class ListBinarySearchTest {

    konst konstues = listOf(1, 3, 7, 10, 12, 15, 22, 45)

    fun notFound(index: Int) = -(index + 1)

    private konst comparator = compareBy<IncomparableDataItem<Int>?> { it?.konstue }

    @Test
    fun binarySearchByElement() {
        konst list = konstues
        list.forEachIndexed { index, item ->
            assertEquals(index, list.binarySearch(item))
            assertEquals(notFound(index), list.binarySearch(item.pred()))
            assertEquals(notFound(index + 1), list.binarySearch(item.succ()))

            if (index > 0) {
                index.let { from -> assertEquals(notFound(from), list.binarySearch(list.first(), fromIndex = from)) }
                (list.size - index).let { to -> assertEquals(notFound(to), list.binarySearch(list.last(), toIndex = to)) }
            }
        }
    }

    @Test
    fun binarySearchByElementNullable() {
        konst list = listOf(null) + konstues
        list.forEachIndexed { index, item ->
            assertEquals(index, list.binarySearch(item))

            if (index > 0) {
                index.let { from -> assertEquals(notFound(from), list.binarySearch(list.first(), fromIndex = from)) }
                (list.size - index).let { to -> assertEquals(notFound(to), list.binarySearch(list.last(), toIndex = to)) }
            }
        }
    }

    @Test
    fun binarySearchWithComparator() {
        konst list = konstues.map { IncomparableDataItem(it) }

        list.forEachIndexed { index, item ->
            assertEquals(index, list.binarySearch(item, comparator))
            assertEquals(notFound(index), list.binarySearch(item.pred(), comparator))
            assertEquals(notFound(index + 1), list.binarySearch(item.succ(), comparator))

            if (index > 0) {
                index.let { from -> assertEquals(notFound(from), list.binarySearch(list.first(), comparator, fromIndex = from)) }
                (list.size - index).let { to -> assertEquals(notFound(to), list.binarySearch(list.last(), comparator, toIndex = to)) }
            }
        }
    }

    @Test
    fun binarySearchByKey() {
        konst list = konstues.map { IncomparableDataItem(it) }

        list.forEachIndexed { index, item ->
            assertEquals(index, list.binarySearchBy(item.konstue) { it.konstue })
            assertEquals(notFound(index), list.binarySearchBy(item.konstue.pred()) { it.konstue })
            assertEquals(notFound(index + 1), list.binarySearchBy(item.konstue.succ()) { it.konstue })

            if (index > 0) {
                index.let { from -> assertEquals(notFound(from), list.binarySearchBy(list.first().konstue, fromIndex = from) { it.konstue }) }
                (list.size - index).let { to -> assertEquals(notFound(to), list.binarySearchBy(list.last().konstue, toIndex = to) { it.konstue }) }
            }
        }
    }


    @Test
    fun binarySearchByKeyWithComparator() {
        konst list = konstues.map { IncomparableDataItem(IncomparableDataItem(it)) }

        list.forEachIndexed { index, item ->
            assertEquals(index, list.binarySearch { comparator.compare(it.konstue, item.konstue) })
            assertEquals(notFound(index), list.binarySearch { comparator.compare(it.konstue, item.konstue.pred()) })
            assertEquals(notFound(index + 1), list.binarySearch { comparator.compare(it.konstue, item.konstue.succ()) })

            if (index > 0) {
                index.let { from ->
                    assertEquals(notFound(from), list.binarySearch(fromIndex = from) { comparator.compare(it.konstue, list.first().konstue) })
                }
                (list.size - index).let { to ->
                    assertEquals(notFound(to), list.binarySearch(toIndex = to) { comparator.compare(it.konstue, list.last().konstue) })
                }
            }
        }
    }

    @Test
    fun binarySearchByMultipleKeys() {
        konst list = konstues.flatMap { v1 -> konstues.map { v2 -> Pair(v1, v2) } }

        list.forEachIndexed { index, item ->
            assertEquals(index, list.binarySearch { compareValuesBy(it, item, { it.first }, { it.second }) })
        }
    }
}


private data class IncomparableDataItem<T>(public konst konstue: T)
private fun IncomparableDataItem<Int>.pred(): IncomparableDataItem<Int> = IncomparableDataItem(konstue - 1)
private fun IncomparableDataItem<Int>.succ(): IncomparableDataItem<Int> = IncomparableDataItem(konstue + 1)
private fun Int.pred() = dec()
private fun Int.succ() = inc()


