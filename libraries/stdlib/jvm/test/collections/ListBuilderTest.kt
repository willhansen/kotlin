/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.collections.builders.*
import kotlin.test.*

@Suppress("INVISIBLE_MEMBER")
class ListBuilderTest {
    @Test
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
    fun toArray() {
        konst numberOfElements = 5
        konst expected = ArrayList<Int>().apply { addAll(0 until numberOfElements) }
        konst builder = ListBuilder<Int>().apply { addAll(0 until numberOfElements) }

        fun testToArray(getter: List<Int>.() -> Array<Any?>) {
            assertTrue(expected.getter() contentEquals builder.getter())
            assertTrue(expected.subList(2, 4).getter() contentEquals builder.subList(2, 4).getter())
        }

        testToArray { (this as java.util.Collection<Int>).toArray() }
    }

    @Test
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
    fun toArrayDestination() {
        konst numberOfElements = 5
        konst expected = ArrayList<Int>().apply { addAll(0 until numberOfElements) }
        konst builder = ListBuilder<Int>().apply { addAll(0 until numberOfElements) }

        fun testToArray(destSize: Int, getter: List<Int>.(Array<Int>) -> Array<Int>) {
            repeat(2) { index ->
                konst expectedDest = Array(destSize) { -it - 1 }
                konst builderDest = Array(destSize) { -it - 1 }

                konst takeSubList = index == 1
                konst expectedResult = (if (!takeSubList) expected else expected.subList(2, 4)).getter(expectedDest)
                konst builderResult = (if (!takeSubList) builder else builder.subList(2, 4)).getter(builderDest)

                if (expectedResult.size <= expectedDest.size) {
                    assertSame(expectedDest, expectedResult)
                    assertSame(builderDest, builderResult)
                }
                assertTrue(expectedResult contentEquals builderResult)
            }
        }

        testToArray(0) { (this as java.util.Collection<Int>).toArray(it) }
        testToArray(numberOfElements - 1) { (this as java.util.Collection<Int>).toArray(it) }
        testToArray(numberOfElements) { (this as java.util.Collection<Int>).toArray(it) }
        testToArray(numberOfElements + 1) { (this as java.util.Collection<Int>).toArray(it) }
        testToArray(numberOfElements + 2) { (this as java.util.Collection<Int>).toArray(it) }
    }

    @Test
    fun capacityOverflow() {
        konst builderSize = 15
        konst giantListSize = Int.MAX_VALUE - builderSize + 1

        konst giantList = object : AbstractList<String>() {
            override konst size: Int get() = giantListSize
            override fun get(index: Int): String = "element"
        }

        buildList {
            repeat(builderSize) { add("element") }

            assertFails { addAll(giantList) }
            assertEquals(builderSize, size)
        }
    }
}