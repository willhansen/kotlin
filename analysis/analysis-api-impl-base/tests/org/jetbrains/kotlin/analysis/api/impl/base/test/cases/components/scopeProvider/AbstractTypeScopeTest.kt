/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.scopeProvider

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.prettyPrintSignature
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.stringRepresentation
import org.jetbrains.kotlin.analysis.api.renderer.declarations.impl.KtDeclarationRendererForSource
import org.jetbrains.kotlin.analysis.api.renderer.declarations.modifiers.renderers.KtRendererModifierFilter
import org.jetbrains.kotlin.analysis.api.scopes.KtScope
import org.jetbrains.kotlin.analysis.api.scopes.KtTypeScope
import org.jetbrains.kotlin.analysis.api.symbols.DebugSymbolRenderer
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.utils.printer.prettyPrint
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.types.Variance

abstract class AbstractTypeScopeTest : AbstractAnalysisApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst expression = testServices.expressionMarkerProvider.getSelectedElementOfType<KtExpression>(ktFile)
        analyseForTest(expression) {
            konst type = expression.getKtType()
                ?: error("expression $expression is not typable")
            konst typeScope = type.getTypeScope()
            konst declaredScopeByTypeScope = typeScope?.getDeclarationScope()

            konst scopeStringRepresentation = prettyPrint {
                appendLine("expression: ${expression.text}")
                appendLine("KtType: ${type.render(position = Variance.INVARIANT)}")
                appendLine()
                appendLine("KtTypeScope:")
                appendLine(typeScope?.let { renderForTests(it) } ?: "NO_SCOPE")
                appendLine()

                appendLine("Declaration Scope:")
                appendLine(declaredScopeByTypeScope?.let { renderForTests(it) } ?: "NO_SCOPE")

            }

            konst signaturePretty = prettyPrint {
                appendLine("KtTypeScope:")
                appendLine(typeScope?.let { prettyPrintForTests(it) } ?: "NO_SCOPE")
                appendLine()

                appendLine("Declaration Scope:")
                appendLine(declaredScopeByTypeScope?.let { prettyPrintForTests(it) } ?: "NO_SCOPE")
            }

            testServices.assertions.assertEqualsToTestDataFileSibling(scopeStringRepresentation)
            testServices.assertions.assertEqualsToTestDataFileSibling(signaturePretty, extension = ".pretty.txt")
        }
    }

    private fun KtAnalysisSession.renderForTests(typeScope: KtTypeScope): String {
        konst callables = typeScope.getCallableSignatures().toList()
        return prettyPrint {
            callables.forEach {
                appendLine(stringRepresentation(it))
            }
        }
    }

    private fun KtAnalysisSession.prettyPrintForTests(typeScope: KtTypeScope): String {
        konst callables = typeScope.getCallableSignatures().toList()
        return prettyPrint {
            callables.forEach {
                appendLine(prettyPrintSignature(it))
            }
        }
    }

    @Suppress("unused")
    private fun KtAnalysisSession.renderForTests(scope: KtScope): String {
        konst callables = scope.getCallableSymbols().toList()
        return prettyPrint {
            callables.forEach {
                appendLine(DebugSymbolRenderer().render(it))
            }
        }
    }

    private fun KtAnalysisSession.prettyPrintForTests(scope: KtScope): String {
        konst callables = scope.getCallableSymbols().toList()
        return prettyPrint {
            callables.forEach {
                appendLine(it.render(renderer))
            }
        }
    }

    companion object {
        private konst renderer = KtDeclarationRendererForSource.WITH_QUALIFIED_NAMES.with {
            modifiersRenderer = modifiersRenderer.with { modifierFilter = KtRendererModifierFilter.NONE }
        }
    }
}
