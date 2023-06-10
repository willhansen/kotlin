/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.calls.model.DefaultValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument
import org.jetbrains.kotlin.resolve.calls.model.VarargValueArgument
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.kotlin.resolve.jvm.AsmTypes.OBJECT_TYPE
import org.jetbrains.kotlin.types.upperIfFlexible
import org.jetbrains.org.objectweb.asm.Type

class CallBasedArgumentGenerator(
    private konst codegen: ExpressionCodegen,
    private konst callGenerator: CallGenerator,
    private konst konstueParameters: List<ValueParameterDescriptor>,
    private konst konstueParameterTypes: List<Type>
) : ArgumentGenerator() {
    private konst isVarargInvoke: Boolean =
        JvmCodegenUtil.isDeclarationOfBigArityFunctionInvoke(konstueParameters.firstOrNull()?.containingDeclaration)

    private konst isPolymorphicSignature: Boolean =
        codegen.state.languageVersionSettings.supportsFeature(LanguageFeature.PolymorphicSignature) &&
        (konstueParameters.firstOrNull()?.containingDeclaration as? FunctionDescriptor)?.let { function ->
            JvmCodegenUtil.isPolymorphicSignature(function)
        } == true

    init {
        if (!isVarargInvoke && !isPolymorphicSignature) {
            assert(konstueParameters.size == konstueParameterTypes.size) {
                "Value parameters and their types mismatch in sizes: ${konstueParameters.size} != ${konstueParameterTypes.size}"
            }
        }
    }

    override fun generateExpression(i: Int, argument: ExpressionValueArgument) {
        callGenerator.genValueAndPut(
            konstueParameters[i],
            argument.konstueArgument!!.getArgumentExpression()!!,
            if (isVarargInvoke) JvmKotlinType(OBJECT_TYPE) else getJvmKotlinType(i),
            i
        )
    }

    override fun generateDefault(i: Int, argument: DefaultValueArgument) {
        callGenerator.putValueIfNeeded(
            getJvmKotlinType(i),
            StackValue.createDefaultValue(konstueParameterTypes[i]),
            if (InlineUtil.isInlineParameter(konstueParameters[i])) ValueKind.DEFAULT_INLINE_PARAMETER else ValueKind.DEFAULT_PARAMETER,
            i
        )
    }

    override fun generateVararg(i: Int, argument: VarargValueArgument) {
        if (isPolymorphicSignature) {
            for ((index, arg) in argument.arguments.withIndex()) {
                konst expression = arg.getArgumentExpression()!!
                konst type = JvmKotlinType(konstueParameterTypes[index], codegen.kotlinType(expression))
                callGenerator.genValueAndPut(null, expression, type, index)
            }
            return
        }

        // Upper bound for type of vararg parameter should always have a form of 'Array<out T>',
        // while its lower bound may be Nothing-typed after approximation
        konst lazyVararg = codegen.genVarargs(argument, konstueParameters[i].type.upperIfFlexible())
        callGenerator.putValueIfNeeded(getJvmKotlinType(i), lazyVararg, ValueKind.GENERAL_VARARG, i)
    }

    override fun reorderArgumentsIfNeeded(args: List<ArgumentAndDeclIndex>) {
        callGenerator.reorderArgumentsIfNeeded(args, konstueParameterTypes)
    }

    private fun getJvmKotlinType(i: Int): JvmKotlinType =
        JvmKotlinType(konstueParameterTypes[i], konstueParameters[i].unsubstitutedType)

    private konst ValueParameterDescriptor.unsubstitutedType
        get() = containingDeclaration.original.konstueParameters[index].type
}
