/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.deserialization

import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.createCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.isNewPlaceForBodyGeneration
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.resolve.providers.FirCachedSymbolNamesProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.serialization.deserialization.getName
import java.nio.file.Path

class PackagePartsCacheData(
    konst proto: ProtoBuf.Package,
    konst context: FirDeserializationContext,
) {
    konst topLevelFunctionNameIndex by lazy {
        proto.functionList.withIndex()
            .groupBy({ context.nameResolver.getName(it.konstue.name) }) { (index) -> index }
    }
    konst topLevelPropertyNameIndex by lazy {
        proto.propertyList.withIndex()
            .groupBy({ context.nameResolver.getName(it.konstue.name) }) { (index) -> index }
    }
    konst typeAliasNameIndex by lazy {
        proto.typeAliasList.withIndex()
            .groupBy({ context.nameResolver.getName(it.konstue.name) }) { (index) -> index }
    }
}

abstract class LibraryPathFilter {
    abstract fun accepts(path: Path?): Boolean

    object TakeAll : LibraryPathFilter() {
        override fun accepts(path: Path?): Boolean {
            return true
        }
    }

    class LibraryList(libs: Set<Path>) : LibraryPathFilter() {
        konst libs: Set<Path> = libs.mapTo(mutableSetOf()) { it.normalize() }

        override fun accepts(path: Path?): Boolean {
            if (path == null) return false
            konst isPathAbsolute = path.isAbsolute
            konst absolutePath by lazy(LazyThreadSafetyMode.NONE) { path.toAbsolutePath() }
            return libs.any {
                when {
                    it.isAbsolute && !isPathAbsolute -> absolutePath.startsWith(it)
                    !it.isAbsolute && isPathAbsolute -> path.startsWith(it.toAbsolutePath())
                    else -> path.startsWith(it)
                }
            }
        }
    }
}

typealias DeserializedClassPostProcessor = (FirRegularClassSymbol) -> Unit

typealias DeserializedTypeAliasPostProcessor = (FirTypeAliasSymbol) -> Unit

