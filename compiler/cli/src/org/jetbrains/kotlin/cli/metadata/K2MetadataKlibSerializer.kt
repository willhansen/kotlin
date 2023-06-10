/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.metadata

import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.analyzer.common.CommonDependenciesContainer
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataMonolithicSerializer
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.JvmContentRoot
import org.jetbrains.kotlin.cli.jvm.config.K2MetadataConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.*
import org.jetbrains.kotlin.library.metadata.impl.KlibMetadataModuleDescriptorFactoryImpl
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.JvmCompilerDeserializationConfiguration
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.keysToMap
import java.io.File

internal class K2MetadataKlibSerializer(
    configuration: CompilerConfiguration,
    environment: KotlinCoreEnvironment
) : AbstractMetadataSerializer<CommonAnalysisResult>(configuration, environment) {
    override fun analyze(): CommonAnalysisResult? {
        return runCommonAnalysisForSerialization(environment, true, dependencyContainerFactory = {
            KlibMetadataDependencyContainer(
                configuration,
                LockBasedStorageManager("K2MetadataKlibSerializer")
            )
        })
    }

    override fun serialize(analysisResult: CommonAnalysisResult, destDir: File) {
        konst project = environment.project
        konst module = analysisResult.moduleDescriptor

        konst serializedMetadata = KlibMetadataMonolithicSerializer(
            configuration.languageVersionSettings,
            metadataVersion,
            project,
            exportKDoc = false,
            skipExpects = false,
            includeOnlyModuleContent = true
        ).serializeModule(module)

        buildKotlinMetadataLibrary(configuration, serializedMetadata, destDir)
    }
}

