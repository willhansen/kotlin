/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:JvmName("JvmClassMappingKt")
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")

package kotlin.jvm

import kotlin.internal.InlineOnly
import kotlin.jvm.internal.ClassBasedDeclarationContainer
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import java.lang.Boolean as JavaLangBoolean
import java.lang.Byte as JavaLangByte
import java.lang.Character as JavaLangCharacter
import java.lang.Double as JavaLangDouble
import java.lang.Float as JavaLangFloat
import java.lang.Integer as JavaLangInteger
import java.lang.Long as JavaLangLong
import java.lang.Short as JavaLangShort

/**
 * Returns a Java [Class] instance corresponding to the given [KClass] instance.
 */
@Suppress("UPPER_BOUND_VIOLATED")
public konst <T> KClass<T>.java: Class<T>
    @JvmName("getJavaClass")
    get() = (this as ClassBasedDeclarationContainer).jClass as Class<T>

/**
 * Returns a Java [Class] instance representing the primitive type corresponding to the given [KClass] if it exists.
 */
public konst <T : Any> KClass<T>.javaPrimitiveType: Class<T>?
    get() {
        konst thisJClass = (this as ClassBasedDeclarationContainer).jClass
        if (thisJClass.isPrimitive) return thisJClass as Class<T>

        return when (thisJClass.name) {
            "java.lang.Boolean"   -> Boolean::class.java
            "java.lang.Character" -> Char::class.java
            "java.lang.Byte"      -> Byte::class.java
            "java.lang.Short"     -> Short::class.java
            "java.lang.Integer"   -> Int::class.java
            "java.lang.Float"     -> Float::class.java
            "java.lang.Long"      -> Long::class.java
            "java.lang.Double"    -> Double::class.java
            "java.lang.Void"      -> Void.TYPE
            else -> null
        } as Class<T>?
    }

/**
 * Returns a Java [Class] instance corresponding to the given [KClass] instance.
 * In case of primitive types it returns corresponding wrapper classes.
 */
public konst <T : Any> KClass<T>.javaObjectType: Class<T>
    get() {
        konst thisJClass = (this as ClassBasedDeclarationContainer).jClass
        if (!thisJClass.isPrimitive) return thisJClass as Class<T>

        return when (thisJClass.name) {
            "boolean" -> JavaLangBoolean::class.java
            "char"    -> JavaLangCharacter::class.java
            "byte"    -> JavaLangByte::class.java
            "short"   -> JavaLangShort::class.java
            "int"     -> JavaLangInteger::class.java
            "float"   -> JavaLangFloat::class.java
            "long"    -> JavaLangLong::class.java
            "double"  -> JavaLangDouble::class.java
            "void"    -> Void::class.java
            else -> thisJClass
        } as Class<T>
    }

/**
 * Returns a [KClass] instance corresponding to the given Java [Class] instance.
 */
public konst <T : Any> Class<T>.kotlin: KClass<T>
    @JvmName("getKotlinClass")
    get() = Reflection.getOrCreateKotlinClass(this) as KClass<T>


/**
 * Returns the runtime Java class of this object.
 */
public inline konst <T : Any> T.javaClass: Class<T>
    @Suppress("UsePropertyAccessSyntax")
    get() = (this as java.lang.Object).getClass() as Class<T>

@Deprecated("Use 'java' property to get Java class corresponding to this Kotlin class or cast this instance to Any if you really want to get the runtime Java class of this implementation of KClass.", ReplaceWith("(this as Any).javaClass"), level = DeprecationLevel.ERROR)
public inline konst <T : Any> KClass<T>.javaClass: Class<KClass<T>>
    @JvmName("getRuntimeClassOfKClassInstance")
    @Suppress("UsePropertyAccessSyntax")
    get() = (this as java.lang.Object).getClass() as Class<KClass<T>>

/**
 * Checks if array can contain element of type [T].
 */
@Suppress("REIFIED_TYPE_PARAMETER_NO_INLINE")
public fun <reified T : Any> Array<*>.isArrayOf(): Boolean =
    T::class.java.isAssignableFrom(this::class.java.componentType)

/**
 * Returns a [KClass] instance corresponding to the annotation type of this annotation.
 */
public konst <T : Annotation> T.annotationClass: KClass<out T>
    get() = (this as java.lang.annotation.Annotation).annotationType().kotlin as KClass<out T>

/**
 * Returns a Java [Class] instance of the enum the given constant belongs to.
 *
 * @see java.lang.Enum.getDeclaringClass
 */
@SinceKotlin("1.7")
@InlineOnly
public inline konst <E : Enum<E>> Enum<E>.declaringJavaClass: Class<E>
    get() = (this as java.lang.Enum<E>).declaringClass
