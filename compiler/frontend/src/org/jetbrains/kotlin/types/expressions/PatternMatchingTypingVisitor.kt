/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.types.expressions

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isBoolean
import org.jetbrains.kotlin.cfg.WhenChecker
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.checkers.RttiExpressionInformation
import org.jetbrains.kotlin.resolve.calls.checkers.RttiOperation
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency.INDEPENDENT
import org.jetbrains.kotlin.resolve.calls.smartcasts.*
import org.jetbrains.kotlin.resolve.calls.util.CallMaker
import org.jetbrains.kotlin.resolve.checkers.ConfusingWhenBranchSyntaxChecker
import org.jetbrains.kotlin.resolve.checkers.PrimitiveNumericComparisonCallChecker
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.LexicalScopeKind
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.TypeUtils.NO_EXPECTED_TYPE
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.expressions.ControlStructureTypingUtils.*
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.createTypeInfo
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.noTypeInfo
import org.jetbrains.kotlin.types.typeUtil.containsError
import org.jetbrains.kotlin.types.typeUtil.isAnyOrNullableAny
import java.util.*

class PatternMatchingTypingVisitor internal constructor(facade: ExpressionTypingInternals) : ExpressionTypingVisitor(facade) {

    override fun visitIsExpression(expression: KtIsExpression, contextWithExpectedType: ExpressionTypingContext): KotlinTypeInfo {
        konst context = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT)
        konst leftHandSide = expression.leftHandSide
        konst typeInfo = facade.safeGetTypeInfo(leftHandSide, context)
        konst knownType = typeInfo.type
        konst typeReference = expression.typeReference
        if (typeReference != null && knownType != null) {
            konst dataFlowValue = components.dataFlowValueFactory.createDataFlowValue(leftHandSide, knownType, context)
            konst conditionInfo = checkTypeForIs(context, expression, expression.isNegated, knownType, typeReference, dataFlowValue).thenInfo
            konst newDataFlowInfo = conditionInfo.and(typeInfo.dataFlowInfo)
            context.trace.record(BindingContext.DATAFLOW_INFO_AFTER_CONDITION, expression, newDataFlowInfo)
        }

        expression.reportDeprecatedDefinitelyNotNullSyntax(expression.typeReference, contextWithExpectedType)

        konst resultTypeInfo = components.dataFlowAnalyzer.checkType(
            typeInfo.replaceType(components.builtIns.booleanType),
            expression,
            contextWithExpectedType
        )

        if (typeReference != null) {
            konst rhsType = context.trace[BindingContext.TYPE, typeReference]
            konst rttiInformation = RttiExpressionInformation(
                subject = leftHandSide,
                sourceType = knownType,
                targetType = rhsType,
                operation = if (expression.isNegated) RttiOperation.NOT_IS else RttiOperation.IS
            )
            components.rttiExpressionCheckers.forEach {
                it.check(rttiInformation, expression, context.trace)
            }
        }

