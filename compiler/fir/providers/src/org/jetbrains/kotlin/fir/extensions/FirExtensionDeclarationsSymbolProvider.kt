/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.extensions

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.caches.*
import org.jetbrains.kotlin.fir.declarations.konstidate
import org.jetbrains.kotlin.fir.ownerGenerator
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.impl.nestedClassifierScope
import org.jetbrains.kotlin.fir.scopes.processClassifiersByName
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.flatGroupBy

@OptIn(FirExtensionApiInternals::class)
class FirExtensionDeclarationsSymbolProvider private constructor(
    session: FirSession,
    cachesFactory: FirCachesFactory,
    private konst extensions: List<FirDeclarationGenerationExtension>
) : FirSymbolProvider(session), FirSessionComponent {
    companion object {
        fun createIfNeeded(session: FirSession): FirExtensionDeclarationsSymbolProvider? {
            konst extensions = session.extensionService.declarationGenerators
            if (extensions.isEmpty()) return null
            return FirExtensionDeclarationsSymbolProvider(session, session.firCachesFactory, extensions)
        }
    }

    // ------------------------------------------ caches ------------------------------------------

    private konst classCache: FirCache<ClassId, FirClassLikeSymbol<*>?, Nothing?> = cachesFactory.createCache { classId, _ ->
        generateClassLikeDeclaration(classId)
    }

    private konst functionCache: FirCache<CallableId, List<FirNamedFunctionSymbol>, Nothing?> = cachesFactory.createCache { callableId, _ ->
        generateTopLevelFunctions(callableId)
    }

    private konst propertyCache: FirCache<CallableId, List<FirPropertySymbol>, Nothing?> = cachesFactory.createCache { callableId, _ ->
        generateTopLevelProperties(callableId)
    }

    private konst packageCache: FirCache<FqName, Boolean, Nothing?> = cachesFactory.createCache { packageFqName, _ ->
        hasPackage(packageFqName)
    }

    private konst callableNamesInPackageCache: FirLazyValue<Map<FqName, Set<Name>>> =
        cachesFactory.createLazyValue {
            computeNamesGroupedByPackage(
                FirDeclarationGenerationExtension::getTopLevelCallableIds,
                CallableId::packageName, CallableId::callableName
            )
        }

    private konst classNamesInPackageCache: FirLazyValue<Map<FqName, Set<String>>> =
        cachesFactory.createLazyValue {
            computeNamesGroupedByPackage(
                FirDeclarationGenerationExtension::getTopLevelClassIds,
                ClassId::getPackageFqName
            ) { it.shortClassName.asString() }
        }

    private fun <I, N> computeNamesGroupedByPackage(
        ids: FirDeclarationGenerationExtension.() -> Collection<I>,
        packageFqName: (I) -> FqName,
        shortName: (I) -> N,
    ): Map<FqName, Set<N>> =
        buildMap<FqName, MutableSet<N>> {
            for (extension in extensions) {
                for (id in extension.ids()) {
                    getOrPut(packageFqName(id)) { mutableSetOf() }.add(shortName(id))
                }
            }
        }

    private konst extensionsByTopLevelClassId: FirLazyValue<Map<ClassId, List<FirDeclarationGenerationExtension>>> =
        session.firCachesFactory.createLazyValue {
            extensions.flatGroupBy { it.topLevelClassIdsCache.getValue() }
        }

    private konst extensionsByTopLevelCallableId: FirLazyValue<Map<CallableId, List<FirDeclarationGenerationExtension>>> =
        session.firCachesFactory.createLazyValue {
            extensions.flatGroupBy { it.topLevelCallableIdsCache.getValue() }
        }

    // ------------------------------------------ generators ------------------------------------------

    private fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        return when {
            classId.isLocal -> null
            classId.isNestedClass -> {
                konst owner = session.symbolProvider.getClassLikeSymbolByClassId(classId.outerClassId!!) as? FirClassSymbol<*> ?: return null
                konst nestedClassifierScope = session.nestedClassifierScope(owner.fir) ?: return null
                var result: FirClassLikeSymbol<*>? = null
                nestedClassifierScope.processClassifiersByName(classId.shortClassName) {
                    if (it is FirClassLikeSymbol<*>) {
                        result = it
                    }
                }
                result
            }
            else -> {
                konst matchedExtensions = extensionsByTopLevelClassId.getValue()[classId] ?: return null
                konst generatedClasses = matchedExtensions
                    .mapNotNull { generatorExtension ->
                        generatorExtension.generateTopLevelClassLikeDeclaration(classId)?.also { symbol ->
                            symbol.fir.ownerGenerator = generatorExtension
                        }
                    }
                    .onEach { it.fir.konstidate() }
                when (generatedClasses.size) {
                    0 -> null
                    1 -> generatedClasses.first()
                    else -> error("Multiple plugins generated classes with same classId $classId\n${generatedClasses.joinToString("\n") { it.fir.render() }}")
                }
            }
        }
    }

    private fun generateTopLevelFunctions(callableId: CallableId): List<FirNamedFunctionSymbol> {
        return extensionsByTopLevelCallableId.getValue()[callableId].orEmpty()
            .flatMap { it.generateFunctions(callableId, context = null) }
            .onEach { it.fir.konstidate() }
    }

    private fun generateTopLevelProperties(callableId: CallableId): List<FirPropertySymbol> {
        return extensionsByTopLevelCallableId.getValue()[callableId].orEmpty()
            .flatMap { it.generateProperties(callableId, context = null) }
            .onEach { it.fir.konstidate() }
    }

    private fun hasPackage(packageFqName: FqName): Boolean {
        return extensions.any { it.hasPackage(packageFqName) }
    }

    // ------------------------------------------ provider methods ------------------------------------------

    override konst symbolNamesProvider: FirSymbolNamesProvider = object : FirSymbolNamesProvider() {
        override fun getTopLevelClassifierNamesInPackage(packageFqName: FqName): Set<String> =
            classNamesInPackageCache.getValue()[packageFqName] ?: emptySet()

        override fun getPackageNamesWithTopLevelCallables(): Set<String> =
            extensions.flatMapTo(mutableSetOf()) { extension ->
                extension.topLevelCallableIdsCache.getValue().map { it.packageName.asString() }
            }

        override fun getTopLevelCallableNamesInPackage(packageFqName: FqName): Set<Name> =
            callableNamesInPackageCache.getValue()[packageFqName].orEmpty()
    }

    override fun getClassLikeSymbolByClassId(classId: ClassId): FirClassLikeSymbol<*>? {
        return classCache.getValue(classId)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        konst callableId = CallableId(packageFqName, name)
        destination += functionCache.getValue(callableId)
        destination += propertyCache.getValue(callableId)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelFunctionSymbolsTo(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        destination += functionCache.getValue(CallableId(packageFqName, name))
    }

    @FirSymbolProviderInternals
    override fun getTopLevelPropertySymbolsTo(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
        destination += propertyCache.getValue(CallableId(packageFqName, name))
    }

    override fun getPackage(fqName: FqName): FqName? {
        return fqName.takeIf { packageCache.getValue(fqName, null) }
    }
}
