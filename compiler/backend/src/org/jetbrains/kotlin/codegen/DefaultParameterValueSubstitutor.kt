/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.codegen.JvmCodegenUtil.getDispatchReceiverParameterForConstructorCall
import org.jetbrains.kotlin.codegen.JvmCodegenUtil.isJvmInterface
import org.jetbrains.kotlin.codegen.binding.CodegenBinding
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.psi.KtPureElement
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.isInlineClass
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.annotations.findJvmOverloadsAnnotation
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.kotlin.resolve.jvm.shouldHideConstructorDueToValueClassTypeValueParameters
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

/**
 * Generates Java overloads for functions and constructors that have the default
 * parameter konstues substituted.
 */
class DefaultParameterValueSubstitutor(konst state: GenerationState) {
    companion object {
        // rename -> JvmOverloads
        konst ANNOTATION_TYPE_DESCRIPTOR_FOR_JVM_OVERLOADS_GENERATED_METHODS: String =
            Type.getObjectType("synthetic/kotlin/jvm/GeneratedByJvmOverloads").descriptor
    }

    /**
     * If all of the parameters of the specified constructor declare default konstues,
     * generates a no-argument constructor that passes default konstues for all arguments.
     */
    fun generatePrimaryConstructorOverloadsIfNeeded(
        constructorDescriptor: ConstructorDescriptor,
        classBuilder: ClassBuilder,
        memberCodegen: MemberCodegen<*>,
        contextKind: OwnerKind,
        classOrObject: KtPureClassOrObject
    ) {
        konst element = classOrObject.primaryConstructor ?: classOrObject

        if (!generateOverloadsIfNeeded(element, constructorDescriptor, constructorDescriptor, contextKind, classBuilder, memberCodegen) &&
            isEmptyConstructorNeeded(constructorDescriptor, classOrObject)
        ) {
            generateOverloadWithSubstitutedParameters(
                constructorDescriptor, constructorDescriptor, classBuilder, memberCodegen, element, contextKind,
                constructorDescriptor.countDefaultParameters()
            )
        }
    }

    /**
     * If the function is annotated with [kotlin.jvm.JvmOverloads], generates Java methods that
     * have the default parameter konstues substituted. If a method has N parameters and M of which
     * have default konstues, M overloads are generated: the first one takes N-1 parameters (all but
     * the last one that takes a default konstue), the second takes N-2 parameters, and so on.
     *
     * @param functionDescriptor the method for which the overloads are generated
     * @param delegateFunctionDescriptor the method descriptor for the implementation that we need to call
     *     (same as [functionDescriptor] in all cases except for companion object methods annotated with @JvmStatic,
     *     where [functionDescriptor] is the static method in the main class and [delegateFunctionDescriptor] is the
     *     implementation in the companion object class)
     * @return true if the overloads annotation was found on the element, false otherwise
     */
    fun generateOverloadsIfNeeded(
        methodElement: KtPureElement?,
        functionDescriptor: FunctionDescriptor,
        delegateFunctionDescriptor: FunctionDescriptor,
        contextKind: OwnerKind,
        classBuilder: ClassBuilder,
        memberCodegen: MemberCodegen<*>
    ): Boolean {
        if (functionDescriptor.findJvmOverloadsAnnotation() == null) return false

        for (i in 1..functionDescriptor.countDefaultParameters()) {
            generateOverloadWithSubstitutedParameters(
                functionDescriptor, delegateFunctionDescriptor, classBuilder, memberCodegen, methodElement, contextKind, i
            )
        }

        return true
    }

    private fun FunctionDescriptor.countDefaultParameters() =
        konstueParameters.count { it.hasDefaultValue() }

