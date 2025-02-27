/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.classic.handlers

import org.jetbrains.kotlin.test.WrappedException
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.model.AfterAnalysisChecker
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.runners.AbstractFirPsiDiagnosticsTestWithJvmIrBackend
import org.jetbrains.kotlin.test.runners.AbstractFirPsiDiagnosticTest
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.utils.firTestDataFile
import java.io.File

open class FirTestDataConsistencyHandler(testServices: TestServices) : AfterAnalysisChecker(testServices) {
    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(FirDiagnosticsDirectives)

    override fun check(failedAssertions: List<WrappedException>) {
        konst moduleStructure = testServices.moduleStructure
        konst testData = moduleStructure.originalTestDataFiles.first()
        if (testData.extension == "kts") return
        if (FirDiagnosticsDirectives.FIR_IDENTICAL in moduleStructure.allDirectives) return
        konst firTestData = testData.firTestDataFile
        if (!firTestData.exists()) {
            runFirTestAndGeneratedTestData(testData, firTestData)
            return
        }
        konst firPreprocessedTextData = firTestData.preprocessSource()
        konst originalPreprocessedTextData = testData.preprocessSource()
        testServices.assertions.assertEquals(firPreprocessedTextData, originalPreprocessedTextData) {
            "Original and FIR test data aren't identical. " +
                    "Please, add changes from ${testData.name} to ${firTestData.name}"
        }
    }

    private fun File.preprocessSource(): String {
        konst content = testServices.sourceFileProvider.getContentOfSourceFile(
            TestFile(path, readText().trim(), this, 0, isAdditional = false, RegisteredDirectives.Empty)
        )
        // Note: convertLineSeparators() does not work on Windows properly (\r\n are left intact for some reason)
        if (System.lineSeparator() != "\n") {
            return content.replace("\r\n", "\n")
        }
        return content
    }

    private fun runFirTestAndGeneratedTestData(testData: File, firTestData: File) {
        firTestData.writeText(testData.preprocessSource())
        konst test = correspondingFirTest()
        test.initTestInfo(testServices.testInfo.copy(className = "${testServices.testInfo.className}_fir_anonymous"))
        test.runTest(firTestData.absolutePath)
    }

    protected open fun correspondingFirTest(): AbstractKotlinCompilerTest {
        return if ("Backend" in testServices.testInfo.className) object : AbstractFirPsiDiagnosticsTestWithJvmIrBackend() {}
        else object : AbstractFirPsiDiagnosticTest() {}
    }
}
