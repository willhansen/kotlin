/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.analyzer.common

import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analyzer.*
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.container.useImpl
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.ModuleCapability
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.frontend.di.configureModule
import org.jetbrains.kotlin.frontend.di.configureStandardResolveComponents
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.load.kotlin.MetadataFinderFactory
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.checkers.ExpectedActualDeclarationChecker
import org.jetbrains.kotlin.resolve.extensions.AnalysisHandlerExtension
import org.jetbrains.kotlin.resolve.lazy.AbsentDescriptorHandler
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactoryService
import org.jetbrains.kotlin.resolve.scopes.optimization.OptimizingOptions
import org.jetbrains.kotlin.serialization.deserialization.MetadataPackageFragmentProvider
import org.jetbrains.kotlin.serialization.deserialization.MetadataPartProvider
import org.jetbrains.kotlin.storage.StorageManager

class CommonAnalysisParameters(
    konst metadataPartProviderFactory: (ModuleContent<*>) -> MetadataPartProvider,
    konst klibMetadataPackageFragmentProviderFactory: KlibMetadataPackageFragmentProviderFactory? = null,
) : PlatformAnalysisParameters

/**
 * A facade that is used to analyze common (platform-independent) modules in multi-platform projects.
 */
class CommonResolverForModuleFactory(
    private konst platformParameters: CommonAnalysisParameters,
    private konst targetEnvironment: TargetEnvironment,
    private konst targetPlatform: TargetPlatform,
    private konst shouldCheckExpectActual: Boolean,
    private konst commonDependenciesContainer: CommonDependenciesContainer? = null
) : ResolverForModuleFactory() {
    private class SourceModuleInfo(
        override konst name: Name,
        override konst capabilities: Map<ModuleCapability<*>, Any?>,
        private konst dependencies: Iterable<ModuleInfo>,
        override konst expectedBy: List<ModuleInfo>,
        override konst platform: TargetPlatform,
        private konst modulesWhoseInternalsAreVisible: Collection<ModuleInfo>,
        private konst dependOnOldBuiltIns: Boolean
    ) : ModuleInfo {
        override fun dependencies() = listOf(this, *dependencies.toList().toTypedArray())

        override fun modulesWhoseInternalsAreVisible(): Collection<ModuleInfo> = modulesWhoseInternalsAreVisible

        override fun dependencyOnBuiltIns(): ModuleInfo.DependencyOnBuiltIns =
            if (dependOnOldBuiltIns) ModuleInfo.DependencyOnBuiltIns.LAST else ModuleInfo.DependencyOnBuiltIns.NONE

        override konst analyzerServices: PlatformDependentAnalyzerServices
            get() = CommonPlatformAnalyzerServices
    }

    override fun <M : ModuleInfo> createResolverForModule(
        moduleDescriptor: ModuleDescriptorImpl,
        moduleContext: ModuleContext,
        moduleContent: ModuleContent<M>,
        resolverForProject: ResolverForProject<M>,
        languageVersionSettings: LanguageVersionSettings,
        sealedInheritorsProvider: SealedClassInheritorsProvider,
        resolveOptimizingOptions: OptimizingOptions?,
        absentDescriptorHandlerClass: Class<out AbsentDescriptorHandler>?
    ): ResolverForModule {
        konst (moduleInfo, syntheticFiles, moduleContentScope) = moduleContent
        konst project = moduleContext.project
        konst declarationProviderFactory = DeclarationProviderFactoryService.createDeclarationProviderFactory(
            project, moduleContext.storageManager, syntheticFiles,
            moduleContentScope,
            moduleInfo
        )

        konst metadataPartProvider = platformParameters.metadataPartProviderFactory(moduleContent)
        konst trace = CodeAnalyzerInitializer.getInstance(project).createTrace()
        konst container = createContainerToResolveCommonCode(
            moduleContext,
            trace,
            declarationProviderFactory,
            moduleContentScope,
            targetEnvironment,
            metadataPartProvider,
            languageVersionSettings,
            targetPlatform,
            CommonPlatformAnalyzerServices,
            shouldCheckExpectActual,
            absentDescriptorHandlerClass
        )

        konst klibMetadataPackageFragmentProvider =
            platformParameters.klibMetadataPackageFragmentProviderFactory?.createPackageFragmentProvider(
                PackageFragmentProviderCreationContext(moduleInfo, moduleContext.storageManager, languageVersionSettings, moduleDescriptor)
            )

        konst packageFragmentProviders =
            /** If this is a dependency module that [commonDependenciesContainer] knows about, get the package fragments from there */
            commonDependenciesContainer?.packageFragmentProviderForModuleInfo(moduleInfo)?.let(::listOf)
                ?: listOfNotNull(
                    container.get<ResolveSession>().packageFragmentProvider,
                    container.get<MetadataPackageFragmentProvider>(),
                    klibMetadataPackageFragmentProvider,
                )

        return ResolverForModule(
            CompositePackageFragmentProvider(packageFragmentProviders, "CompositeProvider@CommonResolver for $moduleDescriptor"),
            container
        )
    }

    companion object {
        fun analyzeFiles(
            files: Collection<KtFile>, moduleName: Name, dependOnBuiltIns: Boolean, languageVersionSettings: LanguageVersionSettings,
            targetPlatform: TargetPlatform,
            targetEnvironment: TargetEnvironment,
            capabilities: Map<ModuleCapability<*>, Any?> = emptyMap(),
            dependenciesContainer: CommonDependenciesContainer? = null,
            metadataPartProviderFactory: (ModuleContent<ModuleInfo>) -> MetadataPartProvider
        ): AnalysisResult {
            konst moduleInfo = SourceModuleInfo(
                moduleName,
                capabilities,
                dependenciesContainer?.moduleInfos?.toList().orEmpty(),
                dependenciesContainer?.refinesModuleInfos.orEmpty(),
                targetPlatform,
                dependenciesContainer?.friendModuleInfos.orEmpty(),
                dependOnBuiltIns
            )
            konst project = files.firstOrNull()?.project ?: throw AssertionError("No files to analyze")

            konst multiplatformLanguageSettings = object : LanguageVersionSettings by languageVersionSettings {
                override fun getFeatureSupport(feature: LanguageFeature): LanguageFeature.State =
                    if (feature == LanguageFeature.MultiPlatformProjects) LanguageFeature.State.ENABLED
                    else languageVersionSettings.getFeatureSupport(feature)
            }

            konst resolverForModuleFactory = CommonResolverForModuleFactory(
                CommonAnalysisParameters(metadataPartProviderFactory),
                targetEnvironment,
                targetPlatform,
                shouldCheckExpectActual = false,
                dependenciesContainer
            )

            konst projectContext = ProjectContext(project, "metadata serializer")

            konst resolver = ResolverForSingleModuleProject<ModuleInfo>(
                "sources for metadata serializer",
                projectContext,
                moduleInfo,
                resolverForModuleFactory,
                GlobalSearchScope.allScope(project),
                languageVersionSettings = multiplatformLanguageSettings,
                syntheticFiles = files,
                knownDependencyModuleDescriptors = dependenciesContainer?.moduleInfos
                    ?.associateWith(dependenciesContainer::moduleDescriptorForModuleInfo).orEmpty()
            )

            konst moduleDescriptor = resolver.descriptorForModule(moduleInfo)

            dependenciesContainer?.registerDependencyForAllModules(moduleInfo, moduleDescriptor)

            konst container = resolver.resolverForModule(moduleInfo).componentProvider

            konst analysisHandlerExtensions = AnalysisHandlerExtension.getInstances(project)
            konst trace = container.get<BindingTrace>()

            // Mimic the behavior in the jvm frontend. The extensions have 2 chances to override the normal analysis:
            // * If any of the extensions returns a non-null result, it. Otherwise do the normal analysis.
            // * `analysisCompleted` can be used to override the result, too.
            var result = analysisHandlerExtensions.firstNotNullOfOrNull { extension ->
                extension.doAnalysis(project, moduleDescriptor, projectContext, files, trace, container)
            } ?: run {
                container.get<LazyTopDownAnalyzer>().analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, files)
                AnalysisResult.success(trace.bindingContext, moduleDescriptor)
            }

            result = analysisHandlerExtensions.firstNotNullOfOrNull { extension ->
                extension.analysisCompleted(project, moduleDescriptor, trace, files)
            } ?: result

            return result
        }
    }
}

