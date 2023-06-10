/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.name

import org.jetbrains.kotlin.builtins.StandardNames

object StandardClassIds {
    konst BASE_KOTLIN_PACKAGE = FqName("kotlin")
    konst BASE_REFLECT_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("reflect"))
    konst BASE_COLLECTIONS_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("collections"))
    konst BASE_RANGES_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("ranges"))
    konst BASE_JVM_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("jvm"))
    konst BASE_JVM_INTERNAL_PACKAGE = BASE_JVM_PACKAGE.child(Name.identifier("internal"))
    konst BASE_ANNOTATION_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("annotation"))
    konst BASE_INTERNAL_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("internal"))
    konst BASE_INTERNAL_IR_PACKAGE = BASE_INTERNAL_PACKAGE.child(Name.identifier("ir"))
    konst BASE_COROUTINES_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("coroutines"))
    konst BASE_ENUMS_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("enums"))
    konst BASE_CONTRACTS_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("contracts"))
    konst BASE_CONCURRENT_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("concurrent"))

    konst builtInsPackages = setOf(
        BASE_KOTLIN_PACKAGE,
        BASE_COLLECTIONS_PACKAGE,
        BASE_RANGES_PACKAGE,
        BASE_ANNOTATION_PACKAGE,
        BASE_REFLECT_PACKAGE,
        BASE_INTERNAL_PACKAGE,
        BASE_COROUTINES_PACKAGE
    )

    konst Nothing = "Nothing".baseId()
    konst Unit = "Unit".baseId()
    konst Any = "Any".baseId()
    konst Enum = "Enum".baseId()
    konst Annotation = "Annotation".baseId()
    konst Array = "Array".baseId()

    konst Boolean = "Boolean".baseId()
    konst Char = "Char".baseId()
    konst Byte = "Byte".baseId()
    konst Short = "Short".baseId()
    konst Int = "Int".baseId()
    konst Long = "Long".baseId()
    konst Float = "Float".baseId()
    konst Double = "Double".baseId()

    konst UByte = Byte.unsignedId()
    konst UShort = Short.unsignedId()
    konst UInt = Int.unsignedId()
    konst ULong = Long.unsignedId()

    konst CharSequence = "CharSequence".baseId()
    konst String = "String".baseId()
    konst Throwable = "Throwable".baseId()

    konst Cloneable = "Cloneable".baseId()

    konst KProperty = "KProperty".reflectId()
    konst KMutableProperty = "KMutableProperty".reflectId()
    konst KProperty0 = "KProperty0".reflectId()
    konst KMutableProperty0 = "KMutableProperty0".reflectId()
    konst KProperty1 = "KProperty1".reflectId()
    konst KMutableProperty1 = "KMutableProperty1".reflectId()
    konst KProperty2 = "KProperty2".reflectId()
    konst KMutableProperty2 = "KMutableProperty2".reflectId()
    konst KFunction = "KFunction".reflectId()
    konst KClass = "KClass".reflectId()
    konst KCallable = "KCallable".reflectId()

    konst Comparable = "Comparable".baseId()
    konst Number = "Number".baseId()

    konst Function = "Function".baseId()

    fun byName(name: String) = name.baseId()
    fun reflectByName(name: String) = name.reflectId()

    konst primitiveTypes = setOf(Boolean, Char, Byte, Short, Int, Long, Float, Double)

    konst primitiveArrayTypeByElementType = primitiveTypes.associateWith { id -> id.shortClassName.primitiveArrayId() }
    konst elementTypeByPrimitiveArrayType = primitiveArrayTypeByElementType.inverseMap()

    konst unsignedTypes = setOf(UByte, UShort, UInt, ULong)
    konst unsignedArrayTypeByElementType = unsignedTypes.associateWith { id -> id.shortClassName.primitiveArrayId() }
    konst elementTypeByUnsignedArrayType = unsignedArrayTypeByElementType.inverseMap()

    konst constantAllowedTypes = primitiveTypes + unsignedTypes + String

    konst Continuation = "Continuation".coroutinesId()

    @Suppress("FunctionName")
    fun FunctionN(n: Int): ClassId {
        return "Function$n".baseId()
    }

    @Suppress("FunctionName")
    fun SuspendFunctionN(n: Int): ClassId {
        return "SuspendFunction$n".coroutinesId()
    }

    @Suppress("FunctionName")
    fun KFunctionN(n: Int): ClassId {
        return "KFunction$n".reflectId()
    }

    @Suppress("FunctionName")
    fun KSuspendFunctionN(n: Int): ClassId {
        return "KSuspendFunction$n".reflectId()
    }

    konst Iterator = "Iterator".collectionsId()
    konst Iterable = "Iterable".collectionsId()
    konst Collection = "Collection".collectionsId()
    konst List = "List".collectionsId()
    konst ListIterator = "ListIterator".collectionsId()
    konst Set = "Set".collectionsId()
    konst Map = "Map".collectionsId()
    konst MutableIterator = "MutableIterator".collectionsId()
    konst CharIterator = "CharIterator".collectionsId()

    konst MutableIterable = "MutableIterable".collectionsId()
    konst MutableCollection = "MutableCollection".collectionsId()
    konst MutableList = "MutableList".collectionsId()
    konst MutableListIterator = "MutableListIterator".collectionsId()
    konst MutableSet = "MutableSet".collectionsId()
    konst MutableMap = "MutableMap".collectionsId()

    konst MapEntry = Map.createNestedClassId(Name.identifier("Entry"))
    konst MutableMapEntry = MutableMap.createNestedClassId(Name.identifier("MutableEntry"))

    konst Result = "Result".baseId()

    konst IntRange = "IntRange".rangesId()
    konst LongRange = "LongRange".rangesId()
    konst CharRange = "CharRange".rangesId()

    konst AnnotationRetention = "AnnotationRetention".annotationId()
    konst AnnotationTarget = "AnnotationTarget".annotationId()
    konst DeprecationLevel = "DeprecationLevel".baseId()

    konst EnumEntries = "EnumEntries".enumsId()

    object Annotations {
        konst Suppress = "Suppress".baseId()
        konst PublishedApi = "PublishedApi".baseId()
        konst SinceKotlin = "SinceKotlin".baseId()
        konst ExtensionFunctionType = "ExtensionFunctionType".baseId()
        konst ContextFunctionTypeParams = "ContextFunctionTypeParams".baseId()
        konst Deprecated = "Deprecated".baseId()
        konst DeprecatedSinceKotlin = "DeprecatedSinceKotlin".baseId()

        konst HidesMembers = "HidesMembers".internalId()
        konst DynamicExtension = "DynamicExtension".internalId()

        konst Retention = "Retention".annotationId()
        konst Target = "Target".annotationId()
        konst Repeatable = "Repeatable".annotationId()
        konst MustBeDocumented = "MustBeDocumented".annotationId()

        konst JvmStatic = "JvmStatic".jvmId()
        konst JvmName = "JvmName".jvmId()
        konst JvmField = "JvmField".jvmId()
        konst JvmDefault = "JvmDefault".jvmId()
        konst JvmRepeatable = "JvmRepeatable".jvmId()
        konst JvmRecord = "JvmRecord".jvmId()
        konst JvmVolatile = "Volatile".jvmId()
        konst Throws = "Throws".jvmId()

        konst Volatile = "Volatile".concurrentId()

        konst RawTypeAnnotation = "RawType".internalIrId()
        konst FlexibleNullability = "FlexibleNullability".internalIrId()
        konst FlexibleMutability = "FlexibleMutability".internalIrId()
        konst EnhancedNullability = "EnhancedNullability".jvmInternalId()

        konst InlineOnly = "InlineOnly".internalId()

        konst OnlyInputTypes = "OnlyInputTypes".internalId()

        konst RestrictsSuspension = "RestrictsSuspension".coroutinesId()

        konst WasExperimental = "WasExperimental".baseId()

        konst AccessibleLateinitPropertyLiteral = "AccessibleLateinitPropertyLiteral".internalId()

        object Java {
            konst Deprecated = "Deprecated".javaLangId()
            konst Repeatable = "Repeatable".javaAnnotationId()
            konst Retention = "Retention".javaAnnotationId()
            konst Documented = "Documented".javaAnnotationId()
            konst Target = "Target".javaAnnotationId()
            konst ElementType = "ElementType".javaAnnotationId()
            konst RetentionPolicy = "RetentionPolicy".javaAnnotationId()
        }

        object ParameterNames {
            konst konstue = Name.identifier("konstue")

            konst retentionValue = konstue
            konst targetAllowedTargets = Name.identifier("allowedTargets")

            konst sinceKotlinVersion = Name.identifier("version")

            konst deprecatedMessage = Name.identifier("message")
            konst deprecatedLevel = Name.identifier("level")

            konst deprecatedSinceKotlinWarningSince = Name.identifier("warningSince")
            konst deprecatedSinceKotlinErrorSince = Name.identifier("errorSince")
            konst deprecatedSinceKotlinHiddenSince = Name.identifier("hiddenSince")

            konst parameterNameName = StandardNames.NAME
        }
    }

    object Callables {
        konst suspend = "suspend".callableId(BASE_KOTLIN_PACKAGE)
        konst coroutineContext = "coroutineContext".callableId(BASE_COROUTINES_PACKAGE)

        konst clone = "clone".callableId(Cloneable)

        konst not = "not".callableId(Boolean)

        konst contract = "contract".callableId(BASE_CONTRACTS_PACKAGE)
    }

    object Java {
        konst Record = "Record".javaLangId()
    }

    object Collections {
        konst baseCollectionToMutableEquikonstent: Map<ClassId, ClassId> = mapOf(
            StandardClassIds.Iterable to StandardClassIds.MutableIterable,
            StandardClassIds.Iterator to StandardClassIds.MutableIterator,
            StandardClassIds.ListIterator to StandardClassIds.MutableListIterator,
            StandardClassIds.List to StandardClassIds.MutableList,
            StandardClassIds.Collection to StandardClassIds.MutableCollection,
            StandardClassIds.Set to StandardClassIds.MutableSet,
            StandardClassIds.Map to StandardClassIds.MutableMap,
            StandardClassIds.MapEntry to StandardClassIds.MutableMapEntry
        )

        konst mutableCollectionToBaseCollection: Map<ClassId, ClassId> =
            baseCollectionToMutableEquikonstent.entries.associateBy({ it.konstue }) { it.key }
    }
}

