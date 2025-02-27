/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("NON_ABSTRACT_FUNCTION_WITH_NO_BODY", "UNUSED_PARAMETER")

package kotlin

/**
 * Represents a konstue which is either `true` or `false`. On the JVM, non-nullable konstues of this type are
 * represented as konstues of the primitive type `boolean`.
 */
public class Boolean private constructor() : Comparable<Boolean> {
    /**
     * Returns the inverse of this boolean.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun not(): Boolean

    /**
     * Performs a logical `and` operation between this Boolean and the [other] one. Unlike the `&&` operator,
     * this function does not perform short-circuit ekonstuation. Both `this` and [other] will always be ekonstuated.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun and(other: Boolean): Boolean

    /**
     * Performs a logical `or` operation between this Boolean and the [other] one. Unlike the `||` operator,
     * this function does not perform short-circuit ekonstuation. Both `this` and [other] will always be ekonstuated.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun or(other: Boolean): Boolean

    /**
     * Performs a logical `xor` operation between this Boolean and the [other] one.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun xor(other: Boolean): Boolean

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun compareTo(other: Boolean): Int


    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @SinceKotlin("1.3")
    companion object {}
}
