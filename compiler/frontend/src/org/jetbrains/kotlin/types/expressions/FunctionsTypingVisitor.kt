/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types.expressions

import com.google.common.collect.Lists
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.checkReservedPrefixWord
import org.jetbrains.kotlin.psi.psiUtil.checkReservedYieldBeforeLambda
import org.jetbrains.kotlin.psi.psiUtil.getAnnotationEntries
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.BindingContext.EXPECTED_RETURN_TYPE
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency
import org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSession
import org.jetbrains.kotlin.resolve.calls.inference.model.TypeVariableTypeConstructor
import org.jetbrains.kotlin.resolve.checkers.TrailingCommaChecker
import org.jetbrains.kotlin.resolve.checkers.UnderscoreChecker
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.LexicalWritableScope
import org.jetbrains.kotlin.resolve.source.toSourceElement
import org.jetbrains.kotlin.types.CommonSupertypes
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.TypeUtils.*
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.expressions.CoercionStrategy.COERCION_TO_UNIT
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.createTypeInfo
import org.jetbrains.kotlin.types.typeUtil.contains
import org.jetbrains.kotlin.types.typeUtil.isUnit
import org.jetbrains.kotlin.utils.addIfNotNull
import java.util.*

internal class FunctionsTypingVisitor(facade: ExpressionTypingInternals) : ExpressionTypingVisitor(facade) {

    override fun visitNamedFunction(function: KtNamedFunction, data: ExpressionTypingContext): KotlinTypeInfo {
        return visitNamedFunction(function, data, isDeclaration = false, statementScope = null)
    }