private fun String.baseId() = ClassId(StandardClassIds.BASE_KOTLIN_PACKAGE, Name.identifier(this))
private fun ClassId.unsignedId() = ClassId(StandardClassIds.BASE_KOTLIN_PACKAGE, Name.identifier("U" + shortClassName.identifier))
private fun String.reflectId() = ClassId(StandardClassIds.BASE_REFLECT_PACKAGE, Name.identifier(this))
private fun Name.primitiveArrayId() = ClassId(StandardClassIds.Array.packageFqName, Name.identifier(identifier + StandardClassIds.Array.shortClassName.identifier))
private fun String.collectionsId() = ClassId(StandardClassIds.BASE_COLLECTIONS_PACKAGE, Name.identifier(this))
private fun String.rangesId() = ClassId(StandardClassIds.BASE_RANGES_PACKAGE, Name.identifier(this))
private fun String.annotationId() = ClassId(StandardClassIds.BASE_ANNOTATION_PACKAGE, Name.identifier(this))
private fun String.jvmId() = ClassId(StandardClassIds.BASE_JVM_PACKAGE, Name.identifier(this))
private fun String.jvmInternalId() = ClassId(StandardClassIds.BASE_JVM_INTERNAL_PACKAGE, Name.identifier(this))
private fun String.internalId() = ClassId(StandardClassIds.BASE_INTERNAL_PACKAGE, Name.identifier(this))
private fun String.internalIrId() = ClassId(StandardClassIds.BASE_INTERNAL_IR_PACKAGE, Name.identifier(this))
private fun String.coroutinesId() = ClassId(StandardClassIds.BASE_COROUTINES_PACKAGE, Name.identifier(this))
private fun String.enumsId() = ClassId(StandardClassIds.BASE_ENUMS_PACKAGE, Name.identifier(this))
private fun String.concurrentId() = ClassId(StandardClassIds.BASE_CONCURRENT_PACKAGE, Name.identifier(this))

private fun String.callableId(packageName: FqName) = CallableId(packageName, Name.identifier(this))
private fun String.callableId(classId: ClassId) = CallableId(classId, Name.identifier(this))

private konst JAVA_LANG_PACKAGE = FqName("java.lang")
private konst JAVA_LANG_ANNOTATION_PACKAGE = JAVA_LANG_PACKAGE.child(Name.identifier("annotation"))

private fun String.javaLangId() = ClassId(JAVA_LANG_PACKAGE, Name.identifier(this))
private fun String.javaAnnotationId() = ClassId(JAVA_LANG_ANNOTATION_PACKAGE, Name.identifier(this))

private fun <K, V> Map<K, V>.inverseMap(): Map<V, K> = entries.associate { (k, v) -> v to k }
