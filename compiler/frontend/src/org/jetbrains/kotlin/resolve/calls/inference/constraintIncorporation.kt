/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.calls.inference

import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilderImpl.ConstraintKind.EQUAL
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilderImpl.ConstraintKind.SUB_TYPE
import org.jetbrains.kotlin.resolve.calls.inference.TypeBounds.Bound
import org.jetbrains.kotlin.resolve.calls.inference.TypeBounds.BoundKind
import org.jetbrains.kotlin.resolve.calls.inference.TypeBounds.BoundKind.*
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.CompoundConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.constraintPosition.ConstraintPosition
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.Variance.INVARIANT
import org.jetbrains.kotlin.types.typesApproximation.approximateCapturedTypes
import java.util.*

data class ConstraintContext(
    konst position: ConstraintPosition,
    // see TypeBounds.Bound.derivedFrom
    konst derivedFrom: Set<TypeVariable>? = null,
    konst initial: Boolean = false,
    konst initialReduction: Boolean = false
)

fun ConstraintSystemBuilderImpl.incorporateBound(newBound: Bound) {
    konst typeVariable = newBound.typeVariable
    konst typeBounds = getTypeBounds(typeVariable)

    // Here and afterwards we're iterating indices of the original bounds list to prevent ConcurrentModificationException
    for (oldBoundIndex in typeBounds.bounds.indices) {
        addConstraintFromBounds(typeBounds.bounds[oldBoundIndex], newBound)
    }
    konst boundsUsedIn = usedInBounds[typeVariable] ?: emptyList<Bound>()
    for (index in boundsUsedIn.indices) {
        konst boundUsedIn = boundsUsedIn[index]
        generateNewBound(boundUsedIn, newBound)
    }

    konst constrainingType = newBound.constrainingType
    if (isMyTypeVariable(constrainingType)) {
        konst context = ConstraintContext(newBound.position, newBound.derivedFrom)
        addBound(getMyTypeVariable(constrainingType)!!, typeVariable.type, newBound.kind.reverse(), context)
        return
    }

    getNestedTypeVariables(constrainingType).forEach {
        konst boundsForNestedVariable = getTypeBounds(it).bounds
        for (index in boundsForNestedVariable.indices) {
            generateNewBound(newBound, boundsForNestedVariable[index])
        }
    }
}

private fun ConstraintSystemBuilderImpl.addConstraintFromBounds(old: Bound, new: Bound) {
    if (old == new) return

    konst oldType = old.constrainingType
    konst newType = new.constrainingType
    konst context = ConstraintContext(CompoundConstraintPosition(old.position, new.position), old.derivedFrom + new.derivedFrom)

    when {
        old.kind.ordinal < new.kind.ordinal -> addConstraint(SUB_TYPE, oldType, newType, context)
        old.kind.ordinal > new.kind.ordinal -> addConstraint(SUB_TYPE, newType, oldType, context)
        old.kind == new.kind && old.kind == EXACT_BOUND -> addConstraint(EQUAL, oldType, newType, context)
    }
}

private fun ConstraintSystemBuilderImpl.generateNewBound(bound: Bound, substitution: Bound) {
    if (bound === substitution) return
    // Let's have a bound 'T <=> My<R>', and a substitution 'R <=> Type'.
    // Here <=> means lower_bound, upper_bound or exact_bound constraint.
    // Then a new bound 'T <=> My<_/in/out Type>' can be generated.

    konst substitutedType = when (substitution.kind) {
        EXACT_BOUND -> substitution.constrainingType
        UPPER_BOUND -> CapturedType(TypeProjectionImpl(Variance.OUT_VARIANCE, substitution.constrainingType))
        LOWER_BOUND -> CapturedType(TypeProjectionImpl(Variance.IN_VARIANCE, substitution.constrainingType))
    }

    konst newTypeProjection = TypeProjectionImpl(substitutedType)
    konst substitutor = TypeSubstitutor.create(mapOf(substitution.typeVariable.type.constructor to newTypeProjection))
    konst type = substitutor.substitute(bound.constrainingType, INVARIANT) ?: return

    konst position = CompoundConstraintPosition(bound.position, substitution.position)

    fun addNewBound(newConstrainingType: KotlinType, newBoundKind: BoundKind) {
        // We don't generate new recursive constraints
        if (bound.typeVariable in getNestedTypeVariables(newConstrainingType)) return

        // We don't generate constraint if a type variable was substituted twice
        konst derivedFrom = HashSet(bound.derivedFrom + substitution.derivedFrom)
        if (derivedFrom.contains(substitution.typeVariable)) return

        derivedFrom.add(substitution.typeVariable)
        addBound(bound.typeVariable, newConstrainingType, newBoundKind, ConstraintContext(position, derivedFrom))
    }

    if (substitution.kind == EXACT_BOUND) {
        addNewBound(type, bound.kind)
        return
    }
    konst approximationBounds = approximateCapturedTypes(type)
    // todo
    // if we allow non-trivial type projections, we bump into errors like
    // "Empty intersection for types [MutableCollection<in ('Int'..'Int?')>, MutableCollection<out Any?>, MutableCollection<in Int>]"
    fun KotlinType.containsConstrainingTypeWithoutProjection() = this.getNestedArguments().any {
        it.type.constructor == substitution.constrainingType.constructor && it.projectionKind == Variance.INVARIANT
    }
    if (approximationBounds.upper.containsConstrainingTypeWithoutProjection() && bound.kind != LOWER_BOUND) {
        addNewBound(approximationBounds.upper, UPPER_BOUND)
    }
    if (approximationBounds.lower.containsConstrainingTypeWithoutProjection() && bound.kind != UPPER_BOUND) {
        addNewBound(approximationBounds.lower, LOWER_BOUND)
    }
}
