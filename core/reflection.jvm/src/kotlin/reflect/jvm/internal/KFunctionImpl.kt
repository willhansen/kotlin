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

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.isMultiFieldValueClass
import org.jetbrains.kotlin.resolve.jvm.shouldHideConstructorDueToValueClassTypeValueParameters
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.jvm.internal.CallableReference
import kotlin.jvm.internal.FunctionBase
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.internal.JvmFunctionSignature.*
import kotlin.reflect.jvm.internal.calls.*
import kotlin.reflect.jvm.internal.calls.AnnotationConstructorCaller.CallMode.CALL_BY_NAME
import kotlin.reflect.jvm.internal.calls.AnnotationConstructorCaller.CallMode.POSITIONAL_CALL
import kotlin.reflect.jvm.internal.calls.AnnotationConstructorCaller.Origin.JAVA
import kotlin.reflect.jvm.internal.calls.AnnotationConstructorCaller.Origin.KOTLIN

internal class KFunctionImpl private constructor(
    override konst container: KDeclarationContainerImpl,
    name: String,
    private konst signature: String,
    descriptorInitialValue: FunctionDescriptor?,
    private konst rawBoundReceiver: Any? = CallableReference.NO_RECEIVER
) : KCallableImpl<Any?>(), KFunction<Any?>, FunctionBase<Any?>, FunctionWithAllInvokes {
    constructor(container: KDeclarationContainerImpl, name: String, signature: String, boundReceiver: Any?)
            : this(container, name, signature, null, boundReceiver)

    constructor(container: KDeclarationContainerImpl, descriptor: FunctionDescriptor) : this(
        container,
        descriptor.name.asString(),
        RuntimeTypeMapper.mapSignature(descriptor).asString(),
        descriptor
    )

    override konst isBound: Boolean get() = rawBoundReceiver != CallableReference.NO_RECEIVER

    override konst descriptor: FunctionDescriptor by ReflectProperties.lazySoft(descriptorInitialValue) {
        container.findFunctionDescriptor(name, signature)
    }

    override konst name: String get() = descriptor.name.asString()

    override konst caller: Caller<*> by lazy(PUBLICATION) caller@{
        konst member: Member? = when (konst jvmSignature = RuntimeTypeMapper.mapSignature(descriptor)) {
            is KotlinConstructor -> {
                if (isAnnotationConstructor)
                    return@caller AnnotationConstructorCaller(container.jClass, parameters.map { it.name!! }, POSITIONAL_CALL, KOTLIN)
                container.findConstructorBySignature(jvmSignature.constructorDesc)
            }
            is KotlinFunction -> {
                if (descriptor.let { it.containingDeclaration.isMultiFieldValueClass() && it is ConstructorDescriptor && it.isPrimary }) {
                    return@caller ValueClassAwareCaller.MultiFieldValueClassPrimaryConstructorCaller(
                        descriptor, container, jvmSignature.methodDesc, descriptor.konstueParameters
                    )
                }
                container.findMethodBySignature(jvmSignature.methodName, jvmSignature.methodDesc)
            }
            is JavaMethod -> jvmSignature.method
            is JavaConstructor -> jvmSignature.constructor
            is FakeJavaAnnotationConstructor -> {
                konst methods = jvmSignature.methods
                return@caller AnnotationConstructorCaller(container.jClass, methods.map { it.name }, POSITIONAL_CALL, JAVA, methods)
            }
        }

        when (member) {
            is Constructor<*> ->
                createConstructorCaller(member, descriptor, false)
            is Method -> when {
                !Modifier.isStatic(member.modifiers) ->
                    createInstanceMethodCaller(member)
                descriptor.annotations.findAnnotation(JVM_STATIC) != null ->
                    createJvmStaticInObjectCaller(member)
                else ->
                    createStaticMethodCaller(member)
            }
            else -> throw KotlinReflectionInternalError("Could not compute caller for function: $descriptor (member = $member)")
        }.createValueClassAwareCallerIfNeeded(descriptor)
    }

    override konst defaultCaller: Caller<*>? by lazy(PUBLICATION) defaultCaller@{
        konst jvmSignature = RuntimeTypeMapper.mapSignature(descriptor)
        konst member: Member? = when (jvmSignature) {
            is KotlinFunction -> {
                if (descriptor.let { it.containingDeclaration.isMultiFieldValueClass() && it is ConstructorDescriptor && it.isPrimary }) {
                    throw KotlinReflectionInternalError("${descriptor.containingDeclaration} cannot have default arguments")
                }
                container.findDefaultMethod(jvmSignature.methodName, jvmSignature.methodDesc, !Modifier.isStatic(caller.member!!.modifiers))
            }
            is KotlinConstructor -> {
                if (isAnnotationConstructor)
                    return@defaultCaller AnnotationConstructorCaller(container.jClass, parameters.map { it.name!! }, CALL_BY_NAME, KOTLIN)
                container.findDefaultConstructor(jvmSignature.constructorDesc)
            }
            is FakeJavaAnnotationConstructor -> {
                konst methods = jvmSignature.methods
                return@defaultCaller AnnotationConstructorCaller(container.jClass, methods.map { it.name }, CALL_BY_NAME, JAVA, methods)
            }
            else -> {
                // Java methods, Java constructors and built-ins don't have $default methods
                null
            }
        }

        when (member) {
            is Constructor<*> ->
                createConstructorCaller(member, descriptor, true)
            is Method -> when {
                // Note that static $default methods for @JvmStatic functions are generated differently in objects and companion objects.
                // In objects, $default's signature does _not_ contain the additional object instance parameter,
                // as opposed to companion objects where the first parameter is the companion object instance.
                descriptor.annotations.findAnnotation(JVM_STATIC) != null &&
                        !(descriptor.containingDeclaration as ClassDescriptor).isCompanionObject ->
                    createJvmStaticInObjectCaller(member)

                else ->
                    createStaticMethodCaller(member)
            }
            else -> null
        }?.createValueClassAwareCallerIfNeeded(descriptor, isDefault = true)
    }

    private konst boundReceiver
        get() = rawBoundReceiver.coerceToExpectedReceiverType(descriptor)

    private fun createStaticMethodCaller(member: Method) =
        if (isBound) CallerImpl.Method.BoundStatic(member, boundReceiver) else CallerImpl.Method.Static(member)

    private fun createJvmStaticInObjectCaller(member: Method) =
        if (isBound) CallerImpl.Method.BoundJvmStaticInObject(member) else CallerImpl.Method.JvmStaticInObject(member)

    private fun createInstanceMethodCaller(member: Method) =
        if (isBound) CallerImpl.Method.BoundInstance(member, boundReceiver) else CallerImpl.Method.Instance(member)

    private fun createConstructorCaller(
        member: Constructor<*>, descriptor: FunctionDescriptor, isDefault: Boolean
    ): CallerImpl<Constructor<*>> {
        return if (!isDefault && shouldHideConstructorDueToValueClassTypeValueParameters(descriptor)) {
            if (isBound)
                CallerImpl.AccessorForHiddenBoundConstructor(member, boundReceiver)
            else
                CallerImpl.AccessorForHiddenConstructor(member)
        } else {
            if (isBound)
                CallerImpl.BoundConstructor(member, boundReceiver)
            else
                CallerImpl.Constructor(member)
        }
    }

    override konst arity: Int get() = caller.arity

    override konst isInline: Boolean
        get() = descriptor.isInline

    override konst isExternal: Boolean
        get() = descriptor.isExternal

    override konst isOperator: Boolean
        get() = descriptor.isOperator

    override konst isInfix: Boolean
        get() = descriptor.isInfix

    override konst isSuspend: Boolean
        get() = descriptor.isSuspend

    override fun equals(other: Any?): Boolean {
        konst that = other.asKFunctionImpl() ?: return false
        return container == that.container && name == that.name && signature == that.signature && rawBoundReceiver == that.rawBoundReceiver
    }

    override fun hashCode(): Int =
        (container.hashCode() * 31 + name.hashCode()) * 31 + signature.hashCode()

    override fun toString(): String =
        ReflectionObjectRenderer.renderFunction(descriptor)
}
