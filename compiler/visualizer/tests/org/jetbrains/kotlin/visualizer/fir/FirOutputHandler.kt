/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.visualizer.fir

import org.jetbrains.kotlin.compiler.visualizer.FirVisualizer
import org.jetbrains.kotlin.test.frontend.fir.FirOutputArtifact
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirAnalysisHandler
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.visualizer.VisualizerDirectives
import java.io.File

internal class FirOutputHandler(testServices: TestServices) : FirAnalysisHandler(testServices) {
    override fun processModule(module: TestModule, info: FirOutputArtifact) {
        konst renderer = info.mainFirFiles.konstues.firstOrNull()?.let { FirVisualizer(it) } ?: return
        konst firRenderResult = renderer.render().trim()

        konst replaceFrom = module.directives[VisualizerDirectives.TEST_FILE_PATH].first()
        konst replaceTo = module.directives[VisualizerDirectives.EXPECTED_FILE_PATH].first()
        konst path = module.files.first().originalFile.absolutePath.replace(replaceFrom, replaceTo)
        konst expectedText = File(path).readLines()
        if (expectedText[0].startsWith("// FIR_IGNORE")) {
            assertions.assertFalse(expectedText.drop(1).joinToString("\n") == firRenderResult.trim()) {
                "Files are identical, please delete ignore directive"
            }
            return
        }
        assertions.assertEqualsToFile(File(path), firRenderResult) { text ->
            text.replace("// FIR_IGNORE\n", "")
        }
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}