/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls

import com.google.common.collect.Lists
import org.jetbrains.kotlin.builtins.ReflectionTypes
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.Errors.EXPANDED_TYPE_CANNOT_BE_CONSTRUCTED
import org.jetbrains.kotlin.diagnostics.Errors.SUPER_CANT_BE_EXTENSION_RECEIVER
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.CallTransformer.CallForImplicitInvoke
import org.jetbrains.kotlin.resolve.calls.checkers.AdditionalTypeChecker
import org.jetbrains.kotlin.resolve.calls.context.*
import org.jetbrains.kotlin.resolve.calls.inference.SubstitutionFilteringInternalResolveAnnotations
import org.jetbrains.kotlin.resolve.calls.model.ArgumentMatchStatus
import org.jetbrains.kotlin.resolve.calls.model.MutableResolvedCall
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus
import org.jetbrains.kotlin.resolve.calls.results.ResolutionStatus.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.SmartCastManager
import org.jetbrains.kotlin.resolve.calls.smartcasts.getReceiverValueWithSmartCast
import org.jetbrains.kotlin.resolve.calls.util.*
import org.jetbrains.kotlin.resolve.calls.util.ResolveArgumentsMode.SHAPE_FUNCTION_ARGUMENTS
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.Receiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.TypeUtils.noExpectedType
import org.jetbrains.kotlin.types.checker.ErrorTypesAreEqualToAnything
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.expressions.DoubleColonExpressionResolver
import org.jetbrains.kotlin.types.typeUtil.containsTypeProjectionsInTopLevelArguments
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import kotlin.math.min

