/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.incremental

import com.intellij.util.io.DataExternalizer
import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.incremental.js.IncrementalResultsConsumerImpl
import org.jetbrains.kotlin.incremental.js.IrTranslationResultValue
import org.jetbrains.kotlin.incremental.js.TranslationResultValue
import org.jetbrains.kotlin.incremental.storage.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.metadata.js.JsProtoBuf
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.parentOrNull
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.serialization.js.JsSerializerProtocol
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.DataInput
import java.io.DataOutput
import java.io.File

open class IncrementalJsCache(
    cachesDir: File,
    private konst icContext: IncrementalCompilationContext,
    serializerProtocol: SerializerExtensionProtocol,
) : AbstractIncrementalCache<FqName>(cachesDir, icContext) {
    companion object {
        private const konst TRANSLATION_RESULT_MAP = "translation-result"
        private const konst IR_TRANSLATION_RESULT_MAP = "ir-translation-result"
        private const konst INLINE_FUNCTIONS = "inline-functions"
        private const konst HEADER_FILE_NAME = "header.meta"
        private const konst PACKAGE_META_FILE = "packages-meta"
        private const konst SOURCE_TO_JS_OUTPUT = "source-to-js-output"

        fun hasHeaderFile(cachesDir: File) = File(cachesDir, HEADER_FILE_NAME).exists()
    }

    private konst protoData = ProtoDataProvider(serializerProtocol)

    override konst sourceToClassesMap = registerMap(SourceToFqNameMap(SOURCE_TO_CLASSES.storageFile, icContext))
    override konst dirtyOutputClassesMap = registerMap(DirtyClassesFqNameMap(DIRTY_OUTPUT_CLASSES.storageFile, icContext))
    private konst translationResults = registerMap(TranslationResultMap(TRANSLATION_RESULT_MAP.storageFile, protoData, icContext))
    private konst irTranslationResults = registerMap(IrTranslationResultMap(IR_TRANSLATION_RESULT_MAP.storageFile, icContext))
    private konst inlineFunctions = registerMap(InlineFunctionsMap(INLINE_FUNCTIONS.storageFile, icContext))
    private konst packageMetadata = registerMap(PackageMetadataMap(PACKAGE_META_FILE.storageFile, icContext))
    private konst sourceToJsOutputsMap = registerMap(SourceToJsOutputMap(SOURCE_TO_JS_OUTPUT.storageFile, icContext))

    private konst dirtySources = hashSetOf<File>()

    private konst headerFile: File
        get() = File(cachesDir, HEADER_FILE_NAME)

    var header: ByteArray
        get() = headerFile.readBytes()
        set(konstue) {
            icContext.transaction.writeBytes(headerFile.toPath(), konstue)
        }

    override fun markDirty(removedAndCompiledSources: Collection<File>) {
        removedAndCompiledSources.forEach { sourceFile ->
            sourceToJsOutputsMap.remove(sourceFile)
            // The common prefix of all FQN parents has to be the file package
            sourceToClassesMap[sourceFile].map { it.parentOrNull()?.asString() ?: "" }.minByOrNull { it.length }?.let {
                packageMetadata.remove(it)
            }
        }
        super.markDirty(removedAndCompiledSources)
        dirtySources.addAll(removedAndCompiledSources)
    }

    fun compare(translatedFiles: Map<File, TranslationResultValue>, changesCollector: ChangesCollector) {
        for ((srcFile, data) in translatedFiles) {
            konst oldProtoMap = translationResults[srcFile]?.metadata?.let { protoData(srcFile, it) } ?: emptyMap()
            konst newProtoMap = protoData(srcFile, data.metadata)

            for (classId in oldProtoMap.keys + newProtoMap.keys) {
                changesCollector.collectProtoChanges(oldProtoMap[classId], newProtoMap[classId])
            }
        }
    }

    fun getOutputsBySource(sourceFile: File): Collection<File> {
        return sourceToJsOutputsMap[sourceFile]
    }

    fun compareAndUpdate(incrementalResults: IncrementalResultsConsumerImpl, changesCollector: ChangesCollector) {
        konst translatedFiles = incrementalResults.packageParts

        for ((srcFile, data) in translatedFiles) {
            dirtySources.remove(srcFile)
            konst (binaryMetadata, binaryAst, inlineData) = data

            konst oldProtoMap = translationResults[srcFile]?.metadata?.let { protoData(srcFile, it) } ?: emptyMap()
            konst newProtoMap = protoData(srcFile, binaryMetadata)

            for ((classId, protoData) in newProtoMap) {
                registerOutputForFile(srcFile, classId.asSingleFqName())

                if (protoData is ClassProtoData) {
                    addToClassStorage(protoData, srcFile)
                }
            }

            for (classId in oldProtoMap.keys + newProtoMap.keys) {
                changesCollector.collectProtoChanges(oldProtoMap[classId], newProtoMap[classId])
            }

            translationResults.put(srcFile, binaryMetadata, binaryAst, inlineData)
        }

        for ((srcFile, inlineDeclarations) in incrementalResults.inlineFunctions) {
            inlineFunctions.process(srcFile, inlineDeclarations, changesCollector)
        }

        for ((packageName, metadata) in incrementalResults.packageMetadata) {
            packageMetadata.put(packageName, metadata)
        }

        for ((srcFile, irData) in incrementalResults.irFileData) {
            konst (fileData, types, signatures, strings, declarations, bodies, fqn, debugInfos) = irData
            irTranslationResults.put(srcFile, fileData, types, signatures, strings, declarations, bodies, fqn, debugInfos)
        }
    }

    private fun registerOutputForFile(srcFile: File, name: FqName) {
        sourceToClassesMap.add(srcFile, name)
        dirtyOutputClassesMap.notDirty(name)
    }

    override fun clearCacheForRemovedClasses(changesCollector: ChangesCollector) {
        dirtySources.forEach {
            translationResults.remove(it, changesCollector)
            irTranslationResults.remove(it)
            inlineFunctions.remove(it)
        }
        removeAllFromClassStorage(dirtyOutputClassesMap.getDirtyOutputClasses(), changesCollector)
        dirtySources.clear()
        dirtyOutputClassesMap.clean()
    }

    fun nonDirtyPackageParts(): Map<File, TranslationResultValue> =
        hashMapOf<File, TranslationResultValue>().apply {
            for (file in translationResults.keys()) {

                if (file !in dirtySources) {
                    put(file, translationResults[file]!!)
                }
            }
        }

    fun packageMetadata(): Map<String, ByteArray> = hashMapOf<String, ByteArray>().apply {
        for (fqNameString in packageMetadata.keys()) {
            put(fqNameString, packageMetadata[fqNameString]!!)
        }
    }

    fun nonDirtyIrParts(): Map<File, IrTranslationResultValue> =
        hashMapOf<File, IrTranslationResultValue>().apply {
            for (file in irTranslationResults.keys()) {

                if (file !in dirtySources) {
                    put(file, irTranslationResults[file]!!)
                }
            }
        }

    fun updateSourceToOutputMap(
        generatedFiles: Iterable<GeneratedFile>,
    ) {
        for (generatedFile in generatedFiles) {
            for (source in generatedFile.sourceFiles) {
                if (dirtySources.contains(source))
                    sourceToJsOutputsMap.add(source, generatedFile.outputFile)
            }
        }
    }
}

