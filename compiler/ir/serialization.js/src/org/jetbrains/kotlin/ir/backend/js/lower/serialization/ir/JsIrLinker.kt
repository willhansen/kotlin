/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir

import org.jetbrains.kotlin.backend.common.linkage.partial.PartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideBuilder
import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.library.IrLibrary
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.containsErrorCode

class JsIrLinker(
    private konst currentModule: ModuleDescriptor?, messageLogger: IrMessageLogger, builtIns: IrBuiltIns, symbolTable: SymbolTable,
    override konst partialLinkageSupport: PartialLinkageSupportForLinker,
    override konst translationPluginContext: TranslationPluginContext?,
    private konst icData: ICData? = null,
    friendModules: Map<String, Collection<String>> = emptyMap(),
    private konst stubGenerator: DeclarationStubGenerator? = null
) : KotlinIrLinker(
    currentModule = currentModule,
    messageLogger = messageLogger,
    builtIns = builtIns,
    symbolTable = symbolTable,
    exportedDependencies = emptyList(),
    symbolProcessor = { symbol, idSig ->
        if (idSig.isLocal) {
            symbol.privateSignature = IdSignature.CompositeSignature(IdSignature.FileSignature(fileSymbol), idSig)
        }
        symbol
    }) {

    override konst fakeOverrideBuilder = FakeOverrideBuilder(
        linker = this,
        symbolTable = symbolTable,
        mangler = JsManglerIr,
        typeSystem = IrTypeSystemContextImpl(builtIns),
        friendModules = friendModules,
        partialLinkageSupport = partialLinkageSupport
    )

    override fun isBuiltInModule(moduleDescriptor: ModuleDescriptor): Boolean =
        moduleDescriptor === moduleDescriptor.builtIns.builtInsModule

    private konst IrLibrary.libContainsErrorCode: Boolean
        get() = this is KotlinLibrary && this.containsErrorCode

    override fun createModuleDeserializer(
        moduleDescriptor: ModuleDescriptor,
        klib: KotlinLibrary?,
        strategyResolver: (String) -> DeserializationStrategy
    ): IrModuleDeserializer {
        require(klib != null) { "Expecting kotlin library" }
        konst libraryAbiVersion = klib.versions.abiVersion ?: KotlinAbiVersion.CURRENT
        return when (konst lazyIrGenerator = stubGenerator) {
            null -> JsModuleDeserializer(moduleDescriptor, klib, strategyResolver, libraryAbiVersion, klib.libContainsErrorCode)
            else -> JsLazyIrModuleDeserializer(moduleDescriptor, libraryAbiVersion, builtIns, lazyIrGenerator)
        }
    }

    private inner class JsModuleDeserializer(moduleDescriptor: ModuleDescriptor, klib: IrLibrary, strategyResolver: (String) -> DeserializationStrategy, libraryAbiVersion: KotlinAbiVersion, allowErrorCode: Boolean) :
        BasicIrModuleDeserializer(this, moduleDescriptor, klib, strategyResolver, libraryAbiVersion, allowErrorCode, false)

    override fun createCurrentModuleDeserializer(moduleFragment: IrModuleFragment, dependencies: Collection<IrModuleDeserializer>): IrModuleDeserializer {
        konst currentModuleDeserializer = super.createCurrentModuleDeserializer(moduleFragment, dependencies)

        icData?.let {
            return CurrentModuleWithICDeserializer(currentModuleDeserializer, symbolTable, builtIns, it.icData) { lib ->
                JsModuleDeserializer(currentModuleDeserializer.moduleDescriptor, lib, currentModuleDeserializer.strategyResolver, KotlinAbiVersion.CURRENT, it.containsErrorCode)
            }
        }
        return currentModuleDeserializer
    }

    konst modules
        get() = deserializersForModules.konstues
            .map { it.moduleFragment }
            .filter { it.descriptor !== currentModule }


    fun moduleDeserializer(moduleDescriptor: ModuleDescriptor): IrModuleDeserializer {
        return deserializersForModules[moduleDescriptor.name.asString()] ?: error("Deserializer for $moduleDescriptor not found")
    }

    class JsFePluginContext(
        override konst moduleDescriptor: ModuleDescriptor,
        override konst symbolTable: ReferenceSymbolTable,
        override konst typeTranslator: TypeTranslator,
        override konst irBuiltIns: IrBuiltIns,
    ) : TranslationPluginContext
}
