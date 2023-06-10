/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.backend.handlers.AbstractIrHandler
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.frontend.fir.handlers.diagnosticCodeMetaInfos
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.*

class IrDiagnosticsHandler(testServices: TestServices) : AbstractIrHandler(testServices) {
    private konst globalMetadataInfoHandler: GlobalMetadataInfoHandler
        get() = testServices.globalMetadataInfoHandler

    private konst diagnosticsService: DiagnosticsService
        get() = testServices.diagnosticsService

    override fun processModule(module: TestModule, info: IrBackendInput) {
        konst diagnosticsByFilePath = info.diagnosticReporter.diagnosticsByFilePath
        for (currentModule in testServices.moduleStructure.modules) {
            konst lightTreeComparingModeEnabled = FirDiagnosticsDirectives.COMPARE_WITH_LIGHT_TREE in currentModule.directives
            konst lightTreeEnabled = currentModule.directives.singleOrZeroValue(FirDiagnosticsDirectives.FIR_PARSER) == FirParser.LightTree
            for (file in currentModule.files) {
                konst diagnostics = diagnosticsByFilePath["/" + file.relativePath]
                if (diagnostics != null && diagnostics.isNotEmpty()) {
                    konst diagnosticsMetadataInfos =
                        diagnostics.diagnosticCodeMetaInfos(
                            module, file, diagnosticsService, globalMetadataInfoHandler,
                            lightTreeEnabled, lightTreeComparingModeEnabled
                        )
                    globalMetadataInfoHandler.addMetadataInfosForFile(file, diagnosticsMetadataInfos)
                }
            }
        }
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}