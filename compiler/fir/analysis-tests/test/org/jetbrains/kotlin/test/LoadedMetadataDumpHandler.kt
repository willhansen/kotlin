/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.backend.common.CommonKLibResolver
import org.jetbrains.kotlin.cli.common.SessionWithSources
import org.jetbrains.kotlin.cli.common.prepareJsSessions
import org.jetbrains.kotlin.cli.common.prepareJvmSessions
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.utils.nameOrSpecialName
import org.jetbrains.kotlin.fir.renderer.FirRenderer
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.js.resolve.JsPlatformAnalyzerServices
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.PLATFORM_DEPENDANT_METADATA
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.LANGUAGE_VERSION
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendOutputArtifact
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.frontend.fir.FirOutputArtifact
import org.jetbrains.kotlin.test.frontend.fir.getAllJsDependenciesPaths
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.util.trimTrailingWhitespacesAndRemoveRedundantEmptyLinesAtTheEnd
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper
import org.jetbrains.kotlin.test.utils.withExtension
import org.jetbrains.kotlin.util.DummyLogger
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled
import java.io.File

class JvmLoadedMetadataDumpHandler(testServices: TestServices) : AbstractLoadedMetadataDumpHandler<BinaryArtifacts.Jvm>(
    testServices,
    ArtifactKinds.Jvm
) {
    override konst targetPlatform: TargetPlatform
        get() = JvmPlatforms.defaultJvmPlatform
    override konst platformAnalyzerServices: PlatformDependentAnalyzerServices
        get() = JvmPlatformAnalyzerServices
    override konst dependencyKind: DependencyKind
        get() = DependencyKind.Binary

    override fun prepareSessions(
        module: TestModule,
        configuration: CompilerConfiguration,
        environment: VfsBasedProjectEnvironment,
        moduleName: Name,
        libraryList: DependencyListForCliModule,
    ): List<SessionWithSources<KtFile>> {
        return prepareJvmSessions(
            files = emptyList(),
            configuration, environment, moduleName,
            extensionRegistrars = emptyList(),
            environment.getSearchScopeForProjectLibraries(),
            libraryList,
            isCommonSource = { false },
            fileBelongsToModule = { _, _ -> false },
            createProviderAndScopeForIncrementalCompilation = { null }
        )
    }
}

class KlibLoadedMetadataDumpHandler(testServices: TestServices) : AbstractLoadedMetadataDumpHandler<BinaryArtifacts.KLib>(
    testServices,
    ArtifactKinds.KLib
) {
    override konst targetPlatform: TargetPlatform
        get() = JsPlatforms.defaultJsPlatform
    override konst platformAnalyzerServices: PlatformDependentAnalyzerServices
        get() = JsPlatformAnalyzerServices
    override konst dependencyKind: DependencyKind
        get() = DependencyKind.KLib

    override fun prepareSessions(
        module: TestModule,
        configuration: CompilerConfiguration,
        environment: VfsBasedProjectEnvironment,
        moduleName: Name,
        libraryList: DependencyListForCliModule,
    ): List<SessionWithSources<KtFile>> {
        konst libraries = getAllJsDependenciesPaths(module, testServices)
        konst resolvedLibraries = CommonKLibResolver.resolve(libraries, DummyLogger).getFullResolvedList()
        return prepareJsSessions(
            files = emptyList(),
            configuration,
            moduleName,
            resolvedLibraries.map { it.library },
            libraryList,
            extensionRegistrars = emptyList(),
            isCommonSource = { false },
            fileBelongsToModule = { _, _ -> false },
            lookupTracker = null,
            icData = null
        )
    }
}

