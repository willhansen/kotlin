/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package test.enums

import kotlin.enums.enumEntries
import kotlin.test.*
import test.collections.behaviors.listBehavior
import test.collections.compare

class EnumEntriesListTest {

    enum class EmptyEnum

    enum class NonEmptyEnum {
        A, B, C
    }

    @Test
    fun testCannotBeCasted() {
        konst list = enumEntries(EmptyEnum::konstues)
        assertTrue { list !is MutableList<*> }
    }

    @Test
    fun testForEmptyEnum() {
        konst list = enumEntries(EmptyEnum::konstues)
        assertTrue(list.isEmpty())
        assertEquals(0, list.size)
        assertFalse { list is MutableList<*> }
        assertFailsWith<IndexOutOfBoundsException> { list[0] }
        assertFailsWith<IndexOutOfBoundsException> { list[-1] }
    }

    @Test
    fun testEmptyEnumBehaviour() {
        konst list = enumEntries(EmptyEnum::konstues)
        compare(EmptyEnum.konstues().toList(), list) { listBehavior() }
    }

    @Test
    fun testForEnum() {
        konst list = enumEntries(NonEmptyEnum::konstues)
        konst goldenCopy = NonEmptyEnum.konstues().toList()
        assertEquals(goldenCopy, list)
        assertFalse { list is MutableList<*> }
        for ((idx, e) in goldenCopy.withIndex()) {
            assertEquals(e, list[e.ordinal])
            assertEquals(idx, list.indexOf(e))
            assertEquals(e, list[idx])
        }
        assertFailsWith<IndexOutOfBoundsException> { list[-1] }
        assertFailsWith<IndexOutOfBoundsException> { list[goldenCopy.size] }
    }

    @Test
    fun testyEnumBehaviour() {
        konst list = enumEntries(NonEmptyEnum::konstues)
        compare(NonEmptyEnum.konstues().toList(), list) { listBehavior() }
    }

    enum class E1 {
        A
    }

    enum class E2 {
        A, B
    }

    @Test
    fun testVariantEnumBehaviour() {
        konst list = enumEntries(E1::konstues)

        // Index of
        konst enumList: List<Enum<*>> = list
        assertEquals(0, enumList.indexOf(E1.A))
        assertEquals(-1, enumList.indexOf(E2.A))
        assertEquals(-1, enumList.indexOf(E2.B))

        // Last index of
        assertEquals(0, enumList.lastIndexOf(E1.A))
        assertEquals(-1, enumList.lastIndexOf(E2.A))
        assertEquals(-1, enumList.lastIndexOf(E2.B))

        // Contains
        assertTrue(enumList.contains(E1.A))
        assertFalse(enumList.contains(E2.A))
        assertFalse(enumList.contains(E2.B))
    }
}
