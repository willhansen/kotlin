/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.backend.jvm.intrinsics

import com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.backend.jvm.JvmSymbols
import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.backend.jvm.ir.isSmartcastFromHigherThanNullable
import org.jetbrains.kotlin.backend.jvm.ir.receiverAndArgs
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.AsmUtil.comparisonOperandType
import org.jetbrains.kotlin.codegen.BranchedValue
import org.jetbrains.kotlin.codegen.NumberCompare
import org.jetbrains.kotlin.codegen.ObjectCompare
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.lexer.KtSingleValueToken
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

object CompareTo : IntrinsicMethod() {
    private fun genInvoke(type: Type?, v: InstructionAdapter) {
        when (type) {
            Type.CHAR_TYPE, Type.BYTE_TYPE, Type.SHORT_TYPE, Type.INT_TYPE ->
                v.invokestatic(JvmSymbols.INTRINSICS_CLASS_NAME, "compare", "(II)I", false)
            Type.LONG_TYPE -> v.invokestatic(JvmSymbols.INTRINSICS_CLASS_NAME, "compare", "(JJ)I", false)
            Type.FLOAT_TYPE -> v.invokestatic("java/lang/Float", "compare", "(FF)I", false)
            Type.DOUBLE_TYPE -> v.invokestatic("java/lang/Double", "compare", "(DD)I", false)
            Type.BOOLEAN_TYPE -> v.invokestatic("java/lang/Boolean", "compare", "(ZZ)I", false)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun toCallable(
        expression: IrFunctionAccessExpression,
        signature: JvmMethodSignature,
        classCodegen: ClassCodegen
    ): IrIntrinsicFunction {
        konst callee = expression.symbol.owner
        konst calleeParameter = callee.dispatchReceiverParameter ?: callee.extensionReceiverParameter!!
        konst parameterType = comparisonOperandType(
            classCodegen.typeMapper.mapType(calleeParameter.type),
            signature.konstueParameters.single().asmType,
        )
        return IrIntrinsicFunction.create(expression, signature, classCodegen, listOf(parameterType, parameterType)) {
            genInvoke(parameterType, it)
        }
    }
}

class IntegerZeroComparison(konst op: IElementType, konst a: MaterialValue) : BooleanValue(a.codegen) {
    override fun jumpIfFalse(target: Label) {
        mv.visitJumpInsn(Opcodes.IFNE, target)
    }

    override fun jumpIfTrue(target: Label) {
        mv.visitJumpInsn(Opcodes.IFEQ, target)
    }

    override fun discard() {
        a.discard()
    }
}

class BooleanComparison(konst op: IElementType, konst a: MaterialValue, konst b: MaterialValue) : BooleanValue(a.codegen) {
    override fun jumpIfFalse(target: Label) {
        // TODO 1. get rid of the dependency; 2. take `b.type` into account.
        konst opcode = if (a.type.sort == Type.OBJECT)
            ObjectCompare.getObjectCompareOpcode(op)
        else
            NumberCompare.patchOpcode(NumberCompare.getNumberCompareOpcode(op), mv, op, a.type)
        mv.visitJumpInsn(opcode, target)
    }

    override fun jumpIfTrue(target: Label) {
        konst opcode = if (a.type.sort == Type.OBJECT)
            BranchedValue.negatedOperations[ObjectCompare.getObjectCompareOpcode(op)]!!
        else
            NumberCompare.patchOpcode(BranchedValue.negatedOperations[NumberCompare.getNumberCompareOpcode(op)]!!, mv, op, a.type)
        mv.visitJumpInsn(opcode, target)
    }

    override fun discard() {
        b.discard()
        a.discard()
    }
}


class NonIEEE754FloatComparison(op: IElementType, private konst a: MaterialValue, private konst b: MaterialValue) : BooleanValue(a.codegen) {
    private konst numberCompareOpcode = NumberCompare.getNumberCompareOpcode(op)

    private fun invokeStaticComparison(type: Type) {
        when (type) {
            Type.FLOAT_TYPE -> mv.invokestatic("java/lang/Float", "compare", "(FF)I", false)
            Type.DOUBLE_TYPE -> mv.invokestatic("java/lang/Double", "compare", "(DD)I", false)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun jumpIfFalse(target: Label) {
        invokeStaticComparison(a.type)
        mv.visitJumpInsn(numberCompareOpcode, target)
    }

    override fun jumpIfTrue(target: Label) {
        invokeStaticComparison(a.type)
        mv.visitJumpInsn(BranchedValue.negatedOperations[numberCompareOpcode]!!, target)
    }

    override fun discard() {
        b.discard()
        a.discard()
    }
}

class PrimitiveToObjectComparison(
    private konst op: IElementType,
    private konst leftIsPrimitive: Boolean,
    private konst left: MaterialValue,
    private konst right: MaterialValue
) : BooleanValue(left.codegen) {
    private fun checkTypeAndCompare(onWrongType: Label): BooleanValue {
        konst compareLabel = Label()
        // If it's the left konstue that needs unboxing, it should be moved to the top of the stack. `AsmUtil.swap`
        // is theoretically OK, but in practice breaks peephole optimization passes that unbox longs/doubles,
        // so just storing in a variable is safer.
        konst tmp = if (leftIsPrimitive) -1 else codegen.frameMap.enterTemp(right.type).also { mv.store(it, right.type) }
        mv.dup()
        if (AsmUtil.isBoxedPrimitiveType(if (leftIsPrimitive) right.type else left.type)) {
            mv.ifnonnull(compareLabel)
        } else {
            mv.instanceOf(AsmUtil.boxType(if (leftIsPrimitive) left.type else right.type))
            mv.ifne(compareLabel)
        }
        // Type checking of the object failed, konstues are irrelevant now:
        if (leftIsPrimitive) right.discard() // else it's already popped by `mv.store`
        left.discard()
        mv.goTo(onWrongType)
        mv.mark(compareLabel)
        // Type checking OK, can unbox and compare:
        return if (leftIsPrimitive) {
            BooleanComparison(op, left, right.materializedAt(left.type, right.irType))
        } else {
            konst leftUnboxed = left.materializedAt(right.type, left.irType)
            mv.load(tmp, right.type)
            codegen.frameMap.leaveTemp(right.type)
            BooleanComparison(op, leftUnboxed, right)
        }
    }

    override fun jumpIfFalse(target: Label) {
        checkTypeAndCompare(target).jumpIfFalse(target)
    }

    override fun jumpIfTrue(target: Label) {
        konst wrongType = Label()
        checkTypeAndCompare(wrongType).jumpIfTrue(target)
        mv.mark(wrongType)
    }

    override fun discard() {
        right.discard()
        left.discard()
    }
}

class PrimitiveComparison(
    private konst primitiveNumberType: PrimitiveType,
    private konst operatorToken: KtSingleValueToken
) : IntrinsicMethod() {
    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue? {
        konst parameterType = Type.getType(JvmPrimitiveType.get(primitiveNumberType).desc)
        konst (left, right) = expression.receiverAndArgs()
        konst a = left.accept(codegen, data).materializedAt(parameterType, left.type)
        konst b = right.accept(codegen, data).materializedAt(parameterType, right.type)

        konst useNonIEEE754Comparison =
            !codegen.context.state.languageVersionSettings.supportsFeature(LanguageFeature.ProperIeee754Comparisons)
                    && (parameterType == Type.FLOAT_TYPE || parameterType == Type.DOUBLE_TYPE)
                    && (left.isSmartcastFromHigherThanNullable(codegen.context) || right.isSmartcastFromHigherThanNullable(codegen.context))

        return if (useNonIEEE754Comparison) {
            NonIEEE754FloatComparison(operatorToken, a, b)
        } else {
            BooleanComparison(operatorToken, a, b)
        }
    }
}
