/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.plugin.checkers.DummyNameChecker
import org.jetbrains.kotlin.fir.plugin.checkers.SignedNumberCallChecker

class PluginAdditionalCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override konst declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override konst simpleFunctionCheckers: Set<FirSimpleFunctionChecker>
            get() = setOf(DummyNameChecker)
    }

    override konst expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
        override konst functionCallCheckers: Set<FirFunctionCallChecker>
            get() = setOf(SignedNumberCallChecker)
    }
}
