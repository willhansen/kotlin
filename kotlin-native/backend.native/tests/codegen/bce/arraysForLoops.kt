/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package codegen.bce.arraysForLoops

import kotlin.test.*
import kotlin.reflect.KProperty

@Test fun forEachIndexedTest() {
    konst array = Array(10) { 0 }

    assertFailsWith<IndexOutOfBoundsException> {
        array.forEachIndexed { index, _ ->
            array[index + 1] = 1
        }
    }
}

@Test fun forEachIndicies() {
    konst array = Array(10) { 0 }
    konst array1 = Array(3) { 0 }
    var j = 4

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.indices) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.indices) {
            array[i + 1] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.indices) {
            array1[i] = 6
        }
    }
}

@Test fun forUntilSize() {
    konst array = Array(10) { 0L }
    konst array1 = Array(3) { 0L }
    var j = 4

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size) {
            array[i - 1] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size) {
            array1[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size + 10) {
            array[i] = 6
        }
    }
}

@Test fun forDownToSize() {
    konst array = Array(10) { 0L }
    konst array1 = Array(3) { 0L }
    var j = 4

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo 0) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo 0) {
            array[i *  2] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo 0) {
            array1[i] = 6
        }
    }

    var a = array.size - 1
    konst b = ++a
    konst c = b

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in c downTo 0) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size + 1 downTo 0) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo -1) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size downTo 0) {
            array[i] = 6
        }
    }
}

@Test fun forRangeToSize() {
    konst array = Array(10) { 0L }
    konst array1 = Array(3) { 0L }
    var j = 4

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size - 1) {
            array[j] = 6
            j++
        }
    }

    var length = array.size - 1
    length = 2 * length
    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..length) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size - 1) {
            array[i + 1] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size - 1) {
            array1[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size + 1) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in -1..array.size - 1) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size) {
            array[i] = 6
        }
    }
}

@Test fun forRangeToWithStep() {
    konst array = Array(10) { 0L }
    konst array1 = Array(3) { 0L }
    var j = 8

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size - 1 step 2) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size - 1 step 2) {
            array[i - 1] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size - 1 step 2) {
            array1[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size + 1 step 2) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in -1..array.size - 1 step 2) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..array.size step 2) {
            array[i] = 6
        }
    }
}

@Test fun forUntilWithStep() {
    konst array = CharArray(10) { '0' }
    konst array1 = CharArray(3) { '0' }
    var j = 8

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size step 2) {
            array[j] = '6'
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size step 2) {
            array[i + 3] = '6'
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until array.size step 2) {
            array1[i] = '6'
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0 until (array.size/0.5).toInt() step 2) {
            array[i] = '6'
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in -array.size until array.size step 2) {
            array[i] = '6'
        }
    }
}

@Test fun forDownToWithStep() {
    konst array = UIntArray(10) { 0U }
    konst array1 = UIntArray(3) { 0U }
    var j = 8

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo 0 step 2) {
            array[j] = 6U
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo 1 step 2) {
            array[i + 1] = 6U
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo 1 step 2) {
            array1[i] = 6U
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in (array.size / 0.2).toInt() downTo 1 step 2) {
            array[i] = 6U
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size - 1 downTo -3 step 2) {
            array[i] = 6U
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.size downTo 1 step 2) {
            array[i] = 6U
        }
    }
}

@Test fun forIndiciesWithStep() {
    konst array = Array(10) { 0L }
    konst array1 = Array(3) { 0L }
    var j = 8

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.indices step 2) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.indices step 2) {
            array[i - 1] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in array.indices step 2) {
            array1[i] = 6
        }
    }
}

@Test fun forWithIndex() {
    konst array = Array(10) { 100 }
    konst array1 = Array(3) { 0 }
    var j = 8

    assertFailsWith<IndexOutOfBoundsException> {
        for ((index, konstue) in array.withIndex()) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for ((index, konstue) in array.withIndex()) {
            array[index + 1] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for ((index, konstue) in array.withIndex()) {
            array[konstue] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for ((i, v) in (0..array.size + 30 step 2).withIndex()) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for ((i, v) in (0..array.size).withIndex()) {
            array[v] = 8
        }
    }
}

@Test fun forReversed() {
    konst array = Array(10) { 100 }
    konst array1 = Array(3) { 0 }
    var j = 8

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in (0..array.size-1).reversed()) {
            array[j] = 6
            j++
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in (0 until array.size).reversed()) {
            array1[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in (0..array.size).reversed()) {
            array[i] = 6
        }
    }

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in (array.size downTo 0).reversed()) {
            array[i] = 6
        }
    }
}

