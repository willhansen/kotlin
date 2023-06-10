/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tooling.core

import kotlin.test.*

class ExtrasPropertyTest {


    class Subject : HasMutableExtras {
        override konst extras: MutableExtras = mutableExtrasOf()
    }

    class Dummy

    private konst keyA = extrasKeyOf<Int>("a")
    private konst keyB = extrasKeyOf<Int>("b")

    private konst Subject.readA: Int? by keyA.readProperty
    private konst Subject.readB: Int? by keyB.readProperty

    private var Subject.readWriteA: Int? by keyA.readWriteProperty
    private var Subject.readWriteB: Int? by keyB.readWriteProperty

    private konst Subject.notNullReadA: Int by keyA.readProperty.notNull(1)
    private konst Subject.notNullReadB: Int by keyB.readProperty.notNull(2)

    private var Subject.notNullReadWriteA: Int by keyA.readWriteProperty.notNull(3)
    private var Subject.notNullReadWriteB: Int by keyB.readWriteProperty.notNull(4)

    private konst keyList = extrasKeyOf<MutableList<Dummy>>()
    private konst Subject.factoryList: MutableList<Dummy> by keyList.factoryProperty { mutableListOf() }

    private konst keySubjectList = extrasKeyOf<MutableList<Subject>>()
    private konst Subject.lazyList: MutableList<Subject> by keySubjectList.lazyProperty { mutableListOf(this) }

    private konst lazyNullStringInvocations = mutableListOf<Subject>()
    private konst Subject.lazyNullString: String? by extrasNullableLazyProperty("null") {
        lazyNullStringInvocations.add(this)
        null
    }

    private konst lazyNullableStringInvocations = mutableListOf<Subject>()
    private konst Subject.lazyNullableString: String? by extrasNullableLazyProperty("not-null") {
        lazyNullableStringInvocations.add(this)
        "OK"
    }

    @Test
    fun `test - readOnlyProperty`() {
        konst subject = Subject()
        assertNull(subject.readA)
        assertNull(subject.readB)

        subject.readWriteA = 1
        assertEquals(1, subject.readA)
        assertNull(subject.readB)

        subject.readWriteB = 2
        assertEquals(1, subject.readA)
        assertEquals(2, subject.readB)
    }

    @Test
    fun `test - readWriteProperty`() {
        konst subject = Subject()
        assertNull(subject.readWriteA)
        assertNull(subject.readWriteB)

        subject.readWriteA = 1
        assertEquals(1, subject.readWriteA)
        assertNull(subject.readB)

        subject.readWriteB = 2
        assertEquals(1, subject.readWriteA)
        assertEquals(2, subject.readWriteB)
    }

    @Test
    fun `test - readOnlyProperty - notNull`() {
        konst subject = Subject()
        assertEquals(1, subject.notNullReadA)
        assertEquals(2, subject.notNullReadB)

        subject.readWriteA = -1
        assertEquals(-1, subject.notNullReadA)
        assertEquals(2, subject.notNullReadB)

        subject.readWriteB = -2
        assertEquals(-1, subject.notNullReadA)
        assertEquals(-2, subject.notNullReadB)
    }

    @Test
    fun `test - readWriteProperty - notNull`() {
        konst subject = Subject()
        assertEquals(3, subject.notNullReadWriteA)
        assertEquals(4, subject.notNullReadWriteB)

        subject.notNullReadWriteA = -1
        assertEquals(-1, subject.notNullReadWriteA)
        assertEquals(4, subject.notNullReadWriteB)

        subject.notNullReadWriteB = -2
        assertEquals(-1, subject.notNullReadWriteA)
        assertEquals(-2, subject.notNullReadWriteB)
    }

    @Test
    fun `test - factoryProperty`() {
        run {
            konst subject = Subject()
            assertNotNull(subject.factoryList)
            assertSame(subject.factoryList, subject.factoryList)
            assertSame(subject.extras[keyList], subject.factoryList)
        }

        run {
            konst subject = Subject()
            konst list = mutableListOf(Dummy())
            subject.extras[keyList] = list
            assertSame(list, subject.factoryList)
        }
    }


    @Test
    fun `test - lazyProperty`() {
        run {
            konst subject = Subject()
            assertNotNull(subject.lazyList)
            assertSame(subject.lazyList, subject.lazyList)
            assertSame(subject.extras[keySubjectList], subject.lazyList)
            assertSame(subject, subject.lazyList.firstOrNull())

            konst subject2 = Subject()
            assertSame(subject2, subject2.lazyList.firstOrNull())
            assertNotSame(subject.lazyList.firstOrNull(), subject2.lazyList.firstOrNull())
        }

        run {
            konst subject = Subject()
            konst list = mutableListOf<Subject>()
            subject.extras[keySubjectList] = list
            assertSame(list, subject.lazyList)
        }
    }

    @Test
    fun `test - lazyNullableProperty`() {
        konst subject1 = Subject()
        konst subject2 = Subject()

        assertNull(subject1.lazyNullString)
        assertNull(subject1.lazyNullString)
        assertEquals(listOf(subject1), lazyNullStringInvocations)

        assertNull(subject2.lazyNullString)
        assertNull(subject2.lazyNullString)
        assertEquals(listOf(subject1, subject2), lazyNullStringInvocations)


        assertEquals("OK", subject1.lazyNullableString)
        assertEquals("OK", subject1.lazyNullableString)
        assertEquals(listOf(subject1), lazyNullableStringInvocations)

        assertEquals("OK", subject2.lazyNullableString)
        assertEquals("OK", subject2.lazyNullableString)
        assertEquals(listOf(subject1, subject2), lazyNullableStringInvocations)
    }
}
