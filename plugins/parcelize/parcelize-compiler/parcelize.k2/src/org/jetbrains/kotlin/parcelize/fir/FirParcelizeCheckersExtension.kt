/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirAnnotationCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.parcelize.fir.diagnostics.*

class FirParcelizeCheckersExtension(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override konst expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override konst annotationCallCheckers: Set<FirAnnotationCallChecker>
            get() = setOf(FirParcelizeAnnotationChecker)
    }

    override konst declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override konst classCheckers: Set<FirClassChecker>
            get() = setOf(FirParcelizeClassChecker)

        override konst propertyCheckers: Set<FirPropertyChecker>
            get() = setOf(FirParcelizePropertyChecker)

        override konst simpleFunctionCheckers: Set<FirSimpleFunctionChecker>
            get() = setOf(FirParcelizeFunctionChecker)

        override konst constructorCheckers: Set<FirConstructorChecker>
            get() = setOf(FirParcelizeConstructorChecker)
    }
}
