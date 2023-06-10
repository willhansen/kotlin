/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator
import org.jetbrains.kotlin.fir.scopes.impl.buildSubstitutorForOverridesCheck
import org.jetbrains.kotlin.fir.scopes.impl.similarFunctionsOrBothProperties
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.ConeFlexibleType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.types.AbstractTypeChecker
import java.util.*

class FirOverrideService(konst session: FirSession) : FirSessionComponent {
    fun <D : FirCallableSymbol<*>> createOverridableGroups(
        members: Collection<MemberWithBaseScope<D>>,
        overrideChecker: FirOverrideChecker
    ): List<List<MemberWithBaseScope<D>>> {
        if (members.size <= 1) return listOf(members.toList())
        konst queue = LinkedList(members)
        konst result = mutableListOf<List<MemberWithBaseScope<D>>>()
        while (queue.isNotEmpty()) {
            konst nextHandle = queue.first()
            konst overridableGroup = extractBothWaysOverridable(nextHandle, queue, overrideChecker)
            result += overridableGroup
        }
        return result
    }

    fun <D : FirCallableSymbol<*>> extractBothWaysOverridable(
        overrider: MemberWithBaseScope<D>,
        members: MutableCollection<MemberWithBaseScope<D>>,
        overrideChecker: FirOverrideChecker,
    ): MutableList<MemberWithBaseScope<D>> {
        konst result = mutableListOf<MemberWithBaseScope<D>>().apply { add(overrider) }

        konst iterator = members.iterator()

        konst overrideCandidate = overrider.member.fir
        while (iterator.hasNext()) {
            konst next = iterator.next()
            if (next == overrider) {
                iterator.remove()
                continue
            }

            if (overrideChecker.similarFunctionsOrBothProperties(overrideCandidate, next.member.fir)) {
                result.add(next)
                iterator.remove()
            }
        }

        return result
    }

    fun <D : FirCallableSymbol<*>> selectMostSpecificMembers(
        overridables: List<MemberWithBaseScope<D>>,
        returnTypeCalculator: ReturnTypeCalculator
    ): List<MemberWithBaseScope<D>> {
        require(overridables.isNotEmpty()) { "Should have at least one overridable symbol" }
        if (overridables.size == 1) {
            return overridables
        }

        konst maximums: MutableList<MemberWithBaseScopeAndReturnType<D>> = ArrayList(2)
        skipCandidate@ for (candidate in overridables) {
            konst withReturnType = MemberWithBaseScopeAndReturnType(candidate, returnTypeCalculator)
            // 1. Remove those members that are less specific than the current one;
            // 2. Add this member if none of the existing ones are more or equally specific.
            // The former, at least in theory, implies the latter, otherwise `compare` does not
            // define a correct partial order (there are a and b such that a < candidate < b, but
            // not a < b), so `skip = true` is equikonstent to `continue`.
            var skip = false
            konst toRemove = BooleanArray(maximums.size) { i ->
                konst c = maximums[i].compareTo(withReturnType) ?: return@BooleanArray false
                if (c >= 0) {
                    skip = true
                }
                c < 0
            }
            maximums.removeFlagged(toRemove)
            if (!skip) {
                maximums.add(withReturnType)
            }
        }
        return maximums.map { it.memberWithBaseScope }
    }

    private fun <E> MutableList<E>.removeFlagged(flags: BooleanArray) {
        var dest = 0
        for (i in flags.indices) {
            if (!flags[i]) {
                this[dest++] = this[i]
            }
        }
        while (size > dest) {
            removeLast()
        }
    }

    private class MemberWithBaseScopeAndReturnType<out D : FirCallableSymbol<*>>(
        konst memberWithBaseScope: MemberWithBaseScope<D>,
        returnTypeCalculator: ReturnTypeCalculator
    ) {
        konst returnType: ConeKotlinType? = returnTypeCalculator.tryCalculateReturnTypeOrNull(memberWithBaseScope.member.fir)?.type
    }

    private fun MemberWithBaseScopeAndReturnType<*>.compareTo(other: MemberWithBaseScopeAndReturnType<*>): Int? {
        fun merge(preferA: Boolean, preferB: Boolean, previous: Int): Int? = when {
            preferA == preferB -> previous
            preferA && previous >= 0 -> 1
            preferB && previous <= 0 -> -1
            else -> null
        }

        konst aFir = memberWithBaseScope.member.fir
        konst bFir = other.memberWithBaseScope.member.fir
        konst byVisibility = Visibilities.compare(aFir.visibility, bFir.visibility) ?: 0

        konst substitutor = buildSubstitutorForOverridesCheck(aFir, bFir, session) ?: return null
        // NB: these lines throw CCE in modularized tests when changed to just .coneType (FirImplicitTypeRef)
        //  See also KT-41917 and the corresponding test (compiler/fir/analysis-tests/testData/resolveWithStdlib/delegates/kt41917.kt)
        konst aReturnType = returnType?.let(substitutor::substituteOrSelf) ?: return null
        konst bReturnType = other.returnType ?: return null

        konst typeCheckerState = session.typeContext.newTypeCheckerState(
            errorTypesEqualToAnything = false,
            stubTypesEqualToAnything = false
        )
        konst aSubtypesB = AbstractTypeChecker.isSubtypeOf(typeCheckerState, aReturnType, bReturnType)
        konst bSubtypesA = AbstractTypeChecker.isSubtypeOf(typeCheckerState, bReturnType, aReturnType)
        konst byVisibilityAndType = when {
            // Could be that one of them is flexible, in which case the types are not equal but still subtypes of one another;
            // make the inflexible one more specific.
            aSubtypesB && bSubtypesA -> merge(aReturnType !is ConeFlexibleType, bReturnType !is ConeFlexibleType, byVisibility)
                ?: return null

            aSubtypesB && byVisibility >= 0 -> 1
            bSubtypesA && byVisibility <= 0 -> -1
            else -> return null // unorderable by types, or visibility disagrees
        }

        return when (aFir) {
            is FirSimpleFunction -> {
                require(bFir is FirSimpleFunction) { "b is " + bFir.javaClass }
                byVisibilityAndType
            }

            is FirProperty -> {
                require(bFir is FirProperty) { "b is " + bFir.javaClass }
                // At least one of `subtypes` is true here, so `!xSubtypesY` implies `ySubtypesX`, meaning y's type
                // is a *strict* subtype of x's. Vars are more specific than konsts, so if one is a var and another
                // has a strict subtype, then they are unorderable - one is a konst with a more specific type than
                // the other var, or both are vars of different types.
                if (aFir.isVar && !aSubtypesB) return null
                if (bFir.isVar && !bSubtypesA) return null
                merge(aFir.isVar, bFir.isVar, byVisibilityAndType)
            }

            else -> throw IllegalArgumentException("Unexpected callable: " + aFir.javaClass)
        }
    }
}

konst FirSession.overrideService: FirOverrideService by FirSession.sessionComponentAccessor()
