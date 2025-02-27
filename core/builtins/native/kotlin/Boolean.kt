/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @SinceKotlin("1.3")
    companion object {}
}
