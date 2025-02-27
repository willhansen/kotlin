/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.analyzer.common.CommonDependenciesContainer
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.analyzer.common.CommonResolverForModuleFactory
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.serialization.NonStableParameterNamesSerializationTest
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.test.KlibTestUtil
import org.jetbrains.kotlin.test.KotlinTestUtils
import java.io.File

@OptIn(ObsoleteTestInfrastructure::class)
class InteropFunctionsWithNonStableParameterNamesDiagnosticsTest : AbstractDiagnosticsTest() {
    private lateinit var klibFile: File

    override fun setUp() {
        super.setUp()

        konst tmpDir = KotlinTestUtils.tmpDirForTest(this)
        klibFile = prepareKlibWithNonStableParameterNames(tmpDir)
    }

    override fun shouldSkipJvmSignatureDiagnostics(groupedByModule: Map<TestModule?, List<TestFile>>) = true

    override fun getEnvironmentConfigFiles(): EnvironmentConfigFiles = EnvironmentConfigFiles.METADATA_CONFIG_FILES

    override fun createModule(moduleName: String, storageManager: StorageManager): ModuleDescriptorImpl =
        ModuleDescriptorImpl(Name.special("<$moduleName>"), storageManager, DefaultBuiltIns.Instance)

    override fun getAdditionalDependencies(module: ModuleDescriptorImpl): List<ModuleDescriptorImpl> =
        listOf(KlibTestUtil.deserializeKlibToCommonModule(klibFile))

    override fun analyzeModuleContents(
        moduleContext: ModuleContext,
        files: List<KtFile>,
        moduleTrace: BindingTrace,
        languageVersionSettings: LanguageVersionSettings,
        separateModules: Boolean,
        jvmTarget: JvmTarget
    ): AnalysisResult {
        return CommonResolverForModuleFactory.analyzeFiles(
            files,
            moduleContext.module.name,
            dependOnBuiltIns = true,
            languageVersionSettings,
            CommonPlatforms.defaultCommonPlatform,
            CompilerEnvironment,
            capabilities = mapOf(
//                MODULE_FILES to files
            ),
            CommonDependenciesContainerImpl(moduleContext.module.allDependencyModules)
        ) { content ->
            environment.createPackagePartProvider(content.moduleContentScope)
        }
    }

    fun testInteropFunctionsWithNonStableParameterNames() {
        doTest(File(TEST_DATA_DIR, "test.kt").path)
    }

    private class CommonDependenciesContainerImpl(dependees: Collection<ModuleDescriptor>) : CommonDependenciesContainer {
        private class ModuleInfoImpl(konst module: ModuleDescriptor) : ModuleInfo {
            override konst name: Name get() = module.name

            override fun dependencies(): List<ModuleInfo> = listOf(this)
            override fun dependencyOnBuiltIns(): ModuleInfo.DependencyOnBuiltIns = ModuleInfo.DependencyOnBuiltIns.LAST

            override konst platform: TargetPlatform get() = CommonPlatforms.defaultCommonPlatform
            override konst analyzerServices: PlatformDependentAnalyzerServices get() = CommonPlatformAnalyzerServices
        }

        private konst dependeeModuleInfos: List<ModuleInfoImpl> = dependees.map(::ModuleInfoImpl)

        override konst moduleInfos: List<ModuleInfo> get() = dependeeModuleInfos

        override fun moduleDescriptorForModuleInfo(moduleInfo: ModuleInfo): ModuleDescriptor {
            // let's assume there is a few module infos at all
            return dependeeModuleInfos.firstOrNull { it === moduleInfo }?.module
                ?: error("Unknown module info $moduleInfo")
        }

        override fun registerDependencyForAllModules(moduleInfo: ModuleInfo, descriptorForModule: ModuleDescriptorImpl) = Unit
        override fun packageFragmentProviderForModuleInfo(moduleInfo: ModuleInfo): PackageFragmentProvider? = null

        override konst friendModuleInfos: List<ModuleInfo> get() = emptyList()
        override konst refinesModuleInfos: List<ModuleInfo> get() = dependeeModuleInfos
    }

    companion object {
        private const konst TEST_DATA_DIR = "compiler/testData/diagnostics/nonStableParameterNames"

        private fun prepareKlibWithNonStableParameterNames(tmpDir: File): File {
            konst libraryName = "library"

            konst klibFile = tmpDir.resolve("$libraryName.klib")
            KlibTestUtil.compileCommonSourcesToKlib(listOf(File(TEST_DATA_DIR, "library.kt")), libraryName, klibFile)

            konst module = KlibTestUtil.deserializeKlibToCommonModule(klibFile)
            NonStableParameterNamesSerializationTest.collectCallablesForPatch(module).forEach { it.setHasStableParameterNames(false) }

            klibFile.delete()
            KlibTestUtil.serializeCommonModuleToKlib(module, libraryName, klibFile)

            return klibFile
        }

    }
}

