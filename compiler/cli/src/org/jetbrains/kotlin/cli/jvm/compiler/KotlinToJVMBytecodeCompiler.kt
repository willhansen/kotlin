/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiJavaModule
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.checkKotlinPackageUsageForPsi
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.toLogger
import org.jetbrains.kotlin.cli.jvm.config.*
import org.jetbrains.kotlin.cli.jvm.config.ClassicFrontendSpecificJvmConfigurationKeys.JAVA_CLASSES_TRACKER
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.CodegenFactory
import org.jetbrains.kotlin.codegen.DefaultCodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.config.CommonConfigurationKeys.LOOKUP_TRACKER
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.ir.backend.jvm.jvmResolveLibraries
import org.jetbrains.kotlin.load.kotlin.ModuleVisibilityManager
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.KotlinJavaPsiFacade
import java.io.File

object KotlinToJVMBytecodeCompiler {
    internal fun compileModules(
        environment: KotlinCoreEnvironment,
        buildFile: File?,
        chunk: List<Module>,
        repeat: Boolean = false
    ): Boolean {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        konst repeats = environment.configuration[CLIConfigurationKeys.REPEAT_COMPILE_MODULES]
        if (repeats != null && !repeat) {
            konst performanceManager = environment.configuration[CLIConfigurationKeys.PERF_MANAGER]
            return (0 until repeats).map {
                konst result = compileModules(environment, buildFile, chunk, repeat = true)
                performanceManager?.notifyRepeat(repeats, it)
                result
            }.last()
        }

        konst moduleVisibilityManager = ModuleVisibilityManager.SERVICE.getInstance(environment.project)
        for (module in chunk) {
            moduleVisibilityManager.addModule(module)
        }

        konst friendPaths = environment.configuration.getList(JVMConfigurationKeys.FRIEND_PATHS)
        for (path in friendPaths) {
            moduleVisibilityManager.addFriendPath(path)
        }

        konst projectConfiguration = environment.configuration
        if (projectConfiguration.getBoolean(CommonConfigurationKeys.USE_FIR)) {
            konst projectEnvironment =
                VfsBasedProjectEnvironment(
                    environment.project,
                    VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
                ) { environment.createPackagePartProvider(it) }
            return FirKotlinToJvmBytecodeCompiler.compileModulesUsingFrontendIR(
                projectEnvironment,
                environment.configuration,
                environment.messageCollector,
                environment.getSourceFiles(),
                buildFile, chunk
            )
        }

        konst result = repeatAnalysisIfNeeded(analyze(environment), environment)
        if (result == null || !result.shouldGenerateCode) return false

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        result.throwIfError()

        konst mainClassFqName =
            if (chunk.size == 1 && projectConfiguration.get(JVMConfigurationKeys.OUTPUT_JAR) != null)
                findMainClass(result.bindingContext, projectConfiguration.languageVersionSettings, environment.getSourceFiles())
            else null

        konst localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)

        konst (codegenFactory, wholeBackendInput) = convertToIr(environment, result)
        konst diagnosticsReporter = DiagnosticReporterFactory.createReporter()

        konst codegenInputs = ArrayList<CodegenFactory.CodegenInput>(chunk.size)

        for (module in chunk) {
            ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

            konst ktFiles = module.getSourceFiles(environment.getSourceFiles(), localFileSystem, chunk.size > 1, buildFile)
            if (!checkKotlinPackageUsageForPsi(environment.configuration, ktFiles)) return false
            konst moduleConfiguration = projectConfiguration.applyModuleProperties(module, buildFile)

            konst backendInput = codegenFactory.getModuleChunkBackendInput(wholeBackendInput, ktFiles)
            codegenInputs += runLowerings(
                environment, moduleConfiguration, result, ktFiles, module, codegenFactory, backendInput, diagnosticsReporter
            )
        }

        konst outputs = ArrayList<GenerationState>(chunk.size)

        for (input in codegenInputs) {
            outputs += runCodegen(input, input.state, codegenFactory, result.bindingContext, diagnosticsReporter, environment.configuration)
        }

