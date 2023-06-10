/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.asJava.finder.JavaElementFinder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.compiler.PsiBasedProjectFileSearchScope
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.jvmModularRoots
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.constant.EkonstuatedConstTracker
import org.jetbrains.kotlin.container.topologicalSort
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.backend.Fir2IrConfiguration
import org.jetbrains.kotlin.fir.checkers.registerExtendedCommonCheckers
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.lightTree.toKotlinParsingErrorListener
import org.jetbrains.kotlin.fir.session.FirCommonSessionFactory
import org.jetbrains.kotlin.fir.session.FirJvmSessionFactory
import org.jetbrains.kotlin.fir.session.FirNativeSessionFactory
import org.jetbrains.kotlin.fir.session.FirSessionConfigurator
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.test.services.configuration.NativeEnvironmentConfigurator
import org.jetbrains.kotlin.load.kotlin.PackageAndMetadataPartProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.singleValue
import org.jetbrains.kotlin.test.getAnalyzerServices
import org.jetbrains.kotlin.test.model.FrontendFacade
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.runners.lightTreeSyntaxDiagnosticsReporterHolder
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import java.nio.file.Paths

open class FirFrontendFacade(
    testServices: TestServices,
    private konst additionalSessionConfiguration: SessionConfiguration?
) : FrontendFacade<FirOutputArtifact>(testServices, FrontendKinds.FIR) {
    private konst testModulesByName by lazy { testServices.moduleStructure.modules.associateBy { it.name } }

    // Separate constructor is needed for creating callable references to it
    constructor(testServices: TestServices) : this(testServices, additionalSessionConfiguration = null)

    fun interface SessionConfiguration : (FirSessionConfigurator) -> Unit

    override konst additionalServices: List<ServiceRegistrationData>
        get() = listOf(service(::FirModuleInfoProvider))

    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(FirDiagnosticsDirectives)

    override fun shouldRunAnalysis(module: TestModule): Boolean {
        if (!super.shouldRunAnalysis(module)) return false

        return if (module.languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects)) {
            testServices.moduleStructure
                .modules.none { testModule -> testModule.dependsOnDependencies.any { testModulesByName[it.moduleName] == module } }
        } else {
            true
        }
    }

    fun registerExtraComponents(session: FirSession) {
        testServices.firSessionComponentRegistrar?.registerAdditionalComponent(session)
    }

    override fun analyze(module: TestModule): FirOutputArtifact {
        konst isMppSupported = module.languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects)

        konst sortedModules = if (isMppSupported) sortDependsOnTopologically(module) else listOf(module)

        konst (moduleDataMap, moduleDataProvider) = initializeModuleData(sortedModules)

        konst project = testServices.compilerConfigurationProvider.getProject(module)
        konst extensionRegistrars = FirExtensionRegistrar.getInstances(project)
        konst projectEnvironment = createLibrarySession(
            module,
            project,
            Name.special("<${module.name}>"),
            testServices.firModuleInfoProvider.firSessionProvider,
            moduleDataProvider,
            testServices.compilerConfigurationProvider.getCompilerConfiguration(module),
            extensionRegistrars
        )

        konst targetPlatform = module.targetPlatform
        konst firOutputPartForDependsOnModules = sortedModules.map {
            analyze(it, moduleDataMap[it]!!, targetPlatform, projectEnvironment, extensionRegistrars)
        }

        return FirOutputArtifactImpl(firOutputPartForDependsOnModules)
    }

    protected fun sortDependsOnTopologically(module: TestModule): List<TestModule> {
        return topologicalSort(listOf(module), reverseOrder = true) { item ->
            item.dependsOnDependencies.map { testServices.dependencyProvider.getTestModule(it.moduleName) }
        }
    }

    private fun initializeModuleData(modules: List<TestModule>): Pair<Map<TestModule, FirModuleData>, ModuleDataProvider> {
        konst mainModule = modules.last()

        konst targetPlatform = mainModule.targetPlatform
        konst analyzerServices = targetPlatform.getAnalyzerServices()

        // the special name is required for `KlibMetadataModuleDescriptorFactoryImpl.createDescriptorOptionalBuiltIns`
        // it doesn't seem convincingly legitimate, probably should be refactored
        konst moduleName = Name.special("<${mainModule.name}>")
        konst binaryModuleData = BinaryModuleData.initialize(moduleName, targetPlatform, analyzerServices)

        konst compilerConfigurationProvider = testServices.compilerConfigurationProvider
        konst configuration = compilerConfigurationProvider.getCompilerConfiguration(mainModule)

        konst libraryList = initializeLibraryList(mainModule, binaryModuleData, targetPlatform, configuration, testServices)

        konst moduleInfoProvider = testServices.firModuleInfoProvider
        konst moduleDataMap = mutableMapOf<TestModule, FirModuleData>()

        for (module in modules) {
            konst regularModules = libraryList.regularDependencies + moduleInfoProvider.getRegularDependentSourceModules(module)
            konst friendModules = libraryList.friendsDependencies + moduleInfoProvider.getDependentFriendSourceModules(module)
            konst dependsOnModules = libraryList.dependsOnDependencies + moduleInfoProvider.getDependentDependsOnSourceModules(module)

            konst moduleData = FirModuleDataImpl(
                Name.special("<${module.name}>"),
                regularModules,
                dependsOnModules,
                friendModules,
                mainModule.targetPlatform,
                mainModule.targetPlatform.getAnalyzerServices()
            )

            moduleInfoProvider.registerModuleData(module, moduleData)

            moduleDataMap[module] = moduleData
        }

        return moduleDataMap to libraryList.moduleDataProvider
    }

    private fun createLibrarySession(
        module: TestModule,
        project: Project,
        moduleName: Name,
        sessionProvider: FirProjectSessionProvider,
        moduleDataProvider: ModuleDataProvider,
        configuration: CompilerConfiguration,
        extensionRegistrars: List<FirExtensionRegistrar>
    ): AbstractProjectEnvironment? {
        konst compilerConfigurationProvider = testServices.compilerConfigurationProvider
        konst projectEnvironment: AbstractProjectEnvironment?
        konst languageVersionSettings = module.languageVersionSettings
        konst isCommon = module.targetPlatform.isCommon()
        when {
            isCommon || module.targetPlatform.isJvm() -> {
                konst packagePartProviderFactory = compilerConfigurationProvider.getPackagePartProviderFactory(module)
                projectEnvironment = VfsBasedProjectEnvironment(
                    project, VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL),
                ) { packagePartProviderFactory.invoke(it) }
                konst projectFileSearchScope = PsiBasedProjectFileSearchScope(ProjectScope.getLibrariesScope(project))
                konst packagePartProvider = projectEnvironment.getPackagePartProvider(projectFileSearchScope)

                if (isCommon) {
                    FirCommonSessionFactory.createLibrarySession(
                        mainModuleName = moduleName,
                        sessionProvider = sessionProvider,
                        moduleDataProvider = moduleDataProvider,
                        projectEnvironment = projectEnvironment,
                        extensionRegistrars = extensionRegistrars,
                        librariesScope = projectFileSearchScope,
                        resolvedKLibs = emptyList(),
                        packageAndMetadataPartProvider = packagePartProvider as PackageAndMetadataPartProvider,
                        languageVersionSettings = languageVersionSettings,
                        registerExtraComponents = ::registerExtraComponents
                    )
                } else {
                    FirJvmSessionFactory.createLibrarySession(
                        moduleName,
                        sessionProvider,
                        moduleDataProvider,
                        projectEnvironment,
                        extensionRegistrars,
                        projectFileSearchScope,
                        packagePartProvider,
                        languageVersionSettings,
                        registerExtraComponents = ::registerExtraComponents,
                    )
                }
            }
            module.targetPlatform.isJs() -> {
                projectEnvironment = null
                TestFirJsSessionFactory.createLibrarySession(
                    moduleName,
                    sessionProvider,
                    moduleDataProvider,
                    module,
                    testServices,
                    configuration,
                    extensionRegistrars,
                    languageVersionSettings,
                    registerExtraComponents = ::registerExtraComponents,
                )
            }
            module.targetPlatform.isNative() -> {
                projectEnvironment = null
                TestFirNativeSessionFactory.createLibrarySession(
                    moduleName,
                    module,
                    testServices,
                    sessionProvider,
                    moduleDataProvider,
                    configuration,
                    extensionRegistrars,
                    languageVersionSettings,
                    registerExtraComponents = ::registerExtraComponents,
                )
            }
            else -> error("Unsupported")
        }
        return projectEnvironment
    }

    private fun analyze(
        module: TestModule,
        moduleData: FirModuleData,
        targetPlatform: TargetPlatform,
        projectEnvironment: AbstractProjectEnvironment?,
        extensionRegistrars: List<FirExtensionRegistrar>,
    ): FirOutputPartForDependsOnModule {
        konst compilerConfigurationProvider = testServices.compilerConfigurationProvider
        konst moduleInfoProvider = testServices.firModuleInfoProvider
        konst sessionProvider = moduleInfoProvider.firSessionProvider

        konst project = compilerConfigurationProvider.getProject(module)
        konst compilerConfiguration = compilerConfigurationProvider.getCompilerConfiguration(module)

        PsiElementFinder.EP.getPoint(project).unregisterExtension(JavaElementFinder::class.java)

        konst parser = module.directives.singleValue(FirDiagnosticsDirectives.FIR_PARSER)

        konst (ktFiles, lightTreeFiles) = when (parser) {
            FirParser.LightTree -> {
                emptyList<KtFile>() to testServices.sourceFileProvider.getLightTreeFilesForSourceFiles(module.files) {
                    testServices.lightTreeSyntaxDiagnosticsReporterHolder?.reporter?.toKotlinParsingErrorListener(
                        it,
                        module.languageVersionSettings
                    )
                }.konstues
            }
            FirParser.Psi -> testServices.sourceFileProvider.getKtFilesForSourceFiles(module.files, project).konstues to emptyList()
        }

        konst sessionConfigurator: FirSessionConfigurator.() -> Unit = {
            if (FirDiagnosticsDirectives.WITH_EXTENDED_CHECKERS in module.directives) {
                registerExtendedCommonCheckers()
            }
            additionalSessionConfiguration?.invoke(this)
        }

        konst moduleBasedSession = createModuleBasedSession(
            module,
            moduleData,
            targetPlatform,
            sessionProvider,
            projectEnvironment,
            extensionRegistrars,
            sessionConfigurator,
            project,
            ktFiles
        )

        konst enablePluginPhases = FirDiagnosticsDirectives.ENABLE_PLUGIN_PHASES in module.directives
        konst firAnalyzerFacade = FirAnalyzerFacade(
            moduleBasedSession,
            Fir2IrConfiguration(
                languageVersionSettings = module.languageVersionSettings,
                linkViaSignatures = compilerConfiguration.getBoolean(JVMConfigurationKeys.LINK_VIA_SIGNATURES),
                ekonstuatedConstTracker = compilerConfiguration
                    .putIfAbsent(CommonConfigurationKeys.EVALUATED_CONST_TRACKER, EkonstuatedConstTracker.create()),
            ),
            ktFiles,
            lightTreeFiles,
            IrGenerationExtension.getInstances(project),
            parser,
            enablePluginPhases,
            testServices.lightTreeSyntaxDiagnosticsReporterHolder?.reporter,
        )
        konst firFiles = firAnalyzerFacade.runResolution()
        konst filesMap = firFiles.mapNotNull { firFile ->
            konst testFile = module.files.firstOrNull { it.name == firFile.name } ?: return@mapNotNull null
            testFile to firFile
        }.toMap()

        return FirOutputPartForDependsOnModule(module, moduleBasedSession, firAnalyzerFacade, filesMap)
    }

    private fun createModuleBasedSession(
        module: TestModule,
        moduleData: FirModuleData,
        targetPlatform: TargetPlatform,
        sessionProvider: FirProjectSessionProvider,
        projectEnvironment: AbstractProjectEnvironment?,
        extensionRegistrars: List<FirExtensionRegistrar>,
        sessionConfigurator: FirSessionConfigurator.() -> Unit,
        project: Project,
        ktFiles: Collection<KtFile>
    ): FirSession {
        konst languageVersionSettings = module.languageVersionSettings
        return when {
            targetPlatform.isCommon() -> {
                FirCommonSessionFactory.createModuleBasedSession(
                    moduleData = moduleData,
                    sessionProvider = sessionProvider,
                    projectEnvironment = projectEnvironment!!,
                    incrementalCompilationContext = null,
                    extensionRegistrars = extensionRegistrars,
                    languageVersionSettings = languageVersionSettings,
                    registerExtraComponents = ::registerExtraComponents,
                    init = sessionConfigurator,
                )
            }
            targetPlatform.isJvm() -> {
                FirJvmSessionFactory.createModuleBasedSession(
                    moduleData,
                    sessionProvider,
                    PsiBasedProjectFileSearchScope(TopDownAnalyzerFacadeForJVM.newModuleSearchScope(project, ktFiles)),
                    projectEnvironment!!,
                    incrementalCompilationContext = null,
                    extensionRegistrars,
                    languageVersionSettings,
                    needRegisterJavaElementFinder = true,
                    registerExtraComponents = ::registerExtraComponents,
                    init = sessionConfigurator,
                )
            }
            targetPlatform.isJs() -> {
                TestFirJsSessionFactory.createModuleBasedSession(
                    moduleData,
                    sessionProvider,
                    extensionRegistrars,
                    languageVersionSettings,
                    null,
                    registerExtraComponents = ::registerExtraComponents,
                    sessionConfigurator,
                )
            }
            targetPlatform.isNative() -> {
                FirNativeSessionFactory.createModuleBasedSession(
                    moduleData,
                    sessionProvider,
                    extensionRegistrars,
                    languageVersionSettings,
                    registerExtraComponents = ::registerExtraComponents,
                    init = sessionConfigurator
                )
            }
            else -> error("Unsupported")
        }
    }

    companion object {
        fun initializeLibraryList(
            mainModule: TestModule,
            binaryModuleData: BinaryModuleData,
            targetPlatform: TargetPlatform,
            configuration: CompilerConfiguration,
            testServices: TestServices
        ): DependencyListForCliModule {
            return DependencyListForCliModule.build(binaryModuleData) {
                when {
                    targetPlatform.isCommon() || targetPlatform.isJvm() -> {
                        dependencies(configuration.jvmModularRoots.map { it.toPath() })
                        dependencies(configuration.jvmClasspathRoots.map { it.toPath() })
                        friendDependencies(configuration[JVMConfigurationKeys.FRIEND_PATHS] ?: emptyList())
                    }
                    targetPlatform.isJs() -> {
                        konst runtimeKlibsPaths = JsEnvironmentConfigurator.getRuntimePathsForModule(mainModule, testServices)
                        konst (transitiveLibraries, friendLibraries) = getTransitivesAndFriends(mainModule, testServices)
                        dependencies(runtimeKlibsPaths.map { Paths.get(it).toAbsolutePath() })
                        dependencies(transitiveLibraries.map { it.toPath().toAbsolutePath() })
                        friendDependencies(friendLibraries.map { it.toPath().toAbsolutePath() })
                    }
                    targetPlatform.isNative() -> {
                        konst runtimeKlibsPaths = NativeEnvironmentConfigurator.getRuntimePathsForModule(mainModule, testServices)
                        konst (transitiveLibraries, friendLibraries) = getTransitivesAndFriends(mainModule, testServices)
                        dependencies(runtimeKlibsPaths.map { Paths.get(it).toAbsolutePath() })
                        dependencies(transitiveLibraries.map { it.toPath().toAbsolutePath() })
                        friendDependencies(friendLibraries.map { it.toPath().toAbsolutePath() })
                    }
                    else -> error("Unsupported")
                }
            }
        }
    }
}
