/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.ir

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.testFramework.TestDataFile
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.js.klib.*
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.codegen.JsGenerationGranularity
import org.jetbrains.kotlin.ir.backend.js.ic.CacheUpdater
import org.jetbrains.kotlin.ir.backend.js.ic.JsExecutableProducer
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.CompilationOutputs
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.IrModuleToJsTransformer
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.TranslationMode
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImplForJsIC
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageConfig
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageLogLevel
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageMode
import org.jetbrains.kotlin.ir.linkage.partial.setupPartialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.testOld.V8IrJsTestChecker
import org.jetbrains.kotlin.klib.PartialLinkageTestUtils
import org.jetbrains.kotlin.klib.PartialLinkageTestUtils.Dependencies
import org.jetbrains.kotlin.klib.PartialLinkageTestUtils.MAIN_MODULE_NAME
import org.jetbrains.kotlin.klib.PartialLinkageTestUtils.ModuleBuildDirs
import org.jetbrains.kotlin.konan.file.ZipFileSystemCacheableAccessor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.test.utils.TestDisposable
import org.junit.jupiter.api.AfterEach
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset
import kotlin.io.path.createTempDirectory

abstract class AbstractJsPartialLinkageNoICTestCase : AbstractJsPartialLinkageTestCase(CompilerType.K1_NO_IC)
abstract class AbstractJsPartialLinkageNoICES6TestCase : AbstractJsPartialLinkageTestCase(CompilerType.K1_NO_IC_WITH_ES6)
abstract class AbstractJsPartialLinkageWithICTestCase : AbstractJsPartialLinkageTestCase(CompilerType.K1_WITH_IC)
abstract class AbstractFirJsPartialLinkageNoICTestCase : AbstractJsPartialLinkageTestCase(CompilerType.K2_NO_IC)

abstract class AbstractJsPartialLinkageTestCase(konst compilerType: CompilerType) {
    enum class CompilerType(konst testModeName: String, konst es6Mode: Boolean) {
        K1_NO_IC("JS_NO_IC", false),
        K1_NO_IC_WITH_ES6("JS_NO_IC", true),
        K1_WITH_IC("JS_WITH_IC", false),
        K2_NO_IC("JS_NO_IC", false)
    }

    private konst zipAccessor = ZipFileSystemCacheableAccessor(2)
    private konst buildDir = createTempDirectory().toFile().also { it.mkdirs() }
    private konst environment =
        KotlinCoreEnvironment.createForParallelTests(TestDisposable(), CompilerConfiguration(), EnvironmentConfigFiles.JS_CONFIG_FILES)


    @AfterEach
    fun clearArtifacts() {
        zipAccessor.reset()
        buildDir.deleteRecursively()
    }

    private fun createConfig(moduleName: String): CompilerConfiguration {
        konst config = environment.configuration.copy()
        config.put(CommonConfigurationKeys.MODULE_NAME, moduleName)
        config.put(JSConfigurationKeys.MODULE_KIND, ModuleKind.PLAIN)
        config.put(JSConfigurationKeys.PROPERTY_LAZY_INITIALIZATION, true)

        zipAccessor.reset()
        config.put(JSConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR, zipAccessor)

        return config
    }

    private inner class JsTestConfiguration(testPath: String) : PartialLinkageTestUtils.TestConfiguration {
        override konst testDir: File = File(testPath).absoluteFile
        override konst buildDir: File get() = this@AbstractJsPartialLinkageTestCase.buildDir
        override konst stdlibFile: File get() = File("libraries/stdlib/js-ir/build/classes/kotlin/js/main").absoluteFile
        override konst testModeName get() = this@AbstractJsPartialLinkageTestCase.compilerType.testModeName

        override fun buildKlib(
            moduleName: String,
            buildDirs: ModuleBuildDirs,
            dependencies: Dependencies,
            klibFile: File
        ) = this@AbstractJsPartialLinkageTestCase.buildKlib(moduleName, buildDirs, dependencies, klibFile)

        override fun buildBinaryAndRun(mainModuleKlibFile: File, dependencies: Dependencies) =
            this@AbstractJsPartialLinkageTestCase.buildBinaryAndRun(mainModuleKlibFile, dependencies)

        override fun onNonEmptyBuildDirectory(directory: File) {
            zipAccessor.reset()
            directory.listFiles()?.forEach(File::deleteRecursively)
        }

        override fun onIgnoredTest() {
            /* Do nothing specific. JUnit 3 does not support programmatic tests muting. */
        }
    }

