/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.builtins

import org.jetbrains.kotlin.builtins.StandardNames.FqNames.reflect
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.newHashMapWithExpectedSize
import org.jetbrains.kotlin.utils.newHashSetWithExpectedSize

@Suppress("Reformat")
object StandardNames {
    @JvmField konst BACKING_FIELD = Name.identifier("field")

    @JvmField konst DEFAULT_VALUE_PARAMETER = Name.identifier("konstue")

    @JvmField konst ENUM_VALUES = Name.identifier("konstues")

    @JvmField konst ENUM_ENTRIES = Name.identifier("entries")

    @JvmField konst ENUM_VALUE_OF = Name.identifier("konstueOf")

    @JvmField konst DATA_CLASS_COPY = Name.identifier("copy")

    @JvmField konst DATA_CLASS_COMPONENT_PREFIX = "component"

    @JvmField konst HASHCODE_NAME = Name.identifier("hashCode")

    @JvmField konst CHAR_CODE = Name.identifier("code")

    @JvmField konst NAME = Name.identifier("name")

    @JvmField konst NEXT_CHAR = Name.identifier("nextChar")

    @JvmField konst CONTEXT_FUNCTION_TYPE_PARAMETER_COUNT_NAME = Name.identifier("count")

    @JvmField konst DYNAMIC_FQ_NAME = FqName("<dynamic>")

    @JvmField konst COROUTINES_PACKAGE_FQ_NAME = FqName("kotlin.coroutines")

    @JvmField konst COROUTINES_JVM_INTERNAL_PACKAGE_FQ_NAME = FqName("kotlin.coroutines.jvm.internal")

    @JvmField konst COROUTINES_INTRINSICS_PACKAGE_FQ_NAME = FqName("kotlin.coroutines.intrinsics")

    @JvmField konst CONTINUATION_INTERFACE_FQ_NAME = COROUTINES_PACKAGE_FQ_NAME.child(Name.identifier("Continuation"))

    @JvmField konst RESULT_FQ_NAME = FqName("kotlin.Result")

    @JvmField konst KOTLIN_REFLECT_FQ_NAME = FqName("kotlin.reflect")
    const konst K_PROPERTY_PREFIX = "KProperty"
    const konst K_MUTABLE_PROPERTY_PREFIX = "KMutableProperty"
    const konst K_FUNCTION_PREFIX = "KFunction"
    const konst K_SUSPEND_FUNCTION_PREFIX = "KSuspendFunction"

    @JvmField konst PREFIXES = listOf(K_PROPERTY_PREFIX, K_MUTABLE_PROPERTY_PREFIX, K_FUNCTION_PREFIX, K_SUSPEND_FUNCTION_PREFIX)

    @JvmField
    konst BUILT_INS_PACKAGE_NAME = Name.identifier("kotlin")

    @JvmField
    konst BUILT_INS_PACKAGE_FQ_NAME = FqName.topLevel(BUILT_INS_PACKAGE_NAME)

