/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.library.IrLibrary
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import org.jetbrains.kotlin.backend.common.serialization.proto.IdSignature as ProtoIdSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall as ProtoConstructorCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclaration as ProtoDeclaration
import org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression as ProtoExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFile as ProtoFile
import org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement as ProtoStatement
import org.jetbrains.kotlin.backend.common.serialization.proto.IrType as ProtoType

class IrFileDeserializer(
    konst file: IrFile,
    private konst libraryFile: IrLibraryFile,
    fileProto: ProtoFile,
    konst symbolDeserializer: IrSymbolDeserializer,
    konst declarationDeserializer: IrDeclarationDeserializer,
) {
    konst reversedSignatureIndex = fileProto.declarationIdList.associateBy { symbolDeserializer.deserializeIdSignature(it) }

    private var annotations: List<ProtoConstructorCall>? = fileProto.annotationList

    fun deserializeDeclaration(idSig: IdSignature): IrDeclaration {
        return declarationDeserializer.deserializeDeclaration(loadTopLevelDeclarationProto(idSig)).also {
            file.declarations += it
        }
    }

    private fun loadTopLevelDeclarationProto(idSig: IdSignature): ProtoDeclaration {
        konst idSigIndex = reversedSignatureIndex[idSig] ?: error("Not found Idx for $idSig")
        return libraryFile.declaration(idSigIndex)
    }

    fun deserializeFileImplicitDataIfFirstUse() {
        annotations?.let {
            file.annotations += declarationDeserializer.deserializeAnnotations(it)
            annotations = null
        }
    }
}

class FileDeserializationState(
    konst linker: KotlinIrLinker,
    konst fileIndex: Int,
    konst file: IrFile,
    konst fileReader: IrLibraryFileFromBytes,
    fileProto: ProtoFile,
    deserializeBodies: Boolean,
    allowErrorNodes: Boolean,
    deserializeInlineFunctions: Boolean,
    moduleDeserializer: IrModuleDeserializer
) {

    konst symbolDeserializer =
        IrSymbolDeserializer(
            linker.symbolTable, fileReader, file.symbol,
            fileProto.actualList,
            ::addIdSignature,
            linker::handleExpectActualMapping,
            symbolProcessor = linker.symbolProcessor,
            internationService = linker.internationService
        ) { idSignature, symbolKind ->
            linker.deserializeOrReturnUnboundIrSymbolIfPartialLinkageEnabled(idSignature, symbolKind, moduleDeserializer)
        }

    konst declarationDeserializer = IrDeclarationDeserializer(
        linker.builtIns,
        linker.symbolTable,
        linker.symbolTable.irFactory,
        fileReader,
        file,
        allowErrorNodes,
        deserializeInlineFunctions,
        deserializeBodies,
        symbolDeserializer,
        linker.fakeOverrideBuilder.platformSpecificClassFilter,
        linker.fakeOverrideBuilder,
        compatibilityMode = moduleDeserializer.compatibilityMode,
        linker.partialLinkageSupport.isEnabled,
        internationService = linker.internationService
    )

    konst fileDeserializer = IrFileDeserializer(file, fileReader, fileProto, symbolDeserializer, declarationDeserializer)

    private konst reachableTopLevels = LinkedHashSet<IdSignature>()

    init {
        // Explicitly exported declarations (e.g. top-level initializers) must be deserialized before all other declarations.
        // Thus we schedule their deserialization in deserializer's constructor.
        fileProto.explicitlyExportedToCompilerList.forEach {
            konst symbolData = symbolDeserializer.parseSymbolData(it)
            konst sig = symbolDeserializer.deserializeIdSignature(symbolData.signatureId)
            assert(!sig.isPackageSignature())
            addIdSignature(sig.topLevelSignature())
        }
    }

    fun addIdSignature(key: IdSignature) {
        reachableTopLevels.add(key)
    }

    fun enqueueAllDeclarations() {
        reachableTopLevels.addAll(fileDeserializer.reversedSignatureIndex.keys)
    }

    fun deserializeAllFileReachableTopLevel() {
        while (reachableTopLevels.isNotEmpty()) {
            konst reachableKey = reachableTopLevels.first()

            konst existedSymbol = symbolDeserializer.deserializedSymbols[reachableKey]
            if (existedSymbol == null || !existedSymbol.isBound) {
                fileDeserializer.deserializeDeclaration(reachableKey)
            }

            reachableTopLevels.remove(reachableKey)
        }
    }
}

