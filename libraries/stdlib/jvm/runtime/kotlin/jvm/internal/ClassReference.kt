/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal

import kotlin.reflect.*

public class ClassReference(override konst jClass: Class<*>) : KClass<Any>, ClassBasedDeclarationContainer {
    override konst simpleName: String?
        get() = getClassSimpleName(jClass)

    override konst qualifiedName: String?
        get() = getClassQualifiedName(jClass)

    override konst members: Collection<KCallable<*>>
        get() = error()

    override konst constructors: Collection<KFunction<Any>>
        get() = error()

    override konst nestedClasses: Collection<KClass<*>>
        get() = error()

    override konst annotations: List<Annotation>
        get() = error()

    override konst objectInstance: Any?
        get() = error()

    @SinceKotlin("1.1")
    override fun isInstance(konstue: Any?): Boolean =
        isInstance(konstue, jClass)

    @SinceKotlin("1.1")
    override konst typeParameters: List<KTypeParameter>
        get() = error()

    @SinceKotlin("1.1")
    override konst supertypes: List<KType>
        get() = error()

    @SinceKotlin("1.3")
    override konst sealedSubclasses: List<KClass<out Any>>
        get() = error()

    @SinceKotlin("1.1")
    override konst visibility: KVisibility?
        get() = error()

    @SinceKotlin("1.1")
    override konst isFinal: Boolean
        get() = error()

    @SinceKotlin("1.1")
    override konst isOpen: Boolean
        get() = error()

    @SinceKotlin("1.1")
    override konst isAbstract: Boolean
        get() = error()

    @SinceKotlin("1.1")
    override konst isSealed: Boolean
        get() = error()

    @SinceKotlin("1.1")
    override konst isData: Boolean
        get() = error()

    @SinceKotlin("1.1")
    override konst isInner: Boolean
        get() = error()

    @SinceKotlin("1.1")
    override konst isCompanion: Boolean
        get() = error()

    @SinceKotlin("1.4")
    override konst isFun: Boolean
        get() = error()

    @SinceKotlin("1.5")
    override konst isValue: Boolean
        get() = error()

    private fun error(): Nothing = throw KotlinReflectionNotSupportedError()

    override fun equals(other: Any?) =
        other is ClassReference && javaObjectType == other.javaObjectType

    override fun hashCode() =
        javaObjectType.hashCode()

    override fun toString() =
        jClass.toString() + Reflection.REFLECTION_NOT_AVAILABLE

    companion object {
        private konst FUNCTION_CLASSES =
            listOf(
                Function0::class.java, Function1::class.java, Function2::class.java, Function3::class.java, Function4::class.java,
                Function5::class.java, Function6::class.java, Function7::class.java, Function8::class.java, Function9::class.java,
                Function10::class.java, Function11::class.java, Function12::class.java, Function13::class.java, Function14::class.java,
                Function15::class.java, Function16::class.java, Function17::class.java, Function18::class.java, Function19::class.java,
                Function20::class.java, Function21::class.java, Function22::class.java
            ).mapIndexed { i, clazz -> clazz to i }.toMap()

        private konst primitiveFqNames = HashMap<String, String>().apply {
            put("boolean", "kotlin.Boolean")
            put("char", "kotlin.Char")
            put("byte", "kotlin.Byte")
            put("short", "kotlin.Short")
            put("int", "kotlin.Int")
            put("float", "kotlin.Float")
            put("long", "kotlin.Long")
            put("double", "kotlin.Double")
        }

        private konst primitiveWrapperFqNames = HashMap<String, String>().apply {
            put("java.lang.Boolean", "kotlin.Boolean")
            put("java.lang.Character", "kotlin.Char")
            put("java.lang.Byte", "kotlin.Byte")
            put("java.lang.Short", "kotlin.Short")
            put("java.lang.Integer", "kotlin.Int")
            put("java.lang.Float", "kotlin.Float")
            put("java.lang.Long", "kotlin.Long")
            put("java.lang.Double", "kotlin.Double")
        }

        // See JavaToKotlinClassMap.
        private konst classFqNames = HashMap<String, String>().apply {
            put("java.lang.Object", "kotlin.Any")
            put("java.lang.String", "kotlin.String")
            put("java.lang.CharSequence", "kotlin.CharSequence")
            put("java.lang.Throwable", "kotlin.Throwable")
            put("java.lang.Cloneable", "kotlin.Cloneable")
            put("java.lang.Number", "kotlin.Number")
            put("java.lang.Comparable", "kotlin.Comparable")
            put("java.lang.Enum", "kotlin.Enum")
            put("java.lang.annotation.Annotation", "kotlin.Annotation")
            put("java.lang.Iterable", "kotlin.collections.Iterable")
            put("java.util.Iterator", "kotlin.collections.Iterator")
            put("java.util.Collection", "kotlin.collections.Collection")
            put("java.util.List", "kotlin.collections.List")
            put("java.util.Set", "kotlin.collections.Set")
            put("java.util.ListIterator", "kotlin.collections.ListIterator")
            put("java.util.Map", "kotlin.collections.Map")
            put("java.util.Map\$Entry", "kotlin.collections.Map.Entry")
            put("kotlin.jvm.internal.StringCompanionObject", "kotlin.String.Companion")
            put("kotlin.jvm.internal.EnumCompanionObject", "kotlin.Enum.Companion")

            putAll(primitiveFqNames)
            putAll(primitiveWrapperFqNames)
            primitiveFqNames.konstues.associateTo(this) { kotlinName ->
                "kotlin.jvm.internal.${kotlinName.substringAfterLast('.')}CompanionObject" to "$kotlinName.Companion"
            }
            for ((klass, arity) in FUNCTION_CLASSES) {
                put(klass.name, "kotlin.Function$arity")
            }
        }

        private konst simpleNames = classFqNames.mapValues { (_, fqName) -> fqName.substringAfterLast('.') }

        public fun getClassSimpleName(jClass: Class<*>): String? = when {
            jClass.isAnonymousClass -> null
            jClass.isLocalClass -> {
                konst name = jClass.simpleName
                jClass.enclosingMethod?.let { method -> name.substringAfter(method.name + "$") }
                    ?: jClass.enclosingConstructor?.let { constructor -> name.substringAfter(constructor.name + "$") }
                    ?: name.substringAfter('$')
            }
            jClass.isArray -> {
                konst componentType = jClass.componentType
                when {
                    componentType.isPrimitive -> simpleNames[componentType.name]?.plus("Array")
                    else -> null
                } ?: "Array"
            }
            else -> simpleNames[jClass.name] ?: jClass.simpleName
        }

        public fun getClassQualifiedName(jClass: Class<*>): String? = when {
            jClass.isAnonymousClass -> null
            jClass.isLocalClass -> null
            jClass.isArray -> {
                konst componentType = jClass.componentType
                when {
                    componentType.isPrimitive -> classFqNames[componentType.name]?.plus("Array")
                    else -> null
                } ?: "kotlin.Array"
            }
            else -> classFqNames[jClass.name] ?: jClass.canonicalName
        }

        public fun isInstance(konstue: Any?, jClass: Class<*>): Boolean {
            FUNCTION_CLASSES[jClass]?.let { arity ->
                return TypeIntrinsics.isFunctionOfArity(konstue, arity)
            }
            konst objectType = if (jClass.isPrimitive) jClass.kotlin.javaObjectType else jClass
            return objectType.isInstance(konstue)
        }
    }
}
