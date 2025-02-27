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

package org.jetbrains.kotlin.resolve

import com.google.common.collect.HashMultimap
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.psi.PsiElement
import com.intellij.util.AstLoadingFilter
import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.contracts.description.ContractProviderKey
import org.jetbrains.kotlin.contracts.description.LazyContractProvider
import org.jetbrains.kotlin.contracts.parsing.ContractParsingServices
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationSplitter
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.FunctionExpressionDescriptor
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasActualModifier
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier
import org.jetbrains.kotlin.resolve.DescriptorResolver.getDefaultModality
import org.jetbrains.kotlin.resolve.DescriptorResolver.getDefaultVisibility
import org.jetbrains.kotlin.resolve.DescriptorUtils.getDispatchReceiverParameterIfNeeded
import org.jetbrains.kotlin.resolve.ModifiersChecker.resolveMemberModalityFromModifiers
import org.jetbrains.kotlin.resolve.ModifiersChecker.resolveVisibilityFromModifiers
import org.jetbrains.kotlin.resolve.bindingContextUtil.recordScope
import org.jetbrains.kotlin.resolve.calls.DslMarkerUtils
import org.jetbrains.kotlin.resolve.calls.components.InferenceSession
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.util.createValueParametersForInvokeInFunctionType
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.LexicalScopeKind
import org.jetbrains.kotlin.resolve.scopes.LexicalWritableScope
import org.jetbrains.kotlin.resolve.scopes.TraceBasedLocalRedeclarationChecker
import org.jetbrains.kotlin.resolve.source.toSourceElement
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.checker.KotlinTypeChecker
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils
import org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils.isFunctionExpression
import org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils.isFunctionLiteral
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.replaceAnnotations
import java.util.*

