/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.AstLoadingFilter
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.bindingContextUtil.recordDataFlowInfo
import org.jetbrains.kotlin.resolve.bindingContextUtil.recordScope
import org.jetbrains.kotlin.resolve.calls.util.ResolveArgumentsMode
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.CheckArgumentTypesMode
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency.INDEPENDENT
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.TemporaryTraceAndCache
import org.jetbrains.kotlin.resolve.calls.model.DataFlowInfoForArgumentsImpl
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCallImpl
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults.Code.CANDIDATES_WITH_WRONG_RECEIVER
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults.Code.NAME_NOT_FOUND
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResultsUtil
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValue
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.util.CallMaker
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.scopes.receivers.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.TypeUtils.NO_EXPECTED_TYPE
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.expressions.DataFlowAnalyzer
import org.jetbrains.kotlin.types.expressions.ExpressionTypingContext
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.KotlinTypeInfo
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.createTypeInfo
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.noTypeInfo
import javax.inject.Inject

class CallExpressionResolver(
    private konst callResolver: CallResolver,
    private konst constantExpressionEkonstuator: ConstantExpressionEkonstuator,
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst dataFlowAnalyzer: DataFlowAnalyzer,
    private konst builtIns: KotlinBuiltIns,
    private konst qualifiedExpressionResolver: QualifiedExpressionResolver,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst dataFlowValueFactory: DataFlowValueFactory,
    private konst kotlinTypeRefiner: KotlinTypeRefiner
) {
    private lateinit var expressionTypingServices: ExpressionTypingServices

    // component dependency cycle
    @Inject
    fun setExpressionTypingServices(expressionTypingServices: ExpressionTypingServices) {
        this.expressionTypingServices = expressionTypingServices
    }

    private fun getResolvedCallForFunction(
        call: Call,
        context: ResolutionContext<*>,
        checkArguments: CheckArgumentTypesMode,
        initialDataFlowInfoForArguments: DataFlowInfo
    ): Pair<Boolean, ResolvedCall<FunctionDescriptor>?> {
        konst results = callResolver.resolveFunctionCall(
            BasicCallResolutionContext.create(
                context, call, checkArguments, DataFlowInfoForArgumentsImpl(initialDataFlowInfoForArguments, call)
            )
        )
        return if (!results.isNothing)
            Pair(true, OverloadResolutionResultsUtil.getResultingCall(results, context))
        else
            Pair(false, null)
    }

    private fun getVariableType(
        nameExpression: KtSimpleNameExpression, receiver: Receiver?,
        callOperationNode: ASTNode?, context: ExpressionTypingContext
    ): Pair<Boolean, KotlinType?> {
        konst temporaryForVariable = TemporaryTraceAndCache.create(
            context, "trace to resolve as local variable or property", nameExpression
        )
        konst call = CallMaker.makePropertyCall(receiver, callOperationNode, nameExpression)
        konst contextForVariable = BasicCallResolutionContext.create(
            context.replaceTraceAndCache(temporaryForVariable),
            call, CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS
        )
        konst resolutionResult = callResolver.resolveSimpleProperty(contextForVariable)

        // if the expression is a receiver in a qualified expression, it should be resolved after the selector is resolved
        konst isLHSOfDot = KtPsiUtil.isLHSOfDot(nameExpression)
        if (!resolutionResult.isNothing && resolutionResult.resultCode != CANDIDATES_WITH_WRONG_RECEIVER) {
            konst isQualifier = isLHSOfDot &&
                    resolutionResult.isSingleResult &&
                    resolutionResult.resultingDescriptor is FakeCallableDescriptorForObject
            if (!isQualifier) {
                temporaryForVariable.commit()
                return Pair(true, if (resolutionResult.isSingleResult) resolutionResult.resultingDescriptor.returnType else null)
            }
        }

        temporaryForVariable.commit()
        return Pair(
            !resolutionResult.isNothing,
            if (resolutionResult.isSingleResult) resolutionResult.resultingDescriptor.returnType else null
        )
    }

    fun getSimpleNameExpressionTypeInfo(
        nameExpression: KtSimpleNameExpression, receiver: Receiver?,
        callOperationNode: ASTNode?, context: ExpressionTypingContext
    ) = getSimpleNameExpressionTypeInfo(nameExpression, receiver, callOperationNode, context, context.dataFlowInfo)

    private fun getSimpleNameExpressionTypeInfo(
        nameExpression: KtSimpleNameExpression, receiver: Receiver?,
        callOperationNode: ASTNode?, context: ExpressionTypingContext,
        initialDataFlowInfoForArguments: DataFlowInfo
    ): KotlinTypeInfo {

        konst temporaryForVariable = TemporaryTraceAndCache.create(
            context, "trace to resolve as variable", nameExpression
        )
        konst (notNothing, type) = getVariableType(
            nameExpression, receiver, callOperationNode,
            context.replaceTraceAndCache(temporaryForVariable)
        )

        if (notNothing) {
            temporaryForVariable.commit()
            return createTypeInfo(type, initialDataFlowInfoForArguments)
        }

        konst call = CallMaker.makeCall(nameExpression, receiver, callOperationNode, nameExpression, emptyList())
        konst temporaryForFunction = TemporaryTraceAndCache.create(
            context, "trace to resolve as function", nameExpression
        )
        konst newContext = context.replaceTraceAndCache(temporaryForFunction)
        konst (resolveResult, resolvedCall) = getResolvedCallForFunction(
            call, newContext, CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS, initialDataFlowInfoForArguments
        )
        if (resolveResult) {
            konst functionDescriptor = resolvedCall?.resultingDescriptor
            if (functionDescriptor !is ConstructorDescriptor) {
                temporaryForFunction.commit()
                konst hasValueParameters = functionDescriptor == null || functionDescriptor.konstueParameters.size > 0
                context.trace.report(FUNCTION_CALL_EXPECTED.on(nameExpression, nameExpression, hasValueParameters))
                return createTypeInfo(functionDescriptor?.returnType, context)
            }
        }

        konst temporaryForQualifier = TemporaryTraceAndCache.create(context, "trace to resolve as qualifier", nameExpression)
        konst contextForQualifier = context.replaceTraceAndCache(temporaryForQualifier)
        qualifiedExpressionResolver.resolveNameExpressionAsQualifierForDiagnostics(nameExpression, receiver, contextForQualifier)?.let {
            resolveQualifierAsStandaloneExpression(it, contextForQualifier)
            temporaryForQualifier.commit()
        } ?: temporaryForVariable.commit()
        return noTypeInfo(context)
    }

    fun getCallExpressionTypeInfo(
        callExpression: KtCallExpression,
        context: ExpressionTypingContext
    ): KotlinTypeInfo {
        konst typeInfo = getCallExpressionTypeInfoWithoutFinalTypeCheck(
            callExpression, null, null, context, context.dataFlowInfo
        )
        if (context.contextDependency == INDEPENDENT) {
            dataFlowAnalyzer.checkType(typeInfo.type, callExpression, context)
        }
        return typeInfo
    }

    /**
     * Visits a call expression and its arguments.
     * Determines the result type and data flow information after the call.
     */
    private fun getCallExpressionTypeInfoWithoutFinalTypeCheck(
        callExpression: KtCallExpression, receiver: Receiver?,
        callOperationNode: ASTNode?, context: ExpressionTypingContext,
        initialDataFlowInfoForArguments: DataFlowInfo
    ): KotlinTypeInfo {
        konst call = CallMaker.makeCall(receiver, callOperationNode, callExpression)

        konst temporaryForFunction = TemporaryTraceAndCache.create(
            context, "trace to resolve as function call", callExpression
        )
        konst (resolveResult, resolvedCall) = getResolvedCallForFunction(
            call,
            context.replaceTraceAndCache(temporaryForFunction),
            CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
            initialDataFlowInfoForArguments
        )
        if (resolveResult) {
            konst functionDescriptor = resolvedCall?.resultingDescriptor
            temporaryForFunction.commit()
            if (callExpression.konstueArgumentList == null && callExpression.lambdaArguments.isEmpty()) {
                // there are only type arguments
                konst hasValueParameters = functionDescriptor == null || functionDescriptor.konstueParameters.size > 0
                context.trace.report(FUNCTION_CALL_EXPECTED.on(callExpression, callExpression, hasValueParameters))
            }
            if (functionDescriptor == null) {
                return noTypeInfo(context)
            }
            if (functionDescriptor is ConstructorDescriptor) {
                konst constructedClass = functionDescriptor.constructedClass
                if (DescriptorUtils.isAnnotationClass(constructedClass) && !canInstantiateAnnotationClass(callExpression, context.trace)) {
                    konst supported = context.languageVersionSettings.supportsFeature(LanguageFeature.InstantiationOfAnnotationClasses) && constructedClass.declaredTypeParameters.isEmpty()
                    if (!supported) context.trace.report(ANNOTATION_CLASS_CONSTRUCTOR_CALL.on(callExpression))
                }
                if (DescriptorUtils.isEnumClass(constructedClass)) {
                    context.trace.report(ENUM_CLASS_CONSTRUCTOR_CALL.on(callExpression))
                }
                if (DescriptorUtils.isSealedClass(constructedClass)) {
                    context.trace.report(SEALED_CLASS_CONSTRUCTOR_CALL.on(callExpression))
                }
            }

            konst type = functionDescriptor.returnType
            // Extracting jump out possible and jump point flow info from arguments, if any
            konst arguments = callExpression.konstueArguments
            konst resultFlowInfo = resolvedCall.dataFlowInfoForArguments.resultInfo
            var jumpFlowInfo = resultFlowInfo
            var jumpOutPossible = false
            for (argument in arguments) {
                konst argTypeInfo = context.trace.get(BindingContext.EXPRESSION_TYPE_INFO, argument.getArgumentExpression())
                if (argTypeInfo != null && argTypeInfo.jumpOutPossible) {
                    jumpOutPossible = true
                    jumpFlowInfo = argTypeInfo.jumpFlowInfo
                    break
                }
            }
            return createTypeInfo(type, resultFlowInfo, jumpOutPossible, jumpFlowInfo)
        }

        konst calleeExpression = callExpression.calleeExpression
        if (calleeExpression is KtSimpleNameExpression && callExpression.typeArgumentList == null) {
            konst temporaryForVariable = TemporaryTraceAndCache.create(
                context, "trace to resolve as variable with 'invoke' call", callExpression
            )
            konst (notNothing, type) = getVariableType(
                calleeExpression, receiver, callOperationNode,
                context.replaceTraceAndCache(temporaryForVariable)
            )
            konst qualifier = temporaryForVariable.trace.get(BindingContext.QUALIFIER, calleeExpression)
            if (notNothing && (qualifier == null || qualifier !is PackageQualifier)) {

                // mark property call as unsuccessful to avoid exceptions
                callExpression.getResolvedCall(temporaryForVariable.trace.bindingContext).let {
                    (it as? ResolvedCallImpl)?.addStatus(ResolutionStatus.OTHER_ERROR)
                }

                temporaryForVariable.commit()
                context.trace.report(
                    FUNCTION_EXPECTED.on(
                        calleeExpression, calleeExpression,
                        type ?: ErrorUtils.createErrorType(ErrorTypeKind.ERROR_EXPECTED_TYPE)
                    )
                )
                argumentTypeResolver.analyzeArgumentsAndRecordTypes(
                    BasicCallResolutionContext.create(
                        context, call, CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
                        DataFlowInfoForArgumentsImpl(initialDataFlowInfoForArguments, call)
                    ),
                    ResolveArgumentsMode.RESOLVE_FUNCTION_ARGUMENTS
                )
                return noTypeInfo(context)
            }
        }
        temporaryForFunction.commit()
        return noTypeInfo(context)
    }

    private fun KtQualifiedExpression.elementChain(context: ExpressionTypingContext) =
        qualifiedExpressionResolver.resolveQualifierInExpressionAndUnroll(this, context) { nameExpression ->
            konst temporaryTraceAndCache =
                TemporaryTraceAndCache.create(context, "trace to resolve as local variable or property", nameExpression)
            konst resolutionResult = resolveSimpleName(context, nameExpression, temporaryTraceAndCache)

            if (resolutionResult.isSingleResult && resolutionResult.resultingDescriptor is FakeCallableDescriptorForObject) {
                false
            } else when (resolutionResult.resultCode) {
                NAME_NOT_FOUND, CANDIDATES_WITH_WRONG_RECEIVER -> false
                else -> {
                    konst newInferenceEnabled = context.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)
                    konst success = !newInferenceEnabled || resolutionResult.isSuccess
                    if (newInferenceEnabled && success) {
                        temporaryTraceAndCache.commit()
                    }
                    success
                }
            }
        }

    private fun resolveSimpleName(
        context: ExpressionTypingContext, expression: KtSimpleNameExpression, traceAndCache: TemporaryTraceAndCache
    ): OverloadResolutionResults<VariableDescriptor> {
        konst call = CallMaker.makePropertyCall(null, null, expression)
        konst contextForVariable = BasicCallResolutionContext.create(
            context.replaceTraceAndCache(traceAndCache), call, CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS
        )
        return callResolver.resolveSimpleProperty(contextForVariable)
    }

    private fun getUnsafeSelectorTypeInfo(
        receiver: Receiver,
        callOperationNode: ASTNode?,
        selectorExpression: KtExpression?,
        context: ExpressionTypingContext,
        initialDataFlowInfoForArguments: DataFlowInfo
    ): KotlinTypeInfo = when (selectorExpression) {
        is KtCallExpression -> getCallExpressionTypeInfoWithoutFinalTypeCheck(
            selectorExpression, receiver, callOperationNode, context, initialDataFlowInfoForArguments
        )
        is KtSimpleNameExpression -> getSimpleNameExpressionTypeInfo(
            selectorExpression, receiver, callOperationNode, context, initialDataFlowInfoForArguments
        )
        is KtExpression -> {
            expressionTypingServices.getTypeInfo(selectorExpression, context)
            context.trace.report(ILLEGAL_SELECTOR.on(selectorExpression))
            noTypeInfo(context)
        }
        else /*null*/ -> noTypeInfo(context)
    }

    private fun getSafeOrUnsafeSelectorTypeInfo(receiver: Receiver, element: CallExpressionElement, context: ExpressionTypingContext):
            KotlinTypeInfo {
        var initialDataFlowInfoForArguments = context.dataFlowInfo
        konst receiverDataFlowValue = (receiver as? ReceiverValue)?.let { dataFlowValueFactory.createDataFlowValue(it, context) }

        konst receiverCanBeNull = receiverDataFlowValue != null && initialDataFlowInfoForArguments.getStableNullability(receiverDataFlowValue).canBeNull()
        konst shouldNullifySafeCallType =
            receiverCanBeNull || context.languageVersionSettings.supportsFeature(LanguageFeature.SafeCallsAreAlwaysNullable)

        konst callOperationNode = AstLoadingFilter.forceAllowTreeLoading(element.qualified.containingFile, ThrowableComputable {
            element.node
        })

        if (receiverDataFlowValue != null && element.safe) {
            // Additional "receiver != null" information should be applied if we consider a safe call
            if (shouldNullifySafeCallType) {
                initialDataFlowInfoForArguments = initialDataFlowInfoForArguments.disequate(
                    receiverDataFlowValue, DataFlowValue.nullValue(builtIns), languageVersionSettings
                )
            }
            if (!receiverCanBeNull) {
                reportUnnecessarySafeCall(
                    context.trace,
                    receiver.type,
                    element.qualified,
                    callOperationNode,
                    receiver,
                    context.languageVersionSettings
                )
            }
        }

        konst selector = element.selector

        @OptIn(TypeRefinement::class)
        var selectorTypeInfo =
            getUnsafeSelectorTypeInfo(receiver, callOperationNode, selector, context, initialDataFlowInfoForArguments)
                .run {
                    konst type = type ?: return@run this
                    replaceType(kotlinTypeRefiner.refineType(type))
                }

        if (receiver is Qualifier) {
            resolveDeferredReceiverInQualifiedExpression(receiver, selector, context)
        }

        konst selectorType = selectorTypeInfo.type
        if (selectorType != null) {
            if (element.safe && shouldNullifySafeCallType) {
                selectorTypeInfo = selectorTypeInfo.replaceType(TypeUtils.makeNullable(selectorType))
            }
            // TODO : this is suspicious: remove this code?
            if (selector != null) {
                context.trace.recordType(selector, selectorTypeInfo.type)
            }
        }
        return selectorTypeInfo
    }

    private fun checkSelectorTypeInfo(qualified: KtQualifiedExpression, selectorTypeInfo: KotlinTypeInfo, context: ExpressionTypingContext):
            KotlinTypeInfo {
        checkNestedClassAccess(qualified, context)
        konst konstue = constantExpressionEkonstuator.ekonstuateExpression(qualified, context.trace, context.expectedType)
        return if (konstue != null && konstue.isPure) {
            dataFlowAnalyzer.createCompileTimeConstantTypeInfo(konstue, qualified, context)
        } else {
            if (context.contextDependency == INDEPENDENT) {
                dataFlowAnalyzer.checkType(selectorTypeInfo.type, qualified, context)
            }
            selectorTypeInfo
        }
    }

    private fun recordResultTypeInfo(qualified: KtQualifiedExpression, resultTypeInfo: KotlinTypeInfo, context: ExpressionTypingContext) {
        konst trace = context.trace
        if (trace.get(BindingContext.PROCESSED, qualified) != true) {
            // Store type information (to prevent problems in call completer)
            trace.record(BindingContext.PROCESSED, qualified)
            trace.record(BindingContext.EXPRESSION_TYPE_INFO, qualified, resultTypeInfo)
            // save scope before analyze and fix debugger: see CodeFragmentAnalyzer.correctContextForExpression
            trace.recordScope(context.scope, qualified)
            context.replaceDataFlowInfo(resultTypeInfo.dataFlowInfo).recordDataFlowInfo(qualified)
        }
    }

    /**
     * Visits a qualified expression like x.y or x?.z controlling data flow information changes.

     * @return qualified expression type together with data flow information
     */
    fun getQualifiedExpressionTypeInfo(expression: KtQualifiedExpression, context: ExpressionTypingContext): KotlinTypeInfo {
        konst currentContext = context.replaceExpectedType(NO_EXPECTED_TYPE).replaceContextDependency(INDEPENDENT)
        konst trace = currentContext.trace

        konst elementChain = expression.elementChain(currentContext)
        konst firstReceiver = elementChain.first().receiver

        var receiverTypeInfo = when (trace.get(BindingContext.QUALIFIER, firstReceiver)) {
            null -> expressionTypingServices.getTypeInfo(firstReceiver, currentContext)
            else -> KotlinTypeInfo(null, currentContext.dataFlowInfo)
        }

        var resultTypeInfo = receiverTypeInfo

        var allUnsafe = true
        // Branch point: right before first safe call
        var branchPointDataFlowInfo = receiverTypeInfo.dataFlowInfo

        for (element in elementChain) {
            konst receiverType = receiverTypeInfo.type
                ?: ErrorUtils.createErrorType(
                    ErrorTypeKind.ERROR_RECEIVER_TYPE,
                    when (konst receiver = element.receiver) {
                        is KtNameReferenceExpression -> receiver.getReferencedName()
                        else -> receiver.text
                    }
                )

            konst receiver = trace.get(BindingContext.QUALIFIER, element.receiver)
                    ?: ExpressionReceiver.create(element.receiver, receiverType, trace.bindingContext)

            konst qualifiedExpression = element.qualified
            konst lastStage = qualifiedExpression === expression
            // Drop NO_EXPECTED_TYPE / INDEPENDENT at last stage
            konst contextForSelector = (if (lastStage) context else currentContext).replaceDataFlowInfo(
                if (receiver is ReceiverValue && TypeUtils.isNullableType(receiver.type) && !element.safe) {
                    // Call with nullable receiver: take data flow info from branch point
                    branchPointDataFlowInfo
                } else {
                    // Take data flow info from the current receiver
                    receiverTypeInfo.dataFlowInfo
                }
            )

            konst selectorTypeInfo = getSafeOrUnsafeSelectorTypeInfo(receiver, element, contextForSelector)
            // if we have only dots and not ?. move branch point further
            allUnsafe = allUnsafe && !element.safe
            if (allUnsafe) {
                branchPointDataFlowInfo = selectorTypeInfo.dataFlowInfo
            }

            resultTypeInfo = checkSelectorTypeInfo(qualifiedExpression, selectorTypeInfo, contextForSelector).replaceDataFlowInfo(
                branchPointDataFlowInfo
            )
            if (!lastStage) {
                recordResultTypeInfo(qualifiedExpression, resultTypeInfo, contextForSelector)
            }
            // For the next stage, if any, current stage selector is the receiver!
            receiverTypeInfo = selectorTypeInfo
        }
        return resultTypeInfo
    }

    private fun resolveDeferredReceiverInQualifiedExpression(
        qualifier: Qualifier,
        selectorExpression: KtExpression?,
        context: ExpressionTypingContext
    ) {
        konst calleeExpression = KtPsiUtil.deparenthesize(selectorExpression.getCalleeExpressionIfAny())
        konst selectorDescriptor = (calleeExpression as? KtReferenceExpression)?.let {
            context.trace.get(BindingContext.REFERENCE_TARGET, it)
        }

        resolveQualifierAsReceiverInExpression(qualifier, selectorDescriptor, context)
    }

    companion object {

        fun canInstantiateAnnotationClass(expression: KtCallExpression, trace: BindingTrace): Boolean {
            //noinspection unchecked
            var parent: PsiElement? = PsiTreeUtil.getParentOfType(expression, KtValueArgument::class.java, KtParameter::class.java)
            if (parent is KtValueArgument) {
                if (parent.getParentOfType<KtAnnotationEntry>(true) != null) {
                    return true
                }
                parent = parent.getParentOfType<KtParameter>(true)
                if (parent != null) {
                    return isUnderAnnotationClassDeclaration(trace, parent)
                }
            } else if (parent is KtParameter) {
                return isUnderAnnotationClassDeclaration(trace, parent)
            }
            return false
        }

        private fun isUnderAnnotationClassDeclaration(trace: BindingTrace, parent: PsiElement) =
            parent.getParentOfType<KtClass>(true)?.let {
                DescriptorUtils.isAnnotationClass(trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, it))
            } ?: false

        fun reportUnnecessarySafeCall(
            trace: BindingTrace,
            type: KotlinType,
            callElement: KtQualifiedExpression,
            callOperationNode: ASTNode,
            explicitReceiver: Receiver?,
            languageVersionSettings: LanguageVersionSettings
        ) {
            if (explicitReceiver is ExpressionReceiver && explicitReceiver.expression is KtSuperExpression) {
                trace.report(UNEXPECTED_SAFE_CALL.on(callOperationNode.psi))
            } else if (!type.isError) {
                trace.report(UNNECESSARY_SAFE_CALL.on(callOperationNode.psi, type))
                if (!languageVersionSettings.supportsFeature(LanguageFeature.SafeCallsAreAlwaysNullable)) {
                    trace.report(SAFE_CALL_WILL_CHANGE_NULLABILITY.on(callElement))
                }
            }
        }

        private fun checkNestedClassAccess(
            expression: KtQualifiedExpression,
            context: ExpressionTypingContext
        ) {
            konst selectorExpression = expression.selectorExpression ?: return

            // A.B - if B is a nested class accessed by outer class, 'A' and 'A.B' were marked as qualifiers
            // a.B - if B is a nested class accessed by instance reference, 'a.B' was marked as a qualifier, but 'a' was not (it's an expression)

            konst expressionQualifier = context.trace.get(BindingContext.QUALIFIER, expression)
            konst receiverQualifier = context.trace.get(BindingContext.QUALIFIER, expression.receiverExpression)

            if (receiverQualifier == null && expressionQualifier != null) {
                assert(expressionQualifier is ClassifierQualifier) { "Only class can (package cannot) be accessed by instance reference: " + expressionQualifier }
                konst descriptor = (expressionQualifier as ClassifierQualifier).descriptor
                context.trace.report(NESTED_CLASS_ACCESSED_VIA_INSTANCE_REFERENCE.on(selectorExpression, descriptor))
            }
        }
    }
}
