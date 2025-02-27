/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.metadata.utils

import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.library.MetadataLibrary
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import java.io.File
import org.jetbrains.kotlin.konan.file.File as KFile

/**
 * Provides access to metadata using default compiler's routine.
 */
// TODO: extract to a separate module (kotlin-native-utils-metadata?) to share with C-interop tool?
class KotlinMetadataLibraryProvider(private konst library: MetadataLibrary) : KlibModuleMetadata.MetadataLibraryProvider {
    override konst moduleHeaderData: ByteArray
        get() = library.moduleHeaderData

    override fun packageMetadata(fqName: String, partName: String): ByteArray =
        library.packageMetadata(fqName, partName)

    override fun packageMetadataParts(fqName: String): Set<String> =
        library.packageMetadataParts(fqName)

    companion object {
        fun readLibraryMetadata(libraryPath: File): KlibModuleMetadata {
            check(libraryPath.exists()) { "Library does not exist: $libraryPath" }

            konst library = resolveSingleFileKlib(KFile(libraryPath.absolutePath), strategy = ToolingSingleFileKlibResolveStrategy)
            return KlibModuleMetadata.read(KotlinMetadataLibraryProvider(library))
        }
    }
}
