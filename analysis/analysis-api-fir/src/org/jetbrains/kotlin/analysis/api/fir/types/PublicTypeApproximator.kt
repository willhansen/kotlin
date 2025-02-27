/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.types

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.typeApproximator
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration

internal object PublicTypeApproximator {
    fun approximateTypeToPublicDenotable(
        type: ConeKotlinType,
        session: FirSession,
        approximateLocalTypes: Boolean
    ): ConeKotlinType? {
        konst approximator = session.typeApproximator
        return approximator.approximateToSuperType(type, PublicApproximatorConfiguration(approximateLocalTypes))
    }

    internal class PublicApproximatorConfiguration(
        override konst localTypes: Boolean
    ) : TypeApproximatorConfiguration.AllFlexibleSameValue() {
        override konst allFlexible: Boolean get() = false
        override konst errorType: Boolean get() = true
        override konst definitelyNotNullType: Boolean get() = false
        override konst integerLiteralConstantType: Boolean get() = true
        override konst intersectionTypesInContravariantPositions: Boolean get() = true
        override konst anonymous: Boolean get() = true
    }
}
