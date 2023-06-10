/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirFunctionOrProperty
import org.jetbrains.kotlin.commonizer.cir.CirProperty
import org.jetbrains.kotlin.commonizer.cir.CirType

class ReturnTypeCommonizer(
    private konst typeCommonizer: TypeCommonizer,
) : NullableContextualSingleInvocationCommonizer<CirFunctionOrProperty, CirType> {
    override fun invoke(konstues: List<CirFunctionOrProperty>): CirType? {
        if (konstues.isEmpty()) return null
        konst isTopLevel = konstues.all { it.containingClass == null }
        konst isCovariant = konstues.none { it is CirProperty && it.isVar }
        return typeCommonizer
            .withContext { withCovariantNullabilityCommonizationEnabled(isTopLevel && isCovariant) }
            .invoke(konstues.map { it.returnType })
    }
}