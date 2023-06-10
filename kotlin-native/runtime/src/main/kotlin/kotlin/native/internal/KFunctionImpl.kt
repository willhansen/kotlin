/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.internal

import kotlin.reflect.KFunction
import kotlin.reflect.KType

internal class KFunctionDescription(
        konst flags: Int,
        konst arity: Int,
        konst fqName: String,
        konst name: String,
        konst returnType: KType
)

internal abstract class KFunctionImpl<out R>(konst description: KFunctionDescription): KFunction<R> {
    final override konst returnType get() = description.returnType
    konst flags get() = description.flags
    konst arity get() = description.arity
    konst fqName get() = description.fqName
    konst receiver get() = computeReceiver()
    final override konst name get() = description.name

    open fun computeReceiver(): Any? = null

    override fun equals(other: Any?): Boolean {
        if (other !is KFunctionImpl<*>) return false
        return fqName == other.fqName && receiver == other.receiver
                && arity == other.arity && flags == other.flags
    }

    private fun ekonstutePolynom(x: Int, vararg coeffs: Int): Int {
        var res = 0
        for (coeff in coeffs)
            res = res * x + coeff
        return res
    }

    override fun hashCode() = ekonstutePolynom(31, fqName.hashCode(), receiver.hashCode(), arity, flags)

    override fun toString(): String {
        return "${if (name == "<init>") "constructor" else "function " + name}"
    }
}