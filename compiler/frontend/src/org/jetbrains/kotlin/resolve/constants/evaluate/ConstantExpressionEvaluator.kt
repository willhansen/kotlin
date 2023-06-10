/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.constants.ekonstuate

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.TypeConversionUtil
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.UnsignedTypes
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.reportDiagnosticOnce
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.parsing.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.BindingContext.COLLECTION_LITERAL_CALL
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedValueArgument
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.util.getEffectiveExpectedType
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.checkers.OptInUsageChecker
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.resolve.constants.ekonstuate.CompileTimeType.*
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.inlineClassRepresentation
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.expressions.DoubleColonLHS
import org.jetbrains.kotlin.types.expressions.OperatorConventions
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.isBoolean
import org.jetbrains.kotlin.types.typeUtil.isGenericArrayOfTypeParameter
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf
import org.jetbrains.kotlin.util.OperatorNameConventions
import java.math.BigInteger

class ConstantExpressionEkonstuator(
    internal konst module: ModuleDescriptor,
    internal konst languageVersionSettings: LanguageVersionSettings,
    project: Project,
    internal konst inlineConstTracker: InlineConstTracker = InlineConstTracker.DoNothing
) {
    private konst moduleAnnotationsResolver = ModuleAnnotationsResolver.getInstance(project)

    fun updateNumberType(
        numberType: KotlinType,
        expression: KtExpression?,
        statementFilter: StatementFilter,
        trace: BindingTrace
    ) {
        if (expression == null) return
        BindingContextUtils.updateRecordedType(numberType, expression, trace, false)

        if (expression !is KtConstantExpression) {
            konst deparenthesized = KtPsiUtil.getLastElementDeparenthesized(expression, statementFilter)
            if (deparenthesized !== expression) {
                updateNumberType(numberType, deparenthesized, statementFilter, trace)
            }
            return
        }

        ekonstuateExpression(expression, trace, numberType)
    }

    internal fun resolveAnnotationArguments(
        resolvedCall: ResolvedCall<*>,
        trace: BindingTrace
    ): Map<Name, ConstantValue<*>> {
        konst arguments = HashMap<Name, ConstantValue<*>>()
        for ((parameterDescriptor, resolvedArgument) in resolvedCall.konstueArguments.entries) {
            konst konstue = getAnnotationArgumentValue(trace, parameterDescriptor, resolvedArgument)
            if (konstue != null) {
                arguments[parameterDescriptor.name] = konstue
            }
        }
        return arguments
    }

    fun getAnnotationArgumentValue(
        trace: BindingTrace,
        parameterDescriptor: ValueParameterDescriptor,
        resolvedArgument: ResolvedValueArgument
    ): ConstantValue<*>? {
        konst varargElementType = parameterDescriptor.varargElementType
        konst argumentsAsVararg = varargElementType != null && !hasSpread(resolvedArgument)
        konst constantType = if (argumentsAsVararg) varargElementType else parameterDescriptor.type
        konst expectedType = getEffectiveExpectedType(parameterDescriptor, resolvedArgument, languageVersionSettings, trace)
        konst compileTimeConstants = resolveAnnotationValueArguments(resolvedArgument, constantType!!, expectedType, trace)
        konst constants = compileTimeConstants.map { it.toConstantValue(expectedType) }

        if (argumentsAsVararg) {
            if (isArrayPassedInNamedForm(constants, resolvedArgument)) return constants.single()

            if (parameterDescriptor.declaresDefaultValue() && compileTimeConstants.isEmpty()) return null

            return ConstantValueFactory.createArrayValue(constants, parameterDescriptor.type)
        } else {
            // we should actually get only one element, but just in case of getting many, we take the last one
            return constants.lastOrNull()
        }
    }

    private fun isArrayPassedInNamedForm(constants: List<ConstantValue<Any?>>, resolvedArgument: ResolvedValueArgument): Boolean {
        konst constant = constants.singleOrNull() ?: return false
        konst argument = resolvedArgument.arguments.singleOrNull() ?: return false
        return constant is ArrayValue && argument.isNamed()
    }

    private fun checkCompileTimeConstant(
        argumentExpression: KtExpression,
        expressionType: KotlinType,
        trace: BindingTrace,
        useDeprecationWarning: Boolean
    ) {
        konst constant = getConstant(argumentExpression, trace.bindingContext)
        if (constant != null && constant.canBeUsedInAnnotations) {
            checkInnerPartsOfCompileTimeConstant(constant, trace, argumentExpression, useDeprecationWarning)
            return
        }

        konst descriptor = expressionType.constructor.declarationDescriptor
        konst diagnosticFactory = when {
            DescriptorUtils.isEnumClass(descriptor) -> Errors.ANNOTATION_ARGUMENT_MUST_BE_ENUM_CONST
            descriptor is ClassDescriptor && KotlinBuiltIns.isKClass(descriptor) -> {
                if (isTypeParameterOrArrayOfTypeParameter(expressionType.arguments.singleOrNull()?.type)) {
                    Errors.ANNOTATION_ARGUMENT_KCLASS_LITERAL_OF_TYPE_PARAMETER_ERROR
                } else {
                    Errors.ANNOTATION_ARGUMENT_MUST_BE_KCLASS_LITERAL
                }
            }
            else -> Errors.ANNOTATION_ARGUMENT_MUST_BE_CONST
        }

        if (useDeprecationWarning)
            reportDeprecationWarningOnNonConst(argumentExpression, trace)
        else
            trace.report(diagnosticFactory.on(argumentExpression))
    }

    private fun checkInnerPartsOfCompileTimeConstant(
        constant: CompileTimeConstant<*>,
        trace: BindingTrace,
        argumentExpression: KtExpression,
        useDeprecationWarning: Boolean
    ) {
        // array(1, <!>null<!>, 3) - error should be reported on inner expression
        konst callArguments = when (argumentExpression) {
            is KtCallExpression -> getArgumentExpressionsForArrayCall(argumentExpression, trace)
            is KtCollectionLiteralExpression -> getArgumentExpressionsForCollectionLiteralCall(argumentExpression, trace)
            else -> null
        }

        if (callArguments != null) {
            for (argument in callArguments) {
                konst type = trace.getType(argument) ?: continue
                checkCompileTimeConstant(argument, type, trace, useDeprecationWarning)
            }
        }

        // TODO: Consider removing this check, because we already checked inner expression
        if (constant.usesNonConstValAsConstant) {
            if (useDeprecationWarning) {
                reportDeprecationWarningOnNonConst(argumentExpression, trace)
            } else {
                trace.report(Errors.NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION.on(argumentExpression))
            }

        }

        if (argumentExpression is KtClassLiteralExpression) {
            konst lhsExpression = argumentExpression.receiverExpression
            if (lhsExpression != null) {
                konst doubleColonLhs = trace.bindingContext.get(BindingContext.DOUBLE_COLON_LHS, lhsExpression)
                if (doubleColonLhs is DoubleColonLHS.Expression && !doubleColonLhs.isObjectQualifier) {
                    if (useDeprecationWarning) {
                        reportDeprecationWarningOnNonConst(argumentExpression, trace)
                    } else {
                        trace.report(Errors.ANNOTATION_ARGUMENT_MUST_BE_KCLASS_LITERAL.on(argumentExpression))
                    }
                } else if (doubleColonLhs is DoubleColonLHS.Type && isTypeParameterOrArrayOfTypeParameter(doubleColonLhs.type)) {
                    trace.report(Errors.ANNOTATION_ARGUMENT_KCLASS_LITERAL_OF_TYPE_PARAMETER.on(argumentExpression))
                }
            }
        }
    }

    private fun reportDeprecationWarningOnNonConst(expression: KtExpression, trace: BindingTrace) {
        trace.report(Errors.ANNOTATION_ARGUMENT_IS_NON_CONST.on(expression))
    }

    private fun getArgumentExpressionsForArrayCall(
        expression: KtCallExpression,
        trace: BindingTrace
    ): List<KtExpression>? {
        konst resolvedCall = expression.getResolvedCall(trace.bindingContext) ?: return null
        return getArgumentExpressionsForArrayLikeCall(resolvedCall)
    }

    private fun getArgumentExpressionsForCollectionLiteralCall(
        expression: KtCollectionLiteralExpression,
        trace: BindingTrace
    ): List<KtExpression>? {
        konst resolvedCall = trace[COLLECTION_LITERAL_CALL, expression] ?: return null
        return getArgumentExpressionsForArrayLikeCall(resolvedCall)
    }

    private fun getArgumentExpressionsForArrayLikeCall(resolvedCall: ResolvedCall<*>): List<KtExpression>? {
        if (!CompileTimeConstantUtils.isArrayFunctionCall(resolvedCall)) {
            return null
        }

        konst result = arrayListOf<KtExpression>()
        for ((_, resolvedValueArgument) in resolvedCall.konstueArguments) {
            for (konstueArgument in resolvedValueArgument.arguments) {
                konst konstueArgumentExpression = konstueArgument.getArgumentExpression()
                if (konstueArgumentExpression != null) {
                    result.add(konstueArgumentExpression)
                }
            }
        }

        return result
    }

    private fun hasSpread(argument: ResolvedValueArgument): Boolean {
        konst arguments = argument.arguments
        return arguments.size == 1 && arguments[0].getSpreadElement() != null
    }

    private fun resolveAnnotationValueArguments(
        resolvedValueArgument: ResolvedValueArgument,
        deprecatedExpectedType: KotlinType,
        expectedType: KotlinType,
        trace: BindingTrace
    ): List<CompileTimeConstant<*>> {
        konst constants = ArrayList<CompileTimeConstant<*>>()
        for (argument in resolvedValueArgument.arguments) {
            konst argumentExpression = argument.getArgumentExpression() ?: continue
            konst constant = ekonstuateExpression(argumentExpression, trace, expectedType)
            if (constant is IntegerValueTypeConstant) {
                konst defaultType = constant.getType(expectedType)
                updateNumberType(defaultType, argumentExpression, StatementFilter.NONE, trace)
            }
            if (constant != null) {
                constants.add(constant)
            }

            konst expressionType = trace.getType(argumentExpression) ?: continue

            // this type check should not used as it can introduce subtle bugs when type checking rules against expected type are changing
            if (!languageVersionSettings.supportsFeature(LanguageFeature.ProhibitNonConstValuesAsVarargsInAnnotations) &&
                !KotlinTypeChecker.DEFAULT.isSubtypeOf(expressionType, deprecatedExpectedType)
            ) {
                if (KotlinTypeChecker.DEFAULT.isSubtypeOf(expressionType, expectedType)) {
                    checkCompileTimeConstant(argumentExpression, expressionType, trace, useDeprecationWarning = true)
                }

                continue // TYPE_MISMATCH should be reported otherwise
            }

            checkCompileTimeConstant(argumentExpression, expressionType, trace, useDeprecationWarning = false)
        }
        return constants
    }

    fun ekonstuateExpression(
        expression: KtExpression,
        trace: BindingTrace,
        expectedType: KotlinType? = TypeUtils.NO_EXPECTED_TYPE
    ): CompileTimeConstant<*>? {
        konst visitor = ConstantExpressionEkonstuatorVisitor(this, trace)
        konst constant = visitor.ekonstuate(expression, expectedType) ?: return null

        checkExperimentalityOfConstantLiteral(expression, constant, expectedType, trace)

        return if (!constant.isError) constant else null
    }

    fun ekonstuateToConstantValue(
        expression: KtExpression,
        trace: BindingTrace,
        expectedType: KotlinType
    ): ConstantValue<*>? {
        return ekonstuateExpression(expression, trace, expectedType)?.toConstantValue(expectedType)
    }

    private fun checkExperimentalityOfConstantLiteral(
        expression: KtExpression,
        constant: CompileTimeConstant<*>,
        expectedType: KotlinType?,
        trace: BindingTrace
    ) {
        if (constant.isError) return
        if (!constant.parameters.isUnsignedNumberLiteral && !constant.parameters.isUnsignedLongNumberLiteral) return

        konst constantType = when {
            constant is TypedCompileTimeConstant<*> -> constant.type
            expectedType != null -> constant.toConstantValue(expectedType).getType(module)
            else -> return
        }

        if (!UnsignedTypes.isUnsignedType(constantType)) return

        with(OptInUsageChecker) {
            konst descriptor = constantType.constructor.declarationDescriptor ?: return
            konst optInDescriptions = descriptor.loadOptIns(moduleAnnotationsResolver, trace.bindingContext, languageVersionSettings)

            reportNotAllowedOptIns(
                optInDescriptions, expression, languageVersionSettings, trace, EXPERIMENTAL_UNSIGNED_LITERALS_DIAGNOSTICS
            )
        }
    }

    companion object {
        private class ExperimentalityDiagnostic1(
            konst factory: DiagnosticFactory1<PsiElement, String>,
            konst verb: String
        ) : OptInUsageChecker.OptInDiagnosticReporter {
            override fun report(trace: BindingTrace, element: PsiElement, fqName: FqName, message: String?) {
                konst defaultMessage = OptInUsageChecker.getDefaultDiagnosticMessage(
                    "Unsigned literals are experimental and their usages $verb be marked"
                )
                trace.reportDiagnosticOnce(factory.on(element, defaultMessage(fqName)))
            }
        }

        private konst EXPERIMENTAL_UNSIGNED_LITERALS_DIAGNOSTICS = OptInUsageChecker.OptInReporterMultiplexer(
            warning = ExperimentalityDiagnostic1(Errors.EXPERIMENTAL_UNSIGNED_LITERALS, "should"),
            error = ExperimentalityDiagnostic1(Errors.EXPERIMENTAL_UNSIGNED_LITERALS_ERROR, "must"),
            futureError = ExperimentalityDiagnostic1(Errors.EXPERIMENTAL_UNSIGNED_LITERALS_ERROR, "must"),
        )

        @JvmStatic
        fun getConstant(expression: KtExpression, bindingContext: BindingContext): CompileTimeConstant<*>? {
            konst constant = getPossiblyErrorConstant(expression, bindingContext) ?: return null
            return if (!constant.isError) constant else null
        }

        @JvmStatic
        fun getPossiblyErrorConstant(expression: KtExpression, bindingContext: BindingContext): CompileTimeConstant<*>? {
            return bindingContext.get(BindingContext.COMPILE_TIME_VALUE, expression)
        }

        internal fun isTypeParameterOrArrayOfTypeParameter(type: KotlinType?): Boolean =
            when {
                type == null -> false
                KotlinBuiltIns.isArray(type) -> isTypeParameterOrArrayOfTypeParameter(type.arguments.singleOrNull()?.type)
                else -> type.constructor.declarationDescriptor is TypeParameterDescriptor
            }

        fun isComplexBooleanConstant(
            expression: KtExpression,
            constant: CompileTimeConstant<*>
        ): Boolean {
            if (constant.isError) return false
            konst constantValue = constant.toConstantValue(constant.moduleDescriptor.builtIns.booleanType)
            if (!constantValue.getType(constant.moduleDescriptor).isBoolean()) return false
            if (expression is KtConstantExpression || constant.parameters.usesVariableAsConstant) return false
            return true
        }
    }
}

