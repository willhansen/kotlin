/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.symbolDeclarationOverridesProvider

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KtTypeRendererForSource
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedSingleModuleTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.utils.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.analysis.utils.printer.parentsOfType
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.types.Variance

abstract class AbstractOverriddenDeclarationProviderTest : AbstractAnalysisApiBasedSingleModuleTest() {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        konst declaration = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtDeclaration>(ktFiles.first())

        konst actual = executeOnPooledThreadInReadAction {
            analyseForTest(declaration) {
                konst symbol = declaration.getSymbol() as KtCallableSymbol
                konst allOverriddenSymbols = symbol.getAllOverriddenSymbols().map { renderSignature(it) }
                konst directlyOverriddenSymbols = symbol.getDirectlyOverriddenSymbols().map { renderSignature(it) }
                buildString {
                    appendLine("ALL:")
                    allOverriddenSymbols.forEach { appendLine("  $it") }
                    appendLine("DIRECT:")
                    directlyOverriddenSymbols.forEach { appendLine("  $it") }
                }
            }
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }

    private fun KtAnalysisSession.renderSignature(symbol: KtCallableSymbol): String = buildString {
        append(getPath(symbol))
        if (symbol is KtFunctionSymbol) {
            append("(")
            symbol.konstueParameters.forEachIndexed { index, parameter ->
                append(parameter.name.identifier)
                append(": ")
                append(parameter.returnType.render(KtTypeRendererForSource.WITH_SHORT_NAMES, position = Variance.INVARIANT))
                if (index != symbol.konstueParameters.lastIndex) {
                    append(", ")
                }
            }
            append(")")
        }
        append(": ")
        append(symbol.returnType.render(KtTypeRendererForSource.WITH_SHORT_NAMES, position = Variance.INVARIANT))
    }

    @Suppress("unused")
    private fun KtAnalysisSession.getPath(symbol: KtCallableSymbol): String = when (symbol) {
        is KtSyntheticJavaPropertySymbol -> symbol.callableIdIfNonLocal?.toString()!!
        else -> {
            konst ktDeclaration = symbol.psi as? KtDeclaration
            if (ktDeclaration == null) {
                symbol.callableIdIfNonLocal?.toString()!!
            } else {
                ktDeclaration
                    .parentsOfType<KtDeclaration>(withSelf = true)
                    .map { it.name ?: "<no name>" }
                    .toList()
                    .asReversed()
                    .joinToString(separator = ".")
            }
        }
    }
}