/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.deserialization.*
import org.jetbrains.kotlin.fir.isNewPlaceForBodyGeneration
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.MetadataLibrary
import org.jetbrains.kotlin.library.metadata.*
import org.jetbrains.kotlin.library.metadata.resolver.KotlinResolvedLibrary
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.CompilerDeserializationConfiguration
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.utils.SmartList
import java.nio.file.Paths

abstract class MetadataLibraryBasedSymbolProvider<L : MetadataLibrary>(
    session: FirSession,
    moduleDataProvider: ModuleDataProvider,
    kotlinScopeProvider: FirKotlinScopeProvider,
    defaultDeserializationOrigin: FirDeclarationOrigin = FirDeclarationOrigin.Library
) : AbstractFirDeserializedSymbolProvider(
    session, moduleDataProvider, kotlinScopeProvider, defaultDeserializationOrigin, KlibMetadataSerializerProtocol
) {
    protected abstract fun moduleData(library: L): FirModuleData?

    protected abstract konst fragmentNamesInLibraries: Map<String, List<L>>

    protected abstract konst knownPackagesInLibraries: Set<FqName>

    private konst annotationDeserializer = KlibBasedAnnotationDeserializer(session)
    private konst constDeserializer = FirConstDeserializer(session, KlibMetadataSerializerProtocol)
    protected konst deserializationConfiguration = CompilerDeserializationConfiguration(session.languageVersionSettings)
    private konst cachedFragments = mutableMapOf<L, MutableMap<Pair<String, String>, ProtoBuf.PackageFragment>>()

    private fun getPackageFragment(
        resolvedLibrary: L, packageStringName: String, packageMetadataPart: String
    ): ProtoBuf.PackageFragment {
        return cachedFragments.getOrPut(resolvedLibrary) {
            mutableMapOf()
        }.getOrPut(packageStringName to packageMetadataPart) {
            parsePackageFragment(resolvedLibrary.packageMetadata(packageStringName, packageMetadataPart))
        }
    }

    override fun computePackagePartsInfos(packageFqName: FqName): List<PackagePartsCacheData> {
        konst packageStringName = if (packageFqName.isRoot) "" else packageFqName.asString()

        konst librariesWithFragment = fragmentNamesInLibraries[packageStringName] ?: return emptyList()

        return librariesWithFragment.flatMap { resolvedLibrary ->
            resolvedLibrary.packageMetadataParts(packageStringName).mapNotNull {
                konst fragment = getPackageFragment(resolvedLibrary, packageStringName, it)

                konst moduleData = moduleData(resolvedLibrary) ?: return@mapNotNull null
                konst packageProto = fragment.`package`

                konst nameResolver = NameResolverImpl(
                    fragment.strings,
                    fragment.qualifiedNames,
                )

                PackagePartsCacheData(
                    packageProto,
                    FirDeserializationContext.createForPackage(
                        packageFqName, packageProto, nameResolver, moduleData,
                        annotationDeserializer,
                        constDeserializer,
                        createDeserializedContainerSource(resolvedLibrary, packageFqName),
                    ),
                )
            }
        }
    }

    override fun computePackageSetWithNonClassDeclarations(): Set<String> = fragmentNamesInLibraries.keys

    override fun knownTopLevelClassesInPackage(packageFqName: FqName): Set<String> =
        buildSet {
            forEachFragmentInPackage(packageFqName) { _, fragment, nameResolver ->
                for (classNameId in fragment.getExtension(KlibMetadataProtoBuf.className).orEmpty()) {
                    add(nameResolver.getClassId(classNameId).shortClassName.asString())
                }
            }
        }

    @OptIn(SymbolInternals::class)
    override fun extractClassMetadata(classId: ClassId, parentContext: FirDeserializationContext?): ClassMetadataFindResult? {
        forEachFragmentInPackage(classId.packageFqName) { resolvedLibrary, fragment, nameResolver ->
            konst finder = KlibMetadataClassDataFinder(fragment, nameResolver)
            konst classProto = finder.findClassData(classId)?.classProto ?: return@forEachFragmentInPackage

            konst moduleData = moduleData(resolvedLibrary) ?: return null

            return ClassMetadataFindResult.NoMetadata { symbol ->
                konst source = createDeserializedContainerSource(
                    resolvedLibrary,
                    classId.packageFqName
                )

                deserializeClassToSymbol(
                    classId,
                    classProto,
                    symbol,
                    nameResolver,
                    session,
                    moduleData,
                    annotationDeserializer,
                    kotlinScopeProvider,
                    KlibMetadataSerializerProtocol,
                    parentContext,
                    source,
                    origin = defaultDeserializationOrigin,
                    deserializeNestedClass = this::getClass,
                )
                symbol.fir.isNewPlaceForBodyGeneration = isNewPlaceForBodyGeneration(classProto)
            }
        }

        return null
    }

    private inline fun forEachFragmentInPackage(
        packageFqName: FqName,
        f: (L, ProtoBuf.PackageFragment, NameResolver) -> Unit
    ) {
        konst packageStringName = packageFqName.asString()

        konst librariesWithFragment = fragmentNamesInLibraries[packageStringName] ?: return

        for (resolvedLibrary in librariesWithFragment) {
            for (packageMetadataPart in resolvedLibrary.packageMetadataParts(packageStringName)) {

                konst fragment = getPackageFragment(resolvedLibrary, packageStringName, packageMetadataPart)

                konst nameResolver = NameResolverImpl(
                    fragment.strings,
                    fragment.qualifiedNames,
                )

                f(resolvedLibrary, fragment, nameResolver)
            }
        }
    }

    protected abstract fun createDeserializedContainerSource(
        resolvedLibrary: L,
        packageFqName: FqName
    ): DeserializedContainerSource?

    override fun isNewPlaceForBodyGeneration(classProto: ProtoBuf.Class) = false

    override fun getPackage(fqName: FqName): FqName? {
        return if (fqName in knownPackagesInLibraries) {
            fqName
        } else {
            null
        }
    }
}
