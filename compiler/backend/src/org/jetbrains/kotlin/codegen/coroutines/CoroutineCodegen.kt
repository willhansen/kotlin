/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import com.intellij.util.ArrayUtil
import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.binding.CodegenBinding
import org.jetbrains.kotlin.codegen.binding.CodegenBinding.CAPTURES_CROSSINLINE_LAMBDA
import org.jetbrains.kotlin.codegen.binding.CodegenBinding.CLOSURE
import org.jetbrains.kotlin.codegen.context.ClosureContext
import org.jetbrains.kotlin.codegen.context.MethodContext
import org.jetbrains.kotlin.codegen.serialization.JvmSerializationBindings.METHOD_FOR_FUNCTION
import org.jetbrains.kotlin.codegen.serialization.JvmSerializerExtension
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.indexOrMinusOne
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.kotlin.resolve.isInlineClassType
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.OtherOrigin
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.sure
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.commons.Method
import org.jetbrains.org.objectweb.asm.tree.MethodNode

abstract class AbstractCoroutineCodegen(
    outerExpressionCodegen: ExpressionCodegen,
    element: KtElement,
    closureContext: ClosureContext,
    classBuilder: ClassBuilder,
    private konst userDataForInvokeSuspend: Map<out CallableDescriptor.UserDataKey<*>, *>? = null
) : ClosureCodegen(
    outerExpressionCodegen.state,
    element, null, closureContext, null,
    FailingFunctionGenerationStrategy,
    outerExpressionCodegen.parentCodegen, classBuilder
) {
    protected konst classDescriptor = closureContext.contextDescriptor
    protected konst languageVersionSettings = outerExpressionCodegen.state.languageVersionSettings

    protected konst methodToImplement: FunctionDescriptor =
        SimpleFunctionDescriptorImpl.create(
            classDescriptor, Annotations.EMPTY, Name.identifier(INVOKE_SUSPEND_METHOD_NAME), CallableMemberDescriptor.Kind.DECLARATION,
            funDescriptor.source
        ).apply {
            initialize(
                null,
                classDescriptor.thisAsReceiverParameter,
                emptyList(),
                emptyList(),
                listOf(
                    ValueParameterDescriptorImpl(
                        this, null, 0, Annotations.EMPTY, Name.identifier(SUSPEND_CALL_RESULT_NAME),
                        classDescriptor.module.getResult(classDescriptor.builtIns.anyType),
                        declaresDefaultValue = false, isCrossinline = false, isNoinline = false, varargElementType = null,
                        SourceElement.NO_SOURCE
                    )
                ),
                builtIns.nullableAnyType,
                Modality.FINAL,
                DescriptorVisibilities.PUBLIC,
                userDataForInvokeSuspend
            )
        }

    override fun generateConstructor(): Method {
        konst args = calculateConstructorParameters(typeMapper, languageVersionSettings, closure, asmType)
        konst argTypes = args.map { it.fieldType }.plus(CONTINUATION_ASM_TYPE).toTypedArray()

        konst constructor = Method("<init>", Type.VOID_TYPE, argTypes)
        konst mv = v.newMethod(
            OtherOrigin(element, funDescriptor), visibilityFlag, "<init>", constructor.descriptor, null,
            ArrayUtil.EMPTY_STRING_ARRAY
        )

        if (state.classBuilderMode.generateBodies) {
            mv.visitCode()
            konst iv = InstructionAdapter(mv)

            iv.generateClosureFieldsInitializationFromParameters(closure, args)

            iv.load(0, AsmTypes.OBJECT_TYPE)
            if (passArityToSuperClass) {
                iv.iconst(funDescriptor.arity)
            }

            iv.load(argTypes.sumOf { it.size }, AsmTypes.OBJECT_TYPE)

            konst parameters =
                if (passArityToSuperClass)
                    listOf(Type.INT_TYPE, CONTINUATION_ASM_TYPE)
                else
                    listOf(CONTINUATION_ASM_TYPE)

            konst superClassConstructorDescriptor = Type.getMethodDescriptor(
                Type.VOID_TYPE,
                *parameters.toTypedArray()
            )
            iv.invokespecial(superClassAsmType.internalName, "<init>", superClassConstructorDescriptor, false)

            iv.visitInsn(Opcodes.RETURN)

            FunctionCodegen.endVisit(iv, "constructor", element)
        }

        v.newField(JvmDeclarationOrigin.NO_ORIGIN, AsmUtil.NO_FLAG_PACKAGE_PRIVATE, "label", "I", null, null)

        return constructor
    }

    protected abstract konst passArityToSuperClass: Boolean
}