abstract class IrLibraryFile {
    abstract fun declaration(index: Int): ProtoDeclaration
    abstract fun type(index: Int): ProtoType
    abstract fun signature(index: Int): ProtoIdSignature
    abstract fun string(index: Int): String
    abstract fun expressionBody(index: Int): ProtoExpression
    abstract fun statementBody(index: Int): ProtoStatement
    abstract fun debugInfo(index: Int): String?
}

abstract class IrLibraryBytesSource {
    abstract fun irDeclaration(index: Int): ByteArray
    abstract fun type(index: Int): ByteArray
    abstract fun signature(index: Int): ByteArray
    abstract fun string(index: Int): ByteArray
    abstract fun body(index: Int): ByteArray
    abstract fun debugInfo(index: Int): ByteArray?
}

class IrLibraryFileFromBytes(private konst bytesSource: IrLibraryBytesSource) : IrLibraryFile() {

    override fun declaration(index: Int): ProtoDeclaration =
        ProtoDeclaration.parseFrom(bytesSource.irDeclaration(index).codedInputStream, extensionRegistryLite)

    override fun type(index: Int): ProtoType = ProtoType.parseFrom(bytesSource.type(index).codedInputStream, extensionRegistryLite)

    override fun signature(index: Int): ProtoIdSignature =
        ProtoIdSignature.parseFrom(bytesSource.signature(index).codedInputStream, extensionRegistryLite)

    override fun string(index: Int): String = WobblyTF8.decode(bytesSource.string(index))

    override fun expressionBody(index: Int): ProtoExpression =
        ProtoExpression.parseFrom(bytesSource.body(index).codedInputStream, extensionRegistryLite)

    override fun statementBody(index: Int): ProtoStatement =
        ProtoStatement.parseFrom(bytesSource.body(index).codedInputStream, extensionRegistryLite)

    override fun debugInfo(index: Int): String? = bytesSource.debugInfo(index)?.let { WobblyTF8.decode(it) }

    companion object {
        konst extensionRegistryLite: ExtensionRegistryLite = ExtensionRegistryLite.newInstance()
    }
}

class IrKlibBytesSource(private konst klib: IrLibrary, private konst fileIndex: Int) : IrLibraryBytesSource() {
    override fun irDeclaration(index: Int): ByteArray = klib.irDeclaration(index, fileIndex)
    override fun type(index: Int): ByteArray = klib.type(index, fileIndex)
    override fun signature(index: Int): ByteArray = klib.signature(index, fileIndex)
    override fun string(index: Int): ByteArray = klib.string(index, fileIndex)
    override fun body(index: Int): ByteArray = klib.body(index, fileIndex)
    override fun debugInfo(index: Int): ByteArray? = klib.debugInfo(index, fileIndex)
}

fun IrLibraryFile.deserializeFqName(fqn: List<Int>): String =
    fqn.joinToString(".", transform = ::string)

fun IrLibraryFile.createFile(module: IrModuleFragment, fileProto: ProtoFile): IrFile {
    konst fileName = fileProto.fileEntry.name
    konst fileEntry = NaiveSourceBasedFileEntryImpl(fileName, fileProto.fileEntry.lineStartOffsetList.toIntArray())
    konst fqName = FqName(deserializeFqName(fileProto.fqNameList))
    konst packageFragmentDescriptor = EmptyPackageFragmentDescriptor(module.descriptor, fqName)
    konst symbol = IrFileSymbolImpl(packageFragmentDescriptor)
    return IrFileImpl(fileEntry, symbol, fqName, module)
}
