/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.ConeClassifierLookupTag
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.utils.addToStdlib.foldMap

// We assume type IS an invariant type projection to prevent additional wrapper here
// (more exactly, invariant type projection contains type)
sealed class ConeKotlinType : ConeKotlinTypeProjection(), KotlinTypeMarker, TypeArgumentListMarker {
    final override konst kind: ProjectionKind
        get() = ProjectionKind.INVARIANT

    abstract konst typeArguments: Array<out ConeTypeProjection>

    final override konst type: ConeKotlinType
        get() = this

    abstract konst nullability: ConeNullability

    abstract konst attributes: ConeAttributes

    final override fun toString(): String {
        return renderForDebugging()
    }

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}

sealed class ConeSimpleKotlinType : ConeKotlinType(), SimpleTypeMarker

class ConeClassLikeErrorLookupTag(override konst classId: ClassId) : ConeClassLikeLookupTag()

class ConeErrorType(
    konst diagnostic: ConeDiagnostic,
    konst isUninferredParameter: Boolean = false,
    override konst typeArguments: Array<out ConeTypeProjection> = EMPTY_ARRAY,
    override konst attributes: ConeAttributes = ConeAttributes.Empty
) : ConeClassLikeType() {
    override konst lookupTag: ConeClassLikeLookupTag
        get() = ConeClassLikeErrorLookupTag(ClassId.fromString("<error>"))

    override konst nullability: ConeNullability
        get() = ConeNullability.UNKNOWN

    override fun equals(other: Any?) = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

abstract class ConeLookupTagBasedType : ConeSimpleKotlinType() {
    abstract konst lookupTag: ConeClassifierLookupTag
}

abstract class ConeClassLikeType : ConeLookupTagBasedType() {
    abstract override konst lookupTag: ConeClassLikeLookupTag
}

open class ConeFlexibleType(
    konst lowerBound: ConeSimpleKotlinType,
    konst upperBound: ConeSimpleKotlinType
) : ConeKotlinType(), FlexibleTypeMarker {

    final override konst typeArguments: Array<out ConeTypeProjection>
        get() = lowerBound.typeArguments

    final override konst nullability: ConeNullability
        get() = lowerBound.nullability.takeIf { it == upperBound.nullability } ?: ConeNullability.UNKNOWN

    final override konst attributes: ConeAttributes
        get() = lowerBound.attributes

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        // I suppose dynamic type (see below) and flexible type should use the same equals,
        // because ft<Any?, Nothing> should never be created
        if (other !is ConeFlexibleType) return false

        if (lowerBound != other.lowerBound) return false
        if (upperBound != other.upperBound) return false

        return true
    }

    final override fun hashCode(): Int {
        var result = lowerBound.hashCode()
        result = 31 * result + upperBound.hashCode()
        return result
    }
}

@RequiresOptIn(message = "Please use ConeDynamicType.create instead")
annotation class DynamicTypeConstructor

class ConeDynamicType @DynamicTypeConstructor constructor(
    lowerBound: ConeSimpleKotlinType,
    upperBound: ConeSimpleKotlinType
) : ConeFlexibleType(lowerBound, upperBound), DynamicTypeMarker {
    companion object
}

fun ConeSimpleKotlinType.unwrapDefinitelyNotNull(): ConeSimpleKotlinType {
    return when (this) {
        is ConeDefinitelyNotNullType -> original
        else -> this
    }
}

class ConeCapturedTypeConstructor(
    konst projection: ConeTypeProjection,
    var supertypes: List<ConeKotlinType>? = null,
    konst typeParameterMarker: TypeParameterMarker? = null
) : CapturedTypeConstructorMarker

data class ConeCapturedType(
    konst captureStatus: CaptureStatus,
    konst lowerType: ConeKotlinType?,
    override konst nullability: ConeNullability = ConeNullability.NOT_NULL,
    konst constructor: ConeCapturedTypeConstructor,
    override konst attributes: ConeAttributes = ConeAttributes.Empty,
    konst isProjectionNotNull: Boolean = false
) : ConeSimpleKotlinType(), CapturedTypeMarker {
    constructor(
        captureStatus: CaptureStatus, lowerType: ConeKotlinType?, projection: ConeTypeProjection,
        typeParameterMarker: TypeParameterMarker
    ) : this(
        captureStatus,
        lowerType,
        constructor = ConeCapturedTypeConstructor(
            projection,
            typeParameterMarker = typeParameterMarker
        )
    )

    override konst typeArguments: Array<out ConeTypeProjection>
        get() = emptyArray()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConeCapturedType

        if (lowerType != other.lowerType) return false
        if (constructor != other.constructor) return false
        if (captureStatus != other.captureStatus) return false
        if (nullability != other.nullability) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 7
        result = 31 * result + (lowerType?.hashCode() ?: 0)
        result = 31 * result + constructor.hashCode()
        result = 31 * result + captureStatus.hashCode()
        result = 31 * result + nullability.hashCode()
        return result
    }
}


data class ConeDefinitelyNotNullType(konst original: ConeSimpleKotlinType) : ConeSimpleKotlinType(), DefinitelyNotNullTypeMarker {
    override konst typeArguments: Array<out ConeTypeProjection>
        get() = original.typeArguments

    override konst nullability: ConeNullability
        get() = ConeNullability.NOT_NULL

    override konst attributes: ConeAttributes
        get() = original.attributes

    companion object
}

class ConeRawType private constructor(
    lowerBound: ConeSimpleKotlinType,
    upperBound: ConeSimpleKotlinType
) : ConeFlexibleType(lowerBound, upperBound) {
    companion object {
        fun create(
            lowerBound: ConeSimpleKotlinType,
            upperBound: ConeSimpleKotlinType,
        ): ConeRawType {
            require(lowerBound is ConeClassLikeType && upperBound is ConeClassLikeType) {
                "Raw bounds are expected to be class-like types, but $lowerBound and $upperBound were found"
            }

            konst lowerBoundToUse = if (!lowerBound.attributes.contains(CompilerConeAttributes.RawType)) {
                ConeClassLikeTypeImpl(
                    lowerBound.lookupTag, lowerBound.typeArguments, lowerBound.isNullable,
                    lowerBound.attributes + CompilerConeAttributes.RawType
                )
            } else {
                lowerBound
            }

            return ConeRawType(lowerBoundToUse, upperBound)
        }
    }
}

/**
 * Contract of the intersection type: it is flat. It means that an intersection type can not contain another intersection type inside it.
 * To comply with this contract, construct new intersection types only via [org.jetbrains.kotlin.fir.types.ConeTypeIntersector].
 */
class ConeIntersectionType(
    konst intersectedTypes: Collection<ConeKotlinType>,
    konst alternativeType: ConeKotlinType? = null,
) : ConeSimpleKotlinType(), IntersectionTypeConstructorMarker {
    override konst typeArguments: Array<out ConeTypeProjection>
        get() = emptyArray()

    override konst nullability: ConeNullability
        get() = ConeNullability.NOT_NULL

    override konst attributes: ConeAttributes = intersectedTypes.foldMap(
        { it.attributes },
        { a, b -> a.intersect(b) }
    )

    private var hashCode = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConeIntersectionType

        if (intersectedTypes != other.intersectedTypes) return false

        return true
    }

    override fun hashCode(): Int {
        if (hashCode != 0) return hashCode
        return intersectedTypes.hashCode().also { hashCode = it }
    }
}