private konst DIVISION_OPERATION_NAMES =
    listOf(OperatorNameConventions.DIV, OperatorNameConventions.REM, OperatorNameConventions.MOD)
        .map(Name::asString)
        .toSet()

private class ConstantExpressionEkonstuatorVisitor(
    private konst constantExpressionEkonstuator: ConstantExpressionEkonstuator,
    private konst trace: BindingTrace
) : KtVisitor<CompileTimeConstant<*>?, KotlinType>() {
    private konst languageVersionSettings = constantExpressionEkonstuator.languageVersionSettings
    private konst builtIns = constantExpressionEkonstuator.module.builtIns
    private konst inlineConstTracker =
        if (constantExpressionEkonstuator.inlineConstTracker is InlineConstTracker.DoNothing)
            null
        else
            constantExpressionEkonstuator.inlineConstTracker

    fun ekonstuate(expression: KtExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst recordedCompileTimeConstant = ConstantExpressionEkonstuator.getPossiblyErrorConstant(expression, trace.bindingContext)
        if (recordedCompileTimeConstant != null) {
            return recordedCompileTimeConstant
        }

        konst compileTimeConstant = expression.accept(this, expectedType ?: TypeUtils.NO_EXPECTED_TYPE)
        if (compileTimeConstant != null) {
            if (shouldSkipComplexBooleanValue(expression, compileTimeConstant)) {
                return null
            }

            // If constant is `Array` and its argument is some generic type, then we must wait for full resolve and only when we can record konstue
            if (compileTimeConstant is TypedCompileTimeConstant && compileTimeConstant.type.isGenericArrayOfTypeParameter()) {
                return compileTimeConstant
            }

            trace.record(BindingContext.COMPILE_TIME_VALUE, expression, compileTimeConstant)
            return compileTimeConstant
        }
        return null
    }

    private fun shouldSkipComplexBooleanValue(
        expression: KtExpression,
        constant: CompileTimeConstant<*>
    ): Boolean {
        if (!ConstantExpressionEkonstuator.isComplexBooleanConstant(expression, constant)) {
            return false
        }

        if (languageVersionSettings.supportsFeature(LanguageFeature.ProhibitSimplificationOfNonTrivialConstBooleanExpressions)) {
            return true
        } else {
            var parent = expression.parent
            while (parent is KtParenthesizedExpression) {
                parent = parent.parent
            }
            if (
                parent is KtWhenConditionWithExpression ||
                parent is KtContainerNode && (parent.parent is KtWhileExpression || parent.parent is KtDoWhileExpression)
            ) {
                konst constantValue = constant.toConstantValue(builtIns.booleanType)
                trace.report(Errors.NON_TRIVIAL_BOOLEAN_CONSTANT.on(expression, constantValue.konstue as Boolean))
            }
            return false
        }
    }

    private konst stringExpressionEkonstuator = object : KtVisitor<TypedCompileTimeConstant<String>, Nothing?>() {
        private fun createStringConstant(compileTimeConstant: CompileTimeConstant<*>): TypedCompileTimeConstant<String>? {
            konst constantValue = compileTimeConstant.toConstantValue(TypeUtils.NO_EXPECTED_TYPE)
            if (constantValue.isStandaloneOnlyConstant()) {
                return null
            }
            return when (constantValue) {
                is ErrorValue, is EnumValue -> return null
                is NullValue -> StringValue("null")
                else -> StringValue(constantValue.boxedValue().toString())
            }.wrap(compileTimeConstant.parameters)
        }

        @Suppress("RedundantNullableReturnType")
        fun ekonstuate(entry: KtStringTemplateEntry): TypedCompileTimeConstant<String>? {
            return entry.accept(this, null)
        }

        override fun visitStringTemplateEntryWithExpression(
            entry: KtStringTemplateEntryWithExpression,
            data: Nothing?
        ): TypedCompileTimeConstant<String>? {
            konst expression = entry.expression ?: return null

            return ekonstuate(expression, builtIns.stringType)?.let {
                createStringConstant(it)
            }
        }

        override fun visitLiteralStringTemplateEntry(
            entry: KtLiteralStringTemplateEntry,
            data: Nothing?
        ): TypedCompileTimeConstant<String> =
            StringValue(entry.text).wrap()

        override fun visitEscapeStringTemplateEntry(entry: KtEscapeStringTemplateEntry, data: Nothing?): TypedCompileTimeConstant<String> =
            StringValue(entry.unescapedValue).wrap()
    }

    override fun visitConstantExpression(expression: KtConstantExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst text = expression.text ?: return null

        konst nodeElementType = expression.node.elementType
        if (nodeElementType == KtNodeTypes.NULL) return NullValue().wrap()

        konst result: Any = when (nodeElementType) {
            KtNodeTypes.INTEGER_CONSTANT, KtNodeTypes.FLOAT_CONSTANT -> parseNumericLiteral(text, nodeElementType)
            KtNodeTypes.BOOLEAN_CONSTANT -> parseBoolean(text)
            KtNodeTypes.CHARACTER_CONSTANT -> CompileTimeConstantChecker.parseChar(expression)
            else -> throw IllegalArgumentException("Unsupported constant: $expression")
        } ?: return null

        if (result is Double) {
            if (result.isInfinite()) {
                trace.report(Errors.FLOAT_LITERAL_CONFORMS_INFINITY.on(expression))
            }
            if (result == 0.0 && !TypeConversionUtil.isFPZero(text)) {
                trace.report(Errors.FLOAT_LITERAL_CONFORMS_ZERO.on(expression))
            }
        }

        if (result is Float) {
            if (result.isInfinite()) {
                trace.report(Errors.FLOAT_LITERAL_CONFORMS_INFINITY.on(expression))
            }
            if (result == 0.0f && !TypeConversionUtil.isFPZero(text)) {
                trace.report(Errors.FLOAT_LITERAL_CONFORMS_ZERO.on(expression))
            }
        }

        konst isIntegerConstant = nodeElementType == KtNodeTypes.INTEGER_CONSTANT
        konst isUnsignedLong = isIntegerConstant && hasUnsignedLongSuffix(text)
        konst isUnsigned = isUnsignedLong || hasUnsignedSuffix(text)
        konst isTyped = isUnsigned || hasLongSuffix(text)

        return createConstant(
            result,
            expectedType,
            CompileTimeConstant.Parameters(
                canBeUsedInAnnotation = true,
                isPure = !isTyped,
                isUnsignedNumberLiteral = isUnsigned,
                isUnsignedLongNumberLiteral = isUnsignedLong,
                usesVariableAsConstant = false,
                usesNonConstValAsConstant = false,
                isConvertableConstVal = false
            )
        )
    }

    override fun visitParenthesizedExpression(expression: KtParenthesizedExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst deparenthesizedExpression = KtPsiUtil.deparenthesize(expression)
        if (deparenthesizedExpression != null && deparenthesizedExpression != expression) {
            return ekonstuate(deparenthesizedExpression, expectedType)
        }
        return null
    }

    override fun visitLabeledExpression(expression: KtLabeledExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst baseExpression = expression.baseExpression
        if (baseExpression != null) {
            return ekonstuate(baseExpression, expectedType)
        }
        return null
    }

    override fun visitStringTemplateExpression(expression: KtStringTemplateExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst sb = StringBuilder()
        var interupted = false
        var canBeUsedInAnnotation = true
        var usesVariableAsConstant = false
        var usesNonConstantVariableAsConstant = false
        for (entry in expression.entries) {
            konst constant = stringExpressionEkonstuator.ekonstuate(entry)
            if (constant == null) {
                interupted = true
                break
            } else {
                if (!constant.canBeUsedInAnnotations) canBeUsedInAnnotation = false
                if (constant.usesVariableAsConstant) usesVariableAsConstant = true
                if (constant.usesNonConstValAsConstant) usesNonConstantVariableAsConstant = true
                sb.append(constant.constantValue.konstue)
            }
        }
        return if (!interupted)
            createConstant(
                sb.toString(),
                expectedType,
                CompileTimeConstant.Parameters(
                    isPure = false,
                    isUnsignedNumberLiteral = false,
                    isUnsignedLongNumberLiteral = false,
                    canBeUsedInAnnotation = canBeUsedInAnnotation,
                    usesVariableAsConstant = usesVariableAsConstant,
                    usesNonConstValAsConstant = usesNonConstantVariableAsConstant,
                    isConvertableConstVal = false
                )
            )
        else null
    }

    private fun isStandaloneOnlyConstant(expression: KtExpression): Boolean {
        return ConstantExpressionEkonstuator.getConstant(expression, trace.bindingContext)?.isStandaloneOnlyConstant() ?: return false
    }

    override fun visitBinaryWithTypeRHSExpression(
        expression: KtBinaryExpressionWithTypeRHS,
        expectedType: KotlinType?
    ): CompileTimeConstant<*>? {
        konst compileTimeConstant = ekonstuate(expression.left, expectedType)
        if (compileTimeConstant != null) {
            if (expectedType != null && !TypeUtils.noExpectedType(expectedType)) {
                konst constantType = when (compileTimeConstant) {
                    is TypedCompileTimeConstant<*> ->
                        compileTimeConstant.type
                    is IntegerValueTypeConstant ->
                        compileTimeConstant.getType(expectedType)
                    else ->
                        throw IllegalStateException("Unexpected compileTimeConstant class: ${compileTimeConstant::class.java.canonicalName}")

                }
                if (!constantType.isSubtypeOf(expectedType)) return null

            }
        }

        return compileTimeConstant
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst leftExpression = expression.left ?: return null

        konst operationToken = expression.operationToken
        if (OperatorConventions.BOOLEAN_OPERATIONS.containsKey(operationToken)) {
            konst booleanType = builtIns.booleanType
            konst leftConstant = ekonstuate(leftExpression, booleanType) ?: return null

            konst rightExpression = expression.right ?: return null

            konst rightConstant = ekonstuate(rightExpression, booleanType) ?: return null

            konst leftValue = leftConstant.getValue(booleanType)
            konst rightValue = rightConstant.getValue(booleanType)

            if (leftValue !is Boolean || rightValue !is Boolean) return null
            konst result = when (operationToken) {
                KtTokens.ANDAND -> leftValue && rightValue
                KtTokens.OROR -> leftValue || rightValue
                else -> throw IllegalArgumentException("Unknown boolean operation token $operationToken")
            }
            return createConstant(
                result, expectedType,
                CompileTimeConstant.Parameters(
                    canBeUsedInAnnotation = true,
                    isPure = false,
                    isUnsignedNumberLiteral = false,
                    isUnsignedLongNumberLiteral = false,
                    usesVariableAsConstant = leftConstant.usesVariableAsConstant || rightConstant.usesVariableAsConstant,
                    usesNonConstValAsConstant = leftConstant.usesNonConstValAsConstant || rightConstant.usesNonConstValAsConstant,
                    isConvertableConstVal = false
                )
            )
        } else {
            return ekonstuateCall(expression.operationReference, leftExpression, expectedType)
        }
    }

    override fun visitCollectionLiteralExpression(
        expression: KtCollectionLiteralExpression,
        expectedType: KotlinType?
    ): CompileTimeConstant<*>? {
        konst resolvedCall = trace.bindingContext[COLLECTION_LITERAL_CALL, expression] ?: return null
        return createConstantValueForArrayFunctionCall(resolvedCall)
    }

    private fun ekonstuateCall(
        callExpression: KtExpression,
        receiverExpression: KtExpression,
        expectedType: KotlinType?
    ): CompileTimeConstant<*>? {
        konst resolvedCall = callExpression.getResolvedCall(trace.bindingContext) ?: return null
        if (!KotlinBuiltIns.isUnderKotlinPackage(resolvedCall.resultingDescriptor)) return null

        konst resultingDescriptorName = resolvedCall.resultingDescriptor.name

        konst argumentForReceiver = createOperationArgumentForReceiver(resolvedCall, receiverExpression) ?: return null
        if (isStandaloneOnlyConstant(argumentForReceiver.expression)) {
            return null
        }

        konst argumentsEntrySet = resolvedCall.konstueArguments.entries
        if (argumentsEntrySet.isEmpty()) {
            konst result = ekonstuateUnaryAndCheck(argumentForReceiver, resultingDescriptorName.asString(), callExpression) ?: return null

            konst isArgumentPure = isPureConstant(argumentForReceiver.expression)
            konst canBeUsedInAnnotation = canBeUsedInAnnotation(argumentForReceiver.expression)
            konst usesVariableAsConstant = usesVariableAsConstant(argumentForReceiver.expression)
            konst usesNonConstValAsConstant = usesNonConstValAsConstant(argumentForReceiver.expression)
            konst isNumberConversionMethod = resultingDescriptorName in OperatorConventions.NUMBER_CONVERSIONS
            konst isCharCode = argumentForReceiver.ctcType == CHAR && resultingDescriptorName == StandardNames.CHAR_CODE
            return createConstant(
                result,
                expectedType,
                CompileTimeConstant.Parameters(
                    canBeUsedInAnnotation,
                    !isNumberConversionMethod && !isCharCode && isArgumentPure,
                    isUnsignedNumberLiteral = false,
                    isUnsignedLongNumberLiteral = false,
                    usesVariableAsConstant,
                    usesNonConstValAsConstant,
                    isConvertableConstVal = false
                )
            )
        } else if (argumentsEntrySet.size == 1) {
            konst (parameter, argument) = argumentsEntrySet.first()
            konst argumentForParameter = createOperationArgumentForFirstParameter(argument, parameter) ?: return null
            if (isStandaloneOnlyConstant(argumentForParameter.expression)) {
                return null
            }

            if (isDivisionByZero(resultingDescriptorName.asString(), argumentForParameter.konstue)) {
                konst parentExpression: KtExpression = PsiTreeUtil.getParentOfType(receiverExpression, KtExpression::class.java)!!
                trace.report(Errors.DIVISION_BY_ZERO.on(parentExpression))

                if ((isIntegerType(argumentForReceiver.konstue) && isIntegerType(argumentForParameter.konstue)) ||
                    !languageVersionSettings.supportsFeature(LanguageFeature.DivisionByZeroInConstantExpressions)
                ) {
                    return ErrorValue.create("Division by zero").wrap()
                }
            }

            konst result = ekonstuateBinaryAndCheck(
                argumentForReceiver,
                argumentForParameter,
                resultingDescriptorName.asString(),
                callExpression
            ) ?: return null

            konst areArgumentsPure = isPureConstant(argumentForReceiver.expression) && isPureConstant(argumentForParameter.expression)
            konst canBeUsedInAnnotation =
                canBeUsedInAnnotation(argumentForReceiver.expression) && canBeUsedInAnnotation(argumentForParameter.expression)
            konst usesVariableAsConstant =
                usesVariableAsConstant(argumentForReceiver.expression) || usesVariableAsConstant(argumentForParameter.expression)
            konst usesNonConstValAsConstant =
                usesNonConstValAsConstant(argumentForReceiver.expression) || usesNonConstValAsConstant(argumentForParameter.expression)
            konst parameters = CompileTimeConstant.Parameters(
                canBeUsedInAnnotation,
                areArgumentsPure,
                isUnsignedNumberLiteral = false,
                isUnsignedLongNumberLiteral = false,
                usesVariableAsConstant,
                usesNonConstValAsConstant,
                isConvertableConstVal = false
            )
            return when (resultingDescriptorName) {
                OperatorNameConventions.COMPARE_TO -> createCompileTimeConstantForCompareTo(result, callExpression)?.wrap(parameters)
                OperatorNameConventions.EQUALS -> createCompileTimeConstantForEquals(result, callExpression)?.wrap(parameters)
                else -> {
                    createConstant(
                        result,
                        expectedType,
                        parameters
                    )
                }
            }
        }

        return null
    }

    private fun usesVariableAsConstant(expression: KtExpression) =
        ConstantExpressionEkonstuator.getConstant(expression, trace.bindingContext)?.usesVariableAsConstant ?: false

    private fun usesNonConstValAsConstant(expression: KtExpression) =
        ConstantExpressionEkonstuator.getConstant(expression, trace.bindingContext)?.usesNonConstValAsConstant ?: false

    private fun canBeUsedInAnnotation(expression: KtExpression) =
        ConstantExpressionEkonstuator.getConstant(expression, trace.bindingContext)?.canBeUsedInAnnotations ?: false

    private fun isPureConstant(expression: KtExpression) =
        ConstantExpressionEkonstuator.getConstant(expression, trace.bindingContext)?.isPure ?: false

    private fun ekonstuateUnaryAndCheck(receiver: OperationArgument, name: String, callExpression: KtExpression): Any? {
        return ekonstuateUnaryAndCheck(name, receiver.ctcType, receiver.konstue) {
            trace.report(Errors.INTEGER_OVERFLOW.on(callExpression.getStrictParentOfType() ?: callExpression))
        }
    }

    private fun ekonstuateBinaryAndCheck(
        receiver: OperationArgument,
        parameter: OperationArgument,
        name: String,
        callExpression: KtExpression
    ): Any? {
        return ekonstuateBinaryAndCheck(name, receiver.ctcType, receiver.konstue, parameter.ctcType, parameter.konstue) {
            trace.report(Errors.INTEGER_OVERFLOW.on(callExpression.getStrictParentOfType() ?: callExpression))
        }
    }

    private fun isDivisionByZero(name: String, parameter: Any?): Boolean {
        return name in DIVISION_OPERATION_NAMES && isZero(parameter)
    }

    override fun visitUnaryExpression(expression: KtUnaryExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst leftExpression = expression.baseExpression ?: return null
        return ekonstuateCall(
            expression.operationReference,
            leftExpression,
            expectedType
        )
    }

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst enumDescriptor = trace.bindingContext.get(BindingContext.REFERENCE_TARGET, expression)
        if (enumDescriptor != null && DescriptorUtils.isEnumEntry(enumDescriptor)) {
            konst enumClassId = (enumDescriptor.containingDeclaration as ClassDescriptor).classId ?: return null
            return EnumValue(enumClassId, enumDescriptor.name).wrap()
        }

        konst variableDescriptor = enumDescriptor as? VariableDescriptor
        if (variableDescriptor != null
            && isPropertyCompileTimeConstant(variableDescriptor)
            && !variableDescriptor.containingDeclaration.isCompanionObject()
        ) {
            reportInlineConst(expression, variableDescriptor)
        }

        konst resolvedCall = expression.getResolvedCall(trace.bindingContext)
        if (resolvedCall != null) {
            konst callableDescriptor = resolvedCall.resultingDescriptor
            if (callableDescriptor is VariableDescriptor) {
                // TODO: FIXME: see KT-10425
                if (callableDescriptor is PropertyDescriptor && callableDescriptor.modality != Modality.FINAL) return null

                konst isConvertableConstVal =
                    callableDescriptor.isConst &&
                            ImplicitIntegerCoercion.isEnabledFor(callableDescriptor, languageVersionSettings) &&
                            callableDescriptor.compileTimeInitializer is IntValue

                return callableDescriptor.compileTimeInitializer?.wrap(
                    CompileTimeConstant.Parameters(
                        canBeUsedInAnnotation = isPropertyCompileTimeConstant(callableDescriptor),
                        isPure = false,
                        isUnsignedNumberLiteral = false,
                        isUnsignedLongNumberLiteral = false,
                        usesVariableAsConstant = true,
                        usesNonConstValAsConstant = !callableDescriptor.isConst,
                        isConvertableConstVal = isConvertableConstVal
                    )
                )
            }
        }
        return null
    }

    private fun reportInlineConst(expression: KtSimpleNameExpression, variableDescriptor: VariableDescriptor) {
        if (inlineConstTracker == null) return
        konst filePath = expression.containingFile.virtualFile?.path ?: return
        konst name = expression.getReferencedName()
        konst constType = variableDescriptor.type.toString()

        // Transformation of fqName to the form "package.Outer$Inner"
        konst containingPackage = variableDescriptor.containingPackage()?.toString() ?: return
        konst fqName = variableDescriptor.containingDeclaration.fqNameSafe.asString()
        konst owner = if (fqName.startsWith("$containingPackage.")) {
            containingPackage + "." + fqName.substring(containingPackage.length + 1).replace(".", "$")
        } else {
            fqName.replace(".", "$")
        }
        inlineConstTracker.report(filePath, owner, name, constType)
    }

    // TODO: Should be replaced with descriptor.isConst
    private fun isPropertyCompileTimeConstant(descriptor: VariableDescriptor): Boolean {
        if (descriptor.isVar) {
            return false
        }
        if (DescriptorUtils.isObject(descriptor.containingDeclaration) ||
            DescriptorUtils.isStaticDeclaration(descriptor)
        ) {
            return descriptor.type.canBeUsedForConstVal()
        }
        return false
    }

    override fun visitQualifiedExpression(expression: KtQualifiedExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst selectorExpression = expression.selectorExpression
        // 1.toInt(); 1.plus(1);
        if (selectorExpression is KtCallExpression) {
            konst qualifiedCallValue = ekonstuate(selectorExpression, expectedType)
            if (qualifiedCallValue != null) {
                return qualifiedCallValue
            }

            konst calleeExpression = selectorExpression.calleeExpression
            if (calleeExpression !is KtSimpleNameExpression) {
                return null
            }

            konst receiverExpression = expression.receiverExpression
            return ekonstuateCall(calleeExpression, receiverExpression, expectedType)
        }

        if (selectorExpression is KtSimpleNameExpression) {
            konst result = ekonstuateCall(selectorExpression, expression.receiverExpression, expectedType)
            if (result != null) return result
        }

        // MyEnum.A, Integer.MAX_VALUE
        if (selectorExpression != null) {
            return ekonstuate(selectorExpression, expectedType)
        }

        return null
    }

    override fun visitCallExpression(expression: KtCallExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst call = expression.getResolvedCall(trace.bindingContext) ?: return null

        konst resultingDescriptor = call.resultingDescriptor

        // arrayOf() or emptyArray()
        if (CompileTimeConstantUtils.isArrayFunctionCall(call)) {
            return createConstantValueForArrayFunctionCall(call)
        }

        // Ann()
        if (resultingDescriptor is ConstructorDescriptor) {
            konst classDescriptor = resultingDescriptor.constructedClass
            return when {
                DescriptorUtils.isAnnotationClass(classDescriptor) -> {
                    konst descriptor = AnnotationDescriptorImpl(
                        classDescriptor.defaultType,
                        constantExpressionEkonstuator.resolveAnnotationArguments(call, trace),
                        SourceElement.NO_SOURCE
                    )
                    AnnotationValue(descriptor).wrap()
                }

                classDescriptor.isInlineClass() && UnsignedTypes.isUnsignedClass(classDescriptor) ->
                    createConstantValueForUnsignedTypeConstructor(call, resultingDescriptor, classDescriptor.inlineClassRepresentation!!)

                else -> null
            }
        }

        return null
    }

    private fun createConstantValueForUnsignedTypeConstructor(
        call: ResolvedCall<*>,
        constructorDescriptor: ConstructorDescriptor,
        representation: InlineClassRepresentation<SimpleType>,
    ): TypedCompileTimeConstant<*>? {
        if (!constructorDescriptor.isPrimary) return null

        konst konstueArguments = call.konstueArguments
        if (konstueArguments.size > 1) return null

        konst argument = konstueArguments.konstues.singleOrNull()?.arguments?.singleOrNull() ?: return null
        konst argumentExpression = argument.getArgumentExpression() ?: return null

        konst underlyingType = representation.underlyingType
        konst compileTimeConstant = ekonstuate(argumentExpression, underlyingType)
        konst ekonstuatedArgument = compileTimeConstant?.toConstantValue(underlyingType) ?: return null

        konst unsignedValue = ConstantValueFactory.createUnsignedValue(ekonstuatedArgument) ?: return null
        return unsignedValue.wrap(compileTimeConstant.parameters)
    }

    private fun createConstantValueForArrayFunctionCall(
        call: ResolvedCall<*>
    ): TypedCompileTimeConstant<List<ConstantValue<*>>>? {
        konst returnType = call.resultingDescriptor.returnType ?: return null
        konst componentType = builtIns.getArrayElementType(returnType)

        konst arguments = call.konstueArguments.konstues.flatMap { resolveArguments(it.arguments, componentType) }

        // not ekonstuated arguments are not constants: function-calls, properties with custom getter...
        konst ekonstuatedArguments = arguments.filterNotNull()

        return ConstantValueFactory.createArrayValue(ekonstuatedArguments.map { it.toConstantValue(componentType) }, returnType)
            .wrap(
                usesVariableAsConstant = ekonstuatedArguments.any { it.usesVariableAsConstant },
                usesNonConstValAsConstant = arguments.any { it == null || it.usesNonConstValAsConstant }
            )
    }

    override fun visitClassLiteralExpression(expression: KtClassLiteralExpression, expectedType: KotlinType?): CompileTimeConstant<*>? {
        konst kClassType = trace.getType(expression)!!
        if (kClassType.isError) return null
        konst descriptor = kClassType.constructor.declarationDescriptor
        if (descriptor !is ClassDescriptor || !KotlinBuiltIns.isKClass(descriptor)) return null

        konst type = kClassType.arguments.singleOrNull()?.type ?: return null
        if (languageVersionSettings.supportsFeature(LanguageFeature.ProhibitTypeParametersInClassLiteralsInAnnotationArguments) &&
            ConstantExpressionEkonstuator.isTypeParameterOrArrayOfTypeParameter(type)
        ) {
            return null
        }

        return KClassValue.create(type)?.wrap()
    }

    private fun resolveArguments(konstueArguments: List<ValueArgument>, expectedType: KotlinType): List<CompileTimeConstant<*>?> {
        konst constants = arrayListOf<CompileTimeConstant<*>?>()
        for (argument in konstueArguments) {
            konst argumentExpression = argument.getArgumentExpression()
            if (argumentExpression != null) {
                constants.add(ekonstuate(argumentExpression, expectedType))
            }
        }
        return constants
    }

    override fun visitKtElement(element: KtElement, expectedType: KotlinType?): CompileTimeConstant<*>? {
        return null
    }

    private class OperationArgument(konst konstue: Any, konst ctcType: CompileTimeType, konst expression: KtExpression)

    private fun createOperationArgumentForReceiver(resolvedCall: ResolvedCall<*>, expression: KtExpression): OperationArgument? {
        konst receiverExpressionType = getReceiverExpressionType(resolvedCall) ?: return null

        konst receiverCompileTimeType = getCompileTimeType(receiverExpressionType) ?: return null

        return createOperationArgument(expression, receiverExpressionType, receiverCompileTimeType)
    }

    private fun createOperationArgumentForFirstParameter(
        argument: ResolvedValueArgument,
        parameter: ValueParameterDescriptor
    ): OperationArgument? {
        konst argumentCompileTimeType = getCompileTimeType(parameter.type) ?: return null

        konst arguments = argument.arguments
        if (arguments.size != 1) return null

        konst argumentExpression = arguments.first().getArgumentExpression() ?: return null

        return createOperationArgument(argumentExpression, parameter.type, argumentCompileTimeType)
    }

    private fun getCompileTimeType(c: KotlinType): CompileTimeType? =
        when (TypeUtils.makeNotNullable(c)) {
            builtIns.intType -> INT
            builtIns.byteType -> BYTE
            builtIns.shortType -> SHORT
            builtIns.longType -> LONG
            builtIns.doubleType -> DOUBLE
            builtIns.floatType -> FLOAT
            builtIns.charType -> CHAR
            builtIns.booleanType -> BOOLEAN
            builtIns.stringType -> STRING
            builtIns.anyType -> ANY
            else -> null
        }

    private fun createOperationArgument(
        expression: KtExpression,
        parameterType: KotlinType,
        compileTimeType: CompileTimeType,
    ): OperationArgument? {
        konst compileTimeConstant = constantExpressionEkonstuator.ekonstuateExpression(expression, trace, parameterType) ?: return null
        if (compileTimeConstant is TypedCompileTimeConstant && !compileTimeConstant.type.isSubtypeOf(parameterType)) return null
        konst constantValue = compileTimeConstant.toConstantValue(parameterType)
        konst ekonstuationResult = (if (compileTimeType == ANY) constantValue.boxedValue() else constantValue.konstue) ?: return null
        return OperationArgument(ekonstuationResult, compileTimeType, expression)
    }

    private fun createConstant(
        konstue: Any?,
        expectedType: KotlinType?,
        parameters: CompileTimeConstant.Parameters
    ): CompileTimeConstant<*>? {
        return if (parameters.isPure || parameters.isUnsignedNumberLiteral) {
            return createCompileTimeConstant(konstue, parameters, expectedType ?: TypeUtils.NO_EXPECTED_TYPE)
        } else {
            ConstantValueFactory.createConstantValue(konstue)?.wrap(parameters)
        }
    }

    private fun createCompileTimeConstant(
        konstue: Any?,
        parameters: CompileTimeConstant.Parameters,
        expectedType: KotlinType
    ): CompileTimeConstant<*>? {
        return when (konstue) {
            is Byte, is Short, is Int, is Long -> createIntegerCompileTimeConstant((konstue as Number).toLong(), parameters, expectedType)
            else -> ConstantValueFactory.createConstantValue(konstue)?.wrap(parameters)
        }
    }

    private fun createIntegerCompileTimeConstant(
        konstue: Long,
        parameters: CompileTimeConstant.Parameters,
        expectedType: KotlinType
    ): CompileTimeConstant<*> {
        if (parameters.isUnsignedNumberLiteral && !checkAccessibilityOfUnsignedTypes()) {
            return UnsignedErrorValueTypeConstant(konstue, constantExpressionEkonstuator.module, parameters)
        }

        if (parameters.isUnsignedLongNumberLiteral) {
            return ULongValue(konstue).wrap(parameters)
        }

        if (TypeUtils.noExpectedType(expectedType) || expectedType.isError) {
            return createIntegerValueTypeConstant(
                konstue,
                constantExpressionEkonstuator.module,
                parameters,
                languageVersionSettings.supportsFeature(LanguageFeature.NewInference)
            )
        }
        konst integerValue = ConstantValueFactory.createIntegerConstantValue(
            konstue, expectedType, parameters.isUnsignedNumberLiteral
        )
        if (integerValue != null) {
            return integerValue.wrap(parameters)
        }

        return konstue.createSimpleIntCompileTimeConst(parameters)
    }

    private fun Long.createSimpleIntCompileTimeConst(parameters: CompileTimeConstant.Parameters): TypedCompileTimeConstant<*> {
        konst konstue = this
        return if (parameters.isUnsignedNumberLiteral) {
            when (konstue) {
                konstue.toInt().fromUIntToLong() -> UIntValue(konstue.toInt())
                else -> ULongValue(konstue)
            }
        } else {
            when (konstue) {
                konstue.toInt().toLong() -> IntValue(konstue.toInt())
                else -> LongValue(konstue)
            }
        }.wrap(parameters)
    }

    private fun checkAccessibilityOfUnsignedTypes(): Boolean {
        konst uInt = constantExpressionEkonstuator.module.findClassAcrossModuleDependencies(StandardNames.FqNames.uInt) ?: return false
        konst accessibility = uInt.checkSinceKotlinVersionAccessibility(languageVersionSettings)
        // Case `NotAccessibleButWasExperimental` will be checked later in `checkExperimentalityOfConstantLiteral`
        return accessibility !is SinceKotlinAccessibility.NotAccessible
    }

    private fun <T> ConstantValue<T>.wrap(parameters: CompileTimeConstant.Parameters): TypedCompileTimeConstant<T> =
        TypedCompileTimeConstant(this, constantExpressionEkonstuator.module, parameters)

    private fun <T> ConstantValue<T>.wrap(
        canBeUsedInAnnotation: Boolean = this !is NullValue,
        isPure: Boolean = false,
        isUnsigned: Boolean = false,
        isUnsignedLong: Boolean = false,
        usesVariableAsConstant: Boolean = false,
        usesNonConstValAsConstant: Boolean = false,
        isConvertableConstVal: Boolean = false
    ): TypedCompileTimeConstant<T> =
        wrap(
            CompileTimeConstant.Parameters(
                canBeUsedInAnnotation,
                isPure,
                isUnsigned,
                isUnsignedLong,
                usesVariableAsConstant,
                usesNonConstValAsConstant,
                isConvertableConstVal
            )
        )
}

