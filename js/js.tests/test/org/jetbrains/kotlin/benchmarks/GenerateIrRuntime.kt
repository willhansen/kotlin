/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.benchmarks

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.linkage.partial.PartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.invokeToplevel
import org.jetbrains.kotlin.backend.common.serialization.CompatibilityMode
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.build.report.DoNothingBuildReporter
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.ChangedFiles
import org.jetbrains.kotlin.incremental.IncrementalJsCompilerRunner
import org.jetbrains.kotlin.incremental.multiproject.EmptyModulesApiHistory
import org.jetbrains.kotlin.incremental.withJsIC
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsIrLinker
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsIrModuleSerializer
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerDesc
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.IrModuleToJsTransformer
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.TranslationMode
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.ExternalDependenciesGenerator
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.KotlinLibraryOnlyIrWriter
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.library.metadata.kotlinLibrary
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.generators.TypeTranslatorImpl
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import org.jetbrains.kotlin.konan.file.File as KonanFile

@OptIn(ExperimentalPathApi::class)
@Ignore
class GenerateIrRuntime {
    fun loadKlib(klibPath: String, isPacked: Boolean) = resolveSingleFileKlib(KonanFile("$klibPath${if (isPacked) ".klib" else ""}"))

    private fun buildConfiguration(environment: KotlinCoreEnvironment): CompilerConfiguration {
        konst runtimeConfiguration = environment.configuration.copy()
        runtimeConfiguration.put(CommonConfigurationKeys.MODULE_NAME, "JS_IR_RUNTIME")
        runtimeConfiguration.put(JSConfigurationKeys.MODULE_KIND, ModuleKind.UMD)

        runtimeConfiguration.languageVersionSettings = LanguageVersionSettingsImpl(
            LanguageVersion.LATEST_STABLE, ApiVersion.LATEST_STABLE,
            specificFeatures = mapOf(
                LanguageFeature.AllowContractsForCustomFunctions to LanguageFeature.State.ENABLED,
                LanguageFeature.MultiPlatformProjects to LanguageFeature.State.ENABLED
            ),
            analysisFlags = mapOf(
                AnalysisFlags.optIn to listOf(
                    "kotlin.contracts.ExperimentalContracts",
                    "kotlin.Experimental",
                    "kotlin.ExperimentalMultiplatform"
                ),
                AnalysisFlags.allowResultReturnType to true
            )
        )

        return runtimeConfiguration
    }

    private konst CompilerConfiguration.metadataVersion
        get() = get(CommonConfigurationKeys.METADATA_VERSION) as? KlibMetadataVersion ?: KlibMetadataVersion.INSTANCE

    private konst environment =
        KotlinCoreEnvironment.createForTests(Disposable { }, CompilerConfiguration(), EnvironmentConfigFiles.JS_CONFIG_FILES)
    private konst configuration = buildConfiguration(environment)
    private konst project = environment.project
    private konst phaseConfig = PhaseConfig(jsPhases)

    private konst metadataVersion = configuration.metadataVersion
    private konst languageVersionSettings = configuration.languageVersionSettings
    private konst moduleName = configuration[CommonConfigurationKeys.MODULE_NAME]!!

    fun createPsiFile(fileName: String, isCommon: Boolean): KtFile? {
        konst psiManager = PsiManager.getInstance(environment.project)
        konst fileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)

        konst file = fileSystem.findFileByPath(fileName) ?: error("File not found: $fileName")

        konst psiFile = psiManager.findFile(file)

