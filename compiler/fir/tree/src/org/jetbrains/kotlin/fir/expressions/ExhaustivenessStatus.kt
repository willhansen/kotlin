/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.diagnostics.WhenMissingCase

sealed class ExhaustivenessStatus {

    /**
     * This konstue is used if the subject has type other than `Nothing`, in which case it's literally exhaustive only if type's possible
     * cases are properly covered.
     */
    object ProperlyExhaustive : ExhaustivenessStatus()

    /**
     *  This konstue is used if the subject has type `Nothing`, in which case even an empty `when` is considered exhaustive. Also, in this
     *  case, a synthetic else branch is created.
     */
    object ExhaustiveAsNothing : ExhaustivenessStatus()

    class NotExhaustive(konst reasons: List<WhenMissingCase>) : ExhaustivenessStatus() {
        companion object {
            konst NO_ELSE_BRANCH = NotExhaustive(listOf(WhenMissingCase.Unknown))
        }
    }
}


konst FirWhenExpression.isExhaustive: Boolean
    get() = exhaustivenessStatus == ExhaustivenessStatus.ProperlyExhaustive || exhaustivenessStatus == ExhaustivenessStatus.ExhaustiveAsNothing

konst FirWhenExpression.isProperlyExhaustive: Boolean
    get() = exhaustivenessStatus == ExhaustivenessStatus.ProperlyExhaustive