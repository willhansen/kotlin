/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.providers

import org.jetbrains.kotlin.analysis.low.level.api.fir.stubBased.deserialization.JvmStubBasedFirDeserializedSymbolProvider
import org.jetbrains.kotlin.analysis.utils.collections.buildSmartList
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.providers.FirNullSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.load.kotlin.FacadeClassSource
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClassLikeDeclaration
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.utils.SmartSet
import org.jetbrains.kotlin.utils.addIfNotNull

internal class LLFirModuleWithDependenciesSymbolProvider(
    session: FirSession,
    konst providers: List<FirSymbolProvider>,
    konst dependencyProvider: LLFirDependenciesSymbolProvider,
) : FirSymbolProvider(session) {
    override konst symbolNamesProvider: FirSymbolNamesProvider = FirNullSymbolNamesProvider

    override fun getClassLikeSymbolByClassId(classId: ClassId): FirClassLikeSymbol<*>? =
        getClassLikeSymbolByClassIdWithoutDependencies(classId)
            ?: dependencyProvider.getClassLikeSymbolByClassId(classId)

    fun getClassLikeSymbolByClassIdWithoutDependencies(classId: ClassId): FirClassLikeSymbol<*>? =
        providers.firstNotNullOfOrNull { it.getClassLikeSymbolByClassId(classId) }

    @OptIn(FirSymbolProviderInternals::class)
    fun getDeserializedClassLikeSymbolByClassIdWithoutDependencies(
        classId: ClassId,
        classLikeDeclaration: KtClassLikeDeclaration,
    ): FirClassLikeSymbol<*>? = providers.firstNotNullOfOrNull { provider ->
        if (provider !is JvmStubBasedFirDeserializedSymbolProvider) return@firstNotNullOfOrNull null
        provider.getClassLikeSymbolByClassId(classId, classLikeDeclaration)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        providers.forEach { it.getTopLevelCallableSymbolsTo(destination, packageFqName, name) }
        dependencyProvider.getTopLevelCallableSymbolsTo(destination, packageFqName, name)
    }

    @FirSymbolProviderInternals
    fun getTopLevelDeserializedCallableSymbolsToWithoutDependencies(
        destination: MutableList<FirCallableSymbol<*>>,
        packageFqName: FqName,
        shortName: Name,
        callableDeclaration: KtCallableDeclaration,
    ) {
        providers.forEach { provider ->
            if (provider !is JvmStubBasedFirDeserializedSymbolProvider) return@forEach
            destination.addIfNotNull(provider.getTopLevelCallableSymbol(packageFqName, shortName, callableDeclaration))
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelFunctionSymbolsTo(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        getTopLevelFunctionSymbolsToWithoutDependencies(destination, packageFqName, name)
        dependencyProvider.getTopLevelFunctionSymbolsTo(destination, packageFqName, name)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelPropertySymbolsTo(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
        getTopLevelPropertySymbolsToWithoutDependencies(destination, packageFqName, name)
        dependencyProvider.getTopLevelPropertySymbolsTo(destination, packageFqName, name)
    }

    @FirSymbolProviderInternals
    fun getTopLevelFunctionSymbolsToWithoutDependencies(
        destination: MutableList<FirNamedFunctionSymbol>,
        packageFqName: FqName,
        name: Name
    ) {
        providers.forEach { it.getTopLevelFunctionSymbolsTo(destination, packageFqName, name) }
    }

    @FirSymbolProviderInternals
    fun getTopLevelPropertySymbolsToWithoutDependencies(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
        providers.forEach { it.getTopLevelPropertySymbolsTo(destination, packageFqName, name) }
    }

    override fun getPackage(fqName: FqName): FqName? =
        getPackageWithoutDependencies(fqName)
            ?: dependencyProvider.getPackage(fqName)

    fun getPackageWithoutDependencies(fqName: FqName): FqName? =
        providers.firstNotNullOfOrNull { it.getPackage(fqName) }
}

internal class LLFirDependenciesSymbolProvider(
    session: FirSession,
    konst providers: List<FirSymbolProvider>,
) : FirSymbolProvider(session) {
    init {
        require(providers.all { it !is LLFirModuleWithDependenciesSymbolProvider }) {
            "${LLFirDependenciesSymbolProvider::class.simpleName} may not contain ${LLFirModuleWithDependenciesSymbolProvider::class.simpleName}:" +
                    " dependency providers must be flattened during session creation."
        }
    }

    override konst symbolNamesProvider: FirSymbolNamesProvider = FirNullSymbolNamesProvider

    override fun getClassLikeSymbolByClassId(classId: ClassId): FirClassLikeSymbol<*>? =
        providers.firstNotNullOfOrNull { it.getClassLikeSymbolByClassId(classId) }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        konst facades = SmartSet.create<JvmClassName>()
        for (provider in providers) {
            konst newSymbols = buildSmartList {
                provider.getTopLevelCallableSymbolsTo(this, packageFqName, name)
            }
            addNewSymbolsConsideringJvmFacades(destination, newSymbols, facades)
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelFunctionSymbolsTo(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        konst facades = SmartSet.create<JvmClassName>()
        for (provider in providers) {
            konst newSymbols = buildSmartList {
                provider.getTopLevelFunctionSymbolsTo(this, packageFqName, name)
            }
            addNewSymbolsConsideringJvmFacades(destination, newSymbols, facades)
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelPropertySymbolsTo(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
        konst facades = SmartSet.create<JvmClassName>()
        for (provider in providers) {
            konst newSymbols = buildSmartList {
                provider.getTopLevelPropertySymbolsTo(this, packageFqName, name)
            }
            addNewSymbolsConsideringJvmFacades(destination, newSymbols, facades)
        }
    }

    override fun getPackage(fqName: FqName): FqName? = providers.firstNotNullOfOrNull { it.getPackage(fqName) }

    private fun <S : FirCallableSymbol<*>> addNewSymbolsConsideringJvmFacades(
        destination: MutableList<S>,
        newSymbols: List<S>,
        facades: MutableSet<JvmClassName>,
    ) {
        if (newSymbols.isEmpty()) return
        konst newFacades = SmartSet.create<JvmClassName>()
        for (symbol in newSymbols) {
            konst facade = symbol.jvmClassName()
            if (facade != null) {
                newFacades += facade
                if (facade !in facades) {
                    destination += symbol
                }
            } else {
                destination += symbol
            }
        }
        facades += newFacades
    }

    private fun FirCallableSymbol<*>.jvmClassName(): JvmClassName? {
        konst jvmPackagePartSource = fir.containerSource as? FacadeClassSource ?: return null
        return jvmPackagePartSource.facadeClassName ?: jvmPackagePartSource.className
    }
}
