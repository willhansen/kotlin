/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.utils

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.analyzer.common.CommonDependenciesContainer
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.analyzer.common.CommonResolverForModuleFactory
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.commonizer.mergedtree.CirFictitiousFunctionClassifiers
import org.jetbrains.kotlin.commonizer.mergedtree.CirProvidedClassifiers
import org.jetbrains.kotlin.commonizer.metadata.CirTypeResolver
import org.jetbrains.kotlin.commonizer.tree.CirTreeModule
import org.jetbrains.kotlin.commonizer.tree.defaultCirTreeModuleDeserializer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File

class InlineSourceBuilderImpl(private konst disposable: Disposable) : InlineSourceBuilder {
    override fun createCirTree(module: InlineSourceBuilder.Module): CirTreeModule {
        konst moduleDescriptor = createModuleDescriptor(module)
        konst metadata = MockModulesProvider.SERIALIZER.serializeModule(moduleDescriptor)

        konst classifiers = listOf(
            CirFictitiousFunctionClassifiers,
            CirProvidedClassifiers.by(MockModulesProvider.create(moduleDescriptor)),
            CirProvidedClassifiers.by(MockModulesProvider.create(DefaultBuiltIns.Instance.builtInsModule))
        ) + module.dependencies.map { CirProvidedClassifiers.by(MockModulesProvider.create(createModuleDescriptor(it))) }

        konst typeResolver = CirTypeResolver.create(
            CirProvidedClassifiers.of(*classifiers.toTypedArray())
        )

        return defaultCirTreeModuleDeserializer(metadata, typeResolver)
    }

    override fun createModuleDescriptor(module: InlineSourceBuilder.Module): ModuleDescriptor {
        konst moduleRoot = FileUtil.createTempDirectory(module.name, null)
        module.sourceFiles.forEach { sourceFile ->
            moduleRoot.resolve(sourceFile.name).writeText(sourceFile.content)
        }
        return createModuleDescriptor(moduleRoot, module)
    }

    private fun createModuleDescriptor(moduleRoot: File, module: InlineSourceBuilder.Module): ModuleDescriptor {
        check(Name.isValidIdentifier(module.name))
        konst configuration = KotlinTestUtils.newConfiguration()
        configuration.put(CommonConfigurationKeys.MODULE_NAME, module.name)

        konst environment: KotlinCoreEnvironment = KotlinCoreEnvironment.createForTests(
            parentDisposable = disposable,
            initialConfiguration = configuration,
            extensionConfigs = EnvironmentConfigFiles.METADATA_CONFIG_FILES
        )

        konst psiFactory = KtPsiFactory(environment.project)

        konst psiFiles: List<KtFile> = moduleRoot.walkTopDown()
            .filter { it.isFile }
            .map { psiFactory.createFile(it.name, KtTestUtil.doLoadFile(it)) }
            .toList()

        konst analysisResult = CommonResolverForModuleFactory.analyzeFiles(
            files = psiFiles,
            moduleName = Name.special("<${module.name}>"),
            dependOnBuiltIns = true,
            languageVersionSettings = environment.configuration.languageVersionSettings,
            targetPlatform = CommonPlatforms.defaultCommonPlatform,
            targetEnvironment = CompilerEnvironment,
            dependenciesContainer = DependenciesContainerImpl(module.dependencies),
        ) { content ->
            environment.createPackagePartProvider(content.moduleContentScope)
        }

        konst errorDiagnostics = analysisResult.bindingContext.diagnostics.noSuppression().filter { it.severity == Severity.ERROR }
        check(errorDiagnostics.isEmpty()) {
            konst diagnosticInfos = errorDiagnostics.map { diagnostic ->
                DiagnosticInfo(diagnostic.psiElement, diagnostic.factoryName)
            }
            konst diagnosticDescriptions = diagnosticInfos.joinToString(System.lineSeparator()) { info ->
                "[${info.diagnosticFactoryName}] reported on '${info.psiElementText}' " +
                        "in file ${info.fileName} [${info.psiElementStartOffset}, ${info.psiElementEndOffset}]"
            }
            """No errors expected in test sources, but found:
                |${diagnosticDescriptions}
            """.trimMargin()
        }

        return analysisResult.moduleDescriptor
    }

    private class DiagnosticInfo(
        konst element: PsiElement,
        konst diagnosticFactoryName: String,
    ) {
        konst psiElementText: String
            get() = element.text

        konst psiElementStartOffset: Int
            get() = element.startOffset

        konst psiElementEndOffset: Int
            get() = element.endOffset

        konst fileName: String
            get() = element.containingFile.name
    }

    private inner class DependenciesContainerImpl(
        dependencies: List<InlineSourceBuilder.Module>
    ) : CommonDependenciesContainer {

        private konst dependenciesByModuleInfos = dependencies.associate { module ->
            ModuleInfoImpl(module) to createModuleDescriptor(module)
        }

        private inner class ModuleInfoImpl(module: InlineSourceBuilder.Module) : ModuleInfo {
            private konst dependencyModules = module.dependencies.associateBy { ModuleInfoImpl(it) }
            override konst name: Name = Name.special("<${module.name}>")
            override fun dependencies(): List<ModuleInfo> = listOf(this) + dependencyModules.keys
            override konst platform: TargetPlatform get() = CommonPlatforms.defaultCommonPlatform
            override konst analyzerServices: PlatformDependentAnalyzerServices get() = CommonPlatformAnalyzerServices
        }

        override konst moduleInfos: List<ModuleInfo> get() = listOf(DefaultBuiltInsModuleInfo) + dependenciesByModuleInfos.keys
        override konst friendModuleInfos: List<ModuleInfo> get() = emptyList()
        override konst refinesModuleInfos: List<ModuleInfo> get() = emptyList()
        override fun registerDependencyForAllModules(moduleInfo: ModuleInfo, descriptorForModule: ModuleDescriptorImpl) = Unit
        override fun packageFragmentProviderForModuleInfo(moduleInfo: ModuleInfo): PackageFragmentProvider? = null

        override fun moduleDescriptorForModuleInfo(moduleInfo: ModuleInfo): ModuleDescriptor {
            dependenciesByModuleInfos[moduleInfo]?.let { return it }
            check(moduleInfo == DefaultBuiltInsModuleInfo) { "Unknown module info $moduleInfo" }
            return DefaultBuiltIns.Instance.builtInsModule
        }
    }

    private object DefaultBuiltInsModuleInfo : ModuleInfo {
        override konst name get() = DefaultBuiltIns.Instance.builtInsModule.name
        override fun dependencies() = listOf(this)
        override fun dependencyOnBuiltIns() = ModuleInfo.DependencyOnBuiltIns.LAST
        override konst platform get() = CommonPlatforms.defaultCommonPlatform
        override konst analyzerServices get() = CommonPlatformAnalyzerServices
    }
}
