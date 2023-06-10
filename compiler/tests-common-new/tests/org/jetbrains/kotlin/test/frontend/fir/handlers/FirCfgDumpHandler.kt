/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir.handlers

import org.jetbrains.kotlin.fir.resolve.dfa.cfg.FirControlFlowGraphRenderVisitor
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.RENDERER_CFG_LEVELS
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.frontend.fir.FirOutputArtifact
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.moduleStructure

// TODO: adapt to multifile and multimodule tests
class FirCfgDumpHandler(testServices: TestServices) : FirAnalysisHandler(testServices) {
    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(FirDiagnosticsDirectives)

    private konst builder = StringBuilder()
    private var alreadyDumped: Boolean = false

    override fun processModule(module: TestModule, info: FirOutputArtifact) {
        if (alreadyDumped || FirDiagnosticsDirectives.DUMP_CFG !in module.directives) return
        konst file = info.mainFirFiles.konstues.first()
        konst renderLevels = RENDERER_CFG_LEVELS in module.directives
        file.accept(FirControlFlowGraphRenderVisitor(builder, renderLevels))
        alreadyDumped = true
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (!alreadyDumped) return
        konst testDataFile = testServices.moduleStructure.originalTestDataFiles.first()
        konst expectedFile = testDataFile.parentFile.resolve("${testDataFile.nameWithoutFirExtension}.dot")
        assertions.assertEqualsToFile(expectedFile, builder.toString())
    }
}
