/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.multiplatformInfoProvider

import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedTest
import org.jetbrains.kotlin.analysis.test.framework.project.structure.ktModuleProvider
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.utils.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractExpectForActualTest : AbstractAnalysisApiBasedTest() {
    override fun doTestByModuleStructure(moduleStructure: TestModuleStructure, testServices: TestServices) {
        konst (declaration, _) = moduleStructure.modules.flatMap { module ->
            konst ktFiles = testServices.ktModuleProvider.getModuleFiles(module).filterIsInstance<KtFile>()
            testServices.expressionMarkerProvider.getElementsOfTypeAtCarets<KtDeclaration>(ktFiles)
        }.single()

        konst expectedSymbolText: String? = executeOnPooledThreadInReadAction {
            analyseForTest(declaration) {
                konst expectedSymbol = declaration.getSymbol().getExpectForActual() ?: return@analyseForTest null
                expectedSymbol.psi?.containingFile?.name + " : " + expectedSymbol.render()
            }
        }

        konst actual = buildString {
            appendLine("expected symbol: $expectedSymbolText")
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}