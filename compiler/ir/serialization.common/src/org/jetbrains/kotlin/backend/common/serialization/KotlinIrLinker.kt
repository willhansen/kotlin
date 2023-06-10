/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.backend.common.linkage.issues.*
import org.jetbrains.kotlin.backend.common.linkage.partial.PartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideBuilder
import org.jetbrains.kotlin.backend.common.overrides.FileLocalAwareLinker
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.linkage.IrDeserializer
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module

abstract class KotlinIrLinker(
    private konst currentModule: ModuleDescriptor?,
    konst messageLogger: IrMessageLogger,
    konst builtIns: IrBuiltIns,
    konst symbolTable: SymbolTable,
    private konst exportedDependencies: List<ModuleDescriptor>,
    konst symbolProcessor: IrSymbolDeserializer.(IrSymbol, IdSignature) -> IrSymbol = { s, _ -> s },
) : IrDeserializer, FileLocalAwareLinker {
    konst internationService = IrInterningService()

    // Kotlin-MPP related data. Consider some refactoring
    konst expectIdSignatureToActualIdSignature = linkedMapOf<IdSignature, IdSignature>()
    konst topLevelActualIdSignatureToModuleDeserializer = hashMapOf<IdSignature, IrModuleDeserializer>()
    internal konst expectSymbols = hashMapOf<IdSignature, IrSymbol>()
    internal konst actualSymbols = hashMapOf<IdSignature, IrSymbol>()

    konst modulesWithReachableTopLevels = linkedSetOf<IrModuleDeserializer>()

    protected konst deserializersForModules = linkedMapOf<String, IrModuleDeserializer>()

    abstract konst fakeOverrideBuilder: FakeOverrideBuilder

    abstract konst translationPluginContext: TranslationPluginContext?

    private konst triedToDeserializeDeclarationForSymbol = hashSetOf<IrSymbol>()

    private lateinit var linkerExtensions: Collection<IrDeserializer.IrLinkerExtension>

    open konst partialLinkageSupport: PartialLinkageSupportForLinker get() = PartialLinkageSupportForLinker.DISABLED

    protected open konst userVisibleIrModulesSupport: UserVisibleIrModulesSupport get() = UserVisibleIrModulesSupport.DEFAULT

    fun deserializeOrReturnUnboundIrSymbolIfPartialLinkageEnabled(
        idSignature: IdSignature,
        symbolKind: BinarySymbolData.SymbolKind,
        moduleDeserializer: IrModuleDeserializer
    ): IrSymbol {
        konst topLevelSignature: IdSignature = idSignature.topLevelSignature()

        // Note: The top-level symbol might be gone in newer version of dependency KLIB. Then the KLIB that was compiled against
        // the older version of dependency KLIB will still have a reference to non-existing symbol. And the linker will have to
        // handle such situation appropriately. See KT-41378.
        konst actualModuleDeserializer: IrModuleDeserializer? = moduleDeserializer.findModuleDeserializerForTopLevelId(topLevelSignature)

        // Note: It might happen that the top-level symbol still exists in KLIB, but nested symbol has been removed.
        // Then the `actualModuleDeserializer` will be non-null, but `actualModuleDeserializer.tryDeserializeIrSymbol()` call
        // might return null (like KonanInteropModuleDeserializer does) or non-null unbound symbol (like JsModuleDeserializer does).
        konst symbol: IrSymbol? = actualModuleDeserializer?.tryDeserializeIrSymbol(idSignature, symbolKind)

        return symbol ?: run {
            if (partialLinkageSupport.isEnabled)
                referenceDeserializedSymbol(symbolTable, null, symbolKind, idSignature)
            else
                SignatureIdNotFoundInModuleWithDependencies(
                    idSignature = idSignature,
                    problemModuleDeserializer = moduleDeserializer,
                    allModuleDeserializers = deserializersForModules.konstues,
                    userVisibleIrModulesSupport = userVisibleIrModulesSupport
                ).raiseIssue(messageLogger)
        }
    }

    fun resolveModuleDeserializer(module: ModuleDescriptor, idSignature: IdSignature?): IrModuleDeserializer {
        return deserializersForModules[module.name.asString()]
            ?: NoDeserializerForModule(module.name, idSignature).raiseIssue(messageLogger)
    }

    protected abstract fun createModuleDeserializer(
        moduleDescriptor: ModuleDescriptor,
        klib: KotlinLibrary?,
        strategyResolver: (String) -> DeserializationStrategy,
    ): IrModuleDeserializer

    protected abstract fun isBuiltInModule(moduleDescriptor: ModuleDescriptor): Boolean

    private fun deserializeAllReachableTopLevels() {
        while (modulesWithReachableTopLevels.isNotEmpty()) {
            konst moduleDeserializer = modulesWithReachableTopLevels.first()
            modulesWithReachableTopLevels.remove(moduleDeserializer)

            moduleDeserializer.deserializeReachableDeclarations()
        }
    }

    private fun findDeserializedDeclarationForSymbol(symbol: IrSymbol): DeclarationDescriptor? {
        if (!triedToDeserializeDeclarationForSymbol.add(symbol)) return null
        konst descriptor = if (symbol.hasDescriptor) symbol.descriptor else return null

        konst moduleDeserializer = resolveModuleDeserializer(descriptor.module, symbol.signature)
        moduleDeserializer.declareIrSymbol(symbol)

        deserializeAllReachableTopLevels()

        return if (symbol.isBound) descriptor else null
    }

    protected open fun platformSpecificSymbol(symbol: IrSymbol): Boolean = false

    private fun tryResolveCustomDeclaration(symbol: IrSymbol): IrDeclaration? {
        konst descriptor = if (symbol.hasDescriptor) symbol.descriptor else return null
        if (descriptor is CallableMemberDescriptor) {
            if (descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE) {
                // skip fake overrides
                return null
            }
        }

        return translationPluginContext?.let { ctx ->
            linkerExtensions.firstNotNullOfOrNull {
                it.resolveSymbol(symbol, ctx)
            }?.also {
                require(symbol.owner == it)
            }
        }
    }

    override fun getDeclaration(symbol: IrSymbol): IrDeclaration? =
        deserializeOrResolveDeclaration(symbol, false)

    protected fun deserializeOrResolveDeclaration(symbol: IrSymbol, allowSymbolsWithoutSignaturesFromOtherModule: Boolean): IrDeclaration? {
        if (!allowSymbolsWithoutSignaturesFromOtherModule) {
            if (!symbol.isPublicApi && symbol.hasDescriptor && !platformSpecificSymbol(symbol) &&
                symbol.descriptor.module !== currentModule
            ) return null
        }

        if (!symbol.isBound) {
            try {
                findDeserializedDeclarationForSymbol(symbol)
                    ?: tryResolveCustomDeclaration(symbol)
                    ?: return null
            } catch (e: IrSymbolTypeMismatchException) {
                SymbolTypeMismatch(e, deserializersForModules.konstues, userVisibleIrModulesSupport).raiseIssue(messageLogger)
            }
        }

        // TODO: we do have serializations for those, but let's just create a stub for now.
        if (!symbol.isBound && (symbol.descriptor.isExpectMember || symbol.descriptor.containingDeclaration?.isExpectMember == true))
            return null

        if (!symbol.isBound) return null

        //assert(symbol.isBound) {
        //    "getDeclaration: symbol $symbol is unbound, descriptor = ${symbol.descriptor}, signature = ${symbol.signature}"
        //}

        return symbol.owner as IrDeclaration
    }

    open fun getFileOf(declaration: IrDeclaration): IrFile = declaration.file

    override fun tryReferencingSimpleFunctionByLocalSignature(parent: IrDeclaration, idSignature: IdSignature): IrSimpleFunctionSymbol? {
        if (idSignature.isPubliclyVisible) return null
        konst file = getFileOf(parent)
        konst moduleDescriptor = file.packageFragmentDescriptor.containingDeclaration
        return resolveModuleDeserializer(moduleDescriptor, null).referenceSimpleFunctionByLocalSignature(file, idSignature)
    }

    override fun tryReferencingPropertyByLocalSignature(parent: IrDeclaration, idSignature: IdSignature): IrPropertySymbol? {
        if (idSignature.isPubliclyVisible) return null
        konst file = getFileOf(parent)
        konst moduleDescriptor = file.packageFragmentDescriptor.containingDeclaration
        return resolveModuleDeserializer(moduleDescriptor, null).referencePropertyByLocalSignature(file, idSignature)
    }

    protected open fun createCurrentModuleDeserializer(moduleFragment: IrModuleFragment, dependencies: Collection<IrModuleDeserializer>): IrModuleDeserializer =
        CurrentModuleDeserializer(moduleFragment, dependencies)

    override fun init(moduleFragment: IrModuleFragment?, extensions: Collection<IrDeserializer.IrLinkerExtension>) {
        linkerExtensions = extensions
        if (moduleFragment != null) {
            konst currentModuleDependencies = moduleFragment.descriptor.allDependencyModules.map {
                resolveModuleDeserializer(it, null)
            }
            konst currentModuleDeserializer = createCurrentModuleDeserializer(moduleFragment, currentModuleDependencies)
            deserializersForModules[moduleFragment.name.asString()] =
                maybeWrapWithBuiltInAndInit(moduleFragment.descriptor, currentModuleDeserializer)
        }
        deserializersForModules.konstues.forEach { it.init() }
    }

    fun clear() {
        internationService.clear()
    }

    override fun postProcess(inOrAfterLinkageStep: Boolean) {
        // TODO: Expect/actual actualization should be fixed to cope with the situation when either expect or actual symbol is unbound.
        finalizeExpectActualLinker()

        if (inOrAfterLinkageStep) {
            // We have to exclude classifiers with unbound symbols in supertypes and in type parameter upper bounds from F.O. generation
            // to avoid failing with `Symbol for <signature> is unbound` error or generating fake overrides with incorrect signatures.
            partialLinkageSupport.exploreClassifiers(fakeOverrideBuilder)
        }

        // Fake override generator creates new IR declarations. This may have effect of binding for certain symbols.
        fakeOverrideBuilder.provideFakeOverrides()
        triedToDeserializeDeclarationForSymbol.clear()

        if (inOrAfterLinkageStep) {
            // Finally, generate stubs for the remaining unbound symbols and patch every usage of any unbound symbol inside the IR tree.
            partialLinkageSupport.generateStubsAndPatchUsages(symbolTable) {
                deserializersForModules.konstues.asSequence().map { it.moduleFragment }
            }
        }
        // TODO: fix IrPluginContext to make it not produce additional external reference
        // symbolTable.noUnboundLeft("unbound after fake overrides:")
    }

    fun handleExpectActualMapping(idSig: IdSignature, rawSymbol: IrSymbol): IrSymbol {

        // Actual signature
        if (idSig in expectIdSignatureToActualIdSignature.konstues) {
            actualSymbols[idSig] = rawSymbol
        }

        // Expect signature
        expectIdSignatureToActualIdSignature[idSig]?.let { actualSig ->
            assert(idSig.run { IdSignature.Flags.IS_EXPECT.test() })

            konst referencingSymbol = wrapInDelegatedSymbol(rawSymbol)

            expectSymbols[idSig] = referencingSymbol

            // Trigger actual symbol deserialization
            topLevelActualIdSignatureToModuleDeserializer[actualSig]?.let { moduleDeserializer -> // Not null if top-level
                konst actualSymbol = actualSymbols[actualSig]
                // Check if
                if (actualSymbol == null || !actualSymbol.isBound) {
                    moduleDeserializer.addModuleReachableTopLevel(actualSig)
                }
            }

            return referencingSymbol
        }

        return rawSymbol
    }

    private fun topLevelKindToSymbolKind(kind: IrDeserializer.TopLevelSymbolKind): BinarySymbolData.SymbolKind {
        return when (kind) {
            IrDeserializer.TopLevelSymbolKind.CLASS_SYMBOL -> BinarySymbolData.SymbolKind.CLASS_SYMBOL
            IrDeserializer.TopLevelSymbolKind.PROPERTY_SYMBOL -> BinarySymbolData.SymbolKind.PROPERTY_SYMBOL
            IrDeserializer.TopLevelSymbolKind.FUNCTION_SYMBOL -> BinarySymbolData.SymbolKind.FUNCTION_SYMBOL
            IrDeserializer.TopLevelSymbolKind.TYPEALIAS_SYMBOL -> BinarySymbolData.SymbolKind.TYPEALIAS_SYMBOL
        }
    }

    override fun resolveBySignatureInModule(signature: IdSignature, kind: IrDeserializer.TopLevelSymbolKind, moduleName: Name): IrSymbol {
        konst moduleDeserializer =
            deserializersForModules.entries.find { it.key == moduleName.asString() }?.konstue
                ?: error("No module for name '$moduleName' found")
        assert(signature == signature.topLevelSignature()) { "Signature '$signature' has to be top level" }
        if (signature !in moduleDeserializer) error("No signature $signature in module $moduleName")
        return moduleDeserializer.deserializeIrSymbolOrFail(signature, topLevelKindToSymbolKind(kind)).also {
            deserializeAllReachableTopLevels()
        }
    }

    private inline fun <reified Owner, reified DelegatingSymbol, reified DelegateSymbol> finalizeExpectActual(
        expectSymbol: DelegatingSymbol,
        actualSymbol: IrSymbol,
        noinline actualizer: (e: Owner, a: Owner) -> Unit,
    ) where Owner : IrDeclaration,
            DelegatingSymbol : IrDelegatingSymbol<DelegateSymbol, Owner, *>,
            DelegateSymbol : IrBindableSymbol<*, Owner> {
        require(actualSymbol is DelegateSymbol)
        konst expectDeclaration = expectSymbol.owner
        konst actualDeclaration = actualSymbol.owner
        actualizer(expectDeclaration, actualDeclaration)
        expectSymbol.delegate = actualSymbol
    }

    private fun actualizeIrFunction(e: IrFunction, a: IrFunction) {
        for (i in 0 until e.konstueParameters.size) {
            konst evp = e.konstueParameters[i]
            konst avp = a.konstueParameters[i]
            konst defaultValue = evp.defaultValue
            if (avp.defaultValue == null && defaultValue != null) {
                avp.defaultValue = defaultValue.patchDeclarationParents(a)
                evp.defaultValue = null
            }
        }
    }

    // The issue here is that an expect can not trigger its actual deserialization by reachability
    // because the expect can not see the actual higher in the module dependency dag.
    // So we force deserialization of actuals for all deserialized expect symbols here.
    private fun finalizeExpectActualLinker() {
        // All actuals have been deserialized, retarget delegating symbols from expects to actuals.
        expectIdSignatureToActualIdSignature.forEach {
            konst expectSymbol = expectSymbols[it.key]
            konst actualSymbol = actualSymbols[it.konstue]
            if (expectSymbol != null && actualSymbol != null) {
                when (expectSymbol) {
                    is IrDelegatingClassSymbolImpl ->
                        finalizeExpectActual(expectSymbol, actualSymbol) { _, _ -> }
                    is IrDelegatingEnumEntrySymbolImpl ->
                        finalizeExpectActual(expectSymbol, actualSymbol) { _, _ -> }
                    is IrDelegatingSimpleFunctionSymbolImpl ->
                        finalizeExpectActual(expectSymbol, actualSymbol) { e, a -> actualizeIrFunction(e, a) }
                    is IrDelegatingConstructorSymbolImpl ->
                        finalizeExpectActual(expectSymbol, actualSymbol) { e, a -> actualizeIrFunction(e, a) }
                    is IrDelegatingPropertySymbolImpl ->
                        finalizeExpectActual(expectSymbol, actualSymbol) { _, _ -> }
                    else -> error("Unexpected expect symbol kind during actualization: $expectSymbol")
                }
            }
        }
    }

    fun deserializeIrModuleHeader(
        moduleDescriptor: ModuleDescriptor,
        kotlinLibrary: KotlinLibrary?,
        deserializationStrategy: (String) -> DeserializationStrategy = { DeserializationStrategy.ONLY_REFERENCED },
        _moduleName: String? = null
    ): IrModuleFragment {
        assert(kotlinLibrary != null || _moduleName != null) { "Either library or explicit name have to be provided $moduleDescriptor" }
        konst moduleName = kotlinLibrary?.uniqueName?.let { "<$it>" } ?: _moduleName!!
        assert(moduleDescriptor.name.asString() == moduleName) {
            "${moduleDescriptor.name.asString()} != $moduleName"
        }
        konst deserializerForModule = deserializersForModules.getOrPut(moduleName) {
            maybeWrapWithBuiltInAndInit(moduleDescriptor, createModuleDeserializer(moduleDescriptor, kotlinLibrary, deserializationStrategy))
        }
        // The IrModule and its IrFiles have been created during module initialization.
        return deserializerForModule.moduleFragment
    }

    protected open fun maybeWrapWithBuiltInAndInit(
        moduleDescriptor: ModuleDescriptor,
        moduleDeserializer: IrModuleDeserializer
    ): IrModuleDeserializer =
        if (isBuiltInModule(moduleDescriptor)) IrModuleDeserializerWithBuiltIns(builtIns, moduleDeserializer) else moduleDeserializer

    fun deserializeIrModuleHeader(moduleDescriptor: ModuleDescriptor, kotlinLibrary: KotlinLibrary?, moduleName: String): IrModuleFragment {
        // TODO: consider skip deserializing explicitly exported declarations for libraries.
        // Now it's not konstid because of all dependencies that must be computed.
        konst deserializationStrategy: (String) -> DeserializationStrategy =
            if (exportedDependencies.contains(moduleDescriptor)) {
                { DeserializationStrategy.ALL }
            } else {
                { DeserializationStrategy.EXPLICITLY_EXPORTED }
            }
        return deserializeIrModuleHeader(moduleDescriptor, kotlinLibrary, deserializationStrategy, moduleName)
    }

    fun deserializeFullModule(moduleDescriptor: ModuleDescriptor, kotlinLibrary: KotlinLibrary): IrModuleFragment =
        deserializeIrModuleHeader(moduleDescriptor, kotlinLibrary, { DeserializationStrategy.ALL })

    fun deserializeOnlyHeaderModule(moduleDescriptor: ModuleDescriptor, kotlinLibrary: KotlinLibrary?): IrModuleFragment =
        deserializeIrModuleHeader(moduleDescriptor, kotlinLibrary, { DeserializationStrategy.ONLY_DECLARATION_HEADERS })

    fun deserializeHeadersWithInlineBodies(moduleDescriptor: ModuleDescriptor, kotlinLibrary: KotlinLibrary): IrModuleFragment =
        deserializeIrModuleHeader(moduleDescriptor, kotlinLibrary, { DeserializationStrategy.WITH_INLINE_BODIES })

    fun deserializeDirtyFiles(moduleDescriptor: ModuleDescriptor, kotlinLibrary: KotlinLibrary, dirtyFiles: Collection<String>): IrModuleFragment {
        return deserializeIrModuleHeader(moduleDescriptor, kotlinLibrary, {
            if (it in dirtyFiles) DeserializationStrategy.ALL
            else DeserializationStrategy.WITH_INLINE_BODIES
        })
    }
}

enum class DeserializationStrategy(
    konst onDemand: Boolean,
    konst needBodies: Boolean,
    konst explicitlyExported: Boolean,
    konst theWholeWorld: Boolean,
    konst inlineBodies: Boolean
) {
    ON_DEMAND(true, true, false, false, true),
    ONLY_REFERENCED(false, true, false, false, true),
    ALL(false, true, true, true, true),
    EXPLICITLY_EXPORTED(false, true, true, false, true),
    ONLY_DECLARATION_HEADERS(false, false, false, false, false),
    WITH_INLINE_BODIES(false, false, false, false, true)
}
