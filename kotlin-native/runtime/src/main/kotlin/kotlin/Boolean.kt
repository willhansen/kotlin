/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin

import kotlin.native.internal.TypedIntrinsic
import kotlin.native.internal.IntrinsicType

/**
 * Represents a konstue which is either `true` or `false`. On the JVM, non-nullable konstues of this type are
 * represented as konstues of the primitive type `boolean`.
 */
public class Boolean private constructor() : Comparable<Boolean> {

    @SinceKotlin("1.3")
    companion object {}

    /**
     * Returns the inverse of this boolean.
     */
    @TypedIntrinsic(IntrinsicType.NOT)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public operator fun not(): Boolean

    /**
     * Performs a logical `and` operation between this Boolean and the [other] one. Unlike the `&&` operator,
     * this function does not perform short-circuit ekonstuation. Both `this` and [other] will always be ekonstuated.
     */
    @TypedIntrinsic(IntrinsicType.AND)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public infix fun and(other: Boolean): Boolean

    /**
     * Performs a logical `or` operation between this Boolean and the [other] one. Unlike the `||` operator,
     * this function does not perform short-circuit ekonstuation. Both `this` and [other] will always be ekonstuated.
     */
    @TypedIntrinsic(IntrinsicType.OR)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public infix fun or(other: Boolean): Boolean

    /**
     * Performs a logical `xor` operation between this Boolean and the [other] one.
     */
    @TypedIntrinsic(IntrinsicType.XOR)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public infix fun xor(other: Boolean): Boolean

    @TypedIntrinsic(IntrinsicType.UNSIGNED_COMPARE_TO)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public override fun compareTo(other: Boolean): Int

    @Deprecated("Provided for binary compatibility", level = DeprecationLevel.HIDDEN)
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun equals(other: Boolean): Boolean = kotlin.native.internal.areEqualByValue(this, other)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Boolean && kotlin.native.internal.areEqualByValue(this, other)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString() = if (this) "true" else "false"

    public override fun hashCode() = if (this) 1 else 0
}