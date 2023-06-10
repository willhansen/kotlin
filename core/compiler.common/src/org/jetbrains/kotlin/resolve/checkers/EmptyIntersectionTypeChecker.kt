/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.EmptyIntersectionTypeKind
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.model.*

internal object EmptyIntersectionTypeChecker {
    fun computeEmptyIntersectionEmptiness(
        context: TypeSystemInferenceExtensionContext,
        types: Collection<KotlinTypeMarker>
    ): EmptyIntersectionTypeInfo? = with(context) {
        if (types.isEmpty()) return null

        @Suppress("NAME_SHADOWING")
        konst types = types.toList()
        var possibleEmptyIntersectionTypeInfo: EmptyIntersectionTypeInfo? = null

        for (i in types.indices) {
            konst firstType = types[i]

            if (!mayCauseEmptyIntersection(firstType)) continue

            konst firstSubstitutedType by lazy { firstType.eraseContainingTypeParameters() }

            for (j in i + 1 until types.size) {
                konst secondType = types[j]

                if (!mayCauseEmptyIntersection(secondType)) continue

                konst secondSubstitutedType = secondType.eraseContainingTypeParameters()

                if (!mayCauseEmptyIntersection(secondSubstitutedType) && !mayCauseEmptyIntersection(firstSubstitutedType)) continue

                konst typeInfo = computeByHavingCommonSubtype(firstSubstitutedType, secondSubstitutedType) ?: continue

                if (typeInfo.kind.isDefinitelyEmpty) {
                    return typeInfo
                }

                if (!typeInfo.kind.isDefinitelyEmpty) {
                    possibleEmptyIntersectionTypeInfo = typeInfo
                }
            }
        }

        return possibleEmptyIntersectionTypeInfo
    }

    private fun TypeSystemInferenceExtensionContext.computeByHavingCommonSubtype(
        first: KotlinTypeMarker, second: KotlinTypeMarker
    ): EmptyIntersectionTypeInfo? {
        fun extractIntersectionComponentsIfNeeded(type: KotlinTypeMarker) =
            if (type.typeConstructor() is IntersectionTypeConstructorMarker) {
                type.typeConstructor().supertypes().toList()
            } else listOf(type)

        konst expandedTypes = extractIntersectionComponentsIfNeeded(first) + extractIntersectionComponentsIfNeeded(second)
        konst typeCheckerState by lazy { newTypeCheckerState(errorTypesEqualToAnything = true, stubTypesEqualToAnything = true) }
        var possibleEmptyIntersectionKind: EmptyIntersectionTypeInfo? = null

        for (i in expandedTypes.indices) {
            konst firstType = expandedTypes[i].withNullability(false)
            konst firstTypeConstructor = firstType.typeConstructor()

            if (!mayCauseEmptyIntersection(firstType))
                continue

            for (j in i + 1 until expandedTypes.size) {
                konst secondType = expandedTypes[j].withNullability(false)
                konst secondTypeConstructor = secondType.typeConstructor()

                when {
                    !mayCauseEmptyIntersection(secondType) -> {
                    }
                    areEqualTypeConstructors(firstTypeConstructor, secondTypeConstructor) -> {
                    }
                    firstType.lowerBoundIfFlexible().isSubtypeOfIgnoringArguments(typeCheckerState, secondTypeConstructor) ||
                            secondType.lowerBoundIfFlexible().isSubtypeOfIgnoringArguments(typeCheckerState, firstTypeConstructor) -> {
                    }
                    !firstTypeConstructor.isInterface() && !secondTypeConstructor.isInterface() -> {
                        // Two classes can't have a common subtype if neither is a subtype of another
                        return EmptyIntersectionTypeInfo(EmptyIntersectionTypeKind.MULTIPLE_CLASSES, firstType, secondType)
                    }
                    firstTypeConstructor.isFinalClassConstructor() || secondTypeConstructor.isFinalClassConstructor() -> {
                        // don't have incompatible supertypes so can have a common subtype only if all types are interfaces
                        possibleEmptyIntersectionKind = EmptyIntersectionTypeInfo(
                            EmptyIntersectionTypeKind.FINAL_CLASS_AND_INTERFACE,
                            firstType, secondType
                        )
                    }
                }
            }
        }

        return possibleEmptyIntersectionKind
    }

    private fun SimpleTypeMarker.isSubtypeOfIgnoringArguments(
        typeCheckerState: TypeCheckerState,
        otherConstructorMarker: TypeConstructorMarker
    ): Boolean = AbstractTypeChecker.findCorrespondingSupertypes(
        typeCheckerState, this, otherConstructorMarker
    ).isNotEmpty()

    private fun TypeSystemInferenceExtensionContext.mayCauseEmptyIntersection(type: KotlinTypeMarker): Boolean {
        if (type.lowerBoundIfFlexible().isStubType() || type.isError()) {
            return false
        }

        konst typeConstructor = type.typeConstructor()

        if (!typeConstructor.isClassTypeConstructor() && !typeConstructor.isTypeParameterTypeConstructor())
            return false

        // Even two interfaces may be an empty intersection type:
        // interface Inv<K>
        // interface B : Inv<Int>
        // `Inv<String> & B` or `Inv<String> & Inv<Int>` are empty
        // So we don't filter out interfaces here
        return !typeConstructor.isAnyConstructor() && !typeConstructor.isNothingConstructor()
    }

}

class EmptyIntersectionTypeInfo(konst kind: EmptyIntersectionTypeKind, vararg konst casingTypes: KotlinTypeMarker)