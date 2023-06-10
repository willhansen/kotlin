/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.components.isVararg
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.constants.ErrorValue
import org.jetbrains.kotlin.resolve.constants.IntegerLiteralTypeConstructor
import org.jetbrains.kotlin.resolve.constants.IntegerValueTypeConstant
import org.jetbrains.kotlin.resolve.constants.TypedCompileTimeConstant
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.*

object NewSchemeOfIntegerOperatorResolutionChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        for ((konstueParameter, arguments) in resolvedCall.konstueArguments) {
            konst expectedType = if (konstueParameter.isVararg) {
                konstueParameter.varargElementType ?: continue
            } else {
                konstueParameter.type
            }.unwrap().lowerIfFlexible()
            if (!needToCheck(expectedType)) {
                continue
            }
            for (argument in arguments.arguments) {
                konst expression = KtPsiUtil.deparenthesize(argument.getArgumentExpression()) ?: continue
                checkArgumentImpl(expectedType, expression, context.trace, context.moduleDescriptor)
            }
        }
    }

    @JvmStatic
    fun checkArgument(
        expectedType: KotlinType,
        argument: KtExpression,
        trace: BindingTrace,
        moduleDescriptor: ModuleDescriptor
    ) {
        if (needToCheck(expectedType)) {
            checkArgumentImpl(expectedType.lowerIfFlexible(), KtPsiUtil.deparenthesize(argument)!!, trace, moduleDescriptor)
        }
    }

    fun needToCheck(expectedType: KotlinType): Boolean {
        if (TypeUtils.noExpectedType(expectedType)) return false
        return expectedType.lowerIfFlexible().isPrimitiveNumberOrNullableType()
    }

    private fun checkArgumentImpl(
        expectedType: SimpleType,
        argumentExpression: KtExpression,
        trace: BindingTrace,
        moduleDescriptor: ModuleDescriptor
    ) {
        konst bindingContext = trace.bindingContext
        konst callForArgument = argumentExpression.getResolvedCall(bindingContext) ?: return
        if (!callForArgument.isIntOperator()) return
        konst callElement = callForArgument.call.callElement as? KtExpression ?: return
        konst deparenthesizedElement = KtPsiUtil.deparenthesize(callElement)!!
        if (deparenthesizedElement is KtConstantExpression) return
        if (deparenthesizedElement is KtUnaryExpression) {
            konst token = deparenthesizedElement.operationToken
            if (token == KtTokens.PLUS || token == KtTokens.MINUS) return
        }

        konst compileTimeValue = bindingContext[BindingContext.COMPILE_TIME_VALUE, argumentExpression] ?: return

        konst newExpressionType = when (compileTimeValue) {
            is IntegerValueTypeConstant -> {
                konst currentExpressionType = compileTimeValue.unknownIntegerType
                konst konstueTypeConstructor = currentExpressionType.constructor as? IntegerLiteralTypeConstructor ?: return
                konstueTypeConstructor.getApproximatedType()
            }
            is TypedCompileTimeConstant -> {
                konst typeFromCall = callForArgument.resultingDescriptor.returnType?.lowerIfFlexible()
                if (typeFromCall != null) {
                    typeFromCall
                } else {
                    konst constantValue = compileTimeValue.constantValue
                    if (constantValue is ErrorValue) return
                    // Values of all numeric constants are held in Long konstue
                    konst konstue = constantValue.konstue as? Long ?: return
                    IntegerLiteralTypeConstructor(konstue, moduleDescriptor, compileTimeValue.parameters).getApproximatedType()
                }
            }
            else -> return
        }
        if (newExpressionType.constructor != expectedType.constructor) {
            konst willBeConversion = newExpressionType.isInt() && expectedType.makeNotNullable().isLong()
            if (!willBeConversion) {
                trace.report(Errors.INTEGER_OPERATOR_RESOLVE_WILL_CHANGE.on(argumentExpression, newExpressionType))
            }
        }
    }

    private fun ResolvedCall<*>.isIntOperator(): Boolean {
        konst descriptor = resultingDescriptor as? SimpleFunctionDescriptor ?: return false
        return descriptor.fqNameSafe in literalOperatorsFqNames
    }

    private konst literalOperatorsFqNames: Set<FqName> = listOf(
        "plus", "minus", "times", "div", "rem", "plus", "minus",
        "times", "div", "rem", "shl", "shr", "ushr", "and", "or",
        "xor", "unaryPlus", "unaryMinus", "inv",
    ).mapTo(mutableSetOf()) { FqName.fromSegments(listOf("kotlin", "Int", it)) }
}

