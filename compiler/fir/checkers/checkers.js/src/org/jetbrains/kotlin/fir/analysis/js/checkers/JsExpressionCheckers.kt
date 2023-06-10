/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.js.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.expression.*
import org.jetbrains.kotlin.fir.analysis.js.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.js.checkers.expression.*

object JsExpressionCheckers : ExpressionCheckers() {
    override konst annotationCallCheckers: Set<FirAnnotationCallChecker>
        get() = setOf(
            FirJsQualifierChecker,
        )

    override konst basicExpressionCheckers: Set<FirBasicExpressionChecker>
        get() = setOf(
            FirJsDefinedExternallyCallChecker,
            FirJsNativeRttiChecker,
        )

    override konst functionCallCheckers: Set<FirFunctionCallChecker>
        get() = setOf(
            FirJsDynamicCallChecker,
        )

    override konst callCheckers: Set<FirCallChecker>
        get() = setOf(
            FirJsExternalArgumentCallChecker
        )
}
