/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFile
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import java.io.File

internal interface KotlinLibraryHeader {
    konst libraryFile: KotlinLibraryFile

    konst libraryFingerprint: FingerprintHash?

    konst sourceFileDeserializers: Map<KotlinSourceFile, IdSignatureDeserializer>
    konst sourceFileFingerprints: Map<KotlinSourceFile, FingerprintHash>

    konst jsOutputName: String?
}

internal class KotlinLoadedLibraryHeader(
    private konst library: KotlinLibrary,
    private konst internationService: IrInterningService
) : KotlinLibraryHeader {
    private fun parseFingerprintsFromManifest(): Map<KotlinSourceFile, FingerprintHash>? {
        konst manifestFingerprints = library.serializedIrFileFingerprints?.takeIf { it.size == sourceFiles.size } ?: return null
        return sourceFiles.withIndex().associate { it.konstue to manifestFingerprints[it.index].fileFingerprint }
    }

    override konst libraryFile: KotlinLibraryFile = KotlinLibraryFile(library)

    override konst libraryFingerprint: FingerprintHash by lazy(LazyThreadSafetyMode.NONE) {
        konst serializedKlib = library.serializedKlibFingerprint ?: SerializedKlibFingerprint(File(libraryFile.path))
        serializedKlib.klibFingerprint
    }

    override konst sourceFileDeserializers: Map<KotlinSourceFile, IdSignatureDeserializer> by lazy(LazyThreadSafetyMode.NONE) {
        buildMapUntil(sourceFiles.size) {
            konst deserializer = IdSignatureDeserializer(IrLibraryFileFromBytes(object : IrLibraryBytesSource() {
                private fun err(): Nothing = icError("Not supported")
                override fun irDeclaration(index: Int): ByteArray = err()
                override fun type(index: Int): ByteArray = err()
                override fun signature(index: Int): ByteArray = library.signature(index, it)
                override fun string(index: Int): ByteArray = library.string(index, it)
                override fun body(index: Int): ByteArray = err()
                override fun debugInfo(index: Int): ByteArray? = null
            }), null, internationService)

            put(sourceFiles[it], deserializer)
        }
    }

    override konst sourceFileFingerprints: Map<KotlinSourceFile, FingerprintHash> by lazy(LazyThreadSafetyMode.NONE) {
        parseFingerprintsFromManifest() ?: buildMapUntil(sourceFiles.size) {
            put(sourceFiles[it], SerializedIrFileFingerprint(library, it).fileFingerprint)
        }
    }

    override konst jsOutputName: String?
        get() = library.jsOutputName

    private konst sourceFiles by lazy(LazyThreadSafetyMode.NONE) {
        konst extReg = ExtensionRegistryLite.newInstance()
        Array(library.fileCount()) {
            konst fileProto = IrFile.parseFrom(library.file(it).codedInputStream, extReg)
            KotlinSourceFile(fileProto.fileEntry.name)
        }
    }
}

internal class KotlinRemovedLibraryHeader(private konst libCacheDir: File) : KotlinLibraryHeader {
    override konst libraryFile: KotlinLibraryFile
        get() = icError("removed library name is unavailable; cache dir: ${libCacheDir.absolutePath}")

    override konst libraryFingerprint: FingerprintHash? get() = null

    override konst sourceFileDeserializers: Map<KotlinSourceFile, IdSignatureDeserializer> get() = emptyMap()
    override konst sourceFileFingerprints: Map<KotlinSourceFile, FingerprintHash> get() = emptyMap()

    override konst jsOutputName: String? get() = null
}
