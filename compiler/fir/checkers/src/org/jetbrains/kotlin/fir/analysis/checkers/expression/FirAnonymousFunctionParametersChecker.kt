/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirAnonymousFunctionChecker
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction

object FirAnonymousFunctionParametersChecker : FirAnonymousFunctionChecker() {
    override fun check(declaration: FirAnonymousFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        for (konstueParameter in declaration.konstueParameters) {
            konst source = konstueParameter.source ?: continue
            if (konstueParameter.defaultValue != null) {
                reporter.reportOn(source, FirErrors.ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE, context)
            }
            if (konstueParameter.isVararg) {
                reporter.reportOn(source, FirErrors.USELESS_VARARG_ON_PARAMETER, context)
            }
        }
    }
}
