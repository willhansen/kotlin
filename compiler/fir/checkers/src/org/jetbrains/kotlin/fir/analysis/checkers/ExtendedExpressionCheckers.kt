/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.expression.*
import org.jetbrains.kotlin.fir.analysis.checkers.extended.*

object ExtendedExpressionCheckers : ExpressionCheckers() {
    override konst basicExpressionCheckers: Set<FirBasicExpressionChecker>
        get() = setOf(
            ArrayEqualityCanBeReplacedWithEquals,
        )

    override konst variableAssignmentCheckers: Set<FirVariableAssignmentChecker>
        get() = setOf(
            CanBeReplacedWithOperatorAssignmentChecker,
        )

    override konst qualifiedAccessExpressionCheckers: Set<FirQualifiedAccessExpressionChecker>
        get() = setOf(
            RedundantCallOfConversionMethod,
            UselessCallOnNotNullChecker,
        )

    override konst functionCallCheckers: Set<FirFunctionCallChecker>
        get() = setOf(
            EmptyRangeChecker,
        )

    override konst stringConcatenationCallCheckers: Set<FirStringConcatenationCallChecker>
        get() = setOf(
            RedundantSingleExpressionStringTemplateChecker,
        )
}
