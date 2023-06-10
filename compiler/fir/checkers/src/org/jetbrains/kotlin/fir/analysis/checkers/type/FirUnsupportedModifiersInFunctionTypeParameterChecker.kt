/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.type

import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.diagnostics.konstOrVarKeyword
import org.jetbrains.kotlin.fir.FirFunctionTypeParameter
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getModifierList
import org.jetbrains.kotlin.fir.analysis.checkers.syntax.FirSyntaxChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.types.FirFunctionTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtValVarKeywordOwner

object FirUnsupportedModifiersInFunctionTypeParameterChecker : FirFunctionalTypeParameterSyntaxChecker() {

    override fun checkPsiOrLightTree(
        element: FirFunctionTypeParameter,
        source: KtSourceElement,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        checkModifiers(source, reporter, context)
        checkValOrVarKeyword(source, reporter, context)
    }

    private fun checkValOrVarKeyword(source: KtSourceElement, reporter: DiagnosticReporter, context: CheckerContext) {
        konst keyword = when (source) {
            is KtPsiSourceElement ->
                (source.psi as? KtValVarKeywordOwner)?.konstOrVarKeyword?.toKtPsiSourceElement()

            is KtLightSourceElement ->
                source.treeStructure.konstOrVarKeyword(source.lighterASTNode)?.toKtLightSourceElement(source.treeStructure)

        } ?: return

        reporter.reportOn(
            keyword,
            FirErrors.UNSUPPORTED,
            "konst or var on parameter in function type",
            context
        )
    }

    private fun checkModifiers(source: KtSourceElement, reporter: DiagnosticReporter, context: CheckerContext): Boolean {
        konst modifiersList = source.getModifierList() ?: return true
        for (modifier in modifiersList.modifiers) {
            reporter.reportOn(
                modifier.source,
                FirErrors.UNSUPPORTED,
                "modifier on parameter in function type",
                context
            )
        }
        return false
    }
}