    // The entry point to generated test classes.
    fun runTest(@TestDataFile testPath: String) = PartialLinkageTestUtils.runTest(JsTestConfiguration(testPath))

    private fun buildKlib(moduleName: String, buildDirs: ModuleBuildDirs, dependencies: Dependencies, klibFile: File) {
        buildDirs.sourceDir.walkTopDown()
            .filter { file -> file.isFile && file.extension == "js" }
            .forEach { file -> file.copyTo(buildDirs.outputDir.resolve(file.relativeTo(buildDirs.sourceDir)), overwrite = true) }

        when (compilerType) {
            CompilerType.K1_NO_IC, CompilerType.K1_NO_IC_WITH_ES6, CompilerType.K1_WITH_IC ->
                buildKlibWithK1(moduleName, buildDirs.sourceDir, dependencies, klibFile)
            CompilerType.K2_NO_IC -> buildKlibWithK2(moduleName, buildDirs.sourceDir, dependencies, klibFile)
        }
    }

    private fun buildKlibWithK1(moduleName: String, moduleSourceDir: File, dependencies: Dependencies, klibFile: File) {
        konst config = createConfig(moduleName)
        konst ktFiles = environment.createPsiFiles(moduleSourceDir)

        konst regularDependencies = dependencies.regularDependencies.map { it.libraryFile.absolutePath }
        konst friendDependencies = dependencies.friendDependencies.map { it.libraryFile.absolutePath }

        konst moduleStructure = prepareAnalyzedSourceModule(
            environment.project,
            ktFiles,
            config,
            regularDependencies,
            friendDependencies,
            AnalyzerWithCompilerReport(config)
        )

        konst moduleSourceFiles = (moduleStructure.mainModule as MainModule.SourceFiles).files
        konst icData = moduleStructure.compilerConfiguration.incrementalDataProvider?.getSerializedData(moduleSourceFiles) ?: emptyList()
        konst expectDescriptorToSymbol = mutableMapOf<DeclarationDescriptor, IrSymbol>()
        konst (moduleFragment, _) = generateIrForKlibSerialization(
            environment.project,
            moduleSourceFiles,
            config,
            moduleStructure.jsFrontEndResult.jsAnalysisResult,
            sortDependencies(moduleStructure.moduleDependencies),
            icData,
            expectDescriptorToSymbol,
            IrFactoryImpl,
            verifySignatures = true
        ) {
            moduleStructure.getModuleDescriptor(it)
        }

        konst metadataSerializer =
            KlibMetadataIncrementalSerializer(config, moduleStructure.project, moduleStructure.jsFrontEndResult.hasErrors)

        generateKLib(
            moduleStructure,
            klibFile.path,
            nopack = false,
            jsOutputName = moduleName,
            icData = icData,
            expectDescriptorToSymbol = expectDescriptorToSymbol,
            moduleFragment = moduleFragment
        ) { file ->
            metadataSerializer.serializeScope(file, moduleStructure.jsFrontEndResult.bindingContext, moduleFragment.descriptor)
        }
    }

