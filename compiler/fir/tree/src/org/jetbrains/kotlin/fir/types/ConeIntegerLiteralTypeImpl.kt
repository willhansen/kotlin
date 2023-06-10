/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.isLong
import org.jetbrains.kotlin.fir.isULong
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralTypeExtensions.approximateIntegerLiteralBounds
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralTypeExtensions.createClassLikeType
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralTypeExtensions.createSupertypeList
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralTypeExtensions.getApproximatedTypeImpl
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralTypeExtensions.withNullabilityAndAttributes
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.model.SimpleTypeMarker

class ConeIntegerLiteralConstantTypeImpl(
    konstue: Long,
    override konst possibleTypes: Collection<ConeClassLikeType>,
    isUnsigned: Boolean,
    nullability: ConeNullability
) : ConeIntegerLiteralConstantType(konstue, isUnsigned, nullability) {
    override konst supertypes: List<ConeClassLikeType> by lazy {
        createSupertypeList(this)
    }

    override fun getApproximatedType(expectedType: ConeKotlinType?): ConeClassLikeType {
        return getApproximatedTypeImpl(expectedType)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    companion object {
        fun create(
            konstue: Long,
            isUnsigned: Boolean,
            isTypePresent: (ConeClassLikeType) -> Boolean,
            nullability: ConeNullability = ConeNullability.NOT_NULL
        ): ConeSimpleKotlinType {
            konst possibleTypes = mutableListOf<ConeClassLikeType>()

            fun checkBoundsAndAddPossibleType(classId: ClassId, range: LongRange) {
                if (konstue in range) {
                    possibleTypes.add(createType(classId))
                }
            }

            fun addSignedPossibleTypes() {
                checkBoundsAndAddPossibleType(StandardClassIds.Int, INT_RANGE)
                possibleTypes += createType(StandardClassIds.Long)
                checkBoundsAndAddPossibleType(StandardClassIds.Byte, BYTE_RANGE)
                checkBoundsAndAddPossibleType(StandardClassIds.Short, SHORT_RANGE)
            }

            fun addUnsignedPossibleType() {
                checkBoundsAndAddPossibleType(StandardClassIds.UInt, UINT_RANGE)
                possibleTypes += createType(StandardClassIds.ULong)
                checkBoundsAndAddPossibleType(StandardClassIds.UByte, UBYTE_RANGE)
                checkBoundsAndAddPossibleType(StandardClassIds.UShort, USHORT_RANGE)
            }

            if (isUnsigned) {
                addUnsignedPossibleType()
                if (possibleTypes.any { !isTypePresent(it) }) {
                    return ConeErrorType(ConeSimpleDiagnostic("Unsigned integers need stdlib", DiagnosticKind.UnsignedNumbersAreNotPresent))
                }
            } else {
                addSignedPossibleTypes()
            }
            return if (possibleTypes.size == 1) {
                possibleTypes.single().withNullabilityAndAttributes(nullability, ConeAttributes.Empty).also {
                    if (AbstractTypeChecker.RUN_SLOW_ASSERTIONS) {
                        assert(it.isLong() || it.isULong())
                    }
                }
            } else {
                ConeIntegerLiteralConstantTypeImpl(konstue, possibleTypes, isUnsigned, nullability)
            }
        }

        private fun createType(classId: ClassId): ConeClassLikeType {
            return ConeClassLikeTypeImpl(classId.toLookupTag(), emptyArray(), false)
        }

        private konst INT_RANGE = Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
        private konst BYTE_RANGE = Byte.MIN_VALUE.toLong()..Byte.MAX_VALUE.toLong()
        private konst SHORT_RANGE = Short.MIN_VALUE.toLong()..Short.MAX_VALUE.toLong()

        private konst UBYTE_RANGE = UByte.MIN_VALUE.toLong()..UByte.MAX_VALUE.toLong()
        private konst USHORT_RANGE = UShort.MIN_VALUE.toLong()..UShort.MAX_VALUE.toLong()
        private konst UINT_RANGE = UInt.MIN_VALUE.toLong()..UInt.MAX_VALUE.toLong()
    }
}

class ConeIntegerConstantOperatorTypeImpl(
    isUnsigned: Boolean,
    nullability: ConeNullability
) : ConeIntegerConstantOperatorType(isUnsigned, nullability) {
    override konst possibleTypes: Collection<ConeClassLikeType> = when (isUnsigned) {
        false -> setOf(
            createClassLikeType(StandardClassIds.Int),
            createClassLikeType(StandardClassIds.Long),
        )
        true -> setOf(
            createClassLikeType(StandardClassIds.UInt),
            createClassLikeType(StandardClassIds.ULong),
        )
    }

    override konst supertypes: List<ConeClassLikeType> by lazy {
        createSupertypeList(this)
    }

    override fun getApproximatedType(expectedType: ConeKotlinType?): ConeClassLikeType {
        return getApproximatedTypeImpl(expectedType)
    }
}

/**
 * This methods detects common super type only for special rules for integer literal types
 * If it returns null then CST will be found by regular rules using real supertypes
 *   of integer literal types
 */
fun ConeIntegerLiteralType.Companion.findCommonSuperType(types: Collection<SimpleTypeMarker>): SimpleTypeMarker? {
    return ConeIntegerLiteralTypeExtensions.findCommonSuperType(types)
}

fun ConeKotlinType.approximateIntegerLiteralType(expectedType: ConeKotlinType? = null): ConeKotlinType {
    return when (this) {
        is ConeIntegerLiteralType -> getApproximatedType(expectedType)
        is ConeFlexibleType -> approximateIntegerLiteralBounds(expectedType)
        else -> this
    }
}

private object ConeIntegerLiteralTypeExtensions {
    fun createSupertypeList(type: ConeIntegerLiteralType): List<ConeClassLikeType> {
        return listOf(
            createClassLikeType(StandardClassIds.Number),
            ConeClassLikeTypeImpl(StandardClassIds.Comparable.toLookupTag(), arrayOf(ConeKotlinTypeProjectionIn(type)), false)
        )
    }

    fun createClassLikeType(classId: ClassId): ConeClassLikeType {
        return ConeClassLikeTypeImpl(classId.toLookupTag(), emptyArray(), false)
    }

    fun ConeIntegerLiteralType.getApproximatedTypeImpl(expectedType: ConeKotlinType?): ConeClassLikeType {
        konst expectedTypeForApproximation = (expectedType?.lowerBoundIfFlexible() as? ConeClassLikeType)
            ?.withNullabilityAndAttributes(ConeNullability.NOT_NULL, ConeAttributes.Empty)
        konst approximatedType = when (expectedTypeForApproximation) {
            null, !in possibleTypes -> possibleTypes.first()
            else -> expectedTypeForApproximation
        }
        return approximatedType.withNullabilityAndAttributes(nullability, attributes)
    }


    fun findCommonSuperType(types: Collection<SimpleTypeMarker>): SimpleTypeMarker? {
        if (types.isEmpty()) return null
        @Suppress("UNCHECKED_CAST")
        return types.reduce { left: SimpleTypeMarker?, right: SimpleTypeMarker? -> commonSuperType(left, right) }
    }

    private fun commonSuperType(left: SimpleTypeMarker?, right: SimpleTypeMarker?): SimpleTypeMarker? {
        if (left == null || right == null) return null

        return when {
            left is ConeIntegerLiteralType && right !is ConeIntegerLiteralType -> {
                commonSuperTypeBetweenIntegerTypeAndRegularType(left, right)
            }

            right is ConeIntegerLiteralType && left !is ConeIntegerLiteralType -> {
                commonSuperTypeBetweenIntegerTypeAndRegularType(right, left)
            }

            left is ConeIntegerLiteralConstantType && right is ConeIntegerLiteralConstantType -> {
                commonSuperTypeBetweenTwoConstantTypes(left, right)
            }

            left is ConeIntegerConstantOperatorType -> left
            right is ConeIntegerConstantOperatorType -> right
            else -> null
        }
    }

    private fun commonSuperTypeBetweenIntegerTypeAndRegularType(
        integerLiteralType: ConeIntegerLiteralType,
        regularType: SimpleTypeMarker
    ): SimpleTypeMarker? {
        return when (regularType) {
            in integerLiteralType.possibleTypes -> regularType
            else -> null
        }
    }

    private fun commonSuperTypeBetweenTwoConstantTypes(
        left: ConeIntegerLiteralConstantType,
        right: ConeIntegerLiteralConstantType
    ): ConeIntegerLiteralConstantType {
        konst possibleTypes = left.possibleTypes intersect right.possibleTypes
        return ConeIntegerLiteralConstantTypeImpl(left.konstue, possibleTypes, left.isUnsigned, left.nullability)
    }

    fun ConeFlexibleType.approximateIntegerLiteralBounds(expectedType: ConeKotlinType? = null): ConeFlexibleType {
        konst newLowerBound = lowerBound.approximateIntegerLiteralType(expectedType)
        konst newUpperBound = upperBound.approximateIntegerLiteralType(expectedType)

        if (newLowerBound !== lowerBound || newUpperBound !== upperBound) {
            return ConeFlexibleType(
                newLowerBound.lowerBoundIfFlexible(),
                newUpperBound.upperBoundIfFlexible()
            )
        }

        return this
    }

    fun ConeClassLikeType.withNullabilityAndAttributes(nullability: ConeNullability, attributes: ConeAttributes): ConeClassLikeType {
        if (nullability == this.nullability && attributes == this.attributes) return this

        return when (this) {
            is ConeErrorType -> this
            is ConeClassLikeTypeImpl -> ConeClassLikeTypeImpl(lookupTag, typeArguments, nullability.isNullable, attributes)
            else -> error("sealed")
        }
    }
}
