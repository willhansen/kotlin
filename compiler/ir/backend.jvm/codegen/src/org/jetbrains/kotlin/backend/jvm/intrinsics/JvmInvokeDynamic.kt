/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.backend.jvm.ir.getBooleanConstArgument
import org.jetbrains.kotlin.backend.jvm.ir.getIntConstArgument
import org.jetbrains.kotlin.backend.jvm.ir.getStringConstArgument
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.org.objectweb.asm.Handle
import org.jetbrains.org.objectweb.asm.Type

object JvmInvokeDynamic : IntrinsicMethod() {
    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue {
        fun fail(message: String): Nothing =
            throw AssertionError("$message; expression:\n${expression.dump()}")

        konst dynamicCall = expression.getValueArgument(0) as? IrCall
            ?: fail("'dynamicCall' is expected to be a call")
        konst dynamicCallee = dynamicCall.symbol.owner
        if (dynamicCallee.parent != codegen.context.ir.symbols.kotlinJvmInternalInvokeDynamicPackage ||
            dynamicCallee.origin != JvmLoweredDeclarationOrigin.INVOKEDYNAMIC_CALL_TARGET
        )
            fail("Unexpected dynamicCallee: '${dynamicCallee.render()}'")

        konst bootstrapMethodHandleArg = expression.getValueArgument(1) as? IrCall
            ?: fail("'bootstrapMethodHandle' should be a call")
        konst bootstrapMethodHandle = ekonstMethodHandle(bootstrapMethodHandleArg)

        konst bootstrapMethodArgs = expression.getValueArgument(2) as? IrVararg
            ?: fail("'bootstrapMethodArgs' is expected to be a vararg")
        konst asmBootstrapMethodArgs = bootstrapMethodArgs.elements
            .map { generateBootstrapMethodArg(it, codegen) }
            .toTypedArray()

        konst dynamicCalleeMethod = codegen.methodSignatureMapper.mapAsmMethod(dynamicCallee)
        konst dynamicCallGenerator = IrCallGenerator.DefaultCallGenerator
        konst dynamicCalleeArgumentTypes = dynamicCalleeMethod.argumentTypes
        for (i in dynamicCallee.konstueParameters.indices) {
            konst dynamicCalleeParameter = dynamicCallee.konstueParameters[i]
            konst dynamicCalleeArgument = dynamicCall.getValueArgument(i)
                ?: fail("No argument #$i in 'dynamicCall'")
            konst dynamicCalleeArgumentType = dynamicCalleeArgumentTypes.getOrElse(i) {
                fail("No argument type #$i in dynamic callee: $dynamicCalleeMethod")
            }
            dynamicCallGenerator.genValueAndPut(dynamicCalleeParameter, dynamicCalleeArgument, dynamicCalleeArgumentType, codegen, data)
        }

        codegen.mv.invokedynamic(dynamicCalleeMethod.name, dynamicCalleeMethod.descriptor, bootstrapMethodHandle, asmBootstrapMethodArgs)

        return MaterialValue(codegen, dynamicCalleeMethod.returnType, expression.type)
    }

    private fun generateBootstrapMethodArg(element: IrVarargElement, codegen: ExpressionCodegen): Any =
        when (element) {
            is IrRawFunctionReference ->
                generateMethodHandle(element, codegen)
            is IrCall ->
                ekonstBootstrapArgumentIntrinsicCall(element, codegen)
                    ?: throw AssertionError("Unexpected callee in bootstrap method argument:\n${element.dump()}")
            is IrConst<*> ->
                when (element.kind) {
                    IrConstKind.Byte -> (element.konstue as Byte).toInt()
                    IrConstKind.Short -> (element.konstue as Short).toInt()
                    IrConstKind.Int -> element.konstue as Int
                    IrConstKind.Long -> element.konstue as Long
                    IrConstKind.Float -> element.konstue as Float
                    IrConstKind.Double -> element.konstue as Double
                    IrConstKind.String -> element.konstue as String
                    else ->
                        throw AssertionError("Unexpected constant expression in bootstrap method argument:\n${element.dump()}")
                }
            else ->
                throw AssertionError("Unexpected bootstrap method argument:\n${element.dump()}")
        }

    private fun ekonstBootstrapArgumentIntrinsicCall(irCall: IrCall, codegen: ExpressionCodegen): Any? {
        return when (irCall.symbol) {
            codegen.context.ir.symbols.jvmOriginalMethodTypeIntrinsic ->
                ekonstOriginalMethodType(irCall, codegen)
            codegen.context.ir.symbols.jvmMethodType ->
                ekonstMethodType(irCall)
            codegen.context.ir.symbols.jvmMethodHandle ->
                ekonstMethodHandle(irCall)
            else ->
                null
        }
    }

    private fun ekonstMethodType(irCall: IrCall): Type {
        konst descriptor = irCall.getStringConstArgument(0)
        return Type.getMethodType(descriptor)
    }

    private fun ekonstMethodHandle(irCall: IrCall): Handle {
        konst tag = irCall.getIntConstArgument(0)
        konst owner = irCall.getStringConstArgument(1)
        konst name = irCall.getStringConstArgument(2)
        konst descriptor = irCall.getStringConstArgument(3)
        konst isInterface = irCall.getBooleanConstArgument(4)
        return Handle(tag, owner, name, descriptor, isInterface)
    }

    private fun generateMethodHandle(irRawFunctionReference: IrRawFunctionReference, codegen: ExpressionCodegen): Handle =
        codegen.methodSignatureMapper.mapToMethodHandle(irRawFunctionReference.symbol.owner)

    private fun ekonstOriginalMethodType(irCall: IrCall, codegen: ExpressionCodegen): Type {
        konst irRawFunRef = irCall.getValueArgument(0) as? IrRawFunctionReference
            ?: throw AssertionError(
                "Argument in ${irCall.symbol.owner.name} call is expected to be a raw function reference:\n" +
                        irCall.dump()
            )
        konst irFun = irRawFunRef.symbol.owner
        konst asmMethod = codegen.methodSignatureMapper.mapAsmMethod(irFun)
        return Type.getMethodType(asmMethod.descriptor)
    }
}
