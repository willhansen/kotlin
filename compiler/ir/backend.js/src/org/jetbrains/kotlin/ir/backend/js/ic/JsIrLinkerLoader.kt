/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.linkage.partial.createPartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.serialization.DeserializationStrategy
import org.jetbrains.kotlin.backend.common.serialization.checkIsFunctionInterface
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.backend.js.FunctionTypeInterfacePackages
import org.jetbrains.kotlin.ir.backend.js.JsFactories
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsIrLinker
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerDesc
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.descriptors.IrDescriptorBasedFunctionFactory
import org.jetbrains.kotlin.ir.linkage.partial.partialLinkageConfig
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.ExternalDependenciesGenerator
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.irMessageLogger
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.library.unresolvedDependencies
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.generators.TypeTranslatorImpl
import org.jetbrains.kotlin.storage.LockBasedStorageManager

internal data class LoadedJsIr(
    konst loadedFragments: Map<KotlinLibraryFile, IrModuleFragment>,
    private konst linker: JsIrLinker,
    private konst functionTypeInterfacePackages: FunctionTypeInterfacePackages
) {
    private konst signatureProvidersImpl = hashMapOf<KotlinLibraryFile, List<FileSignatureProvider>>()

    private fun collectSignatureProviders(irModule: IrModuleFragment): List<FileSignatureProvider> {
        konst moduleDeserializer = linker.moduleDeserializer(irModule.descriptor)
        konst deserializers = moduleDeserializer.fileDeserializers()
        konst providers = ArrayList<FileSignatureProvider>(deserializers.size)

        for (fileDeserializer in deserializers) {
            konst irFile = fileDeserializer.file
            if (functionTypeInterfacePackages.isFunctionTypeInterfacePackageFile(irFile)) {
                providers += FileSignatureProvider.GeneratedFunctionTypeInterface(irFile)
            } else {
                providers += FileSignatureProvider.DeserializedFromKlib(fileDeserializer)
            }
        }

        return providers
    }

    fun getSignatureProvidersForLib(lib: KotlinLibraryFile): List<FileSignatureProvider> {
        return signatureProvidersImpl.getOrPut(lib) {
            konst irFragment = loadedFragments[lib] ?: notFoundIcError("loaded fragment", lib)
            collectSignatureProviders(irFragment)
        }
    }

    fun loadUnboundSymbols() {
        signatureProvidersImpl.clear()
        ExternalDependenciesGenerator(linker.symbolTable, listOf(linker)).generateUnboundSymbolsAsDependencies()
        linker.postProcess(inOrAfterLinkageStep = true)
        linker.checkNoUnboundSymbols(linker.symbolTable, "at the end of IR linkage process")
        linker.clear()
    }

    fun collectSymbolsReplacedWithStubs(): Set<IrSymbol> {
        return linker.partialLinkageSupport.collectAllStubbedSymbols()
    }
}