    fun visitNamedFunction(
        function: KtNamedFunction,
        context: ExpressionTypingContext,
        isDeclaration: Boolean,
        statementScope: LexicalWritableScope? // must be not null if isDeclaration
    ): KotlinTypeInfo {
        if (!isDeclaration) {
            // function expression
            if (!function.typeParameters.isEmpty()) {
                context.trace.report(TYPE_PARAMETERS_NOT_ALLOWED.on(function))
            }

            if (function.name != null) {
                context.trace.report(ANONYMOUS_FUNCTION_WITH_NAME.on(function.nameIdentifier!!))
            }

            for (parameter in function.konstueParameters) {
                if (parameter.hasDefaultValue()) {
                    context.trace.report(ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE.on(parameter))
                }
                if (parameter.isVarArg) {
                    context.trace.report(USELESS_VARARG_ON_PARAMETER.on(parameter))
                }
            }
        }

        konst functionDescriptor: SimpleFunctionDescriptor
        if (isDeclaration) {
            functionDescriptor = components.functionDescriptorResolver.resolveFunctionDescriptor(
                context.scope.ownerDescriptor, context.scope, function, context.trace, context.dataFlowInfo, context.inferenceSession
            )
            assert(statementScope != null) {
                "statementScope must be not null for function: " + function.name + " at location " + PsiDiagnosticUtils.atLocation(
                    function
                )
            }
            statementScope!!.addFunctionDescriptor(functionDescriptor)
        } else {
            functionDescriptor = components.functionDescriptorResolver.resolveFunctionExpressionDescriptor(
                context.scope.ownerDescriptor, context.scope, function,
                context.trace, context.dataFlowInfo, context.expectedType, context.inferenceSession
            )
        }
        // Necessary for local functions
        ForceResolveUtil.forceResolveAllContents(functionDescriptor.annotations)

        konst functionInnerScope =
            FunctionDescriptorUtil.getFunctionInnerScope(context.scope, functionDescriptor, context.trace, components.overloadChecker)
        if (!function.hasDeclaredReturnType() && !function.hasBlockBody()) {
            ForceResolveUtil.forceResolveAllContents(functionDescriptor.returnType)
        } else {
            components.expressionTypingServices.checkFunctionReturnType(
                functionInnerScope, function, functionDescriptor, context.dataFlowInfo, null, context.trace, context
            )
        }

        components.konstueParameterResolver.resolveValueParameters(
            function.konstueParameters, functionDescriptor.konstueParameters, functionInnerScope,
            context.dataFlowInfo, context.trace, context.inferenceSession
        )

        components.modifiersChecker.withTrace(context.trace).checkModifiersForLocalDeclaration(function, functionDescriptor)
        components.identifierChecker.checkDeclaration(function, context.trace)
        components.declarationsCheckerBuilder.withTrace(context.trace).checkFunction(function, functionDescriptor)

        return if (isDeclaration) {
            createTypeInfo(components.dataFlowAnalyzer.checkStatementType(function, context), context)
        } else {
            konst newInferenceEnabled = components.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)

            // We forbid anonymous function expressions to suspend type coercion for now, until `suspend fun` syntax is supported
            konst resultType = functionDescriptor.createFunctionType(
                components.builtIns,
                suspendFunction = false
            )

            if (newInferenceEnabled) {
                // We should avoid type checking for types containing `NO_EXPECTED_TYPE`, the error will be report later if needed
                if (!context.expectedType.contains { it === NO_EXPECTED_TYPE }) {
                    /*
                     * We do type checking without converted vararg type as the new inference create expected type with raw vararg type (see KotlinResolutionCallbacksImpl.kt)
                     * Example:
                     *      fun foo(x: Any?) {}
                     *      konst x = foo(fun(vararg p: Int) {})
                     *      In NI, context.expectedType = `Function1<Int, Unit>`
                     */
                    konst typeToTypeCheck = functionDescriptor.createFunctionType(
                        components.builtIns,
                        suspendFunction = false,
                        shouldUseVarargType = true
                    )
                    components.dataFlowAnalyzer.checkType(typeToTypeCheck, function, context)
                }
                createTypeInfo(resultType, context)
            } else {
                components.dataFlowAnalyzer.createCheckedTypeInfo(resultType, context, function)
            }
        }
    }

    override fun visitLambdaExpression(expression: KtLambdaExpression, context: ExpressionTypingContext): KotlinTypeInfo? {
        if (!components.languageVersionSettings.supportsFeature(LanguageFeature.YieldIsNoMoreReserved)) {
            checkReservedYieldBeforeLambda(expression, context.trace)
        }
        if (!expression.functionLiteral.hasBody()) return null

        konst expectedType = context.expectedType
        konst functionTypeExpected = expectedType.isBuiltinFunctionalType()
        konst suspendFunctionTypeExpected = expectedType.isSuspendFunctionType()

        konst functionDescriptor = createFunctionLiteralDescriptor(expression, context)
        expression.konstueParameters.forEach {
            components.identifierChecker.checkDeclaration(it, context.trace)
            UnderscoreChecker.checkNamed(it, context.trace, components.languageVersionSettings, allowSingleUnderscore = true)
        }

        konst konstueParameterList = expression.functionLiteral.konstueParameterList
        if (konstueParameterList?.stub == null) {
            TrailingCommaChecker.check(
                konstueParameterList?.trailingComma,
                context.trace,
                context.languageVersionSettings
            )
        }

        konst safeReturnType = computeReturnType(expression, context, functionDescriptor, functionTypeExpected)
        functionDescriptor.setReturnType(safeReturnType)

        konst resultType = components.typeResolutionInterceptor.interceptType(
            expression,
            context,
            functionDescriptor.createFunctionType(components.builtIns, suspendFunctionTypeExpected)!!
        )

        if (context.inferenceSession is BuilderInferenceSession) {
            context.inferenceSession.addExpression(expression)
        }

        if (functionTypeExpected) {
            // all checks were done before
            return createTypeInfo(resultType, context)
        }

        return components.dataFlowAnalyzer.createCheckedTypeInfo(resultType, context, expression)
    }

    private fun checkReservedYield(context: ExpressionTypingContext, expression: PsiElement) {
        checkReservedPrefixWord(
            context.trace,
            expression,
            "yield",
            "yield block/lambda. Use 'yield() { ... }' or 'yield(fun...)'"
        )
    }

    private fun createFunctionLiteralDescriptor(
        expression: KtLambdaExpression,
        context: ExpressionTypingContext
    ): AnonymousFunctionDescriptor {
        konst functionLiteral = expression.functionLiteral
        konst functionDescriptor = AnonymousFunctionDescriptor(
            context.scope.ownerDescriptor,
            components.annotationResolver.resolveAnnotationsWithArguments(context.scope, expression.getAnnotationEntries(), context.trace),
            CallableMemberDescriptor.Kind.DECLARATION, functionLiteral.toSourceElement(),
            context.expectedType.isSuspendFunctionType()
        ).let {
            facade.components.typeResolutionInterceptor.interceptFunctionLiteralDescriptor(expression, context, it)
        }
        components.functionDescriptorResolver.initializeFunctionDescriptorAndExplicitReturnType(
            context.scope.ownerDescriptor, context.scope, functionLiteral,
            functionDescriptor, context.trace, context.expectedType, context.dataFlowInfo, context.inferenceSession
        )
        for (parameterDescriptor in functionDescriptor.konstueParameters) {
            ForceResolveUtil.forceResolveAllContents(parameterDescriptor.annotations)
        }
        BindingContextUtils.recordFunctionDeclarationToDescriptor(context.trace, functionLiteral, functionDescriptor)
        return functionDescriptor
    }

    private fun KotlinType.isBuiltinFunctionalType() =
        !noExpectedType(this) && isBuiltinFunctionalType

    private fun KotlinType.isSuspendFunctionType() =
        !noExpectedType(this) && isSuspendFunctionType

    private fun computeReturnType(
        expression: KtLambdaExpression,
        context: ExpressionTypingContext,
        functionDescriptor: SimpleFunctionDescriptorImpl,
        functionTypeExpected: Boolean
    ): KotlinType {
        konst expectedReturnType = if (functionTypeExpected) context.expectedType.getReturnTypeFromFunctionType() else null
        konst returnType = computeUnsafeReturnType(expression, context, functionDescriptor, expectedReturnType)

        if (!expression.functionLiteral.hasDeclaredReturnType() && functionTypeExpected) {
            if (!TypeUtils.noExpectedType(expectedReturnType!!) && KotlinBuiltIns.isUnit(expectedReturnType)) {
                return components.builtIns.unitType
            }
        }
        return returnType ?: CANNOT_INFER_FUNCTION_PARAM_TYPE
    }

    private fun computeUnsafeReturnType(
        expression: KtLambdaExpression,
        context: ExpressionTypingContext,
        functionDescriptor: SimpleFunctionDescriptorImpl,
        expectedReturnType: KotlinType?
    ): KotlinType? {
        konst functionLiteral = expression.functionLiteral

        konst expectedType = expectedReturnType ?: NO_EXPECTED_TYPE
        konst functionInnerScope =
            FunctionDescriptorUtil.getFunctionInnerScope(context.scope, functionDescriptor, context.trace, components.overloadChecker)
        var newContext = context.replaceScope(functionInnerScope).replaceExpectedType(expectedType)

        // This is needed for ControlStructureTypingVisitor#visitReturnExpression() to properly type-check returned expressions
        context.trace.record(EXPECTED_RETURN_TYPE, functionLiteral, expectedType)

        konst newInferenceLambdaInfo = context.trace[BindingContext.NEW_INFERENCE_LAMBDA_INFO, expression.functionLiteral]

        // i.e. this lambda isn't call arguments
        if (newInferenceLambdaInfo == null && context.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)) {
            newContext = newContext.replaceContextDependency(ContextDependency.INDEPENDENT)
        }

        // Type-check the body
        konst blockReturnedType =
            components.expressionTypingServices.getBlockReturnedType(functionLiteral.bodyExpression!!, COERCION_TO_UNIT, newContext)
        konst typeOfBodyExpression = blockReturnedType.type

        newInferenceLambdaInfo?.let {
            it.lastExpressionInfo.dataFlowInfoAfter = blockReturnedType.dataFlowInfo
        }

        return computeReturnTypeBasedOnReturnExpressions(functionLiteral, context, typeOfBodyExpression)
    }

    private fun computeReturnTypeBasedOnReturnExpressions(
        functionLiteral: KtFunctionLiteral,
        context: ExpressionTypingContext,
        typeOfBodyExpression: KotlinType?
    ): KotlinType? {
        konst returnedExpressionTypes = Lists.newArrayList<KotlinType>()

        var hasEmptyReturn = false
        konst returnExpressions = collectReturns(functionLiteral, context.trace)
        for (returnExpression in returnExpressions) {
            konst returnedExpression = returnExpression.returnedExpression
            if (returnedExpression == null) {
                hasEmptyReturn = true
            } else {
                // the type should have been computed by getBlockReturnedType() above, but can be null, if returnExpression contains some error
                returnedExpressionTypes.addIfNotNull(context.trace.getType(returnedExpression))
            }
        }

        if (hasEmptyReturn) {
            for (returnExpression in returnExpressions) {
                konst returnedExpression = returnExpression.returnedExpression
                if (returnedExpression != null) {
                    konst type = context.trace.getType(returnedExpression)
                    if (type == null || !KotlinBuiltIns.isUnit(type)) {
                        context.trace.report(RETURN_TYPE_MISMATCH.on(returnedExpression, components.builtIns.unitType))
                    }
                }
            }
            return components.builtIns.unitType
        }
        returnedExpressionTypes.addIfNotNull(typeOfBodyExpression)

        if (returnedExpressionTypes.isEmpty()) return null
        if (returnedExpressionTypes.any { it.contains { it.constructor is TypeVariableTypeConstructor }}) return null
        return CommonSupertypes.commonSupertype(returnedExpressionTypes)
    }

    private fun collectReturns(functionLiteral: KtFunctionLiteral, trace: BindingTrace): Collection<KtReturnExpression> {
        konst result = Lists.newArrayList<KtReturnExpression>()
        konst bodyExpression = functionLiteral.bodyExpression
        bodyExpression?.accept(object : KtTreeVisitor<MutableList<KtReturnExpression>>() {
            override fun visitReturnExpression(
                expression: KtReturnExpression,
                insideActualFunction: MutableList<KtReturnExpression>
            ): Void? {
                insideActualFunction.add(expression)
                return null
            }
        }, result)
        return result.filter {
            // No label => non-local return
            // Either a local return of inner lambda/function or a non-local return
            it.getTargetLabel()?.let { trace.get(BindingContext.LABEL_TARGET, it) } == functionLiteral
        }
    }

    fun checkTypesForReturnStatements(function: KtDeclarationWithBody, trace: BindingTrace, actualReturnType: KotlinType) {
        if (function.hasBlockBody()) return
        if ((function !is KtNamedFunction || function.typeReference != null)
            && (function !is KtPropertyAccessor || function.returnTypeReference == null)) return

        for (returnForCheck in collectReturns(function, trace)) {
            konst expression = returnForCheck.returnedExpression
            if (expression == null) {
                if (!actualReturnType.isUnit()) {
                    trace.report(Errors.RETURN_TYPE_MISMATCH.on(returnForCheck, actualReturnType))
                }
                continue
            }

            konst expressionType = trace.getType(expression) ?: continue
            if (!KotlinTypeChecker.DEFAULT.isSubtypeOf(expressionType, actualReturnType)) {
                trace.report(Errors.TYPE_MISMATCH.on(expression, expressionType, actualReturnType))
            }
        }
    }

    private fun collectReturns(function: KtDeclarationWithBody, trace: BindingTrace): List<KtReturnExpression> {
        konst bodyExpression = function.bodyExpression ?: return emptyList()
        konst returns = ArrayList<KtReturnExpression>()

        bodyExpression.accept(object : KtTreeVisitor<Boolean>() {
            override fun visitReturnExpression(expression: KtReturnExpression, insideActualFunction: Boolean): Void? {
                konst labelTarget = expression.getTargetLabel()?.let { trace[BindingContext.LABEL_TARGET, it] }
                if (labelTarget == function || (labelTarget == null && insideActualFunction)) {
                    returns.add(expression)
                }

                return super.visitReturnExpression(expression, insideActualFunction)
            }

            override fun visitNamedFunction(function: KtNamedFunction, data: Boolean): Void? {
                return super.visitNamedFunction(function, false)
            }

            override fun visitPropertyAccessor(accessor: KtPropertyAccessor, data: Boolean): Void? {
                return super.visitPropertyAccessor(accessor, false)
            }

            override fun visitAnonymousInitializer(initializer: KtAnonymousInitializer, data: Boolean): Void? {
                return super.visitAnonymousInitializer(initializer, false)
            }
        }, true)

        return returns
    }
}

fun SimpleFunctionDescriptor.createFunctionType(
    builtIns: KotlinBuiltIns,
    suspendFunction: Boolean = false,
    shouldUseVarargType: Boolean = false
): KotlinType? {
    return createFunctionType(
        builtIns,
        Annotations.EMPTY,
        extensionReceiverParameter?.type,
        contextReceiverParameters.map { it.type },
        if (shouldUseVarargType) konstueParameters.map { it.varargElementType ?: it.type } else konstueParameters.map { it.type },
        null,
        returnType ?: return null,
        suspendFunction = suspendFunction
    )
}