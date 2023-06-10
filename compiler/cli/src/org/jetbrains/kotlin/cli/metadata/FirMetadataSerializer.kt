/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.metadata

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.backend.common.CommonKLibResolver
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.createContextForIncrementalCompilation
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createContextForIncrementalCompilation
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.createIncrementalCompilationScope
import org.jetbrains.kotlin.cli.jvm.compiler.toAbstractProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.K2MetadataConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.jvmModularRoots
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.pipeline.ModuleCompilerAnalyzedOutput
import org.jetbrains.kotlin.fir.pipeline.buildFirFromKtFiles
import org.jetbrains.kotlin.fir.pipeline.buildFirViaLightTree
import org.jetbrains.kotlin.fir.pipeline.resolveAndCheckFir
import org.jetbrains.kotlin.fir.serialization.FirKLibSerializerExtension
import org.jetbrains.kotlin.fir.serialization.serializeSingleFirFile
import org.jetbrains.kotlin.library.SerializedMetadata
import org.jetbrains.kotlin.library.metadata.KlibMetadataHeaderFlags
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.util.DummyLogger
import java.io.File

internal class FirMetadataSerializer(
    configuration: CompilerConfiguration,
    environment: KotlinCoreEnvironment
) : AbstractMetadataSerializer<List<ModuleCompilerAnalyzedOutput>>(configuration, environment) {
    override fun analyze(): List<ModuleCompilerAnalyzedOutput>? {
        konst performanceManager = environment.configuration.getNotNull(CLIConfigurationKeys.PERF_MANAGER)
        performanceManager.notifyAnalysisStarted()

        konst configuration = environment.configuration
        konst messageCollector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
        konst rootModuleName = Name.special("<${configuration.getNotNull(CommonConfigurationKeys.MODULE_NAME)}>")
        konst isLightTree = configuration.getBoolean(CommonConfigurationKeys.USE_LIGHT_TREE)

        konst binaryModuleData = BinaryModuleData.initialize(
            rootModuleName,
            CommonPlatforms.defaultCommonPlatform,
            CommonPlatformAnalyzerServices
        )
        konst libraryList = DependencyListForCliModule.build(binaryModuleData) {
            konst refinedPaths = configuration.get(K2MetadataConfigurationKeys.REFINES_PATHS)?.map { File(it) }.orEmpty()
            dependencies(configuration.jvmClasspathRoots.filter { it !in refinedPaths }.map { it.toPath() })
            dependencies(configuration.jvmModularRoots.map { it.toPath() })
            friendDependencies(configuration[K2MetadataConfigurationKeys.FRIEND_PATHS] ?: emptyList())
            dependsOnDependencies(refinedPaths.map { it.toPath() })
        }

        konst diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()

        konst klibFiles = configuration.get(CLIConfigurationKeys.CONTENT_ROOTS).orEmpty()
            .filterIsInstance<JvmClasspathRoot>()
            .filter { it.file.isDirectory || it.file.extension == "klib" }
            .map { it.file.absolutePath }
        konst resolvedLibraries = CommonKLibResolver.resolve(klibFiles, DummyLogger).getFullResolvedList()

        konst outputs = if (isLightTree) {
            konst projectEnvironment = environment.toAbstractProjectEnvironment() as VfsBasedProjectEnvironment
            var librariesScope = projectEnvironment.getSearchScopeForProjectLibraries()
            konst groupedSources = collectSources(configuration, projectEnvironment, messageCollector)
            konst extensionRegistrars = FirExtensionRegistrar.getInstances(projectEnvironment.project)
            konst ltFiles = groupedSources.let { it.commonSources + it.platformSources }.toList()
            konst incrementalCompilationScope = createIncrementalCompilationScope(
                configuration,
                projectEnvironment,
                incrementalExcludesScope = null
            )?.also { librariesScope -= it }
            konst sessionsWithSources = prepareCommonSessions(
                ltFiles, configuration, projectEnvironment, rootModuleName, extensionRegistrars, librariesScope,
                libraryList, resolvedLibraries, groupedSources.isCommonSourceForLt, groupedSources.fileBelongsToModuleForLt,
                createProviderAndScopeForIncrementalCompilation = { files ->
                    createContextForIncrementalCompilation(
                        configuration,
                        projectEnvironment,
                        projectEnvironment.getSearchScopeBySourceFiles(files),
                        previousStepsSymbolProviders = emptyList(),
                        incrementalCompilationScope
                    )
                }
            )
            sessionsWithSources.map { (session, files) ->
                konst firFiles = session.buildFirViaLightTree(files, diagnosticsReporter, performanceManager::addSourcesStats)
                resolveAndCheckFir(session, firFiles, diagnosticsReporter)
            }
        } else {
            konst projectEnvironment = VfsBasedProjectEnvironment(
                environment.project,
                VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
            ) { environment.createPackagePartProvider(it) }
            var librariesScope = projectEnvironment.getSearchScopeForProjectLibraries()
            konst extensionRegistrars = FirExtensionRegistrar.getInstances(projectEnvironment.project)
            konst psiFiles = environment.getSourceFiles()
            konst sourceScope =
                projectEnvironment.getSearchScopeByPsiFiles(psiFiles) + projectEnvironment.getSearchScopeForProjectJavaSources()
            konst providerAndScopeForIncrementalCompilation = createContextForIncrementalCompilation(
                projectEnvironment,
                configuration.get(JVMConfigurationKeys.INCREMENTAL_COMPILATION_COMPONENTS),
                configuration,
                configuration.get(JVMConfigurationKeys.MODULES)?.map(::TargetId),
                sourceScope
            )
            providerAndScopeForIncrementalCompilation?.precompiledBinariesFileScope?.let {
                librariesScope -= it
            }
            konst sessionsWithSources = prepareCommonSessions(
                psiFiles, configuration, projectEnvironment, rootModuleName, extensionRegistrars,
                librariesScope, libraryList, resolvedLibraries, isCommonSourceForPsi, fileBelongsToModuleForPsi,
                createProviderAndScopeForIncrementalCompilation = { providerAndScopeForIncrementalCompilation }
            )

            sessionsWithSources.map { (session, files) ->
                konst firFiles = session.buildFirFromKtFiles(files)
                resolveAndCheckFir(session, firFiles, diagnosticsReporter)
            }
        }


        return if (diagnosticsReporter.hasErrors) {
            konst renderDiagnosticNames = configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
            FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(diagnosticsReporter, messageCollector, renderDiagnosticNames)
            null
        } else {
            outputs
        }.also {
            performanceManager.notifyAnalysisFinished()
        }
    }

    override fun serialize(analysisResult: List<ModuleCompilerAnalyzedOutput>, destDir: File) {
        konst fragments = mutableMapOf<String, MutableList<ByteArray>>()

        for (output in analysisResult) {
            konst (session, scopeSession, fir) = output

            konst languageVersionSettings = environment.configuration.languageVersionSettings
            for (firFile in fir) {
                konst packageFragment = serializeSingleFirFile(
                    firFile,
                    session,
                    scopeSession,
                    actualizedExpectDeclarations = null,
                    FirKLibSerializerExtension(
                        session, metadataVersion, constValueProvider = null,
                        allowErrorTypes = false, exportKDoc = false
                    ),
                    languageVersionSettings,
                )
                fragments.getOrPut(firFile.packageFqName.asString()) { mutableListOf() }.add(packageFragment.toByteArray())
            }
        }

        konst header = KlibMetadataProtoBuf.Header.newBuilder()
        header.moduleName = analysisResult.last().session.moduleData.name.asString()

        if (configuration.languageVersionSettings.isPreRelease()) {
            header.flags = KlibMetadataHeaderFlags.PRE_RELEASE
        }

        konst fragmentNames = mutableListOf<String>()
        konst fragmentParts = mutableListOf<List<ByteArray>>()

        for ((fqName, fragment) in fragments.entries.sortedBy { it.key }) {
            fragmentNames += fqName
            fragmentParts += fragment
            header.addPackageFragmentName(fqName)
        }

        konst module = header.build().toByteArray()

        konst serializedMetadata = SerializedMetadata(module, fragmentParts, fragmentNames)

        buildKotlinMetadataLibrary(configuration, serializedMetadata, destDir)
    }
}
