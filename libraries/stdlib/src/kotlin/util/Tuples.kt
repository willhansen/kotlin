/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmName("TuplesKt")

package kotlin


/**
 * Represents a generic pair of two konstues.
 *
 * There is no meaning attached to konstues in this class, it can be used for any purpose.
 * Pair exhibits konstue semantics, i.e. two pairs are equal if both components are equal.
 *
 * An example of decomposing it into konstues:
 * @sample samples.misc.Tuples.pairDestructuring
 *
 * @param A type of the first konstue.
 * @param B type of the second konstue.
 * @property first First konstue.
 * @property second Second konstue.
 * @constructor Creates a new instance of Pair.
 */
public data class Pair<out A, out B>(
    public konst first: A,
    public konst second: B
) : Serializable {

    /**
     * Returns string representation of the [Pair] including its [first] and [second] konstues.
     */
    public override fun toString(): String = "($first, $second)"
}

/**
 * Creates a tuple of type [Pair] from this and [that].
 *
 * This can be useful for creating [Map] literals with less noise, for example:
 * @sample samples.collections.Maps.Instantiation.mapFromPairs
 */
public infix fun <A, B> A.to(that: B): Pair<A, B> = Pair(this, that)

/**
 * Converts this pair into a list.
 * @sample samples.misc.Tuples.pairToList
 */
public fun <T> Pair<T, T>.toList(): List<T> = listOf(first, second)

/**
 * Represents a triad of konstues
 *
 * There is no meaning attached to konstues in this class, it can be used for any purpose.
 * Triple exhibits konstue semantics, i.e. two triples are equal if all three components are equal.
 * An example of decomposing it into konstues:
 * @sample samples.misc.Tuples.tripleDestructuring
 *
 * @param A type of the first konstue.
 * @param B type of the second konstue.
 * @param C type of the third konstue.
 * @property first First konstue.
 * @property second Second konstue.
 * @property third Third konstue.
 */
public data class Triple<out A, out B, out C>(
    public konst first: A,
    public konst second: B,
    public konst third: C
) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second] and [third] konstues.
     */
    public override fun toString(): String = "($first, $second, $third)"
}

/**
 * Converts this triple into a list.
 * @sample samples.misc.Tuples.tripleToList
 */
public fun <T> Triple<T, T, T>.toList(): List<T> = listOf(first, second, third)
