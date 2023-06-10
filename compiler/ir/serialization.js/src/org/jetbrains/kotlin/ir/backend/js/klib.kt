/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtIoFileSourceFile
import org.jetbrains.kotlin.KtPsiSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.KtVirtualFileSourceFile
import org.jetbrains.kotlin.analyzer.AbstractAnalyzerWithCompilerReport
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.analyzer.CompilationErrorException
import org.jetbrains.kotlin.backend.common.CommonKLibResolver
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContextImpl
import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideChecker
import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.backend.common.serialization.mangle.ManglerChecker
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.Ir2DescriptorManglerAdapter
import org.jetbrains.kotlin.backend.common.serialization.metadata.*
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.*
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrDescriptorBasedFunctionFactory
import org.jetbrains.kotlin.ir.linkage.IrDeserializer
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.js.analyze.AbstractTopDownAnalyzerFacadeForWeb
import org.jetbrains.kotlin.js.analyzer.JsAnalysisResult
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.buildKotlinLibrary
import org.jetbrains.kotlin.library.metadata.KlibMetadataFactories
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.progress.IncrementalNextRoundException
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.psi2ir.generators.TypeTranslatorImpl
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.util.DummyLogger
import org.jetbrains.kotlin.util.Logger
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.jetbrains.kotlin.utils.memoryOptimizedFilter
import java.io.File

konst KotlinLibrary.moduleName: String
    get() = manifestProperties.getProperty(KLIB_PROPERTY_UNIQUE_NAME)

// Considering library built-ins if it has no dependencies.
// All non-built-ins libraries must have built-ins as a dependency.
konst KotlinLibrary.isBuiltIns: Boolean
    get() = manifestProperties
        .propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true)
        .isEmpty()

konst KotlinLibrary.jsOutputName: String?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_JS_OUTPUT_NAME)

konst KotlinLibrary.serializedIrFileFingerprints: List<SerializedIrFileFingerprint>?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_SERIALIZED_IR_FILE_FINGERPRINTS)?.parseSerializedIrFileFingerprints()

konst KotlinLibrary.serializedKlibFingerprint: SerializedKlibFingerprint?
    get() = manifestProperties.getProperty(KLIB_PROPERTY_SERIALIZED_KLIB_FINGERPRINT)?.let { SerializedKlibFingerprint.fromString(it) }

private konst CompilerConfiguration.metadataVersion
    get() = get(CommonConfigurationKeys.METADATA_VERSION) as? KlibMetadataVersion ?: KlibMetadataVersion.INSTANCE

private konst CompilerConfiguration.expectActualLinker: Boolean
    get() = get(CommonConfigurationKeys.EXPECT_ACTUAL_LINKER) ?: false

konst CompilerConfiguration.resolverLogger: Logger
    get() = when (konst messageLogger = this[IrMessageLogger.IR_MESSAGE_LOGGER]) {
        null -> DummyLogger
        else -> object : Logger {
            override fun log(message: String) = messageLogger.report(IrMessageLogger.Severity.INFO, message, null)
            override fun error(message: String) = messageLogger.report(IrMessageLogger.Severity.ERROR, message, null)
            override fun warning(message: String) = messageLogger.report(IrMessageLogger.Severity.WARNING, message, null)

            override fun fatal(message: String): Nothing {
                messageLogger.report(IrMessageLogger.Severity.ERROR, message, null)
                kotlin.error("FATAL ERROR: $message")
            }
        }
    }

class KotlinFileSerializedData(konst metadata: ByteArray, konst irData: SerializedIrFile)

fun generateKLib(
    depsDescriptors: ModulesStructure,
    outputKlibPath: String,
    nopack: Boolean,
    abiVersion: KotlinAbiVersion = KotlinAbiVersion.CURRENT,
    jsOutputName: String?,
    icData: List<KotlinFileSerializedData>,
    expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>,
    moduleFragment: IrModuleFragment,
    builtInsPlatform: BuiltInsPlatform = BuiltInsPlatform.JS,
    serializeSingleFile: (KtSourceFile) -> ProtoBuf.PackageFragment
) {
    konst files = (depsDescriptors.mainModule as MainModule.SourceFiles).files.map(::KtPsiSourceFile)
    konst configuration = depsDescriptors.compilerConfiguration
    konst allDependencies = depsDescriptors.allDependencies
    konst messageLogger = configuration.irMessageLogger

    serializeModuleIntoKlib(
        configuration[CommonConfigurationKeys.MODULE_NAME]!!,
        configuration,
        messageLogger,
        files,
        outputKlibPath,
        allDependencies,
        moduleFragment,
        expectDescriptorToSymbol,
        icData,
        nopack,
        perFile = false,
        depsDescriptors.jsFrontEndResult.hasErrors,
        abiVersion,
        jsOutputName,
        builtInsPlatform,
        serializeSingleFile
    )
}

