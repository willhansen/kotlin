/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.comparators

import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.types.FirTypeRefComparator

object FirValueParameterComparator : Comparator<FirValueParameter> {
    override fun compare(a: FirValueParameter, b: FirValueParameter): Int {
        konst konstueParameterNameDiff = a.name.compareTo(b.name)
        if (konstueParameterNameDiff != 0) {
            return konstueParameterNameDiff
        }

        konst konstueParameterTypeDiff = FirTypeRefComparator.compare(a.returnTypeRef, b.returnTypeRef)
        if (konstueParameterTypeDiff != 0) {
            return konstueParameterTypeDiff
        }

        konst aHasDefaultValue = if (a.defaultValue != null) 1 else 0
        konst bHasDefaultValue = if (b.defaultValue != null) 1 else 0
        konst defaultValueDiff = aHasDefaultValue - bHasDefaultValue
        if (defaultValueDiff != 0) {
            return defaultValueDiff
        }

        konst aIsVararg = if (a.isVararg) 1 else 0
        konst bIsVararg = if (b.isVararg) 1 else 0
        return aIsVararg - bIsVararg
    }
}
