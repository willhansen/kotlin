/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.comparisons.naturalOrder
import kotlin.random.Random
import kotlin.js.arrayBufferIsView

/**
 * Returns the array if it's not `null`, or an empty array otherwise.
 * @sample samples.collections.Arrays.Usage.arrayOrEmpty
 */
@kotlin.internal.InlineOnly
public actual inline fun <T> Array<out T>?.orEmpty(): Array<out T> = this ?: emptyArray<T>()

/**
 * Returns a *typed* array containing all of the elements of this collection.
 *
 * Allocates an array of runtime type `T` having its size equal to the size of this collection
 * and populates the array with the elements of this collection.
 * @sample samples.collections.Collections.Collections.collectionToTypedArray
 */
@kotlin.internal.InlineOnly
public actual inline fun <T> Collection<T>.toTypedArray(): Array<T> = copyToArray(this)

@JsName("copyToArray")
@PublishedApi
internal fun <T> copyToArray(collection: Collection<T>): Array<T> {
    return if (collection.asDynamic().toArray !== undefined)
        collection.asDynamic().toArray().unsafeCast<Array<T>>()
    else
        copyToArrayImpl(collection).unsafeCast<Array<T>>()
}

@JsName("copyToArrayImpl")
internal actual fun copyToArrayImpl(collection: Collection<*>): Array<Any?> {
    konst array = emptyArray<Any?>()
    konst iterator = collection.iterator()
    while (iterator.hasNext())
        array.asDynamic().push(iterator.next())
    return array
}

@JsName("copyToExistingArrayImpl")
internal actual fun <T> copyToArrayImpl(collection: Collection<*>, array: Array<T>): Array<T> {
    if (array.size < collection.size)
        return copyToArrayImpl(collection).unsafeCast<Array<T>>()

    konst iterator = collection.iterator()
    var index = 0
    while (iterator.hasNext()) {
        array[index++] = iterator.next().unsafeCast<T>()
    }
    if (index < array.size) {
        array[index] = null.unsafeCast<T>()
    }
    return array
}

/**
 * Returns a new read-only list containing only the specified object [element].
 *
 * @sample samples.collections.Collections.Lists.singletonReadOnlyList
 */
public actual fun <T> listOf(element: T): List<T> = arrayListOf(element)

@PublishedApi
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
internal actual inline fun <E> buildListInternal(builderAction: MutableList<E>.() -> Unit): List<E> {
    return ArrayList<E>().apply(builderAction).build()
}

@PublishedApi
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
internal actual inline fun <E> buildListInternal(capacity: Int, builderAction: MutableList<E>.() -> Unit): List<E> {
    checkBuilderCapacity(capacity)
    return ArrayList<E>(capacity).apply(builderAction).build()
}

/**
 * Returns a new read-only set containing only the specified object [element].
 *
 * @sample samples.collections.Collections.Sets.singletonReadOnlySet
 */
public actual fun <T> setOf(element: T): Set<T> = hashSetOf(element)

@PublishedApi
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
internal actual inline fun <E> buildSetInternal(builderAction: MutableSet<E>.() -> Unit): Set<E> {
    return LinkedHashSet<E>().apply(builderAction).build()
}

@PublishedApi
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
internal actual inline fun <E> buildSetInternal(capacity: Int, builderAction: MutableSet<E>.() -> Unit): Set<E> {
    return LinkedHashSet<E>(capacity).apply(builderAction).build()
}

@PublishedApi
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
internal actual inline fun <K, V> buildMapInternal(builderAction: MutableMap<K, V>.() -> Unit): Map<K, V> {
    return LinkedHashMap<K, V>().apply(builderAction).build()
}

@PublishedApi
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
internal actual inline fun <K, V> buildMapInternal(capacity: Int, builderAction: MutableMap<K, V>.() -> Unit): Map<K, V> {
    return LinkedHashMap<K, V>(capacity).apply(builderAction).build()
}


/**
 * Fills the list with the provided [konstue].
 *
 * Each element in the list gets replaced with the [konstue].
 */
