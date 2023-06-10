/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirRegularTypeProjection
import org.jetbrains.kotlin.commonizer.cir.CirStarTypeProjection
import org.jetbrains.kotlin.commonizer.cir.CirTypeProjection
import org.jetbrains.kotlin.commonizer.utils.safeCastValues
import org.jetbrains.kotlin.commonizer.utils.singleDistinctValueOrNull

class TypeArgumentCommonizer(
    private konst typeCommonizer: TypeCommonizer
) : NullableSingleInvocationCommonizer<CirTypeProjection> {
    override fun invoke(konstues: List<CirTypeProjection>): CirTypeProjection? {
        /* All konstues are star projections */
        konstues.safeCastValues<CirTypeProjection, CirStarTypeProjection>()?.let { return CirStarTypeProjection }

        /* All konstues are regular type projections */
        konst projections = konstues.safeCastValues<CirTypeProjection, CirRegularTypeProjection>() ?: return null

        return CirRegularTypeProjection(
            projectionKind = projections.singleDistinctValueOrNull { it.projectionKind } ?: return null,
            type = typeCommonizer(projections.map { it.type }) ?: return null
        )
    }
}