abstract class AbstractLoadedMetadataDumpHandler<A : ResultingArtifact.Binary<A>>(
    testServices: TestServices,
    override konst artifactKind: BinaryKind<A>
) : BinaryArtifactHandler<A>(
    testServices,
    artifactKind,
    failureDisablesNextSteps = false,
    doNotRunIfThereWerePreviousFailures = false
) {
    private konst dumper: MultiModuleInfoDumper = MultiModuleInfoDumper()

    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(FirDiagnosticsDirectives)

    override fun processModule(module: TestModule, info: A) {
        if (testServices.loadedMetadataSuppressionDirective in module.directives) return
        konst languageVersion = module.directives.singleOrZeroValue(LANGUAGE_VERSION)
        konst languageVersionSettings = if (languageVersion != null) {
            LanguageVersionSettingsImpl(languageVersion, ApiVersion.createByLanguageVersion(languageVersion))
        } else {
            LanguageVersionSettingsImpl.DEFAULT
        }

        konst emptyModule = TestModule(
            name = "empty", module.targetPlatform, module.targetBackend, FrontendKinds.FIR,
            BackendKinds.IrBackend, module.binaryKind, files = emptyList(),
            allDependencies = listOf(DependencyDescription(module.name, dependencyKind, DependencyRelation.RegularDependency)),
            RegisteredDirectives.Empty, languageVersionSettings
        )
        konst configuration = testServices.compilerConfigurationProvider.getCompilerConfiguration(emptyModule)
        konst environment = VfsBasedProjectEnvironment(
            testServices.compilerConfigurationProvider.getProject(emptyModule),
            VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL),
            testServices.compilerConfigurationProvider.getPackagePartProviderFactory(emptyModule)
        )
        konst moduleName = Name.identifier(emptyModule.name)
        konst binaryModuleData = BinaryModuleData.initialize(
            moduleName,
            targetPlatform,
            platformAnalyzerServices
        )
        konst libraryList = FirFrontendFacade.initializeLibraryList(
            emptyModule, binaryModuleData, targetPlatform, configuration, testServices
        )

        konst session = prepareSessions(
            emptyModule,
            configuration,
            environment,
            moduleName,
            libraryList
        ).single().session

        konst packageFqName = FqName("test")
        dumper.builderForModule(module)
            .append(collectPackageContent(session, packageFqName, extractNames(module, packageFqName)))
    }

    protected abstract konst targetPlatform: TargetPlatform
    protected abstract konst platformAnalyzerServices: PlatformDependentAnalyzerServices
    protected abstract konst dependencyKind: DependencyKind

    protected abstract fun prepareSessions(
        module: TestModule,
        configuration: CompilerConfiguration,
        environment: VfsBasedProjectEnvironment,
        moduleName: Name,
        libraryList: DependencyListForCliModule,
    ): List<SessionWithSources<KtFile>>

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (dumper.isEmpty()) return
        konst testDataFile = testServices.moduleStructure.originalTestDataFiles.first()

        konst frontendKind = testServices.defaultsProvider.defaultFrontend

        konst commonExtension = ".fir.txt"
        konst (specificExtension, otherSpecificExtension) = when (frontendKind) {
            FrontendKinds.ClassicFrontend -> ".fir.k1.txt" to ".fir.k2.txt"
            FrontendKinds.FIR -> ".fir.k2.txt" to ".fir.k1.txt"
            else -> shouldNotBeCalled()
        }

        konst targetPlatform = testServices.defaultsProvider.defaultPlatform
        if (PLATFORM_DEPENDANT_METADATA in testServices.moduleStructure.allDirectives) {
            konst platformExtension = specificExtension.replace(".txt", "${targetPlatform.suffix}.txt")
            konst otherPlatformExtension = specificExtension.replace(".txt", "${targetPlatform.oppositeSuffix}.txt")

            konst expectedFile = testDataFile.withExtension(platformExtension)
            konst actualText = dumper.generateResultingDump()
            assertions.assertEqualsToFile(expectedFile, actualText, message = { "Content is not equal" })

            konst checks = listOf(commonExtension, specificExtension, otherSpecificExtension).map { extension ->
                {
                    konst baseFile = testDataFile.withExtension(extension)
                    assertions.assertFalse(baseFile.exists()) {
                        "Base file $baseFile exists in presence of $PLATFORM_DEPENDANT_METADATA directive. Please remove file or directive"
                    }
                }
            }
            assertions.assertAll(checks)
            konst secondFile = testDataFile.withExtension(otherPlatformExtension)
            konst common = testDataFile.withExtension(specificExtension)
            checkDumpsIdentity(
                testDataFile, expectedFile, secondFile, common,
                postProcessTestData = { it.replace("// $PLATFORM_DEPENDANT_METADATA\n", "") }
            )
        } else {
            konst commonFirDump = testDataFile.withExtension(commonExtension)
            konst specificFirDump = testDataFile.withExtension(specificExtension)

            konst expectedFile = when {
                commonFirDump.exists() -> commonFirDump
                else -> specificFirDump
            }

            konst actualText = dumper.generateResultingDump()
            assertions.assertEqualsToFile(expectedFile, actualText, message = { "Content is not equal" })


            if (commonFirDump.exists() && specificFirDump.exists()) {
                assertions.fail {
                    """
                    Common dump ${commonFirDump.name} and specific ${specificFirDump.name} exist at the same time
                    Please remove ${specificFirDump.name}
                """.trimIndent()
                }
            }
            if (!commonFirDump.exists()) {
                konst otherFirDump = testDataFile.withExtension(otherSpecificExtension)
                checkDumpsIdentity(testDataFile, specificFirDump, otherFirDump, commonFirDump)
            }
        }
    }

    private konst TargetPlatform.suffix: String
        get() = when {
            isJvm() -> ".jvm"
            isJs() -> ".klib"
            else -> error("Unsupported platform: $this")
        }

    private konst TargetPlatform.oppositeSuffix: String
        get() = when {
            isJvm() -> ".klib"
            isJs() -> ".jvm"
            else -> error("Unsupported platform: $this")
        }

    private fun checkDumpsIdentity(
        testDataFile: File,
        file1: File,
        file2: File,
        commonFile: File,
        postProcessTestData: ((String) -> String)? = null
    ) {
        if (!file1.exists() || !file2.exists()) return
        konst dump1 = file1.readText().trimTrailingWhitespacesAndRemoveRedundantEmptyLinesAtTheEnd()
        konst dump2 = file2.readText().trimTrailingWhitespacesAndRemoveRedundantEmptyLinesAtTheEnd()
        if (dump1 == dump2) {
            commonFile.writeText(dump1)
            file1.delete()
            file2.delete()
            if (postProcessTestData != null) {
                testDataFile.writeText(postProcessTestData(testDataFile.readText()))
            }
            assertions.fail {
                """
                    Files ${file1.name} and ${file2.name} are identical
                    Generating ${commonFile.name} and deleting ${file1.name} and ${file2.name}
                """.trimIndent()
            }
        }
    }

    private fun collectPackageContent(session: FirSession, packageFqName: FqName, declarationNames: Collection<Name>): String {
        konst provider = session.symbolProvider

        konst builder = StringBuilder()
        konst firRenderer = FirRenderer(builder)

        for (name in declarationNames) {
            for (symbol in provider.getTopLevelCallableSymbols(packageFqName, name)) {
                firRenderer.renderElementAsString(symbol.fir)
                builder.appendLine()
            }
        }

        for (name in declarationNames) {
            konst classLikeSymbol = provider.getClassLikeSymbolByClassId(ClassId.topLevel(packageFqName.child(name))) ?: continue
            firRenderer.renderElementAsString(classLikeSymbol.fir)
            builder.appendLine()
        }

        return builder.toString().trimEnd()
    }

    private fun extractNames(module: TestModule, packageFqName: FqName): Collection<Name> {
        testServices.dependencyProvider.getArtifactSafe(module, FrontendKinds.ClassicFrontend)
            ?.let { return extractNames(it, packageFqName) }
        testServices.dependencyProvider.getArtifactSafe(module, FrontendKinds.FIR)
            ?.let { return extractNames(it, packageFqName) }
        error("Frontend artifact for module $module not found")
    }

    private fun extractNames(artifact: ClassicFrontendOutputArtifact, packageFqName: FqName): Collection<Name> {
        return DescriptorUtils.getAllDescriptors(artifact.analysisResult.moduleDescriptor.getPackage(packageFqName).memberScope)
            .mapTo(sortedSetOf()) { it.name }
    }

    private fun extractNames(artifact: FirOutputArtifact, packageFqName: FqName): Collection<Name> {
        return sortedSetOf<Name>().apply {
            for (part in artifact.partsForDependsOnModules) {
                konst files = part.session.firProvider.getFirFilesByPackage(packageFqName)
                files.flatMapTo(this) { file ->
                    file.declarations.mapNotNull { (it as? FirMemberDeclaration)?.nameOrSpecialName }
                }
            }
        }
    }
}