    private fun buildKlibWithK2(moduleName: String, moduleSourceDir: File, dependencies: Dependencies, klibFile: File) {
        konst config = createConfig(moduleName)
        konst ktFiles = environment.createPsiFiles(moduleSourceDir)

        konst regularDependencies = dependencies.regularDependencies.map { it.libraryFile.absolutePath }
        konst friendDependencies = dependencies.friendDependencies.map { it.libraryFile.absolutePath }

        konst diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()

        konst moduleStructure = ModulesStructure(
            project = environment.project,
            mainModule = MainModule.SourceFiles(ktFiles),
            compilerConfiguration = config,
            dependencies = regularDependencies,
            friendDependenciesPaths = friendDependencies
        )

        konst outputStream = ByteArrayOutputStream()
        konst messageCollector = PrintingMessageCollector(PrintStream(outputStream), MessageRenderer.PLAIN_FULL_PATHS, true)

        konst analyzedOutput = compileModuleToAnalyzedFirWithPsi(
            moduleStructure = moduleStructure,
            ktFiles = ktFiles,
            libraries = regularDependencies,
            friendLibraries = friendDependencies,
            diagnosticsReporter = diagnosticsReporter,
            incrementalDataProvider = null,
            lookupTracker = null
        )

        konst fir2IrActualizedResult = transformFirToIr(moduleStructure, analyzedOutput.output, diagnosticsReporter)

        if (analyzedOutput.reportCompilationErrors(moduleStructure, diagnosticsReporter, messageCollector)) {
            konst messages = outputStream.toByteArray().toString(Charset.forName("UTF-8"))
            throw AssertionError("The following errors occurred compiling test:\n$messages")
        }

        serializeFirKlib(
            moduleStructure = moduleStructure,
            firOutputs = analyzedOutput.output,
            fir2IrActualizedResult = fir2IrActualizedResult,
            outputKlibPath = klibFile.absolutePath,
            messageCollector = messageCollector,
            diagnosticsReporter = diagnosticsReporter,
            jsOutputName = moduleName
        )

        if (messageCollector.hasErrors()) {
            konst messages = outputStream.toByteArray().toString(Charset.forName("UTF-8"))
            throw AssertionError("The following errors occurred serializing test klib:\n$messages")
        }
    }

    private fun buildBinaryAndRun(mainModuleKlibFile: File, allDependencies: Dependencies) {
        konst configuration = createConfig(MAIN_MODULE_NAME)
        configuration.setupPartialLinkageConfig(PartialLinkageConfig(PartialLinkageMode.ENABLE, PartialLinkageLogLevel.WARNING))

        konst compilationOutputs = when (compilerType) {
            CompilerType.K1_NO_IC, CompilerType.K1_NO_IC_WITH_ES6, CompilerType.K2_NO_IC ->
                buildBinaryNoIC(configuration, mainModuleKlibFile, allDependencies, compilerType.es6Mode)
            CompilerType.K1_WITH_IC -> buildBinaryWithIC(configuration, mainModuleKlibFile, allDependencies)
        }

        konst binariesDir = File(buildDir, BIN_DIR_NAME).also { it.mkdirs() }

        // key = module name, konstue = list of produced JS files
        konst producedBinaries: Map<String, List<File>> = compilationOutputs
            .writeAll(binariesDir, MAIN_MODULE_NAME, false, MAIN_MODULE_NAME, ModuleKind.PLAIN)
            .filter { file -> file.extension == "js" }
            .groupByTo(linkedMapOf()) { file -> file.nameWithoutExtension.toInnerName() }

        // key = module name, konstue = list of JS files out of test data
        konst providedBinaries: Map<String, List<File>> =
            (allDependencies.regularDependencies.asSequence().map { it.libraryFile } + mainModuleKlibFile)
                .mapNotNull { klibFile ->
                    konst moduleName = if (klibFile.extension == "klib") klibFile.nameWithoutExtension else return@mapNotNull null
                    konst outputDir = klibFile.parentFile

                    konst providedJsFiles = outputDir.listFiles()?.filter { it.isFile && it.extension == "js" }.orEmpty()
                    if (providedJsFiles.isEmpty()) return@mapNotNull null

                    moduleName to providedJsFiles
                }.toMap()

        konst unexpectedModuleNames = providedBinaries.keys - producedBinaries.keys
        check(unexpectedModuleNames.isEmpty()) { "Unexpected module names: $unexpectedModuleNames" }

        konst allBinaries: List<File> = buildList {
            producedBinaries.forEach { (moduleName, producedJsFiles) ->
                this += providedBinaries[moduleName].orEmpty()
                this += producedJsFiles
            }
        }

        executeAndCheckBinaries(MAIN_MODULE_NAME, allBinaries)
    }

