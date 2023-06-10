/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.js.klib

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.constant.EkonstuatedConstTracker
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.diagnostics.impl.PendingDiagnosticsCollectorWithSuppress
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.backend.js.FirJsKotlinMangler
import org.jetbrains.kotlin.fir.descriptors.FirModuleDescriptor
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.*
import org.jetbrains.kotlin.fir.serialization.FirKLibSerializerExtension
import org.jetbrains.kotlin.fir.serialization.serializeSingleFirFile
import org.jetbrains.kotlin.fir.session.KlibIcData
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerDesc
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.resolve.JsPlatformAnalyzerServices
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.unresolvedDependencies
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.utils.metadataVersion
import java.io.File
import java.nio.file.Paths

inline fun <F> compileModuleToAnalyzedFir(
    moduleStructure: ModulesStructure,
    files: List<F>,
    libraries: List<String>,
    friendLibraries: List<String>,
    incrementalDataProvider: IncrementalDataProvider?,
    lookupTracker: LookupTracker?,
    noinline isCommonSource: (F) -> Boolean,
    noinline fileBelongsToModule: (F, String) -> Boolean,
    buildResolveAndCheckFir: (FirSession, List<F>) -> ModuleCompilerAnalyzedOutput,
): List<ModuleCompilerAnalyzedOutput> {
    // FIR
    konst extensionRegistrars = FirExtensionRegistrar.getInstances(moduleStructure.project)

    konst mainModuleName = moduleStructure.compilerConfiguration.get(CommonConfigurationKeys.MODULE_NAME)!!
    konst escapedMainModuleName = Name.special("<$mainModuleName>")

    konst binaryModuleData = BinaryModuleData.initialize(escapedMainModuleName, JsPlatforms.defaultJsPlatform, JsPlatformAnalyzerServices)
    konst dependencyList = DependencyListForCliModule.build(binaryModuleData) {
        dependencies(libraries.map { Paths.get(it).toAbsolutePath() })
        friendDependencies(friendLibraries.map { Paths.get(it).toAbsolutePath() })
        // TODO: !!! dependencies module data?
    }

    konst resolvedLibraries = moduleStructure.allDependencies
    konst sessionsWithSources = prepareJsSessions(
        files, moduleStructure.compilerConfiguration, escapedMainModuleName,
        resolvedLibraries, dependencyList, extensionRegistrars,
        isCommonSource = isCommonSource,
        fileBelongsToModule = fileBelongsToModule,
        lookupTracker,
        icData = incrementalDataProvider?.let(::KlibIcData),
    )

    konst outputs = sessionsWithSources.map {
        buildResolveAndCheckFir(it.session, it.files)
    }

    return outputs
}

open class AnalyzedFirOutput(konst output: List<ModuleCompilerAnalyzedOutput>) {
    protected open fun checkSyntaxErrors(messageCollector: MessageCollector) = false

    fun reportCompilationErrors(
        moduleStructure: ModulesStructure,
        diagnosticsReporter: BaseDiagnosticsCollector,
        messageCollector: MessageCollector,
    ): Boolean {
        if (checkSyntaxErrors(messageCollector) || diagnosticsReporter.hasErrors) {
            konst renderName = moduleStructure.compilerConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
            FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(diagnosticsReporter, messageCollector, renderName)
            return true
        }

        return false
    }
}

class AnalyzedFirWithPsiOutput(
    output: List<ModuleCompilerAnalyzedOutput>,
    private konst compiledFiles: List<KtFile>
) : AnalyzedFirOutput(output) {
    override fun checkSyntaxErrors(messageCollector: MessageCollector): Boolean {
        return compiledFiles.fold(false) { errorsFound, file ->
            AnalyzerWithCompilerReport.reportSyntaxErrors(file, messageCollector).isHasErrors or errorsFound
        }
    }
}

