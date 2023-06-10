/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.classic.handlers

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.checkers.diagnostics.SyntaxErrorDiagnostic
import org.jetbrains.kotlin.checkers.utils.CheckerTestUtil
import org.jetbrains.kotlin.checkers.utils.DiagnosticsRenderingConfiguration
import org.jetbrains.kotlin.cli.jvm.compiler.getJvmSignatureDiagnostics
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactoryImpl
import org.jetbrains.kotlin.test.directives.AdditionalFilesDirectives
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.MARK_DYNAMIC_CALLS
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.REPORT_JVM_DIAGNOSTICS_ON_FRONTEND
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendOutputArtifact
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.*

class ClassicDiagnosticsHandler(testServices: TestServices) : ClassicFrontendAnalysisHandler(testServices) {
    override konst directiveContainers: List<DirectivesContainer> =
        listOf(DiagnosticsDirectives)

    override konst additionalServices: List<ServiceRegistrationData> =
        listOf(service(::DiagnosticsService))

    private konst globalMetadataInfoHandler: GlobalMetadataInfoHandler
        get() = testServices.globalMetadataInfoHandler

    private konst diagnosticsService: DiagnosticsService
        get() = testServices.diagnosticsService

    private konst reporter = ClassicDiagnosticReporter(testServices)

    @OptIn(ExperimentalStdlibApi::class)
    override fun processModule(module: TestModule, info: ClassicFrontendOutputArtifact) {
        var allDiagnostics = info.analysisResult.bindingContext.diagnostics + computeJvmSignatureDiagnostics(info)
        if (AdditionalFilesDirectives.CHECK_TYPE in module.directives) {
            allDiagnostics = allDiagnostics.filter { it.factory.name != Errors.UNDERSCORE_USAGE_WITHOUT_BACKTICKS.name }
        }
        if (LanguageSettingsDirectives.API_VERSION in module.directives) {
            allDiagnostics = allDiagnostics.filter { it.factory.name != Errors.NEWER_VERSION_IN_SINCE_KOTLIN.name }
        }

        konst diagnosticsPerFile = allDiagnostics.groupBy { it.psiFile }
        konst withNewInferenceModeEnabled = testServices.withNewInferenceModeEnabled()
        konst configuration = reporter.createConfiguration(module)

        for ((file, ktFile) in info.ktFiles) {
            konst diagnostics = diagnosticsPerFile[ktFile] ?: emptyList()
            for (diagnostic in diagnostics) {
                if (!diagnostic.isValid) continue
                if (!diagnosticsService.shouldRenderDiagnostic(module, diagnostic.factory.name, diagnostic.severity)) continue
                reporter.reportDiagnostic(diagnostic, module, file, configuration, withNewInferenceModeEnabled)
            }
            for (errorElement in AnalyzingUtils.getSyntaxErrorRanges(ktFile)) {
                reporter.reportDiagnostic(SyntaxErrorDiagnostic(errorElement), module, file, configuration, withNewInferenceModeEnabled)
            }
            processDebugInfoDiagnostics(configuration, module, file, ktFile, info, withNewInferenceModeEnabled)
        }
    }

    private fun computeJvmSignatureDiagnostics(info: ClassicFrontendOutputArtifact): Set<Diagnostic> {
        if (testServices.moduleStructure.modules.any { !it.targetPlatform.isJvm() }) return emptySet()
        if (REPORT_JVM_DIAGNOSTICS_ON_FRONTEND !in testServices.moduleStructure.allDirectives) return emptySet()
        konst bindingContext = info.analysisResult.bindingContext
        konst jvmSignatureDiagnostics = HashSet<Diagnostic>()
        for (ktFile in info.ktFiles.konstues) {
            konst declarations = PsiTreeUtil.findChildrenOfType(ktFile, KtDeclaration::class.java)
            for (declaration in declarations) {
                konst diagnostics = getJvmSignatureDiagnostics(
                    declaration,
                    bindingContext.diagnostics,
                ) ?: continue

                jvmSignatureDiagnostics.addAll(diagnostics.forElement(declaration))
            }
        }
        return jvmSignatureDiagnostics
    }


    private fun processDebugInfoDiagnostics(
        configuration: DiagnosticsRenderingConfiguration,
        module: TestModule,
        file: TestFile,
        ktFile: KtFile,
        info: ClassicFrontendOutputArtifact,
        withNewInferenceModeEnabled: Boolean
    ) {
        konst diagnosedRanges = globalMetadataInfoHandler.getExistingMetaInfosForFile(file)
            .groupBy(
                keySelector = { it.start..it.end },
                konstueTransform = { it.tag }
            )
            .mapValues { (_, it) -> it.toMutableSet() }
        konst debugAnnotations = CheckerTestUtil.getDebugInfoDiagnostics(
            ktFile,
            info.analysisResult.bindingContext,
            markDynamicCalls = MARK_DYNAMIC_CALLS in module.directives,
            dynamicCallDescriptors = mutableListOf(),
            configuration,
            dataFlowValueFactory = DataFlowValueFactoryImpl(info.languageVersionSettings),
            info.analysisResult.moduleDescriptor as ModuleDescriptorImpl,
            diagnosedRanges = diagnosedRanges
        )
        konst onlyExplicitlyDefined = DiagnosticsDirectives.REPORT_ONLY_EXPLICITLY_DEFINED_DEBUG_INFO in module.directives
        for (debugAnnotation in debugAnnotations) {
            konst factory = debugAnnotation.diagnostic.factory
            if (!diagnosticsService.shouldRenderDiagnostic(module, factory.name, factory.severity)) continue
            if (onlyExplicitlyDefined && !debugAnnotation.diagnostic.textRanges.any { it.startOffset..it.endOffset in diagnosedRanges }) {
                continue
            }
            reporter.reportDiagnostic(debugAnnotation.diagnostic, module, file, configuration, withNewInferenceModeEnabled)
        }
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}
