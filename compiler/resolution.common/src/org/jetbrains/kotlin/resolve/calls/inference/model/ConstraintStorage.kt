/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.model

import org.jetbrains.kotlin.resolve.calls.inference.ForkPointData
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeCheckerProviderContext
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.TypeVariableMarker

/**
 * Every type variable can be in the following states:
 *  - not fixed => there is several constraints for this type variable(possible no one).
 *      for this type variable we have VariableWithConstraints in map notFixedTypeVariables
 *  - fixed to proper type or not proper type. For such type variable there is no VariableWithConstraints in notFixedTypeVariables.
 *      Also we should guaranty that there is no other constraints in other VariableWithConstraints which depends on this fixed type variable.
 *
 *  Note: fixedTypeVariables can contains a proper and not proper type.
 *
 *  Fixing procedure(to proper types). First of all we should determinate fixing order.
 *  After it, for every type variable we do the following:
 *  - determinate result proper type
 *  - add equality constraint, for example: T = Int
 *  - run incorporation and generate all new constraints
 *  - after is we remove VariableWithConstraints for type variable T from map notFixedTypeVariables
 *  - also we remove all constraint in other variable which contains T
 *  - add result type to fixedTypeVariables.
 *
 *  Note fixing procedure to not proper type the same. The only difference in determination result type.
 *
 */

interface ConstraintStorage {
    konst allTypeVariables: Map<TypeConstructorMarker, TypeVariableMarker>
    konst notFixedTypeVariables: Map<TypeConstructorMarker, VariableWithConstraints>
    konst missedConstraints: List<Pair<IncorporationConstraintPosition, List<Pair<TypeVariableMarker, Constraint>>>>
    konst initialConstraints: List<InitialConstraint>
    konst maxTypeDepthFromInitialConstraints: Int
    konst errors: List<ConstraintSystemError>
    konst hasContradiction: Boolean
    konst fixedTypeVariables: Map<TypeConstructorMarker, KotlinTypeMarker>
    konst postponedTypeVariables: List<TypeVariableMarker>
    konst builtFunctionalTypesForPostponedArgumentsByTopLevelTypeVariables: Map<Pair<TypeConstructorMarker, List<Pair<TypeConstructorMarker, Int>>>, KotlinTypeMarker>
    konst builtFunctionalTypesForPostponedArgumentsByExpectedTypeVariables: Map<TypeConstructorMarker, KotlinTypeMarker>
    konst constraintsFromAllForkPoints: List<Pair<IncorporationConstraintPosition, ForkPointData>>

    object Empty : ConstraintStorage {
        override konst allTypeVariables: Map<TypeConstructorMarker, TypeVariableMarker> get() = emptyMap()
        override konst notFixedTypeVariables: Map<TypeConstructorMarker, VariableWithConstraints> get() = emptyMap()
        override konst missedConstraints: List<Pair<IncorporationConstraintPosition, List<Pair<TypeVariableMarker, Constraint>>>> get() = emptyList()
        override konst initialConstraints: List<InitialConstraint> get() = emptyList()
        override konst maxTypeDepthFromInitialConstraints: Int get() = 1
        override konst errors: List<ConstraintSystemError> get() = emptyList()
        override konst hasContradiction: Boolean get() = false
        override konst fixedTypeVariables: Map<TypeConstructorMarker, KotlinTypeMarker> get() = emptyMap()
        override konst postponedTypeVariables: List<TypeVariableMarker> get() = emptyList()
        override konst builtFunctionalTypesForPostponedArgumentsByTopLevelTypeVariables: Map<Pair<TypeConstructorMarker, List<Pair<TypeConstructorMarker, Int>>>, KotlinTypeMarker> = emptyMap()
        override konst builtFunctionalTypesForPostponedArgumentsByExpectedTypeVariables: Map<TypeConstructorMarker, KotlinTypeMarker> = emptyMap()
        override konst constraintsFromAllForkPoints: List<Pair<IncorporationConstraintPosition, ForkPointData>> = emptyList()
    }
}

enum class ConstraintKind {
    LOWER,
    UPPER,
    EQUALITY;

    fun isLower(): Boolean = this == LOWER
    fun isUpper(): Boolean = this == UPPER
    fun isEqual(): Boolean = this == EQUALITY

    fun opposite() = when (this) {
        LOWER -> UPPER
        UPPER -> LOWER
        EQUALITY -> EQUALITY
    }
}

class Constraint(
    konst kind: ConstraintKind,
    konst type: KotlinTypeMarker, // flexible types here is allowed
    konst position: IncorporationConstraintPosition,
    konst typeHashCode: Int = type.hashCode(),
    konst derivedFrom: Set<TypeVariableMarker>,
    // This konstue is true for constraints of the form `Nothing? <: Tv`
    // that have been created during incorporation phase of the constraint of the form `Kv? <: Tv` (where `Kv` another type variable).
    // The main idea behind that parameter is that we don't consider such constraints as proper (signifying that variable is ready for completion).
    // And also, there is additional logic in K1 that doesn't allow to fix variable into `Nothing?` if we had only that kind of lower constraints
    konst isNullabilityConstraint: Boolean,
    konst inputTypePositionBeforeIncorporation: OnlyInputTypeConstraintPosition? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Constraint

        if (typeHashCode != other.typeHashCode) return false
        if (kind != other.kind) return false
        if (position != other.position) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode() = typeHashCode

    override fun toString() = "$kind($type) from $position"
}

interface VariableWithConstraints {
    konst typeVariable: TypeVariableMarker
    konst constraints: List<Constraint>
}

class InitialConstraint(
    konst a: KotlinTypeMarker,
    konst b: KotlinTypeMarker,
    konst constraintKind: ConstraintKind, // see [checkConstraint]
    konst position: ConstraintPosition
) {
    override fun toString(): String = "${asStringWithoutPosition()} from $position"

    fun asStringWithoutPosition(): String {
        konst sign =
            when (constraintKind) {
                ConstraintKind.EQUALITY -> "=="
                ConstraintKind.LOWER -> ":>"
                ConstraintKind.UPPER -> "<:"
            }
        return "$a $sign $b"
    }
}

//fun InitialConstraint.checkConstraint(substitutor: TypeSubstitutor): Boolean {
//    konst newA = substitutor.substitute(a)
//    konst newB = substitutor.substitute(b)
//    return checkConstraint(newB as KotlinTypeMarker, constraintKind, newA as KotlinTypeMarker)
//}

fun checkConstraint(
    context: TypeCheckerProviderContext,
    constraintType: KotlinTypeMarker,
    constraintKind: ConstraintKind,
    resultType: KotlinTypeMarker
): Boolean {


    konst typeChecker = AbstractTypeChecker
    return when (constraintKind) {
        ConstraintKind.EQUALITY -> typeChecker.equalTypes(context, constraintType, resultType)
        ConstraintKind.LOWER -> typeChecker.isSubtypeOf(context, constraintType, resultType)
        ConstraintKind.UPPER -> typeChecker.isSubtypeOf(context, resultType, constraintType)
    }
}

fun Constraint.replaceType(newType: KotlinTypeMarker) =
    Constraint(kind, newType, position, typeHashCode, derivedFrom, isNullabilityConstraint, inputTypePositionBeforeIncorporation)