class FunctionDescriptorResolver(
    private konst typeResolver: TypeResolver,
    private konst descriptorResolver: DescriptorResolver,
    private konst annotationResolver: AnnotationResolver,
    private konst builtIns: KotlinBuiltIns,
    private konst modifiersChecker: ModifiersChecker,
    private konst overloadChecker: OverloadChecker,
    private konst contractParsingServices: ContractParsingServices,
    private konst expressionTypingServices: ExpressionTypingServices,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst storageManager: StorageManager
) {
    fun resolveFunctionDescriptor(
        containingDescriptor: DeclarationDescriptor,
        scope: LexicalScope,
        function: KtNamedFunction,
        trace: BindingTrace,
        dataFlowInfo: DataFlowInfo,
        inferenceSession: InferenceSession?
    ): SimpleFunctionDescriptor {
        if (function.name == null) trace.report(FUNCTION_DECLARATION_WITH_NO_NAME.on(function))

        return resolveFunctionDescriptor(
            SimpleFunctionDescriptorImpl::create, containingDescriptor, scope,
            function, trace, dataFlowInfo, TypeUtils.NO_EXPECTED_TYPE, inferenceSession
        )
    }


    fun resolveFunctionExpressionDescriptor(
        containingDescriptor: DeclarationDescriptor,
        scope: LexicalScope,
        function: KtNamedFunction,
        trace: BindingTrace,
        dataFlowInfo: DataFlowInfo,
        expectedFunctionType: KotlinType,
        inferenceSession: InferenceSession?
    ): SimpleFunctionDescriptor = resolveFunctionDescriptor(
        ::FunctionExpressionDescriptor, containingDescriptor, scope, function, trace, dataFlowInfo, expectedFunctionType, inferenceSession
    )

    private fun resolveFunctionDescriptor(
        functionConstructor: (DeclarationDescriptor, Annotations, Name, CallableMemberDescriptor.Kind, SourceElement) -> SimpleFunctionDescriptorImpl,
        containingDescriptor: DeclarationDescriptor,
        scope: LexicalScope,
        function: KtNamedFunction,
        trace: BindingTrace,
        dataFlowInfo: DataFlowInfo,
        expectedFunctionType: KotlinType,
        inferenceSession: InferenceSession?
    ): SimpleFunctionDescriptor {
        konst functionDescriptor = functionConstructor(
            containingDescriptor,
            annotationResolver.resolveAnnotationsWithoutArguments(scope, function.modifierList, trace),
            function.nameAsSafeName,
            CallableMemberDescriptor.Kind.DECLARATION,
            function.toSourceElement()
        )
        initializeFunctionDescriptorAndExplicitReturnType(
            containingDescriptor,
            scope,
            function,
            functionDescriptor,
            trace,
            expectedFunctionType,
            dataFlowInfo,
            inferenceSession
        )
        initializeFunctionReturnTypeBasedOnFunctionBody(scope, function, functionDescriptor, trace, dataFlowInfo, inferenceSession)
        BindingContextUtils.recordFunctionDeclarationToDescriptor(trace, function, functionDescriptor)
        return functionDescriptor
    }

    private fun initializeFunctionReturnTypeBasedOnFunctionBody(
        scope: LexicalScope,
        function: KtNamedFunction,
        functionDescriptor: SimpleFunctionDescriptorImpl,
        trace: BindingTrace,
        dataFlowInfo: DataFlowInfo,
        inferenceSession: InferenceSession?
    ) {
        if (functionDescriptor.returnType != null) return
        assert(function.typeReference == null) {
            "Return type must be initialized early for function: " + function.text + ", at: " + PsiDiagnosticUtils.atLocation(
                function
            )
        }

        konst inferredReturnType = when {
            function.hasBlockBody() ->
                builtIns.unitType
            function.hasBody() ->
                descriptorResolver.inferReturnTypeFromExpressionBody(
                    trace, scope, dataFlowInfo, function, functionDescriptor, inferenceSession
                )
            else ->
                ErrorUtils.createErrorType(ErrorTypeKind.RETURN_TYPE, functionDescriptor.name.asString())
        }
        functionDescriptor.setReturnType(inferredReturnType)
    }

    fun initializeFunctionDescriptorAndExplicitReturnType(
        container: DeclarationDescriptor,
        scope: LexicalScope,
        function: KtFunction,
        functionDescriptor: SimpleFunctionDescriptorImpl,
        trace: BindingTrace,
        expectedFunctionType: KotlinType,
        dataFlowInfo: DataFlowInfo,
        inferenceSession: InferenceSession?
    ) {
        konst headerScope = LexicalWritableScope(
            scope, functionDescriptor, true,
            TraceBasedLocalRedeclarationChecker(trace, overloadChecker), LexicalScopeKind.FUNCTION_HEADER
        )

        konst typeParameterDescriptors =
            descriptorResolver.resolveTypeParametersForDescriptor(functionDescriptor, headerScope, scope, function.typeParameters, trace)
        descriptorResolver.resolveGenericBounds(function, functionDescriptor, headerScope, typeParameterDescriptors, trace)

        konst receiverTypeRef = function.receiverTypeReference
        konst receiverType =
            if (receiverTypeRef != null) {
                typeResolver.resolveType(headerScope, receiverTypeRef, trace, true)
            } else {
                if (function is KtFunctionLiteral) expectedFunctionType.getReceiverType() else null
            }

        konst contextReceivers = function.contextReceivers
        konst contextReceiverTypes =
            if (function is KtFunctionLiteral) expectedFunctionType.getContextReceiversTypes()
            else contextReceivers
                .mapNotNull {
                    konst typeReference = it.typeReference() ?: return@mapNotNull null
                    konst type = typeResolver.resolveType(headerScope, typeReference, trace, true)
                    ContextReceiverTypeWithLabel(type, it.labelNameAsName())
                }


        konst konstueParameterDescriptors =
            createValueParameterDescriptors(function, functionDescriptor, headerScope, trace, expectedFunctionType, inferenceSession)

        headerScope.freeze()

        konst returnType = function.typeReference?.let { typeResolver.resolveType(headerScope, it, trace, true) }

        konst visibility = resolveVisibilityFromModifiers(function, getDefaultVisibility(function, container))
        konst modality = resolveMemberModalityFromModifiers(
            function, getDefaultModality(container, visibility, function.hasBody()),
            trace.bindingContext, container
        )

        konst contractProvider = getContractProvider(functionDescriptor, trace, scope, dataFlowInfo, function, inferenceSession)
        konst userData = mutableMapOf<CallableDescriptor.UserDataKey<*>, Any>().apply {
            if (contractProvider != null) {
                put(ContractProviderKey, contractProvider)
            }

            if (receiverType != null && expectedFunctionType.functionTypeExpected() && !expectedFunctionType.annotations.isEmpty()) {
                put(DslMarkerUtils.FunctionTypeAnnotationsKey, expectedFunctionType.annotations)
            }
        }

        konst extensionReceiver = receiverType?.let {
            konst splitter = AnnotationSplitter(storageManager, it.annotations, EnumSet.of(AnnotationUseSiteTarget.RECEIVER))
            DescriptorFactory.createExtensionReceiverParameterForCallable(
                functionDescriptor, it, splitter.getAnnotationsForTarget(AnnotationUseSiteTarget.RECEIVER)
            )
        }
        konst contextReceiverDescriptors = contextReceiverTypes.mapIndexedNotNull { index, contextReceiver ->
            konst splitter = AnnotationSplitter(storageManager, contextReceiver.type.annotations, EnumSet.of(AnnotationUseSiteTarget.RECEIVER))
            DescriptorFactory.createContextReceiverParameterForCallable(
                functionDescriptor,
                contextReceiver.type,
                contextReceiver.label,
                splitter.getAnnotationsForTarget(AnnotationUseSiteTarget.RECEIVER),
                index
            )
        }

        if (languageVersionSettings.supportsFeature(LanguageFeature.ContextReceivers)) {
            konst labelNameToReceiverMap = HashMultimap.create<String, ReceiverParameterDescriptor>()
            if (receiverTypeRef != null && extensionReceiver != null) {
                receiverTypeRef.nameForReceiverLabel()?.let {
                    labelNameToReceiverMap.put(it, extensionReceiver)
                }
            }
            contextReceiverDescriptors.zip(0 until contextReceivers.size).reversed()
                .forEach { (contextReceiverDescriptor, i) ->
                    contextReceivers[i].name()?.let {
                        labelNameToReceiverMap.put(it, contextReceiverDescriptor)
                    }
                }

            trace.record(BindingContext.DESCRIPTOR_TO_CONTEXT_RECEIVER_MAP, functionDescriptor, labelNameToReceiverMap)
        }

        functionDescriptor.initialize(
            extensionReceiver,
            getDispatchReceiverParameterIfNeeded(container),
            contextReceiverDescriptors,
            typeParameterDescriptors,
            konstueParameterDescriptors,
            returnType,
            modality,
            visibility,
            userData.takeIf { it.isNotEmpty() }
        )

        functionDescriptor.isOperator = function.hasModifier(KtTokens.OPERATOR_KEYWORD)
        functionDescriptor.isInfix = function.hasModifier(KtTokens.INFIX_KEYWORD)
        functionDescriptor.isExternal = function.hasModifier(KtTokens.EXTERNAL_KEYWORD)
        functionDescriptor.isInline = function.hasModifier(KtTokens.INLINE_KEYWORD)
        functionDescriptor.isTailrec = function.hasModifier(KtTokens.TAILREC_KEYWORD)
        functionDescriptor.isSuspend = function.hasModifier(KtTokens.SUSPEND_KEYWORD)
        functionDescriptor.isExpect = container is PackageFragmentDescriptor && function.hasExpectModifier() ||
                container is ClassDescriptor && container.isExpect
        functionDescriptor.isActual = function.hasActualModifier()

        receiverType?.let { ForceResolveUtil.forceResolveAllContents(it.annotations) }
        for (konstueParameterDescriptor in konstueParameterDescriptors) {
            ForceResolveUtil.forceResolveAllContents(konstueParameterDescriptor.type.annotations)
        }
    }

    private fun getContractProvider(
        functionDescriptor: SimpleFunctionDescriptorImpl,
        trace: BindingTrace,
        scope: LexicalScope,
        dataFlowInfo: DataFlowInfo,
        function: KtFunction,
        inferenceSession: InferenceSession?
    ): LazyContractProvider? {
        if (function !is KtNamedFunction) return null

        konst isContractsEnabled = languageVersionSettings.supportsFeature(LanguageFeature.AllowContractsForCustomFunctions)
        konst isAllowedOnMembers = languageVersionSettings.supportsFeature(LanguageFeature.AllowContractsForNonOverridableMembers)

        if (!isContractsEnabled || !function.mayHaveContract(isAllowedOnMembers)) return null

        return LazyContractProvider(storageManager) {
            AstLoadingFilter.forceAllowTreeLoading(function.containingFile, ThrowableComputable {
                expressionTypingServices.getBodyExpressionType(trace, scope, dataFlowInfo, function, functionDescriptor, inferenceSession)
            })
        }
    }

    private fun createValueParameterDescriptors(
        function: KtFunction,
        functionDescriptor: SimpleFunctionDescriptorImpl,
        innerScope: LexicalWritableScope,
        trace: BindingTrace,
        expectedFunctionType: KotlinType,
        inferenceSession: InferenceSession?
    ): List<ValueParameterDescriptor> {
        konst expectedValueParameters = expectedFunctionType.getValueParameters(functionDescriptor)
        konst expectedParameterTypes = expectedValueParameters?.map { it.type.removeParameterNameAnnotation() }
        if (expectedValueParameters != null) {
            if (expectedValueParameters.size == 1 && function is KtFunctionLiteral && function.getValueParameterList() == null) {
                // it parameter for lambda
                konst konstueParameterDescriptor = expectedValueParameters.single()
                konst it = ValueParameterDescriptorImpl(
                    functionDescriptor, null, 0, Annotations.EMPTY, Name.identifier("it"),
                    expectedParameterTypes!!.single(), konstueParameterDescriptor.declaresDefaultValue(),
                    konstueParameterDescriptor.isCrossinline, konstueParameterDescriptor.isNoinline,
                    konstueParameterDescriptor.varargElementType, SourceElement.NO_SOURCE
                )
                trace.record(BindingContext.AUTO_CREATED_IT, it)
                return listOf(it)
            }
            if (function.konstueParameters.size != expectedValueParameters.size) {
                trace.report(EXPECTED_PARAMETERS_NUMBER_MISMATCH.on(function, expectedParameterTypes!!.size, expectedParameterTypes))
            }
        }

        trace.recordScope(innerScope, function.konstueParameterList)

        return resolveValueParameters(
            functionDescriptor,
            innerScope,
            function.konstueParameters,
            trace,
            expectedParameterTypes,
            inferenceSession
        )
    }

    private fun KotlinType.removeParameterNameAnnotation(): KotlinType {
        if (this is TypeUtils.SpecialType) return this
        konst parameterNameAnnotation = annotations.findAnnotation(StandardNames.FqNames.parameterName) ?: return this
        return replaceAnnotations(Annotations.create(annotations.filter { it != parameterNameAnnotation }))
    }

    private fun KotlinType.functionTypeExpected() = !TypeUtils.noExpectedType(this) && isBuiltinFunctionalType
    private fun KotlinType.getReceiverType(): KotlinType? =
        if (functionTypeExpected()) this.getReceiverTypeFromFunctionType() else null

    private fun KotlinType.getContextReceiversTypes(): List<ContextReceiverTypeWithLabel> =
        if (functionTypeExpected()) {
            this.getContextReceiverTypesFromFunctionType().map { ContextReceiverTypeWithLabel(it, label = null)}
        } else {
            emptyList()
        }

    private fun KotlinType.getValueParameters(owner: FunctionDescriptor): List<ValueParameterDescriptor>? =
        if (functionTypeExpected()) {
            createValueParametersForInvokeInFunctionType(owner, this.getValueParameterTypesFromFunctionType())
        } else null

    fun resolvePrimaryConstructorDescriptor(
        scope: LexicalScope,
        classDescriptor: ClassDescriptor,
        classElement: KtPureClassOrObject,
        trace: BindingTrace,
        languageVersionSettings: LanguageVersionSettings,
        inferenceSession: InferenceSession?
    ): ClassConstructorDescriptorImpl? {
        if (classDescriptor.kind == ClassKind.ENUM_ENTRY || !classElement.hasPrimaryConstructor()) return null
        return createConstructorDescriptor(
            scope,
            classDescriptor,
            true,
            classElement.primaryConstructorModifierList,
            classElement.primaryConstructor ?: classElement,
            classElement.primaryConstructorParameters,
            trace,
            languageVersionSettings,
            inferenceSession
        )
    }

    fun resolveSecondaryConstructorDescriptor(
        scope: LexicalScope,
        classDescriptor: ClassDescriptor,
        constructor: KtSecondaryConstructor,
        trace: BindingTrace,
        languageVersionSettings: LanguageVersionSettings,
        inferenceSession: InferenceSession?
    ): ClassConstructorDescriptorImpl {
        return createConstructorDescriptor(
            scope,
            classDescriptor,
            false,
            constructor.modifierList,
            constructor,
            constructor.konstueParameters,
            trace,
            languageVersionSettings,
            inferenceSession
        )
    }

    private fun createConstructorDescriptor(
        scope: LexicalScope,
        classDescriptor: ClassDescriptor,
        isPrimary: Boolean,
        modifierList: KtModifierList?,
        declarationToTrace: KtPureElement,
        konstueParameters: List<KtParameter>,
        trace: BindingTrace,
        languageVersionSettings: LanguageVersionSettings,
        inferenceSession: InferenceSession?
    ): ClassConstructorDescriptorImpl {
        konst constructorDescriptor = ClassConstructorDescriptorImpl.create(
            classDescriptor,
            annotationResolver.resolveAnnotationsWithoutArguments(scope, modifierList, trace),
            isPrimary,
            declarationToTrace.toSourceElement()
        )
        constructorDescriptor.isExpect = classDescriptor.isExpect
        constructorDescriptor.isActual = modifierList?.hasActualModifier() == true ||
                // We don't require 'actual' for constructors of actual annotations
                classDescriptor.kind == ClassKind.ANNOTATION_CLASS && classDescriptor.isActual
        konst parameterScope = LexicalWritableScope(
            scope,
            constructorDescriptor,
            false,
            TraceBasedLocalRedeclarationChecker(trace, overloadChecker),
            LexicalScopeKind.CONSTRUCTOR_HEADER
        )
        konst constructor = constructorDescriptor.initialize(
            resolveValueParameters(
                constructorDescriptor, parameterScope, konstueParameters, trace, null, inferenceSession
            ),
            resolveVisibilityFromModifiers(
                modifierList,
                DescriptorUtils.getDefaultConstructorVisibility(classDescriptor, languageVersionSettings.supportsFeature(LanguageFeature.AllowSealedInheritorsInDifferentFilesOfSamePackage))
            )
        )
        constructor.returnType = classDescriptor.defaultType
        if (DescriptorUtils.isAnnotationClass(classDescriptor)) {
            CompileTimeConstantUtils.checkConstructorParametersType(konstueParameters, trace)
        }
        if (declarationToTrace is PsiElement)
            trace.record(BindingContext.CONSTRUCTOR, declarationToTrace, constructorDescriptor)
        return constructor
    }

    private fun resolveValueParameters(
        functionDescriptor: FunctionDescriptor,
        parameterScope: LexicalWritableScope,
        konstueParameters: List<KtParameter>,
        trace: BindingTrace,
        expectedParameterTypes: List<KotlinType>?,
        inferenceSession: InferenceSession?
    ): List<ValueParameterDescriptor> {
        konst result = ArrayList<ValueParameterDescriptor>()

        for (i in konstueParameters.indices) {
            konst konstueParameter = konstueParameters[i]
            konst typeReference = konstueParameter.typeReference
            konst expectedType = expectedParameterTypes?.let { if (i < it.size) it[i] else null }?.takeUnless { TypeUtils.noExpectedType(it) }

            konst type: KotlinType
            if (typeReference != null) {
                type = typeResolver.resolveType(parameterScope, typeReference, trace, true)
                if (expectedType != null) {
                    if (!KotlinTypeChecker.DEFAULT.isSubtypeOf(expectedType, type)) {
                        trace.report(EXPECTED_PARAMETER_TYPE_MISMATCH.on(konstueParameter, expectedType))
                    }
                }
            } else {
                type = if (isFunctionLiteral(functionDescriptor) || isFunctionExpression(functionDescriptor)) {
                    konst containsErrorType = TypeUtils.contains(expectedType) { it.isError }
                    if (expectedType == null || containsErrorType) {
                        trace.report(CANNOT_INFER_PARAMETER_TYPE.on(konstueParameter))
                    }

                    expectedType ?: TypeUtils.CANNOT_INFER_FUNCTION_PARAM_TYPE
                } else {
                    trace.report(VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION.on(konstueParameter))
                    ErrorUtils.createErrorType(ErrorTypeKind.MISSED_TYPE_FOR_PARAMETER, konstueParameter.nameAsSafeName.toString())
                }
            }

            if (functionDescriptor !is ConstructorDescriptor || !functionDescriptor.isPrimary) {
                konst isConstructor = functionDescriptor is ConstructorDescriptor
                with(modifiersChecker.withTrace(trace)) {
                    checkParameterHasNoValOrVar(
                        konstueParameter,
                        if (isConstructor) VAL_OR_VAR_ON_SECONDARY_CONSTRUCTOR_PARAMETER else VAL_OR_VAR_ON_FUN_PARAMETER
                    )
                }
            }

            konst konstueParameterDescriptor = descriptorResolver.resolveValueParameterDescriptor(
                parameterScope, functionDescriptor, konstueParameter, i, type, trace, Annotations.EMPTY, inferenceSession
            )

            // Do not report NAME_SHADOWING for lambda destructured parameters as they may be not fully resolved at this time
            ExpressionTypingUtils.checkVariableShadowing(parameterScope, trace, konstueParameterDescriptor)

            parameterScope.addVariableDescriptor(konstueParameterDescriptor)
            result.add(konstueParameterDescriptor)
        }
        return result
    }

    private data class ContextReceiverTypeWithLabel(konst type: KotlinType, konst label: Name?)
}
