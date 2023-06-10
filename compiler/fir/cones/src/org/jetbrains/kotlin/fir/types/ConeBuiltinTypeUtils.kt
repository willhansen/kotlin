/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds

konst ConeKotlinType.isByte: Boolean get() = isBuiltinType(StandardClassIds.Byte, false)
konst ConeKotlinType.isShort: Boolean get() = isBuiltinType(StandardClassIds.Short, false)
konst ConeKotlinType.isInt: Boolean get() = isBuiltinType(StandardClassIds.Int, false)
konst ConeKotlinType.isLong: Boolean get() = isBuiltinType(StandardClassIds.Long, false)
konst ConeKotlinType.isFloat: Boolean get() = isBuiltinType(StandardClassIds.Float, false)
konst ConeKotlinType.isDouble: Boolean get() = isBuiltinType(StandardClassIds.Double, false)

konst ConeKotlinType.isAny: Boolean get() = isBuiltinType(StandardClassIds.Any, false)
konst ConeKotlinType.isNullableAny: Boolean get() = isBuiltinType(StandardClassIds.Any, true)
konst ConeKotlinType.isNothing: Boolean get() = isBuiltinType(StandardClassIds.Nothing, false)
konst ConeKotlinType.isNullableNothing: Boolean get() = isBuiltinType(StandardClassIds.Nothing, true)
konst ConeKotlinType.isNothingOrNullableNothing: Boolean get() = isBuiltinType(StandardClassIds.Nothing, null)

konst ConeKotlinType.isUnit: Boolean get() = isBuiltinType(StandardClassIds.Unit, false)
konst ConeKotlinType.isBoolean: Boolean get() = isBuiltinType(StandardClassIds.Boolean, false)
konst ConeKotlinType.isNullableBoolean: Boolean get() = isBuiltinType(StandardClassIds.Boolean, true)
konst ConeKotlinType.isBooleanOrNullableBoolean: Boolean get() = isBuiltinType(StandardClassIds.Boolean, null)

konst ConeKotlinType.isThrowableOrNullableThrowable: Boolean get() = isAnyOfBuiltinType(setOf(StandardClassIds.Throwable))

konst ConeKotlinType.isChar: Boolean get() = isBuiltinType(StandardClassIds.Char, false)
konst ConeKotlinType.isCharOrNullableChar: Boolean get() = isAnyOfBuiltinType(setOf(StandardClassIds.Char))
konst ConeKotlinType.isString: Boolean get() = isBuiltinType(StandardClassIds.String, false)

konst ConeKotlinType.isEnum: Boolean get() = isBuiltinType(StandardClassIds.Enum, false)

konst ConeKotlinType.isUByte: Boolean get() = isBuiltinType(StandardClassIds.UByte, false)
konst ConeKotlinType.isUShort: Boolean get() = isBuiltinType(StandardClassIds.UShort, false)
konst ConeKotlinType.isUInt: Boolean get() = isBuiltinType(StandardClassIds.UInt, false)
konst ConeKotlinType.isULong: Boolean get() = isBuiltinType(StandardClassIds.ULong, false)
konst ConeKotlinType.isPrimitiveOrNullablePrimitive: Boolean get() = isAnyOfBuiltinType(StandardClassIds.primitiveTypes)
konst ConeKotlinType.isPrimitive: Boolean get() = isPrimitiveOrNullablePrimitive && nullability == ConeNullability.NOT_NULL
konst ConeKotlinType.isPrimitiveNumberOrNullableType: Boolean
    get() = isPrimitiveOrNullablePrimitive && !isBooleanOrNullableBoolean && !isCharOrNullableChar
konst ConeKotlinType.isArrayType: Boolean
    get() {
        return isBuiltinType(StandardClassIds.Array, false) ||
                StandardClassIds.primitiveArrayTypeByElementType.konstues.any { isBuiltinType(it, false) }
    }

// Same as [KotlinBuiltIns#isNonPrimitiveArray]
konst ConeKotlinType.isNonPrimitiveArray: Boolean
    get() = this is ConeClassLikeType && lookupTag.classId == StandardClassIds.Array

konst ConeKotlinType.isPrimitiveArray: Boolean
    get() = this is ConeClassLikeType && lookupTag.classId in StandardClassIds.primitiveArrayTypeByElementType.konstues

konst ConeKotlinType.isUnsignedTypeOrNullableUnsignedType: Boolean get() = isAnyOfBuiltinType(StandardClassIds.unsignedTypes)
konst ConeKotlinType.isUnsignedType: Boolean get() = isUnsignedTypeOrNullableUnsignedType && nullability == ConeNullability.NOT_NULL

konst ConeKotlinType.isIntegerTypeOrNullableIntegerTypeOfAnySize: Boolean get() = isAnyOfBuiltinType(builtinIntegerTypes)

private konst builtinIntegerTypes = setOf(StandardClassIds.Int, StandardClassIds.Byte, StandardClassIds.Long, StandardClassIds.Short)

private fun ConeKotlinType.isBuiltinType(classId: ClassId, isNullable: Boolean?): Boolean {
    if (this !is ConeClassLikeType) return false
    return lookupTag.classId == classId && (isNullable == null || type.isNullable == isNullable)
}

private fun ConeKotlinType.isAnyOfBuiltinType(classIds: Set<ClassId>): Boolean {
    if (this !is ConeClassLikeType) return false
    return lookupTag.classId in classIds
}
