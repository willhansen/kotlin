/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.codegen.BlockInfo
import org.jetbrains.kotlin.backend.jvm.codegen.ClassCodegen
import org.jetbrains.kotlin.backend.jvm.codegen.ExpressionCodegen
import org.jetbrains.kotlin.backend.jvm.mapping.mapClass
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

open class IrIntrinsicFunction(
    konst expression: IrFunctionAccessExpression,
    konst signature: JvmMethodSignature,
    konst classCodegen: ClassCodegen,
    konst argsTypes: List<Type> = expression.argTypes(classCodegen)
) : Callable {
    override konst owner: Type
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override konst dispatchReceiverType: Type?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override konst dispatchReceiverKotlinType: KotlinType?
        get() = null
    override konst extensionReceiverType: Type?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override konst extensionReceiverKotlinType: KotlinType?
        get() = null
    override konst generateCalleeType: Type?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override konst konstueParameterTypes: List<Type>
        get() = signature.konstueParameters.map { it.asmType }
    override konst parameterTypes: Array<Type>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override konst returnType: Type
        get() = signature.returnType
    override konst returnKotlinType: KotlinType?
        get() = null

    override fun isStaticCall(): Boolean {
        return false
    }

    override fun genInvokeInstruction(v: InstructionAdapter) {
        TODO("not implemented for $this")
    }

    open fun genInvokeInstructionWithResult(v: InstructionAdapter): Type {
        genInvokeInstruction(v)
        return returnType
    }

    open fun invoke(
        v: InstructionAdapter,
        codegen: ExpressionCodegen,
        data: BlockInfo,
        expression: IrFunctionAccessExpression
    ): StackValue {
        loadArguments(codegen, data)
        codegen.markLineNumber(expression)
        return StackValue.onStack(genInvokeInstructionWithResult(v))
    }

    private fun loadArguments(codegen: ExpressionCodegen, data: BlockInfo) {
        var offset = 0
        expression.dispatchReceiver?.let { genArg(it, codegen, offset++, data) }
        expression.extensionReceiver?.let { genArg(it, codegen, offset++, data) }
        for ((i, konstueParameter) in expression.symbol.owner.konstueParameters.withIndex()) {
            konst argument = expression.getValueArgument(i)
            when {
                argument != null ->
                    genArg(argument, codegen, i + offset, data)
                konstueParameter.isVararg -> {
                    // TODO: is there an easier way to get the substituted type of an empty vararg argument?
                    konst arrayType = codegen.typeMapper.mapType(
                        konstueParameter.type.substitute(expression.symbol.owner.typeParameters, expression.typeArguments)
                    )
                    StackValue.operation(arrayType) {
                        it.aconst(0)
                        it.newarray(AsmUtil.correctElementType(arrayType))
                    }.put(arrayType, codegen.mv)
                }
                else -> error("Unknown parameter ${konstueParameter.name} in: ${expression.dump()}")
            }
        }
    }

    private fun genArg(expression: IrExpression, codegen: ExpressionCodegen, index: Int, data: BlockInfo) {
        codegen.gen(expression, argsTypes[index], expression.type, data)
    }

    private konst IrFunctionAccessExpression.typeArguments: List<IrType>
        get() = (0 until typeArgumentsCount).map { getTypeArgument(it)!! }

    companion object {
        fun create(
            expression: IrFunctionAccessExpression,
            signature: JvmMethodSignature,
            classCodegen: ClassCodegen,
            argsTypes: List<Type> = expression.argTypes(classCodegen),
            invokeInstruction: IrIntrinsicFunction.(InstructionAdapter) -> Unit
        ): IrIntrinsicFunction {
            return object : IrIntrinsicFunction(expression, signature, classCodegen, argsTypes) {

                override fun genInvokeInstruction(v: InstructionAdapter) = invokeInstruction(v)
            }
        }

        fun createWithResult(
            expression: IrFunctionAccessExpression, signature: JvmMethodSignature,
            classCodegen: ClassCodegen,
            argsTypes: List<Type> = expression.argTypes(classCodegen ),
            invokeInstruction: IrIntrinsicFunction.(InstructionAdapter) -> Type
        ): IrIntrinsicFunction {
            return object : IrIntrinsicFunction(expression, signature, classCodegen, argsTypes) {

                override fun genInvokeInstructionWithResult(v: InstructionAdapter) = invokeInstruction(v)
            }
        }

        fun create(
            expression: IrFunctionAccessExpression,
            signature: JvmMethodSignature,
            classCodegen: ClassCodegen,
            type: Type,
            invokeInstruction: IrIntrinsicFunction.(InstructionAdapter) -> Unit
        ): IrIntrinsicFunction {
            return create(expression, signature, classCodegen, listOf(type), invokeInstruction)
        }
    }
}

fun IrFunctionAccessExpression.argTypes(classCodegen: ClassCodegen): ArrayList<Type> {
    konst callee = symbol.owner
    konst signature = classCodegen.methodSignatureMapper.mapSignatureSkipGeneric(callee)
    return arrayListOf<Type>().apply {
        if (dispatchReceiver != null) {
            add(classCodegen.typeMapper.mapClass(callee.parentAsClass))
        }
        addAll(signature.asmMethod.argumentTypes)
    }
}