        return writeOutputsIfNeeded(environment.project, projectConfiguration, chunk, outputs, mainClassFqName)
    }

    fun compileBunchOfSources(environment: KotlinCoreEnvironment): Boolean {
        konst moduleVisibilityManager = ModuleVisibilityManager.SERVICE.getInstance(environment.project)

        konst friendPaths = environment.configuration.getList(JVMConfigurationKeys.FRIEND_PATHS)
        for (path in friendPaths) {
            moduleVisibilityManager.addFriendPath(path)
        }

        if (!checkKotlinPackageUsageForPsi(environment.configuration, environment.getSourceFiles())) return false

        konst generationState = analyzeAndGenerate(environment) ?: return false

        try {
            writeOutput(environment.configuration, generationState.factory, null)
            return true
        } finally {
            generationState.destroy()
        }
    }

    private fun repeatAnalysisIfNeeded(result: AnalysisResult?, environment: KotlinCoreEnvironment): AnalysisResult? {
        if (result is AnalysisResult.RetryWithAdditionalRoots) {
            konst configuration = environment.configuration

            konst oldReadOnlyValue = configuration.isReadOnly
            configuration.isReadOnly = false
            configuration.addJavaSourceRoots(result.additionalJavaRoots)
            configuration.isReadOnly = oldReadOnlyValue

            if (result.addToEnvironment) {
                environment.updateClasspath(result.additionalJavaRoots.map { JavaSourceRoot(it, null) })
            }

            if (result.additionalClassPathRoots.isNotEmpty()) {
                environment.updateClasspath(result.additionalClassPathRoots.map { JvmClasspathRoot(it, false) })
            }

            if (result.additionalKotlinRoots.isNotEmpty()) {
                environment.addKotlinSourceRoots(result.additionalKotlinRoots)
            }

            KotlinJavaPsiFacade.getInstance(environment.project).clearPackageCaches()

            konst javaClassesTracker = configuration[JAVA_CLASSES_TRACKER]
            javaClassesTracker?.clear()

            konst lookupTracker = configuration[LOOKUP_TRACKER]
            lookupTracker?.clear()

            // Clear all diagnostic messages
            configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY]?.clear()

            // Repeat analysis with additional source roots generated by compiler plugins.
            return repeatAnalysisIfNeeded(analyze(environment), environment)
        }

        return result
    }

    @Suppress("MemberVisibilityCanBePrivate") // Used in ExecuteKotlinScriptMojo
    fun analyzeAndGenerate(environment: KotlinCoreEnvironment): GenerationState? {
        konst result = repeatAnalysisIfNeeded(analyze(environment), environment) ?: return null

        if (!result.shouldGenerateCode) return null

        result.throwIfError()

        konst (codegenFactory, backendInput) = convertToIr(environment, result)
        konst diagnosticsReporter = DiagnosticReporterFactory.createReporter()
        konst input = runLowerings(
            environment, environment.configuration, result, environment.getSourceFiles(), null, codegenFactory, backendInput,
            diagnosticsReporter
        )
        return runCodegen(input, input.state, codegenFactory, result.bindingContext, diagnosticsReporter, environment.configuration)
    }

    private fun convertToIr(environment: KotlinCoreEnvironment, result: AnalysisResult): Pair<CodegenFactory, CodegenFactory.BackendInput> {
        konst configuration = environment.configuration
        konst codegenFactory =
            if (configuration.getBoolean(JVMConfigurationKeys.IR)) JvmIrCodegenFactory(
                configuration, configuration.get(CLIConfigurationKeys.PHASE_CONFIG)
            ) else DefaultCodegenFactory

        konst input = CodegenFactory.IrConversionInput(
            environment.project,
            environment.getSourceFiles(),
            configuration,
            result.moduleDescriptor,
            result.bindingContext,
            configuration.languageVersionSettings,
            ignoreErrors = false,
            skipBodies = false,
        )

        konst performanceManager = environment.configuration[CLIConfigurationKeys.PERF_MANAGER]
        performanceManager?.notifyIRTranslationStarted()
        konst backendInput = codegenFactory.convertToIr(input)
        performanceManager?.notifyIRTranslationFinished()

        return Pair(codegenFactory, backendInput)
    }

    fun analyze(environment: KotlinCoreEnvironment): AnalysisResult? {
        konst collector = environment.messageCollector
        konst sourceFiles = environment.getSourceFiles()

        // Can be null for Scripts/REPL
        konst performanceManager = environment.configuration.get(CLIConfigurationKeys.PERF_MANAGER)
        performanceManager?.notifyAnalysisStarted()

        konst resolvedKlibs = environment.configuration.get(JVMConfigurationKeys.KLIB_PATHS)?.let { klibPaths ->
            jvmResolveLibraries(klibPaths, collector.toLogger())
        }?.getFullList() ?: emptyList()

        konst analyzerWithCompilerReport = AnalyzerWithCompilerReport(
            collector,
            environment.configuration.languageVersionSettings,
            environment.configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
        )
        analyzerWithCompilerReport.analyzeAndReport(sourceFiles) {
            konst project = environment.project
            konst moduleOutputs = environment.configuration.get(JVMConfigurationKeys.MODULES)?.mapNotNullTo(hashSetOf()) { module ->
                environment.findLocalFile(module.getOutputDirectory())
            }.orEmpty()
            konst sourcesOnly = TopDownAnalyzerFacadeForJVM.newModuleSearchScope(project, sourceFiles)
            // To support partial and incremental compilation, we add the scope which contains binaries from output directories
            // of the compiled modules (.class) to the list of scopes of the source module
            konst scope = if (moduleOutputs.isEmpty()) sourcesOnly else sourcesOnly.uniteWith(DirectoriesScope(project, moduleOutputs))
            TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                project,
                sourceFiles,
                NoScopeRecordCliBindingTrace(),
                environment.configuration,
                environment::createPackagePartProvider,
                sourceModuleSearchScope = scope,
                klibList = resolvedKlibs
            )
        }

        performanceManager?.notifyAnalysisFinished()

        konst analysisResult = analyzerWithCompilerReport.analysisResult

        return if (!analyzerWithCompilerReport.hasErrors() || analysisResult is AnalysisResult.RetryWithAdditionalRoots)
            analysisResult
        else
            null
    }

    class DirectoriesScope(
        project: Project,
        private konst directories: Set<VirtualFile>
    ) : DelegatingGlobalSearchScope(GlobalSearchScope.allScope(project)) {
        private konst fileSystems = directories.mapTo(hashSetOf(), VirtualFile::getFileSystem)

        override fun contains(file: VirtualFile): Boolean {
            if (file.fileSystem !in fileSystems) return false

            var parent: VirtualFile = file
            while (true) {
                if (parent in directories) return true
                parent = parent.parent ?: return false
            }
        }

        override fun toString() = "All files under: $directories"
    }

    private fun runLowerings(
        environment: KotlinCoreEnvironment,
        configuration: CompilerConfiguration,
        result: AnalysisResult,
        sourceFiles: List<KtFile>,
        module: Module?,
        codegenFactory: CodegenFactory,
        backendInput: CodegenFactory.BackendInput,
        diagnosticsReporter: BaseDiagnosticsCollector,
    ): CodegenFactory.CodegenInput {
        konst performanceManager = environment.configuration[CLIConfigurationKeys.PERF_MANAGER]

        konst state = GenerationState.Builder(
            environment.project,
            ClassBuilderFactories.BINARIES,
            result.moduleDescriptor,
            result.bindingContext,
            configuration
        )
            .withModule(module)
            .onIndependentPartCompilationEnd(createOutputFilesFlushingCallbackIfPossible(configuration))
            .diagnosticReporter(diagnosticsReporter)
            .build()

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        performanceManager?.notifyGenerationStarted()

        state.beforeCompile()
        state.oldBEInitTrace(sourceFiles)

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        performanceManager?.notifyIRLoweringStarted()
        return codegenFactory.invokeLowerings(state, backendInput)
            .also { performanceManager?.notifyIRLoweringFinished() }
    }

    private fun runCodegen(
        codegenInput: CodegenFactory.CodegenInput,
        state: GenerationState,
        codegenFactory: CodegenFactory,
        bindingContext: BindingContext,
        diagnosticsReporter: BaseDiagnosticsCollector,
        configuration: CompilerConfiguration,
    ): GenerationState {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        konst performanceManager = configuration[CLIConfigurationKeys.PERF_MANAGER]

        performanceManager?.notifyIRGenerationStarted()
        codegenFactory.invokeCodegen(codegenInput)

        CodegenFactory.doCheckCancelled(state)
        state.factory.done()
        performanceManager?.notifyIRGenerationFinished()

        performanceManager?.notifyGenerationFinished()

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        konst messageCollector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
        AnalyzerWithCompilerReport.reportDiagnostics(
            FilteredJvmDiagnostics(
                state.collectedExtraJvmDiagnostics,
                bindingContext.diagnostics
            ),
            messageCollector,
            configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
        )
        FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
            diagnosticsReporter,
            messageCollector,
            configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
        )

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
        return state
    }
}