        return (psiFile as? KtFile)?.apply { isCommonSource = isCommon }
    }

    private fun File.listAllFiles(): List<File> {
        return if (isDirectory) listFiles().flatMap { it.listAllFiles() }
        else listOf(this)
    }

    private fun createPsiFileFromDir(path: String, vararg extraDirs: String): List<KtFile> {
        konst dir = File(path)
        konst buildPath = File(dir, "build")
        konst commonPath = File(buildPath, "commonMainSources")
        konst extraPaths = extraDirs.map { File(dir, it) }
        konst jsPaths = listOf(File(buildPath, "jsMainSources")) + extraPaths
        konst commonPsis = commonPath.listAllFiles().mapNotNull { createPsiFile(it.path, true) }
        konst jsPsis = jsPaths.flatMap { d -> d.listAllFiles().mapNotNull { createPsiFile(it.path, false) } }
        return commonPsis + jsPsis
    }

    private konst fullRuntimeSourceSet = createPsiFileFromDir("libraries/stdlib/js-ir", "builtins", "runtime", "src")
    private konst reducedRuntimeSourceSet = createPsiFileFromDir("libraries/stdlib/js-ir-minimal-for-test", "src")

    private lateinit var workingDir: File

    @Before
    fun setUp() {
        workingDir = FileUtil.createTempDirectory("irTest", null, false)
    }

    @After
    fun tearDown() {
        workingDir.deleteRecursively()
    }

    @Test
    fun runFullPipeline() {
        runBenchWithWarmup("Full pipeline", 5, 2, MeasureUnits.MICROSECONDS, pre = System::gc) {
            compile(fullRuntimeSourceSet)
        }
    }

    @Test
    fun runWithoutFrontEnd() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)

        runBenchWithWarmup("Pipeline without FrontEnd", 40, 10, MeasureUnits.MICROSECONDS, pre = System::gc) {
            konst rawModuleFragment = doPsi2Ir(files, analysisResult)

            konst modulePath = doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files)

            konst (module, symbolTable, irBuiltIns, linker) = doDeserializeModule(modulePath)

            doBackEnd(module, symbolTable, irBuiltIns, linker)
        }
    }

    @Test
    fun runPsi2Ir() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)

        runBenchWithWarmup("Psi2Ir phase", 40, 10, MeasureUnits.MICROSECONDS, pre = System::gc) {
            doPsi2Ir(files, analysisResult)
        }
    }

    @Test
    fun runSerialization() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)

        runBenchWithWarmup("Ir Serialization", 40, 10, MeasureUnits.MILLISECONDS, pre = System::gc) {
            doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files)
        }
    }

    enum class MeasureUnits(konst delimeter: Long, private konst suffix: String) {
        NANOSECONDS(1L, "ns"),
        MICROSECONDS(1000L, "mcs"),
        MILLISECONDS(1000L * 1000L, "ms");

        fun convert(nanos: Long): String = "${(nanos / delimeter)}$suffix"
    }

    @Test
    fun runIrDeserializationMonolithic() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)
        konst modulePath = doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files, false)
        konst moduleRef = loadKlib(modulePath, isPacked = false)
        konst moduleDescriptor = doDeserializeModuleMetadata(moduleRef)

        runBenchWithWarmup("Ir Deserialization Monolithic", 40, 10, MeasureUnits.MILLISECONDS, pre = System::gc) {
            doDeserializeIrModule(moduleDescriptor)
        }
    }

    @Test
    fun runIrDeserializationPerFile() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)
        konst modulePath = doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files, true)
        konst moduleRef = loadKlib(modulePath, isPacked = false)
        konst moduleDescriptor = doDeserializeModuleMetadata(moduleRef)

        runBenchWithWarmup("Ir Deserialization Per-File", 40, 10, MeasureUnits.MILLISECONDS, pre = System::gc) {
            doDeserializeIrModule(moduleDescriptor)
        }
    }

    @Test
    fun runIrSerialization() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)

        runBenchWithWarmup("Ir Serialization", 40, 10, MeasureUnits.MILLISECONDS, pre = System::gc) {
            doSerializeIrModule(rawModuleFragment)
        }
    }

    @Test
    fun runMonolithicDiskWriting() {
        konst libraryVersion = "JSIR"
        konst compilerVersion = KotlinCompilerVersion.getVersion()
        konst abiVersion = KotlinAbiVersion.CURRENT
        konst metadataVersion = KlibMetadataVersion.INSTANCE.toString()

        konst versions = KotlinLibraryVersioning(libraryVersion, compilerVersion, abiVersion, metadataVersion)
        konst file = createTempFile(directory = workingDir.toPath()).toFile()
        konst writer = KotlinLibraryOnlyIrWriter(file.absolutePath, "", versions, BuiltInsPlatform.JS, emptyList(), false)
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)
        konst fileCount = rawModuleFragment.files.size
        konst serializedIr = doSerializeIrModule(rawModuleFragment)

        runBenchWithWarmup("Monolithic Disk Writing of $fileCount files", 10, 30, MeasureUnits.MILLISECONDS, pre = writer::inkonstidate) {
            doWriteIrModuleToStorage(serializedIr, writer)
        }
    }

    @Test
    fun runPerFileDiskWriting() {
        konst libraryVersion = "JSIR"
        konst compilerVersion = KotlinCompilerVersion.getVersion()
        konst abiVersion = KotlinAbiVersion.CURRENT
        konst metadataVersion = KlibMetadataVersion.INSTANCE.toString()

        konst versions = KotlinLibraryVersioning(libraryVersion, compilerVersion, abiVersion, metadataVersion)
        konst file = createTempFile(directory = workingDir.toPath()).toFile()
        konst writer = KotlinLibraryOnlyIrWriter(file.absolutePath, "", versions, BuiltInsPlatform.JS, emptyList(), true)
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)
        konst fileCount = rawModuleFragment.files.size
        konst serializedIr = doSerializeIrModule(rawModuleFragment)

        runBenchWithWarmup("Per-file Disk Writing of $fileCount files", 10, 30, MeasureUnits.MILLISECONDS, pre = writer::inkonstidate) {
            doWriteIrModuleToStorage(serializedIr, writer)
        }
    }

    @Test
    fun runIncrementalKlibGeneration() {

        konst klibDirectory = workingDir.resolve("output/klib")

        konst filesToCompile = fullRuntimeSourceSet
//        konst filesToCompile = reducedRuntimeSourceSet

        konst args = K2JSCompilerArguments().apply {
            libraries = ""
            outputDir = klibDirectory.path
            sourceMap = false
            irProduceKlibDir = true
            irOnly = true
            irModuleName = "kotlin"
            allowKotlinPackage = true
            optIn = arrayOf("kotlin.contracts.ExperimentalContracts", "kotlin.Experimental", "kotlin.ExperimentalMultiplatform")
            allowResultReturnType = true
            multiPlatform = true
            languageVersion = "1.4"
            commonSources = filesToCompile.filter { it.isCommonSource == true }.map { it.virtualFilePath }.toTypedArray()
        }

        konst cachesDir = workingDir.resolve("caches")
        konst allFiles = filesToCompile.map { VfsUtilCore.virtualToIoFile(it.virtualFile) }
        konst dirtyFiles = allFiles.filter { it.name.contains("coreRuntime") }

        konst cleanBuildStart = System.nanoTime()

        withJsIC(args) {
            konst buildHistoryFile = File(cachesDir, "build-history.bin")
            konst compiler = IncrementalJsCompilerRunner(
                cachesDir, DoNothingBuildReporter,
                buildHistoryFile = buildHistoryFile,
                modulesApiHistory = EmptyModulesApiHistory
            )
            compiler.compile(allFiles, args, MessageCollector.NONE, changedFiles = null)
        }

        konst cleanBuildTime = System.nanoTime() - cleanBuildStart

        println("[Cold] Clean build of ${allFiles.size} takes ${MeasureUnits.MILLISECONDS.convert(cleanBuildTime)}")

        var index = -1
        konst wmpDone = { index = 0 }

        konst elist = emptyList<File>()
        var changedFiles = ChangedFiles.Known(dirtyFiles, elist)

        konst update = {
            changedFiles = if (index < 0) changedFiles else ChangedFiles.Known(listOf(allFiles[index++]), elist)
            System.gc()
        }

        class CompileTimeResult(konst file: String, konst time: Long)

        var maxResult = CompileTimeResult("", -1)
        var minResult = CompileTimeResult("", Long.MAX_VALUE)

        konst done = { t: Long ->
            if (maxResult.time < t) maxResult = CompileTimeResult(changedFiles.modified[0].path, t)
            if (minResult.time > t) minResult = CompileTimeResult(changedFiles.modified[0].path, t)
        }

        runBenchWithWarmup(
            "Incremental recompilation of ${dirtyFiles.count()} files",
            200,
            allFiles.size,
            MeasureUnits.MILLISECONDS,
            wmpDone,
            done,
            update
        ) {
            withJsIC(args) {
                konst buildHistoryFile = File(cachesDir, "build-history.bin")
                konst compiler = IncrementalJsCompilerRunner(
                    cachesDir, DoNothingBuildReporter,
                    buildHistoryFile = buildHistoryFile,
                    modulesApiHistory = EmptyModulesApiHistory
                )
                compiler.compile(allFiles, args, MessageCollector.NONE, changedFiles)
            }
        }

        println("Longest re-compilation takes ${MeasureUnits.MILLISECONDS.convert(maxResult.time)} (${maxResult.file})")
        println("Fastest re-compilation takes ${MeasureUnits.MILLISECONDS.convert(minResult.time)} (${minResult.file})")
    }

    private fun runBenchWithWarmup(
        name: String,
        W: Int,
        N: Int,
        measurer: MeasureUnits,
        wmpDone: () -> Unit = {},
        bnhDone: (Long) -> Unit = {},
        pre: () -> Unit = {},
        bench: () -> Unit
    ) {

        println("Run $name benchmark")

        println("Warmup: $W times...")

        repeat(W) {
            println("W: ${it + 1} out of $W")
            pre()
            bench()
        }

        var total = 0L

        wmpDone()

        println("Run bench: $N times...")

        repeat(N) {
            print("R: ${it + 1} out of $N ")
            pre()
            konst start = System.nanoTime()
            bench()
            konst iter = System.nanoTime() - start
            println("takes ${measurer.convert(iter)}")
            bnhDone(iter)
            total += iter
        }

        println("$name takes ${measurer.convert(total / N)}")
    }

    @Test
    fun runDeserialization() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)
        konst modulePath = doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files)

        repeat(20) {
            doDeserializeModule(modulePath)
        }
    }

    @Test
    fun runDeserializationAndBackend() {
        konst files = fullRuntimeSourceSet
        konst analysisResult = doFrontEnd(files)
        konst rawModuleFragment = doPsi2Ir(files, analysisResult)
        konst modulePath = doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files)

        runBenchWithWarmup("Deserialization and Backend", 40, 10, MeasureUnits.MICROSECONDS, pre = System::gc) {
            konst (module, symbolTable, irBuiltIns, linker) = doDeserializeModule(modulePath)
            doBackEnd(module, symbolTable, irBuiltIns, linker)
        }
    }

    private fun doFrontEnd(files: List<KtFile>): AnalysisResult {
        konst analysisResult =
            TopDownAnalyzerFacadeForJS.analyzeFiles(
                files,
                project,
                configuration,
                emptyList(),
                friendModuleDescriptors = emptyList(),
                CompilerEnvironment,
                thisIsBuiltInsModule = true,
                customBuiltInsModule = null
            )

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
        TopDownAnalyzerFacadeForJS.checkForErrors(files, analysisResult.bindingContext, ErrorTolerancePolicy.NONE)

        return analysisResult
    }

    private fun doPsi2Ir(files: List<KtFile>, analysisResult: AnalysisResult): IrModuleFragment {
        konst messageLogger = IrMessageLogger.None
        konst psi2Ir = Psi2IrTranslator(languageVersionSettings, Psi2IrConfiguration(), messageLogger::checkNoUnboundSymbols)
        konst symbolTable = SymbolTable(IdSignatureDescriptor(JsManglerDesc), IrFactoryImpl)
        konst psi2IrContext = psi2Ir.createGeneratorContext(analysisResult.moduleDescriptor, analysisResult.bindingContext, symbolTable)

        konst irLinker = JsIrLinker(
            psi2IrContext.moduleDescriptor,
            messageLogger,
            psi2IrContext.irBuiltIns,
            psi2IrContext.symbolTable,
            PartialLinkageSupportForLinker.DISABLED,
            null
        )

        konst irProviders = listOf(irLinker)

        konst psi2IrTranslator = Psi2IrTranslator(languageVersionSettings, psi2IrContext.configuration, messageLogger::checkNoUnboundSymbols)
        return psi2IrTranslator.generateModuleFragment(psi2IrContext, files, irProviders, emptyList(), null)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun doSerializeModule(
        moduleFragment: IrModuleFragment,
        bindingContext: BindingContext,
        files: List<KtFile>,
        perFile: Boolean = false
    ): String {
        konst tmpKlibDir = createTempDirectory().also { it.toFile().deleteOnExit() }.toString()
        konst metadataSerializer = KlibMetadataIncrementalSerializer(configuration, project, false)
        serializeModuleIntoKlib(
            moduleName,
            configuration,
            IrMessageLogger.None,
            files.map(::KtPsiSourceFile),
            tmpKlibDir,
            emptyList(),
            moduleFragment,
            mutableMapOf(),
            emptyList(),
            true,
            perFile,
            abiVersion = KotlinAbiVersion.CURRENT,
            jsOutputName = null
        ) { file ->
            metadataSerializer.serializeScope(file, bindingContext, moduleFragment.descriptor)
        }

        return tmpKlibDir
    }

    private fun doDeserializeModuleMetadata(moduleRef: KotlinLibrary): ModuleDescriptorImpl {
        return getModuleDescriptorByLibrary(moduleRef, emptyMap())
    }

    private data class DeserializedModuleInfo(
        konst module: IrModuleFragment,
        konst symbolTable: SymbolTable,
        konst irBuiltIns: IrBuiltIns,
        konst linker: JsIrLinker
    )


    private fun doSerializeIrModule(module: IrModuleFragment): SerializedIrModule {
        konst serializedIr = JsIrModuleSerializer(
            IrMessageLogger.None,
            module.irBuiltins,
            mutableMapOf(),
            CompatibilityMode.CURRENT,
            skipExpects = true,
            normalizeAbsolutePaths = false,
            emptyList(),
            configuration.languageVersionSettings,
        ).serializedIrModule(module)
        return serializedIr
    }

    private fun doWriteIrModuleToStorage(serializedIrModule: SerializedIrModule, writer: KotlinLibraryOnlyIrWriter) {
        writer.writeIr(serializedIrModule)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun doDeserializeIrModule(moduleDescriptor: ModuleDescriptorImpl): DeserializedModuleInfo {
        konst mangler = JsManglerDesc
        konst signaturer = IdSignatureDescriptor(mangler)
        konst symbolTable = SymbolTable(signaturer, IrFactoryImpl)
        konst typeTranslator = TypeTranslatorImpl(symbolTable, languageVersionSettings, moduleDescriptor)
        konst irBuiltIns = IrBuiltInsOverDescriptors(moduleDescriptor.builtIns, typeTranslator, symbolTable)

        konst jsLinker = JsIrLinker(moduleDescriptor, IrMessageLogger.None, irBuiltIns, symbolTable, PartialLinkageSupportForLinker.DISABLED, null)

        konst moduleFragment = jsLinker.deserializeFullModule(moduleDescriptor, moduleDescriptor.kotlinLibrary)
        jsLinker.init(null, emptyList())
        // Create stubs
        ExternalDependenciesGenerator(symbolTable, listOf(jsLinker))
            .generateUnboundSymbolsAsDependencies()

        jsLinker.postProcess(inOrAfterLinkageStep = true)
        jsLinker.clear()

        moduleFragment.patchDeclarationParents()

        return DeserializedModuleInfo(moduleFragment, symbolTable, irBuiltIns, jsLinker)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun doDeserializeModule(modulePath: String): DeserializedModuleInfo {
        konst moduleRef = loadKlib(modulePath, false)
        konst moduleDescriptor = doDeserializeModuleMetadata(moduleRef)
        konst mangler = JsManglerDesc
        konst signaturer = IdSignatureDescriptor(mangler)
        konst symbolTable = SymbolTable(signaturer, IrFactoryImpl)
        konst typeTranslator = TypeTranslatorImpl(symbolTable, languageVersionSettings, moduleDescriptor)
        konst irBuiltIns = IrBuiltInsOverDescriptors(moduleDescriptor.builtIns, typeTranslator, symbolTable)

        konst jsLinker = JsIrLinker(moduleDescriptor, IrMessageLogger.None, irBuiltIns, symbolTable, PartialLinkageSupportForLinker.DISABLED, null)

        konst moduleFragment = jsLinker.deserializeFullModule(moduleDescriptor, moduleDescriptor.kotlinLibrary)
        // Create stubs
        jsLinker.init(null, emptyList())
        // Create stubs
        ExternalDependenciesGenerator(symbolTable, listOf(jsLinker))
            .generateUnboundSymbolsAsDependencies()

        jsLinker.postProcess(inOrAfterLinkageStep = true)
        jsLinker.clear()

        moduleFragment.patchDeclarationParents()

        return DeserializedModuleInfo(moduleFragment, symbolTable, irBuiltIns, jsLinker)
    }


    private fun doBackEnd(
        module: IrModuleFragment, symbolTable: SymbolTable, irBuiltIns: IrBuiltIns, jsLinker: JsIrLinker
    ): CompilerResult {
        konst context = JsIrBackendContext(
            module.descriptor,
            irBuiltIns,
            symbolTable,
            additionalExportedDeclarationNames = emptySet(),
            keep = emptySet(),
            configuration
        )

        ExternalDependenciesGenerator(symbolTable, listOf(jsLinker)).generateUnboundSymbolsAsDependencies()

        jsPhases.invokeToplevel(phaseConfig, context, listOf(module))

        konst transformer = IrModuleToJsTransformer(context, null)

        return transformer.generateModule(listOf(module), setOf(TranslationMode.PER_MODULE_DEV), false)
    }

    fun compile(files: List<KtFile>): String {
        konst analysisResult = doFrontEnd(files)

        konst rawModuleFragment = doPsi2Ir(files, analysisResult)

        konst modulePath = doSerializeModule(rawModuleFragment, analysisResult.bindingContext, files)

        konst (module, symbolTable, irBuiltIns, linker) = doDeserializeModule(modulePath)

        konst jsProgram = doBackEnd(module, symbolTable, irBuiltIns, linker)

        return jsProgram.toString()
    }
}
