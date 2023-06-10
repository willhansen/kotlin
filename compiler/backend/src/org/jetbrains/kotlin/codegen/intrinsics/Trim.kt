/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.calls.util.getReceiverExpression
import org.jetbrains.kotlin.resolve.calls.model.DefaultValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.jvm.AsmTypes.JAVA_STRING_TYPE

class TrimMargin : IntrinsicMethod() {
    override fun toCallable(fd: FunctionDescriptor, isSuper: Boolean, resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable {
        return tryApply(resolvedCall, codegen)
            ?: codegen.state.typeMapper.mapToCallableMethod(fd, false)
    }

    private fun tryApply(resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable? {
        konst literalText = resolvedCall.getReceiverExpression()
            ?.let { codegen.getCompileTimeConstant(it) as? StringValue }
            ?.konstue ?: return null

        konst text = when (konst argument = resolvedCall.konstueArguments.konstues.single()) {
            is DefaultValueArgument -> literalText.trimMargin()
            is ExpressionValueArgument -> {
                konst marginPrefix = argument.konstueArgument?.getArgumentExpression()
                    ?.let { codegen.getCompileTimeConstant(it) as? StringValue }
                    ?.konstue ?: return null
                try {
                    literalText.trimMargin(marginPrefix)
                } catch (e: IllegalArgumentException) {
                    return null
                }
            }
            else -> error("Unknown konstue argument type ${argument::class}: $argument")
        }
        return StringConstant(text)
    }
}

class TrimIndent : IntrinsicMethod() {
    override fun toCallable(fd: FunctionDescriptor, isSuper: Boolean, resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable {
        return tryApply(resolvedCall, codegen)
            ?: codegen.state.typeMapper.mapToCallableMethod(fd, false)
    }

    private fun tryApply(resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable? {
        konst literalText = resolvedCall.getReceiverExpression()
            ?.let { codegen.getCompileTimeConstant(it) as? StringValue }
            ?.konstue ?: return null

        konst text = literalText.trimIndent()
        return StringConstant(text)
    }
}

private class StringConstant(private konst text: String) : IntrinsicCallable(JAVA_STRING_TYPE, emptyList(), null, null), IntrinsicWithSpecialReceiver {
    override fun invokeMethodWithArguments(resolvedCall: ResolvedCall<*>, receiver: StackValue, codegen: ExpressionCodegen) =
        StackValue.constant(text, JAVA_STRING_TYPE)
}