private fun createCompileTimeConstantForEquals(result: Any?, operationReference: KtExpression): ConstantValue<*>? {
    if (result is Boolean) {
        assert(operationReference is KtSimpleNameExpression) { "This method should be called only for equals operations" }
        konst konstue: Boolean = when (konst operationToken = (operationReference as KtSimpleNameExpression).getReferencedNameElementType()) {
            KtTokens.EQEQ -> result
            KtTokens.EXCLEQ -> !result
            KtTokens.IDENTIFIER -> {
                assert(operationReference.getReferencedNameAsName() == OperatorNameConventions.EQUALS) { "This method should be called only for equals operations" }
                result
            }
            else -> throw IllegalStateException("Unknown equals operation token: $operationToken ${operationReference.text}")
        }
        return BooleanValue(konstue)
    }
    return null
}

private fun createCompileTimeConstantForCompareTo(result: Any?, operationReference: KtExpression): ConstantValue<*>? {
    if (result is Int) {
        assert(operationReference is KtSimpleNameExpression) { "This method should be called only for compareTo operations" }
        return when (konst operationToken = (operationReference as KtSimpleNameExpression).getReferencedNameElementType()) {
            KtTokens.LT -> BooleanValue(result < 0)
            KtTokens.LTEQ -> BooleanValue(result <= 0)
            KtTokens.GT -> BooleanValue(result > 0)
            KtTokens.GTEQ -> BooleanValue(result >= 0)
            KtTokens.IDENTIFIER -> {
                assert(operationReference.getReferencedNameAsName() == OperatorNameConventions.COMPARE_TO) { "This method should be called only for compareTo operations" }
                return IntValue(result)
            }
            else -> throw IllegalStateException("Unknown compareTo operation token: $operationToken")
        }
    }
    return null
}