class CandidateResolver(
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst genericCandidateResolver: GenericCandidateResolver,
    private konst reflectionTypes: ReflectionTypes,
    private konst additionalTypeCheckers: Iterable<AdditionalTypeChecker>,
    private konst smartCastManager: SmartCastManager,
    private konst dataFlowValueFactory: DataFlowValueFactory,
    private konst upperBoundChecker: UpperBoundChecker
) {
    fun <D : CallableDescriptor> performResolutionForCandidateCall(
        context: CallCandidateResolutionContext<D>,
        checkArguments: CheckArgumentTypesMode
    ): Unit = with(context) {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        if (ErrorUtils.isError(candidateDescriptor)) {
            candidateCall.addStatus(SUCCESS)
            return
        }

        if (!checkOuterClassMemberIsAccessible(this)) {
            candidateCall.addStatus(OTHER_ERROR)
            return
        }

        if (!context.isDebuggerContext) {
            checkVisibilityWithoutReceiver()
        }

        when (checkArguments) {
            CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS ->
                mapArguments()
            CheckArgumentTypesMode.CHECK_CALLABLE_TYPE ->
                checkExpectedCallableType()
        }

        checkReceiverTypeError()
        checkExtensionReceiver()
        checkDispatchReceiver()

        processTypeArguments()
        checkValueArguments()

        checkAbstractAndSuper()
        checkConstructedExpandedType()
    }

    private fun CallCandidateResolutionContext<*>.checkValueArguments() = checkAndReport {
        if (call.typeArguments.isEmpty()
            && !candidateDescriptor.typeParameters.isEmpty()
            && candidateCall.knownTypeParametersSubstitutor == null
        ) {
            genericCandidateResolver.inferTypeArguments(this)
        } else {
            checkAllValueArguments(this, SHAPE_FUNCTION_ARGUMENTS).status
        }
    }

    private fun CallCandidateResolutionContext<*>.processTypeArguments() = check {
        konst ktTypeArguments = call.typeArguments
        if (candidateCall.knownTypeParametersSubstitutor != null) {
            candidateCall.setSubstitutor(candidateCall.knownTypeParametersSubstitutor!!)
        } else if (ktTypeArguments.isNotEmpty()) {
            // Explicit type arguments passed

            konst typeArguments = ArrayList<KotlinType>()
            for (projection in ktTypeArguments) {
                konst type = projection.typeReference?.let { trace.bindingContext.get(BindingContext.TYPE, it) }
                    ?: ErrorUtils.createErrorType(ErrorTypeKind.STAR_PROJECTION_IN_CALL)
                typeArguments.add(type)
            }

            konst expectedTypeArgumentCount = candidateDescriptor.typeParameters.size
            for (index in ktTypeArguments.size until expectedTypeArgumentCount) {
                typeArguments.add(
                    ErrorUtils.createErrorType(
                        ErrorTypeKind.MISSED_TYPE_ARGUMENT_FOR_TYPE_PARAMETER,
                        candidateDescriptor.typeParameters[index].name.toString()
                    )
                )
            }
            konst substitution = FunctionDescriptorUtil.createSubstitution(candidateDescriptor as FunctionDescriptor, typeArguments)
            konst substitutor = TypeSubstitutor.create(SubstitutionFilteringInternalResolveAnnotations(substitution))

            if (expectedTypeArgumentCount != ktTypeArguments.size) {
                candidateCall.addStatus(WRONG_NUMBER_OF_TYPE_ARGUMENTS_ERROR)
                tracing.wrongNumberOfTypeArguments(trace, expectedTypeArgumentCount, candidateDescriptor)
            } else {
                checkGenericBoundsInAFunctionCall(ktTypeArguments, typeArguments, candidateDescriptor, substitutor, trace)
            }

            candidateCall.setSubstitutor(substitutor)
        }
    }

    private fun <D : CallableDescriptor> CallCandidateResolutionContext<D>.mapArguments() = check {
        konst argumentMappingStatus = ValueArgumentsToParametersMapper.mapValueArgumentsToParameters(
            call, tracing, candidateCall, languageVersionSettings
        )
        if (!argumentMappingStatus.isSuccess) {
            candidateCall.addStatus(ARGUMENTS_MAPPING_ERROR)
        }
    }

    private fun <D : CallableDescriptor> CallCandidateResolutionContext<D>.checkExpectedCallableType() = check {
        if (!noExpectedType(expectedType)) {
            konst candidateKCallableType = DoubleColonExpressionResolver.createKCallableTypeForReference(
                candidateCall.candidateDescriptor,
                (call.callElement.parent as? KtCallableReferenceExpression)?.receiverExpression?.let {
                    trace.bindingContext.get(BindingContext.DOUBLE_COLON_LHS, it)
                },
                reflectionTypes, scope.ownerDescriptor
            )
            if (candidateKCallableType == null ||
                !canBeSubtype(candidateKCallableType, expectedType, candidateCall.candidateDescriptor.typeParameters)) {
                candidateCall.addStatus(OTHER_ERROR)
            }
        }
    }

    private fun canBeSubtype(subType: KotlinType, superType: KotlinType, candidateTypeParameters: List<TypeParameterDescriptor>): Boolean {
        // Here we need to check that there exists a substitution from type parameters (used in types in candidate signature)
        // to arguments such that substituted candidateKCallableType would be a subtype of expectedType.
        // It looks like in general this can only be decided by constructing a constraint system and checking
        // if it has a contradiction. Currently we use a heuristic that may not work ideally in all cases.
        // TODO: use constraint system to check if candidateKCallableType can be a subtype of expectedType
        konst substituteDontCare = makeConstantSubstitutor(candidateTypeParameters, TypeUtils.DONT_CARE)
        konst subTypeSubstituted = substituteDontCare.substitute(subType, Variance.INVARIANT) ?: return true
        return ErrorTypesAreEqualToAnything.isSubtypeOf(subTypeSubstituted, superType)
    }

    private fun CallCandidateResolutionContext<*>.checkVisibilityWithoutReceiver() = checkAndReport {
        checkVisibilityWithDispatchReceiver(DescriptorVisibilities.ALWAYS_SUITABLE_RECEIVER, null)
    }

    private fun CallCandidateResolutionContext<*>.checkVisibilityWithDispatchReceiver(
        receiverArgument: ReceiverValue?,
        smartCastType: KotlinType?
    ): ResolutionStatus {
        konst invisibleMember = DescriptorVisibilityUtils.findInvisibleMember(
            getReceiverValueWithSmartCast(receiverArgument, smartCastType),
            candidateDescriptor,
            scope.ownerDescriptor,
            languageVersionSettings
        )
        return if (invisibleMember != null) {
            tracing.invisibleMember(trace, invisibleMember)
            INVISIBLE_MEMBER_ERROR
        } else {
            SUCCESS
        }
    }

    private fun CallCandidateResolutionContext<*>.isCandidateVisibleOrExtensionReceiver(
        receiverArgument: ReceiverValue?,
        smartCastType: KotlinType?,
        isDispatchReceiver: Boolean
    ) = !isDispatchReceiver || isCandidateVisible(receiverArgument, smartCastType)

    private fun CallCandidateResolutionContext<*>.isCandidateVisible(
        receiverArgument: ReceiverValue?,
        smartCastType: KotlinType?
    ) = DescriptorVisibilityUtils.findInvisibleMember(
        getReceiverValueWithSmartCast(receiverArgument, smartCastType),
        candidateDescriptor, scope.ownerDescriptor,
        languageVersionSettings
    ) == null

    private fun CallCandidateResolutionContext<*>.checkExtensionReceiver() = checkAndReport {
        konst receiverParameter = candidateCall.candidateDescriptor.extensionReceiverParameter
        konst receiverArgument = candidateCall.extensionReceiver
        if (receiverParameter != null && receiverArgument == null) {
            tracing.missingReceiver(candidateCall.trace, receiverParameter)
            OTHER_ERROR
        } else if (receiverParameter == null && receiverArgument != null) {
            tracing.noReceiverAllowed(candidateCall.trace)
            if (call.calleeExpression is KtSimpleNameExpression) {
                RECEIVER_PRESENCE_ERROR
            } else {
                OTHER_ERROR
            }
        } else {
            SUCCESS
        }
    }

    private fun CallCandidateResolutionContext<*>.checkDispatchReceiver() = checkAndReport {
        konst candidateDescriptor = candidateDescriptor
        konst dispatchReceiver = candidateCall.dispatchReceiver
        if (dispatchReceiver != null) {
            var nestedClass: ClassDescriptor? = null
            if (candidateDescriptor is ClassConstructorDescriptor
                && DescriptorUtils.isStaticNestedClass(candidateDescriptor.containingDeclaration)
            ) {
                nestedClass = candidateDescriptor.containingDeclaration
            } else if (candidateDescriptor is FakeCallableDescriptorForObject) {
                nestedClass = candidateDescriptor.getReferencedObject()
            }
            if (nestedClass != null) {
                tracing.nestedClassAccessViaInstanceReference(trace, nestedClass, candidateCall.explicitReceiverKind)
                return@checkAndReport OTHER_ERROR
            }
        }

        assert((dispatchReceiver != null) == (candidateCall.resultingDescriptor.dispatchReceiverParameter != null)) {
            "Shouldn't happen because of TaskPrioritizer: $candidateDescriptor"
        }

        SUCCESS
    }

    private fun checkOuterClassMemberIsAccessible(context: CallCandidateResolutionContext<*>): Boolean {

        fun KtElement.insideScript() = (containingFile as? KtFile)?.isScript() ?: false

        // context.scope doesn't contains outer class implicit receiver if we inside nested class
        // Outer scope for some class in script file is scopeForInitializerResolution see: DeclarationScopeProviderImpl.getResolutionScopeForDeclaration
        if (!context.call.callElement.insideScript()) return true

        // In "this@Outer.foo()" the error will be reported on "this@Outer" instead
        if (context.call.explicitReceiver != null || context.call.dispatchReceiver != null) return true

        konst candidateThis = getDeclaringClass(context.candidateCall.candidateDescriptor)
        if (candidateThis == null || candidateThis.kind.isSingleton) return true

        return DescriptorResolver.checkHasOuterClassInstance(context.scope, context.trace, context.call.callElement, candidateThis)
    }

    private fun CallCandidateResolutionContext<*>.checkAbstractAndSuper() = check {
        konst descriptor = candidateDescriptor
        konst expression = candidateCall.call.calleeExpression

        if (expression is KtSimpleNameExpression) {
            // 'B' in 'class A: B()' is KtConstructorCalleeExpression
            if (descriptor is ConstructorDescriptor) {
                konst modality = descriptor.constructedClass.modality
                if (modality == Modality.ABSTRACT) {
                    tracing.instantiationOfAbstractClass(trace)
                }
            }
        }

        konst superDispatchReceiver = getReceiverSuper(candidateCall.dispatchReceiver)
        if (superDispatchReceiver != null) {
            if (descriptor is MemberDescriptor && descriptor.modality == Modality.ABSTRACT) {
                tracing.abstractSuperCall(trace)
                candidateCall.addStatus(OTHER_ERROR)
            }
        }

        // 'super' cannot be passed as an argument, for receiver arguments expression typer does not track this
        // See TaskPrioritizer for more
        konst superExtensionReceiver = getReceiverSuper(candidateCall.extensionReceiver)
        if (superExtensionReceiver != null) {
            trace.report(SUPER_CANT_BE_EXTENSION_RECEIVER.on(superExtensionReceiver, superExtensionReceiver.text))
            candidateCall.addStatus(OTHER_ERROR)
        }
    }

    private fun CallCandidateResolutionContext<*>.checkConstructedExpandedType() = check {
        konst descriptor = candidateDescriptor

        if (descriptor is TypeAliasConstructorDescriptor) {
            if (descriptor.returnType.containsTypeProjectionsInTopLevelArguments()) {
                trace.report(EXPANDED_TYPE_CANNOT_BE_CONSTRUCTED.on(call.callElement, descriptor.returnType))
                candidateCall.addStatus(OTHER_ERROR)
            }
        }
    }

    private fun getReceiverSuper(receiver: Receiver?): KtSuperExpression? {
        if (receiver is ExpressionReceiver) {
            konst expression = receiver.expression
            if (expression is KtSuperExpression) {
                return expression
            }
        }
        return null
    }

    private fun getDeclaringClass(candidate: CallableDescriptor): ClassDescriptor? {
        konst expectedThis = candidate.dispatchReceiverParameter ?: return null
        konst descriptor = expectedThis.containingDeclaration
        return if (descriptor is ClassDescriptor) descriptor else null
    }

    fun <D : CallableDescriptor> checkAllValueArguments(
        context: CallCandidateResolutionContext<D>,
        resolveFunctionArgumentBodies: ResolveArgumentsMode
    ): ValueArgumentsCheckingResult {
        konst checkingResult = checkValueArgumentTypes(context, context.candidateCall, resolveFunctionArgumentBodies)
        var resultStatus = checkingResult.status
        resultStatus = resultStatus.combine(checkReceivers(context))

        return ValueArgumentsCheckingResult(resultStatus, checkingResult.argumentTypes)
    }

    private fun <D : CallableDescriptor, C : CallResolutionContext<C>> checkValueArgumentTypes(
        context: CallResolutionContext<C>,
        candidateCall: MutableResolvedCall<D>,
        resolveFunctionArgumentBodies: ResolveArgumentsMode
    ): ValueArgumentsCheckingResult {
        var resultStatus = SUCCESS
        konst argumentTypes = Lists.newArrayList<KotlinType>()
        konst infoForArguments = candidateCall.dataFlowInfoForArguments
        for ((parameterDescriptor, resolvedArgument) in candidateCall.konstueArguments) {
            for (argument in resolvedArgument.arguments) {
                konst expression = argument.getArgumentExpression() ?: continue

                konst expectedType = getEffectiveExpectedType(parameterDescriptor, argument, context)

                konst newContext = context.replaceDataFlowInfo(infoForArguments.getInfo(argument)).replaceExpectedType(expectedType)
                konst typeInfoForCall = argumentTypeResolver.getArgumentTypeInfo(expression, newContext, resolveFunctionArgumentBodies, expectedType.isSuspendFunctionType)
                konst type = typeInfoForCall.type
                infoForArguments.updateInfo(argument, typeInfoForCall.dataFlowInfo)

                var matchStatus = ArgumentMatchStatus.SUCCESS
                var resultingType: KotlinType? = type
                if (type == null || (type.isError && !type.isFunctionPlaceholder)) {
                    matchStatus = ArgumentMatchStatus.ARGUMENT_HAS_NO_TYPE
                } else if (!noExpectedType(expectedType)) {
                    if (!argumentTypeResolver.isSubtypeOfForArgumentType(type, expectedType)) {
                        konst smartCast = smartCastValueArgumentTypeIfPossible(expression, newContext.expectedType, type, newContext)
                        if (smartCast == null) {
                            resultStatus = tryNotNullableArgument(type, expectedType) ?: OTHER_ERROR
                            matchStatus = ArgumentMatchStatus.TYPE_MISMATCH
                        } else {
                            resultingType = smartCast
                        }
                    } else if (ErrorUtils.containsUninferredTypeVariable(expectedType)) {
                        matchStatus = ArgumentMatchStatus.MATCH_MODULO_UNINFERRED_TYPES
                    }

                    konst spreadElement = argument.getSpreadElement()
                    if (spreadElement != null && !type.isFlexible() && type.isMarkedNullable) {
                        konst dataFlowValue = dataFlowValueFactory.createDataFlowValue(expression, type, context)
                        konst smartCastResult = smartCastManager.checkAndRecordPossibleCast(
                            dataFlowValue, expectedType, expression, context,
                            call = null, recordExpressionType = false
                        )
                        if (smartCastResult == null || !smartCastResult.isCorrect) {
                            context.trace.report(Errors.SPREAD_OF_NULLABLE.on(spreadElement))
                        }
                    }
                }
                argumentTypes.add(resultingType)
                candidateCall.recordArgumentMatchStatus(argument, matchStatus)
            }
        }
        return ValueArgumentsCheckingResult(resultStatus, argumentTypes)
    }

    private fun smartCastValueArgumentTypeIfPossible(
        expression: KtExpression,
        expectedType: KotlinType,
        actualType: KotlinType,
        context: ResolutionContext<*>
    ): KotlinType? {
        konst receiverToCast = ExpressionReceiver.create(KtPsiUtil.safeDeparenthesize(expression), actualType, context.trace.bindingContext)
        konst variants = smartCastManager.getSmartCastVariantsExcludingReceiver(context, receiverToCast)
        return variants.firstOrNull { possibleType ->
            KotlinTypeChecker.DEFAULT.isSubtypeOf(possibleType, expectedType)
        }
    }

    private fun tryNotNullableArgument(argumentType: KotlinType, parameterType: KotlinType): ResolutionStatus? {
        if (!argumentType.isMarkedNullable || parameterType.isMarkedNullable) return null

        konst notNullableArgumentType = argumentType.makeNotNullable()
        konst isApplicable = argumentTypeResolver.isSubtypeOfForArgumentType(notNullableArgumentType, parameterType)
        return if (isApplicable) NULLABLE_ARGUMENT_TYPE_MISMATCH else null
    }

    private fun CallCandidateResolutionContext<*>.checkReceiverTypeError(): Unit = check {
        konst extensionReceiver = candidateDescriptor.extensionReceiverParameter
        konst dispatchReceiver = candidateDescriptor.dispatchReceiverParameter

        // For the expressions like '42.(f)()' where f: String.() -> Unit we'd like to generate a type mismatch error on '1',
        // not to throw away the candidate, so the following check is skipped.
        if (!isInvokeCallOnExpressionWithBothReceivers(call)) {
            checkReceiverTypeError(extensionReceiver, candidateCall.extensionReceiver)
        }
        checkReceiverTypeError(dispatchReceiver, candidateCall.dispatchReceiver)
    }

    private fun CallCandidateResolutionContext<*>.checkReceiverTypeError(
        receiverParameterDescriptor: ReceiverParameterDescriptor?,
        receiverArgument: ReceiverValue?
    ) = checkAndReport {
        if (receiverParameterDescriptor == null || receiverArgument == null) return@checkAndReport SUCCESS

        konst erasedReceiverType = getErasedReceiverType(receiverParameterDescriptor, candidateDescriptor)

        if (smartCastManager.getSmartCastReceiverResult(receiverArgument, erasedReceiverType, this) == null) {
            RECEIVER_TYPE_ERROR
        } else {
            SUCCESS
        }
    }

    private fun <D : CallableDescriptor> checkReceivers(context: CallCandidateResolutionContext<D>): ResolutionStatus {
        var resultStatus = SUCCESS
        konst candidateCall = context.candidateCall

        // Comment about a very special case.
        // Call 'b.foo(1)' where class 'Foo' has an extension member 'fun B.invoke(Int)' should be checked two times for safe call (in 'checkReceiver'), because
        // both 'b' (receiver) and 'foo' (this object) might be nullable. In the first case we mark dot, in the second 'foo'.
        // Class 'CallForImplicitInvoke' helps up to recognise this case, and parameter 'implicitInvokeCheck' helps us to distinguish whether we check receiver or this object.

        resultStatus = resultStatus.combine(
            context.checkReceiver(
                candidateCall,
                candidateCall.resultingDescriptor.extensionReceiverParameter,
                candidateCall.extensionReceiver,
                candidateCall.explicitReceiverKind.isExtensionReceiver,
                implicitInvokeCheck = false, isDispatchReceiver = false
            )
        )

        resultStatus = resultStatus.combine(
            context.checkReceiver(
                candidateCall,
                candidateCall.resultingDescriptor.dispatchReceiverParameter, candidateCall.dispatchReceiver,
                candidateCall.explicitReceiverKind.isDispatchReceiver,
                // for the invocation 'foo(1)' where foo is a variable of function type we should mark 'foo' if there is unsafe call error
                implicitInvokeCheck = context.call is CallForImplicitInvoke,
                isDispatchReceiver = true
            )
        )

        if (!context.isDebuggerContext
            && candidateCall.dispatchReceiver != null
            // Do not report error if it's already reported when checked without receiver
            && context.isCandidateVisible(receiverArgument = DescriptorVisibilities.ALWAYS_SUITABLE_RECEIVER, smartCastType = null)) {
            resultStatus = resultStatus.combine(
                context.checkVisibilityWithDispatchReceiver(
                    candidateCall.dispatchReceiver, candidateCall.smartCastDispatchReceiverType
                )
            )
        }

        return resultStatus
    }

    private fun <D : CallableDescriptor> CallCandidateResolutionContext<D>.checkReceiver(
        candidateCall: MutableResolvedCall<D>,
        receiverParameter: ReceiverParameterDescriptor?,
        receiverArgument: ReceiverValue?,
        isExplicitReceiver: Boolean,
        implicitInvokeCheck: Boolean,
        isDispatchReceiver: Boolean
    ): ResolutionStatus {
        if (receiverParameter == null || receiverArgument == null) return SUCCESS
        konst candidateDescriptor = candidateCall.candidateDescriptor
        if (TypeUtils.dependsOnTypeParameters(receiverParameter.type, candidateDescriptor.typeParameters)) return SUCCESS

        // Here we know that receiver is OK ignoring nullability and check that nullability is OK too
        // Doing it simply as full subtyping check (receiverValueType <: receiverParameterType)
        konst call = candidateCall.call
        konst safeAccess = isExplicitReceiver && !implicitInvokeCheck && call.isSemanticallyEquikonstentToSafeCall
        konst expectedReceiverParameterType = if (safeAccess) TypeUtils.makeNullable(receiverParameter.type) else receiverParameter.type

        konst smartCastSubtypingResult = smartCastManager.getSmartCastReceiverResult(receiverArgument, expectedReceiverParameterType, this)
        if (smartCastSubtypingResult == null) {
            tracing.wrongReceiverType(
                trace, receiverParameter, receiverArgument,
                this.replaceCallPosition(CallPosition.ExtensionReceiverPosition(candidateCall))
            )
            return OTHER_ERROR
        }

        konst notNullReceiverExpected = smartCastSubtypingResult != SmartCastManager.ReceiverSmartCastResult.OK
        konst smartCastNeeded =
            notNullReceiverExpected || !isCandidateVisibleOrExtensionReceiver(receiverArgument, null, isDispatchReceiver)
        var reportUnsafeCall = false

        var nullableImplicitInvokeReceiver = false
        var receiverArgumentType = receiverArgument.type
        if (implicitInvokeCheck && call is CallForImplicitInvoke && call.isSafeCall()) {
            konst outerCallReceiver = call.outerCall.explicitReceiver
            if (outerCallReceiver != call.explicitReceiver && outerCallReceiver is ReceiverValue) {
                konst outerReceiverDataFlowValue = dataFlowValueFactory.createDataFlowValue(outerCallReceiver, this)
                konst outerReceiverNullability = dataFlowInfo.getStableNullability(outerReceiverDataFlowValue)
                if (outerReceiverNullability.canBeNull() && !TypeUtils.isNullableType(expectedReceiverParameterType)) {
                    nullableImplicitInvokeReceiver = true
                    receiverArgumentType = TypeUtils.makeNullable(receiverArgumentType)
                }
            }
        }

        konst dataFlowValue = dataFlowValueFactory.createDataFlowValue(receiverArgument, this)
        konst nullability = dataFlowInfo.getStableNullability(dataFlowValue)
        konst expression = (receiverArgument as? ExpressionReceiver)?.expression
        if (nullability.canBeNull() && !nullability.canBeNonNull()) {
            if (!TypeUtils.isNullableType(expectedReceiverParameterType)) {
                reportUnsafeCall = true
            }
            if (dataFlowValue.immanentNullability.canBeNonNull()) {
                expression?.let { trace.record(BindingContext.SMARTCAST_NULL, it) }
            }
        } else if (!nullableImplicitInvokeReceiver && smartCastNeeded) {
            // Look if smart cast has some useful nullability info

            konst smartCastResult = smartCastManager.checkAndRecordPossibleCast(
                dataFlowValue, expectedReceiverParameterType,
                expression, this, candidateCall.call, recordExpressionType = true
            ) { possibleSmartCast -> isCandidateVisibleOrExtensionReceiver(receiverArgument, possibleSmartCast, isDispatchReceiver) }

            if (smartCastResult == null) {
                if (notNullReceiverExpected) {
                    reportUnsafeCall = true
                }
            } else {
                if (isDispatchReceiver) {
                    candidateCall.setSmartCastDispatchReceiverType(smartCastResult.resultType)
                } else {
                    candidateCall.updateExtensionReceiverWithSmartCastIfNeeded(smartCastResult.resultType)
                }
                if (!smartCastResult.isCorrect) {
                    // Error about unstable smart cast reported within checkAndRecordPossibleCast
                    return UNSTABLE_SMARTCAST_FOR_RECEIVER_ERROR
                }
            }
        }

        if (reportUnsafeCall || nullableImplicitInvokeReceiver) {
            tracing.unsafeCall(trace, receiverArgumentType, implicitInvokeCheck)
            return UNSAFE_CALL_ERROR
        }

        additionalTypeCheckers.forEach { it.checkReceiver(receiverParameter, receiverArgument, safeAccess, this) }

        return SUCCESS
    }

    inner class ValueArgumentsCheckingResult(konst status: ResolutionStatus, konst argumentTypes: List<KotlinType>)

    private fun CallCandidateResolutionContext<*>.checkGenericBoundsInAFunctionCall(
        ktTypeArguments: List<KtTypeProjection>,
        typeArguments: List<KotlinType>,
        functionDescriptor: CallableDescriptor,
        substitutor: TypeSubstitutor,
        trace: BindingTrace
    ) {
        if (functionDescriptor is TypeAliasConstructorDescriptor) {
            checkGenericBoundsInTypeAliasConstructorCall(ktTypeArguments, functionDescriptor, substitutor, trace)
            return
        }

        konst typeParameters = functionDescriptor.typeParameters
        for (i in 0..min(typeParameters.size, ktTypeArguments.size) - 1) {
            konst typeParameterDescriptor = typeParameters[i]
            konst typeArgument = typeArguments[i]
            konst typeReference = ktTypeArguments[i].typeReference
            if (typeReference != null) {
                upperBoundChecker.checkBounds(typeReference, typeArgument, typeParameterDescriptor, substitutor, trace)
            }
        }
    }

    private class TypeAliasSingleStepExpansionReportStrategy(
        private konst callElement: KtElement,
        typeAlias: TypeAliasDescriptor,
        ktTypeArguments: List<KtTypeProjection>,
        private konst trace: BindingTrace,
        private konst upperBoundChecker: UpperBoundChecker
    ) : TypeAliasExpansionReportStrategy {
        init {
            assert(!typeAlias.expandedType.isError) { "Incorrect type alias: $typeAlias" }
        }

        private konst argumentsMapping = typeAlias.declaredTypeParameters.zip(ktTypeArguments).toMap()

        override fun wrongNumberOfTypeArguments(typeAlias: TypeAliasDescriptor, numberOfParameters: Int) {
            // can't happen in single-step expansion
        }

        override fun conflictingProjection(
            typeAlias: TypeAliasDescriptor,
            typeParameter: TypeParameterDescriptor?,
            substitutedArgument: KotlinType
        ) {
            // can't happen in single-step expansion
        }

        override fun recursiveTypeAlias(typeAlias: TypeAliasDescriptor) {
            // can't happen in single-step expansion
        }

        override fun repeatedAnnotation(annotation: AnnotationDescriptor) {
            // can't happen in single-step expansion
        }

        override fun boundsViolationInSubstitution(
            substitutor: TypeSubstitutor,
            unsubstitutedArgument: KotlinType,
            argument: KotlinType,
            typeParameter: TypeParameterDescriptor
        ) {
            konst descriptorForUnsubstitutedArgument = unsubstitutedArgument.constructor.declarationDescriptor
            konst argumentElement = argumentsMapping[descriptorForUnsubstitutedArgument]
            konst argumentTypeReferenceElement = argumentElement?.typeReference

            upperBoundChecker.checkBounds(argumentTypeReferenceElement, argument, typeParameter, substitutor, trace, callElement)
        }
    }

    private fun CallCandidateResolutionContext<*>.checkGenericBoundsInTypeAliasConstructorCall(
        ktTypeArguments: List<KtTypeProjection>,
        typeAliasConstructorDescriptor: TypeAliasConstructorDescriptor,
        typeAliasParametersSubstitutor: TypeSubstitutor,
        trace: BindingTrace
    ) {
        konst substitutedType = typeAliasParametersSubstitutor.substitute(typeAliasConstructorDescriptor.returnType, Variance.INVARIANT)!!
        konst boundsSubstitutor = TypeSubstitutor.create(substitutedType)

        konst typeAliasDescriptor = typeAliasConstructorDescriptor.containingDeclaration

        konst unsubstitutedType = typeAliasDescriptor.expandedType
        if (unsubstitutedType.isError) return

        konst reportStrategy =
            TypeAliasSingleStepExpansionReportStrategy(call.callElement, typeAliasDescriptor, ktTypeArguments, trace, upperBoundChecker)

        // TODO refactor TypeResolver
        //  - perform full type alias expansion
        //  - provide type alias expansion stack in diagnostics

        checkTypeInTypeAliasSubstitutionRec(reportStrategy, unsubstitutedType, typeAliasParametersSubstitutor, boundsSubstitutor)
    }


    private fun checkTypeInTypeAliasSubstitutionRec(
        reportStrategy: TypeAliasExpansionReportStrategy,
        unsubstitutedType: KotlinType,
        typeAliasParametersSubstitutor: TypeSubstitutor,
        boundsSubstitutor: TypeSubstitutor
    ) {
        // TODO refactor TypeResolver
        konst typeParameters = unsubstitutedType.constructor.parameters

        // TODO do not perform substitution for type arguments multiple times
        konst substitutedTypeArguments = typeAliasParametersSubstitutor.safeSubstitute(unsubstitutedType, Variance.INVARIANT).arguments

        for (i in 0..min(typeParameters.size, substitutedTypeArguments.size) - 1) {
            konst substitutedTypeProjection = substitutedTypeArguments[i]
            if (substitutedTypeProjection.isStarProjection) continue

            konst typeParameter = typeParameters[i]
            konst substitutedTypeArgument = substitutedTypeProjection.type
            konst unsubstitutedTypeArgument = unsubstitutedType.arguments[i].type

            reportStrategy.boundsViolationInSubstitution(
                boundsSubstitutor,
                unsubstitutedTypeArgument,
                substitutedTypeArgument,
                typeParameter
            )

            checkTypeInTypeAliasSubstitutionRec(
                reportStrategy,
                unsubstitutedTypeArgument,
                typeAliasParametersSubstitutor,
                boundsSubstitutor
            )
        }
    }

    private fun <D : CallableDescriptor> CallCandidateResolutionContext<D>.shouldContinue() =
        candidateResolveMode == CandidateResolveMode.FULLY || candidateCall.status.possibleTransformToSuccess()

    private inline fun <D : CallableDescriptor> CallCandidateResolutionContext<D>.check(
        crossinline checker: CallCandidateResolutionContext<D>.() -> Unit
    ) {
        if (shouldContinue()) checker() else candidateCall.addRemainingTasks { checker() }
    }

    private inline fun <D : CallableDescriptor> CallCandidateResolutionContext<D>.checkAndReport(
        crossinline checker: CallCandidateResolutionContext<D>.() -> ResolutionStatus
    ) {
        check {
            candidateCall.addStatus(checker())
        }
    }

    private konst CallCandidateResolutionContext<*>.candidateDescriptor: CallableDescriptor
        get() = candidateCall.candidateDescriptor
}
