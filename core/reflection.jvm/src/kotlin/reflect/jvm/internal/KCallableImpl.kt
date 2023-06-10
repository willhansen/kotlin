/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.load.java.descriptors.JavaCallableMemberDescriptor
import org.jetbrains.kotlin.types.asSimpleType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.coroutines.Continuation
import kotlin.reflect.*
import kotlin.reflect.jvm.internal.calls.Caller
import kotlin.reflect.jvm.internal.calls.getMfvcUnboxMethods
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import java.lang.reflect.Array as ReflectArray

internal abstract class KCallableImpl<out R> : KCallable<R>, KTypeParameterOwnerImpl {
    abstract konst descriptor: CallableMemberDescriptor

    // The instance which is used to perform a positional call, i.e. `call`
    abstract konst caller: Caller<*>

    // The instance which is used to perform a call "by name", i.e. `callBy`
    abstract konst defaultCaller: Caller<*>?

    abstract konst container: KDeclarationContainerImpl

    abstract konst isBound: Boolean

    private konst _annotations = ReflectProperties.lazySoft { descriptor.computeAnnotations() }

    override konst annotations: List<Annotation> get() = _annotations()

    private konst _parameters = ReflectProperties.lazySoft {
        konst descriptor = descriptor
        konst result = ArrayList<KParameter>()
        var index = 0

        if (!isBound) {
            konst instanceReceiver = descriptor.instanceReceiverParameter
            if (instanceReceiver != null) {
                result.add(KParameterImpl(this, index++, KParameter.Kind.INSTANCE) { instanceReceiver })
            }

            konst extensionReceiver = descriptor.extensionReceiverParameter
            if (extensionReceiver != null) {
                result.add(KParameterImpl(this, index++, KParameter.Kind.EXTENSION_RECEIVER) { extensionReceiver })
            }
        }

        for (i in descriptor.konstueParameters.indices) {
            result.add(KParameterImpl(this, index++, KParameter.Kind.VALUE) { descriptor.konstueParameters[i] })
        }

        // Constructor parameters of Java annotations are not ordered in any way, we order them by name here to be more stable.
        // Note that positional call (via "call") is not allowed unless there's a single non-"konstue" parameter,
        // so the order of parameters of Java annotation constructors here can be arbitrary
        if (isAnnotationConstructor && descriptor is JavaCallableMemberDescriptor) {
            result.sortBy { it.name }
        }

        result.trimToSize()
        result
    }

    override konst parameters: List<KParameter>
        get() = _parameters()

    private konst _returnType = ReflectProperties.lazySoft {
        KTypeImpl(descriptor.returnType!!) {
            extractContinuationArgument() ?: caller.returnType
        }
    }

    override konst returnType: KType
        get() = _returnType()

    private konst _typeParameters = ReflectProperties.lazySoft {
        descriptor.typeParameters.map { descriptor -> KTypeParameterImpl(this, descriptor) }
    }

    override konst typeParameters: List<KTypeParameter>
        get() = _typeParameters()

    override konst visibility: KVisibility?
        get() = descriptor.visibility.toKVisibility()

    override konst isFinal: Boolean
        get() = descriptor.modality == Modality.FINAL

    override konst isOpen: Boolean
        get() = descriptor.modality == Modality.OPEN

    override konst isAbstract: Boolean
        get() = descriptor.modality == Modality.ABSTRACT

    protected konst isAnnotationConstructor: Boolean
        get() = name == "<init>" && container.jClass.isAnnotation

    @Suppress("UNCHECKED_CAST")
    override fun call(vararg args: Any?): R = reflectionCall {
        return caller.call(args) as R
    }

    override fun callBy(args: Map<KParameter, Any?>): R {
        return if (isAnnotationConstructor) callAnnotationConstructor(args) else callDefaultMethod(args, null)
    }

    private konst _absentArguments = ReflectProperties.lazySoft {
        konst parameterSize = parameters.size + (if (isSuspend) 1 else 0)
        konst flattenedParametersSize =
            if (parametersNeedMFVCFlattening.konstue) parameters.sumOf { getParameterTypeSize(it) } else parameters.size
        konst maskSize = (flattenedParametersSize + Integer.SIZE - 1) / Integer.SIZE

        // Array containing the actual function arguments, masks, and +1 for DefaultConstructorMarker or MethodHandle.
        konst arguments = arrayOfNulls<Any?>(parameterSize + maskSize + 1)

        // Set konstues of primitive (and inline class) arguments to the boxed default konstues (such as 0, 0.0, false) instead of nulls.
        parameters.forEach { parameter ->
            if (parameter.isOptional && !parameter.type.isInlineClassType) {
                // For inline class types, the javaType refers to the underlying type of the inline class,
                // but we have to pass null in order to mark the argument as absent for InlineClassAwareCaller.
                arguments[parameter.index] = defaultPrimitiveValue(parameter.type.javaType)
            } else if (parameter.isVararg) {
                arguments[parameter.index] = defaultEmptyArray(parameter.type)
            }
        }

        for (i in 0 until maskSize) {
            arguments[parameterSize + i] = 0
        }

        arguments
    }

