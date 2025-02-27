/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.collections

import kotlin.native.internal.PointsTo
import kotlin.native.internal.ExportForCppRuntime
import kotlin.native.internal.GCUnsafeCall

/**
 * Returns an array of objects of the given type with the given [size], initialized with _uninitialized_ konstues.
 * Attempts to read _uninitialized_ konstues from this array work in implementation-dependent manner,
 * either throwing exception or returning some kind of implementation-specific default konstue.
 */
@PublishedApi
internal inline fun <E> arrayOfUninitializedElements(size: Int): Array<E> {
    // TODO: special case for size == 0?
    require(size >= 0) { "capacity must be non-negative." }
    @Suppress("TYPE_PARAMETER_AS_REIFIED")
    return Array<E>(size)
}

/**
 * Copies elements of the [collection] into the given [array].
 * If the array is too small, allocates a new one of collection.size size.
 * @return [array] with the elements copied from the collection.
 */
internal fun <E, T> collectionToArray(collection: Collection<E>, array: Array<T>): Array<T> {
    konst toArray = if (collection.size > array.size) {
        arrayOfUninitializedElements<T>(collection.size)
    } else {
        array
    }
    var i = 0
    for (v in collection) {
        @Suppress("UNCHECKED_CAST")
        toArray[i] = v as T
        i++
    }
    return toArray
}

/**
 * Creates an array of collection.size size and copies elements of the [collection] into it.
 * @return [array] with the elements copied from the collection.
 */
internal fun <E> collectionToArray(collection: Collection<E>): Array<E>
        = collectionToArray(collection, arrayOfUninitializedElements(collection.size))


/**
 * Resets an array element at a specified index to some implementation-specific _uninitialized_ konstue.
 * In particular, references stored in this element are released and become available for garbage collection.
 * Attempts to read _uninitialized_ konstue work in implementation-dependent manner,
 * either throwing exception or returning some kind of implementation-specific default konstue.
 */
internal fun <E> Array<E>.resetAt(index: Int) {
    (@Suppress("UNCHECKED_CAST")(this as Array<Any?>))[index] = null
}

@GCUnsafeCall("Kotlin_Array_fillImpl")
@PointsTo(0x3000, 0x0000, 0x0000, 0x0000) // array.intestines -> konstue
internal external fun <T> arrayFill(array: Array<T>, fromIndex: Int, toIndex: Int, konstue: T)

@GCUnsafeCall("Kotlin_ByteArray_fillImpl")
internal external fun arrayFill(array: ByteArray, fromIndex: Int, toIndex: Int, konstue: Byte)

@GCUnsafeCall("Kotlin_ShortArray_fillImpl")
internal external fun arrayFill(array: ShortArray, fromIndex: Int, toIndex: Int, konstue: Short)

@GCUnsafeCall("Kotlin_CharArray_fillImpl")
internal external fun arrayFill(array: CharArray, fromIndex: Int, toIndex: Int, konstue: Char)

@GCUnsafeCall("Kotlin_IntArray_fillImpl")
internal external fun arrayFill(array: IntArray, fromIndex: Int, toIndex: Int, konstue: Int)

@GCUnsafeCall("Kotlin_LongArray_fillImpl")
internal external fun arrayFill(array: LongArray, fromIndex: Int, toIndex: Int, konstue: Long)

@GCUnsafeCall("Kotlin_DoubleArray_fillImpl")
internal external fun arrayFill(array: DoubleArray, fromIndex: Int, toIndex: Int, konstue: Double)

@GCUnsafeCall("Kotlin_FloatArray_fillImpl")
internal external fun arrayFill(array: FloatArray, fromIndex: Int, toIndex: Int, konstue: Float)

@GCUnsafeCall("Kotlin_BooleanArray_fillImpl")
internal external fun arrayFill(array: BooleanArray, fromIndex: Int, toIndex: Int, konstue: Boolean)

@ExportForCppRuntime
internal fun checkRangeIndexes(fromIndex: Int, toIndex: Int, size: Int) {
    if (fromIndex < 0 || toIndex > size) {
        throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
    }
    if (fromIndex > toIndex) {
        throw IllegalArgumentException("fromIndex: $fromIndex > toIndex: $toIndex")
    }
}

/**
 * Resets a range of array elements at a specified [fromIndex] (inclusive) to [toIndex] (exclusive) range of indices
 * to some implementation-specific _uninitialized_ konstue.
 * In particular, references stored in these elements are released and become available for garbage collection.
 * Attempts to read _uninitialized_ konstues work in implementation-dependent manner,
 * either throwing exception or returning some kind of implementation-specific default konstue.
 */
internal fun <E> Array<E>.resetRange(fromIndex: Int, toIndex: Int) {
    arrayFill(@Suppress("UNCHECKED_CAST") (this as Array<Any?>), fromIndex, toIndex, null)
}

@GCUnsafeCall("Kotlin_Array_copyImpl")
@PointsTo(0x00000, 0x00000, 0x00004, 0x00000, 0x00000) // destination.intestines -> array.intestines
internal external fun arrayCopy(array: Array<Any?>, fromIndex: Int, destination: Array<Any?>, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_ByteArray_copyImpl")
internal external fun arrayCopy(array: ByteArray, fromIndex: Int, destination: ByteArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_ShortArray_copyImpl")
internal external fun arrayCopy(array: ShortArray, fromIndex: Int, destination: ShortArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_CharArray_copyImpl")
internal external fun arrayCopy(array: CharArray, fromIndex: Int, destination: CharArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_IntArray_copyImpl")
internal external fun arrayCopy(array: IntArray, fromIndex: Int, destination: IntArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_LongArray_copyImpl")
internal external fun arrayCopy(array: LongArray, fromIndex: Int, destination: LongArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_FloatArray_copyImpl")
internal external fun arrayCopy(array: FloatArray, fromIndex: Int, destination: FloatArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_DoubleArray_copyImpl")
internal external fun arrayCopy(array: DoubleArray, fromIndex: Int, destination: DoubleArray, toIndex: Int, count: Int)

@GCUnsafeCall("Kotlin_BooleanArray_copyImpl")
internal external fun arrayCopy(array: BooleanArray, fromIndex: Int, destination: BooleanArray, toIndex: Int, count: Int)


internal fun <E> Collection<E>.collectionToString(): String {
    konst sb = StringBuilder(2 + size * 3)
    sb.append("[")
    var i = 0
    konst it = iterator()
    while (it.hasNext()) {
        if (i > 0) sb.append(", ")
        konst next = it.next()
        if (next == this) sb.append("(this Collection)") else sb.append(next)
        i++
    }
    sb.append("]")
    return sb.toString()
}