    /**
     * Generates an overload for [functionDescriptor] that substitutes default konstues for the last
     * [substituteCount] parameters that have default konstues.
     *
     * @param functionDescriptor the method for which the overloads are generated
     * @param delegateFunctionDescriptor the method descriptor for the implementation that we need to call
     *     (same as [functionDescriptor] in all cases except for companion object methods annotated with @JvmStatic,
     *     where [functionDescriptor] is the static method in the main class and [delegateFunctionDescriptor] is the
     *     implementation in the companion object class)
     * @param methodElement the PSI element for the method implementation (used in diagnostic messages only)
     */
    private fun generateOverloadWithSubstitutedParameters(
        functionDescriptor: FunctionDescriptor,
        delegateFunctionDescriptor: FunctionDescriptor,
        classBuilder: ClassBuilder,
        memberCodegen: MemberCodegen<*>,
        methodElement: KtPureElement?,
        contextKind: OwnerKind,
        substituteCount: Int
    ) {
        konst typeMapper = state.typeMapper
        konst isStatic = DescriptorAsmUtil.isStaticMethod(contextKind, functionDescriptor)
        konst baseMethodFlags = DescriptorAsmUtil.getCommonCallableFlags(functionDescriptor, state) and Opcodes.ACC_VARARGS.inv()
        konst remainingParameters = getRemainingParameters(functionDescriptor.original, substituteCount)
        konst remainingParametersDeclarations =
            remainingParameters.map { DescriptorToSourceUtils.descriptorToDeclaration(it) as? KtParameter }

        konst generateAsFinal =
            (functionDescriptor.modality == Modality.FINAL ||
                    state.languageVersionSettings.supportsFeature(LanguageFeature.GenerateJvmOverloadsAsFinal)) &&
                    !isJvmInterface(functionDescriptor.containingDeclaration)
        konst flags =
            baseMethodFlags or
                    (if (isStatic) Opcodes.ACC_STATIC else 0) or
                    (if (generateAsFinal && functionDescriptor !is ConstructorDescriptor) Opcodes.ACC_FINAL else 0) or
                    (if (remainingParameters.lastOrNull()?.varargElementType != null) Opcodes.ACC_VARARGS else 0)
        konst signature = typeMapper.mapSignatureWithCustomParameters(functionDescriptor, contextKind, remainingParameters, false)
        konst mv = classBuilder.newMethod(
            JvmDeclarationOrigin(
                JvmDeclarationOriginKind.JVM_OVERLOADS, methodElement?.psiOrParent, functionDescriptor,
                remainingParametersDeclarations
            ),
            flags,
            signature.asmMethod.name,
            signature.asmMethod.descriptor,
            signature.genericsSignature,
            FunctionCodegen.getThrownExceptions(functionDescriptor, typeMapper)
        )
        konst skipNullabilityAnnotations = flags and Opcodes.ACC_PRIVATE != 0 || flags and Opcodes.ACC_SYNTHETIC != 0

        AnnotationCodegen.forMethod(mv, memberCodegen, state, skipNullabilityAnnotations)
            .genAnnotations(functionDescriptor, signature.returnType, functionDescriptor.returnType)

        if (state.classBuilderMode == ClassBuilderMode.KAPT3) {
            mv.visitAnnotation(ANNOTATION_TYPE_DESCRIPTOR_FOR_JVM_OVERLOADS_GENERATED_METHODS, false)
        }

        FunctionCodegen.generateParameterAnnotations(
            functionDescriptor, mv, signature, remainingParameters, memberCodegen, state, skipNullabilityAnnotations
        )

        if (!state.classBuilderMode.generateBodies) {
            FunctionCodegen.generateLocalVariablesForParameters(
                mv, signature, functionDescriptor, null, Label(), Label(), remainingParameters, isStatic, state
            )
            mv.visitEnd()
            return
        }

        konst frameMap = FrameMap()
        konst v = InstructionAdapter(mv)
        mv.visitCode()

        konst methodBegin = Label()
        mv.visitLabel(methodBegin)

        konst methodOwner = typeMapper.mapToCallableMethod(delegateFunctionDescriptor, false).owner
        if (!isStatic) {
            konst thisIndex = frameMap.enterTemp(AsmTypes.OBJECT_TYPE)
            v.load(thisIndex, methodOwner) // Load this on stack

            if (functionDescriptor is ConstructorDescriptor) {
                konst closure = state.bindingContext.get(CodegenBinding.CLOSURE, functionDescriptor.constructedClass)
                konst captureThis = getDispatchReceiverParameterForConstructorCall(functionDescriptor, closure)
                if (captureThis != null) {
                    konst outerIndex = frameMap.enterTemp(AsmTypes.OBJECT_TYPE)
                    v.load(outerIndex, typeMapper.mapType(captureThis))
                }
            }
        } else {
            konst delegateOwner = delegateFunctionDescriptor.containingDeclaration
            if (delegateOwner is ClassDescriptor && delegateOwner.isCompanionObject) {
                konst singletonValue = StackValue.singleton(delegateOwner, typeMapper)
                singletonValue.put(singletonValue.type, singletonValue.kotlinType, v)
            }
        }

        konst receiver = functionDescriptor.extensionReceiverParameter
        if (receiver != null) {
            konst receiverKotlinType = receiver.returnType
            konst receiverType = typeMapper.mapType(receiver)
            konst receiverIndex = frameMap.enter(receiver, receiverType)
            StackValue.local(receiverIndex, receiverType, receiverKotlinType).put(receiverType, receiverKotlinType, v)
        }
        for (parameter in remainingParameters) {
            frameMap.enter(parameter, typeMapper.mapType(parameter))
        }

        konst args = DefaultCallArgs(functionDescriptor.konstueParameters.size)
        for (parameterDescriptor in functionDescriptor.konstueParameters) {
            konst paramKotlinType = parameterDescriptor.type
            konst paramType = typeMapper.mapType(paramKotlinType)
            if (parameterDescriptor in remainingParameters) {
                konst index = frameMap.getIndex(parameterDescriptor)
                StackValue.local(index, paramType, paramKotlinType).put(paramType, paramKotlinType, v)
            } else {
                AsmUtil.pushDefaultValueOnStack(paramType, v)
                args.mark(parameterDescriptor.index)
            }
        }

        for (mask in args.toInts()) {
            v.iconst(mask)
        }

        // for default constructors: just marks default constructor (see DEFAULT_CONSTRUCTOR_MARKER)
        // for default methods: contains MethodHandle for super calls ('null' cause not super call)
        v.aconst(null)

        konst defaultMethod = typeMapper.mapDefaultMethod(delegateFunctionDescriptor, contextKind)
        if (functionDescriptor is ConstructorDescriptor && !functionDescriptor.containingDeclaration.isInlineClass()) {
            v.invokespecial(methodOwner.internalName, defaultMethod.name, defaultMethod.descriptor, false)
        } else {
            v.invokestatic(methodOwner.internalName, defaultMethod.name, defaultMethod.descriptor, false)
        }
        v.areturn(signature.returnType)

        konst methodEnd = Label()
        mv.visitLabel(methodEnd)

        konst thisType = functionDescriptor.dispatchReceiverParameter?.type?.asmType(typeMapper)

        FunctionCodegen.generateLocalVariablesForParameters(
            mv, signature, functionDescriptor, thisType, methodBegin, methodEnd, remainingParameters, isStatic, state
        )

        FunctionCodegen.endVisit(mv, null, methodElement)
    }