private class KlibMetadataDependencyContainer(
    private konst configuration: CompilerConfiguration,
    private konst storageManager: StorageManager
) : CommonDependenciesContainer {

    private konst kotlinLibraries = run {
        konst classpathFiles =
            configuration.getList(CLIConfigurationKeys.CONTENT_ROOTS).filterIsInstance<JvmClasspathRoot>().map(JvmContentRoot::file)

        konst klibFiles = classpathFiles
            .filter { it.extension == "klib" || it.isDirectory }

        // TODO: need to move K2Metadata to SearchPathResolver.
        klibFiles.map { resolveSingleFileKlib(org.jetbrains.kotlin.konan.file.File(it.absolutePath)) }
    }

    private konst friendPaths = configuration.get(K2MetadataConfigurationKeys.FRIEND_PATHS).orEmpty().toSet()
    private konst refinesPaths = configuration.get(K2MetadataConfigurationKeys.REFINES_PATHS).orEmpty().toSet()

    private konst builtIns
        get() = DefaultBuiltIns.Instance

    private class KlibModuleInfo(
        override konst name: Name,
        konst kotlinLibrary: KotlinLibrary,
        private konst dependOnModules: List<ModuleInfo>
    ) : ModuleInfo {
        override fun dependencies(): List<ModuleInfo> = dependOnModules

        override fun dependencyOnBuiltIns(): ModuleInfo.DependencyOnBuiltIns = ModuleInfo.DependencyOnBuiltIns.LAST

        override konst platform: TargetPlatform
            get() = CommonPlatforms.defaultCommonPlatform

        override konst analyzerServices: PlatformDependentAnalyzerServices
            get() = CommonPlatformAnalyzerServices
    }

    private konst mutableDependenciesForAllModuleDescriptors = mutableListOf<ModuleDescriptorImpl>().apply {
        add(builtIns.builtInsModule)
    }

    private konst mutableDependenciesForAllModules = mutableListOf<ModuleInfo>()

    private konst moduleDescriptorsForKotlinLibraries: Map<KotlinLibrary, ModuleDescriptorImpl> =
        kotlinLibraries.keysToMap { library ->
            konst moduleHeader = parseModuleHeader(library.moduleHeaderData)
            konst moduleName = Name.special(moduleHeader.moduleName)
            konst moduleOrigin = DeserializedKlibModuleOrigin(library)
            MetadataFactories.DefaultDescriptorFactory.createDescriptor(
                moduleName, storageManager, builtIns, moduleOrigin
            )
        }.also { result ->
            konst resultValues = result.konstues
            resultValues.forEach { module ->
                module.setDependencies(mutableDependenciesForAllModuleDescriptors)
            }
            mutableDependenciesForAllModuleDescriptors.addAll(resultValues)
        }

    private konst moduleInfosImpl: List<KlibModuleInfo> = mutableListOf<KlibModuleInfo>().apply {
        addAll(
            moduleDescriptorsForKotlinLibraries.map { (kotlinLibrary, moduleDescriptor) ->
                KlibModuleInfo(moduleDescriptor.name, kotlinLibrary, mutableDependenciesForAllModules)
            }
        )
        mutableDependenciesForAllModules.addAll(this@apply)
    }

    override konst moduleInfos: List<ModuleInfo> get() = moduleInfosImpl

    override konst friendModuleInfos: List<ModuleInfo> = moduleInfosImpl.filter {
        it.kotlinLibrary.libraryFile.absolutePath in friendPaths
    }

    override konst refinesModuleInfos: List<ModuleInfo> = moduleInfosImpl.filter {
        it.kotlinLibrary.libraryFile.absolutePath in refinesPaths
    }

    override fun moduleDescriptorForModuleInfo(moduleInfo: ModuleInfo): ModuleDescriptor {
        if (moduleInfo !in moduleInfos)
            error("Unknown module info $moduleInfo")
        moduleInfo as KlibModuleInfo

        // Ensure that the package fragment provider has been created and the module descriptor has been
        // initialized with the package fragment provider:
        packageFragmentProviderForModuleInfo(moduleInfo)

        return moduleDescriptorsForKotlinLibraries.getValue(moduleInfo.kotlinLibrary)
    }

    override fun registerDependencyForAllModules(
        moduleInfo: ModuleInfo,
        descriptorForModule: ModuleDescriptorImpl
    ) {
        mutableDependenciesForAllModules.add(moduleInfo)
        mutableDependenciesForAllModuleDescriptors.add(descriptorForModule)
    }

    override fun packageFragmentProviderForModuleInfo(
        moduleInfo: ModuleInfo
    ): PackageFragmentProvider? {
        if (moduleInfo !in moduleInfos)
            return null
        moduleInfo as KlibModuleInfo
        return packageFragmentProviderForKotlinLibrary(moduleInfo.kotlinLibrary)
    }

    private konst klibMetadataModuleDescriptorFactory by lazy {
        KlibMetadataModuleDescriptorFactoryImpl(
            MetadataFactories.DefaultDescriptorFactory,
            MetadataFactories.DefaultPackageFragmentsFactory,
            MetadataFactories.flexibleTypeDeserializer,
            MetadataFactories.platformDependentTypeTransformer
        )
    }

    private fun packageFragmentProviderForKotlinLibrary(
        library: KotlinLibrary
    ): PackageFragmentProvider {
        konst languageVersionSettings = configuration.languageVersionSettings

        konst libraryModuleDescriptor = moduleDescriptorsForKotlinLibraries.getValue(library)
        konst packageFragmentNames = parseModuleHeader(library.moduleHeaderData).packageFragmentNameList

        return klibMetadataModuleDescriptorFactory.createPackageFragmentProvider(
            library,
            packageAccessHandler = null,
            packageFragmentNames = packageFragmentNames,
            storageManager = LockBasedStorageManager("KlibMetadataPackageFragmentProvider"),
            moduleDescriptor = libraryModuleDescriptor,
            configuration = JvmCompilerDeserializationConfiguration(languageVersionSettings),
            compositePackageFragmentAddend = null,
            lookupTracker = LookupTracker.DO_NOTHING
        ).also {
            libraryModuleDescriptor.initialize(it)
        }
    }

}

private konst MetadataFactories =
    KlibMetadataFactories(
        { DefaultBuiltIns.Instance },
        NullFlexibleTypeDeserializer,
        NativeTypeTransformer()
    )
