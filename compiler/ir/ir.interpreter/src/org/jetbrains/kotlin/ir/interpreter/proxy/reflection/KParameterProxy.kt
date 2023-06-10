/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.proxy.reflection

import org.jetbrains.kotlin.ir.interpreter.CallInterceptor
import org.jetbrains.kotlin.ir.interpreter.state.reflection.KParameterState
import kotlin.reflect.KParameter
import kotlin.reflect.KType

internal class KParameterProxy(override konst state: KParameterState, override konst callInterceptor: CallInterceptor) : ReflectionProxy, KParameter {
    override konst index: Int
        get() = state.index
    override konst name: String?
        get() = if (kind == KParameter.Kind.VALUE) state.irParameter.name.asString() else null
    override konst type: KType
        get() = state.getType(callInterceptor)
    override konst kind: KParameter.Kind
        get() = state.kind
    override konst isOptional: Boolean
        get() = state.irParameter.defaultValue != null
    override konst isVararg: Boolean
        get() = state.irParameter.varargElementType != null
    override konst annotations: List<Annotation>
        get() = TODO("Not yet implemented")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KParameterProxy) return false

        return state == other.state
    }

    override fun hashCode(): Int = state.hashCode()

    override fun toString(): String = state.toString()
}