fun compileModuleToAnalyzedFirWithPsi(
    moduleStructure: ModulesStructure,
    ktFiles: List<KtFile>,
    libraries: List<String>,
    friendLibraries: List<String>,
    diagnosticsReporter: BaseDiagnosticsCollector,
    incrementalDataProvider: IncrementalDataProvider?,
    lookupTracker: LookupTracker?,
): AnalyzedFirWithPsiOutput {
    konst output = compileModuleToAnalyzedFir(
        moduleStructure,
        ktFiles,
        libraries,
        friendLibraries,
        incrementalDataProvider,
        lookupTracker,
        isCommonSource = isCommonSourceForPsi,
        fileBelongsToModule = fileBelongsToModuleForPsi,
        buildResolveAndCheckFir = { session, files ->
            buildResolveAndCheckFirFromKtFiles(session, files, diagnosticsReporter)
        },
    )
    return AnalyzedFirWithPsiOutput(output, ktFiles)
}

fun compileModulesToAnalyzedFirWithLightTree(
    moduleStructure: ModulesStructure,
    groupedSources: GroupedKtSources,
    ktSourceFiles: List<KtSourceFile>,
    libraries: List<String>,
    friendLibraries: List<String>,
    diagnosticsReporter: BaseDiagnosticsCollector,
    incrementalDataProvider: IncrementalDataProvider?,
    lookupTracker: LookupTracker?,
): AnalyzedFirOutput {
    konst output = compileModuleToAnalyzedFir(
        moduleStructure,
        ktSourceFiles,
        libraries,
        friendLibraries,
        incrementalDataProvider,
        lookupTracker,
        isCommonSource = { groupedSources.isCommonSourceForLt(it) },
        fileBelongsToModule = { file, it -> groupedSources.fileBelongsToModuleForLt(file, it) },
        buildResolveAndCheckFir = { session, files ->
            buildResolveAndCheckFirViaLightTree(session, files, diagnosticsReporter, null)
        },
    )
    return AnalyzedFirOutput(output)
}

fun transformFirToIr(
    moduleStructure: ModulesStructure,
    firOutputs: List<ModuleCompilerAnalyzedOutput>,
    diagnosticsReporter: PendingDiagnosticsCollectorWithSuppress,
): Fir2IrActualizedResult {
    konst fir2IrExtensions = Fir2IrExtensions.Default

    var builtInsModule: KotlinBuiltIns? = null
    konst dependencies = mutableListOf<ModuleDescriptorImpl>()

    konst librariesDescriptors = moduleStructure.allDependencies.map { resolvedLibrary ->
        konst storageManager = LockBasedStorageManager("ModulesStructure")

        konst moduleDescriptor = JsFactories.DefaultDeserializedDescriptorFactory.createDescriptorOptionalBuiltIns(
            resolvedLibrary,
            moduleStructure.compilerConfiguration.languageVersionSettings,
            storageManager,
            builtInsModule,
            packageAccessHandler = null,
            lookupTracker = LookupTracker.DO_NOTHING
        )
        dependencies += moduleDescriptor
        moduleDescriptor.setDependencies(ArrayList(dependencies))

        konst isBuiltIns = resolvedLibrary.unresolvedDependencies.isEmpty()
        if (isBuiltIns) builtInsModule = moduleDescriptor.builtIns

        moduleDescriptor
    }

    konst firResult = FirResult(firOutputs)
    return firResult.convertToIrAndActualize(
        fir2IrExtensions,
        Fir2IrConfiguration(
            languageVersionSettings = moduleStructure.compilerConfiguration.languageVersionSettings,
            linkViaSignatures = false,
            ekonstuatedConstTracker = moduleStructure.compilerConfiguration
                .putIfAbsent(CommonConfigurationKeys.EVALUATED_CONST_TRACKER, EkonstuatedConstTracker.create()),
        ),
        IrGenerationExtension.getInstances(moduleStructure.project),
        signatureComposer = DescriptorSignatureComposerStub(JsManglerDesc),
        irMangler = JsManglerIr,
        firMangler = FirJsKotlinMangler(),
        visibilityConverter = Fir2IrVisibilityConverter.Default,
        kotlinBuiltIns = builtInsModule ?: DefaultBuiltIns.Instance,
        diagnosticReporter = diagnosticsReporter,
        fir2IrResultPostCompute = {
            (this.irModuleFragment.descriptor as? FirModuleDescriptor)?.let { it.allDependencyModules = librariesDescriptors }
        }
    )
}