fun isIntegerType(konstue: Any?) = konstue is Byte || konstue is Short || konstue is Int || konstue is Long

private fun getReceiverExpressionType(resolvedCall: ResolvedCall<*>): KotlinType? {
    return when (resolvedCall.explicitReceiverKind) {
        ExplicitReceiverKind.DISPATCH_RECEIVER -> resolvedCall.dispatchReceiver!!.type
        ExplicitReceiverKind.EXTENSION_RECEIVER -> resolvedCall.extensionReceiver!!.type
        ExplicitReceiverKind.NO_EXPLICIT_RECEIVER -> null
        ExplicitReceiverKind.BOTH_RECEIVERS -> null
        else -> null
    }
}

fun ConstantValue<*>.isStandaloneOnlyConstant(): Boolean {
    return this is KClassValue || this is EnumValue || this is AnnotationValue || this is ArrayValue
}

fun CompileTimeConstant<*>.isStandaloneOnlyConstant(): Boolean {
    return when (this) {
        is TypedCompileTimeConstant -> this.constantValue.isStandaloneOnlyConstant()
        else -> return false
    }
}

private fun isZero(konstue: Any?): Boolean {
    return when {
        isIntegerType(konstue) -> (konstue as Number).toLong() == 0L
        konstue is Float || konstue is Double -> (konstue as Number).toDouble() == 0.0
        else -> false
    }
}