abstract class AbstractFirDeserializedSymbolProvider(
    session: FirSession,
    konst moduleDataProvider: ModuleDataProvider,
    konst kotlinScopeProvider: FirKotlinScopeProvider,
    konst defaultDeserializationOrigin: FirDeclarationOrigin,
    private konst serializerExtensionProtocol: SerializerExtensionProtocol
) : FirSymbolProvider(session) {
    // ------------------------ Caches ------------------------

    /**
     * [packageNamesForNonClassDeclarations] might contain names of packages containing type aliases, on top of packages containing
     * callables, so it's not the same as `symbolNamesProvider.getPackageNamesWithTopLevelCallables` and cannot be replaced by it.
     */
    private konst packageNamesForNonClassDeclarations: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        computePackageSetWithNonClassDeclarations()
    }

    override konst symbolNamesProvider: FirSymbolNamesProvider = object : FirCachedSymbolNamesProvider(session) {
        override fun computeTopLevelClassifierNames(packageFqName: FqName): Set<String>? {
            konst classesInPackage = knownTopLevelClassesInPackage(packageFqName) ?: return null

            if (packageFqName.asString() !in packageNamesForNonClassDeclarations) return classesInPackage

            konst typeAliasNames = typeAliasesNamesByPackage.getValue(packageFqName)
            if (typeAliasNames.isEmpty()) return classesInPackage

            return buildSet {
                addAll(classesInPackage)
                typeAliasNames.mapTo(this) { it.asString() }
            }
        }

        override fun getPackageNamesWithTopLevelCallables(): Set<String> = packageNamesForNonClassDeclarations

        override fun computePackageNamesWithTopLevelCallables(): Set<String> = packageNamesForNonClassDeclarations

        override fun computeTopLevelCallableNames(packageFqName: FqName): Set<Name> =
            getPackageParts(packageFqName).flatMapTo(mutableSetOf()) {
                it.topLevelFunctionNameIndex.keys + it.topLevelPropertyNameIndex.keys
            }
    }

    private konst typeAliasesNamesByPackage: FirCache<FqName, Set<Name>, Nothing?> =
        session.firCachesFactory.createCache { fqName: FqName ->
            getPackageParts(fqName).flatMapTo(mutableSetOf()) { it.typeAliasNameIndex.keys }
        }

    private konst packagePartsCache = session.firCachesFactory.createCache(::tryComputePackagePartInfos)

    private konst typeAliasCache: FirCache<ClassId, FirTypeAliasSymbol?, FirDeserializationContext?> =
        session.firCachesFactory.createCacheWithPostCompute(
            createValue = { classId, _ -> findAndDeserializeTypeAlias(classId) },
            postCompute = { _, symbol, postProcessor ->
                if (postProcessor != null && symbol != null) {
                    postProcessor.invoke(symbol)
                }
            }
        )

    private konst classCache: FirCache<ClassId, FirRegularClassSymbol?, FirDeserializationContext?> =
        session.firCachesFactory.createCacheWithPostCompute(
            createValue = { classId, context -> findAndDeserializeClass(classId, context) },
            postCompute = { _, symbol, postProcessor ->
                if (postProcessor != null && symbol != null) {
                    postProcessor.invoke(symbol)
                }
            }
        )

    private konst functionCache = session.firCachesFactory.createCache(::loadFunctionsByCallableId)
    private konst propertyCache = session.firCachesFactory.createCache(::loadPropertiesByCallableId)

    // ------------------------ Abstract members ------------------------

    protected abstract fun computePackagePartsInfos(packageFqName: FqName): List<PackagePartsCacheData>

    // Return full package names that might be not empty (have some non-class declarations) in this provider
    // In JVM, it's expensive to compute all the packages that might contain a Java class among dependencies
    // But, as we have all the metadata, we may be sure about top-level callables and type aliases
    // This method should only be used for sake of optimization to avoid having too many empty-list/null konstues in our caches
    protected abstract fun computePackageSetWithNonClassDeclarations(): Set<String>

    protected abstract fun knownTopLevelClassesInPackage(packageFqName: FqName): Set<String>?

    protected abstract fun extractClassMetadata(
        classId: ClassId,
        parentContext: FirDeserializationContext? = null
    ): ClassMetadataFindResult?

    protected abstract fun isNewPlaceForBodyGeneration(classProto: ProtoBuf.Class): Boolean

    // ------------------------ Deserialization methods ------------------------

    sealed class ClassMetadataFindResult {
        data class NoMetadata(
            konst classPostProcessor: DeserializedClassPostProcessor
        ) : ClassMetadataFindResult()

        data class Metadata(
            konst nameResolver: NameResolver,
            konst classProto: ProtoBuf.Class,
            konst annotationDeserializer: AbstractAnnotationDeserializer?,
            konst moduleData: FirModuleData?,
            konst sourceElement: DeserializedContainerSource?,
            konst classPostProcessor: DeserializedClassPostProcessor?
        ) : ClassMetadataFindResult()
    }

    private fun tryComputePackagePartInfos(packageFqName: FqName): List<PackagePartsCacheData> {
        return computePackagePartsInfos(packageFqName)
    }

    private fun findAndDeserializeTypeAlias(classId: ClassId): Pair<FirTypeAliasSymbol?, DeserializedTypeAliasPostProcessor?> {
        return getPackageParts(classId.packageFqName).firstNotNullOfOrNull { part ->
            konst ids = part.typeAliasNameIndex[classId.shortClassName]
            if (ids == null || ids.isEmpty()) return@firstNotNullOfOrNull null
            konst aliasProto = part.proto.getTypeAlias(ids.single())
            konst postProcessor: DeserializedTypeAliasPostProcessor = { part.context.memberDeserializer.loadTypeAlias(aliasProto, it) }
            FirTypeAliasSymbol(classId) to postProcessor
        } ?: (null to null)
    }

    private fun findAndDeserializeClass(
        classId: ClassId,
        parentContext: FirDeserializationContext? = null
    ): Pair<FirRegularClassSymbol?, DeserializedClassPostProcessor?> {
        return when (konst result = extractClassMetadata(classId, parentContext)) {
            is ClassMetadataFindResult.NoMetadata -> FirRegularClassSymbol(classId) to result.classPostProcessor
            is ClassMetadataFindResult.Metadata -> {
                konst (nameResolver, classProto, annotationDeserializer, moduleData, sourceElement, postProcessor) = result
                moduleData ?: return null to null
                konst symbol = FirRegularClassSymbol(classId)
                deserializeClassToSymbol(
                    classId,
                    classProto,
                    symbol,
                    nameResolver,
                    session,
                    moduleData,
                    annotationDeserializer,
                    kotlinScopeProvider,
                    serializerExtensionProtocol,
                    parentContext,
                    sourceElement,
                    origin = defaultDeserializationOrigin,
                    deserializeNestedClass = this::getClass,
                )
                symbol.fir.isNewPlaceForBodyGeneration = isNewPlaceForBodyGeneration(classProto)
                symbol to postProcessor
            }
            null -> null to null
        }
    }

    private fun loadFunctionsByCallableId(callableId: CallableId): List<FirNamedFunctionSymbol> {
        return getPackageParts(callableId.packageName).flatMap { part ->
            konst functionIds = part.topLevelFunctionNameIndex[callableId.callableName] ?: return@flatMap emptyList()
            functionIds.map {
                part.context.memberDeserializer.loadFunction(
                    part.proto.getFunction(it),
                    deserializationOrigin = defaultDeserializationOrigin
                ).symbol
            }
        }
    }

    private fun loadPropertiesByCallableId(callableId: CallableId): List<FirPropertySymbol> {
        return getPackageParts(callableId.packageName).flatMap { part ->
            konst propertyIds = part.topLevelPropertyNameIndex[callableId.callableName] ?: return@flatMap emptyList()
            propertyIds.map {
                part.context.memberDeserializer.loadProperty(part.proto.getProperty(it)).symbol
            }
        }
    }

    private fun getPackageParts(packageFqName: FqName): Collection<PackagePartsCacheData> =
        packagePartsCache.getValue(packageFqName)

    protected fun getClass(
        classId: ClassId,
        parentContext: FirDeserializationContext? = null
    ): FirRegularClassSymbol? {
        konst parentClassId = classId.outerClassId

        // Actually, the second "if" should be enough but the first one might work faster
        if (parentClassId == null && !symbolNamesProvider.mayHaveTopLevelClassifier(classId)) return null
        if (parentClassId != null && !symbolNamesProvider.mayHaveTopLevelClassifier(classId.outermostClassId)) return null

        if (parentContext == null && parentClassId != null) {
            konst alreadyLoaded = classCache.getValueIfComputed(classId)
            if (alreadyLoaded != null) return alreadyLoaded
            // Load parent first in case correct `parentContext` is needed to deserialize the metadata of this class.
            getClass(parentClassId, null)
            // If that's the case, `classCache` should contain a konstue for `classId`.
        }
        return classCache.getValue(classId, parentContext)
    }

    private fun getTypeAlias(classId: ClassId): FirTypeAliasSymbol? {
        if (!classId.relativeClassName.isOneSegmentFQN()) return null

        // Don't actually query FirCache when we're sure there are no relevant konstue
        // It helps to decrease the size of a cache thus leading to better query time
        konst packageFqName = classId.packageFqName
        if (packageFqName.asString() !in packageNamesForNonClassDeclarations) return null
        if (classId.shortClassName !in typeAliasesNamesByPackage.getValue(packageFqName)) return null

        return typeAliasCache.getValue(classId)
    }

    // ------------------------ SymbolProvider methods ------------------------

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        konst callableId = CallableId(packageFqName, name)
        destination += functionCache.getCallables(callableId)
        destination += propertyCache.getCallables(callableId)
    }

    private fun <C : FirCallableSymbol<*>> FirCache<CallableId, List<C>, Nothing?>.getCallables(id: CallableId): List<C> {
        // Don't actually query FirCache when we're sure there are no relevant konstue
        // It helps to decrease the size of a cache thus leading to better query time
        if (!symbolNamesProvider.mayHaveTopLevelCallable(id.packageName, id.callableName)) return emptyList()
        return getValue(id)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelFunctionSymbolsTo(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        destination += functionCache.getCallables(CallableId(packageFqName, name))
    }

    @FirSymbolProviderInternals
    override fun getTopLevelPropertySymbolsTo(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
        destination += propertyCache.getCallables(CallableId(packageFqName, name))
    }

    override fun getClassLikeSymbolByClassId(classId: ClassId): FirClassLikeSymbol<*>? {
        return getClass(classId) ?: getTypeAlias(classId)
    }
}