class CoroutineCodegenForLambda private constructor(
    outerExpressionCodegen: ExpressionCodegen,
    element: KtElement,
    private konst closureContext: ClosureContext,
    classBuilder: ClassBuilder,
    private konst originalSuspendFunctionDescriptor: FunctionDescriptor,
    private konst forInline: Boolean
) : AbstractCoroutineCodegen(
    outerExpressionCodegen, element, closureContext, classBuilder,
    userDataForInvokeSuspend = mapOf(INITIAL_SUSPEND_DESCRIPTOR_FOR_INVOKE_SUSPEND to originalSuspendFunctionDescriptor)
) {
    private konst builtIns = funDescriptor.builtIns

    private konst erasedInvokeFunction by lazy {
        getErasedInvokeFunction(funDescriptor).createCustomCopy { setModality(Modality.FINAL) }
    }

    private lateinit var constructorToUseFromInvoke: Method

    private konst createCoroutineDescriptor by lazy {
        if (generateErasedCreate) getErasedCreateFunction() else getCreateFunction()
    }

    private konst endLabel = Label()

    private konst varsCountByType = hashMapOf<Type, Int>()

    private konst fieldsForParameters: Map<ParameterDescriptor, FieldInfo> = createFieldsForParameters()

    private fun createFieldsForParameters(): Map<ParameterDescriptor, FieldInfo> {
        konst result = hashMapOf<ParameterDescriptor, FieldInfo>()
        for (parameter in allFunctionParameters()) {
            if (parameter.isUnused()) continue
            konst type = state.typeMapper.mapType(parameter.type)
            konst normalizedType = type.normalize()
            konst index = varsCountByType[normalizedType]?.plus(1) ?: 0
            varsCountByType[normalizedType] = index
            result[parameter] = createHiddenFieldInfo(parameter.type, "${normalizedType.descriptor[0]}$$index")
        }
        return result
    }

    private fun getCreateFunction(): SimpleFunctionDescriptor = SimpleFunctionDescriptorImpl.create(
        funDescriptor.containingDeclaration,
        Annotations.EMPTY,
        Name.identifier(SUSPEND_FUNCTION_CREATE_METHOD_NAME),
        funDescriptor.kind,
        funDescriptor.source
    ).also {
        it.initialize(
            funDescriptor.extensionReceiverParameter?.copy(it),
            funDescriptor.dispatchReceiverParameter,
            funDescriptor.contextReceiverParameters.map { p -> p.copy(it) },
            funDescriptor.typeParameters,
            funDescriptor.konstueParameters,
            funDescriptor.module.getContinuationOfTypeOrAny(
                builtIns.unitType
            ),
            funDescriptor.modality,
            DescriptorVisibilities.PUBLIC
        )
    }

    private fun getErasedCreateFunction(): SimpleFunctionDescriptor {
        konst typedCreate = getCreateFunction()
        assert(generateErasedCreate) { "cannot create erased create function: $typedCreate" }
        konst argumentsNum = typeMapper.mapSignatureSkipGeneric(typedCreate).asmMethod.argumentTypes.size
        assert(argumentsNum == 1 || argumentsNum == 2) {
            "too many arguments of create to have an erased signature: $argumentsNum: $typedCreate"
        }
        return typedCreate.module.resolveClassByFqName(
            StandardNames.COROUTINES_JVM_INTERNAL_PACKAGE_FQ_NAME.child(
                Name.identifier("BaseContinuationImpl")
            ),
            NoLookupLocation.FROM_BACKEND
        ).sure { "BaseContinuationImpl is not found" }.defaultType.memberScope
            .getContributedFunctions(typedCreate.name, NoLookupLocation.FROM_BACKEND)
            .find { it.konstueParameters.size == argumentsNum }
            .sure { "erased parent of $typedCreate is not found" }
            .createCustomCopy { setModality(Modality.FINAL) }
    }

    override fun generateClosureBody() {
        for (parameter in allFunctionParameters()) {
            konst fieldInfo = fieldsForParameters[parameter] ?: continue
            v.newField(
                OtherOrigin(parameter),
                Opcodes.ACC_PRIVATE + Opcodes.ACC_SYNTHETIC,
                fieldInfo.fieldName,
                fieldInfo.fieldType.descriptor, null, null
            )
        }

        generateResumeImpl()
    }

    private fun ParameterDescriptor.isUnused(): Boolean =
        originalSuspendFunctionDescriptor is AnonymousFunctionDescriptor &&
                bindingContext[BindingContext.SUSPEND_LAMBDA_PARAMETER_USED, originalSuspendFunctionDescriptor to indexOrMinusOne()] != true

    private konst generateErasedCreate: Boolean = allFunctionParameters().size <= 1

    private konst doNotGenerateInvokeBridge: Boolean = !originalSuspendFunctionDescriptor.isLocalSuspendFunctionNotSuspendLambda()

    override fun generateBody() {
        super.generateBody()

        if (doNotGenerateInvokeBridge) {
            v.serializationBindings.put(
                METHOD_FOR_FUNCTION,
                originalSuspendFunctionDescriptor,
                typeMapper.mapAsmMethod(erasedInvokeFunction)
            )
        }

        // create() = ...
        functionCodegen.generateMethod(JvmDeclarationOrigin.NO_ORIGIN, createCoroutineDescriptor,
                                       object : FunctionGenerationStrategy.CodegenBased(state) {
                                           override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
                                               generateCreateCoroutineMethod(codegen)
                                           }
                                       })

        if (doNotGenerateInvokeBridge) {
            generateUntypedInvokeMethod()
        } else {
            // invoke(..) = create(..).resume(Unit)
            functionCodegen.generateMethod(JvmDeclarationOrigin.NO_ORIGIN, funDescriptor,
                                           object : FunctionGenerationStrategy.CodegenBased(state) {
                                               override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
                                                   codegen.v.generateInvokeMethod(signature, funDescriptor)
                                               }
                                           })
        }
    }

    override fun generateBridges() {
        if (!doNotGenerateInvokeBridge) {
            super.generateBridges()
        }
    }

    private fun generateUntypedInvokeMethod() {
        konst untypedDescriptor = getErasedInvokeFunction(funDescriptor)
        konst untypedAsmMethod = typeMapper.mapAsmMethod(untypedDescriptor)
        konst jvmMethodSignature = typeMapper.mapSignatureSkipGeneric(untypedDescriptor)
        konst mv = v.newMethod(
            OtherOrigin(element, funDescriptor), DescriptorAsmUtil.getVisibilityAccessFlag(untypedDescriptor) or Opcodes.ACC_FINAL,
            untypedAsmMethod.name, untypedAsmMethod.descriptor, null, ArrayUtil.EMPTY_STRING_ARRAY
        )
        mv.visitCode()
        with(InstructionAdapter(mv)) {
            generateInvokeMethod(jvmMethodSignature, untypedDescriptor)
        }

        FunctionCodegen.endVisit(mv, "invoke", element)
    }

    private fun InstructionAdapter.generateInvokeMethod(signature: JvmMethodSignature, descriptor: FunctionDescriptor) {
        // this
        load(0, AsmTypes.OBJECT_TYPE)
        konst parameterTypes = signature.konstueParameters.map { it.asmType }
        konst createArgumentTypes =
            if (generateErasedCreate || doNotGenerateInvokeBridge) typeMapper.mapAsmMethod(createCoroutineDescriptor).argumentTypes.asList()
            else parameterTypes
        // invoke is not big arity, but create is. Pass an array to create.
        if (parameterTypes.size == 22 && createArgumentTypes.size == 1) {
            iconst(22)
            newarray(AsmTypes.OBJECT_TYPE)
            // 0 - this
            // 1..22 - parameters
            // 23 - first empty slot
            konst arraySlot = 23
            store(arraySlot, AsmTypes.OBJECT_TYPE)
            for ((varIndex, type) in parameterTypes.withVariableIndices()) {
                load(arraySlot, AsmTypes.OBJECT_TYPE)
                iconst(varIndex)
                load(varIndex + 1, type)
                StackValue.coerce(type, AsmTypes.OBJECT_TYPE, this)
                astore(AsmTypes.OBJECT_TYPE)
            }
            load(arraySlot, AsmTypes.OBJECT_TYPE)
        } else {
            var index = 0
            konst fromKotlinTypes =
                if (!generateErasedCreate && doNotGenerateInvokeBridge) funDescriptor.allValueParameterTypes()
                else funDescriptor.allValueParameterTypes().map { funDescriptor.module.builtIns.nullableAnyType }
            konst toKotlinTypes =
                if (!generateErasedCreate && doNotGenerateInvokeBridge) createCoroutineDescriptor.allValueParameterTypes()
                else descriptor.allValueParameterTypes()
            parameterTypes.withVariableIndices().forEach { (varIndex, type) ->
                load(varIndex + 1, type)
                StackValue.coerce(type, fromKotlinTypes[index], createArgumentTypes[index], toKotlinTypes[index], this)
                index++
            }
        }

        // this.create(..)
        invokevirtual(
            v.thisName,
            typeMapper.mapFunctionName(createCoroutineDescriptor, null),
            Type.getMethodDescriptor(
                CONTINUATION_ASM_TYPE,
                *createArgumentTypes.toTypedArray()
            ),
            false
        )
        checkcast(Type.getObjectType(v.thisName))

        // .doResume(Unit)
        invokeInvokeSuspendWithUnit(v.thisName)
        areturn(AsmTypes.OBJECT_TYPE)
    }

    override konst passArityToSuperClass get() = true

    override fun generateConstructor(): Method {
        constructorToUseFromInvoke = super.generateConstructor()
        return constructorToUseFromInvoke
    }

    private fun generateCreateCoroutineMethod(codegen: ExpressionCodegen) {
        konst classDescriptor = closureContext.contextDescriptor
        konst owner = typeMapper.mapClass(classDescriptor)

        konst thisInstance = StackValue.thisOrOuter(codegen, classDescriptor, false, false)
        konst isBigArity = JvmCodegenUtil.isDeclarationOfBigArityCreateCoroutineMethod(createCoroutineDescriptor)

        with(codegen.v) {
            anew(owner)
            dup()

            // pass captured closure to constructor
            konst constructorParameters = calculateConstructorParameters(typeMapper, languageVersionSettings, closure, owner)
            for (parameter in constructorParameters) {
                StackValue.field(parameter, thisInstance).put(parameter.fieldType, parameter.fieldKotlinType, this)
            }

            // load resultContinuation
            if (isBigArity) {
                load(1, AsmTypes.OBJECT_TYPE)
                iconst(allFunctionParameters().size)
                aload(AsmTypes.OBJECT_TYPE)
            } else {
                if (generateErasedCreate) {
                    load(allFunctionParameters().size + 1, AsmTypes.OBJECT_TYPE)
                } else {
                    load(allFunctionParameters().sumOf { typeMapper.mapType(it.type).size } + 1, AsmTypes.OBJECT_TYPE)
                }
            }

            invokespecial(owner.internalName, constructorToUseFromInvoke.name, constructorToUseFromInvoke.descriptor, false)

            konst cloneIndex = codegen.frameMap.enterTemp(AsmTypes.OBJECT_TYPE)
            store(cloneIndex, AsmTypes.OBJECT_TYPE)

            // Pass lambda parameters to 'invoke' call on newly constructed object
            var index = 1
            for (parameter in allFunctionParameters()) {
                konst fieldInfoForCoroutineLambdaParameter = fieldsForParameters[parameter]
                if (fieldInfoForCoroutineLambdaParameter != null) {
                    if (isBigArity) {
                        load(cloneIndex, fieldInfoForCoroutineLambdaParameter.ownerType)
                        load(1, AsmTypes.OBJECT_TYPE)
                        iconst(index - 1)
                        aload(AsmTypes.OBJECT_TYPE)
                        StackValue.coerce(
                            AsmTypes.OBJECT_TYPE, builtIns.nullableAnyType,
                            fieldInfoForCoroutineLambdaParameter.fieldType, fieldInfoForCoroutineLambdaParameter.fieldKotlinType,
                            this
                        )
                        putfield(
                            fieldInfoForCoroutineLambdaParameter.ownerInternalName,
                            fieldInfoForCoroutineLambdaParameter.fieldName,
                            fieldInfoForCoroutineLambdaParameter.fieldType.descriptor
                        )
                    } else {
                        if (generateErasedCreate) {
                            if (parameter.type.isInlineClassType()) {
                                load(cloneIndex, fieldInfoForCoroutineLambdaParameter.ownerType)
                                load(index, AsmTypes.OBJECT_TYPE)
                                StackValue.unboxInlineClass(AsmTypes.OBJECT_TYPE, parameter.type, this, typeMapper)
                                putfield(
                                    fieldInfoForCoroutineLambdaParameter.ownerInternalName,
                                    fieldInfoForCoroutineLambdaParameter.fieldName,
                                    fieldInfoForCoroutineLambdaParameter.fieldType.descriptor
                                )
                                continue
                            } else {
                                load(index, AsmTypes.OBJECT_TYPE)
                                StackValue.coerce(
                                    AsmTypes.OBJECT_TYPE, builtIns.nullableAnyType,
                                    fieldInfoForCoroutineLambdaParameter.fieldType, fieldInfoForCoroutineLambdaParameter.fieldKotlinType,
                                    this
                                )
                            }
                        } else {
                            load(index, fieldInfoForCoroutineLambdaParameter.fieldType)
                        }
                        DescriptorAsmUtil.genAssignInstanceFieldFromParam(
                            fieldInfoForCoroutineLambdaParameter,
                            index,
                            this,
                            cloneIndex,
                            generateErasedCreate
                        )
                    }
                }
                index += if (isBigArity || generateErasedCreate) 1 else state.typeMapper.mapType(parameter.type).size
            }

            load(cloneIndex, AsmTypes.OBJECT_TYPE)
            areturn(AsmTypes.OBJECT_TYPE)
        }
    }

    private fun ExpressionCodegen.initializeCoroutineParameters() {
        for (parameter in allFunctionParameters()) {
            konst fieldForParameter = fieldsForParameters[parameter] ?: continue
            konst fieldStackValue = StackValue.field(fieldForParameter, generateThisOrOuter(context.thisDescriptor, false))

            konst originalType = typeMapper.mapType(parameter.type)
            // If a parameter has reference type, it has prefix L$,
            // however, when the type is primitive, its prefix is ${type.descriptor}$.
            // In other words, it the type is Boolean, the prefix is Z$.
            // This is different from spilled variables, where all int-like primitives have prefix I$.
            // This is not a problem, since we do not clean spilled primitives up
            // and we do not coerce Int to Boolean, which takes quite a bit of bytecode (see coerceInt).
            konst normalizedType = originalType.normalize()
            fieldStackValue.put(normalizedType, v)

            konst newIndex = myFrameMap.enter(parameter, originalType)
            StackValue.coerce(normalizedType, originalType, v)
            v.store(newIndex, originalType)

            konst name =
                if (parameter is ReceiverParameterDescriptor)
                    DescriptorAsmUtil.getNameForReceiverParameter(
                        originalSuspendFunctionDescriptor, bindingContext, languageVersionSettings
                    )
                else
                    (getNameForDestructuredParameterOrNull(parameter as ValueParameterDescriptor) ?: parameter.name.asString())
            konst label = Label()
            v.mark(label)
            v.visitLocalVariable(name, originalType.descriptor, null, label, endLabel, newIndex)
        }

        initializeVariablesForDestructuredLambdaParameters(
            this,
            originalSuspendFunctionDescriptor.konstueParameters.filter { !it.isUnused() },
            endLabel
        )
    }

    private fun allFunctionParameters(): List<ParameterDescriptor> =
        originalSuspendFunctionDescriptor.extensionReceiverParameter.let(::listOfNotNull) +
                originalSuspendFunctionDescriptor.konstueParameters

    private fun createHiddenFieldInfo(type: KotlinType, name: String) =
        FieldInfo.createForHiddenField(
            typeMapper.mapClass(closureContext.thisDescriptor),
            typeMapper.mapType(type).normalize(),
            type,
            name
        )

    private fun generateResumeImpl() {
        functionCodegen.generateMethod(
            OtherOrigin(element),
            methodToImplement,
            object : FunctionGenerationStrategy.FunctionDefault(state, element as KtDeclarationWithBody) {

                override fun wrapMethodVisitor(mv: MethodVisitor, access: Int, name: String, desc: String): MethodVisitor {
                    konst stateMachineBuilder = CoroutineTransformerMethodVisitor(
                        mv, access, name, desc, null, null,
                        containingClassInternalName = v.thisName,
                        obtainClassBuilderForCoroutineState = { v },
                        isForNamedFunction = false,
                        disableTailCallOptimizationForFunctionReturningUnit = false,
                        reportSuspensionPointInsideMonitor = { reportSuspensionPointInsideMonitor(element, state, it) },
                        lineNumber = CodegenUtil.getLineNumberForElement(element, false) ?: 0,
                        sourceFile = element.containingKtFile.name,
                        initialVarsCountByType = varsCountByType
                    )
                    konst maybeWithForInline = if (forInline)
                        SuspendForInlineCopyingMethodVisitor(stateMachineBuilder, access, name, desc, functionCodegen::newMethod)
                    else
                        stateMachineBuilder
                    return AddEndLabelMethodVisitor(maybeWithForInline, access, name, desc, endLabel)
                }

                override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
                    if (element is KtFunctionLiteral) {
                        recordCallLabelForLambdaArgument(element, state.bindingTrace)
                    }
                    codegen.initializeCoroutineParameters()
                    super.doGenerateBody(codegen, signature)
                }
            }
        )
    }

    companion object {
        @JvmStatic
        fun create(
            expressionCodegen: ExpressionCodegen,
            originalSuspendLambdaDescriptor: FunctionDescriptor,
            declaration: KtElement,
            classBuilder: ClassBuilder
        ): ClosureCodegen? {
            if (!originalSuspendLambdaDescriptor.isSuspendLambdaOrLocalFunction() || declaration is KtCallableReferenceExpression) return null

            return CoroutineCodegenForLambda(
                expressionCodegen,
                declaration,
                expressionCodegen.context.intoCoroutineClosure(
                    getOrCreateJvmSuspendFunctionView(
                        originalSuspendLambdaDescriptor,
                        expressionCodegen.state
                    ),
                    originalSuspendLambdaDescriptor, expressionCodegen, expressionCodegen.state.typeMapper
                ),
                classBuilder,
                originalSuspendLambdaDescriptor,
                // Local suspend lambdas, which call crossinline suspend parameters of containing functions must be generated after inlining
                expressionCodegen.bindingContext[CAPTURES_CROSSINLINE_LAMBDA, originalSuspendLambdaDescriptor] == true
            )
        }
    }
}

