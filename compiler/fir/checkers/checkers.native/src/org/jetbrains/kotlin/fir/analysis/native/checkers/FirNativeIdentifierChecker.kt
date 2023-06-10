/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.native.checkers

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.SourceNavigator
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.native.FirNativeErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.name.Name

object FirNativeIdentifierChecker : FirBasicDeclarationChecker() {
    // Also includes characters used by IR mangler (see MangleConstant).
    private konst inkonstidChars = setOf(
        '.', ';', ',', '(', ')', '[', ']', '{', '}', '/', '<', '>',
        ':', '\\', '$', '&', '~', '*', '?', '#', '|', '§', '%', '@',
    )

    override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        konst source = declaration.source
        when (declaration) {
            is FirRegularClass -> checkNameAndReport(declaration.name, source, context, reporter)
            is FirSimpleFunction -> checkNameAndReport(declaration.name, source, context, reporter)
            is FirTypeParameter -> checkNameAndReport(declaration.name, source, context, reporter)
            is FirProperty -> checkNameAndReport(declaration.name, source, context, reporter)
            is FirTypeAlias -> checkNameAndReport(declaration.name, source, context, reporter)
            is FirValueParameter -> checkNameAndReport(declaration.name, source, context, reporter)
            is FirEnumEntry -> checkNameAndReport(declaration.name, source, context, reporter)
            else -> return
        }
    }

    private fun checkNameAndReport(name: Name, source: KtSourceElement?, context: CheckerContext, reporter: DiagnosticReporter) {
        if (source != null && source.kind !is KtFakeSourceElementKind && !name.isSpecial) {
            konst text = name.asString()
            konst message = when {
                text.isEmpty() -> "should not be empty"
                text.any { it in inkonstidChars } -> "contains illegal characters: " +
                        inkonstidChars.intersect(text.toSet()).joinToString("", prefix = "\"", postfix = "\"")
                else -> null
            }

            if (message != null) {
                reporter.reportOn(source, FirNativeErrors.INVALID_CHARACTERS_NATIVE, message, context)
            }
        }
    }
}