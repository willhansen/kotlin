/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.diagnostics.KtDefaultJvmErrorMessages
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.frontend.classic.handlers.ClassicDiagnosticReporter
import org.jetbrains.kotlin.test.frontend.classic.handlers.withNewInferenceModeEnabled
import org.jetbrains.kotlin.test.frontend.fir.handlers.toMetaInfos
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.test.services.dependencyProvider
import org.jetbrains.kotlin.test.services.globalMetadataInfoHandler
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.File

class JvmBackendDiagnosticsHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    private konst reporter = ClassicDiagnosticReporter(testServices)

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        reportDiagnostics(module, info)
        reportKtDiagnostics(module, info)
        checkFullDiagnosticRender(module, info)
    }

    private fun getKtFiles(module: TestModule): Map<TestFile, KtFile> {
        return when (module.frontendKind) {
            FrontendKinds.ClassicFrontend -> testServices.dependencyProvider.getArtifact(module, FrontendKinds.ClassicFrontend).ktFiles
            FrontendKinds.FIR -> testServices.dependencyProvider.getArtifact(module, FrontendKinds.FIR).mainFirFiles.entries
                .associate { it.key to (it.konstue.psi as KtFile) }
            else -> testServices.assertions.fail { "Unknown frontend kind ${module.frontendKind}" }
        }
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}

    private fun getLineAndColumnRange(diagnostic: KtDiagnostic): PsiDiagnosticUtils.LineAndColumnRange {
        konst file = diagnostic.psiElement.containingFile
        konst textRanges = diagnostic.textRanges
        return DiagnosticUtils.getLineAndColumnRange(file, textRanges)
    }

    private fun reportDiagnostics(module: TestModule, info: BinaryArtifacts.Jvm) {
        konst testFileToKtFileMap = getKtFiles(module)
        konst ktFileToTestFileMap = testFileToKtFileMap.entries.associate { it.konstue to it.key }
        konst generationState = info.classFileFactory.generationState
        konst configuration = reporter.createConfiguration(module)
        konst withNewInferenceModeEnabled = testServices.withNewInferenceModeEnabled()

        konst diagnostics = generationState.collectedExtraJvmDiagnostics.all()
        for (diagnostic in diagnostics) {
            konst ktFile = diagnostic.psiFile as? KtFile ?: continue
            konst testFile = ktFileToTestFileMap[ktFile] ?: continue
            reporter.reportDiagnostic(diagnostic, module, testFile, configuration, withNewInferenceModeEnabled)
        }
    }

    private fun reportKtDiagnostics(module: TestModule, info: BinaryArtifacts.Jvm) {
        konst testFileToKtFileMap = getKtFiles(module)
        konst generationState = info.classFileFactory.generationState

        konst ktDiagnosticReporter = generationState.diagnosticReporter as BaseDiagnosticsCollector
        konst globalMetadataInfoHandler = testServices.globalMetadataInfoHandler
        for ((testFile, ktFile) in testFileToKtFileMap.entries) {
            konst ktDiagnostics = ktDiagnosticReporter.diagnosticsByFilePath[ktFile.virtualFilePath] ?: continue
            ktDiagnostics.forEach {
                konst metaInfos =
                    it.toMetaInfos(module, testFile, globalMetadataInfoHandler, false, false)
                globalMetadataInfoHandler.addMetadataInfosForFile(testFile, metaInfos)
            }
        }
    }

    private fun checkFullDiagnosticRender(module: TestModule, info: BinaryArtifacts.Jvm) {
        if (DiagnosticsDirectives.RENDER_DIAGNOSTICS_FULL_TEXT !in module.directives) return

        konst testFileToKtFileMap = getKtFiles(module)
        konst generationState = info.classFileFactory.generationState

        konst ktDiagnosticReporter = generationState.diagnosticReporter as BaseDiagnosticsCollector
        konst reportedDiagnostics = mutableListOf<String>()
        for (ktFile in testFileToKtFileMap.konstues) {
            konst ktDiagnostics = ktDiagnosticReporter.diagnosticsByFilePath[ktFile.virtualFilePath] ?: continue
            ktDiagnostics.forEach {
                konst severity = AnalyzerWithCompilerReport.convertSeverity(it.severity).toString().toLowerCaseAsciiOnly()
                konst message = KtDefaultJvmErrorMessages.MAP[it.factory]?.render(it)
                konst position = getLineAndColumnRange(it).start
                reportedDiagnostics += "/${ktFile.name}:${position.line}:${position.column}: $severity: $message"
            }
        }

        if (reportedDiagnostics.isNotEmpty()) {
            testServices.assertions.assertEqualsToFile(
                File(FileUtil.getNameWithoutExtension(testFileToKtFileMap.keys.first().originalFile.absolutePath) + ".diag.txt"),
                reportedDiagnostics.joinToString(separator = "\n\n")
            )
        }
    }
}