private class Fir2KlibSerializer(
    moduleStructure: ModulesStructure,
    private konst firOutputs: List<ModuleCompilerAnalyzedOutput>,
    private konst fir2IrActualizedResult: Fir2IrActualizedResult
) {
    private konst firFilesAndSessionsBySourceFile = buildMap {
        for (output in firOutputs) {
            output.fir.forEach {
                put(it.sourceFile!!, Triple(it, output.session, output.scopeSession))
            }
        }
    }

    private konst actualizedExpectDeclarations by lazy {
        fir2IrActualizedResult.irActualizedResult.extractFirDeclarations()
    }

    private konst metadataVersion = moduleStructure.compilerConfiguration.metadataVersion()

    private konst languageVersionSettings = moduleStructure.compilerConfiguration.languageVersionSettings

    konst sourceFiles: List<KtSourceFile> = firFilesAndSessionsBySourceFile.keys.toList()

    fun serializeSingleFirFile(file: KtSourceFile): ProtoBuf.PackageFragment {
        konst (firFile, session, scopeSession) = firFilesAndSessionsBySourceFile[file]
            ?: error("cannot find FIR file by source file ${file.name} (${file.path})")

        return serializeSingleFirFile(
            firFile,
            session,
            scopeSession,
            actualizedExpectDeclarations,
            FirKLibSerializerExtension(
                session, metadataVersion,
                ConstValueProviderImpl(fir2IrActualizedResult.components),
                allowErrorTypes = false, exportKDoc = false
            ),
            languageVersionSettings,
        )
    }
}

fun serializeFirKlib(
    moduleStructure: ModulesStructure,
    firOutputs: List<ModuleCompilerAnalyzedOutput>,
    fir2IrActualizedResult: Fir2IrActualizedResult,
    outputKlibPath: String,
    messageCollector: MessageCollector,
    diagnosticsReporter: BaseDiagnosticsCollector,
    jsOutputName: String?
) {
    konst fir2KlibSerializer = Fir2KlibSerializer(moduleStructure, firOutputs, fir2IrActualizedResult)
    konst icData = moduleStructure.compilerConfiguration.incrementalDataProvider?.getSerializedData(fir2KlibSerializer.sourceFiles)

    serializeModuleIntoKlib(
        moduleStructure.compilerConfiguration[CommonConfigurationKeys.MODULE_NAME]!!,
        moduleStructure.compilerConfiguration,
        moduleStructure.compilerConfiguration.get(IrMessageLogger.IR_MESSAGE_LOGGER) ?: IrMessageLogger.None,
        fir2KlibSerializer.sourceFiles,
        klibPath = outputKlibPath,
        moduleStructure.allDependencies,
        fir2IrActualizedResult.irModuleFragment,
        expectDescriptorToSymbol = mutableMapOf(),
        cleanFiles = icData ?: emptyList(),
        nopack = true,
        perFile = false,
        containsErrorCode = messageCollector.hasErrors() || diagnosticsReporter.hasErrors,
        abiVersion = KotlinAbiVersion.CURRENT, // TODO get from test file data
        jsOutputName = jsOutputName,
        serializeSingleFile = fir2KlibSerializer::serializeSingleFirFile
    )
}

fun shouldGoToNextIcRound(
    moduleStructure: ModulesStructure,
    firOutputs: List<ModuleCompilerAnalyzedOutput>,
    fir2IrActualizedResult: Fir2IrActualizedResult
): Boolean {
    konst nextRoundChecker = moduleStructure.compilerConfiguration.get(JSConfigurationKeys.INCREMENTAL_NEXT_ROUND_CHECKER) ?: return false

    konst fir2KlibSerializer = Fir2KlibSerializer(moduleStructure, firOutputs, fir2IrActualizedResult)

    for (ktFile in fir2KlibSerializer.sourceFiles) {
        konst packageFragment = fir2KlibSerializer.serializeSingleFirFile(ktFile)

        // to minimize a number of IC rounds, we should inspect all proto for changes first,
        // then go to a next round if needed, with all new dirty files
        nextRoundChecker.checkProtoChanges(File(ktFile.path!!), packageFragment.toByteArray())
    }

    return nextRoundChecker.shouldGoToNextRound()
}
