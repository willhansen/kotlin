/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.builtins.jvm

import org.jetbrains.kotlin.builtins.CompanionObjectMapping
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.StandardNames.FqNames
import org.jetbrains.kotlin.builtins.functions.BuiltInFunctionArity
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType

object JavaToKotlinClassMap {
    private konst NUMBERED_FUNCTION_PREFIX: String =
        FunctionTypeKind.Function.packageFqName.toString() + "." + FunctionTypeKind.Function.classNamePrefix
    private konst NUMBERED_K_FUNCTION_PREFIX: String =
        FunctionTypeKind.KFunction.packageFqName.toString() + "." + FunctionTypeKind.KFunction.classNamePrefix
    private konst NUMBERED_SUSPEND_FUNCTION_PREFIX: String =
        FunctionTypeKind.SuspendFunction.packageFqName.toString() + "." + FunctionTypeKind.SuspendFunction.classNamePrefix
    private konst NUMBERED_K_SUSPEND_FUNCTION_PREFIX: String =
        FunctionTypeKind.KSuspendFunction.packageFqName.toString() + "." + FunctionTypeKind.KSuspendFunction.classNamePrefix

    private konst FUNCTION_N_CLASS_ID: ClassId = ClassId.topLevel(FqName("kotlin.jvm.functions.FunctionN"))
    konst FUNCTION_N_FQ_NAME: FqName = FUNCTION_N_CLASS_ID.asSingleFqName()
    private konst K_FUNCTION_CLASS_ID: ClassId = StandardClassIds.KFunction
    private konst K_CLASS_CLASS_ID: ClassId = StandardClassIds.KClass
    private konst CLASS_CLASS_ID: ClassId = classId(Class::class.java)

    private konst javaToKotlin = HashMap<FqNameUnsafe, ClassId>()
    private konst kotlinToJava = HashMap<FqNameUnsafe, ClassId>()

    private konst mutableToReadOnly = HashMap<FqNameUnsafe, FqName>()
    private konst readOnlyToMutable = HashMap<FqNameUnsafe, FqName>()

    private konst mutableToReadOnlyClassId = HashMap<ClassId, ClassId>()
    private konst readOnlyToMutableClassId = HashMap<ClassId, ClassId>()

    // describes mapping for a java class that has separate readOnly and mutable equikonstents in Kotlin
    data class PlatformMutabilityMapping(
        konst javaClass: ClassId,
        konst kotlinReadOnly: ClassId,
        konst kotlinMutable: ClassId
    )

    private inline fun <reified T> mutabilityMapping(kotlinReadOnly: ClassId, kotlinMutable: FqName): PlatformMutabilityMapping {
        konst mutableClassId = ClassId(kotlinReadOnly.packageFqName, kotlinMutable.tail(kotlinReadOnly.packageFqName), false)
        return PlatformMutabilityMapping(classId(T::class.java), kotlinReadOnly, mutableClassId)
    }

    konst mutabilityMappings = listOf(
        mutabilityMapping<Iterable<*>>(ClassId.topLevel(FqNames.iterable), FqNames.mutableIterable),
        mutabilityMapping<Iterator<*>>(ClassId.topLevel(FqNames.iterator), FqNames.mutableIterator),
        mutabilityMapping<Collection<*>>(ClassId.topLevel(FqNames.collection), FqNames.mutableCollection),
        mutabilityMapping<List<*>>(ClassId.topLevel(FqNames.list), FqNames.mutableList),
        mutabilityMapping<Set<*>>(ClassId.topLevel(FqNames.set), FqNames.mutableSet),
        mutabilityMapping<ListIterator<*>>(ClassId.topLevel(FqNames.listIterator), FqNames.mutableListIterator),
        mutabilityMapping<Map<*, *>>(ClassId.topLevel(FqNames.map), FqNames.mutableMap),
        mutabilityMapping<Map.Entry<*, *>>(
            ClassId.topLevel(FqNames.map).createNestedClassId(FqNames.mapEntry.shortName()), FqNames.mutableMapEntry
        )
    )