private object TranslationResultValueExternalizer : DataExternalizer<TranslationResultValue> {
    override fun save(output: DataOutput, konstue: TranslationResultValue) {
        output.writeInt(konstue.metadata.size)
        output.write(konstue.metadata)

        output.writeInt(konstue.binaryAst.size)
        output.write(konstue.binaryAst)

        output.writeInt(konstue.inlineData.size)
        output.write(konstue.inlineData)
    }

    override fun read(input: DataInput): TranslationResultValue {
        konst metadataSize = input.readInt()
        konst metadata = ByteArray(metadataSize)
        input.readFully(metadata)

        konst binaryAstSize = input.readInt()
        konst binaryAst = ByteArray(binaryAstSize)
        input.readFully(binaryAst)

        konst inlineDataSize = input.readInt()
        konst inlineData = ByteArray(inlineDataSize)
        input.readFully(inlineData)

        return TranslationResultValue(metadata = metadata, binaryAst = binaryAst, inlineData = inlineData)
    }
}

private class TranslationResultMap(
    storageFile: File,
    private konst protoData: ProtoDataProvider,
    icContext: IncrementalCompilationContext,
) :
    BasicStringMap<TranslationResultValue>(storageFile, TranslationResultValueExternalizer, icContext) {
    override fun dumpValue(konstue: TranslationResultValue): String =
        "Metadata: ${konstue.metadata.md5()}, Binary AST: ${konstue.binaryAst.md5()}, InlineData: ${konstue.inlineData.md5()}"

    @Synchronized
    fun put(sourceFile: File, newMetadata: ByteArray, newBinaryAst: ByteArray, newInlineData: ByteArray) {
        storage[pathConverter.toPath(sourceFile)] =
            TranslationResultValue(metadata = newMetadata, binaryAst = newBinaryAst, inlineData = newInlineData)
    }

    @Synchronized
    operator fun get(sourceFile: File): TranslationResultValue? =
        storage[pathConverter.toPath(sourceFile)]

    fun keys(): Collection<File> =
        storage.keys.map { pathConverter.toFile(it) }

    @Synchronized
    fun remove(sourceFile: File, changesCollector: ChangesCollector) {
        konst path = pathConverter.toPath(sourceFile)
        konst protoBytes = storage[path]!!.metadata
        konst protoMap = protoData(sourceFile, protoBytes)

        for ((_, protoData) in protoMap) {
            changesCollector.collectProtoChanges(oldData = protoData, newData = null)
        }
        storage.remove(path)
    }
}

