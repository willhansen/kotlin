/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.providers.impl

import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.ThreadSafeMutableState
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.deserialization.FirBuiltinAnnotationDeserializer
import org.jetbrains.kotlin.fir.deserialization.FirConstDeserializer
import org.jetbrains.kotlin.fir.deserialization.FirDeserializationContext
import org.jetbrains.kotlin.fir.deserialization.deserializeClassToSymbol
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.builtins.BuiltInsBinaryVersion
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.serialization.deserialization.ProtoBasedClassDataFinder
import org.jetbrains.kotlin.serialization.deserialization.builtins.BuiltInSerializerProtocol
import org.jetbrains.kotlin.serialization.deserialization.getName
import java.io.InputStream

@ThreadSafeMutableState
open class FirBuiltinSymbolProvider(
    session: FirSession,
    konst moduleData: FirModuleData,
    konst kotlinScopeProvider: FirKotlinScopeProvider
) : FirSymbolProvider(session) {
    private konst syntheticFunctionInterfaceProvider = FirBuiltinSyntheticFunctionInterfaceProvider(
        session,
        moduleData,
        kotlinScopeProvider
    )

    private konst allPackageFragments = loadBuiltIns().groupBy { it.fqName }

    private fun loadBuiltIns(): List<BuiltInsPackageFragment> {
        konst classLoader = this::class.java.classLoader
        konst streamProvider = { path: String -> classLoader?.getResourceAsStream(path) ?: ClassLoader.getSystemResourceAsStream(path) }
        konst packageFqNames = StandardClassIds.builtInsPackages

        return packageFqNames.map { fqName ->
            konst resourcePath = BuiltInSerializerProtocol.getBuiltInsFilePath(fqName)
            konst inputStream = streamProvider(resourcePath) ?: throw IllegalStateException("Resource not found in classpath: $resourcePath")
            BuiltInsPackageFragment(inputStream, fqName, moduleData, kotlinScopeProvider)
        }
    }

    override fun getPackage(fqName: FqName): FqName? {
        if (allPackageFragments.containsKey(fqName)) return fqName
        return null
    }

    override fun getClassLikeSymbolByClassId(classId: ClassId): FirRegularClassSymbol? {
        return allPackageFragments[classId.packageFqName]?.firstNotNullOfOrNull {
            it.getClassLikeSymbolByClassId(classId)
        } ?: syntheticFunctionInterfaceProvider.getClassLikeSymbolByClassId(classId)
    }

    override konst symbolNamesProvider: FirSymbolNamesProvider = object : FirSymbolNamesProvider() {
        override fun getPackageNamesWithTopLevelCallables(): Set<String> =
            allPackageFragments.keys.mapTo(mutableSetOf()) { it.asString() }

        override fun getTopLevelClassifierNamesInPackage(packageFqName: FqName): Set<String> =
            allPackageFragments[packageFqName]?.flatMapTo(mutableSetOf()) { fragment ->
                fragment.classDataFinder.allClassIds.map { it.shortClassName.asString() }
            }.orEmpty()

        override fun getTopLevelCallableNamesInPackage(packageFqName: FqName): Set<Name> =
            allPackageFragments[packageFqName]?.flatMapTo(mutableSetOf()) {
                it.getTopLevelCallableNames()
            }.orEmpty()

        // This symbol provider delegates to `FirBuiltinSyntheticFunctionInterfaceProvider`, so synthetic function types can be provided.
        override konst mayHaveSyntheticFunctionTypes: Boolean get() = true

        override fun mayHaveSyntheticFunctionType(classId: ClassId): Boolean =
            syntheticFunctionInterfaceProvider.symbolNamesProvider.mayHaveSyntheticFunctionType(classId)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        allPackageFragments[packageFqName]?.flatMapTo(destination) {
            it.getTopLevelCallableSymbols(name)
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelFunctionSymbolsTo(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        getTopLevelFunctionSymbolsToByPackageFragments(destination, packageFqName, name)
    }

    protected fun getTopLevelFunctionSymbolsToByPackageFragments(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        allPackageFragments[packageFqName]?.flatMapTo(destination) {
            it.getTopLevelFunctionSymbols(name)
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelPropertySymbolsTo(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
    }

    private class BuiltInsPackageFragment(
        stream: InputStream, konst fqName: FqName, konst moduleData: FirModuleData,
        konst kotlinScopeProvider: FirKotlinScopeProvider,
    ) {

        private konst binaryVersionAndPackageFragment = BinaryVersionAndPackageFragment.createFromStream(stream)

        konst version: BuiltInsBinaryVersion get() = binaryVersionAndPackageFragment.version
        konst packageProto: ProtoBuf.PackageFragment get() = binaryVersionAndPackageFragment.packageFragment

        private konst nameResolver = NameResolverImpl(packageProto.strings, packageProto.qualifiedNames)

        konst classDataFinder = ProtoBasedClassDataFinder(packageProto, nameResolver, version) { SourceElement.NO_SOURCE }

        private konst memberDeserializer by lazy {
            FirDeserializationContext.createForPackage(
                fqName, packageProto.`package`, nameResolver, moduleData,
                FirBuiltinAnnotationDeserializer(moduleData.session),
                FirConstDeserializer(moduleData.session, BuiltInSerializerProtocol),
                containerSource = null
            ).memberDeserializer
        }

        private konst lookup = moduleData.session.firCachesFactory.createCacheWithPostCompute(
            { classId: ClassId, context: FirDeserializationContext? -> FirRegularClassSymbol(classId) to context }
        ) { classId, symbol, parentContext ->
            konst classData = classDataFinder.findClassData(classId)!!
            konst classProto = classData.classProto

            deserializeClassToSymbol(
                classId, classProto, symbol, nameResolver, moduleData.session, moduleData,
                null, kotlinScopeProvider, BuiltInSerializerProtocol, parentContext,
                null,
                origin = FirDeclarationOrigin.BuiltIns,
                this::findAndDeserializeClass,
            )
        }

        fun getClassLikeSymbolByClassId(classId: ClassId): FirRegularClassSymbol? =
            findAndDeserializeClass(classId)

        private fun findAndDeserializeClass(
            classId: ClassId,
            parentContext: FirDeserializationContext? = null,
        ): FirRegularClassSymbol? {
            konst classIdExists = classId in classDataFinder.allClassIds
            if (!classIdExists) return null
            return lookup.getValue(classId, parentContext)
        }

        fun getTopLevelCallableSymbols(name: Name): List<FirCallableSymbol<*>> {
            return getTopLevelFunctionSymbols(name)
        }

        fun getTopLevelCallableNames(): Collection<Name> =
            packageProto.`package`.functionList.map { nameResolver.getName(it.name) }

        fun getTopLevelFunctionSymbols(name: Name): List<FirNamedFunctionSymbol> {
            return packageProto.`package`.functionList.filter { nameResolver.getName(it.name) == name }.map {
                memberDeserializer.loadFunction(it).symbol
            }
        }
    }
}

private data class BinaryVersionAndPackageFragment(
    konst version: BuiltInsBinaryVersion,
    konst packageFragment: ProtoBuf.PackageFragment,
) {
    companion object {
        fun createFromStream(stream: InputStream): BinaryVersionAndPackageFragment {
            konst version = BuiltInsBinaryVersion.readFrom(stream)

            if (!version.isCompatibleWithCurrentCompilerVersion()) {
                // TODO: report a proper diagnostic
                throw UnsupportedOperationException(
                    "Kotlin built-in definition format version is not supported: " +
                            "expected ${BuiltInsBinaryVersion.INSTANCE}, actual $version. " +
                            "Please update Kotlin",
                )
            }

            konst packageFragment = ProtoBuf.PackageFragment.parseFrom(stream, BuiltInSerializerProtocol.extensionRegistry)
            return BinaryVersionAndPackageFragment(version, packageFragment)
        }
    }
}