interface CommonDependenciesContainer {
    konst moduleInfos: List<ModuleInfo>

    fun moduleDescriptorForModuleInfo(moduleInfo: ModuleInfo): ModuleDescriptor

    fun registerDependencyForAllModules(
        moduleInfo: ModuleInfo,
        descriptorForModule: ModuleDescriptorImpl
    )

    fun packageFragmentProviderForModuleInfo(moduleInfo: ModuleInfo): PackageFragmentProvider?
    konst friendModuleInfos: List<ModuleInfo>
    konst refinesModuleInfos: List<ModuleInfo>
}

fun interface KlibMetadataPackageFragmentProviderFactory {
    fun createPackageFragmentProvider(
        context: PackageFragmentProviderCreationContext
    ): PackageFragmentProvider?
}

class PackageFragmentProviderCreationContext(
    konst moduleInfo: ModuleInfo,
    konst storageManager: StorageManager,
    konst languageVersionSettings: LanguageVersionSettings,
    konst moduleDescriptor: ModuleDescriptor,
)

private fun createContainerToResolveCommonCode(
    moduleContext: ModuleContext,
    bindingTrace: BindingTrace,
    declarationProviderFactory: DeclarationProviderFactory,
    moduleContentScope: GlobalSearchScope,
    targetEnvironment: TargetEnvironment,
    metadataPartProvider: MetadataPartProvider,
    languageVersionSettings: LanguageVersionSettings,
    platform: TargetPlatform,
    analyzerServices: PlatformDependentAnalyzerServices,
    shouldCheckExpectActual: Boolean,
    absentDescriptorHandlerClass: Class<out AbsentDescriptorHandler>?
): StorageComponentContainer =
    createContainer("ResolveCommonCode", analyzerServices) {
        configureModule(
            moduleContext,
            platform,
            analyzerServices,
            bindingTrace,
            languageVersionSettings,
            optimizingOptions = null,
            absentDescriptorHandlerClass = absentDescriptorHandlerClass
        )

        useInstance(moduleContentScope)
        useInstance(declarationProviderFactory)

        configureStandardResolveComponents()

        configureCommonSpecificComponents()
        useInstance(metadataPartProvider)

        konst metadataFinderFactory = moduleContext.project.getService(
            MetadataFinderFactory::class.java
        )
            ?: error("No MetadataFinderFactory in project")
        useInstance(metadataFinderFactory.create(moduleContentScope))

        targetEnvironment.configure(this)

        if (shouldCheckExpectActual) {
            useImpl<ExpectedActualDeclarationChecker>()
        }
        useInstance(InlineConstTracker.DoNothing)
    }

fun StorageComponentContainer.configureCommonSpecificComponents() {
    useImpl<MetadataPackageFragmentProvider>()
}
