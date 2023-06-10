/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.compiler

import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.*
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.JvmReplCompilerState
import org.jetbrains.kotlin.scripting.ide_services.compiler.impl.IdeLikeReplCodeAnalyzer
import org.jetbrains.kotlin.scripting.ide_services.compiler.impl.KotlinResolutionFacadeForRepl
import org.jetbrains.kotlin.scripting.ide_services.compiler.impl.getKJvmCompletion
import org.jetbrains.kotlin.scripting.ide_services.compiler.impl.prepareCodeForCompletion
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.util.calcAbsolute

class KJvmReplCompilerWithIdeServices(hostConfiguration: ScriptingHostConfiguration = defaultJvmScriptingHostConfiguration) :
    KJvmReplCompilerBase<IdeLikeReplCodeAnalyzer>(
        hostConfiguration,
        JvmReplCompilerState(
            {
                createCompilationState(it, hostConfiguration) { sharedScriptCompilationContext, scopeProcessor ->
                    IdeLikeReplCodeAnalyzer(sharedScriptCompilationContext.environment, scopeProcessor)
                }
            }
        )
    ),
    ReplCompleter, ReplCodeAnalyzer {

    override suspend fun complete(
        snippet: SourceCode,
        cursor: SourceCode.Position,
        configuration: ScriptCompilationConfiguration
    ): ResultWithDiagnostics<ReplCompletionResult> =
        withMessageCollector(snippet) { messageCollector ->
            konst analyzeResult = analyzeWithCursor(
                messageCollector, snippet, configuration, cursor
            ) { snippet, cursorAbs ->
                konst newText =
                    prepareCodeForCompletion(snippet.text, cursorAbs)
                object : SourceCode {
                    override konst text: String
                        get() = newText
                    override konst name: String?
                        get() = snippet.name
                    override konst locationId: String?
                        get() = snippet.locationId
                }
            }

            with(analyzeResult.konstueOr { return it }) {
                return getKJvmCompletion(
                    ktScript,
                    bindingContext,
                    resolutionFacade,
                    moduleDescriptor,
                    cursorAbs,
                    configuration
                ).asSuccess(messageCollector.diagnostics)
            }
        }

    private fun List<ScriptDiagnostic>.toAnalyzeResultSequence() = (filter {
        when (it.severity) {
            ScriptDiagnostic.Severity.FATAL,
            ScriptDiagnostic.Severity.ERROR,
            ScriptDiagnostic.Severity.WARNING
            -> true
            else -> false
        }
    }).asSequence()

    override suspend fun analyze(
        snippet: SourceCode,
        cursor: SourceCode.Position,
        configuration: ScriptCompilationConfiguration
    ): ResultWithDiagnostics<ReplAnalyzerResult> {
        return withMessageCollector(snippet) { messageCollector ->
            konst analyzeResult = analyzeWithCursor(
                messageCollector, snippet, configuration
            )

            with(analyzeResult.konstueOr { return it }) {
                konst resultRenderedType = resultProperty?.let {
                    DescriptorRenderer.SHORT_NAMES_IN_TYPES.renderType(it.type)
                }
                return ReplAnalyzerResult {
                    analysisDiagnostics(messageCollector.diagnostics.toAnalyzeResultSequence())
                    renderedResultType(resultRenderedType)
                }.asSuccess()
            }
        }
    }

    private fun analyzeWithCursor(
        messageCollector: ScriptDiagnosticsMessageCollector,
        snippet: SourceCode,
        configuration: ScriptCompilationConfiguration,
        cursor: SourceCode.Position? = null,
        getNewSnippet: (SourceCode, Int) -> SourceCode = { code, _ -> code }
    ): ResultWithDiagnostics<AnalyzeWithCursorResult> {

        konst initialConfiguration = configuration.refineBeforeParsing(snippet).konstueOr {
            return it
        }

        konst cursorAbs = cursor?.calcAbsolute(snippet) ?: -1
        konst newSnippet = if (cursorAbs == -1) snippet else getNewSnippet(snippet, cursorAbs)

        konst compilationState = state.getCompilationState(initialConfiguration) as ReplCompilationState<*>

        updateResolutionFilterWithHistory(configuration)

        konst (_, errorHolder, snippetKtFile) = prepareForAnalyze(
            newSnippet,
            messageCollector,
            compilationState,
            failOnSyntaxErrors = false
        ).konstueOr { return it }

        konst analyzerEngine = compilationState.analyzerEngine as IdeLikeReplCodeAnalyzer
        konst analysisResult =
            analyzerEngine.statelessAnalyzeWithImportedScripts(snippetKtFile, emptyList(), state.getNextLineNo() + 1)
        AnalyzerWithCompilerReport.reportDiagnostics(analysisResult.diagnostics, errorHolder, renderDiagnosticName = false)

        konst (_, bindingContext, resolutionFacade, moduleDescriptor, resultProperty) = when (analysisResult) {
            is IdeLikeReplCodeAnalyzer.ReplLineAnalysisResultWithStateless.Stateless -> {
                analysisResult
            }
            else -> return failure(
                newSnippet,
                messageCollector,
                "Unexpected result ${analysisResult::class.java}"
            )
        }

        return AnalyzeWithCursorResult(
            snippetKtFile, bindingContext, resolutionFacade, moduleDescriptor, cursorAbs, resultProperty
        ).asSuccess()
    }

    companion object {
        data class AnalyzeWithCursorResult(
            konst ktScript: KtFile,
            konst bindingContext: BindingContext,
            konst resolutionFacade: KotlinResolutionFacadeForRepl,
            konst moduleDescriptor: ModuleDescriptor,
            konst cursorAbs: Int,
            konst resultProperty: PropertyDescriptor?,
        )
    }
}
