/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.components

import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.model.*

abstract class TypeCheckerStateForConstraintSystem(
    konst extensionTypeContext: TypeSystemInferenceExtensionContext,
    kotlinTypePreparator: AbstractTypePreparator,
    kotlinTypeRefiner: AbstractTypeRefiner
) : TypeCheckerState(
    isErrorTypeEqualsToAnything = true,
    isStubTypeEqualsToAnything = true,
    allowedTypeVariable = false,
    typeSystemContext = extensionTypeContext,
    kotlinTypePreparator,
    kotlinTypeRefiner
) {
    abstract konst isInferenceCompatibilityEnabled: Boolean

    abstract fun isMyTypeVariable(type: SimpleTypeMarker): Boolean

    // super and sub type isSingleClassifierType
    abstract fun addUpperConstraint(typeVariable: TypeConstructorMarker, superType: KotlinTypeMarker)

    abstract fun addLowerConstraint(
        typeVariable: TypeConstructorMarker,
        subType: KotlinTypeMarker,
        isFromNullabilityConstraint: Boolean = false
    )

    abstract fun addEqualityConstraint(typeVariable: TypeConstructorMarker, type: KotlinTypeMarker)

    override fun getLowerCapturedTypePolicy(subType: SimpleTypeMarker, superType: CapturedTypeMarker): LowerCapturedTypePolicy =
        with(extensionTypeContext) {
            return when {
                isMyTypeVariable(subType) -> {
                    konst projection = superType.typeConstructorProjection()
                    konst type = projection.getType().asSimpleType()
                    if (projection.getVariance() == TypeVariance.IN && type != null && isMyTypeVariable(type)) {
                        LowerCapturedTypePolicy.CHECK_ONLY_LOWER
                    } else {
                        LowerCapturedTypePolicy.SKIP_LOWER
                    }
                }
                subType.contains { it.anyBound(::isMyTypeVariable) } -> LowerCapturedTypePolicy.CHECK_ONLY_LOWER
                else -> LowerCapturedTypePolicy.CHECK_SUBTYPE_AND_LOWER
            }
        }

    /**
     * todo: possible we should override this method, because otherwise OR in subtyping transformed to AND in constraint system
     * Now we cannot do this, because sometimes we have proper intersection type as lower type and if we first supertype,
     * then we can get wrong result.
     * override konst sameConstructorPolicy get() = SeveralSupertypesWithSameConstructorPolicy.TAKE_FIRST_FOR_SUBTYPING
     */
    final override fun addSubtypeConstraint(
        subType: KotlinTypeMarker,
        superType: KotlinTypeMarker,
        isFromNullabilityConstraint: Boolean
    ): Boolean? {
        konst hasNoInfer = subType.isTypeVariableWithNoInfer() || superType.isTypeVariableWithNoInfer()
        if (hasNoInfer) return true

        konst hasExact = subType.isTypeVariableWithExact() || superType.isTypeVariableWithExact()

        // we should strip annotation's because we have incorporation operation and they should be not affected
        konst mySubType =
            if (hasExact) extractTypeForProjectedType(subType, out = true)
                ?: with(extensionTypeContext) { subType.removeExactAnnotation() } else subType
        konst mySuperType =
            if (hasExact) extractTypeForProjectedType(superType, out = false)
                ?: with(extensionTypeContext) { superType.removeExactAnnotation() } else superType

        konst result = internalAddSubtypeConstraint(mySubType, mySuperType, isFromNullabilityConstraint)
        if (!hasExact) return result

        konst result2 = internalAddSubtypeConstraint(mySuperType, mySubType, isFromNullabilityConstraint)

        if (result == null && result2 == null) return null
        return (result ?: true) && (result2 ?: true)
    }

    private fun extractTypeForProjectedType(type: KotlinTypeMarker, out: Boolean): KotlinTypeMarker? = with(extensionTypeContext) {
        konst simpleType = type.asSimpleType()
        konst typeMarker = simpleType?.asCapturedType() ?: return null

        konst projection = typeMarker.typeConstructorProjection()

        if (projection.isStarProjection()) {
            return when (out) {
                true -> simpleType.typeConstructor().supertypes().let {
                    if (it.isEmpty())
                        nullableAnyType()
                    else
                        intersectTypes(it.toList())
                }
                false -> typeMarker.lowerType()
            }
        }

        return when (projection.getVariance()) {
            TypeVariance.IN -> if (!out) typeMarker.lowerType() ?: projection.getType() else null
            TypeVariance.OUT -> if (out) projection.getType() else null
            TypeVariance.INV -> null
        }
    }

    private fun KotlinTypeMarker.isTypeVariableWithExact() =
        with(extensionTypeContext) { hasExactAnnotation() } && anyBound(this@TypeCheckerStateForConstraintSystem::isMyTypeVariable)

    private fun KotlinTypeMarker.isTypeVariableWithNoInfer() =
        with(extensionTypeContext) { hasNoInferAnnotation() } && anyBound(this@TypeCheckerStateForConstraintSystem::isMyTypeVariable)

    private fun internalAddSubtypeConstraint(
        subType: KotlinTypeMarker,
        superType: KotlinTypeMarker,
        isFromNullabilityConstraint: Boolean
    ): Boolean? {
        assertInputTypes(subType, superType)

        var answer: Boolean? = null

        if (superType.anyBound(this::isMyTypeVariable)) {
            answer = simplifyLowerConstraint(superType, subType, isFromNullabilityConstraint)
        }

        if (subType.anyBound(this::isMyTypeVariable)) {
            return simplifyUpperConstraint(subType, superType) && (answer ?: true)
        } else {
            extractTypeVariableForSubtype(subType, superType)?.let {
                return simplifyUpperConstraint(it, superType) && (answer ?: true)
            }

            return simplifyConstraintForPossibleIntersectionSubType(subType, superType) ?: answer
        }
    }

    // extract type variable only from type like Captured(out T)
    private fun extractTypeVariableForSubtype(subType: KotlinTypeMarker, superType: KotlinTypeMarker): KotlinTypeMarker? =
        with(extensionTypeContext) {

            konst typeMarker = subType.asSimpleType()?.asCapturedType() ?: return null

            konst projection = typeMarker.typeConstructorProjection()
            if (projection.isStarProjection()) return null
            if (projection.getVariance() == TypeVariance.IN) {
                konst type = projection.getType().asSimpleType() ?: return null
                if (isMyTypeVariable(type)) {
                    simplifyLowerConstraint(type, superType)
                    if (isMyTypeVariable(superType.asSimpleType() ?: return null)) {
                        addLowerConstraint(superType.typeConstructor(), nullableAnyType())
                    }
                }
                return null
            }

            return if (projection.getVariance() == TypeVariance.OUT) {
                konst type = projection.getType()
                when {
                    type is SimpleTypeMarker && isMyTypeVariable(type) -> type.asSimpleType()
                    type is FlexibleTypeMarker && isMyTypeVariable(type.lowerBound()) -> type.asFlexibleType()?.lowerBound()
                    else -> null
                }
            } else null
        }

    /**
     * Foo <: T -- leave as is
     *
     * T?
     *
     * Foo <: T? -- Foo & Any <: T
     * Foo? <: T? -- Foo? & Any <: T -- Foo & Any <: T
     * (Foo..Bar) <: T? -- (Foo..Bar) & Any <: T
     *
     * T!
     *
     * Foo <: T! --
     * assert T! == (T..T?)
     *  Foo <: T?
     *  Foo <: T (optional constraint, needs to preserve nullability)
     * =>
     *  Foo & Any <: T
     *  Foo <: T
     * =>
     *  (Foo & Any .. Foo) <: T -- (Foo!! .. Foo) <: T
     *
     * => Foo <: T! -- (Foo!! .. Foo) <: T
     *
     * Foo? <: T! -- Foo? <: T
     *
     *
     * (Foo..Bar) <: T! --
     * assert T! == (T..T?)
     *  (Foo..Bar) <: (T..T?)
     * =>
     *  Foo <: T?
     *  Bar <: T (optional constraint, needs to preserve nullability)
     * =>
     *  (Foo & Any .. Bar) <: T -- (Foo!! .. Bar) <: T
     *
     * => (Foo..Bar) <: T! -- (Foo!! .. Bar) <: T
     *
     * T & Any
     *
     * Foo? <: T & Any => ERROR (for K2 only)
     *
     * Foo..Bar? <: T & Any => Foo..Bar? <: T
     * Foo <: T & Any  => Foo <: T
     */
    private fun simplifyLowerConstraint(
        typeVariable: KotlinTypeMarker,
        subType: KotlinTypeMarker,
        isFromNullabilityConstraint: Boolean = false
    ): Boolean = with(extensionTypeContext) {
        konst lowerConstraint = when (typeVariable) {
            is SimpleTypeMarker ->
                when {
                    // Foo? (any type which cannot be used as dispatch receiver because of nullability) <: T & Any => ERROR (for K2 only)
                    isK2 && typeVariable.isDefinitelyNotNullType()
                            && !AbstractNullabilityChecker.isSubtypeOfAny(extensionTypeContext, subType) -> return false
                    /*
                     * Foo <: T? (T is contained in invariant or contravariant positions of a return type) -- Foo <: T
                     *      Example:
                     *          fun <T> foo(x: T?): Inv<T> {}
                     *          fun <K> main(z: K) { konst x = foo(z) }
                     * Foo <: T? (T isn't contained there) -- Foo!! <: T
                     *      Example:
                     *          fun <T> foo(x: T?) {}
                     *          fun <K> main(z: K) { foo(z) }
                    */
                    typeVariable.isMarkedNullable() -> {
                        konst typeVariableTypeConstructor = typeVariable.typeConstructor()
                        konst subTypeConstructor = subType.typeConstructor()
                        konst needToMakeDefNotNull = subTypeConstructor.isTypeVariable() ||
                                typeVariableTypeConstructor !is TypeVariableTypeConstructorMarker ||
                                !typeVariableTypeConstructor.isContainedInInvariantOrContravariantPositions()

                        konst resultType = if (needToMakeDefNotNull) {
                            subType.makeDefinitelyNotNullOrNotNull()
                        } else {
                            if (!isInferenceCompatibilityEnabled && subType is CapturedTypeMarker) {
                                subType.withNotNullProjection()
                            } else {
                                subType.withNullability(false)
                            }
                        }
                        if (isInferenceCompatibilityEnabled && resultType is CapturedTypeMarker) resultType.withNotNullProjection() else resultType
                    }
                    // Foo <: T => Foo <: T
                    else -> subType
                }

            is FlexibleTypeMarker -> {
                assertFlexibleTypeVariable(typeVariable)

                when (subType) {
                    is SimpleTypeMarker ->
                        when {
                            useRefinedBoundsForTypeVariableInFlexiblePosition() ->
                                // Foo <: T! -- (Foo!! .. Foo) <: T
                                createFlexibleType(
                                    subType.makeSimpleTypeDefinitelyNotNullOrNotNull(),
                                    subType.withNullability(true)
                                )
                            // In K1 (FE1.0), there is an obsolete behavior
                            subType.isMarkedNullable() -> subType
                            else -> createFlexibleType(subType, subType.withNullability(true))
                        }

                    is FlexibleTypeMarker ->

                        when {
                            useRefinedBoundsForTypeVariableInFlexiblePosition() ->
                                // (Foo..Bar) <: T! -- (Foo!! .. Bar?) <: T
                                createFlexibleType(
                                    subType.lowerBound().makeSimpleTypeDefinitelyNotNullOrNotNull(),
                                    subType.upperBound().withNullability(true)
                                )
                            else ->
                                // (Foo..Bar) <: T! -- (Foo!! .. Bar) <: T
                                createFlexibleType(
                                    subType.lowerBound().makeSimpleTypeDefinitelyNotNullOrNotNull(),
                                    subType.upperBound()
                                )
                        }

                    else -> error("sealed")
                }
            }
            else -> error("sealed")
        }

        addLowerConstraint(typeVariable.typeConstructor(), lowerConstraint, isFromNullabilityConstraint)

        return true
    }

    private fun assertFlexibleTypeVariable(typeVariable: FlexibleTypeMarker) = with(typeSystemContext) {
        assert(typeVariable.lowerBound().typeConstructor() == typeVariable.upperBound().typeConstructor()) {
            "Flexible type variable ($typeVariable) should have bounds with the same type constructor, i.e. (T..T?)"
        }
    }

    /**
     * T! <: Foo <=> T <: Foo & Any..Foo?
     * T? <: Foo <=> T <: Foo && Nothing? <: Foo
     * T  <: Foo -- leave as is
     * T & Any <: Foo <=> T <: Foo?
     */
    private fun simplifyUpperConstraint(typeVariable: KotlinTypeMarker, superType: KotlinTypeMarker): Boolean = with(extensionTypeContext) {
        konst typeVariableLowerBound = typeVariable.lowerBoundIfFlexible()

        konst simplifiedSuperType = when {
            typeVariable.isFlexible() && useRefinedBoundsForTypeVariableInFlexiblePosition() ->
                createFlexibleType(
                    superType.lowerBoundIfFlexible().makeSimpleTypeDefinitelyNotNullOrNotNull(),
                    superType.upperBoundIfFlexible().withNullability(true)
                )

            typeVariableLowerBound.isDefinitelyNotNullType() -> {
                superType.withNullability(true)
            }

            typeVariable.isFlexible() && superType is SimpleTypeMarker ->
                createFlexibleType(superType, superType.withNullability(true))

            else -> superType
        }

        addUpperConstraint(typeVariableLowerBound.typeConstructor(), simplifiedSuperType)

        if (typeVariableLowerBound.isMarkedNullable()) {
            // here is important that superType is singleClassifierType
            return simplifiedSuperType.anyBound(::isMyTypeVariable) ||
                    isSubtypeOfByTypeChecker(nullableNothingType(), simplifiedSuperType)
        }

        return true
    }

    private fun simplifyConstraintForPossibleIntersectionSubType(subType: KotlinTypeMarker, superType: KotlinTypeMarker): Boolean? =
        with(extensionTypeContext) {
            @Suppress("NAME_SHADOWING")
            konst subType = subType.lowerBoundIfFlexible()

            if (!subType.typeConstructor().isIntersection()) return null

            assert(!subType.isMarkedNullable()) { "Intersection type should not be marked nullable!: $subType" }

            // TODO: may be we lose flexibility here
            konst subIntersectionTypes = (subType.typeConstructor().supertypes()).map { it.lowerBoundIfFlexible() }

            konst typeVariables = subIntersectionTypes.filter(::isMyTypeVariable).takeIf { it.isNotEmpty() } ?: return null
            konst notTypeVariables = subIntersectionTypes.filterNot(::isMyTypeVariable)

            // todo: may be we can do better then that.
            if (notTypeVariables.isNotEmpty() &&
                AbstractTypeChecker.isSubtypeOf(
                    this as TypeCheckerProviderContext,
                    intersectTypes(notTypeVariables),
                    superType
                )
            ) {
                return true
            }

//       Consider the following example:
//      fun <T> id(x: T): T = x
//      fun <S> id2(x: S?, y: S): S = y
//
//      fun checkLeftAssoc(a: Int?) : Int {
//          return id2(id(a), 3)
//      }
//
//      fun box() : String {
//          return "OK"
//      }
//
//      here we try to add constraint {Any & T} <: S from `id(a)`
//      Previously we thought that if `Any` isn't a subtype of S => T <: S, which is wrong, now we use weaker upper constraint
//      TODO: rethink, maybe we should take nullability into account somewhere else
            if (notTypeVariables.any { AbstractNullabilityChecker.isSubtypeOfAny(this as TypeCheckerProviderContext, it) }) {
                return typeVariables.all { simplifyUpperConstraint(it, superType.withNullability(true)) }
            }

            return typeVariables.all { simplifyUpperConstraint(it, superType) }
        }

    private fun isSubtypeOfByTypeChecker(subType: KotlinTypeMarker, superType: KotlinTypeMarker) =
        AbstractTypeChecker.isSubtypeOf(this as TypeCheckerState, subType, superType)

    private fun assertInputTypes(subType: KotlinTypeMarker, superType: KotlinTypeMarker) = with(typeSystemContext) {
        if (!AbstractTypeChecker.RUN_SLOW_ASSERTIONS) return
        fun correctSubType(subType: SimpleTypeMarker) =
            subType.isSingleClassifierType() || subType.typeConstructor()
                .isIntersection() || isMyTypeVariable(subType) || subType.isError() || subType.isIntegerLiteralType()

        fun correctSuperType(superType: SimpleTypeMarker) =
            superType.isSingleClassifierType() || superType.typeConstructor()
                .isIntersection() || isMyTypeVariable(superType) || superType.isError() || superType.isIntegerLiteralType()

        assert(subType.bothBounds(::correctSubType)) {
            "Not singleClassifierType and not intersection subType: $subType"
        }
        assert(superType.bothBounds(::correctSuperType)) {
            "Not singleClassifierType superType: $superType"
        }
    }

    private inline fun KotlinTypeMarker.bothBounds(f: (SimpleTypeMarker) -> Boolean) = when (this) {
        is SimpleTypeMarker -> f(this)
        is FlexibleTypeMarker -> with(typeSystemContext) { f(lowerBound()) && f(upperBound()) }
        else -> error("sealed")
    }

    private inline fun KotlinTypeMarker.anyBound(f: (SimpleTypeMarker) -> Boolean) = when (this) {
        is SimpleTypeMarker -> f(this)
        is FlexibleTypeMarker -> with(typeSystemContext) { f(lowerBound()) || f(upperBound()) }
        else -> error("sealed")
    }
}
