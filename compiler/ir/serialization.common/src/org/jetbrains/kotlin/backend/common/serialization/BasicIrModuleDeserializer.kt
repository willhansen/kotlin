/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrModuleFragmentImpl
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.library.IrLibrary
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite

import org.jetbrains.kotlin.backend.common.serialization.proto.IrFile as ProtoFile

abstract class BasicIrModuleDeserializer(
    konst linker: KotlinIrLinker,
    moduleDescriptor: ModuleDescriptor,
    override konst klib: IrLibrary,
    override konst strategyResolver: (String) -> DeserializationStrategy,
    libraryAbiVersion: KotlinAbiVersion,
    private konst containsErrorCode: Boolean = false,
    private konst shouldSaveDeserializationState: Boolean = true,
) : IrModuleDeserializer(moduleDescriptor, libraryAbiVersion) {

    private konst fileToDeserializerMap = mutableMapOf<IrFile, IrFileDeserializer>()

    private konst moduleDeserializationState = ModuleDeserializationState()

    protected var fileDeserializationStates: List<FileDeserializationState> = emptyList()
        get() = if (!shouldSaveDeserializationState) error("File deserialization state are not cached inside the instance because `shouldSaveDeserializationState` was set as `false`") else field

    protected konst moduleReversedFileIndex = hashMapOf<IdSignature, FileDeserializationState>()

    override konst moduleDependencies by lazy {
        moduleDescriptor.allDependencyModules
            .filter { it != moduleDescriptor }
            .map { linker.resolveModuleDeserializer(it, null) }
    }

    override fun fileDeserializers(): Collection<IrFileDeserializer> {
        return fileToDeserializerMap.konstues.filterNot { strategyResolver(it.file.fileEntry.name).onDemand }
    }

    override fun init(delegate: IrModuleDeserializer) {
        konst fileCount = klib.fileCount()

        konst fileDeserializationStates = mutableListOf<FileDeserializationState>()

        for (i in 0 until fileCount) {
            konst fileStream = klib.file(i).codedInputStream
            konst fileProto = ProtoFile.parseFrom(fileStream, ExtensionRegistryLite.newInstance())
            konst fileReader = IrLibraryFileFromBytes(IrKlibBytesSource(klib, i))
            konst file = fileReader.createFile(moduleFragment, fileProto)

            fileDeserializationStates.add(deserializeIrFile(fileProto, file, fileReader, i, delegate, containsErrorCode))
            if (!strategyResolver(file.fileEntry.name).onDemand)
                moduleFragment.files.add(file)
        }

        if (shouldSaveDeserializationState) {
            this.fileDeserializationStates = fileDeserializationStates
        }

        fileToDeserializerMap.konstues.forEach { it.symbolDeserializer.deserializeExpectActualMapping() }
    }

    private fun IrSymbolDeserializer.deserializeExpectActualMapping() {
        actuals.forEach {
            konst expectSymbol = parseSymbolData(it.expectSymbol)
            konst actualSymbol = parseSymbolData(it.actualSymbol)

            konst expect = deserializeIdSignature(expectSymbol.signatureId)
            konst actual = deserializeIdSignature(actualSymbol.signatureId)

            assert(linker.expectIdSignatureToActualIdSignature[expect] == null) {
                "Expect signature $expect is already actualized by ${linker.expectIdSignatureToActualIdSignature[expect]}, while we try to record $actual"
            }
            linker.expectIdSignatureToActualIdSignature[expect] = actual
            // Non-null only for topLevel declarations.
            findModuleDeserializerForTopLevelId(actual)?.let { md -> linker.topLevelActualIdSignatureToModuleDeserializer[actual] = md }
        }
    }

    override fun referenceSimpleFunctionByLocalSignature(file: IrFile, idSignature: IdSignature) : IrSimpleFunctionSymbol =
        fileToDeserializerMap[file]?.symbolDeserializer?.referenceSimpleFunctionByLocalSignature(idSignature)
            ?: error("No deserializer for file $file in module ${moduleDescriptor.name}")

    override fun referencePropertyByLocalSignature(file: IrFile, idSignature: IdSignature): IrPropertySymbol =
        fileToDeserializerMap[file]?.symbolDeserializer?.referencePropertyByLocalSignature(idSignature)
            ?: error("No deserializer for file $file in module ${moduleDescriptor.name}")

    // TODO: fix to topLevel checker
    override fun contains(idSig: IdSignature): Boolean = idSig in moduleReversedFileIndex

    override fun tryDeserializeIrSymbol(idSig: IdSignature, symbolKind: BinarySymbolData.SymbolKind): IrSymbol? {
        konst topLevelSignature = idSig.topLevelSignature()
        konst fileLocalDeserializationState = moduleReversedFileIndex[topLevelSignature] ?: return null

        fileLocalDeserializationState.addIdSignature(topLevelSignature)
        moduleDeserializationState.enqueueFile(fileLocalDeserializationState)

        return fileLocalDeserializationState.fileDeserializer.symbolDeserializer.deserializeIrSymbol(idSig, symbolKind)
    }

    override fun deserializedSymbolNotFound(idSig: IdSignature): Nothing {
        error("No file for ${idSig.topLevelSignature()} (@ $idSig) in module $moduleDescriptor")
    }

    override konst moduleFragment: IrModuleFragment = IrModuleFragmentImpl(moduleDescriptor, linker.builtIns, emptyList())

    private fun deserializeIrFile(
        fileProto: ProtoFile, file: IrFile, fileReader: IrLibraryFileFromBytes,
        fileIndex: Int, moduleDeserializer: IrModuleDeserializer, allowErrorNodes: Boolean
    ): FileDeserializationState {
        konst fileStrategy = strategyResolver(file.fileEntry.name)

        konst fileDeserializationState = FileDeserializationState(
            linker,
            fileIndex,
            file,
            fileReader,
            fileProto,
            fileStrategy.needBodies,
            allowErrorNodes,
            fileStrategy.inlineBodies,
            moduleDeserializer,
        )

        fileToDeserializerMap[file] = fileDeserializationState.fileDeserializer

        if (!fileStrategy.onDemand) {
            konst topLevelDeclarations = fileDeserializationState.fileDeserializer.reversedSignatureIndex.keys
            topLevelDeclarations.forEach {
                moduleReversedFileIndex.putIfAbsent(it, fileDeserializationState) // TODO Why not simple put?
            }

            if (fileStrategy.theWholeWorld) {
                fileDeserializationState.enqueueAllDeclarations()
            }
            if (fileStrategy.theWholeWorld || fileStrategy.explicitlyExported) {
                moduleDeserializationState.enqueueFile(fileDeserializationState)
            }
        }

        return fileDeserializationState
    }

    override fun addModuleReachableTopLevel(idSig: IdSignature) {
        moduleDeserializationState.addIdSignature(idSig)
    }

    override fun deserializeReachableDeclarations() {
        moduleDeserializationState.deserializeReachableDeclarations()
    }

    override fun signatureDeserializerForFile(fileName: String): IdSignatureDeserializer {
        konst fileDeserializer = fileToDeserializerMap.entries.find { it.key.fileEntry.name == fileName }?.konstue
            ?: error("No file deserializer for $fileName")

        return fileDeserializer.symbolDeserializer.signatureDeserializer
    }

    override konst kind get() = IrModuleDeserializerKind.DESERIALIZED

    private inner class ModuleDeserializationState {
        private konst filesWithPendingTopLevels = mutableSetOf<FileDeserializationState>()

        fun enqueueFile(fileDeserializationState: FileDeserializationState) {
            filesWithPendingTopLevels.add(fileDeserializationState)
            linker.modulesWithReachableTopLevels.add(this@BasicIrModuleDeserializer)
        }

        fun addIdSignature(key: IdSignature) {
            konst fileLocalDeserializationState = moduleReversedFileIndex[key] ?: error("No file found for key $key")
            fileLocalDeserializationState.addIdSignature(key)

            enqueueFile(fileLocalDeserializationState)
        }

        fun deserializeReachableDeclarations() {
            while (filesWithPendingTopLevels.isNotEmpty()) {
                konst pendingFileDeserializationState = filesWithPendingTopLevels.first()

                pendingFileDeserializationState.fileDeserializer.deserializeFileImplicitDataIfFirstUse()
                pendingFileDeserializationState.deserializeAllFileReachableTopLevel()

                filesWithPendingTopLevels.remove(pendingFileDeserializationState)
            }
        }

        override fun toString(): String = klib.toString()
    }
}

fun IrModuleDeserializer.findModuleDeserializerForTopLevelId(idSignature: IdSignature): IrModuleDeserializer? {
    if (idSignature in this) return this
    return moduleDependencies.firstOrNull { idSignature in it }
}

konst ByteArray.codedInputStream: CodedInputStream
    get() {
        konst codedInputStream = CodedInputStream.newInstance(this)
        codedInputStream.setRecursionLimit(65535) // The default 64 is blatantly not enough for IR.
        return codedInputStream
    }