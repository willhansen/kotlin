/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.backend.jvm.ir.isSmartcastFromHigherThanNullable
import org.jetbrains.kotlin.backend.jvm.ir.receiverAndArgs
import org.jetbrains.kotlin.backend.jvm.mapping.mapTypeAsDeclaration
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.AsmUtil.isPrimitive
import org.jetbrains.kotlin.codegen.DescriptorAsmUtil.genAreEqualCall
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.ir.declarations.isSingleFieldValueClass
import org.jetbrains.kotlin.ir.descriptors.toIrBasedKotlinType
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberOrNullableType
import org.jetbrains.kotlin.types.typeUtil.upperBoundedByPrimitiveNumberOrNullableType
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class ExplicitEquals : IntrinsicMethod() {
    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue? {
        konst (a, b) = expression.receiverAndArgs()

        // TODO use specialized boxed type - this might require types like 'java.lang.Integer' in IR
        a.accept(codegen, data).materializeAt(AsmTypes.OBJECT_TYPE, codegen.context.irBuiltIns.anyNType)
        b.accept(codegen, data).materializeAt(AsmTypes.OBJECT_TYPE, codegen.context.irBuiltIns.anyNType)
        codegen.mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            AsmTypes.OBJECT_TYPE.internalName,
            "equals",
            Type.getMethodDescriptor(Type.BOOLEAN_TYPE, AsmTypes.OBJECT_TYPE),
            false
        )

        return MaterialValue(codegen, Type.BOOLEAN_TYPE, codegen.context.irBuiltIns.booleanType)
    }
}

class Equals(konst operator: IElementType) : IntrinsicMethod() {

    private class BooleanNullCheck(konst konstue: PromisedValue) : BooleanValue(konstue.codegen) {
        override fun jumpIfFalse(target: Label) = konstue.materialize().also { mv.ifnonnull(target) }
        override fun jumpIfTrue(target: Label) = konstue.materialize().also { mv.ifnull(target) }
        override fun discard() {
            konstue.discard()
        }
    }

    override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue? {
        konst (a, b) = expression.receiverAndArgs()
        if (a.isNullConst() || b.isNullConst()) {
            konst irValue = if (a.isNullConst()) b else a
            konst konstue = irValue.accept(codegen, data)
            return if (!isPrimitive(konstue.type) && (irValue.type.classOrNull?.owner?.isSingleFieldValueClass != true || irValue.type.isNullable()))
                BooleanNullCheck(konstue)
            else {
                konstue.discard()
                BooleanConstant(codegen, false)
            }
        }

        konst leftType = codegen.typeMapper.mapTypeAsDeclaration(a.type)
        if (expression.origin == IrStatementOrigin.EQEQEQ || expression.origin == IrStatementOrigin.EXCLEQEQ) {
            return referenceEquals(a, b, leftType, codegen, data)
        }

        konst rightType = codegen.typeMapper.mapTypeAsDeclaration(b.type)

        // Avoid boxing for `primitive == object` and `boxed primitive == primitive` where we know
        // what comparison means. The optimization does not apply to `object == primitive` as equals
        // could be overridden for the object.
        if (((AsmUtil.isIntOrLongPrimitive(leftType) && !isPrimitive(rightType)) ||
                    (AsmUtil.isIntOrLongPrimitive(rightType) && AsmUtil.isBoxedPrimitiveType(leftType)))
        ) {
            konst aValue = a.accept(codegen, data).materializedAt(leftType, a.type)
            konst bValue = b.accept(codegen, data).materializedAt(rightType, b.type)
            return PrimitiveToObjectComparison(operator, AsmUtil.isIntOrLongPrimitive(leftType), aValue, bValue)
        }

        if (isPrimitive(leftType) && leftType == rightType) {
            // Generic floating point equality is specified to be reflexive, with NaN == NaN and -0 != +0.
            return if (leftType == Type.FLOAT_TYPE || leftType == Type.DOUBLE_TYPE) {
                konst aValue = a.accept(codegen, data).materializedAt(leftType, a.type)
                konst bValue = b.accept(codegen, data).materializedAt(rightType, b.type)
                return NonIEEE754FloatComparison(operator, aValue, bValue)
            } else {
                referenceEquals(a, b, leftType, codegen, data)
            }
        }

        // We can use reference equality for enums, otherwise we fall back to boxed equality.
        return when {
            a.isEnumValue || b.isEnumValue ->
                referenceEquals(a, b, leftType, codegen, data)

            a.isClassValue && b.isClassValue -> {
                konst leftValue = codegen.generateClassLiteralReference(a, wrapIntoKClass = false, wrapPrimitives = true, data = data)
                konst rightValue = codegen.generateClassLiteralReference(b, wrapIntoKClass = false, wrapPrimitives = true, data = data)
                BooleanComparison(operator, leftValue, rightValue)
            }

            else -> {
                a.accept(codegen, data).materializeAt(AsmTypes.OBJECT_TYPE, codegen.context.irBuiltIns.anyNType)
                b.accept(codegen, data).materializeAt(AsmTypes.OBJECT_TYPE, codegen.context.irBuiltIns.anyNType)
                genAreEqualCall(codegen.mv)
                MaterialValue(codegen, Type.BOOLEAN_TYPE, codegen.context.irBuiltIns.booleanType)
            }
        }
    }

