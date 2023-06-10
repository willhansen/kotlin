/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

sealed class IrConstKind<T>(konst asString: kotlin.String) {
    @Suppress("UNCHECKED_CAST")
    fun konstueOf(aConst: IrConst<*>) =
        (aConst as IrConst<T>).konstue

    object Null : IrConstKind<Nothing?>("Null")
    object Boolean : IrConstKind<kotlin.Boolean>("Boolean")
    object Char : IrConstKind<kotlin.Char>("Char")
    object Byte : IrConstKind<kotlin.Byte>("Byte")
    object Short : IrConstKind<kotlin.Short>("Short")
    object Int : IrConstKind<kotlin.Int>("Int")
    object Long : IrConstKind<kotlin.Long>("Long")
    object String : IrConstKind<kotlin.String>("String")
    object Float : IrConstKind<kotlin.Float>("Float")
    object Double : IrConstKind<kotlin.Double>("Double")

    override fun toString() = asString
}
