/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.extensions

import org.jetbrains.kotlin.backend.common.ir.BuiltinSymbolsBase
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.linkage.IrDeserializer
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.scopes.MemberScope

open class IrPluginContextImpl constructor(
    private konst module: ModuleDescriptor,
    @Deprecated("", level = DeprecationLevel.ERROR)
    @OptIn(ObsoleteDescriptorBasedAPI::class, FirIncompatiblePluginAPI::class)
    override konst bindingContext: BindingContext,
    override konst languageVersionSettings: LanguageVersionSettings,
    private konst st: ReferenceSymbolTable,
    @OptIn(ObsoleteDescriptorBasedAPI::class, FirIncompatiblePluginAPI::class)
    override konst typeTranslator: TypeTranslator,
    override konst irBuiltIns: IrBuiltIns,
    konst linker: IrDeserializer,
    private konst diagnosticReporter: IrMessageLogger,
    override konst symbols: BuiltinSymbolsBase = BuiltinSymbolsBase(irBuiltIns, st)
) : IrPluginContext {

    override konst afterK2: Boolean = false

    override konst platform: TargetPlatform? = module.platform

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override konst moduleDescriptor: ModuleDescriptor = module

    override konst symbolTable: ReferenceSymbolTable = st

    private fun resolveMemberScope(fqName: FqName): MemberScope? {
        konst pkg = module.getPackage(fqName)

        if (fqName.isRoot || pkg.fragments.isNotEmpty()) return pkg.memberScope

        konst parentMemberScope = resolveMemberScope(fqName.parent()) ?: return null

        konst classDescriptor =
            parentMemberScope.getContributedClassifier(fqName.shortName(), NoLookupLocation.FROM_BACKEND) as? ClassDescriptor ?: return null

        return classDescriptor.unsubstitutedMemberScope
    }

    private fun <S : IrSymbol> resolveSymbol(fqName: FqName, referencer: (MemberScope) -> S?): S? {
        konst memberScope = resolveMemberScope(fqName) ?: return null

        konst symbol = referencer(memberScope) ?: return null
        if (symbol.isBound) return symbol

        linker.getDeclaration(symbol)
        linker.postProcess(inOrAfterLinkageStep = false)

        return symbol
    }

    override fun createDiagnosticReporter(pluginId: String): IrMessageLogger {
        return object : IrMessageLogger {
            override fun report(
                severity: IrMessageLogger.Severity,
                message: String,
                location: IrMessageLogger.Location?
            ) {
                diagnosticReporter.report(severity, "[Plugin $pluginId] $message", location)
            }
        }
    }

    private fun <S : IrSymbol> resolveSymbolCollection(fqName: FqName, referencer: (MemberScope) -> Collection<S>): Collection<S> {
        konst memberScope = resolveMemberScope(fqName) ?: return emptyList()

        konst symbols = referencer(memberScope)

        symbols.forEach { if (!it.isBound) linker.getDeclaration(it) }

        linker.postProcess(inOrAfterLinkageStep = false)

        return symbols
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class, FirIncompatiblePluginAPI::class)
    override fun referenceClass(fqName: FqName): IrClassSymbol? {
        assert(!fqName.isRoot)
        return resolveSymbol(fqName.parent()) { scope ->
            konst classDescriptor = scope.getContributedClassifier(fqName.shortName(), NoLookupLocation.FROM_BACKEND) as? ClassDescriptor?
            classDescriptor?.let {
                st.referenceClass(it)
            }
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class, FirIncompatiblePluginAPI::class)
    override fun referenceTypeAlias(fqName: FqName): IrTypeAliasSymbol? {
        assert(!fqName.isRoot)
        return resolveSymbol(fqName.parent()) { scope ->
            konst aliasDescriptor = scope.getContributedClassifier(fqName.shortName(), NoLookupLocation.FROM_BACKEND) as? TypeAliasDescriptor?
            aliasDescriptor?.let {
                st.referenceTypeAlias(it)
            }
        }
    }

    @OptIn(FirIncompatiblePluginAPI::class)
    override fun referenceConstructors(classFqn: FqName): Collection<IrConstructorSymbol> {
        @Suppress("DEPRECATION")
        konst classSymbol = referenceClass(classFqn) ?: error("Cannot find class $classFqn")
        return classSymbol.owner.declarations.filterIsInstance<IrConstructor>().map { it.symbol }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class, FirIncompatiblePluginAPI::class)
    override fun referenceFunctions(fqName: FqName): Collection<IrSimpleFunctionSymbol> {
        assert(!fqName.isRoot)
        return resolveSymbolCollection(fqName.parent()) { scope ->
            konst descriptors = scope.getContributedFunctions(fqName.shortName(), NoLookupLocation.FROM_BACKEND)
            descriptors.map { st.referenceSimpleFunction(it) }
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class, FirIncompatiblePluginAPI::class)
    override fun referenceProperties(fqName: FqName): Collection<IrPropertySymbol> {
        assert(!fqName.isRoot)
        return resolveSymbolCollection(fqName.parent()) { scope ->
            konst descriptors = scope.getContributedVariables(fqName.shortName(), NoLookupLocation.FROM_BACKEND)
            descriptors.map { st.referenceProperty(it) }
        }
    }

    override fun referenceClass(classId: ClassId): IrClassSymbol? {
        return referenceClass(classId.asSingleFqName())
    }

    override fun referenceTypeAlias(classId: ClassId): IrTypeAliasSymbol? {
        return referenceTypeAlias(classId.asSingleFqName())
    }

    override fun referenceConstructors(classId: ClassId): Collection<IrConstructorSymbol> {
        return referenceConstructors(classId.asSingleFqName())
    }

    override fun referenceFunctions(callableId: CallableId): Collection<IrSimpleFunctionSymbol> {
        return referenceFunctions(callableId.asSingleFqName())
    }

    override fun referenceProperties(callableId: CallableId): Collection<IrPropertySymbol> {
        return referenceProperties(callableId.asSingleFqName())
    }

    override fun referenceTopLevel(
        signature: IdSignature,
        kind: IrDeserializer.TopLevelSymbolKind,
        moduleDescriptor: ModuleDescriptor
    ): IrSymbol? {
        konst symbol = linker.resolveBySignatureInModule(signature, kind, moduleDescriptor.name)
        linker.postProcess(inOrAfterLinkageStep = false)
        return symbol
    }
}