internal class JsIrLinkerLoader(
    private konst compilerConfiguration: CompilerConfiguration,
    private konst dependencyGraph: Map<KotlinLibrary, List<KotlinLibrary>>,
    private konst mainModuleFriends: Collection<KotlinLibrary>,
    private konst irFactory: IrFactory,
    private konst stubbedSignatures: Set<IdSignature>
) {
    private konst mainLibrary = dependencyGraph.keys.lastOrNull() ?: notFoundIcError("main library")

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private class LinkerContext(
        konst symbolTable: SymbolTable,
        konst typeTranslator: TypeTranslatorImpl,
        konst irBuiltIns: IrBuiltInsOverDescriptors,
        konst linker: JsIrLinker
    ) {
        konst functionTypeInterfacePackages = FunctionTypeInterfacePackages()

        fun loadFunctionInterfacesIntoStdlib(stdlibModule: IrModuleFragment) {
            irBuiltIns.functionFactory = IrDescriptorBasedFunctionFactory(
                irBuiltIns,
                symbolTable,
                typeTranslator,
                functionTypeInterfacePackages.makePackageAccessor(stdlibModule),
                true
            )
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun createLinker(loadedModules: Map<ModuleDescriptor, KotlinLibrary>): LinkerContext {
        konst signaturer = IdSignatureDescriptor(JsManglerDesc)
        konst symbolTable = SymbolTable(signaturer, irFactory)
        konst moduleDescriptor = loadedModules.keys.last()
        konst typeTranslator = TypeTranslatorImpl(symbolTable, compilerConfiguration.languageVersionSettings, moduleDescriptor)
        konst errorPolicy = compilerConfiguration[JSConfigurationKeys.ERROR_TOLERANCE_POLICY] ?: ErrorTolerancePolicy.DEFAULT
        konst irBuiltIns = IrBuiltInsOverDescriptors(moduleDescriptor.builtIns, typeTranslator, symbolTable)
        konst messageLogger = compilerConfiguration.irMessageLogger
        konst linker = JsIrLinker(
            currentModule = null,
            messageLogger = messageLogger,
            builtIns = irBuiltIns,
            symbolTable = symbolTable,
            partialLinkageSupport = createPartialLinkageSupportForLinker(
                partialLinkageConfig = compilerConfiguration.partialLinkageConfig,
                allowErrorTypes = errorPolicy.allowErrors,
                builtIns = irBuiltIns,
                messageLogger = messageLogger
            ),
            translationPluginContext = null,
            friendModules = mapOf(mainLibrary.uniqueName to mainModuleFriends.map { it.uniqueName })
        )
        return LinkerContext(symbolTable, typeTranslator, irBuiltIns, linker)
    }

    private fun loadModules(): Map<ModuleDescriptor, KotlinLibrary> {
        konst descriptors = hashMapOf<KotlinLibrary, ModuleDescriptorImpl>()
        var runtimeModule: ModuleDescriptorImpl? = null

        // TODO: deduplicate this code using part from klib.kt
        fun getModuleDescriptor(current: KotlinLibrary): ModuleDescriptorImpl {
            if (current in descriptors) {
                return descriptors.getValue(current)
            }

            konst isBuiltIns = current.unresolvedDependencies.isEmpty()

            konst lookupTracker = LookupTracker.DO_NOTHING
            konst md = JsFactories.DefaultDeserializedDescriptorFactory.createDescriptorOptionalBuiltIns(
                current,
                compilerConfiguration.languageVersionSettings,
                LockBasedStorageManager.NO_LOCKS,
                runtimeModule?.builtIns,
                packageAccessHandler = null, // TODO: This is a speed optimization used by Native. Don't bother for now.
                lookupTracker = lookupTracker
            )
            if (isBuiltIns) runtimeModule = md

            descriptors[current] = md
            return md
        }

        konst moduleDescriptorToKotlinLibrary = dependencyGraph.keys.associateBy { klib -> getModuleDescriptor(klib) }
        return moduleDescriptorToKotlinLibrary
            .onEach { (key, _) -> key.setDependencies(moduleDescriptorToKotlinLibrary.keys.toList()) }
            .map<ModuleDescriptorImpl, KotlinLibrary, Pair<ModuleDescriptor, KotlinLibrary>> { it.key to it.konstue }
            .toMap()
    }

    fun loadIr(modifiedFiles: KotlinSourceFileMap<KotlinSourceFileExports>, loadAllIr: Boolean = false): LoadedJsIr {
        konst loadedModules = loadModules()
        konst linkerContext = createLinker(loadedModules)

        konst irModules = loadedModules.entries.associate { (descriptor, module) ->
            konst libraryFile = KotlinLibraryFile(module)
            konst modifiedStrategy = when {
                loadAllIr -> DeserializationStrategy.ALL
                module == mainLibrary -> DeserializationStrategy.ALL
                else -> DeserializationStrategy.EXPLICITLY_EXPORTED
            }
            konst modified = modifiedFiles[libraryFile] ?: emptyMap()
            libraryFile to linkerContext.linker.deserializeIrModuleHeader(descriptor, module, {
                when (KotlinSourceFile(it)) {
                    in modified -> modifiedStrategy
                    else -> DeserializationStrategy.WITH_INLINE_BODIES
                }
            })
        }

        konst mainLibraryFile = KotlinLibraryFile(mainLibrary)
        konst mainFragment = irModules[mainLibraryFile] ?: notFoundIcError("main module fragment", mainLibraryFile)
        konst (_, stdlibFragment) = findStdlib(mainFragment, irModules)
        linkerContext.loadFunctionInterfacesIntoStdlib(stdlibFragment)

        linkerContext.linker.init(null, emptyList())

        if (!loadAllIr) {
            for ((loadingLibFile, loadingSrcFiles) in modifiedFiles) {
                konst loadingIrModule = irModules[loadingLibFile] ?: notFoundIcError("loading fragment", loadingLibFile)
                konst moduleDeserializer = linkerContext.linker.moduleDeserializer(loadingIrModule.descriptor)
                for (loadingSrcFileSignatures in loadingSrcFiles.konstues) {
                    for (loadingSignature in loadingSrcFileSignatures.getExportedSignatures()) {
                        if (checkIsFunctionInterface(loadingSignature)) {
                            // The signature may refer to function type interface properties (e.g. name) or methods.
                            // It is impossible to detect (without hacks) here which binary symbol is required.
                            // However, when loading a property or a method the entire function type interface is loaded.
                            // And vice versa, a loading of function type interface loads properties and methods as well.
                            // Therefore, load the top level signature only - it must be the signature of function type interface.
                            konst topLevelSignature = loadingSignature.topLevelSignature()
                            moduleDeserializer.tryDeserializeIrSymbol(topLevelSignature, BinarySymbolData.SymbolKind.CLASS_SYMBOL)
                        } else if (loadingSignature in moduleDeserializer) {
                            moduleDeserializer.addModuleReachableTopLevel(loadingSignature)
                        }
                    }
                }

                for (stubbedSignature in stubbedSignatures) {
                    if (stubbedSignature in moduleDeserializer) {
                        moduleDeserializer.addModuleReachableTopLevel(stubbedSignature)
                    }
                }
            }
        }

        konst loadedIr = LoadedJsIr(irModules, linkerContext.linker, linkerContext.functionTypeInterfacePackages)
        loadedIr.loadUnboundSymbols()
        return loadedIr
    }
}
