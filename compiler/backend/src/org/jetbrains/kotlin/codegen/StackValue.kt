/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class CoercionValue(
    konst konstue: StackValue,
    private konst castType: Type,
    private konst castKotlinType: KotlinType?,
    private konst underlyingKotlinType: KotlinType? // type of the underlying parameter for inline class
) : StackValue(castType, castKotlinType, konstue.canHaveSideEffects()) {

    override fun putSelector(type: Type, kotlinType: KotlinType?, v: InstructionAdapter) {
        konstue.putSelector(konstue.type, konstue.kotlinType, v)

        // consider the following example:

        // inline class AsAny(konst a: Any)
        // konst a = AsAny(1)
        //
        // Here we should coerce `Int` (1) to `Any` and remember that resulting type is inline class type `AsAny` (not `Any`)
        StackValue.coerce(konstue.type, konstue.kotlinType, castType, underlyingKotlinType ?: castKotlinType, v)
        StackValue.coerce(castType, castKotlinType, type, kotlinType, v)
    }

    override fun storeSelector(topOfStackType: Type, topOfStackKotlinType: KotlinType?, v: InstructionAdapter) {
        konstue.storeSelector(topOfStackType, topOfStackKotlinType, v)
    }

    override fun putReceiver(v: InstructionAdapter, isRead: Boolean) {
        konstue.putReceiver(v, isRead)
    }

    override fun isNonStaticAccess(isRead: Boolean): Boolean {
        return konstue.isNonStaticAccess(isRead)
    }
}


class StackValueWithLeaveTask(
    konst stackValue: StackValue,
    konst leaveTasks: (StackValue) -> Unit
) : StackValue(stackValue.type, stackValue.kotlinType) {

    override fun putReceiver(v: InstructionAdapter, isRead: Boolean) {
        stackValue.putReceiver(v, isRead)
    }

    override fun putSelector(type: Type, kotlinType: KotlinType?, v: InstructionAdapter) {
        stackValue.putSelector(type, kotlinType, v)
        leaveTasks(stackValue)
    }
}

open class OperationStackValue(
    resultType: Type,
    resultKotlinType: KotlinType?,
    konst lambda: (v: InstructionAdapter) -> Unit
) : StackValue(resultType, resultKotlinType) {

    override fun putSelector(type: Type, kotlinType: KotlinType?, v: InstructionAdapter) {
        lambda(v)
        coerceTo(type, kotlinType, v)
    }
}

class FunctionCallStackValue(
    resultType: Type,
    resultKotlinType: KotlinType?,
    lambda: (v: InstructionAdapter) -> Unit
) : OperationStackValue(resultType, resultKotlinType, lambda)