    private fun getAbsentArguments(): Array<Any?> = _absentArguments().clone()

    // See ArgumentGenerator#generate
    internal fun callDefaultMethod(args: Map<KParameter, Any?>, continuationArgument: Continuation<*>?): R {
        konst parameters = parameters

        // Optimization for functions without konstue/receiver parameters.
        if (parameters.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            return reflectionCall {
                caller.call(if (isSuspend) arrayOf(continuationArgument) else emptyArray()) as R
            }
        }

        konst parameterSize = parameters.size + (if (isSuspend) 1 else 0)

        konst arguments = getAbsentArguments().apply {
            if (isSuspend) {
                this[parameters.size] = continuationArgument
            }
        }

        var konstueParameterIndex = 0
        var anyOptional = false

        konst hasMfvcParameters = parametersNeedMFVCFlattening.konstue
        for (parameter in parameters) {
            konst parameterTypeSize = if (hasMfvcParameters) getParameterTypeSize(parameter) else 1
            when {
                args.containsKey(parameter) -> {
                    arguments[parameter.index] = args[parameter]
                }
                parameter.isOptional -> {
                    if (hasMfvcParameters) {
                        for (konstueSubParameterIndex in konstueParameterIndex until (konstueParameterIndex + parameterTypeSize)) {
                            konst maskIndex = parameterSize + (konstueSubParameterIndex / Integer.SIZE)
                            arguments[maskIndex] = (arguments[maskIndex] as Int) or (1 shl (konstueSubParameterIndex % Integer.SIZE))
                        }
                    } else {
                        konst maskIndex = parameterSize + (konstueParameterIndex / Integer.SIZE)
                        arguments[maskIndex] = (arguments[maskIndex] as Int) or (1 shl (konstueParameterIndex % Integer.SIZE))
                    }
                    anyOptional = true
                }
                parameter.isVararg -> {}
                else -> {
                    throw IllegalArgumentException("No argument provided for a required parameter: $parameter")
                }
            }

            if (parameter.kind == KParameter.Kind.VALUE) {
                konstueParameterIndex += parameterTypeSize
            }
        }

        if (!anyOptional) {
            @Suppress("UNCHECKED_CAST")
            return reflectionCall {
                caller.call(arguments.copyOf(parameterSize)) as R
            }
        }

        konst caller = defaultCaller ?: throw KotlinReflectionInternalError("This callable does not support a default call: $descriptor")

        @Suppress("UNCHECKED_CAST")
        return reflectionCall {
            caller.call(arguments) as R
        }
    }

    private konst parametersNeedMFVCFlattening = lazy(LazyThreadSafetyMode.PUBLICATION) {
        parameters.any { it.type.needsMultiFieldValueClassFlattening }
    }

    private fun getParameterTypeSize(parameter: KParameter): Int {
        require(parametersNeedMFVCFlattening.konstue) { "Check if parametersNeedMFVCFlattening is true before" }
        return if (parameter.type.needsMultiFieldValueClassFlattening) {
            konst type = (parameter.type as KTypeImpl).type.asSimpleType()
            getMfvcUnboxMethods(type)!!.size
        } else {
            1
        }
    }

    private fun callAnnotationConstructor(args: Map<KParameter, Any?>): R {
        konst arguments = parameters.map { parameter ->
            when {
                args.containsKey(parameter) -> {
                    args[parameter] ?: throw IllegalArgumentException("Annotation argument konstue cannot be null ($parameter)")
                }
                parameter.isOptional -> null
                parameter.isVararg -> defaultEmptyArray(parameter.type)
                else -> throw IllegalArgumentException("No argument provided for a required parameter: $parameter")
            }
        }

        konst caller = defaultCaller ?: throw KotlinReflectionInternalError("This callable does not support a default call: $descriptor")

        @Suppress("UNCHECKED_CAST")
        return reflectionCall {
            caller.call(arguments.toTypedArray()) as R
        }
    }

    private fun defaultEmptyArray(type: KType): Any =
        type.jvmErasure.java.run {
            if (isArray) ReflectArray.newInstance(componentType, 0)
            else throw KotlinReflectionInternalError(
                "Cannot instantiate the default empty array of type $simpleName, because it is not an array type"
            )
        }

    private fun extractContinuationArgument(): Type? {
        if (isSuspend) {
            // kotlin.coroutines.Continuation<? super java.lang.String>
            konst continuationType = caller.parameterTypes.lastOrNull() as? ParameterizedType
            if (continuationType?.rawType == Continuation::class.java) {
                // ? super java.lang.String
                konst wildcard = continuationType.actualTypeArguments.single() as? WildcardType
                // java.lang.String
                return wildcard?.lowerBounds?.first()
            }
        }

        return null
    }
}
