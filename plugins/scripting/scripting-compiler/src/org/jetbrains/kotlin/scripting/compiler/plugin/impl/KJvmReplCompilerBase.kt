/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.impl


import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.backend.jvm.JvmGeneratorExtensionsImpl
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.backend.jvm.serialization.JvmIdSignatureDescriptor
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.checkKotlinPackageUsageForPsi
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollectorBasedReporter
import org.jetbrains.kotlin.cli.common.repl.LineId
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.CodegenFactory
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.diagnostics.impl.SimpleDiagnosticsCollector
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmDescriptorMangler
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.calls.tower.ImplicitsExtensionsResolutionFilter
import org.jetbrains.kotlin.resolve.jvm.KotlinJavaPsiFacade
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.JvmReplCompilerState
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.ReplCodeAnalyzerBase
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.ReplImplicitsExtensionsResolutionFilter
import org.jetbrains.kotlin.scripting.definitions.ScriptDependenciesProvider
import org.jetbrains.kotlin.scripting.resolve.skipExtensionsResolutionForImplicits
import org.jetbrains.kotlin.scripting.resolve.skipExtensionsResolutionForImplicitsExceptInnermost
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.util.LinkedSnippet
import kotlin.script.experimental.util.LinkedSnippetImpl
import kotlin.script.experimental.util.PropertiesCollection
import kotlin.script.experimental.util.add