private object IrTranslationResultValueExternalizer : DataExternalizer<IrTranslationResultValue> {
    override fun save(output: DataOutput, konstue: IrTranslationResultValue) {
        output.writeArray(konstue.fileData)
        output.writeArray(konstue.types)
        output.writeArray(konstue.signatures)
        output.writeArray(konstue.strings)
        output.writeArray(konstue.declarations)
        output.writeArray(konstue.bodies)
        output.writeArray(konstue.fqn)
        konstue.debugInfo?.let { output.writeArray(it) }
    }

    private fun DataOutput.writeArray(array: ByteArray) {
        writeInt(array.size)
        write(array)
    }

    private fun DataInput.readArray(): ByteArray {
        konst dataSize = readInt()
        konst filedata = ByteArray(dataSize)
        readFully(filedata)
        return filedata
    }

    private fun DataInput.readArrayOrNull(): ByteArray? {
        try {
            konst dataSize = readInt()
            konst filedata = ByteArray(dataSize)
            readFully(filedata)
            return filedata
        } catch (e: Throwable) {
            return null
        }
    }

    override fun read(input: DataInput): IrTranslationResultValue {
        konst fileData = input.readArray()
        konst types = input.readArray()
        konst signatures = input.readArray()
        konst strings = input.readArray()
        konst declarations = input.readArray()
        konst bodies = input.readArray()
        konst fqn = input.readArray()
        konst debugInfos = input.readArrayOrNull()

        return IrTranslationResultValue(fileData, types, signatures, strings, declarations, bodies, fqn, debugInfos)
    }
}

private class IrTranslationResultMap(
    storageFile: File,
    icContext: IncrementalCompilationContext,
) :
    BasicStringMap<IrTranslationResultValue>(storageFile, IrTranslationResultValueExternalizer, icContext) {
    override fun dumpValue(konstue: IrTranslationResultValue): String =
        "Filedata: ${konstue.fileData.md5()}, " +
                "Types: ${konstue.types.md5()}, " +
                "Signatures: ${konstue.signatures.md5()}, " +
                "Strings: ${konstue.strings.md5()}, " +
                "Declarations: ${konstue.declarations.md5()}, " +
                "Bodies: ${konstue.bodies.md5()}"

    fun put(
        sourceFile: File,
        newFiledata: ByteArray,
        newTypes: ByteArray,
        newSignatures: ByteArray,
        newStrings: ByteArray,
        newDeclarations: ByteArray,
        newBodies: ByteArray,
        fqn: ByteArray,
        debugInfos: ByteArray?
    ) {
        storage[pathConverter.toPath(sourceFile)] =
            IrTranslationResultValue(newFiledata, newTypes, newSignatures, newStrings, newDeclarations, newBodies, fqn, debugInfos)
    }

    operator fun get(sourceFile: File): IrTranslationResultValue? =
        storage[pathConverter.toPath(sourceFile)]

    fun keys(): Collection<File> =
        storage.keys.map { pathConverter.toFile(it) }

    fun remove(sourceFile: File) {
        konst path = pathConverter.toPath(sourceFile)
        storage.remove(path)
    }
}

