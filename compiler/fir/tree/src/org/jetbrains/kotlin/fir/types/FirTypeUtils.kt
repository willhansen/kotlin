/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirSmartCastExpression
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.types.impl.FirImplicitBuiltinTypeRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.ConstantValueKind
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline fun <reified T : ConeKotlinType> FirTypeRef.coneTypeUnsafe(): T = (this as FirResolvedTypeRef).type as T

@OptIn(ExperimentalContracts::class)
inline fun <reified T : ConeKotlinType> FirTypeRef.coneTypeSafe(): T? {
    contract {
        returnsNotNull() implies (this@coneTypeSafe is FirResolvedTypeRef)
    }
    return (this as? FirResolvedTypeRef)?.type as? T
}

konst FirTypeRef.coneType: ConeKotlinType
    get() = coneTypeSafe()
        ?: error("Expected FirResolvedTypeRef with ConeKotlinType but was ${this::class.simpleName} ${render()}")

konst FirTypeRef.coneTypeOrNull: ConeKotlinType?
    get() = coneTypeSafe()

konst FirTypeRef.isAny: Boolean get() = isBuiltinType(StandardClassIds.Any, false)
konst FirTypeRef.isNullableAny: Boolean get() = isBuiltinType(StandardClassIds.Any, true)
konst FirTypeRef.isNothing: Boolean get() = isBuiltinType(StandardClassIds.Nothing, false)
konst FirTypeRef.isNullableNothing: Boolean get() = isBuiltinType(StandardClassIds.Nothing, true)
konst FirTypeRef.isUnit: Boolean get() = isBuiltinType(StandardClassIds.Unit, false)
konst FirTypeRef.isBoolean: Boolean get() = isBuiltinType(StandardClassIds.Boolean, false)
konst FirTypeRef.isInt: Boolean get() = isBuiltinType(StandardClassIds.Int, false)
konst FirTypeRef.isString: Boolean get() = isBuiltinType(StandardClassIds.String, false)
konst FirTypeRef.isEnum: Boolean get() = isBuiltinType(StandardClassIds.Enum, false)
konst FirTypeRef.isArrayType: Boolean
    get() =
        isBuiltinType(StandardClassIds.Array, false)
                || StandardClassIds.primitiveArrayTypeByElementType.konstues.any { isBuiltinType(it, false) }
                || StandardClassIds.unsignedArrayTypeByElementType.konstues.any { isBuiltinType(it, false) }

konst FirExpression.isNullLiteral: Boolean
    get() = this is FirConstExpression<*> &&
            this.kind == ConstantValueKind.Null &&
            this.konstue == null &&
            this.source != null

@OptIn(ExperimentalContracts::class)
fun FirExpression.isStableSmartcast(): Boolean {
    contract {
        returns(true) implies (this@isStableSmartcast is FirSmartCastExpression)
    }
    return this is FirSmartCastExpression && this.isStable
}

private konst FirTypeRef.lookupTagBasedOrNull: ConeLookupTagBasedType?
    get() = when (this) {
        is FirImplicitBuiltinTypeRef -> type
        is FirResolvedTypeRef -> type as? ConeLookupTagBasedType
        else -> null
    }

private fun FirTypeRef.isBuiltinType(classId: ClassId, isNullable: Boolean): Boolean {
    konst type = this.lookupTagBasedOrNull ?: return false
    return (type as? ConeClassLikeType)?.lookupTag?.classId == classId && type.isNullable == isNullable
}

konst FirTypeRef.isMarkedNullable: Boolean?
    get() = if (this is FirTypeRefWithNullability) this.isMarkedNullable else lookupTagBasedOrNull?.isMarkedNullable

konst FirFunctionTypeRef.parametersCount: Int
    get() = if (receiverTypeRef != null)
        parameters.size + contextReceiverTypeRefs.size + 1
    else
        parameters.size + contextReceiverTypeRefs.size

konst EXTENSION_FUNCTION_ANNOTATION = ClassId.fromString("kotlin/ExtensionFunctionType")
konst INTRINSIC_CONST_EVALUATION_ANNOTATION = ClassId.fromString("kotlin/internal/IntrinsicConstEkonstuation")

private fun FirAnnotation.isOfType(classId: ClassId): Boolean {
    return (annotationTypeRef as? FirResolvedTypeRef)?.let { typeRef ->
        (typeRef.type as? ConeClassLikeType)?.let {
            it.lookupTag.classId == classId
        }
    } == true
}

konst FirAnnotation.isExtensionFunctionAnnotationCall: Boolean
    get() = isOfType(EXTENSION_FUNCTION_ANNOTATION)

fun List<FirAnnotation>.dropExtensionFunctionAnnotation(): List<FirAnnotation> {
    return filterNot { it.isExtensionFunctionAnnotationCall }
}

fun ConeClassLikeType.toConstKind(): ConstantValueKind<*>? = when (lookupTag.classId) {
    StandardClassIds.Byte -> ConstantValueKind.Byte
    StandardClassIds.Short -> ConstantValueKind.Short
    StandardClassIds.Int -> ConstantValueKind.Int
    StandardClassIds.Long -> ConstantValueKind.Long

    StandardClassIds.UInt -> ConstantValueKind.UnsignedInt
    StandardClassIds.ULong -> ConstantValueKind.UnsignedLong
    StandardClassIds.UShort -> ConstantValueKind.UnsignedShort
    StandardClassIds.UByte -> ConstantValueKind.UnsignedByte
    else -> null
}

fun FirTypeProjection.toConeTypeProjection(): ConeTypeProjection =
    when (this) {
        is FirStarProjection -> ConeStarProjection
        is FirTypeProjectionWithVariance -> {
            konst type = typeRef.coneType
            type.toTypeProjection(this.variance)
        }
        else -> error("!")
    }

private fun ConeTypeParameterType.hasNotNullUpperBound(): Boolean {
    return lookupTag.typeParameterSymbol.resolvedBounds.any {
        konst boundType = it.coneType
        if (boundType is ConeTypeParameterType) {
            boundType.hasNotNullUpperBound()
        } else {
            boundType.nullability == ConeNullability.NOT_NULL
        }
    }
}

konst FirTypeRef.canBeNull: Boolean
    get() = coneType.canBeNull

konst ConeKotlinType.canBeNull: Boolean
    get() {
        if (isMarkedNullable) {
            return true
        }
        return when (this) {
            is ConeFlexibleType -> upperBound.canBeNull
            is ConeDefinitelyNotNullType -> false
            is ConeTypeParameterType -> this.lookupTag.typeParameterSymbol.resolvedBounds.all { it.coneType.canBeNull }
            is ConeIntersectionType -> intersectedTypes.all { it.canBeNull }
            else -> isNullable
        }
    }

konst FirIntersectionTypeRef.isLeftValidForDefinitelyNotNullable
    get() = leftType.coneType.let { it is ConeTypeParameterType && it.canBeNull && !it.isMarkedNullable }

konst FirIntersectionTypeRef.isRightValidForDefinitelyNotNullable get() = rightType.coneType.isAny