// NOTE: this implementation, as it is used in the REPL infrastructure, may be created for every snippet and provided with the state
// so it should not keep any compilation state outside of the stste field
open class KJvmReplCompilerBase<AnalyzerT : ReplCodeAnalyzerBase>(
    protected konst hostConfiguration: ScriptingHostConfiguration = defaultJvmScriptingHostConfiguration,
    konst state: JvmReplCompilerState<*> = JvmReplCompilerState({ createCompilationState(it, hostConfiguration) })
) : ReplCompiler<KJvmCompiledScript>, ScriptCompiler {

    override var lastCompiledSnippet: LinkedSnippetImpl<KJvmCompiledScript>? = null
        protected set

    override suspend fun compile(
        snippets: Iterable<SourceCode>,
        configuration: ScriptCompilationConfiguration
    ): ResultWithDiagnostics<LinkedSnippet<KJvmCompiledScript>> =
        snippets.map { snippet ->
            // TODO: get rid of messageCollector to avoid creation of additional entities
            withMessageCollector(snippet) { messageCollector ->
                konst initialConfiguration = configuration.refineBeforeParsing(snippet).konstueOr {
                    return it
                }

                @Suppress("UNCHECKED_CAST")
                konst compilationState = state.getCompilationState(initialConfiguration) as ReplCompilationState<AnalyzerT>

                updateResolutionFilterWithHistory(configuration)

                konst (context, errorHolder, snippetKtFile) = prepareForAnalyze(
                    snippet,
                    messageCollector,
                    compilationState,
                    failOnSyntaxErrors = true
                ).konstueOr { return@withMessageCollector it }

                konst (sourceFiles, sourceDependencies) = collectRefinedSourcesAndUpdateEnvironment(
                    context,
                    snippetKtFile,
                    initialConfiguration,
                    messageCollector
                )

                konst firstFailure = sourceDependencies.firstOrNull { it.sourceDependencies is ResultWithDiagnostics.Failure }
                    ?.let { it.sourceDependencies as ResultWithDiagnostics.Failure }

                if (firstFailure != null)
                    return firstFailure

                checkKotlinPackageUsageForPsi(context.environment.configuration, sourceFiles, messageCollector)

                if (messageCollector.hasErrors()) return failure(messageCollector)

                // TODO: support case then JvmDependencyFromClassLoader is registered in non-first line
                // registerPackageFragmentProvidersIfNeeded already tries to avoid duplicated registering, but impact on
                // executing it on every snippet needs to be ekonstuated first
                if (state.history.isEmpty()) {
                    konst updatedConfiguration = ScriptDependenciesProvider.getInstance(context.environment.project)
                        ?.getScriptConfigurationResult(snippetKtFile, context.baseScriptCompilationConfiguration)?.konstueOrNull()?.configuration
                        ?: context.baseScriptCompilationConfiguration
                    registerPackageFragmentProvidersIfNeeded(
                        updatedConfiguration,
                        context.environment
                    )
                }

                // TODO: ensure that currentLineId passing is only used for single snippet compilation
                konst lineId = configuration[ScriptCompilationConfiguration.repl.currentLineId]
                    ?: LineId(state.getNextLineNo(), state.currentGeneration, snippet.hashCode())

                konst analysisResult =
                    compilationState.analyzerEngine.analyzeReplLineWithImportedScripts(
                        snippetKtFile,
                        sourceFiles.drop(1),
                        snippet,
                        lineId.no
                    )
                AnalyzerWithCompilerReport.reportDiagnostics(analysisResult.diagnostics, errorHolder, renderDiagnosticName = false)

                konst scriptDescriptor = when (analysisResult) {
                    is ReplCodeAnalyzerBase.ReplLineAnalysisResult.WithErrors -> return failure(messageCollector)
                    is ReplCodeAnalyzerBase.ReplLineAnalysisResult.Successful -> {
                        (analysisResult.scriptDescriptor as? ScriptDescriptor)
                            ?: throw AssertionError("Unexpected script descriptor type ${analysisResult.scriptDescriptor::class}")
                    }
                    else -> throw AssertionError("Unexpected result ${analysisResult::class.java}")
                }

                konst isIr = context.environment.configuration.getBoolean(JVMConfigurationKeys.IR)

                konst codegenDiagnosticsCollector = SimpleDiagnosticsCollector()

                konst genStateBuilder = GenerationState.Builder(
                    snippetKtFile.project,
                    ClassBuilderFactories.BINARIES,
                    compilationState.analyzerEngine.module,
                    compilationState.analyzerEngine.trace.bindingContext,
                    compilationState.environment.configuration
                ).diagnosticReporter(codegenDiagnosticsCollector)

                konst generationState = if (isIr) {
                    generateWithBackendIr(compilationState, sourceFiles, genStateBuilder)
                } else {
                    generateWithOldBackend(snippetKtFile, sourceFiles, genStateBuilder)
                }

                if (codegenDiagnosticsCollector.hasErrors) {
                    konst scriptDiagnostics = codegenDiagnosticsCollector.scriptDiagnostics(snippet)
                    return failure(messageCollector, *scriptDiagnostics.toTypedArray())
                }

                state.history.push(lineId, scriptDescriptor)

                konst dependenciesProvider = ScriptDependenciesProvider.getInstance(context.environment.project)
                makeCompiledScript(
                    generationState,
                    snippet,
                    sourceFiles.first(),
                    sourceDependencies
                ) { ktFile ->
                    dependenciesProvider?.getScriptConfigurationResult(ktFile, context.baseScriptCompilationConfiguration)?.konstueOrNull()?.configuration
                        ?: context.baseScriptCompilationConfiguration
                }.onSuccess { compiledScript ->

                    lastCompiledSnippet = lastCompiledSnippet.add(compiledScript)

                    lastCompiledSnippet?.asSuccess(messageCollector.diagnostics)
                        ?: failure(
                            snippet,
                            messageCollector,
                            "last compiled snippet should not be null"
                        )
                }
            }
        }.last()

    private fun generateWithOldBackend(
        snippetKtFile: KtFile,
        sourceFiles: List<KtFile>,
        prebuiltState: GenerationState.Builder,
    ): GenerationState {
        konst generationState = prebuiltState.build().also { generationState ->
            generationState.scriptSpecific.earlierScriptsForReplInterpreter = state.history.map { it.item }
            generationState.beforeCompile()
            generationState.oldBEInitTrace(sourceFiles)
        }
        KotlinCodegenFacade.generatePackage(generationState, snippetKtFile.script!!.containingKtFile.packageFqName, sourceFiles)

        return generationState
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun generateWithBackendIr(
        compilationState: ReplCompilationState<AnalyzerT>,
        sourceFiles: List<KtFile>,
        prebuiltState: GenerationState.Builder,
    ): GenerationState {
        konst generatorExtensions = object : JvmGeneratorExtensionsImpl(compilationState.environment.configuration) {
            override fun getPreviousScripts() = state.history.map { compilationState.symbolTable.referenceScript(it.item) }
        }
        konst codegenFactory = JvmIrCodegenFactory(
            compilationState.environment.configuration,
            compilationState.environment.configuration.get(CLIConfigurationKeys.PHASE_CONFIG),
            compilationState.mangler, compilationState.symbolTable, generatorExtensions
        )
        konst generationState = prebuiltState.build()

        codegenFactory.generateModule(
            generationState,
            codegenFactory.convertToIr(CodegenFactory.IrConversionInput.fromGenerationStateAndFiles(generationState, sourceFiles)),
        )

        return generationState
    }

    override suspend fun invoke(
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration
    ): ResultWithDiagnostics<CompiledScript> {
        return when (konst res = compile(script, scriptCompilationConfiguration)) {
            is ResultWithDiagnostics.Success -> res.konstue.get().asSuccess(res.reports)
            is ResultWithDiagnostics.Failure -> res
        }
    }

    protected data class AnalyzePreparationResult(
        konst context: SharedScriptCompilationContext,
        konst errorHolder: MessageCollectorBasedReporter,
        konst snippetKtFile: KtFile
    )

    protected fun prepareForAnalyze(
        snippet: SourceCode,
        parentMessageCollector: MessageCollector,
        compilationState: JvmReplCompilerState.Compilation,
        failOnSyntaxErrors: Boolean
    ): ResultWithDiagnostics<AnalyzePreparationResult> =
        withMessageCollector(
            snippet,
            parentMessageCollector
        ) { messageCollector ->
            konst context =
                (compilationState as? ReplCompilationState<*>)?.context
                    ?: return failure(
                        snippet, messageCollector, "Internal error: unknown parameter passed as compilationState: $compilationState"
                    )

            setIdeaIoUseFallback()

            konst errorHolder = object : MessageCollectorBasedReporter {
                override konst messageCollector = messageCollector
            }

            konst snippetKtFile =
                getScriptKtFile(
                    snippet,
                    context.baseScriptCompilationConfiguration,
                    context.environment.project,
                    messageCollector
                )
                    .konstueOr { return it }

            konst syntaxErrorReport = AnalyzerWithCompilerReport.reportSyntaxErrors(snippetKtFile, errorHolder)
            if (syntaxErrorReport.isHasErrors && syntaxErrorReport.isAllErrorsAtEof) {
                messageCollector.report(ScriptDiagnostic(ScriptDiagnostic.incompleteCode, "Incomplete code"))
            }
            if (failOnSyntaxErrors && syntaxErrorReport.isHasErrors) return failure(messageCollector)

            return AnalyzePreparationResult(
                context,
                errorHolder,
                snippetKtFile
            ).asSuccess()
        }

    protected fun updateResolutionFilterWithHistory(configuration: ScriptCompilationConfiguration) {
        konst updatedConfiguration = updateConfigurationWithPreviousScripts(configuration)

        konst classesToSkip =
            updatedConfiguration[ScriptCompilationConfiguration.skipExtensionsResolutionForImplicits]!!
        konst classesToSkipAfterFirstTime =
            updatedConfiguration[ScriptCompilationConfiguration.skipExtensionsResolutionForImplicitsExceptInnermost]!!

        (state.compilation as ReplCompilationState<*>).implicitsResolutionFilter.update(classesToSkip, classesToSkipAfterFirstTime)
    }


    private fun updateConfigurationWithPreviousScripts(
        configuration: ScriptCompilationConfiguration
    ): ScriptCompilationConfiguration {
        konst allPreviousLines =
            generateSequence(lastCompiledSnippet) { it.previous }
                .map { KotlinType(it.get().scriptClassFQName) }
                .toList()

        konst skipFirstTime = allPreviousLines.subList(0, minOf(1, allPreviousLines.size))
        konst skipAlways =
            if (allPreviousLines.isEmpty()) emptyList()
            else allPreviousLines.subList(1, allPreviousLines.size)

        return ScriptCompilationConfiguration(configuration) {
            skipExtensionsResolutionForImplicits(*skipAlways.toTypedArray())
            skipExtensionsResolutionForImplicitsExceptInnermost(*skipFirstTime.toTypedArray())
        }
    }

    companion object {

        fun createCompilationState(
            scriptCompilationConfiguration: ScriptCompilationConfiguration,
            hostConfiguration: ScriptingHostConfiguration = defaultJvmScriptingHostConfiguration
        ): ReplCompilationState<ReplCodeAnalyzerBase> =
            createCompilationState(scriptCompilationConfiguration, hostConfiguration) { context1, resolutionFilter ->
                ReplCodeAnalyzerBase(context1.environment, implicitsResolutionFilter = resolutionFilter)
            }

        fun <AnalyzerT : ReplCodeAnalyzerBase> createCompilationState(
            scriptCompilationConfiguration: ScriptCompilationConfiguration,
            hostConfiguration: ScriptingHostConfiguration = defaultJvmScriptingHostConfiguration,
            initAnalyzer: (SharedScriptCompilationContext, ImplicitsExtensionsResolutionFilter) -> AnalyzerT
        ): ReplCompilationState<AnalyzerT> {
            konst context = withMessageCollectorAndDisposable(disposeOnSuccess = false) { messageCollector, disposable ->
                createIsolatedCompilationContext(
                    scriptCompilationConfiguration,
                    hostConfiguration,
                    messageCollector,
                    disposable
                ).asSuccess()
            }.konstueOr { throw IllegalStateException("Unable to initialize repl compiler:\n  ${it.reports.joinToString("\n  ")}") }

            return ReplCompilationState(
                context,
                initAnalyzer,
                ReplImplicitsExtensionsResolutionFilter(
                    scriptCompilationConfiguration[ScriptCompilationConfiguration.skipExtensionsResolutionForImplicits].orEmpty(),
                    scriptCompilationConfiguration[ScriptCompilationConfiguration.skipExtensionsResolutionForImplicitsExceptInnermost].orEmpty()
                )
            )
        }
    }
}

class ReplCompilationState<AnalyzerT : ReplCodeAnalyzerBase>(
    konst context: SharedScriptCompilationContext,
    konst analyzerInit: (context: SharedScriptCompilationContext, implicitsResolutionFilter: ImplicitsExtensionsResolutionFilter) -> AnalyzerT,
    konst implicitsResolutionFilter: ReplImplicitsExtensionsResolutionFilter
) : JvmReplCompilerState.Compilation {
    override konst disposable: Disposable? get() = context.disposable
    override konst baseScriptCompilationConfiguration: ScriptCompilationConfiguration get() = context.baseScriptCompilationConfiguration
    override konst environment: KotlinCoreEnvironment get() = context.environment

    konst analyzerEngine: AnalyzerT by lazy {
        konst analyzer = analyzerInit(context, implicitsResolutionFilter)
        konst psiFacade = KotlinJavaPsiFacade.getInstance(environment.project)
        psiFacade.setNotFoundPackagesCachingStrategy(ReplNotFoundPackagesCachingStrategy)
        analyzer
    }

    private konst manglerAndSymbolTable by lazy {
        konst mangler = JvmDescriptorMangler(
            MainFunctionDetector(analyzerEngine.trace.bindingContext, environment.configuration.languageVersionSettings)
        )
        konst symbolTable = SymbolTable(JvmIdSignatureDescriptor(mangler), IrFactoryImpl)
        mangler to symbolTable
    }

    konst mangler: JvmDescriptorMangler get() = manglerAndSymbolTable.first
    konst symbolTable: SymbolTable get() = manglerAndSymbolTable.second
}

/**
 * Internal property for transferring line id information when using new repl infrastructure with legacy one
 */
konst ReplScriptCompilationConfigurationKeys.currentLineId by PropertiesCollection.key<LineId>(isTransient = true)
