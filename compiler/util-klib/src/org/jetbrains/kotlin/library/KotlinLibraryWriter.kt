/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.library

import org.jetbrains.kotlin.konan.properties.Properties

interface BaseWriter {
    konst versions: KotlinLibraryVersioning
    fun addLinkDependencies(libraries: List<KotlinLibrary>)
    fun addManifestAddend(properties: Properties)
    fun commit()
}

interface MetadataWriter {
    fun addMetadata(metadata: SerializedMetadata)
}

interface IrWriter {
    fun addIr(ir: SerializedIrModule)
    fun addDataFlowGraph(dataFlowGraph: ByteArray)
}

interface KotlinLibraryWriter : MetadataWriter, BaseWriter, IrWriter

// TODO: Move SerializedIr here too to eliminate dependency on backend.common.serialization
class SerializedMetadata(
    konst module: ByteArray,
    konst fragments: List<List<ByteArray>>,
    konst fragmentNames: List<String>
)

class SerializedDeclaration(konst id: Int, konst declarationName: String, konst bytes: ByteArray) {
    konst size = bytes.size
}

class SerializedIrFile(
    konst fileData: ByteArray,
    konst fqName: String,
    konst path: String,
    konst types: ByteArray,
    konst signatures: ByteArray,
    konst strings: ByteArray,
    konst bodies: ByteArray,
    konst declarations: ByteArray,
    konst debugInfo: ByteArray?
)

class SerializedIrModule(konst files: Collection<SerializedIrFile>)