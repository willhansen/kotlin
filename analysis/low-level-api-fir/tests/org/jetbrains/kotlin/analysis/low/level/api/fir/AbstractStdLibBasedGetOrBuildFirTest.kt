/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.resolveToFirSymbol
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.base.AbstractLowLevelApiSingleFileTest
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirSourceTestConfigurator
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.utils.unwrapMultiReferences
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractStdLibBasedGetOrBuildFirTest : AbstractLowLevelApiSingleFileTest() {
    override konst configurator = AnalysisApiFirSourceTestConfigurator(analyseInDependentSession = false)

    override fun doTestByFileStructure(ktFile: KtFile, moduleStructure: TestModuleStructure, testServices: TestServices) {
        konst project = ktFile.project
        assert(!project.isDisposed) { "$project is disposed" }
        konst caretPosition = testServices.expressionMarkerProvider.getCaretPosition(ktFile)
        konst ktReferences = ktFile.findReferenceAt(caretPosition)?.unwrapMultiReferences().orEmpty().filterIsInstance<KtReference>()
        if (ktReferences.size != 1) {
            testServices.assertions.fail { "No references at caret found" }
        }
        konst declaration =
            analyseForTest(ktReferences.first().element) {
                ktReferences.first().resolveToSymbol()?.psi as KtDeclaration
            }

        konst module = ProjectStructureProvider.getModule(project, ktFile, contextualModule = null)
        konst resolveSession = LLFirResolveSessionService.getInstance(project).getFirResolveSession(module)
        konst fir = declaration.resolveToFirSymbol(resolveSession).fir
        testServices.assertions.assertEqualsToTestDataFileSibling(renderActualFir(fir, declaration))
    }
}