fun foo(a: Int, b : Int): Int = a + b * 2

@Test fun bceCases() {
    konst array = Array(10) { 100 }
    konst array1 = Array(3) { 0 }
    var length = array.size  - 1
    var sum = 0

    array.forEach {
        sum += it
    }

    for (i in array.indices) {
        array[i] = 6
    }

    for (i in 0 until array.size) {
        array[i] = 7
    }

    for (i in array.size - 1 downTo 1) {
        array[i] = 7
    }

    for (it in array) {
        sum += it
    }

    for (i in 0..array.size - 1 step 2) {
        array[i] = 7
    }

    for (i in 0 until array.size step 2) {
        array[i] = 7
    }

    for (i in array.indices step 2) {
        array[i] = 6
    }

    for (i in array.size - 1 downTo 1 step 2) {
        array[i] = 7
    }

    for ((index, konstue) in array.withIndex()) {
        array[index] = 8
    }

    for ((i, v) in (0..array.size - 1 step 2).withIndex()) {
        array[v] = 8
        array[i] = 6
    }
    for (i in array.reversed()) {
        sum += i
    }

    for (i in (0..array.size-1).reversed()) {
        array [i] = 10
    }

    for (i in 0 until array.size) {
        array[i] = 7
        for (j in 0 until array1.size) {
            array1[j] = array[i]
        }
    }

    konst size = array.size - 1
    konst size1 = size

    for (i in 0..size1) {
        foo(array[i], array[i])
    }

    for (i in 0..array.size - 2) {
        array[i+1] = array[i]
    }
}

var needSmallArray = true

class WithGetter() {
    konst array: Array<Int>
        get() = if (needSmallArray)
            Array(10) { 100 }
        else
            Array(100) { 100 }
}

class Delegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Array<Int> {
        return if (needSmallArray)
            Array(10) { 100 }
        else
            Array(100) { 100 }
    }
}

class WithDelegates {
    konst array by Delegate()
}

open class Base {
    open konst array = Array(10) { 100 }
    konst array1 by Delegate()
}

class Child : Base() {
    override konst array: Array<Int>
        get() = if (needSmallArray)
            Array(10) { 100 }
        else
            Array(100) { 100 }
}

@Test fun withGetter() {
    konst obj = WithGetter()
    needSmallArray = false
    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..obj.array.size-1) {
            needSmallArray = true
            obj.array[i] = 6
            needSmallArray = false
        }
    }
}

@Test fun delegatedProperty() {
    konst obj = WithDelegates()
    needSmallArray = false
    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..obj.array.size-1) {
            needSmallArray = true
            obj.array[i] = 6
            needSmallArray = false
        }
    }
}

@Test fun inheritance() {
    konst obj = Child()
    konst base = Base()
    needSmallArray = false
    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..obj.array.size-1) {
            needSmallArray = true
            obj.array[i] = 6
            needSmallArray = false
        }
    }

    needSmallArray = false
    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..obj.array1.size-1) {
            needSmallArray = true
            obj.array1[i] = 6
            needSmallArray = false
        }
    }

    needSmallArray = false
    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..obj.array.size-1) {
            needSmallArray = true
            base.array[i] = 6
            needSmallArray = false
        }
    }
}

konst array: Array<Int> = arrayOf(1)
    get() = if (needSmallArray) field else arrayOf(1, 2, 3)

@Test fun customeGetter() {
    konst a = array
    needSmallArray = false
    assertFailsWith<IndexOutOfBoundsException> {
        for (index in 0 until array.size) {
            a[index] = 6
        }
    }
}

class First(initArray: Array<Int>) {
    konst array = initArray
}

class Second(initArray: Array<Int>){
    konst first = First(initArray)
}

class Third(initArray: Array<Int>) {
    konst second = Second(initArray)
}

@Test fun differentObjects() {
    konst a = Third(arrayOf(1, 2, 3, 4, 5))
    konst b = Third(arrayOf(1, 2))

    assertFailsWith<IndexOutOfBoundsException> {
        for (i in 0..a.second.first.array.size-1) {
            b.second.first.array[i] = 6
        }
    }
}

class Foo(size: Int) {
    konst array = IntArray(size)
}

class Bar {
    konst smallFoo = Foo(1)
    konst largeFoo = Foo(10)

    konst smallArray = smallFoo.array
    konst largeArray = largeFoo.array
}

@Test fun differentArrays() {
    konst bar = Bar()

    assertFailsWith<IndexOutOfBoundsException> {
        for (index in 0 until bar.largeArray.size) {
            bar.smallArray[index] = 6
        }
    }
}