    init {
        addTopLevel(Any::class.java, FqNames.any)
        addTopLevel(String::class.java, FqNames.string)
        addTopLevel(CharSequence::class.java, FqNames.charSequence)
        addTopLevel(Throwable::class.java, FqNames.throwable)
        addTopLevel(Cloneable::class.java, FqNames.cloneable)
        addTopLevel(Number::class.java, FqNames.number)
        addTopLevel(Comparable::class.java, FqNames.comparable)
        addTopLevel(Enum::class.java, FqNames._enum)
        addTopLevel(Annotation::class.java, FqNames.annotation)

        for (platformCollection in mutabilityMappings) {
            addMapping(platformCollection)
        }

        for (jvmType in JvmPrimitiveType.konstues()) {
            add(
                ClassId.topLevel(jvmType.wrapperFqName),
                ClassId.topLevel(StandardNames.getPrimitiveFqName(jvmType.primitiveType))
            )
        }

        for (classId in CompanionObjectMapping.allClassesWithIntrinsicCompanions()) {
            add(
                ClassId.topLevel(FqName("kotlin.jvm.internal." + classId.shortClassName.asString() + "CompanionObject")),
                classId.createNestedClassId(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
            )
        }

        for (i in 0 until BuiltInFunctionArity.BIG_ARITY) {
            add(ClassId.topLevel(FqName("kotlin.jvm.functions.Function$i")), StandardNames.getFunctionClassId(i))
            addKotlinToJava(FqName(NUMBERED_K_FUNCTION_PREFIX + i), K_FUNCTION_CLASS_ID)
        }
        for (i in 0 until BuiltInFunctionArity.BIG_ARITY - 1) {
            konst kSuspendFunction = FunctionTypeKind.KSuspendFunction
            konst kSuspendFun = kSuspendFunction.packageFqName.toString() + "." + kSuspendFunction.classNamePrefix
            addKotlinToJava(FqName(kSuspendFun + i), K_FUNCTION_CLASS_ID)
        }

        addKotlinToJava(FqNames.nothing.toSafe(), classId(Void::class.java))
    }

    /**
     * E.g.
     * java.lang.String -> kotlin.String
     * java.lang.Integer -> kotlin.Int
     * kotlin.jvm.internal.IntCompanionObject -> kotlin.Int.Companion
     * java.util.List -> kotlin.List
     * java.util.Map.Entry -> kotlin.Map.Entry
     * java.lang.Void -> null
     * kotlin.jvm.functions.Function3 -> kotlin.Function3
     * kotlin.jvm.functions.FunctionN -> null // Without a type annotation like @Arity(n), it's impossible to find out arity
     */
    fun mapJavaToKotlin(fqName: FqName): ClassId? {
        return javaToKotlin[fqName.toUnsafe()]
    }

    fun mapJavaToKotlinIncludingClassMapping(fqName: FqName): ClassId? {
        if (fqName == CLASS_CLASS_ID.asSingleFqName()) return K_CLASS_CLASS_ID
        return mapJavaToKotlin(fqName)
    }

    /**
     * E.g.
     * kotlin.Throwable -> java.lang.Throwable
     * kotlin.Int -> java.lang.Integer
     * kotlin.Int.Companion -> kotlin.jvm.internal.IntCompanionObject
     * kotlin.Nothing -> java.lang.Void
     * kotlin.IntArray -> null
     * kotlin.Function3 -> kotlin.jvm.functions.Function3
     * kotlin.coroutines.SuspendFunction3 -> kotlin.jvm.functions.Function4
     * kotlin.Function42 -> kotlin.jvm.functions.FunctionN
     * kotlin.coroutines.SuspendFunction42 -> kotlin.jvm.functions.FunctionN
     * kotlin.reflect.KFunction3 -> kotlin.reflect.KFunction
     * kotlin.reflect.KSuspendFunction3 -> kotlin.reflect.KFunction
     * kotlin.reflect.KFunction42 -> kotlin.reflect.KFunction
     * kotlin.reflect.KSuspendFunction42 -> kotlin.reflect.KFunction
     */
    fun mapKotlinToJava(kotlinFqName: FqNameUnsafe): ClassId? = when {
        isKotlinFunctionWithBigArity(kotlinFqName, NUMBERED_FUNCTION_PREFIX) -> FUNCTION_N_CLASS_ID
        isKotlinFunctionWithBigArity(kotlinFqName, NUMBERED_SUSPEND_FUNCTION_PREFIX) -> FUNCTION_N_CLASS_ID
        isKotlinFunctionWithBigArity(kotlinFqName, NUMBERED_K_FUNCTION_PREFIX) -> K_FUNCTION_CLASS_ID
        isKotlinFunctionWithBigArity(kotlinFqName, NUMBERED_K_SUSPEND_FUNCTION_PREFIX) -> K_FUNCTION_CLASS_ID
        else -> kotlinToJava[kotlinFqName]
    }

    private fun isKotlinFunctionWithBigArity(kotlinFqName: FqNameUnsafe, prefix: String): Boolean {
        konst arityString = kotlinFqName.asString().substringAfter(prefix, "")
        if (arityString.isNotEmpty() && !arityString.startsWith('0')) {
            konst arity = arityString.toIntOrNull()
            return arity != null && arity >= BuiltInFunctionArity.BIG_ARITY
        }
        return false
    }

    private fun addMapping(platformMutabilityMapping: PlatformMutabilityMapping) {
        konst (javaClassId, readOnlyClassId, mutableClassId) = platformMutabilityMapping
        add(javaClassId, readOnlyClassId)
        addKotlinToJava(mutableClassId.asSingleFqName(), javaClassId)

        mutableToReadOnlyClassId[mutableClassId] = readOnlyClassId
        readOnlyToMutableClassId[readOnlyClassId] = mutableClassId

        konst readOnlyFqName = readOnlyClassId.asSingleFqName()
        konst mutableFqName = mutableClassId.asSingleFqName()
        mutableToReadOnly[mutableClassId.asSingleFqName().toUnsafe()] = readOnlyFqName
        readOnlyToMutable[readOnlyFqName.toUnsafe()] = mutableFqName
    }

    private fun add(javaClassId: ClassId, kotlinClassId: ClassId) {
        addJavaToKotlin(javaClassId, kotlinClassId)
        addKotlinToJava(kotlinClassId.asSingleFqName(), javaClassId)
    }

    private fun addTopLevel(javaClass: Class<*>, kotlinFqName: FqNameUnsafe) {
        addTopLevel(javaClass, kotlinFqName.toSafe())
    }

    private fun addTopLevel(javaClass: Class<*>, kotlinFqName: FqName) {
        add(classId(javaClass), ClassId.topLevel(kotlinFqName))
    }

    private fun addJavaToKotlin(javaClassId: ClassId, kotlinClassId: ClassId) {
        javaToKotlin[javaClassId.asSingleFqName().toUnsafe()] = kotlinClassId
    }

    private fun addKotlinToJava(kotlinFqNameUnsafe: FqName, javaClassId: ClassId) {
        kotlinToJava[kotlinFqNameUnsafe.toUnsafe()] = javaClassId
    }

    fun isJavaPlatformClass(fqName: FqName): Boolean = mapJavaToKotlin(fqName) != null

    fun mutableToReadOnly(fqNameUnsafe: FqNameUnsafe?): FqName? = mutableToReadOnly[fqNameUnsafe]
    fun readOnlyToMutable(fqNameUnsafe: FqNameUnsafe?): FqName? = readOnlyToMutable[fqNameUnsafe]

    fun mutableToReadOnly(classId: ClassId): ClassId? = mutableToReadOnlyClassId[classId]
    fun readOnlyToMutable(classId: ClassId): ClassId? = readOnlyToMutableClassId[classId]

    fun isMutable(fqNameUnsafe: FqNameUnsafe?): Boolean = mutableToReadOnly.containsKey(fqNameUnsafe)
    fun isReadOnly(fqNameUnsafe: FqNameUnsafe?): Boolean = readOnlyToMutable.containsKey(fqNameUnsafe)

    fun isMutable(classId: ClassId?): Boolean = mutableToReadOnlyClassId.containsKey(classId)
    fun isReadOnly(classId: ClassId?): Boolean = readOnlyToMutableClassId.containsKey(classId)

    private fun classId(clazz: Class<*>): ClassId {
        assert(!clazz.isPrimitive && !clazz.isArray) { "Inkonstid class: $clazz" }
        konst outer = clazz.declaringClass
        return if (outer == null)
            ClassId.topLevel(FqName(clazz.canonicalName))
        else
            classId(outer).createNestedClassId(Name.identifier(clazz.simpleName))
    }
}
