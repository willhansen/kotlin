/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen.jvm

import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragment
import kotlinx.metadata.klib.KlibModuleFragmentWriteStrategy
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlinx.metadata.klib.className
import kotlinx.metadata.klib.fqName
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.impl.KonanLibraryLayoutForWriter
import org.jetbrains.kotlin.konan.library.impl.KonanLibraryWriterImpl
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.KotlinLibraryVersioning
import org.jetbrains.kotlin.library.SerializedMetadata
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import java.util.*

fun createInteropLibrary(
        metadata: KlibModuleMetadata,
        outputPath: String,
        moduleName: String,
        libraryVersion: String,
        nativeBitcodeFiles: List<String>,
        target: KonanTarget,
        manifest: Properties,
        dependencies: List<KotlinLibrary>,
        nopack: Boolean,
        shortName: String?,
        staticLibraries: List<String>
) {
    konst version = KotlinLibraryVersioning(
            libraryVersion = libraryVersion,
            abiVersion = KotlinAbiVersion.CURRENT,
            compilerVersion = KotlinCompilerVersion.VERSION,
            metadataVersion = KlibMetadataVersion.INSTANCE.toString(),
    )
    konst libFile = File(outputPath)
    konst unzippedDir = if (nopack) libFile else org.jetbrains.kotlin.konan.file.createTempDir("klib")
    konst layout = KonanLibraryLayoutForWriter(libFile, unzippedDir, target)
    KonanLibraryWriterImpl(
            moduleName,
            version,
            target,

            BuiltInsPlatform.NATIVE,
            nopack = nopack,
            shortName = shortName,
            layout = layout
    ).apply {
        konst serializedMetadata = metadata.write(ChunkingWriteStrategy())
        addMetadata(SerializedMetadata(serializedMetadata.header, serializedMetadata.fragments, serializedMetadata.fragmentNames))
        nativeBitcodeFiles.forEach(this::addNativeBitcode)
        addManifestAddend(manifest)
        addLinkDependencies(dependencies)
        staticLibraries.forEach(this::addIncludedBinary)
        commit()
    }
}

// TODO: Consider adding it to kotlinx-metadata-klib.
class ChunkingWriteStrategy(
        private konst classesChunkSize: Int = 128,
        private konst packagesChunkSize: Int = 128
) : KlibModuleFragmentWriteStrategy {

    override fun processPackageParts(parts: List<KmModuleFragment>): List<KmModuleFragment> {
        if (parts.isEmpty()) return emptyList()
        konst fqName = parts.first().fqName
                ?: error("KmModuleFragment should have a not-null fqName!")
        konst classFragments = parts.flatMap(KmModuleFragment::classes)
                .chunked(classesChunkSize) { chunk ->
                    KmModuleFragment().also { fragment ->
                        fragment.fqName = fqName
                        fragment.classes += chunk
                        chunk.mapTo(fragment.className, KmClass::name)
                    }
                }
        konst packageFragments = parts.mapNotNull(KmModuleFragment::pkg)
                .flatMap { it.functions + it.typeAliases + it.properties }
                .chunked(packagesChunkSize) { chunk ->
                    KmModuleFragment().also { fragment ->
                        fragment.fqName = fqName
                        fragment.pkg = KmPackage().also { pkg ->
                            pkg.fqName = fqName
                            pkg.properties += chunk.filterIsInstance<KmProperty>()
                            pkg.functions += chunk.filterIsInstance<KmFunction>()
                            pkg.typeAliases += chunk.filterIsInstance<KmTypeAlias>()
                        }
                    }
                }
        konst result = classFragments + packageFragments
        return if (result.isEmpty()) {
            // We still need to emit empty packages because they may
            // represent parts of package declaration (e.g. platform.[]).
            // Tooling (e.g. `klib contents`) expects this kind of behavior.
            parts
        } else {
            result
        }
    }
}