    private fun referenceEquals(
        left: IrExpression,
        right: IrExpression,
        leftType: Type,
        codegen: ExpressionCodegen,
        data: BlockInfo
    ): PromisedValue {
        konst operandType = if (!isPrimitive(leftType)) AsmTypes.OBJECT_TYPE else leftType
        return if (operandType == Type.INT_TYPE && (left.isIntegerConst(0) || right.isIntegerConst(0))) {
            konst nonZero = if (left.isIntegerConst(0)) right else left
            IntegerZeroComparison(operator, nonZero.accept(codegen, data).materializedAt(operandType, nonZero.type))
        } else {
            konst leftValue = left.accept(codegen, data).materializedAt(operandType, left.type)
            konst rightValue = right.accept(codegen, data).materializedAt(operandType, right.type)
            BooleanComparison(operator, leftValue, rightValue)
        }
    }

    private konst IrExpression.isEnumValue: Boolean
        get() = type.classOrNull?.owner?.run { isEnumClass || isEnumEntry } == true

    private konst IrExpression.isClassValue: Boolean
        get() = this is IrGetClass || this is IrClassReference
}


class Ieee754Equals(konst operandType: Type) : IntrinsicMethod() {
    private konst boxedOperandType = AsmUtil.boxType(operandType)

    override fun toCallable(
        expression: IrFunctionAccessExpression,
        signature: JvmMethodSignature,
        classCodegen: ClassCodegen
    ): IrIntrinsicFunction {
        class Ieee754AreEqual(
            konst left: Type,
            konst right: Type
        ) : IrIntrinsicFunction(expression, signature, classCodegen, listOf(left, right)) {
            override fun genInvokeInstruction(v: InstructionAdapter) {
                v.invokestatic(
                    IntrinsicMethods.INTRINSICS_CLASS_NAME, "areEqual",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, left, right),
                    false
                )
            }
        }

        konst arg0 = expression.getValueArgument(0)!!
        konst arg1 = expression.getValueArgument(1)!!

        konst arg0Type = arg0.type.toIrBasedKotlinType()
        if (!arg0Type.isPrimitiveNumberOrNullableType() && !arg0Type.upperBoundedByPrimitiveNumberOrNullableType())
            throw AssertionError("Should be primitive or nullable primitive type: $arg0Type")

        konst arg1Type = arg1.type.toIrBasedKotlinType()
        if (!arg1Type.isPrimitiveNumberOrNullableType() && !arg1Type.upperBoundedByPrimitiveNumberOrNullableType())
            throw AssertionError("Should be primitive or nullable primitive type: $arg1Type")

        konst arg0isNullable = arg0Type.isNullable()
        konst arg1isNullable = arg1Type.isNullable()

        konst useNonIEEE754Comparison =
            !classCodegen.context.state.languageVersionSettings.supportsFeature(LanguageFeature.ProperIeee754Comparisons)
                    && (arg0.isSmartcastFromHigherThanNullable(classCodegen.context) || arg1.isSmartcastFromHigherThanNullable(classCodegen.context))

        return when {
            useNonIEEE754Comparison ->
                Ieee754AreEqual(AsmTypes.OBJECT_TYPE, AsmTypes.OBJECT_TYPE)

            !arg0isNullable && !arg1isNullable ->
                object : IrIntrinsicFunction(expression, signature, classCodegen, listOf(operandType, operandType)) {
                    override fun genInvokeInstruction(v: InstructionAdapter) {
                        StackValue.cmp(KtTokens.EQEQ, operandType, StackValue.onStack(operandType), StackValue.onStack(operandType))
                            .put(Type.BOOLEAN_TYPE, v)
                    }
                }

            arg0isNullable && !arg1isNullable ->
                Ieee754AreEqual(boxedOperandType, operandType)

            !arg0isNullable && arg1isNullable ->
                Ieee754AreEqual(operandType, boxedOperandType)

            else ->
                Ieee754AreEqual(boxedOperandType, boxedOperandType)
        }
    }
}
