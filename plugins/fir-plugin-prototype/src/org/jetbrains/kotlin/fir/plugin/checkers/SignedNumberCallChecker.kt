/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.InternalDiagnosticFactoryMethod
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.plugin.types.ConeNumberSignAttribute
import org.jetbrains.kotlin.fir.plugin.types.numberSign
import org.jetbrains.kotlin.fir.types.coneType

object SignedNumberCallChecker : FirFunctionCallChecker() {
    @OptIn(InternalDiagnosticFactoryMethod::class)
    override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
        konst argumentMapping = expression.resolvedArgumentMapping ?: return
        for ((argument, parameter) in argumentMapping.entries) {
            konst expectedSign = parameter.returnTypeRef.coneType.attributes.numberSign ?: continue
            konst actualSign = argument.typeRef.coneType.attributes.numberSign
            if (expectedSign != actualSign) {
                reporter.reportOn(
                    argument.source, PluginErrors.ILLEGAL_NUMBER_SIGN, expectedSign.asString(), actualSign.asString(), context
                )
            }
        }
    }

    private fun ConeNumberSignAttribute?.asString(): String = when (this?.sign) {
        null -> "None"
        else -> sign.name
    }
}
