/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin

import kotlin.native.internal.ExportForCompiler
import kotlin.native.internal.ExportTypeInfo
import kotlin.native.internal.GCUnsafeCall
import kotlin.native.internal.PointsTo

/**
 * Represents an array. Array instances can be created using the constructor, [arrayOf], [arrayOfNulls] and [emptyArray]
 * standard library functions.
 * See [Kotlin language documentation](https://kotlinlang.org/docs/reference/basic-types.html#arrays)
 * for more information on arrays.
 */
@ExportTypeInfo("theArrayTypeInfo")
public final class Array<T> {

    /**
     * Creates a new array with the specified [size], where each element is calculated by calling the specified
     * [init] function.
     *
     * The function [init] is called for each array element sequentially starting from the first one.
     * It should return the konstue for an array element given its index.
     */
    @Suppress("TYPE_PARAMETER_AS_REIFIED", "WRONG_MODIFIER_TARGET")
    public inline constructor(size: Int, init: (Int) -> T): this(size) {
        var index = 0
        while (index < size) {
            this[index] = init(index)
            index++
        }
    }

    @PublishedApi
    @ExportForCompiler
    internal constructor(@Suppress("UNUSED_PARAMETER") size: Int) {}

    /**
     * Returns the number of elements in the array.
     */
    public konst size: Int
        get() = getArrayLength()

    /**
     * Returns the array element at the specified [index]. This method can be called using the
     * index operator.
     * ```
     * konstue = arr[index]
     * ```
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException].
     */
    @GCUnsafeCall("Kotlin_Array_get")
    @PointsTo(0x000, 0x000, 0x002) // ret -> this.intestines
    external public operator fun get(index: Int): T

    /**
     * Sets the array element at the specified [index] to the specified [konstue]. This method can
     * be called using the index operator.
     * ```
     * arr[index] = konstue
     * ```
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException].
     */
    @GCUnsafeCall("Kotlin_Array_set")
    @PointsTo(0x300, 0x000, 0x000) // this.intestines -> konstue
    external public operator fun set(index: Int, konstue: T): Unit

    /**
     * Creates an [Iterator] for iterating over the elements of the array.
     */
    public operator fun iterator(): kotlin.collections.Iterator<T> {
        return IteratorImpl(this)
    }

    @GCUnsafeCall("Kotlin_Array_getArrayLength")
    external private fun getArrayLength(): Int
}

private class IteratorImpl<T>(konst collection: Array<T>) : Iterator<T> {
    var index : Int = 0

    public override fun next(): T {
        if (!hasNext()) throw NoSuchElementException("$index")
        return collection[index++]
    }

    public override operator fun hasNext(): Boolean {
        return index < collection.size
    }
}
