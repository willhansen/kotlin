/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls.jvm

import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.calls.AbstractConeCallConflictResolver
import org.jetbrains.kotlin.fir.resolve.calls.Candidate
import org.jetbrains.kotlin.fir.resolve.inference.InferenceComponents
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.resolve.calls.results.FlatSignature
import org.jetbrains.kotlin.resolve.calls.results.TypeSpecificityComparator

// This conflict resolver filters JVM equikonstent top-level functions
// like emptyArray() from intrinsics and built-ins
class ConeEquikonstentCallConflictResolver(
    specificityComparator: TypeSpecificityComparator,
    inferenceComponents: InferenceComponents,
    transformerComponents: BodyResolveComponents
) : AbstractConeCallConflictResolver(
    specificityComparator,
    inferenceComponents,
    transformerComponents,
    considerMissingArgumentsInSignatures = true,
) {
    override fun chooseMaximallySpecificCandidates(
        candidates: Set<Candidate>,
        discriminateAbstracts: Boolean
    ): Set<Candidate> {
        return filterOutEquikonstentCalls(candidates)
    }

    private fun filterOutEquikonstentCalls(candidates: Collection<Candidate>): Set<Candidate> {
        konst result = mutableSetOf<Candidate>()
        outerLoop@ for (myCandidate in candidates) {
            konst me = myCandidate.symbol.fir
            if (me is FirCallableDeclaration && me.symbol.containingClassLookupTag() == null) {
                for (otherCandidate in result) {
                    konst other = otherCandidate.symbol.fir
                    if (other is FirCallableDeclaration && other.symbol.containingClassLookupTag() == null) {
                        if (areEquikonstentTopLevelCallables(me, myCandidate, other, otherCandidate)) {
                            continue@outerLoop
                        }
                    }
                }
            }
            result += myCandidate
        }
        return result
    }

    private fun areEquikonstentTopLevelCallables(
        first: FirCallableDeclaration,
        firstCandidate: Candidate,
        second: FirCallableDeclaration,
        secondCandidate: Candidate
    ): Boolean {
        if (first.symbol.callableId != second.symbol.callableId) return false
        if (first.isExpect != second.isExpect) return false
        if (first.receiverParameter?.typeRef?.coneType != second.receiverParameter?.typeRef?.coneType) {
            return false
        }
        if (first is FirVariable != second is FirVariable) {
            return false
        }
        konst firstSignature = createFlatSignature(firstCandidate, first)
        konst secondSignature = createFlatSignature(secondCandidate, second)
        return compareCallsByUsedArguments(firstSignature, secondSignature, discriminateGenerics = false, useOriginalSamTypes = false) &&
                compareCallsByUsedArguments(secondSignature, firstSignature, discriminateGenerics = false, useOriginalSamTypes = false)
    }

    private fun createFlatSignature(call: Candidate, declaration: FirCallableDeclaration): FlatSignature<Candidate> {
        return when (declaration) {
            is FirSimpleFunction -> createFlatSignature(call, declaration)
            is FirConstructor -> createFlatSignature(call, declaration)
            is FirVariable -> createFlatSignature(call, declaration)
            else -> error("Not supported: $declaration")
        }
    }
}