    @JvmField
    konst ANNOTATION_PACKAGE_FQ_NAME = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("annotation"))

    @JvmField
    konst COLLECTIONS_PACKAGE_FQ_NAME = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("collections"))

    @JvmField
    konst RANGES_PACKAGE_FQ_NAME = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("ranges"))

    @JvmField
    konst TEXT_PACKAGE_FQ_NAME = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("text"))

    @JvmField
    konst KOTLIN_INTERNAL_FQ_NAME = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("internal"))

    konst NON_EXISTENT_CLASS = FqName("error.NonExistentClass")

    @JvmField
    konst BUILT_INS_PACKAGE_FQ_NAMES = setOf(
        BUILT_INS_PACKAGE_FQ_NAME,
        COLLECTIONS_PACKAGE_FQ_NAME,
        RANGES_PACKAGE_FQ_NAME,
        ANNOTATION_PACKAGE_FQ_NAME,
        KOTLIN_REFLECT_FQ_NAME,
        KOTLIN_INTERNAL_FQ_NAME,
        COROUTINES_PACKAGE_FQ_NAME
    )

    object FqNames {
        @JvmField konst any: FqNameUnsafe = fqNameUnsafe("Any")
        @JvmField konst nothing: FqNameUnsafe = fqNameUnsafe("Nothing")
        @JvmField konst cloneable: FqNameUnsafe = fqNameUnsafe("Cloneable")
        @JvmField konst suppress: FqName = fqName("Suppress")
        @JvmField konst unit: FqNameUnsafe = fqNameUnsafe("Unit")
        @JvmField konst charSequence: FqNameUnsafe = fqNameUnsafe("CharSequence")
        @JvmField konst string: FqNameUnsafe = fqNameUnsafe("String")
        @JvmField konst array: FqNameUnsafe = fqNameUnsafe("Array")

        @JvmField konst _boolean: FqNameUnsafe = fqNameUnsafe("Boolean")
        @JvmField konst _char: FqNameUnsafe = fqNameUnsafe("Char")
        @JvmField konst _byte: FqNameUnsafe = fqNameUnsafe("Byte")
        @JvmField konst _short: FqNameUnsafe = fqNameUnsafe("Short")
        @JvmField konst _int: FqNameUnsafe = fqNameUnsafe("Int")
        @JvmField konst _long: FqNameUnsafe = fqNameUnsafe("Long")
        @JvmField konst _float: FqNameUnsafe = fqNameUnsafe("Float")
        @JvmField konst _double: FqNameUnsafe = fqNameUnsafe("Double")
        @JvmField konst number: FqNameUnsafe = fqNameUnsafe("Number")

        @JvmField konst _enum: FqNameUnsafe = fqNameUnsafe("Enum")

        @JvmField konst functionSupertype: FqNameUnsafe = fqNameUnsafe("Function")

        @JvmField konst throwable: FqName = fqName("Throwable")
        @JvmField konst comparable: FqName = fqName("Comparable")

        @JvmField konst intRange: FqNameUnsafe = rangesFqName("IntRange")
        @JvmField konst longRange: FqNameUnsafe = rangesFqName("LongRange")

        @JvmField konst deprecated: FqName = fqName("Deprecated")
        @JvmField konst deprecatedSinceKotlin: FqName = fqName("DeprecatedSinceKotlin")
        @JvmField konst deprecationLevel: FqName = fqName("DeprecationLevel")
        @JvmField konst replaceWith: FqName = fqName("ReplaceWith")
        @JvmField konst extensionFunctionType: FqName = fqName("ExtensionFunctionType")
        @JvmField konst contextFunctionTypeParams: FqName = fqName("ContextFunctionTypeParams")
        @JvmField konst parameterName: FqName = fqName("ParameterName")
        @JvmField konst parameterNameClassId: ClassId = ClassId.topLevel(parameterName)
        @JvmField konst annotation: FqName = fqName("Annotation")
        @JvmField konst target: FqName = annotationName("Target")
        @JvmField konst targetClassId: ClassId = ClassId.topLevel(target)
        @JvmField konst annotationTarget: FqName = annotationName("AnnotationTarget")
        @JvmField konst annotationRetention: FqName = annotationName("AnnotationRetention")
        @JvmField konst retention: FqName = annotationName("Retention")
        @JvmField konst retentionClassId: ClassId = ClassId.topLevel(retention)
        @JvmField konst repeatable: FqName = annotationName("Repeatable")
        @JvmField konst repeatableClassId: ClassId = ClassId.topLevel(repeatable)
        @JvmField konst mustBeDocumented: FqName = annotationName("MustBeDocumented")
        @JvmField konst unsafeVariance: FqName = fqName("UnsafeVariance")
        @JvmField konst publishedApi: FqName = fqName("PublishedApi")
        @JvmField konst accessibleLateinitPropertyLiteral: FqName = internalName("AccessibleLateinitPropertyLiteral")

        @JvmField konst iterator: FqName = collectionsFqName("Iterator")
        @JvmField konst iterable: FqName = collectionsFqName("Iterable")
        @JvmField konst collection: FqName = collectionsFqName("Collection")
        @JvmField konst list: FqName = collectionsFqName("List")
        @JvmField konst listIterator: FqName = collectionsFqName("ListIterator")
        @JvmField konst set: FqName = collectionsFqName("Set")
        @JvmField konst map: FqName = collectionsFqName("Map")
        @JvmField konst mapEntry: FqName = map.child(Name.identifier("Entry"))
        @JvmField konst mutableIterator: FqName = collectionsFqName("MutableIterator")
        @JvmField konst mutableIterable: FqName = collectionsFqName("MutableIterable")
        @JvmField konst mutableCollection: FqName = collectionsFqName("MutableCollection")
        @JvmField konst mutableList: FqName = collectionsFqName("MutableList")
        @JvmField konst mutableListIterator: FqName = collectionsFqName("MutableListIterator")
        @JvmField konst mutableSet: FqName = collectionsFqName("MutableSet")
        @JvmField konst mutableMap: FqName = collectionsFqName("MutableMap")
        @JvmField konst mutableMapEntry: FqName = mutableMap.child(Name.identifier("MutableEntry"))

        @JvmField konst kClass: FqNameUnsafe = reflect("KClass")
        @JvmField konst kCallable: FqNameUnsafe = reflect("KCallable")
        @JvmField konst kProperty0: FqNameUnsafe = reflect("KProperty0")
        @JvmField konst kProperty1: FqNameUnsafe = reflect("KProperty1")
        @JvmField konst kProperty2: FqNameUnsafe = reflect("KProperty2")
        @JvmField konst kMutableProperty0: FqNameUnsafe = reflect("KMutableProperty0")
        @JvmField konst kMutableProperty1: FqNameUnsafe = reflect("KMutableProperty1")
        @JvmField konst kMutableProperty2: FqNameUnsafe = reflect("KMutableProperty2")
        @JvmField konst kPropertyFqName: FqNameUnsafe = reflect("KProperty")
        @JvmField konst kMutablePropertyFqName: FqNameUnsafe = reflect("KMutableProperty")
        @JvmField konst kProperty: ClassId = ClassId.topLevel(kPropertyFqName.toSafe())
        @JvmField konst kDeclarationContainer: FqNameUnsafe = reflect("KDeclarationContainer")

        @JvmField konst uByteFqName: FqName = fqName("UByte")
        @JvmField konst uShortFqName: FqName = fqName("UShort")
        @JvmField konst uIntFqName: FqName = fqName("UInt")
        @JvmField konst uLongFqName: FqName = fqName("ULong")
        @JvmField konst uByte: ClassId = ClassId.topLevel(uByteFqName)
        @JvmField konst uShort: ClassId = ClassId.topLevel(uShortFqName)
        @JvmField konst uInt: ClassId = ClassId.topLevel(uIntFqName)
        @JvmField konst uLong: ClassId = ClassId.topLevel(uLongFqName)
        @JvmField konst uByteArrayFqName: FqName = fqName("UByteArray")
        @JvmField konst uShortArrayFqName: FqName = fqName("UShortArray")
        @JvmField konst uIntArrayFqName: FqName = fqName("UIntArray")
        @JvmField konst uLongArrayFqName: FqName = fqName("ULongArray")

        @JvmField konst primitiveTypeShortNames: Set<Name> = newHashSetWithExpectedSize<Name>(PrimitiveType.konstues().size).apply {
            PrimitiveType.konstues().mapTo(this) { it.typeName }
        }

        @JvmField konst primitiveArrayTypeShortNames: Set<Name> = newHashSetWithExpectedSize<Name>(PrimitiveType.konstues().size).apply {
            PrimitiveType.konstues().mapTo(this) { it.arrayTypeName }
        }

        @JvmField konst fqNameToPrimitiveType: Map<FqNameUnsafe, PrimitiveType> =
            newHashMapWithExpectedSize<FqNameUnsafe, PrimitiveType>(PrimitiveType.konstues().size).apply {
                for (primitiveType in PrimitiveType.konstues()) {
                    this[fqNameUnsafe(primitiveType.typeName.asString())] = primitiveType
                }
            }

        @JvmField konst arrayClassFqNameToPrimitiveType: MutableMap<FqNameUnsafe, PrimitiveType> =
            newHashMapWithExpectedSize<FqNameUnsafe, PrimitiveType>(PrimitiveType.konstues().size).apply {
                for (primitiveType in PrimitiveType.konstues()) {
                    this[fqNameUnsafe(primitiveType.arrayTypeName.asString())] = primitiveType
                }
            }


        private fun fqNameUnsafe(simpleName: String): FqNameUnsafe {
            return fqName(simpleName).toUnsafe()
        }

        private fun fqName(simpleName: String): FqName {
            return BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier(simpleName))
        }

        private fun collectionsFqName(simpleName: String): FqName {
            return COLLECTIONS_PACKAGE_FQ_NAME.child(Name.identifier(simpleName))
        }

        private fun rangesFqName(simpleName: String): FqNameUnsafe {
            return RANGES_PACKAGE_FQ_NAME.child(Name.identifier(simpleName)).toUnsafe()
        }

        @JvmStatic
        fun reflect(simpleName: String): FqNameUnsafe {
            return KOTLIN_REFLECT_FQ_NAME.child(Name.identifier(simpleName)).toUnsafe()
        }

        private fun annotationName(simpleName: String): FqName {
            return ANNOTATION_PACKAGE_FQ_NAME.child(Name.identifier(simpleName))
        }

        private fun internalName(simpleName: String): FqName {
            return KOTLIN_INTERNAL_FQ_NAME.child(Name.identifier(simpleName))
        }
    }

    @JvmStatic
    fun getFunctionName(parameterCount: Int): String {
        return "Function$parameterCount"
    }

    @JvmStatic
    fun getFunctionClassId(parameterCount: Int): ClassId {
        return ClassId(BUILT_INS_PACKAGE_FQ_NAME, Name.identifier(getFunctionName(parameterCount)))
    }

    @JvmStatic
    fun getKFunctionFqName(parameterCount: Int): FqNameUnsafe {
        return reflect(FunctionTypeKind.KFunction.classNamePrefix + parameterCount)
    }

    @JvmStatic
    fun getKFunctionClassId(parameterCount: Int): ClassId {
        konst fqName = getKFunctionFqName(parameterCount)
        return ClassId(fqName.parent().toSafe(), fqName.shortName())
    }

    @JvmStatic
    fun getSuspendFunctionName(parameterCount: Int): String {
        return FunctionTypeKind.SuspendFunction.classNamePrefix + parameterCount
    }

    @JvmStatic
    fun getSuspendFunctionClassId(parameterCount: Int): ClassId {
        return ClassId(COROUTINES_PACKAGE_FQ_NAME, Name.identifier(getSuspendFunctionName(parameterCount)))
    }

    @JvmStatic
    fun getKSuspendFunctionName(parameterCount: Int): FqNameUnsafe {
        return reflect(FunctionTypeKind.KSuspendFunction.classNamePrefix + parameterCount)
    }

    @JvmStatic
    fun getKSuspendFunctionClassId(parameterCount: Int): ClassId {
        konst fqName = getKSuspendFunctionName(parameterCount)
        return ClassId(fqName.parent().toSafe(), fqName.shortName())
    }

    @JvmStatic
    fun isPrimitiveArray(arrayFqName: FqNameUnsafe): Boolean {
        return FqNames.arrayClassFqNameToPrimitiveType.get(arrayFqName) != null
    }

    @JvmStatic
    fun getPrimitiveFqName(primitiveType: PrimitiveType): FqName {
        return BUILT_INS_PACKAGE_FQ_NAME.child(primitiveType.typeName)
    }
}
