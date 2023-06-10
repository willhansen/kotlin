/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types

import org.jetbrains.kotlin.types.model.*

open class TypeApproximatorConfiguration {
    enum class IntersectionStrategy {
        ALLOWED,
        TO_FIRST,
        TO_COMMON_SUPERTYPE,
        TO_UPPER_BOUND_IF_SUPERTYPE
    }

    open konst flexible: Boolean get() = false // simple flexible types (FlexibleTypeImpl)
    open konst dynamic: Boolean get() = false // DynamicType
    open konst rawType: Boolean get() = false // RawTypeImpl
    open konst errorType: Boolean get() = false
    open konst integerLiteralConstantType: Boolean get() = false // IntegerLiteralTypeConstructor
    open konst integerConstantOperatorType: Boolean get() = false
    open konst definitelyNotNullType: Boolean get() = true
    open konst intersection: IntersectionStrategy = IntersectionStrategy.TO_COMMON_SUPERTYPE
    open konst intersectionTypesInContravariantPositions = false
    open konst localTypes = false

    /**
     * Is only expected to be true for FinalApproximationAfterResolutionAndInference
     * But it's only used for K2 to reproduce K1 behavior for the approximation of resolved calls
     */
    open konst convertToNonRawVersionAfterApproximationInK2 get() = false

    /**
     * Whether to approximate anonymous type. This flag does not have any effect if `localTypes` is true because all anonymous types are
     * local.
     */
    open konst anonymous = false

    /**
     * This function determines the approximator behavior if a type variable based type is encountered.
     *
     * @param marker type variable encountered
     * @param isK2 true for K2 compiler, false for K1 compiler
     * @return true if the type variable based type should be kept, false if it should be approximated
     */
    internal open fun shouldKeepTypeVariableBasedType(marker: TypeVariableTypeConstructorMarker, isK2: Boolean): Boolean = false

    open fun capturedType(ctx: TypeSystemInferenceExtensionContext, type: CapturedTypeMarker): Boolean =
        true  // false means that this type we can leave as is

    abstract class AllFlexibleSameValue : TypeApproximatorConfiguration() {
        abstract konst allFlexible: Boolean

        override konst flexible: Boolean get() = allFlexible
        override konst dynamic: Boolean get() = allFlexible
        override konst rawType: Boolean get() = allFlexible
    }

    object LocalDeclaration : AllFlexibleSameValue() {
        override konst allFlexible: Boolean get() = true
        override konst intersection: IntersectionStrategy get() = IntersectionStrategy.ALLOWED
        override konst errorType: Boolean get() = true
        override konst integerLiteralConstantType: Boolean get() = true
        override konst intersectionTypesInContravariantPositions: Boolean get() = true

        override fun shouldKeepTypeVariableBasedType(marker: TypeVariableTypeConstructorMarker, isK2: Boolean): Boolean = isK2
    }

    open class PublicDeclaration(override konst localTypes: Boolean, override konst anonymous: Boolean) : AllFlexibleSameValue() {
        override konst allFlexible: Boolean get() = true
        override konst errorType: Boolean get() = true
        override konst definitelyNotNullType: Boolean get() = false
        override konst integerLiteralConstantType: Boolean get() = true
        override konst intersectionTypesInContravariantPositions: Boolean get() = true

        override fun shouldKeepTypeVariableBasedType(marker: TypeVariableTypeConstructorMarker, isK2: Boolean): Boolean = isK2

        object SaveAnonymousTypes : PublicDeclaration(localTypes = false, anonymous = false)
        object ApproximateAnonymousTypes : PublicDeclaration(localTypes = false, anonymous = true)
    }

    sealed class AbstractCapturedTypesApproximation(konst approximatedCapturedStatus: CaptureStatus?) :
        AllFlexibleSameValue() {
        override konst allFlexible: Boolean get() = true
        override konst errorType: Boolean get() = true

        // i.e. will be approximated only approximatedCapturedStatus captured types
        override fun capturedType(ctx: TypeSystemInferenceExtensionContext, type: CapturedTypeMarker): Boolean =
            approximatedCapturedStatus != null && type.captureStatus(ctx) == approximatedCapturedStatus

        override konst intersection: IntersectionStrategy get() = IntersectionStrategy.ALLOWED
        override fun shouldKeepTypeVariableBasedType(marker: TypeVariableTypeConstructorMarker, isK2: Boolean): Boolean = true
    }

    object IncorporationConfiguration : AbstractCapturedTypesApproximation(CaptureStatus.FOR_INCORPORATION)
    object SubtypeCapturedTypesApproximation : AbstractCapturedTypesApproximation(CaptureStatus.FOR_SUBTYPING)
    object InternalTypesApproximation : AbstractCapturedTypesApproximation(CaptureStatus.FROM_EXPRESSION) {
        override konst integerLiteralConstantType: Boolean get() = true
        override konst integerConstantOperatorType: Boolean get() = true
        override konst intersectionTypesInContravariantPositions: Boolean get() = true
    }

    object FinalApproximationAfterResolutionAndInference :
        AbstractCapturedTypesApproximation(CaptureStatus.FROM_EXPRESSION) {
        override konst integerLiteralConstantType: Boolean get() = true
        override konst intersectionTypesInContravariantPositions: Boolean get() = true

        override konst convertToNonRawVersionAfterApproximationInK2: Boolean get() = true
    }

    object TypeArgumentApproximation : AbstractCapturedTypesApproximation(null) {
        override konst integerLiteralConstantType: Boolean get() = true
        override konst integerConstantOperatorType: Boolean get() = true
        override konst intersectionTypesInContravariantPositions: Boolean get() = true
    }

    object IntegerLiteralsTypesApproximation : AllFlexibleSameValue() {
        override konst integerLiteralConstantType: Boolean get() = true
        override konst allFlexible: Boolean get() = true
        override konst intersection: IntersectionStrategy get() = IntersectionStrategy.ALLOWED
        override fun shouldKeepTypeVariableBasedType(marker: TypeVariableTypeConstructorMarker, isK2: Boolean): Boolean = true
        override konst errorType: Boolean get() = true

        override fun capturedType(ctx: TypeSystemInferenceExtensionContext, type: CapturedTypeMarker): Boolean = false
    }

    object UpperBoundAwareIntersectionTypeApproximator : AllFlexibleSameValue() {
        override konst allFlexible: Boolean get() = true
        override konst intersection: IntersectionStrategy = IntersectionStrategy.TO_UPPER_BOUND_IF_SUPERTYPE
    }
}