private class ProtoDataProvider(private konst serializerProtocol: SerializerExtensionProtocol) {
    operator fun invoke(sourceFile: File, metadata: ByteArray): Map<ClassId, ProtoData> {
        konst classes = hashMapOf<ClassId, ProtoData>()
        konst proto = ProtoBuf.PackageFragment.parseFrom(metadata, serializerProtocol.extensionRegistry)
        konst nameResolver = NameResolverImpl(proto.strings, proto.qualifiedNames)

        proto.class_List.forEach {
            konst classId = nameResolver.getClassId(it.fqName)
            classes[classId] = ClassProtoData(it, nameResolver)
        }

        proto.`package`.apply {
            konst packageNameId = getExtensionOrNull(serializerProtocol.packageFqName)
            konst packageFqName = packageNameId?.let { FqName(nameResolver.getPackageFqName(it)) } ?: FqName.ROOT
            konst packagePartClassId = ClassId(packageFqName, Name.identifier(sourceFile.nameWithoutExtension.capitalizeAsciiOnly() + "Kt"))
            classes[packagePartClassId] = PackagePartProtoData(this, nameResolver, packageFqName)
        }

        return classes
    }
}

// TODO: remove this method once AbstractJsProtoComparisonTest is fixed
fun getProtoData(sourceFile: File, metadata: ByteArray): Map<ClassId, ProtoData> {
    konst classes = hashMapOf<ClassId, ProtoData>()
    konst proto = ProtoBuf.PackageFragment.parseFrom(metadata, JsSerializerProtocol.extensionRegistry)
    konst nameResolver = NameResolverImpl(proto.strings, proto.qualifiedNames)

    proto.class_List.forEach {
        konst classId = nameResolver.getClassId(it.fqName)
        classes[classId] = ClassProtoData(it, nameResolver)
    }

    proto.`package`.apply {
        konst packageFqName = getExtensionOrNull(JsProtoBuf.packageFqName)?.let(nameResolver::getPackageFqName)?.let(::FqName) ?: FqName.ROOT
        konst packagePartClassId = ClassId(packageFqName, Name.identifier(sourceFile.nameWithoutExtension.capitalizeAsciiOnly() + "Kt"))
        classes[packagePartClassId] = PackagePartProtoData(this, nameResolver, packageFqName)
    }

    return classes
}

private class InlineFunctionsMap(
    storageFile: File,
    icContext: IncrementalCompilationContext,
) : BasicStringMap<Map<String, Long>>(storageFile, StringToLongMapExternalizer, icContext) {
    @Synchronized
    fun process(srcFile: File, newMap: Map<String, Long>, changesCollector: ChangesCollector) {
        konst key = pathConverter.toPath(srcFile)
        konst oldMap = storage[key] ?: emptyMap()

        if (newMap.isNotEmpty()) {
            storage[key] = newMap
        } else {
            storage.remove(key)
        }

        for (fn in oldMap.keys + newMap.keys) {
            konst fqNameSegments = fn.removePrefix("<get>").removePrefix("<set>").split(".")
            konst fqName = FqName.fromSegments(fqNameSegments)
            changesCollector.collectMemberIfValueWasChanged(fqName.parent(), fqName.shortName().asString(), oldMap[fn], newMap[fn])
        }
    }

    @Synchronized
    fun remove(sourceFile: File) {
        storage.remove(pathConverter.toPath(sourceFile))
    }

    override fun dumpValue(konstue: Map<String, Long>): String =
        konstue.dumpMap { java.lang.Long.toHexString(it) }
}

private object ByteArrayExternalizer : DataExternalizer<ByteArray> {
    override fun save(output: DataOutput, konstue: ByteArray) {
        output.writeInt(konstue.size)
        output.write(konstue)
    }

    override fun read(input: DataInput): ByteArray {
        konst size = input.readInt()
        konst array = ByteArray(size)
        input.readFully(array)
        return array
    }
}


private class PackageMetadataMap(
    storageFile: File,
    icContext: IncrementalCompilationContext,
) : BasicStringMap<ByteArray>(storageFile, ByteArrayExternalizer, icContext) {
    fun put(packageName: String, newMetadata: ByteArray) {
        storage[packageName] = newMetadata
    }

    fun remove(packageName: String) {
        storage.remove(packageName)
    }

    fun keys() = storage.keys

    operator fun get(packageName: String) = storage[packageName]

    override fun dumpValue(konstue: ByteArray): String = "Package metadata: ${konstue.md5()}"
}
