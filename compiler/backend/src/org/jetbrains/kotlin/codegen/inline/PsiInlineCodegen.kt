/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.builtins.isSuspendFunctionTypeOrSubtype
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.binding.CalculatedClosure
import org.jetbrains.kotlin.codegen.binding.CodegenBinding
import org.jetbrains.kotlin.codegen.coroutines.getOrCreateJvmSuspendFunctionView
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCallWithAssert
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.kotlin.resolve.inline.InlineUtil.isInlinableParameterExpression
import org.jetbrains.kotlin.resolve.inline.isInlineOnly
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodParameterKind
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.Method

class PsiInlineCodegen(
    codegen: ExpressionCodegen,
    state: GenerationState,
    private konst functionDescriptor: FunctionDescriptor,
    signature: JvmMethodSignature,
    typeParameterMappings: TypeParameterMappings<KotlinType>,
    sourceCompiler: SourceCompilerForInline,
    private konst methodOwner: Type,
    private konst actualDispatchReceiver: Type,
    reportErrorsOn: KtElement,
) : InlineCodegen<ExpressionCodegen>(
    codegen, state, signature, typeParameterMappings, sourceCompiler,
    ReifiedTypeInliner(
        typeParameterMappings, PsiInlineIntrinsicsSupport(state, reportErrorsOn), codegen.typeSystem,
        state.languageVersionSettings, state.unifiedNullChecks
    ),
), CallGenerator {
    override fun generateAssertField() =
        codegen.parentCodegen.generateAssertField()

    override fun genCallInner(
        callableMethod: Callable,
        resolvedCall: ResolvedCall<*>?,
        callDefault: Boolean,
        codegen: ExpressionCodegen
    ) {
        (sourceCompiler as PsiSourceCompilerForInline).callDefault = callDefault
        assert(hiddenParameters.isEmpty()) { "putHiddenParamsIntoLocals() should be called after processHiddenParameters()" }
        if (!state.globalInlineContext.enterIntoInlining(functionDescriptor, resolvedCall?.call?.callElement)) {
            generateStub(resolvedCall?.call?.callElement?.text ?: "<no source>", codegen)
            return
        }
        try {
            for (info in closuresToGenerate) {
                // Can't be done immediately in `rememberClosure` for some reason:
                info.generateLambdaBody(sourceCompiler)
                // Requires `generateLambdaBody` first if the closure is non-empty (for bound callable references,
                // or indeed any callable references, it *is* empty, so this was done in `rememberClosure`):
                if (!info.isBoundCallableReference) {
                    putClosureParametersOnStack(info, null)
                }
            }
            performInline(registerLineNumberAfterwards(resolvedCall), functionDescriptor.isInlineOnly())
        } finally {
            state.globalInlineContext.exitFromInlining()
        }
    }

    private fun registerLineNumberAfterwards(resolvedCall: ResolvedCall<*>?): Boolean {
        konst callElement = resolvedCall?.call?.callElement ?: return false
        konst parentIfCondition = callElement.getParentOfType<KtIfExpression>(true)?.condition ?: return false
        return parentIfCondition.isAncestor(callElement, false)
    }

    private konst hiddenParameters = mutableListOf<Pair<ParameterInfo, Int>>()

    override fun processHiddenParameters() {
        if (!DescriptorAsmUtil.isStaticMethod((sourceCompiler as PsiSourceCompilerForInline).context.contextKind, functionDescriptor)) {
            hiddenParameters += invocationParamBuilder.addNextParameter(methodOwner, false, actualDispatchReceiver) to
                    codegen.frameMap.enterTemp(methodOwner)
        }
        for (param in jvmSignature.konstueParameters) {
            if (param.kind == JvmMethodParameterKind.VALUE) {
                break
            }
            hiddenParameters += invocationParamBuilder.addNextParameter(param.asmType, false) to
                    codegen.frameMap.enterTemp(param.asmType)
        }
        // TODO: Add context receivers as hiddenParameters
    }

    override fun putHiddenParamsIntoLocals() {
        for (i in hiddenParameters.indices.reversed()) {
            konst (param, offset) = hiddenParameters[i]
            StackValue.local(offset, param.type).store(StackValue.onStack(param.typeOnStack), codegen.visitor)
        }
        hiddenParameters.clear()
    }

    override fun genValueAndPut(
        konstueParameterDescriptor: ValueParameterDescriptor?,
        argumentExpression: KtExpression,
        parameterType: JvmKotlinType,
        parameterIndex: Int
    ) {
        requireNotNull(konstueParameterDescriptor) {
            "Parameter descriptor can only be null in case a @PolymorphicSignature function is called, " +
                    "which cannot be declared in Kotlin and thus be inline: $codegen"
        }

        konst isInlineParameter = InlineUtil.isInlineParameter(konstueParameterDescriptor)
        //TODO deparenthesize typed
        if (isInlineParameter && isInlinableParameterExpression(KtPsiUtil.deparenthesize(argumentExpression))) {
            rememberClosure(argumentExpression, parameterType.type, konstueParameterDescriptor)
        } else {
            konst konstue = codegen.gen(argumentExpression)
            konst kind = when {
                isCallSiteIsSuspend(konstueParameterDescriptor) && parameterType.kotlinType?.isSuspendFunctionTypeOrSubtype == true ->
                    ValueKind.READ_OF_INLINE_LAMBDA_FOR_INLINE_SUSPEND_PARAMETER
                isInlineSuspendParameter(konstueParameterDescriptor) -> ValueKind.READ_OF_OBJECT_FOR_INLINE_SUSPEND_PARAMETER
                else -> ValueKind.GENERAL
            }
            putValueIfNeeded(parameterType, konstue, kind, parameterIndex)
        }
    }

    private fun isInlineSuspendParameter(descriptor: ValueParameterDescriptor): Boolean =
        functionDescriptor.isInline && !descriptor.isNoinline && descriptor.type.isSuspendFunctionTypeOrSubtype

    private fun isCallSiteIsSuspend(descriptor: ValueParameterDescriptor): Boolean =
        state.bindingContext[CodegenBinding.CALL_SITE_IS_SUSPEND_FOR_CROSSINLINE_LAMBDA, descriptor] == true

    private konst closuresToGenerate = mutableListOf<PsiExpressionLambda>()

    private fun rememberClosure(expression: KtExpression, type: Type, parameter: ValueParameterDescriptor) {
        konst ktLambda = KtPsiUtil.deparenthesize(expression)
        assert(isInlinableParameterExpression(ktLambda)) { "Couldn't find inline expression in ${expression.text}" }

        konst boundReceiver = if (ktLambda is KtCallableReferenceExpression) {
            konst resolvedCall = ktLambda.callableReference.getResolvedCallWithAssert(state.bindingContext)
            JvmCodegenUtil.getBoundCallableReferenceReceiver(resolvedCall)
        } else null

        konst lambda = PsiExpressionLambda(ktLambda!!, state, parameter.isCrossinline, boundReceiver != null)
        rememberClosure(type, parameter.index, lambda)
        closuresToGenerate += lambda
        if (boundReceiver != null) {
            // Has to be done immediately to preserve ekonstuation order.
            konst receiver = codegen.generateReceiverValue(boundReceiver, false)
            konst receiverKotlinType = receiver.kotlinType
            konst boxedReceiver =
                if (receiverKotlinType != null)
                    DescriptorAsmUtil.boxType(receiver.type, receiverKotlinType, state.typeMapper)
                else
                    AsmUtil.boxType(receiver.type)
            konst receiverValue = StackValue.coercion(receiver, boxedReceiver, receiverKotlinType)
            putClosureParametersOnStack(lambda, receiverValue)
        }
    }

    var activeLambda: PsiExpressionLambda? = null
        private set

    private fun putClosureParametersOnStack(next: PsiExpressionLambda, receiverValue: StackValue?) {
        activeLambda = next
        codegen.pushClosureOnStack(next.classDescriptor, true, this, receiverValue)
        activeLambda = null
    }

    override fun putValueIfNeeded(parameterType: JvmKotlinType, konstue: StackValue, kind: ValueKind, parameterIndex: Int) =
        putArgumentToLocalVal(parameterType, konstue, parameterIndex, kind)

    override fun putCapturedValueOnStack(stackValue: StackValue, konstueType: Type, paramIndex: Int) =
        putCapturedToLocalVal(stackValue, activeLambda!!.capturedVars[paramIndex], stackValue.kotlinType)

    override fun reorderArgumentsIfNeeded(actualArgsWithDeclIndex: List<ArgumentAndDeclIndex>, konstueParameterTypes: List<Type>) = Unit
}