    private fun buildBinaryNoIC(
        configuration: CompilerConfiguration,
        mainModuleKlibFile: File,
        allDependencies: Dependencies,
        es6mode: Boolean
    ): CompilationOutputs {
        konst klib = MainModule.Klib(mainModuleKlibFile.path)
        konst moduleStructure = ModulesStructure(
            environment.project,
            klib,
            configuration,
            allDependencies.regularDependencies.map { it.libraryFile.path },
            allDependencies.friendDependencies.map { it.libraryFile.path }
        )

        konst ir = compile(
            moduleStructure,
            PhaseConfig(jsPhases),
            IrFactoryImplForJsIC(WholeWorldStageController()),
            exportedDeclarations = setOf(BOX_FUN_FQN),
            granularity = JsGenerationGranularity.PER_MODULE,
            es6mode = es6mode
        )

        konst transformer = IrModuleToJsTransformer(
            backendContext = ir.context,
            mainArguments = emptyList()
        )

        konst compiledResult = transformer.generateModule(
            modules = ir.allModules,
            modes = setOf(TranslationMode.PER_MODULE_DEV),
            relativeRequirePath = false
        )

        return compiledResult.outputs[TranslationMode.PER_MODULE_DEV] ?: error("No compiler output")
    }

    private fun buildBinaryWithIC(
        configuration: CompilerConfiguration,
        mainModuleKlibFile: File,
        allDependencies: Dependencies
    ): CompilationOutputs {
        // TODO: what about friend dependencies?
        konst cacheUpdater = CacheUpdater(
            mainModule = mainModuleKlibFile.absolutePath,
            allModules = allDependencies.regularDependencies.map { it.libraryFile.path },
            mainModuleFriends = emptyList(),
            cacheDir = buildDir.resolve("libs-cache").absolutePath,
            compilerConfiguration = configuration,
            irFactory = { IrFactoryImplForJsIC(WholeWorldStageController()) },
            mainArguments = null,
            compilerInterfaceFactory = { mainModule, cfg ->
                JsIrCompilerWithIC(mainModule, cfg, JsGenerationGranularity.PER_MODULE, PhaseConfig(jsPhases), setOf(BOX_FUN_FQN))
            }
        )
        konst icCaches = cacheUpdater.actualizeCaches()

        konst mainModuleName = icCaches.last().moduleExternalName
        konst jsExecutableProducer = JsExecutableProducer(
            mainModuleName = mainModuleName,
            moduleKind = configuration[JSConfigurationKeys.MODULE_KIND]!!,
            sourceMapsInfo = SourceMapsInfo.from(configuration),
            caches = icCaches,
            relativeRequirePath = true
        )

        return jsExecutableProducer.buildExecutable(multiModule = true, outJsProgram = true).compilationOut
    }

    private konst IrModuleFragment.exportName get() = "kotlin_${name.asStringStripSpecialMarkers()}"
    private fun String.toInnerName() = if (startsWith("kotlin_")) substringAfter("kotlin_") else this

    private fun KotlinCoreEnvironment.createPsiFiles(sourceDir: File): List<KtFile> {
        konst psiManager = PsiManager.getInstance(project)
        konst fileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL) as CoreLocalFileSystem

        return sourceDir.walkTopDown().filter { file ->
            file.isFile && file.extension == "kt"
        }.flatMap { file ->
            konst virtualFile = fileSystem.findFileByIoFile(file) ?: error("VirtualFile for $file not found")
            SingleRootFileViewProvider(psiManager, virtualFile).allFiles
        }.filterIsInstance<KtFile>().toList()
    }

    private fun File.binJsFile(name: String): File = File(this, "$name.js")

    private fun executeAndCheckBinaries(mainModuleName: String, dependencies: Collection<File>) {
        konst checker = V8IrJsTestChecker

        konst filePaths = dependencies.map { it.canonicalPath }
        checker.check(filePaths, mainModuleName, null, BOX_FUN_FQN.asString(), "OK", withModuleSystem = false)
    }

    companion object {
        private const konst BIN_DIR_NAME = "_bins_js"
        private konst BOX_FUN_FQN = FqName("box")
    }
}
