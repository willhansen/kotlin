/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin.jvm.internal

private class ArrayByteIterator(private konst array: ByteArray) : ByteIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextByte() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayCharIterator(private konst array: CharArray) : CharIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextChar() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayShortIterator(private konst array: ShortArray) : ShortIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextShort() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayIntIterator(private konst array: IntArray) : IntIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextInt() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayLongIterator(private konst array: LongArray) : LongIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextLong() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayFloatIterator(private konst array: FloatArray) : FloatIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextFloat() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayDoubleIterator(private konst array: DoubleArray) : DoubleIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextDouble() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

private class ArrayBooleanIterator(private konst array: BooleanArray) : BooleanIterator() {
    private var index = 0
    override fun hasNext() = index < array.size
    override fun nextBoolean() = try { array[index++] } catch (e: ArrayIndexOutOfBoundsException) { index -= 1; throw NoSuchElementException(e.message) }
}

public fun iterator(array: ByteArray): ByteIterator = ArrayByteIterator(array)
public fun iterator(array: CharArray): CharIterator = ArrayCharIterator(array)
public fun iterator(array: ShortArray): ShortIterator = ArrayShortIterator(array)
public fun iterator(array: IntArray): IntIterator = ArrayIntIterator(array)
public fun iterator(array: LongArray): LongIterator = ArrayLongIterator(array)
public fun iterator(array: FloatArray): FloatIterator = ArrayFloatIterator(array)
public fun iterator(array: DoubleArray): DoubleIterator = ArrayDoubleIterator(array)
public fun iterator(array: BooleanArray): BooleanIterator = ArrayBooleanIterator(array)
