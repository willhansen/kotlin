/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.CallableMethod
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

open class IntrinsicCallable(
        override konst returnType: Type,
        override konst konstueParameterTypes: List<Type>,
        override konst dispatchReceiverType: Type?,
        override konst extensionReceiverType: Type?,
        private konst invoke: IntrinsicCallable.(v: InstructionAdapter) -> Unit = { throw UnsupportedOperationException() }
) : Callable {

    constructor(
            callable: CallableMethod,
            invoke: IntrinsicCallable.(v: InstructionAdapter) -> Unit = {}
    ) : this(
            callable.returnType,
            callable.konstueParameterTypes,
            callable.dispatchReceiverType,
            callable.extensionReceiverType,
            invoke
    )

    override fun genInvokeInstruction(v: InstructionAdapter) {
        invokeIntrinsic(v)
    }

    open fun invokeIntrinsic(v: InstructionAdapter) {
        invoke(v)
    }

    override konst parameterTypes: Array<Type>
        get() = throw UnsupportedOperationException()

    override konst dispatchReceiverKotlinType: KotlinType?
        get() = null

    override konst extensionReceiverKotlinType: KotlinType?
        get() = null

    override konst returnKotlinType: KotlinType?
        get() = null

    override fun isStaticCall() = false

    override konst generateCalleeType: Type?
        get() = null

    override konst owner: Type
        get() = throw UnsupportedOperationException()

    fun calcReceiverType(): Type =
            extensionReceiverType ?: dispatchReceiverType!!
}

fun createBinaryIntrinsicCallable(
        returnType: Type,
        konstueParameterType: Type,
        thisType: Type? = null,
        receiverType: Type? = null,
        lambda: IntrinsicCallable.(v: InstructionAdapter) -> Unit
): IntrinsicCallable {
    assert(AsmUtil.isPrimitive(returnType)) { "Return type of BinaryOp intrinsic should be of primitive type: $returnType" }

    return object : IntrinsicCallable(returnType, listOf(konstueParameterType), thisType, receiverType) {
        override fun invokeIntrinsic(v: InstructionAdapter) {
            lambda(v)
        }
    }
}

fun createUnaryIntrinsicCallable(
        callable: CallableMethod,
        newReturnType: Type? = null,
        needPrimitiveCheck: Boolean = false,
        newThisType: Type? = null,
        invoke: IntrinsicCallable.(v: InstructionAdapter) -> Unit
): IntrinsicCallable {
    konst intrinsic = IntrinsicCallable(
            newReturnType ?: callable.returnType,
            callable.konstueParameterTypes,
            newThisType ?: callable.dispatchReceiverType,
            callable.extensionReceiverType,
            invoke
    )
    assert(intrinsic.konstueParameterTypes.isEmpty()) { "Unary operation should not have any parameters" }
    if (needPrimitiveCheck) {
        assert(AsmUtil.isPrimitive(intrinsic.returnType)) {
            "Return type of UnaryPlus intrinsic should be of primitive type: ${intrinsic.returnType}"
        }
    }
    return intrinsic
}

fun createIntrinsicCallable(
        callable: CallableMethod,
        invoke: IntrinsicCallable.(v: InstructionAdapter) -> Unit
): IntrinsicCallable {
    return IntrinsicCallable(callable, invoke)
}

/**
 * A marker interface that signifies that this [IntrinsicCallable] instance generates the receiver of the intrinsic function call itself.
 */
interface IntrinsicWithSpecialReceiver : Callable