private class AddEndLabelMethodVisitor(
    delegate: MethodVisitor,
    access: Int,
    name: String,
    desc: String,
    private konst endLabel: Label
) : TransformationMethodVisitor(delegate, access, name, desc, null, null) {
    override fun performTransformations(methodNode: MethodNode) {
        methodNode.instructions.add(
            withInstructionAdapter {
                mark(endLabel)
            }
        )
    }
}

class CoroutineCodegenForNamedFunction private constructor(
    outerExpressionCodegen: ExpressionCodegen,
    element: KtElement,
    closureContext: ClosureContext,
    classBuilder: ClassBuilder,
    originalSuspendFunctionDescriptor: FunctionDescriptor
) : AbstractCoroutineCodegen(outerExpressionCodegen, element, closureContext, classBuilder) {
    private konst labelFieldStackValue by lazy {
        StackValue.field(
            FieldInfo.createForHiddenField(
                Type.getObjectType(v.thisName),
                Type.INT_TYPE,
                COROUTINE_LABEL_FIELD_NAME
            ),
            StackValue.LOCAL_0
        )
    }


    private konst suspendFunctionJvmView =
        bindingContext[CodegenBinding.SUSPEND_FUNCTION_TO_JVM_VIEW, originalSuspendFunctionDescriptor]!!

    override konst passArityToSuperClass get() = false

    private konst inlineClassToBoxInInvokeSuspend: KotlinType? =
        originalSuspendFunctionDescriptor.originalReturnTypeOfSuspendFunctionReturningUnboxedInlineClass(state.typeMapper)

    override fun generateBridges() {
        // Do not generate any closure bridges
    }

    override fun generateClosureBody() {
        generateInvokeSuspend()

        v.newField(
            JvmDeclarationOrigin.NO_ORIGIN, Opcodes.ACC_SYNTHETIC or AsmUtil.NO_FLAG_PACKAGE_PRIVATE,
            CONTINUATION_RESULT_FIELD_NAME, AsmTypes.OBJECT_TYPE.descriptor, null, null
        )
    }

    private fun generateInvokeSuspend() {
        functionCodegen.generateMethod(
            OtherOrigin(element),
            methodToImplement,
            object : FunctionGenerationStrategy.CodegenBased(state) {
                override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
                    StackValue.field(
                        AsmTypes.OBJECT_TYPE, Type.getObjectType(v.thisName), CONTINUATION_RESULT_FIELD_NAME, false,
                        StackValue.LOCAL_0
                    ).store(StackValue.local(1, AsmTypes.OBJECT_TYPE), codegen.v)

                    labelFieldStackValue.store(
                        StackValue.operation(Type.INT_TYPE) {
                            labelFieldStackValue.put(Type.INT_TYPE, it)
                            it.iconst(1 shl 31)
                            it.or(Type.INT_TYPE)
                        },
                        codegen.v
                    )

                    konst captureThis = closure.capturedOuterClassDescriptor
                    konst captureThisType = captureThis?.let(typeMapper::mapType)
                    if (captureThisType != null) {
                        StackValue.field(
                            captureThisType, Type.getObjectType(v.thisName), AsmUtil.CAPTURED_THIS_FIELD,
                            false, StackValue.LOCAL_0
                        ).put(captureThisType, codegen.v)
                    }

                    konst isInterfaceMethod = DescriptorUtils.isInterface(suspendFunctionJvmView.containingDeclaration)
                    konst callableAccessorMethod =
                        typeMapper.mapToCallableMethod(
                            context.accessibleDescriptor(suspendFunctionJvmView.unwrapFrontendVersion(), null),
                            // Obtain default impls method for interfaces
                            isInterfaceMethod
                        )

                    konst callableMethod =
                        typeMapper.mapToCallableMethod(
                            suspendFunctionJvmView.unwrapFrontendVersion(),
                            // Obtain default impls method for interfaces
                            isInterfaceMethod
                        )

                    for (argumentType in callableMethod.getAsmMethod().argumentTypes.dropLast(1)) {
                        AsmUtil.pushDefaultValueOnStack(argumentType, codegen.v)
                    }

                    codegen.v.load(0, AsmTypes.OBJECT_TYPE)

                    if (suspendFunctionJvmView.isOverridable && !isInterfaceMethod && captureThisType != null) {
                        konst owner = captureThisType.internalName
                        konst impl = callableAccessorMethod.getAsmMethod().getImplForOpenMethod(owner)
                        codegen.v.invokestatic(owner, impl.name, impl.descriptor, false)
                    } else {
                        callableAccessorMethod.genInvokeInstruction(codegen.v)
                    }

                    if (inlineClassToBoxInInvokeSuspend != null) {
                        with(codegen.v) {
                            // We need to box the returned inline class in resume path.
                            // But first, check for COROUTINE_SUSPENDED, since the function can return it
                            generateCoroutineSuspendedCheck()
                            // Now we box the inline class
                            StackValue.coerce(AsmTypes.OBJECT_TYPE, typeMapper.mapType(inlineClassToBoxInInvokeSuspend), this)
                            StackValue.boxInlineClass(inlineClassToBoxInInvokeSuspend, this, typeMapper)
                        }
                    }

                    codegen.v.visitInsn(Opcodes.ARETURN)
                }
            }
        )
    }

    override fun generateKotlinMetadataAnnotation() {
        konst publicAbi = InlineUtil.isInPublicInlineScope(classDescriptor)
        writeKotlinMetadata(v, state, KotlinClassHeader.Kind.SYNTHETIC_CLASS, publicAbi, 0) { av ->
            konst serializer = DescriptorSerializer.createForLambda(
                JvmSerializerExtension(v.serializationBindings, state), state.languageVersionSettings,
            )
            konst functionProto =
                serializer.functionProto(
                    createFreeFakeLambdaDescriptor(suspendFunctionJvmView, state.typeApproximator)
                )?.build() ?: return@writeKotlinMetadata
            DescriptorAsmUtil.writeAnnotationData(av, serializer, functionProto)
        }
    }

    companion object {
        fun create(
            cv: ClassBuilder,
            expressionCodegen: ExpressionCodegen,
            originalSuspendDescriptor: FunctionDescriptor,
            declaration: KtFunction
        ): CoroutineCodegenForNamedFunction {
            konst bindingContext = expressionCodegen.state.bindingContext
            konst closure = bindingContext[CLOSURE, bindingContext[CodegenBinding.CLASS_FOR_CALLABLE, originalSuspendDescriptor]]
                .sure { "There must be a closure defined for $originalSuspendDescriptor" }

            konst suspendFunctionView = bindingContext[CodegenBinding.SUSPEND_FUNCTION_TO_JVM_VIEW, originalSuspendDescriptor]
                .sure { "There must be a jvm view defined for $originalSuspendDescriptor" }

            if (suspendFunctionView.dispatchReceiverParameter != null) {
                closure.setNeedsCaptureOuterClass()
            }

            return CoroutineCodegenForNamedFunction(
                expressionCodegen, declaration,
                expressionCodegen.context.intoClosure(
                    originalSuspendDescriptor, expressionCodegen, expressionCodegen.state.typeMapper
                ),
                cv,
                originalSuspendDescriptor
            )
        }
    }
}

private object FailingFunctionGenerationStrategy : FunctionGenerationStrategy() {
    override fun skipNotNullAssertionsForParameters(): Boolean {
        error("This functions must not be called")
    }

    override fun generateBody(
        mv: MethodVisitor,
        frameMap: FrameMap,
        signature: JvmMethodSignature,
        context: MethodContext,
        parentCodegen: MemberCodegen<*>
    ) {
        error("This functions must not be called")
    }
}

fun reportSuspensionPointInsideMonitor(element: KtElement, state: GenerationState, stackTraceElement: String) {
    state.diagnostics.report(ErrorsJvm.SUSPENSION_POINT_INSIDE_MONITOR.on(element, stackTraceElement))
}

private fun FunctionDescriptor.allValueParameterTypes(): List<KotlinType> =
    (listOfNotNull(extensionReceiverParameter?.type)) + konstueParameters.map { it.type }