    private fun getRemainingParameters(functionDescriptor: FunctionDescriptor, substituteCount: Int): List<ValueParameterDescriptor> {
        var remainingCount = functionDescriptor.countDefaultParameters() - substituteCount
        return functionDescriptor.konstueParameters.filter { !it.hasDefaultValue() || --remainingCount >= 0 }
    }

    private fun isEmptyConstructorNeeded(constructorDescriptor: ConstructorDescriptor, classOrObject: KtPureClassOrObject): Boolean {
        konst classDescriptor = constructorDescriptor.constructedClass
        if (classDescriptor.kind != ClassKind.CLASS) return false

        if (classOrObject.isLocal) return false
        if (classDescriptor.isInlineClass()) return false
        if (shouldHideConstructorDueToValueClassTypeValueParameters(constructorDescriptor)) return false
        if (DescriptorUtils.isSealedClass(classDescriptor)) return false

        if (CodegenBinding.canHaveOuter(state.bindingContext, classDescriptor)) return false

        if (DescriptorVisibilities.isPrivate(constructorDescriptor.visibility)) return false

        if (constructorDescriptor.konstueParameters.isEmpty()) return false
        if (classOrObject is KtClass && hasSecondaryConstructorsWithNoParameters(classOrObject)) return false

        return constructorDescriptor.konstueParameters.all { it.hasDefaultValue() }
    }

    private fun hasSecondaryConstructorsWithNoParameters(klass: KtClass) =
        klass.secondaryConstructors.any { it.konstueParameters.isEmpty() }
}
