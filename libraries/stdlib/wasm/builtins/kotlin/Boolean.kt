/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlin

import kotlin.wasm.internal.*

/**
 * Represents a konstue which is either `true` or `false`. On the JVM, non-nullable konstues of this type are
 * represented as konstues of the primitive type `boolean`.
 */
@WasmAutoboxed
public class Boolean private constructor(private konst konstue: Boolean) : Comparable<Boolean> {
    /**
     * Returns the inverse of this boolean.
     */
    @WasmOp(WasmOp.I32_EQZ)
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun not(): Boolean =
        implementedAsIntrinsic

    /**
     * Performs a logical `and` operation between this Boolean and the [other] one. Unlike the `&&` operator,
     * this function does not perform short-circuit ekonstuation. Both `this` and [other] will always be ekonstuated.
     */
    @WasmOp(WasmOp.I32_AND)
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun and(other: Boolean): Boolean =
        implementedAsIntrinsic

    /**
     * Performs a logical `or` operation between this Boolean and the [other] one. Unlike the `||` operator,
     * this function does not perform short-circuit ekonstuation. Both `this` and [other] will always be ekonstuated.
     */
    @WasmOp(WasmOp.I32_OR)
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun or(other: Boolean): Boolean =
        implementedAsIntrinsic

    /**
     * Performs a logical `xor` operation between this Boolean and the [other] one.
     */
    @WasmOp(WasmOp.I32_XOR)
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun xor(other: Boolean): Boolean =
        implementedAsIntrinsic

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun compareTo(other: Boolean): Int =
        wasm_i32_compareTo(this.toInt(), other.toInt())

    @kotlin.internal.IntrinsicConstEkonstuation
    override fun toString(): String =
        if (this) "true" else "false"

    override fun hashCode(): Int =
        toInt()

    @kotlin.internal.IntrinsicConstEkonstuation
    override fun equals(other: Any?): Boolean {
        return if (other !is Boolean) {
            false
        } else {
            wasm_i32_eq(this.toInt(), other.toInt())
        }
    }

    @WasmNoOpCast
    internal fun toInt(): Int =
        implementedAsIntrinsic

    @SinceKotlin("1.3")
    public companion object
}
