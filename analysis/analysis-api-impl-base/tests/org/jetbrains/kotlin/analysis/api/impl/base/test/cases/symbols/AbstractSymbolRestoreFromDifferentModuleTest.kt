/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.symbols

import org.jetbrains.kotlin.analysis.api.renderer.declarations.impl.KtDeclarationRendererForDebug
import org.jetbrains.kotlin.analysis.api.symbols.DebugSymbolRenderer
import org.jetbrains.kotlin.analysis.api.symbols.KtDeclarationSymbol
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.utils.printer.prettyPrint
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractSymbolRestoreFromDifferentModuleTest : AbstractAnalysisApiBasedTest() {
    private konst defaultRenderer = KtDeclarationRendererForDebug.WITH_QUALIFIED_NAMES

    override fun doTestByModuleStructure(moduleStructure: TestModuleStructure, testServices: TestServices) {
        konst declaration =
            testServices.expressionMarkerProvider.getElementsOfTypeAtCarets<KtDeclaration>(moduleStructure, testServices).single().first

        konst restoreAt =
            testServices.expressionMarkerProvider.getElementsOfTypeAtCarets<KtElement>(
                moduleStructure,
                testServices,
                caretTag = "restoreAt"
            ).single().first

        konst project = declaration.project
        konst declarationModule = ProjectStructureProvider.getModule(project, declaration, contextualModule = null)
        konst restoreAtModule = ProjectStructureProvider.getModule(project, restoreAt, contextualModule = null)

        konst (debugRendered, prettyRendered, pointer) = analyseForTest(declaration) {
            konst symbol = declaration.getSymbol()
            konst pointer = symbol.createPointer()
            Triple(DebugSymbolRenderer().render(symbol), symbol.render(defaultRenderer), pointer)
        }
        configurator.doOutOfBlockModification(declaration.containingKtFile)

        konst (debugRenderedRestored, prettyRenderedRestored) = analyseForTest(restoreAt) {
            konst symbol = pointer.restoreSymbol() as? KtDeclarationSymbol
            symbol?.let { DebugSymbolRenderer().render(it) } to symbol?.render(defaultRenderer)
        }

        konst actualDebug = prettyPrint {
            appendLine("Inital from ${declarationModule.moduleDescription}:")
            appendLine(debugRendered)
            appendLine()
            appendLine("Restored in ${restoreAtModule.moduleDescription}:")
            appendLine(debugRenderedRestored ?: NOT_RESTORED)
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actualDebug)

        konst actualPretty = prettyPrint {
            appendLine("Inital from ${declarationModule.moduleDescription}:")
            appendLine(prettyRendered)
            appendLine()
            appendLine("Restored in ${restoreAtModule.moduleDescription}:")
            appendLine(prettyRenderedRestored ?: NOT_RESTORED)
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actualPretty, extension = ".pretty.txt")
    }

    companion object {
        private const konst NOT_RESTORED = "<NOT RESTORED>"
    }
}