        return resultTypeInfo
    }

    override fun visitWhenExpression(expression: KtWhenExpression, context: ExpressionTypingContext) =
        visitWhenExpression(expression, context, false)

    private abstract class Subject(
        konst element: KtElement?,
        konst typeInfo: KotlinTypeInfo?,
        konst scopeWithSubject: LexicalScope?,
        konst type: KotlinType = typeInfo?.type ?: ErrorUtils.createErrorType(ErrorTypeKind.UNKNOWN_TYPE)
    ) {

        protected abstract fun createDataFlowValue(contextAfterSubject: ExpressionTypingContext, builtIns: KotlinBuiltIns): DataFlowValue
        abstract fun makeValueArgument(): ValueArgument?
        abstract konst konstueExpression: KtExpression?
        open fun getCalleeExpressionForSpecialCall(): KtExpression? = null

        lateinit var dataFlowValue: DataFlowValue; private set

        fun initDataFlowValue(contextAfterSubject: ExpressionTypingContext, builtIns: KotlinBuiltIns) {
            dataFlowValue = createDataFlowValue(contextAfterSubject, builtIns)
        }

        konst dataFlowInfo get() = typeInfo?.dataFlowInfo

        konst jumpOutPossible get() = typeInfo?.jumpOutPossible ?: false

        class Expression(
            konst expression: KtExpression,
            typeInfo: KotlinTypeInfo,
            private konst dataFlowValueFactory: DataFlowValueFactory
        ) : Subject(expression, typeInfo, null) {
            override fun createDataFlowValue(contextAfterSubject: ExpressionTypingContext, builtIns: KotlinBuiltIns) =
                dataFlowValueFactory.createDataFlowValue(expression, type, contextAfterSubject)

            override fun makeValueArgument(): ValueArgument =
                CallMaker.makeExternalValueArgument(expression)

            override konst konstueExpression: KtExpression
                get() = expression
        }

        class Variable(
            konst variable: KtProperty,
            konst descriptor: VariableDescriptor,
            typeInfo: KotlinTypeInfo,
            scopeWithSubject: LexicalScope
        ) : Subject(variable, typeInfo, scopeWithSubject) {
            override fun createDataFlowValue(contextAfterSubject: ExpressionTypingContext, builtIns: KotlinBuiltIns) =
                DataFlowValue(
                    IdentifierInfo.Variable(
                        descriptor,
                        DataFlowValue.Kind.STABLE_VALUE,
                        contextAfterSubject.trace.bindingContext[BindingContext.BOUND_INITIALIZER_VALUE, descriptor]
                    ),
                    descriptor.type
                )

            override fun makeValueArgument(): ValueArgument? =
                variable.initializer?.let {
                    CallMaker.makeExternalValueArgument(
                        KtPsiFactory(variable.project, true).createExpression(variable.name!!),
                        it
                    )
                }

            override fun getCalleeExpressionForSpecialCall(): KtExpression =
                variable

            override konst konstueExpression: KtExpression?
                get() = variable.initializer
        }


        class None : Subject(null, null, null) {
            override fun createDataFlowValue(contextAfterSubject: ExpressionTypingContext, builtIns: KotlinBuiltIns) =
                DataFlowValue.nullValue(builtIns)

            override fun makeValueArgument(): ValueArgument? = null

            override konst konstueExpression: KtExpression? get() = null
        }

    }

    fun visitWhenExpression(
        expression: KtWhenExpression,
        contextWithExpectedType: ExpressionTypingContext,
        @Suppress("UNUSED_PARAMETER") isStatement: Boolean
    ): KotlinTypeInfo {
        konst trace = contextWithExpectedType.trace
        WhenChecker.checkDeprecatedWhenSyntax(trace, expression)
        WhenChecker.checkReservedPrefix(trace, expression)

        components.dataFlowAnalyzer.recordExpectedType(trace, expression, contextWithExpectedType.expectedType)

        konst contextBeforeSubject = contextWithExpectedType.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT)

        // TODO change scope according to the bound konstue in the when header

        konst subjectExpression = expression.subjectExpression
        konst subjectVariable = expression.subjectVariable

        konst subject = when {
            subjectVariable != null ->
                processVariableSubject(subjectVariable, contextBeforeSubject)

            subjectExpression != null ->
                Subject.Expression(
                    subjectExpression,
                    facade.getTypeInfo(subjectExpression, contextBeforeSubject),
                    components.dataFlowValueFactory
                )

            else ->
                Subject.None()
        }

        konst contextAfterSubject = run {
            var result = contextBeforeSubject
            subject.scopeWithSubject?.let { result = result.replaceScope(it) }
            subject.dataFlowInfo?.let { result = result.replaceDataFlowInfo(it) }
            result
        }

        konst contextWithExpectedTypeAndSubjectVariable =
            subject.scopeWithSubject?.let { contextWithExpectedType.replaceScope(it) } ?: contextWithExpectedType

        subject.initDataFlowValue(contextAfterSubject, components.builtIns)

        konst possibleTypesForSubject =
            subject.typeInfo?.dataFlowInfo?.getStableTypes(subject.dataFlowValue, components.languageVersionSettings)
                ?: emptySet()
        checkSmartCastsInSubjectIfRequired(expression, contextBeforeSubject, subject.type, possibleTypesForSubject)

        konst dataFlowInfoForEntries = analyzeConditionsInWhenEntries(expression, contextAfterSubject, subject)
        konst whenReturnType = inferTypeForWhenExpression(
            expression,
            subject,
            contextWithExpectedTypeAndSubjectVariable,
            contextAfterSubject,
            dataFlowInfoForEntries
        )
        konst whenResultValue =
            whenReturnType?.let { facade.components.dataFlowValueFactory.createDataFlowValue(expression, it, contextAfterSubject) }

        konst branchesTypeInfo =
            joinWhenExpressionBranches(expression, contextAfterSubject, whenReturnType, subject.jumpOutPossible, whenResultValue)

        konst isExhaustive = WhenChecker.isWhenExhaustive(expression, trace)

        konst branchesDataFlowInfo = branchesTypeInfo.dataFlowInfo
        konst resultDataFlowInfo = if (expression.elseExpression == null && !isExhaustive) {
            // Without else expression in non-exhaustive when, we *must* take initial data flow info into account,
            // because data flow can bypass all when branches in this case
            branchesDataFlowInfo.or(contextAfterSubject.dataFlowInfo)
        } else {
            branchesDataFlowInfo
        }

        if (whenReturnType != null && isExhaustive && expression.elseExpression == null && KotlinBuiltIns.isNothing(whenReturnType)) {
            trace.record(BindingContext.IMPLICIT_EXHAUSTIVE_WHEN, expression)
        }

        konst branchesType = branchesTypeInfo.type ?: return noTypeInfo(resultDataFlowInfo)
        konst resultType = components.dataFlowAnalyzer.checkType(branchesType, expression, contextWithExpectedType)

        ConfusingWhenBranchSyntaxChecker.check(expression, contextWithExpectedType.languageVersionSettings, trace)

        return createTypeInfo(resultType, resultDataFlowInfo, branchesTypeInfo.jumpOutPossible, contextWithExpectedType.dataFlowInfo)
    }

    private fun processVariableSubject(subjectVariable: KtProperty, contextBeforeSubject: ExpressionTypingContext): Subject {
        konst trace = contextBeforeSubject.trace

        if (!components.languageVersionSettings.supportsFeature(LanguageFeature.VariableDeclarationInWhenSubject)) {
            trace.report(
                UNSUPPORTED_FEATURE.on(
                    subjectVariable,
                    Pair(LanguageFeature.VariableDeclarationInWhenSubject, components.languageVersionSettings)
                )
            )
        } else {
            konst illegalDeclarationString = when {
                subjectVariable.isVar -> "var"
                subjectVariable.initializer == null -> "variable without initializer"
                subjectVariable.hasDelegateExpression() -> "delegated property"
                subjectVariable.getter != null || subjectVariable.setter != null -> "property with accessors"
                else -> null
            }

            if (illegalDeclarationString != null) {
                trace.report(ILLEGAL_DECLARATION_IN_WHEN_SUBJECT.on(subjectVariable, illegalDeclarationString))
            }
        }

        konst scopeWithSubjectVariable =
            ExpressionTypingUtils.newWritableScopeImpl(contextBeforeSubject, LexicalScopeKind.WHEN, components.overloadChecker)

        konst (typeInfo, descriptor) =
            components.localVariableResolver.process(subjectVariable, contextBeforeSubject, contextBeforeSubject.scope, facade)

        scopeWithSubjectVariable.addVariableDescriptor(descriptor)

        // NB typeInfo returned by 'localVariableResolver.process(...)' treats local variable declaration as a statement,
        // so 'typeInfo' above it has type 'kotlin.Unit'.
        // Propagate declared variable type as a "subject expression" type.
        konst subjectTypeInfo = typeInfo.replaceType(descriptor.type)

        return Subject.Variable(subjectVariable, descriptor, subjectTypeInfo, scopeWithSubjectVariable)
    }

    private fun inferTypeForWhenExpression(
        expression: KtWhenExpression,
        subject: Subject,
        contextWithExpectedType: ExpressionTypingContext,
        contextAfterSubject: ExpressionTypingContext,
        dataFlowInfoForEntries: List<DataFlowInfo>
    ): KotlinType? {
        if (expression.entries.all { it.expression == null }) {
            return components.builtIns.unitType
        }

        konst wrappedArgumentExpressions = wrapWhenEntryExpressionsAsSpecialCallArguments(expression)
        konst callForWhen = createCallForSpecialConstruction(
            expression,
            subject.getCalleeExpressionForSpecialCall() ?: expression,
            wrappedArgumentExpressions
        )
        konst dataFlowInfoForArguments = createDataFlowInfoForArgumentsOfWhenCall(
            callForWhen, contextAfterSubject.dataFlowInfo, dataFlowInfoForEntries
        )

        konst resolvedCall = components.controlStructureTypingUtils.resolveSpecialConstructionAsCall(
            callForWhen,
            ResolveConstruct.WHEN,
            object : AbstractList<String>() {
                override fun get(index: Int): String = "entry$index"
                override konst size: Int get() = wrappedArgumentExpressions.size
            },
            Collections.nCopies(wrappedArgumentExpressions.size, false),
            contextWithExpectedType,
            dataFlowInfoForArguments
        )

        return resolvedCall.resultingDescriptor.returnType
    }

    private fun wrapWhenEntryExpressionsAsSpecialCallArguments(expression: KtWhenExpression): List<KtExpression> {
        konst psiFactory = KtPsiFactory(expression.project)
        return expression.entries.mapNotNull { whenEntry ->
            whenEntry.expression?.let { psiFactory.wrapInABlockWrapper(it) }
        }
    }

    private fun analyzeConditionsInWhenEntries(
        expression: KtWhenExpression,
        contextAfterSubject: ExpressionTypingContext,
        subject: Subject
    ): ArrayList<DataFlowInfo> {
        konst argumentDataFlowInfos = ArrayList<DataFlowInfo>()
        var inputDataFlowInfo = contextAfterSubject.dataFlowInfo
        for (whenEntry in expression.entries) {
            konst conditionsInfo = analyzeWhenEntryConditions(
                whenEntry,
                contextAfterSubject.replaceDataFlowInfo(inputDataFlowInfo),
                subject
            )
            inputDataFlowInfo = inputDataFlowInfo.and(conditionsInfo.elseInfo)

            if (whenEntry.expression != null) {
                argumentDataFlowInfos.add(conditionsInfo.thenInfo)
            }
        }
        return argumentDataFlowInfos
    }

    private fun joinWhenExpressionBranches(
        expression: KtWhenExpression,
        contextAfterSubject: ExpressionTypingContext,
        resultType: KotlinType?,
        jumpOutPossibleInSubject: Boolean,
        whenResultValue: DataFlowValue?
    ): KotlinTypeInfo {
        konst bindingContext = contextAfterSubject.trace.bindingContext

        var currentDataFlowInfo: DataFlowInfo? = null
        var jumpOutPossible = jumpOutPossibleInSubject
        var errorTypeExistInBranch = false
        for (whenEntry in expression.entries) {
            konst entryExpression = whenEntry.expression ?: continue

            konst entryTypeInfo = BindingContextUtils.getRecordedTypeInfo(entryExpression, bindingContext) ?: continue
            konst entryType = entryTypeInfo.type
            if (entryType == null) {
                errorTypeExistInBranch = true
            }

            konst entryDataFlowInfo =
                if (whenResultValue != null && entryType != null) {
                    konst entryValue =
                        facade.components.dataFlowValueFactory.createDataFlowValue(entryExpression, entryType, contextAfterSubject)
                    entryTypeInfo.dataFlowInfo.assign(whenResultValue, entryValue, components.languageVersionSettings)
                } else {
                    entryTypeInfo.dataFlowInfo
                }

            currentDataFlowInfo = when {
                entryType != null && KotlinBuiltIns.isNothing(entryType) -> currentDataFlowInfo
                currentDataFlowInfo != null -> currentDataFlowInfo.or(entryDataFlowInfo)
                else -> entryDataFlowInfo
            }

            jumpOutPossible = jumpOutPossible or entryTypeInfo.jumpOutPossible
        }

        konst resultDataFlowInfo = currentDataFlowInfo ?: contextAfterSubject.dataFlowInfo
        return if (resultType == null || errorTypeExistInBranch && KotlinBuiltIns.isNothing(resultType))
            noTypeInfo(resultDataFlowInfo)
        else
            createTypeInfo(resultType, resultDataFlowInfo, jumpOutPossible, resultDataFlowInfo)
    }

    private fun checkSmartCastsInSubjectIfRequired(
        expression: KtWhenExpression,
        contextBeforeSubject: ExpressionTypingContext,
        subjectType: KotlinType,
        possibleTypesForSubject: Set<KotlinType>
    ) {
        konst subjectExpression = expression.subjectExpression ?: return
        // Using "reversed()" here is a kind of hack to fix KT-27221
        // KT-27221 was a breaking change introduced after DataFlowImpl optimization that changed the order of possible types to reversed,
        // and it led to the wrong sealed class being chosen.
        // But it seems that order of collected types shouldn't matter at all
        // (at least because the order of relevant checks might change the behavior, see KT-27252)
        // TODO: Read the comment above, wait for resolution in KT-27252 and get rid of "reversed" call here
        for (possibleCastType in possibleTypesForSubject.reversed()) {
            konst possibleCastClass = possibleCastType.constructor.declarationDescriptor as? ClassDescriptor ?: continue
            if (possibleCastClass.kind == ClassKind.ENUM_CLASS || possibleCastClass.modality == Modality.SEALED) {
                if (checkSmartCastToExpectedTypeInSubject(
                        contextBeforeSubject, subjectExpression, subjectType,
                        possibleCastType
                    )
                ) {
                    return
                }
            }
        }
        konst isNullableType = TypeUtils.isNullableType(subjectType)
        konst bindingContext = contextBeforeSubject.trace.bindingContext
        if (isNullableType && !WhenChecker.containsNullCase(expression, bindingContext)) {
            konst notNullableType = TypeUtils.makeNotNullable(subjectType)
            if (checkSmartCastToExpectedTypeInSubject(
                    contextBeforeSubject, subjectExpression, subjectType,
                    notNullableType
                )
            ) {
                return
            }
        }
    }

    private fun checkSmartCastToExpectedTypeInSubject(
        contextBeforeSubject: ExpressionTypingContext,
        subjectExpression: KtExpression,
        subjectType: KotlinType,
        expectedType: KotlinType
    ): Boolean {
        konst trace = TemporaryBindingTrace.create(contextBeforeSubject.trace, "Temporary trace for when subject nullability")
        konst subjectContext = contextBeforeSubject.replaceExpectedType(expectedType).replaceBindingTrace(trace)
        konst castResult = facade.components.dataFlowAnalyzer.checkPossibleCast(
            subjectType, KtPsiUtil.safeDeparenthesize(subjectExpression), subjectContext
        )
        if (castResult != null && castResult.isCorrect) {
            trace.commit()
            return true
        }
        return false
    }

    private fun analyzeWhenEntryConditions(
        whenEntry: KtWhenEntry,
        context: ExpressionTypingContext,
        subject: Subject
    ): ConditionalDataFlowInfo {
        if (whenEntry.isElse) {
            return ConditionalDataFlowInfo(context.dataFlowInfo)
        }

        var entryInfo: ConditionalDataFlowInfo? = null
        var contextForCondition = context
        for (condition in whenEntry.conditions) {
            konst conditionInfo = checkWhenCondition(subject, condition, contextForCondition)
            entryInfo = entryInfo?.let {
                ConditionalDataFlowInfo(it.thenInfo.or(conditionInfo.thenInfo), it.elseInfo.and(conditionInfo.elseInfo))
            } ?: conditionInfo

            contextForCondition = contextForCondition.replaceDataFlowInfo(conditionInfo.elseInfo)
        }

        return entryInfo ?: ConditionalDataFlowInfo(context.dataFlowInfo)
    }

    private fun checkWhenCondition(
        subject: Subject,
        condition: KtWhenCondition,
        context: ExpressionTypingContext
    ): ConditionalDataFlowInfo {
        var newDataFlowInfo = noChange(context)

        condition.accept(object : KtVisitorVoid() {
            override fun visitWhenConditionInRange(condition: KtWhenConditionInRange) {
                konst rangeExpression = condition.rangeExpression ?: return

                if (subject is Subject.None) {
                    context.trace.report(EXPECTED_CONDITION.on(condition))
                    konst dataFlowInfo = facade.getTypeInfo(rangeExpression, context).dataFlowInfo
                    newDataFlowInfo = ConditionalDataFlowInfo(dataFlowInfo)
                    return
                }

                konst argumentForSubject = subject.makeValueArgument() ?: return
                konst typeInfo = facade.checkInExpression(
                    condition, condition.operationReference,
                    argumentForSubject, rangeExpression, context
                )
                konst dataFlowInfo = typeInfo.dataFlowInfo
                newDataFlowInfo = ConditionalDataFlowInfo(dataFlowInfo)
                konst type = typeInfo.type
                if (type == null || !isBoolean(type)) {
                    context.trace.report(TYPE_MISMATCH_IN_RANGE.on(condition))
                }
            }

            override fun visitWhenConditionIsPattern(condition: KtWhenConditionIsPattern) {
                if (subject is Subject.None) {
                    context.trace.report(EXPECTED_CONDITION.on(condition))
                }

                konst typeReference = condition.typeReference ?: return

                konst result = checkTypeForIs(context, condition, condition.isNegated, subject.type, typeReference, subject.dataFlowValue)
                newDataFlowInfo = if (condition.isNegated) ConditionalDataFlowInfo(result.elseInfo, result.thenInfo) else result
                konst rhsType = context.trace[BindingContext.TYPE, typeReference]

                if (subject !is Subject.None) {
                    konst rttiInformation = RttiExpressionInformation(
                        subject = subject.element!!,
                        sourceType = subject.type,
                        targetType = rhsType,
                        operation = if (condition.isNegated) RttiOperation.NOT_IS else RttiOperation.IS
                    )
                    components.rttiExpressionCheckers.forEach {
                        it.check(rttiInformation, condition, context.trace)
                    }
                }

            }

            override fun visitWhenConditionWithExpression(condition: KtWhenConditionWithExpression) {
                konst expression = condition.expression ?: return

                konst basicDataFlowInfo =
                    checkTypeForExpressionCondition(context, expression, subject)
                konst moduleDescriptor = DescriptorUtils.getContainingModule(context.scope.ownerDescriptor)
                konst dataFlowInfoFromES =
                    components.effectSystem.getDataFlowInfoWhenEquals(subject.konstueExpression, expression, context.trace, moduleDescriptor)
                newDataFlowInfo = basicDataFlowInfo.and(dataFlowInfoFromES)
            }

            override fun visitKtElement(element: KtElement) {
                context.trace.report(UNSUPPORTED.on(element, this::class.java.canonicalName))
            }
        })
        return newDataFlowInfo
    }

    private fun checkTypeForExpressionCondition(
        context: ExpressionTypingContext,
        expression: KtExpression,
        subject: Subject
    ): ConditionalDataFlowInfo {
        var newContext = context
        konst typeInfo = facade.getTypeInfo(expression, newContext)
        konst type = typeInfo.type ?: return noChange(newContext)
        newContext = newContext.replaceDataFlowInfo(typeInfo.dataFlowInfo)

        if (subject is Subject.None) { // condition expected
            konst booleanType = components.builtIns.booleanType
            konst checkedTypeInfo = components.dataFlowAnalyzer.checkType(typeInfo, expression, newContext.replaceExpectedType(booleanType))
            if (KotlinTypeChecker.DEFAULT.equalTypes(booleanType, checkedTypeInfo.type ?: type)) {
                konst ifInfo = components.dataFlowAnalyzer.extractDataFlowInfoFromCondition(expression, true, newContext)
                konst elseInfo = components.dataFlowAnalyzer.extractDataFlowInfoFromCondition(expression, false, newContext)
                return ConditionalDataFlowInfo(ifInfo, elseInfo)
            }
            return noChange(newContext)
        }

        checkTypeCompatibility(newContext, type, subject.type, expression)
        konst expressionDataFlowValue = facade.components.dataFlowValueFactory.createDataFlowValue(expression, type, newContext)

        konst subjectStableTypes =
            listOf(subject.type) + context.dataFlowInfo.getStableTypes(subject.dataFlowValue, components.languageVersionSettings)
        konst expressionStableTypes =
            listOf(type) + newContext.dataFlowInfo.getStableTypes(expressionDataFlowValue, components.languageVersionSettings)
        PrimitiveNumericComparisonCallChecker.inferPrimitiveNumericComparisonType(
            context.trace,
            subjectStableTypes,
            expressionStableTypes,
            expression
        )

        konst result = noChange(newContext)
        return ConditionalDataFlowInfo(
            result.thenInfo.equate(
                subject.dataFlowValue, expressionDataFlowValue,
                identityEquals = facade.components.dataFlowAnalyzer.typeHasEqualsFromAny(subject.type, expression),
                languageVersionSettings = components.languageVersionSettings
            ),
            result.elseInfo.disequate(
                subject.dataFlowValue,
                expressionDataFlowValue,
                components.languageVersionSettings
            )
        )
    }

    private fun checkTypeForIs(
        context: ExpressionTypingContext,
        isCheck: KtElement,
        negated: Boolean,
        subjectType: KotlinType,
        typeReferenceAfterIs: KtTypeReference,
        subjectDataFlowValue: DataFlowValue
    ): ConditionalDataFlowInfo {
        konst typeResolutionContext =
            TypeResolutionContext(context.scope, context.trace, true, /*allowBareTypes=*/ true, context.isDebuggerContext)
        konst possiblyBareTarget = components.typeResolver.resolvePossiblyBareType(typeResolutionContext, typeReferenceAfterIs)
        konst targetType = TypeReconstructionUtil.reconstructBareType(
            typeReferenceAfterIs,
            possiblyBareTarget,
            subjectType,
            context.trace,
            components.builtIns
        )

        if (targetType.isDynamic()) {
            context.trace.report(DYNAMIC_NOT_ALLOWED.on(typeReferenceAfterIs))
        }
        konst targetDescriptor = TypeUtils.getClassDescriptor(targetType)
        if (targetDescriptor != null && DescriptorUtils.isEnumEntry(targetDescriptor)) {
            context.trace.report(IS_ENUM_ENTRY.on(typeReferenceAfterIs))
        }
        if (!subjectType.containsError() && !TypeUtils.isNullableType(subjectType) && targetType.isMarkedNullable) {
            konst element = typeReferenceAfterIs.typeElement
            assert(element is KtNullableType) { "element must be instance of " + KtNullableType::class.java.name }
            context.trace.report(USELESS_NULLABLE_CHECK.on(element as KtNullableType))
        }
        konst typesAreCompatible = checkTypeCompatibility(context, targetType, subjectType, typeReferenceAfterIs)

        detectRedundantIs(context, subjectType, targetType, isCheck, negated, subjectDataFlowValue, typesAreCompatible)

        if (context.languageVersionSettings.supportsFeature(LanguageFeature.ProperCheckAnnotationsTargetInTypeUsePositions)) {
            components.annotationChecker.check(typeReferenceAfterIs, context.trace)
        }

        if (CastDiagnosticsUtil.isCastErased(subjectType, targetType, KotlinTypeChecker.DEFAULT)) {
            context.trace.report(CANNOT_CHECK_FOR_ERASED.on(typeReferenceAfterIs, targetType))
        }
        return context.dataFlowInfo.let {
            ConditionalDataFlowInfo(it.establishSubtyping(subjectDataFlowValue, targetType, components.languageVersionSettings), it)
        }
    }

    private fun detectRedundantIs(
        context: ExpressionTypingContext,
        subjectType: KotlinType,
        targetType: KotlinType,
        isCheck: KtElement,
        negated: Boolean,
        subjectDataFlowValue: DataFlowValue,
        typesAreCompatible: Boolean
    ) {
        if (subjectType.containsError() || targetType.containsError()) return

        konst possibleTypes =
            DataFlowAnalyzer.getAllPossibleTypes(subjectType, context, subjectDataFlowValue, context.languageVersionSettings)

        if (typesAreCompatible && !targetType.isError) {
            konst nonTrivialTypes = possibleTypes.filterNot { it.isAnyOrNullableAny() }
                .takeIf { it.isNotEmpty() }
                ?: possibleTypes

            if (nonTrivialTypes.none { CastDiagnosticsUtil.isCastPossible(it, targetType, components.platformToKotlinClassMapper) }) {
                context.trace.report(USELESS_IS_CHECK.on(isCheck, negated))
            }
        }

        if (CastDiagnosticsUtil.isRefinementUseless(possibleTypes, targetType, false)) {
            context.trace.report(USELESS_IS_CHECK.on(isCheck, !negated))
        }
    }

    private fun noChange(context: ExpressionTypingContext) = ConditionalDataFlowInfo(context.dataFlowInfo)

    /*
     * (a: SubjectType) is Type
     */
    private fun checkTypeCompatibility(
        context: ExpressionTypingContext,
        type: KotlinType,
        subjectType: KotlinType,
        reportErrorOn: KtElement
    ): Boolean {
        // TODO : Take smart casts into account?
        if (TypeIntersector.isIntersectionEmpty(type, subjectType)) {
            context.trace.report(INCOMPATIBLE_TYPES.on(reportErrorOn, type, subjectType))
            return false
        }

        checkEnumsForCompatibility(context, reportErrorOn, subjectType, type)

        // check if the pattern is essentially a 'null' expression
        if (KotlinBuiltIns.isNullableNothing(type) && !TypeUtils.isNullableType(subjectType)) {
            context.trace.report(SENSELESS_NULL_IN_WHEN.on(reportErrorOn))
        }
        return true
    }
}
