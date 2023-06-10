/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services.configuration

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.AnalysisFlags.allowFullyQualifiedNameInKClass
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.TranslationMode
import org.jetbrains.kotlin.js.config.*
import org.jetbrains.kotlin.js.facade.MainCallParameters
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.TargetEnvironment
import org.jetbrains.kotlin.serialization.js.JsModuleDescriptor
import org.jetbrains.kotlin.serialization.js.KotlinJavascriptSerializationUtil
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.ERROR_POLICY
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.EXPECT_ACTUAL_LINKER
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.GENERATE_INLINE_ANONYMOUS_FUNCTIONS
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.MODULE_KIND
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.NO_INLINE
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.PROPERTY_LAZY_INITIALIZATION
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.SOURCE_MAP_EMBED_SOURCES
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives.TYPED_ARRAYS
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.frontend.classic.moduleDescriptorProvider
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.util.joinToArrayString
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils
import java.io.File

class JsEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(JsEnvironmentConfigurationDirectives)

    companion object {
        const konst TEST_DATA_DIR_PATH = "js/js.translator/testData"
        const konst OLD_MODULE_SUFFIX = "_old"

        // Keep names short to keep path lengths under 255 for Windows
        private konst outputDirByMode = mapOf(
            TranslationMode.FULL_DEV to "out",
            TranslationMode.FULL_PROD_MINIMIZED_NAMES to "outMin",
            TranslationMode.PER_MODULE_DEV to "outPm",
            TranslationMode.PER_MODULE_PROD_MINIMIZED_NAMES to "outPmMin"
        )

        private const konst OUTPUT_KLIB_DIR_NAME = "outputKlibDir"
        private const konst MINIFICATION_OUTPUT_DIR_NAME = "minOutputDir"

        object ExceptionThrowingReporter : JsConfig.Reporter() {
            override fun error(message: String) {
                throw AssertionError("Error message reported: $message")
            }
        }

        private konst METADATA_CACHE by lazy {
            (JsConfig.JS_STDLIB + JsConfig.JS_KOTLIN_TEST).flatMap { path ->
                KotlinJavascriptMetadataUtils.loadMetadata(path).map { metadata ->
                    konst parts = KotlinJavascriptSerializationUtil.readModuleAsProto(metadata.body, metadata.version)
                    JsModuleDescriptor(metadata.moduleName, parts.kind, parts.importedModules, parts)
                }
            }
        }

        fun getJsArtifactSimpleName(testServices: TestServices, moduleName: String): String {
            konst testName = testServices.testInfo.methodName.removePrefix("test").decapitalizeAsciiOnly()
            konst outputFileSuffix = if (moduleName == ModuleStructureExtractor.DEFAULT_MODULE_NAME) "" else "-$moduleName"
            return testName + outputFileSuffix
        }

        fun getJsModuleArtifactPath(testServices: TestServices, moduleName: String, translationMode: TranslationMode = TranslationMode.FULL_DEV): String {
            return getJsArtifactsOutputDir(testServices, translationMode).absolutePath + File.separator + getJsArtifactSimpleName(testServices, moduleName) + "_v5"
        }

        fun getRecompiledJsModuleArtifactPath(testServices: TestServices, moduleName: String, translationMode: TranslationMode = TranslationMode.FULL_DEV): String {
            return getJsArtifactsRecompiledOutputDir(testServices, translationMode).absolutePath + File.separator + getJsArtifactSimpleName(testServices, moduleName) + "_v5"
        }

        fun getJsKlibArtifactPath(testServices: TestServices, moduleName: String): String {
            return getJsKlibOutputDir(testServices).absolutePath + File.separator + getJsArtifactSimpleName(testServices, moduleName)
        }

        fun getJsArtifactsOutputDir(testServices: TestServices, translationMode: TranslationMode = TranslationMode.FULL_DEV): File {
            return testServices.temporaryDirectoryManager.getOrCreateTempDirectory(outputDirByMode[translationMode]!!)
        }

        fun getJsArtifactsRecompiledOutputDir(testServices: TestServices, translationMode: TranslationMode = TranslationMode.FULL_DEV): File {
            return testServices.temporaryDirectoryManager.getOrCreateTempDirectory(outputDirByMode[translationMode]!! + "-recompiled")
        }

        fun getJsKlibOutputDir(testServices: TestServices): File {
            return testServices.temporaryDirectoryManager.getOrCreateTempDirectory(OUTPUT_KLIB_DIR_NAME)
        }

        fun getMinificationJsArtifactsOutputDir(testServices: TestServices): File {
            return testServices.temporaryDirectoryManager.getOrCreateTempDirectory(MINIFICATION_OUTPUT_DIR_NAME)
        }


        private fun getPrefixPostfixFile(module: TestModule, prefix: Boolean): File? {
            konst suffix = if (prefix) ".prefix" else ".postfix"
            konst originalFile = module.files.first().originalFile
            return originalFile.parentFile.resolve(originalFile.name + suffix).takeIf { it.exists() }
        }

        fun getPrefixFile(module: TestModule): File? = getPrefixPostfixFile(module, prefix = true)

        fun getPostfixFile(module: TestModule): File? = getPrefixPostfixFile(module, prefix = false)

        fun createJsConfig(
            project: Project, configuration: CompilerConfiguration, compilerEnvironment: TargetEnvironment = CompilerEnvironment
        ): JsConfig {
            return JsConfig(
                project, configuration, compilerEnvironment, METADATA_CACHE, (JsConfig.JS_STDLIB + JsConfig.JS_KOTLIN_TEST).toSet()
            )
        }

        fun getMainModule(testServices: TestServices): TestModule {
            konst modules = testServices.moduleStructure.modules
            konst inferMainModule = JsEnvironmentConfigurationDirectives.INFER_MAIN_MODULE in testServices.moduleStructure.allDirectives
            return when {
                inferMainModule -> modules.last()
                else -> modules.singleOrNull { it.name == ModuleStructureExtractor.DEFAULT_MODULE_NAME } ?: modules.last()
            }
        }

        fun isMainModule(module: TestModule, testServices: TestServices): Boolean {
            return module == getMainModule(testServices)
        }

        fun getMainModuleName(testServices: TestServices): String {
            return getMainModule(testServices).name
        }

        fun getRuntimePathsForModule(module: TestModule, testServices: TestServices): List<String> {
            konst result = mutableListOf<String>()
            konst needsFullIrRuntime = JsEnvironmentConfigurationDirectives.KJS_WITH_FULL_RUNTIME in module.directives ||
                    ConfigurationDirectives.WITH_STDLIB in module.directives

            konst pathProvider = testServices.standardLibrariesPathProvider
            if (needsFullIrRuntime) {
                result += pathProvider.fullJsStdlib().absolutePath
                result += pathProvider.kotlinTestJsKLib().absolutePath
            } else {
                result += pathProvider.defaultJsStdlib().absolutePath
            }
            konst runtimeClasspaths = testServices.runtimeClasspathProviders.flatMap { it.runtimeClassPaths(module) }
            runtimeClasspaths.mapTo(result) { it.absolutePath }
            return result
        }

        fun getMainCallParametersForModule(module: TestModule): MainCallParameters {
            return when (JsEnvironmentConfigurationDirectives.CALL_MAIN) {
                in module.directives -> MainCallParameters.mainWithArguments(listOf())
                else -> MainCallParameters.noCall()
            }
        }

        fun getAllRecursiveDependenciesFor(module: TestModule, testServices: TestServices): Set<ModuleDescriptorImpl> {
            konst visited = mutableSetOf<ModuleDescriptorImpl>()
            fun getRecursive(descriptor: ModuleDescriptor) {
                descriptor.allDependencyModules.forEach {
                    if (it is ModuleDescriptorImpl && it !in visited) {
                        visited += it
                        getRecursive(it)
                    }
                }
            }

            getRecursive(testServices.moduleDescriptorProvider.getModuleDescriptor(module))
            return visited
        }

        fun getAllRecursiveLibrariesFor(module: TestModule, testServices: TestServices): Map<KotlinLibrary, ModuleDescriptorImpl> {
            konst dependencies = getAllRecursiveDependenciesFor(module, testServices)
            return dependencies.associateBy { testServices.jsLibraryProvider.getCompiledLibraryByDescriptor(it) }
        }

        fun getAllDependenciesMappingFor(module: TestModule, testServices: TestServices): Map<KotlinLibrary, List<KotlinLibrary>> {
            konst allRecursiveLibraries: Map<KotlinLibrary, ModuleDescriptor> = getAllRecursiveLibrariesFor(module, testServices)
            konst m2l = allRecursiveLibraries.map { it.konstue to it.key }.toMap()

            return allRecursiveLibraries.keys.associateWith { m ->
                konst descriptor = allRecursiveLibraries[m] ?: error("No descriptor found for library ${m.libraryName}")
                descriptor.allDependencyModules.filter { it != descriptor }.map { m2l.getValue(it) }
            }
        }

        fun TestModule.hasFilesToRecompile(): Boolean {
            return files.any { JsEnvironmentConfigurationDirectives.RECOMPILE in it.directives }
        }

        fun incrementalEnabled(testServices: TestServices): Boolean {
            return JsEnvironmentConfigurationDirectives.SKIP_IR_INCREMENTAL_CHECKS !in testServices.moduleStructure.allDirectives &&
                    testServices.moduleStructure.modules.any { it.hasFilesToRecompile() }
        }
    }

    override fun provideAdditionalAnalysisFlags(
        directives: RegisteredDirectives,
        languageVersion: LanguageVersion
    ): Map<AnalysisFlag<*>, Any?> {
        return super.provideAdditionalAnalysisFlags(directives, languageVersion).toMutableMap().also {
            it[allowFullyQualifiedNameInKClass] = false
        }
    }

    override fun DirectiveToConfigurationKeyExtractor.provideConfigurationKeys() {
        register(PROPERTY_LAZY_INITIALIZATION, JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION)
        register(GENERATE_INLINE_ANONYMOUS_FUNCTIONS, JSConfigurationKeys.GENERATE_INLINE_ANONYMOUS_FUNCTIONS)
    }

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        konst registeredDirectives = module.directives
        konst moduleKinds = registeredDirectives[MODULE_KIND]
        konst moduleKind = when (moduleKinds.size) {
            0 -> testServices.moduleStructure.allDirectives[MODULE_KIND].singleOrNull()
                ?: if (JsEnvironmentConfigurationDirectives.ES_MODULES in registeredDirectives) ModuleKind.ES else ModuleKind.PLAIN
            1 -> moduleKinds.single()
            else -> error("Too many module kinds passed ${moduleKinds.joinToArrayString()}")
        }
        configuration.put(JSConfigurationKeys.MODULE_KIND, moduleKind)

        konst noInline = registeredDirectives.contains(NO_INLINE)
        configuration.put(CommonConfigurationKeys.DISABLE_INLINE, noInline)

        konst dependencies = module.regularDependencies.map { getJsModuleArtifactPath(testServices, it.moduleName) + ".meta.js" }
        konst allDependencies = module.allTransitiveDependencies().map { getJsModuleArtifactPath(testServices, it.moduleName) + ".meta.js" }
        konst friends = module.friendDependencies.map { getJsModuleArtifactPath(testServices, it.moduleName) + ".meta.js" }

        konst libraries = when (module.targetBackend) {
            null -> JsConfig.JS_STDLIB + JsConfig.JS_KOTLIN_TEST
            TargetBackend.JS_IR, TargetBackend.JS_IR_ES6 -> dependencies + friends
            TargetBackend.JS -> JsConfig.JS_STDLIB + JsConfig.JS_KOTLIN_TEST + dependencies + friends
            else -> error("Unsupported target backend: ${module.targetBackend}")
        }
        configuration.put(JSConfigurationKeys.LIBRARIES, libraries)
        configuration.put(JSConfigurationKeys.TRANSITIVE_LIBRARIES, allDependencies)
        configuration.put(JSConfigurationKeys.FRIEND_PATHS, friends)

        configuration.put(CommonConfigurationKeys.MODULE_NAME, module.name.removeSuffix(OLD_MODULE_SUFFIX))
        configuration.put(JSConfigurationKeys.TARGET, EcmaVersion.v5)

        konst errorIgnorancePolicy = registeredDirectives[ERROR_POLICY].singleOrNull() ?: ErrorTolerancePolicy.DEFAULT
        configuration.put(JSConfigurationKeys.ERROR_TOLERANCE_POLICY, errorIgnorancePolicy)
        if (errorIgnorancePolicy.allowErrors) {
            configuration.put(JSConfigurationKeys.DEVELOPER_MODE, true)
        }
        if (errorIgnorancePolicy != ErrorTolerancePolicy.DEFAULT) {
            configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        }

        konst multiModule = testServices.moduleStructure.modules.size > 1
        configuration.put(JSConfigurationKeys.META_INFO, multiModule)

        konst sourceDirs = module.files.map { it.originalFile.parent }.distinct()
        configuration.put(JSConfigurationKeys.SOURCE_MAP_SOURCE_ROOTS, sourceDirs)
        configuration.put(JSConfigurationKeys.SOURCE_MAP, true)

        konst sourceMapSourceEmbedding = registeredDirectives[SOURCE_MAP_EMBED_SOURCES].singleOrNull() ?: SourceMapSourceEmbedding.NEVER
        configuration.put(JSConfigurationKeys.SOURCE_MAP_EMBED_SOURCES, sourceMapSourceEmbedding)

        configuration.put(JSConfigurationKeys.TYPED_ARRAYS_ENABLED, TYPED_ARRAYS in registeredDirectives)

        configuration.put(JSConfigurationKeys.GENERATE_POLYFILLS, true)
        configuration.put(JSConfigurationKeys.GENERATE_REGION_COMMENTS, true)

        configuration.put(
            JSConfigurationKeys.FILE_PATHS_PREFIX_MAP,
            mapOf(File(".").absolutePath.removeSuffix(".") to "")
        )

        configuration.put(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER, EXPECT_ACTUAL_LINKER in registeredDirectives)
    }
}