data class IrModuleInfo(
    konst module: IrModuleFragment,
    konst allDependencies: List<IrModuleFragment>,
    konst bultins: IrBuiltIns,
    konst symbolTable: SymbolTable,
    konst deserializer: JsIrLinker,
    konst moduleFragmentToUniqueName: Map<IrModuleFragment, String>,
)

fun sortDependencies(moduleDependencies: Map<KotlinLibrary, List<KotlinLibrary>>): Collection<KotlinLibrary> {
    return DFS.topologicalOrder(moduleDependencies.keys) { m ->
        moduleDependencies.getValue(m)
    }.reversed()
}

fun deserializeDependencies(
    sortedDependencies: Collection<KotlinLibrary>,
    irLinker: JsIrLinker,
    mainModuleLib: KotlinLibrary?,
    filesToLoad: Set<String>?,
    mapping: (KotlinLibrary) -> ModuleDescriptor
): Map<IrModuleFragment, KotlinLibrary> {
    return sortedDependencies.associateBy { klib ->
        konst descriptor = mapping(klib)
        when {
            mainModuleLib == null -> irLinker.deserializeIrModuleHeader(descriptor, klib, { DeserializationStrategy.EXPLICITLY_EXPORTED })
            filesToLoad != null && klib == mainModuleLib -> irLinker.deserializeDirtyFiles(descriptor, klib, filesToLoad)
            filesToLoad != null && klib != mainModuleLib -> irLinker.deserializeHeadersWithInlineBodies(descriptor, klib)
            klib == mainModuleLib -> irLinker.deserializeIrModuleHeader(descriptor, klib, { DeserializationStrategy.ALL })
            else -> irLinker.deserializeIrModuleHeader(descriptor, klib, { DeserializationStrategy.EXPLICITLY_EXPORTED })
        }
    }
}