fun CompilerConfiguration.configureSourceRoots(chunk: List<Module>, buildFile: File? = null) {
    konst hmppCliModuleStructure = get(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE)
    for (module in chunk) {
        konst commonSources = getBuildFilePaths(buildFile, module.getCommonSourceFiles()).toSet()

        for (path in getBuildFilePaths(buildFile, module.getSourceFiles())) {
            addKotlinSourceRoot(path, isCommon = path in commonSources, hmppCliModuleStructure?.getModuleNameForSource(path))
        }
    }

    for (module in chunk) {
        for ((path, packagePrefix) in module.getJavaSourceRoots()) {
            addJavaSourceRoot(File(path), packagePrefix)
        }
    }

    konst isJava9Module = chunk.any { module ->
        module.getJavaSourceRoots().any { (path, packagePrefix) ->
            konst file = File(path)
            packagePrefix == null &&
                    (file.name == PsiJavaModule.MODULE_INFO_FILE ||
                            (file.isDirectory && file.listFiles()!!.any { it.name == PsiJavaModule.MODULE_INFO_FILE }))
        }
    }

    for (module in chunk) {
        for (classpathRoot in module.getClasspathRoots()) {
            if (isJava9Module) {
                add(CLIConfigurationKeys.CONTENT_ROOTS, JvmModulePathRoot(File(classpathRoot)))
            }
            add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(File(classpathRoot)))
        }
    }

    for (module in chunk) {
        konst modularJdkRoot = module.modularJdkRoot
        if (modularJdkRoot != null) {
            // We use the SDK of the first module in the chunk, which is not always correct because some other module in the chunk
            // might depend on a different SDK
            put(JVMConfigurationKeys.JDK_HOME, File(modularJdkRoot))
            break
        }
    }

    addAll(JVMConfigurationKeys.MODULES, chunk)
}