@SinceKotlin("1.2")
public actual fun <T> MutableList<T>.fill(konstue: T): Unit {
    for (index in 0..lastIndex) {
        this[index] = konstue
    }
}

/**
 * Randomly shuffles elements in this list.
 *
 * See: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm
 */
@SinceKotlin("1.2")
public actual fun <T> MutableList<T>.shuffle(): Unit = shuffle(Random)

/**
 * Returns a new list with the elements of this list randomly shuffled.
 */
@SinceKotlin("1.2")
public actual fun <T> Iterable<T>.shuffled(): List<T> = toMutableList().apply { shuffle() }

/**
 * Sorts elements in the list in-place according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Collections.Sorting.sortMutableList
 */
public actual fun <T : Comparable<T>> MutableList<T>.sort(): Unit {
    collectionsSort(this, naturalOrder())
}

/**
 * Sorts elements in the list in-place according to the order specified with [comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Collections.Sorting.sortMutableListWith
 */
public actual fun <T> MutableList<T>.sortWith(comparator: Comparator<in T>): Unit {
    collectionsSort(this, comparator)
}

private fun <T> collectionsSort(list: MutableList<T>, comparator: Comparator<in T>) {
    if (list.size <= 1) return

    konst array = copyToArray(list)
    sortArrayWith(array, comparator)

    for (i in 0 until array.size) {
        list[i] = array[i]
    }
}

internal actual fun <T> arrayOfNulls(reference: Array<T>, size: Int): Array<T> {
    return arrayOfNulls<Any>(size).unsafeCast<Array<T>>()
}

@SinceKotlin("1.3")
@PublishedApi
@JsName("arrayCopy")
internal fun <T> arrayCopy(source: Array<out T>, destination: Array<in T>, destinationOffset: Int, startIndex: Int, endIndex: Int) {
    AbstractList.checkRangeIndexes(startIndex, endIndex, source.size)
    konst rangeSize = endIndex - startIndex
    AbstractList.checkRangeIndexes(destinationOffset, destinationOffset + rangeSize, destination.size)

    if (arrayBufferIsView(destination) && arrayBufferIsView(source)) {
        konst subrange = source.asDynamic().subarray(startIndex, endIndex)
        destination.asDynamic().set(subrange, destinationOffset)
    } else {
        if (source !== destination || destinationOffset <= startIndex) {
            for (index in 0 until rangeSize) {
                destination[destinationOffset + index] = source[startIndex + index]
            }
        } else {
            for (index in rangeSize - 1 downTo 0) {
                destination[destinationOffset + index] = source[startIndex + index]
            }
        }
    }
}

// no singleton map implementation in js, return map as is
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <K, V> Map<K, V>.toSingletonMapOrSelf(): Map<K, V> = this

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <K, V> Map<out K, V>.toSingletonMap(): Map<K, V> = this.toMutableMap()


@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <T> Array<out T>.copyToArrayOfAny(isVarargs: Boolean): Array<out Any?> =
    if (isVarargs)
    // no need to copy vararg array in JS
        this
    else
        this.copyOf()



@PublishedApi
internal actual fun checkIndexOverflow(index: Int): Int {
    if (index < 0) {
        throwIndexOverflow()
    }
    return index
}

@PublishedApi
internal actual fun checkCountOverflow(count: Int): Int {
    if (count < 0) {
        throwCountOverflow()
    }
    return count
}


/**
 * JS map and set implementations do not make use of capacities or load factors.
 */
@PublishedApi
internal actual fun mapCapacity(expectedSize: Int) = expectedSize

/**
 * Checks a collection builder function capacity argument.
 * In JS no konstidation is made in Map/Set constructor yet.
 */
@SinceKotlin("1.3")
@PublishedApi
internal fun checkBuilderCapacity(capacity: Int) {
    require(capacity >= 0) { "capacity must be non-negative." }
}

/**
 * Returns a new read-only map, mapping only the specified key to the
 * specified konstue.
 *
 * @sample samples.collections.Maps.Instantiation.mapFromPairs
 */
public actual fun <K, V> mapOf(pair: Pair<K, V>): Map<K, V> = hashMapOf(pair)