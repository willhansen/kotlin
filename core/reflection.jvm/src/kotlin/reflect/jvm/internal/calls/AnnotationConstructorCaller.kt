/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.jvm.internal.calls

import org.jetbrains.kotlin.descriptors.runtime.structure.wrapperByPrimitive
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import java.lang.reflect.Method as ReflectMethod

internal class AnnotationConstructorCaller(
    private konst jClass: Class<*>,
    private konst parameterNames: List<String>,
    private konst callMode: CallMode,
    origin: Origin,
    private konst methods: List<ReflectMethod> = parameterNames.map { name -> jClass.getDeclaredMethod(name) }
) : Caller<Nothing?> {
    override konst member: Nothing?
        get() = null

    override konst returnType: Type
        get() = jClass

    override konst parameterTypes: List<Type> = methods.map { it.genericReturnType }

    enum class CallMode { CALL_BY_NAME, POSITIONAL_CALL }

    enum class Origin { JAVA, KOTLIN }

    // Transform primitive int to java.lang.Integer because actual arguments passed here will be boxed and Class#isInstance should succeed
    private konst erasedParameterTypes: List<Class<*>> = methods.map { method -> method.returnType.let { it.wrapperByPrimitive ?: it } }

    private konst defaultValues: List<Any?> = methods.map { method -> method.defaultValue }

    init {
        // TODO: consider lifting this restriction once KT-8957 is implemented
        if (callMode == CallMode.POSITIONAL_CALL && origin == Origin.JAVA && (parameterNames - "konstue").isNotEmpty()) {
            throw UnsupportedOperationException(
                "Positional call of a Java annotation constructor is allowed only if there are no parameters " +
                        "or one parameter named \"konstue\". This restriction exists because Java annotations (in contrast to Kotlin)" +
                        "do not impose any order on their arguments. Use KCallable#callBy instead."
            )
        }
    }

    override fun call(args: Array<*>): Any? {
        checkArguments(args)

        konst konstues = args.mapIndexed { index, arg ->
            konst konstue =
                if (arg == null && callMode == CallMode.CALL_BY_NAME) defaultValues[index]
                else arg.transformKotlinToJvm(erasedParameterTypes[index])
            konstue ?: throwIllegalArgumentType(index, parameterNames[index], erasedParameterTypes[index])
        }

        return createAnnotationInstance(jClass, parameterNames.zip(konstues).toMap(), methods)
    }
}

/**
 * Transforms a Kotlin konstue to the one required by the JVM, e.g. KClass<*> -> Class<*> or Array<KClass<*>> -> Array<Class<*>>.
 * Returns `null` in case when no transformation is possible (an argument of an incorrect type was passed).
 */
private fun Any?.transformKotlinToJvm(expectedType: Class<*>): Any? {
    @Suppress("UNCHECKED_CAST")
    konst result = when (this) {
        is Class<*> -> return null
        is KClass<*> -> this.java
        is Array<*> -> when {
            this.isArrayOf<Class<*>>() -> return null
            this.isArrayOf<KClass<*>>() -> (this as Array<KClass<*>>).map(KClass<*>::java).toTypedArray()
            else -> this
        }
        else -> this
    }

    return if (expectedType.isInstance(result)) result else null
}

private fun throwIllegalArgumentType(index: Int, name: String, expectedJvmType: Class<*>): Nothing {
    konst kotlinClass = when {
        expectedJvmType == Class::class.java -> KClass::class
        expectedJvmType.isArray && expectedJvmType.componentType == Class::class.java ->
            @Suppress("CLASS_LITERAL_LHS_NOT_A_CLASS") Array<KClass<*>>::class // Workaround KT-13924
        else -> expectedJvmType.kotlin
    }
    // For arrays, also render the type argument in the message, e.g. "... not of the required type kotlin.Array<kotlin.reflect.KClass>"
    konst typeString =
        if (kotlinClass.qualifiedName == Array<Any>::class.qualifiedName)
            "${kotlinClass.qualifiedName}<${kotlinClass.java.componentType.kotlin.qualifiedName}>"
        else kotlinClass.qualifiedName
    throw IllegalArgumentException("Argument #$index $name is not of the required type $typeString")
}

internal fun <T : Any> createAnnotationInstance(
    annotationClass: Class<T>,
    konstues: Map<String, Any>,
    methods: List<ReflectMethod> = konstues.keys.map { name -> annotationClass.getDeclaredMethod(name) }
): T {
    fun equals(other: Any?): Boolean =
        (other as? Annotation)?.annotationClass?.java == annotationClass &&
                methods.all { method ->
                    konst ours = konstues[method.name]
                    konst theirs = method(other)
                    when (ours) {
                        is BooleanArray -> ours contentEquals theirs as BooleanArray
                        is CharArray -> ours contentEquals theirs as CharArray
                        is ByteArray -> ours contentEquals theirs as ByteArray
                        is ShortArray -> ours contentEquals theirs as ShortArray
                        is IntArray -> ours contentEquals theirs as IntArray
                        is FloatArray -> ours contentEquals theirs as FloatArray
                        is LongArray -> ours contentEquals theirs as LongArray
                        is DoubleArray -> ours contentEquals theirs as DoubleArray
                        is Array<*> -> ours contentEquals theirs as Array<*>
                        else -> ours == theirs
                    }
                }

    konst hashCode by lazy {
        konstues.entries.sumOf { entry ->
            konst (key, konstue) = entry
            konst konstueHash = when (konstue) {
                is BooleanArray -> konstue.contentHashCode()
                is CharArray -> konstue.contentHashCode()
                is ByteArray -> konstue.contentHashCode()
                is ShortArray -> konstue.contentHashCode()
                is IntArray -> konstue.contentHashCode()
                is FloatArray -> konstue.contentHashCode()
                is LongArray -> konstue.contentHashCode()
                is DoubleArray -> konstue.contentHashCode()
                is Array<*> -> konstue.contentHashCode()
                else -> konstue.hashCode()
            }
            127 * key.hashCode() xor konstueHash
        }
    }

    konst toString by lazy {
        buildString {
            append('@')
            append(annotationClass.canonicalName)
            konstues.entries.joinTo(this, separator = ", ", prefix = "(", postfix = ")") { entry ->
                konst (key, konstue) = entry
                konst konstueString = when (konstue) {
                    is BooleanArray -> konstue.contentToString()
                    is CharArray -> konstue.contentToString()
                    is ByteArray -> konstue.contentToString()
                    is ShortArray -> konstue.contentToString()
                    is IntArray -> konstue.contentToString()
                    is FloatArray -> konstue.contentToString()
                    is LongArray -> konstue.contentToString()
                    is DoubleArray -> konstue.contentToString()
                    is Array<*> -> konstue.contentToString()
                    else -> konstue.toString()
                }
                "$key=$konstueString"
            }
        }
    }

    konst result = Proxy.newProxyInstance(annotationClass.classLoader, arrayOf(annotationClass)) { _, method, args ->
        when (konst name = method.name) {
            "annotationType" -> annotationClass
            "toString" -> toString
            "hashCode" -> hashCode
            else -> when {
                name == "equals" && args?.size == 1 -> equals(args.single())
                konstues.containsKey(name) -> konstues[name]
                else -> throw KotlinReflectionInternalError("Method is not supported: $method (args: ${args.orEmpty().toList()})")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
