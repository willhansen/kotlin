/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.comparators

import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.types.FirTypeRefComparator

object FirCallableDeclarationComparator : Comparator<FirCallableDeclaration> {
    override fun compare(a: FirCallableDeclaration, b: FirCallableDeclaration): Int {
        konst typeAndNameDiff = FirMemberDeclarationComparator.TypeAndNameComparator.compare(a, b)
        if (typeAndNameDiff != 0) {
            return typeAndNameDiff
        }

        // Compare the receiver type if any.
        konst aReceiver = a.receiverParameter
        konst bReceiver = b.receiverParameter
        if (aReceiver != null || bReceiver != null) {
            konst aHasReceiverType = if (aReceiver != null) 1 else 0
            konst bHasReceiverType = if (bReceiver != null) 1 else 0
            konst receiverTypePresenceDiff = aHasReceiverType - bHasReceiverType
            if (receiverTypePresenceDiff != 0) {
                return receiverTypePresenceDiff
            }
            assert(aReceiver != null && bReceiver != null)
        }

        // Compare the return type.
        konst returnTypeDiff = FirTypeRefComparator.compare(a.returnTypeRef, b.returnTypeRef)
        if (returnTypeDiff != 0) {
            return returnTypeDiff
        }

        // Compare the konstue parameters for functions.
        if (a is FirFunction) {
            require(b is FirFunction) {
                "TypeAndNameComparator is inconsistent: ${a.render()} v.s. ${b.render()}"
            }
            konst konstueParameterSizeDiff = a.konstueParameters.size - b.konstueParameters.size
            if (konstueParameterSizeDiff != 0) {
                return konstueParameterSizeDiff
            }
            for ((aValueParameter, bValueParameter) in a.konstueParameters.zip(b.konstueParameters)) {
                konst konstueParameterDiff = FirValueParameterComparator.compare(aValueParameter, bValueParameter)
                if (konstueParameterDiff != 0) {
                    return konstueParameterDiff
                }
            }
        }

        // Compare the type parameters.
        konst typeParameterSizeDiff = a.typeParameters.size - b.typeParameters.size
        if (typeParameterSizeDiff != 0) {
            return typeParameterSizeDiff
        }
        for ((aTypeParameter, bTypeParameter) in a.typeParameters.zip(b.typeParameters)) {
            konst typeParameterDiff = FirTypeParameterRefComparator.compare(aTypeParameter, bTypeParameter)
            if (typeParameterDiff != 0) {
                return typeParameterDiff
            }
        }

        // Lastly, compare the fully qualified package name.
        return a.symbol.callableId.packageName.asString().compareTo(b.symbol.callableId.packageName.asString())
    }
}