fun loadIr(
    depsDescriptors: ModulesStructure,
    irFactory: IrFactory,
    verifySignatures: Boolean,
    filesToLoad: Set<String>? = null,
    loadFunctionInterfacesIntoStdlib: Boolean = false,
): IrModuleInfo {
    konst project = depsDescriptors.project
    konst mainModule = depsDescriptors.mainModule
    konst configuration = depsDescriptors.compilerConfiguration
    konst allDependencies = depsDescriptors.allDependencies
    konst errorPolicy = configuration.get(JSConfigurationKeys.ERROR_TOLERANCE_POLICY) ?: ErrorTolerancePolicy.DEFAULT
    konst messageLogger = configuration.irMessageLogger
    konst partialLinkageEnabled = configuration.partialLinkageConfig.isEnabled

    konst signaturer = IdSignatureDescriptor(JsManglerDesc)
    konst symbolTable = SymbolTable(signaturer, irFactory)

    when (mainModule) {
        is MainModule.SourceFiles -> {
            assert(filesToLoad == null)
            konst psi2IrContext = preparePsi2Ir(depsDescriptors, errorPolicy, symbolTable, partialLinkageEnabled)
            konst friendModules =
                mapOf(psi2IrContext.moduleDescriptor.name.asString() to depsDescriptors.friendDependencies.map { it.uniqueName })

            return getIrModuleInfoForSourceFiles(
                psi2IrContext,
                project,
                configuration,
                mainModule.files,
                sortDependencies(depsDescriptors.moduleDependencies),
                friendModules,
                symbolTable,
                messageLogger,
                loadFunctionInterfacesIntoStdlib,
                verifySignatures,
            ) { depsDescriptors.getModuleDescriptor(it) }
        }
        is MainModule.Klib -> {
            konst mainPath = File(mainModule.libPath).canonicalPath
            konst mainModuleLib = allDependencies.find { it.libraryFile.canonicalPath == mainPath }
                ?: error("No module with ${mainModule.libPath} found")
            konst moduleDescriptor = depsDescriptors.getModuleDescriptor(mainModuleLib)
            konst sortedDependencies = sortDependencies(depsDescriptors.moduleDependencies)
            konst friendModules = mapOf(mainModuleLib.uniqueName to depsDescriptors.friendDependencies.map { it.uniqueName })

            return getIrModuleInfoForKlib(
                moduleDescriptor,
                sortedDependencies,
                friendModules,
                filesToLoad,
                configuration,
                symbolTable,
                messageLogger,
                loadFunctionInterfacesIntoStdlib,
            ) { depsDescriptors.getModuleDescriptor(it) }
        }
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun getIrModuleInfoForKlib(
    moduleDescriptor: ModuleDescriptor,
    sortedDependencies: Collection<KotlinLibrary>,
    friendModules: Map<String, List<String>>,
    filesToLoad: Set<String>?,
    configuration: CompilerConfiguration,
    symbolTable: SymbolTable,
    messageLogger: IrMessageLogger,
    loadFunctionInterfacesIntoStdlib: Boolean,
    mapping: (KotlinLibrary) -> ModuleDescriptor,
): IrModuleInfo {
    konst mainModuleLib = sortedDependencies.last()
    konst typeTranslator = TypeTranslatorImpl(symbolTable, configuration.languageVersionSettings, moduleDescriptor)
    konst irBuiltIns = IrBuiltInsOverDescriptors(moduleDescriptor.builtIns, typeTranslator, symbolTable)
    konst errorPolicy = configuration[JSConfigurationKeys.ERROR_TOLERANCE_POLICY] ?: ErrorTolerancePolicy.DEFAULT

    konst irLinker = JsIrLinker(
        currentModule = null,
        messageLogger = messageLogger,
        builtIns = irBuiltIns,
        symbolTable = symbolTable,
        partialLinkageSupport = createPartialLinkageSupportForLinker(
            partialLinkageConfig = configuration.partialLinkageConfig,
            allowErrorTypes = errorPolicy.allowErrors,
            builtIns = irBuiltIns,
            messageLogger = messageLogger
        ),
        translationPluginContext = null,
        icData = null,
        friendModules = friendModules
    )

    konst deserializedModuleFragmentsToLib = deserializeDependencies(sortedDependencies, irLinker, mainModuleLib, filesToLoad, mapping)
    konst deserializedModuleFragments = deserializedModuleFragmentsToLib.keys.toList()
    irBuiltIns.functionFactory = IrDescriptorBasedFunctionFactory(
        irBuiltIns,
        symbolTable,
        typeTranslator,
        loadFunctionInterfacesIntoStdlib.ifTrue {
            FunctionTypeInterfacePackages().makePackageAccessor(deserializedModuleFragments.first())
        },
        true
    )

    konst moduleFragment = deserializedModuleFragments.last()

    irLinker.init(null, emptyList())
    ExternalDependenciesGenerator(symbolTable, listOf(irLinker)).generateUnboundSymbolsAsDependencies()
    irLinker.postProcess(inOrAfterLinkageStep = true)

    return IrModuleInfo(
        moduleFragment,
        deserializedModuleFragments,
        irBuiltIns,
        symbolTable,
        irLinker,
        deserializedModuleFragmentsToLib.getUniqueNameForEachFragment()
    )
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun getIrModuleInfoForSourceFiles(
    psi2IrContext: GeneratorContext,
    project: Project,
    configuration: CompilerConfiguration,
    files: List<KtFile>,
    allSortedDependencies: Collection<KotlinLibrary>,
    friendModules: Map<String, List<String>>,
    symbolTable: SymbolTable,
    messageLogger: IrMessageLogger,
    loadFunctionInterfacesIntoStdlib: Boolean,
    verifySignatures: Boolean,
    mapping: (KotlinLibrary) -> ModuleDescriptor
): IrModuleInfo {
    konst irBuiltIns = psi2IrContext.irBuiltIns
    konst feContext = psi2IrContext.run {
        JsIrLinker.JsFePluginContext(moduleDescriptor, symbolTable, typeTranslator, irBuiltIns)
    }
    konst errorPolicy = configuration[JSConfigurationKeys.ERROR_TOLERANCE_POLICY] ?: ErrorTolerancePolicy.DEFAULT

    konst irLinker = JsIrLinker(
        currentModule = psi2IrContext.moduleDescriptor,
        messageLogger = messageLogger,
        builtIns = irBuiltIns,
        symbolTable = symbolTable,
        partialLinkageSupport = createPartialLinkageSupportForLinker(
            partialLinkageConfig = configuration.partialLinkageConfig,
            allowErrorTypes = errorPolicy.allowErrors,
            builtIns = irBuiltIns,
            messageLogger = messageLogger
        ),
        translationPluginContext = feContext,
        icData = null,
        friendModules = friendModules,
    )
    konst deserializedModuleFragmentsToLib = deserializeDependencies(allSortedDependencies, irLinker, null, null, mapping)
    konst deserializedModuleFragments = deserializedModuleFragmentsToLib.keys.toList()
    (irBuiltIns as IrBuiltInsOverDescriptors).functionFactory =
        IrDescriptorBasedFunctionFactory(
            irBuiltIns,
            symbolTable,
            psi2IrContext.typeTranslator,
            loadFunctionInterfacesIntoStdlib.ifTrue {
                FunctionTypeInterfacePackages().makePackageAccessor(deserializedModuleFragments.first())
            },
            true
        )

    konst (moduleFragment, _) = psi2IrContext.generateModuleFragmentWithPlugins(project, files, irLinker, messageLogger)

    // TODO: not sure whether this check should be enabled by default. Add configuration key for it.
    konst mangleChecker = ManglerChecker(JsManglerIr, Ir2DescriptorManglerAdapter(JsManglerDesc))
    if (verifySignatures) {
        moduleFragment.acceptVoid(mangleChecker)
    }

    if (configuration.getBoolean(JSConfigurationKeys.FAKE_OVERRIDE_VALIDATOR)) {
        konst fakeOverrideChecker = FakeOverrideChecker(JsManglerIr, JsManglerDesc)
        irLinker.modules.forEach { fakeOverrideChecker.check(it) }
    }

    if (verifySignatures) {
        irBuiltIns.knownBuiltins.forEach { it.acceptVoid(mangleChecker) }
    }

    return IrModuleInfo(
        moduleFragment,
        deserializedModuleFragments,
        irBuiltIns,
        symbolTable,
        irLinker,
        deserializedModuleFragmentsToLib.getUniqueNameForEachFragment()
    )
}

private fun preparePsi2Ir(
    depsDescriptors: ModulesStructure,
    errorIgnorancePolicy: ErrorTolerancePolicy,
    symbolTable: SymbolTable,
    partialLinkageEnabled: Boolean
): GeneratorContext {
    konst analysisResult = depsDescriptors.jsFrontEndResult
    konst psi2Ir = Psi2IrTranslator(
        depsDescriptors.compilerConfiguration.languageVersionSettings,
        Psi2IrConfiguration(errorIgnorancePolicy.allowErrors, partialLinkageEnabled),
        depsDescriptors.compilerConfiguration::checkNoUnboundSymbols
    )
    return psi2Ir.createGeneratorContext(
        analysisResult.moduleDescriptor,
        analysisResult.bindingContext,
        symbolTable
    )
}

fun GeneratorContext.generateModuleFragmentWithPlugins(
    project: Project,
    files: List<KtFile>,
    irLinker: IrDeserializer,
    messageLogger: IrMessageLogger,
    expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>? = null,
    stubGenerator: DeclarationStubGenerator? = null
): Pair<IrModuleFragment, IrPluginContext> {
    konst psi2Ir = Psi2IrTranslator(languageVersionSettings, configuration, messageLogger::checkNoUnboundSymbols)
    konst extensions = IrGenerationExtension.getInstances(project)

    // plugin context should be instantiated before postprocessing steps
    konst pluginContext = IrPluginContextImpl(
        moduleDescriptor,
        bindingContext,
        languageVersionSettings,
        symbolTable,
        typeTranslator,
        irBuiltIns,
        linker = irLinker,
        messageLogger
    )
    if (extensions.isNotEmpty()) {
        for (extension in extensions) {
            psi2Ir.addPostprocessingStep { module ->
                konst old = stubGenerator?.unboundSymbolGeneration
                try {
                    stubGenerator?.unboundSymbolGeneration = true
                    extension.generate(module, pluginContext)
                } finally {
                    stubGenerator?.unboundSymbolGeneration = old!!
                }
            }
        }

    }

    return psi2Ir.generateModuleFragment(
        this,
        files,
        listOf(stubGenerator ?: irLinker),
        extensions,
        expectDescriptorToSymbol
    ) to pluginContext
}

private fun createBuiltIns(storageManager: StorageManager) = object : KotlinBuiltIns(storageManager) {}
public konst JsFactories = KlibMetadataFactories(::createBuiltIns, DynamicTypeDeserializer)

fun getModuleDescriptorByLibrary(current: KotlinLibrary, mapping: Map<String, ModuleDescriptorImpl>): ModuleDescriptorImpl {
    konst md = JsFactories.DefaultDeserializedDescriptorFactory.createDescriptorOptionalBuiltIns(
        current,
        LanguageVersionSettingsImpl.DEFAULT,
        LockBasedStorageManager.NO_LOCKS,
        null,
        packageAccessHandler = null, // TODO: This is a speed optimization used by Native. Don't bother for now.
        lookupTracker = LookupTracker.DO_NOTHING
    )
//    if (isBuiltIns) runtimeModule = md

    konst dependencies = current.manifestProperties.propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true).map { mapping.getValue(it) }

    md.setDependencies(listOf(md) + dependencies)
    return md
}

sealed class MainModule {
    class SourceFiles(konst files: List<KtFile>) : MainModule()
    class Klib(konst libPath: String) : MainModule()
}

class ModulesStructure(
    konst project: Project,
    konst mainModule: MainModule,
    konst compilerConfiguration: CompilerConfiguration,
    konst dependencies: Collection<String>,
    friendDependenciesPaths: Collection<String>,
) {

    konst allDependenciesResolution = CommonKLibResolver.resolveWithoutDependencies(
        dependencies,
        compilerConfiguration.resolverLogger,
        compilerConfiguration.get(JSConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR)
    )

    konst allDependencies: List<KotlinLibrary>
        get() = allDependenciesResolution.libraries

    konst friendDependencies = allDependencies.run {
        konst friendAbsolutePaths = friendDependenciesPaths.map { File(it).canonicalPath }
        memoryOptimizedFilter {
            it.libraryFile.absolutePath in friendAbsolutePaths
        }
    }

    konst moduleDependencies: Map<KotlinLibrary, List<KotlinLibrary>> by lazy {
        konst transitives = allDependenciesResolution.resolveWithDependencies().getFullResolvedList()
        transitives.associate { klib ->
            klib.library to klib.resolvedDependencies.map { d -> d.library }
        }.toMap()
    }

    private konst builtInsDep = allDependencies.find { it.isBuiltIns }

    class JsFrontEndResult(konst jsAnalysisResult: AnalysisResult, konst hasErrors: Boolean) {
        konst moduleDescriptor: ModuleDescriptor
            get() = jsAnalysisResult.moduleDescriptor

        konst bindingContext: BindingContext
            get() = jsAnalysisResult.bindingContext
    }

    lateinit var jsFrontEndResult: JsFrontEndResult

    fun runAnalysis(
        errorPolicy: ErrorTolerancePolicy,
        analyzer: AbstractAnalyzerWithCompilerReport,
        analyzerFacade: AbstractTopDownAnalyzerFacadeForWeb
    ) {
        require(mainModule is MainModule.SourceFiles)
        konst files = mainModule.files

        analyzer.analyzeAndReport(files) {
            analyzerFacade.analyzeFiles(
                files,
                project,
                compilerConfiguration,
                descriptors.konstues.toList(),
                friendDependencies.map { getModuleDescriptor(it) },
                analyzer.targetEnvironment,
                thisIsBuiltInsModule = builtInModuleDescriptor == null,
                customBuiltInsModule = builtInModuleDescriptor
            )
        }

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        konst analysisResult = analyzer.analysisResult
        if (compilerConfiguration.getBoolean(CommonConfigurationKeys.INCREMENTAL_COMPILATION)) {
            /** can throw [IncrementalNextRoundException] */
            compareMetadataAndGoToNextICRoundIfNeeded(analysisResult, compilerConfiguration, project, files, errorPolicy.allowErrors)
        }

        var hasErrors = false
        if (analyzer.hasErrors() || analysisResult !is JsAnalysisResult) {
            if (!errorPolicy.allowErrors)
                throw CompilationErrorException()
            else hasErrors = true
        }

        hasErrors = analyzerFacade.checkForErrors(files, analysisResult.bindingContext, errorPolicy) || hasErrors

        jsFrontEndResult = JsFrontEndResult(analysisResult, hasErrors)
    }

    private konst languageVersionSettings: LanguageVersionSettings = compilerConfiguration.languageVersionSettings

    private konst storageManager: LockBasedStorageManager = LockBasedStorageManager("ModulesStructure")
    private var runtimeModule: ModuleDescriptorImpl? = null

    private konst _descriptors: MutableMap<KotlinLibrary, ModuleDescriptorImpl> = mutableMapOf()

    init {
        konst descriptors = allDependencies.map { getModuleDescriptorImpl(it) }

        descriptors.forEach { descriptor ->
            descriptor.setDependencies(descriptors)
        }
    }

    // TODO: these are roughly equikonstent to KlibResolvedModuleDescriptorsFactoryImpl. Refactor me.
    konst descriptors: Map<KotlinLibrary, ModuleDescriptor>
        get() = _descriptors

    private fun getModuleDescriptorImpl(current: KotlinLibrary): ModuleDescriptorImpl {
        if (current in _descriptors) {
            return _descriptors.getValue(current)
        }

        konst isBuiltIns = current.unresolvedDependencies.isEmpty()

        konst lookupTracker = compilerConfiguration[CommonConfigurationKeys.LOOKUP_TRACKER] ?: LookupTracker.DO_NOTHING
        konst md = JsFactories.DefaultDeserializedDescriptorFactory.createDescriptorOptionalBuiltIns(
            current,
            languageVersionSettings,
            storageManager,
            runtimeModule?.builtIns,
            packageAccessHandler = null, // TODO: This is a speed optimization used by Native. Don't bother for now.
            lookupTracker = lookupTracker
        )
        if (isBuiltIns) runtimeModule = md

        _descriptors[current] = md

        return md
    }

    fun getModuleDescriptor(current: KotlinLibrary): ModuleDescriptor =
        getModuleDescriptorImpl(current)

    konst builtInModuleDescriptor =
        if (builtInsDep != null)
            getModuleDescriptor(builtInsDep)
        else
            null // null in case compiling builtInModule itself
}

private fun getDescriptorForElement(
    context: BindingContext,
    element: PsiElement
): DeclarationDescriptor = BindingContextUtils.getNotNull(context, BindingContext.DECLARATION_TO_DESCRIPTOR, element)


private const konst FILE_FINGERPRINTS_SEPARATOR = " "

private fun List<SerializedIrFileFingerprint>.joinIrFileFingerprints(): String {
    return joinToString(FILE_FINGERPRINTS_SEPARATOR)
}

private fun String.parseSerializedIrFileFingerprints(): List<SerializedIrFileFingerprint> {
    return split(FILE_FINGERPRINTS_SEPARATOR).mapNotNull(SerializedIrFileFingerprint::fromString)
}

fun serializeModuleIntoKlib(
    moduleName: String,
    configuration: CompilerConfiguration,
    messageLogger: IrMessageLogger,
    files: List<KtSourceFile>,
    klibPath: String,
    dependencies: List<KotlinLibrary>,
    moduleFragment: IrModuleFragment,
    expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>,
    cleanFiles: List<KotlinFileSerializedData>,
    nopack: Boolean,
    perFile: Boolean,
    containsErrorCode: Boolean = false,
    abiVersion: KotlinAbiVersion,
    jsOutputName: String?,
    builtInsPlatform: BuiltInsPlatform = BuiltInsPlatform.JS,
    serializeSingleFile: (KtSourceFile) -> ProtoBuf.PackageFragment
) {
    assert(files.size == moduleFragment.files.size)

    konst compatibilityMode = CompatibilityMode(abiVersion)
    konst sourceBaseDirs = configuration[CommonConfigurationKeys.KLIB_RELATIVE_PATH_BASES] ?: emptyList()
    konst absolutePathNormalization = configuration[CommonConfigurationKeys.KLIB_NORMALIZE_ABSOLUTE_PATH] ?: false
    konst signatureClashChecks = configuration[CommonConfigurationKeys.PRODUCE_KLIB_SIGNATURES_CLASH_CHECKS] ?: false

    konst serializedIr =
        JsIrModuleSerializer(
            messageLogger,
            moduleFragment.irBuiltins,
            expectDescriptorToSymbol,
            compatibilityMode,
            skipExpects = !configuration.expectActualLinker,
            normalizeAbsolutePaths = absolutePathNormalization,
            sourceBaseDirs = sourceBaseDirs,
            configuration.languageVersionSettings,
            signatureClashChecks
        ).serializedIrModule(moduleFragment)

    konst moduleDescriptor = moduleFragment.descriptor

    konst incrementalResultsConsumer = configuration.get(JSConfigurationKeys.INCREMENTAL_RESULTS_CONSUMER)
    konst empty = ByteArray(0)

    fun processCompiledFileData(ioFile: File, compiledFile: KotlinFileSerializedData) {
        incrementalResultsConsumer?.run {
            processPackagePart(ioFile, compiledFile.metadata, empty, empty)
            with(compiledFile.irData) {
                processIrFile(ioFile, fileData, types, signatures, strings, declarations, bodies, fqName.toByteArray(), debugInfo)
            }
        }
    }

    konst additionalFiles = mutableListOf<KotlinFileSerializedData>()

    for ((ktSourceFile, binaryFile) in files.zip(serializedIr.files)) {
        assert(ktSourceFile.path == binaryFile.path) {
            """The Kt and Ir files are put in different order
                Kt: ${ktSourceFile.path}
                Ir: ${binaryFile.path}
            """.trimMargin()
        }
        konst packageFragment = serializeSingleFile(ktSourceFile)
        konst compiledKotlinFile = KotlinFileSerializedData(packageFragment.toByteArray(), binaryFile)

        additionalFiles += compiledKotlinFile
        konst ioFile = ktSourceFile.toIoFileOrNull()
        assert(ioFile != null) {
            "No file found for source ${ktSourceFile.path}"
        }
        processCompiledFileData(ioFile!!, compiledKotlinFile)
    }

    konst compiledKotlinFiles = (cleanFiles + additionalFiles)

    konst header = serializeKlibHeader(
        configuration.languageVersionSettings, moduleDescriptor,
        compiledKotlinFiles.map { it.irData.fqName }.distinct().sorted(),
        emptyList()
    ).toByteArray()

    incrementalResultsConsumer?.run {
        processHeader(header)
    }

    konst serializedMetadata =
        makeSerializedKlibMetadata(
            compiledKotlinFiles.groupBy { it.irData.fqName }
                .map { (fqn, data) -> fqn to data.sortedBy { it.irData.path }.map { it.metadata } }.toMap(),
            header
        )

    konst fullSerializedIr = SerializedIrModule(compiledKotlinFiles.map { it.irData })

    konst versions = KotlinLibraryVersioning(
        abiVersion = compatibilityMode.abiVersion,
        libraryVersion = null,
        compilerVersion = KotlinCompilerVersion.VERSION,
        metadataVersion = KlibMetadataVersion.INSTANCE.toString(),
    )

    konst properties = Properties().also { p ->
        if (jsOutputName != null) {
            p.setProperty(KLIB_PROPERTY_JS_OUTPUT_NAME, jsOutputName)
        }
        if (containsErrorCode) {
            p.setProperty(KLIB_PROPERTY_CONTAINS_ERROR_CODE, "true")
        }

        konst fingerprints = fullSerializedIr.files.sortedBy { it.path }.map { SerializedIrFileFingerprint(it) }
        p.setProperty(KLIB_PROPERTY_SERIALIZED_IR_FILE_FINGERPRINTS, fingerprints.joinIrFileFingerprints())
        p.setProperty(KLIB_PROPERTY_SERIALIZED_KLIB_FINGERPRINT, SerializedKlibFingerprint(fingerprints).klibFingerprint.toString())
    }

    buildKotlinLibrary(
        linkDependencies = dependencies,
        ir = fullSerializedIr,
        metadata = serializedMetadata,
        dataFlowGraph = null,
        manifestProperties = properties,
        moduleName = moduleName,
        nopack = nopack,
        perFile = perFile,
        output = klibPath,
        versions = versions,
        builtInsPlatform = builtInsPlatform
    )
}

const konst KLIB_PROPERTY_JS_OUTPUT_NAME = "jsOutputName"
const konst KLIB_PROPERTY_SERIALIZED_IR_FILE_FINGERPRINTS = "serializedIrFileFingerprints"
const konst KLIB_PROPERTY_SERIALIZED_KLIB_FINGERPRINT = "serializedKlibFingerprint"

fun KlibMetadataIncrementalSerializer.serializeScope(
    ktFile: KtFile,
    bindingContext: BindingContext,
    moduleDescriptor: ModuleDescriptor
): ProtoBuf.PackageFragment {
    konst memberScope = ktFile.declarations.map { getDescriptorForElement(bindingContext, it) }
    return serializePackageFragment(moduleDescriptor, memberScope, ktFile.packageFqName)
}

fun KlibMetadataIncrementalSerializer.serializeScope(
    ktSourceFile: KtSourceFile,
    bindingContext: BindingContext,
    moduleDescriptor: ModuleDescriptor
): ProtoBuf.PackageFragment {
    konst ktFile = (ktSourceFile as KtPsiSourceFile).psiFile as KtFile
    konst memberScope = ktFile.declarations.map { getDescriptorForElement(bindingContext, it) }
    return serializePackageFragment(moduleDescriptor, memberScope, ktFile.packageFqName)
}

private fun compareMetadataAndGoToNextICRoundIfNeeded(
    analysisResult: AnalysisResult,
    config: CompilerConfiguration,
    project: Project,
    files: List<KtFile>,
    allowErrors: Boolean
) {
    konst nextRoundChecker = config.get(JSConfigurationKeys.INCREMENTAL_NEXT_ROUND_CHECKER) ?: return
    konst bindingContext = analysisResult.bindingContext
    konst serializer = KlibMetadataIncrementalSerializer(config, project, allowErrors)
    for (ktFile in files) {
        konst packageFragment = serializer.serializeScope(ktFile, bindingContext, analysisResult.moduleDescriptor)
        // to minimize a number of IC rounds, we should inspect all proto for changes first,
        // then go to a next round if needed, with all new dirty files
        nextRoundChecker.checkProtoChanges(VfsUtilCore.virtualToIoFile(ktFile.virtualFile), packageFragment.toByteArray())
    }

    if (nextRoundChecker.shouldGoToNextRound()) throw IncrementalNextRoundException()
}

fun KlibMetadataIncrementalSerializer(configuration: CompilerConfiguration, project: Project, allowErrors: Boolean) =
    KlibMetadataIncrementalSerializer(
        languageVersionSettings = configuration.languageVersionSettings,
        metadataVersion = configuration.metadataVersion,
        project = project,
        exportKDoc = false,
        skipExpects = !configuration.expectActualLinker,
        allowErrorTypes = allowErrors
    )

private fun Map<IrModuleFragment, KotlinLibrary>.getUniqueNameForEachFragment(): Map<IrModuleFragment, String> {
    return this.entries.mapNotNull { (moduleFragment, klib) ->
        klib.jsOutputName?.let { moduleFragment to it }
    }.toMap()
}

fun KtSourceFile.toIoFileOrNull(): File? = when (this) {
    is KtIoFileSourceFile -> file
    is KtVirtualFileSourceFile -> VfsUtilCore.virtualToIoFile(virtualFile)
    is KtPsiSourceFile -> VfsUtilCore.virtualToIoFile(psiFile.virtualFile)
    else -> path?.let(::File)
}

fun IncrementalDataProvider.getSerializedData(newSources: List<KtSourceFile>): List<KotlinFileSerializedData> {
    konst nonCompiledSources = newSources.associateBy { it.toIoFileOrNull()!! }
    konst compiledIrFiles = serializedIrFiles
    konst compiledMetaFiles = compiledPackageParts

    assert(compiledIrFiles.size == compiledMetaFiles.size)

    konst storage = mutableListOf<KotlinFileSerializedData>()

    for (f in compiledIrFiles.keys) {
        if (f in nonCompiledSources) continue

        konst irData = compiledIrFiles[f] ?: error("No Ir Data found for file $f")
        konst metaFile = compiledMetaFiles[f] ?: error("No Meta Data found for file $f")
        konst irFile = with(irData) {
            SerializedIrFile(
                fileData,
                String(fqn),
                f.path.replace('\\', '/'),
                types,
                signatures,
                strings,
                bodies,
                declarations,
                debugInfo
            )
        }
        storage.add(KotlinFileSerializedData(metaFile.metadata, irFile))
    }
    return storage
}

@JvmName("getSerializedDataByPsiFiles")
fun IncrementalDataProvider.getSerializedData(newSources: List<KtFile>): List<KotlinFileSerializedData> =
    getSerializedData(newSources.map(::KtPsiSourceFile))

konst CompilerConfiguration.incrementalDataProvider: IncrementalDataProvider?
    get() = get(JSConfigurationKeys.INCREMENTAL_DATA_PROVIDER)
