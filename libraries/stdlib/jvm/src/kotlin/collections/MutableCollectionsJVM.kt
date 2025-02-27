/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("CollectionsKt")

package kotlin.collections


@Deprecated("Use sortWith(comparator) instead.", ReplaceWith("this.sortWith(comparator)"), level = DeprecationLevel.ERROR)
@kotlin.internal.InlineOnly
@Suppress("UNUSED_PARAMETER", "EXTENSION_SHADOWED_BY_MEMBER")
public inline fun <T> MutableList<T>.sort(comparator: Comparator<in T>): Unit = throw NotImplementedError()

@Deprecated("Use sortWith(Comparator(comparison)) instead.", ReplaceWith("this.sortWith(Comparator(comparison))"), level = DeprecationLevel.ERROR)
@kotlin.internal.InlineOnly
@Suppress("UNUSED_PARAMETER")
public inline fun <T> MutableList<T>.sort(comparison: (T, T) -> Int): Unit = throw NotImplementedError()


/**
 * Sorts elements in the list in-place according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Collections.Sorting.sortMutableList
 */
public actual fun <T : Comparable<T>> MutableList<T>.sort(): Unit {
    if (size > 1) java.util.Collections.sort(this)
}

/**
 * Sorts elements in the list in-place according to the order specified with [comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 *
 * @sample samples.collections.Collections.Sorting.sortMutableListWith
 */
public actual fun <T> MutableList<T>.sortWith(comparator: Comparator<in T>): Unit {
    if (size > 1) java.util.Collections.sort(this, comparator)
}

/**
 * Fills the list with the provided [konstue].
 *
 * Each element in the list gets replaced with the [konstue].
 */
@kotlin.internal.InlineOnly
@SinceKotlin("1.2")
public actual inline fun <T> MutableList<T>.fill(konstue: T) {
    java.util.Collections.fill(this, konstue)
}


/**
 * Randomly shuffles elements in this mutable list.
 */
@kotlin.internal.InlineOnly
@SinceKotlin("1.2")
public actual inline fun <T> MutableList<T>.shuffle() {
    java.util.Collections.shuffle(this)
}

/**
 * Randomly shuffles elements in this mutable list using the specified [random] instance as the source of randomness.
 */
@kotlin.internal.InlineOnly
@SinceKotlin("1.2")
public inline fun <T> MutableList<T>.shuffle(random: java.util.Random) {
    java.util.Collections.shuffle(this, random)
}