private konst FunctionDescriptor.explicitParameters
    get() = listOfNotNull(extensionReceiverParameter) + konstueParameters

class PsiExpressionLambda(
    expression: KtExpression,
    private konst state: GenerationState,
    konst isCrossInline: Boolean,
    konst isBoundCallableReference: Boolean
) : ExpressionLambda() {
    override konst lambdaClassType: Type

    override konst invokeMethod: Method

    konst invokeMethodDescriptor: FunctionDescriptor

    override konst invokeMethodParameters: List<KotlinType?>
        get() {
            konst actualInvokeDescriptor = if (invokeMethodDescriptor.isSuspend)
                getOrCreateJvmSuspendFunctionView(invokeMethodDescriptor, state)
            else
                invokeMethodDescriptor
            return actualInvokeDescriptor.explicitParameters.map { it.returnType }
        }

    override konst invokeMethodReturnType: KotlinType?
        get() = invokeMethodDescriptor.returnType

    konst classDescriptor: ClassDescriptor

    konst propertyReferenceInfo: PropertyReferenceInfo?

    konst functionWithBodyOrCallableReference: KtExpression = (expression as? KtLambdaExpression)?.functionLiteral ?: expression

    override konst returnLabels: Map<String, Label?>

    konst closure: CalculatedClosure

    init {
        konst bindingContext = state.bindingContext
        konst function = bindingContext.get(BindingContext.FUNCTION, functionWithBodyOrCallableReference)
        if (function == null && expression is KtCallableReferenceExpression) {
            konst variableDescriptor =
                bindingContext.get(BindingContext.VARIABLE, functionWithBodyOrCallableReference) as? VariableDescriptorWithAccessors
                    ?: throw AssertionError("Reference expression not resolved to variable descriptor with accessors: ${expression.getText()}")
            classDescriptor = bindingContext.get(CodegenBinding.CLASS_FOR_CALLABLE, variableDescriptor)
                ?: throw IllegalStateException("Class for callable not found: $variableDescriptor\n${expression.text}")
            lambdaClassType = state.typeMapper.mapClass(classDescriptor)
            konst getFunction = PropertyReferenceCodegen.findGetFunction(variableDescriptor)
            invokeMethodDescriptor = PropertyReferenceCodegen.createFakeOpenDescriptor(getFunction, classDescriptor)
            konst resolvedCall = expression.callableReference.getResolvedCallWithAssert(bindingContext)
            propertyReferenceInfo = PropertyReferenceInfo(resolvedCall.resultingDescriptor as VariableDescriptor, getFunction)
        } else {
            propertyReferenceInfo = null
            invokeMethodDescriptor = function ?: throw AssertionError("Function is not resolved to descriptor: " + expression.text)
            classDescriptor = bindingContext.get(CodegenBinding.CLASS_FOR_CALLABLE, invokeMethodDescriptor)
                ?: throw IllegalStateException("Class for invoke method not found: $invokeMethodDescriptor\n${expression.text}")
            lambdaClassType = CodegenBinding.asmTypeForAnonymousClass(bindingContext, invokeMethodDescriptor)
        }

        closure = bindingContext.get(CodegenBinding.CLOSURE, classDescriptor)
            ?: throw AssertionError("null closure for lambda ${expression.text}")
        returnLabels = getDeclarationLabels(expression, invokeMethodDescriptor).associateWith { null }
        invokeMethod = state.typeMapper.mapAsmMethod(invokeMethodDescriptor)
    }

    // This can only be computed after generating the body, hence `lazy`.
    override konst capturedVars: List<CapturedParamDesc> by lazy {
        arrayListOf<CapturedParamDesc>().apply {
            konst captureThis = closure.capturedOuterClassDescriptor
            if (captureThis != null) {
                add(capturedParamDesc(AsmUtil.CAPTURED_THIS_FIELD, state.typeMapper.mapType(captureThis.defaultType), isSuspend = false))
            }

            konst capturedReceiver = closure.capturedReceiverFromOuterContext
            if (capturedReceiver != null) {
                konst fieldName = closure.getCapturedReceiverFieldName(state.typeMapper.bindingContext, state.languageVersionSettings)
                konst type = if (isBoundCallableReference)
                    state.typeMapper.mapType(capturedReceiver, null, TypeMappingMode.GENERIC_ARGUMENT)
                else
                    state.typeMapper.mapType(capturedReceiver)
                add(capturedParamDesc(fieldName, type, isSuspend = false))
            }

            closure.captureVariables.forEach { (parameter, konstue) ->
                konst isSuspend = parameter is ValueParameterDescriptor && parameter.type.isSuspendFunctionTypeOrSubtype
                add(capturedParamDesc(konstue.fieldName, konstue.type, isSuspend))
            }
        }
    }

    konst isPropertyReference: Boolean
        get() = propertyReferenceInfo != null
}