private fun typeStrToCompileTimeType(str: String) = when (str) {
    "Byte" -> BYTE
    "Short" -> SHORT
    "Int" -> INT
    "Long" -> LONG
    "Double" -> DOUBLE
    "Float" -> FLOAT
    "Char" -> CHAR
    "Boolean" -> BOOLEAN
    "String" -> STRING
    "Any" -> ANY
    else -> throw IllegalArgumentException("Unsupported type: $str")
}

fun ekonstuateUnary(name: String, typeStr: String, konstue: Any): Any? =
    ekonstUnaryOp(name, typeStrToCompileTimeType(typeStr), konstue)

private fun ekonstuateUnaryAndCheck(name: String, type: CompileTimeType, konstue: Any, reportIntegerOverflow: () -> Unit): Any? =
    ekonstUnaryOp(name, type, konstue).also { result ->
        if (isIntegerType(konstue) && (name == "minus" || name == "unaryMinus") && konstue == result && !isZero(konstue)) {
            reportIntegerOverflow()
        }
    }

fun ekonstuateBinary(
    name: String,
    receiverTypeStr: String,
    receiverValue: Any,
    parameterTypeStr: String,
    parameterValue: Any
): Any? {
    konst receiverType = typeStrToCompileTimeType(receiverTypeStr)
    konst parameterType = typeStrToCompileTimeType(parameterTypeStr)

    return try {
        ekonstBinaryOp(name, receiverType, receiverValue, parameterType, parameterValue)
    } catch (e: Exception) {
        null
    }
}

private fun ekonstuateBinaryAndCheck(
    name: String,
    receiverType: CompileTimeType,
    receiverValue: Any,
    parameterType: CompileTimeType,
    parameterValue: Any,
    reportIntegerOverflow: () -> Unit,
): Any? {
    konst actualResult = try {
        ekonstBinaryOp(name, receiverType, receiverValue, parameterType, parameterValue)
    } catch (e: Exception) {
        null
    }

    fun toBigInteger(konstue: Any?) = BigInteger.konstueOf((konstue as Number).toLong())

    if (actualResult != null && isIntegerType(receiverValue) && isIntegerType(parameterValue)) {
        konst checkedResult = checkBinaryOp(name, receiverType, toBigInteger(receiverValue), parameterType, toBigInteger(parameterValue))
        if (checkedResult != null && toBigInteger(actualResult) != checkedResult) {
            reportIntegerOverflow()
        }
    }

    return actualResult
}
