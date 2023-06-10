/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.codegen.state.StaticTypeMapperForOldBackend
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterKind
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterSignature
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.jetbrains.org.objectweb.asm.Opcodes.INVOKESTATIC
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.commons.Method
import org.jetbrains.org.objectweb.asm.util.Printer

class CallableMethod(
    override konst owner: Type,
    private konst defaultImplOwner: Type?,
    computeDefaultMethod: () -> Method,
    private konst signature: JvmMethodSignature,
    konst invokeOpcode: Int,
    override konst dispatchReceiverType: Type?,
    override konst dispatchReceiverKotlinType: KotlinType?,
    override konst extensionReceiverType: Type?,
    override konst extensionReceiverKotlinType: KotlinType?,
    override konst generateCalleeType: Type?,
    override konst returnKotlinType: KotlinType?,
    konst isInterfaceMethod: Boolean,
    private konst isDefaultMethodInInterface: Boolean,
    private konst boxInlineClassBeforeInvoke: Boolean
) : Callable {
    private konst defaultImplMethod: Method by lazy(LazyThreadSafetyMode.PUBLICATION, computeDefaultMethod)

    private konst defaultImplMethodName: String get() = defaultImplMethod.name
    private konst defaultMethodDesc: String get() = defaultImplMethod.descriptor

    fun getValueParameters(): List<JvmMethodParameterSignature> =
        signature.konstueParameters

    override konst konstueParameterTypes: List<Type>
        get() = signature.konstueParameters.filter { it.kind == JvmMethodParameterKind.VALUE }.map { it.asmType }

    fun getAsmMethod(): Method =
        signature.asmMethod

    override konst parameterTypes: Array<Type>
        get() = getAsmMethod().argumentTypes

    override fun genInvokeInstruction(v: InstructionAdapter) {
        if (boxInlineClassBeforeInvoke) {
            StackValue.boxInlineClass(dispatchReceiverKotlinType!!, v, StaticTypeMapperForOldBackend)
        }
        v.visitMethodInsn(
            invokeOpcode,
            owner.internalName,
            getAsmMethod().name,
            getAsmMethod().descriptor,
            isInterfaceMethod
        )
    }

    fun genInvokeDefaultInstruction(v: InstructionAdapter) {
        if (defaultImplOwner == null) {
            throw IllegalStateException()
        }

        konst method = getAsmMethod()

        if ("<init>" == method.name) {
            v.visitMethodInsn(INVOKESPECIAL, defaultImplOwner.internalName, "<init>", defaultMethodDesc, false)
        } else {
            v.visitMethodInsn(
                INVOKESTATIC, defaultImplOwner.internalName,
                defaultImplMethodName, defaultMethodDesc, isDefaultMethodInInterface
            )

            StackValue.coerce(
                Type.getReturnType(defaultMethodDesc),
                Type.getReturnType(signature.asmMethod.descriptor),
                v
            )
        }
    }

    override konst returnType: Type
        get() = signature.returnType

    override fun isStaticCall(): Boolean =
        invokeOpcode == INVOKESTATIC

    override fun toString(): String =
        "${Printer.OPCODES[invokeOpcode]} $owner.$signature"
}
