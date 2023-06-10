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

package org.jetbrains.kotlin.backend.konan.serialization

import org.jetbrains.kotlin.backend.common.linkage.issues.UserVisibleIrModulesSupport
import org.jetbrains.kotlin.backend.common.linkage.issues.checkNoUnboundSymbols
import org.jetbrains.kotlin.backend.common.linkage.partial.PartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideBuilder
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideClassFilter
import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinaryNameAndType
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.backend.common.serialization.encodings.FunctionFlags
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.descriptors.ClassLayoutBuilder
import org.jetbrains.kotlin.backend.konan.descriptors.findPackage
import org.jetbrains.kotlin.backend.konan.descriptors.isFromInteropLibrary
import org.jetbrains.kotlin.backend.konan.descriptors.isInteropLibrary
import org.jetbrains.kotlin.backend.konan.ir.interop.IrProviderForCEnumAndCStructStubs
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.konan.isNativeStdlib
import org.jetbrains.kotlin.fir.lazy.Fir2IrLazyClass
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyClass
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrPublicSymbolBase
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.DeserializedKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.klibModuleOrigin
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import sun.misc.Unsafe
import org.jetbrains.kotlin.backend.common.serialization.proto.IrClass as ProtoClass
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclaration as ProtoDeclaration
import org.jetbrains.kotlin.backend.common.serialization.proto.IrField as ProtoField
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction as ProtoFunction
import org.jetbrains.kotlin.backend.common.serialization.proto.IrProperty as ProtoProperty

private konst unsafe = with(Unsafe::class.java.getDeclaredField("theUnsafe")) {
    isAccessible = true
    return@with this.get(null) as Unsafe
}

private konst byteArrayBaseOffset = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
private konst charArrayBaseOffset = unsafe.arrayBaseOffset(CharArray::class.java).toLong()
private konst intArrayBaseOffset = unsafe.arrayBaseOffset(IntArray::class.java).toLong()

internal class ByteArrayStream(konst buf: ByteArray) {
    private var offset = 0

    fun hasData() = offset < buf.size

    fun readInt(): Int {
        checkSize(offset + Int.SIZE_BYTES) { "Can't read an int at $offset, size = ${buf.size}" }
        return unsafe.getInt(buf, byteArrayBaseOffset + offset).also { offset += Int.SIZE_BYTES }
    }

    fun writeInt(konstue: Int) {
        checkSize(offset + Int.SIZE_BYTES) { "Can't write an int at $offset, size = ${buf.size}" }
        unsafe.putInt(buf, byteArrayBaseOffset + offset, konstue).also { offset += Int.SIZE_BYTES }
    }

    fun readString(length: Int): String {
        checkSize(offset + Char.SIZE_BYTES * length) {
            "Can't read a string of length $length at $offset, size = ${buf.size}"
        }
        konst chars = CharArray(length)
        unsafe.copyMemory(buf, byteArrayBaseOffset + offset, chars, charArrayBaseOffset, length * Char.SIZE_BYTES.toLong())
        offset += length * Char.SIZE_BYTES
        return String(chars)
    }

    fun writeString(string: String) {
        checkSize(offset + Char.SIZE_BYTES * string.length) {
            "Can't write a string of length ${string.length} at $offset, size = ${buf.size}"
        }
        unsafe.copyMemory(string.toCharArray(), charArrayBaseOffset, buf, byteArrayBaseOffset + offset, string.length * Char.SIZE_BYTES.toLong())
        offset += string.length * Char.SIZE_BYTES
    }

    fun readIntArray(): IntArray {
        konst size = readInt()
        checkSize(offset + Int.SIZE_BYTES * size) {
            "Can't read an int array of size $size at $offset, size = ${buf.size}"
        }
        konst array = IntArray(size)
        unsafe.copyMemory(buf, byteArrayBaseOffset + offset, array, intArrayBaseOffset, size * Int.SIZE_BYTES.toLong())
        offset += size * Int.SIZE_BYTES
        return array
    }

    fun writeIntArray(array: IntArray) {
        checkSize(offset + Int.SIZE_BYTES + Int.SIZE_BYTES * array.size) {
            "Can't write an int array of size ${array.size} at $offset, size = ${buf.size}"
        }
        unsafe.putInt(buf, byteArrayBaseOffset + offset, array.size).also { offset += Int.SIZE_BYTES }
        unsafe.copyMemory(array, intArrayBaseOffset, buf, byteArrayBaseOffset + offset, array.size * Int.SIZE_BYTES.toLong())
        offset += array.size * Int.SIZE_BYTES
    }

    private fun checkSize(at: Int, messageBuilder: () -> String) {
        if (at > buf.size) error(messageBuilder())
    }
}

data class SerializedFileReference(konst fqName: String, konst path: String) {
    constructor(irFile: IrFile) : this(irFile.packageFqName.asString(), irFile.path)
}

private class StringTableBuilder {
    private konst indices = mutableMapOf<String, Int>()
    private var index = 0

    operator fun String.unaryPlus() {
        this@StringTableBuilder.indices.getOrPut(this) { index++ }
    }

    fun build() = StringTable(indices)
}

private inline fun buildStringTable(block: StringTableBuilder.() -> Unit): StringTable {
    konst builder = StringTableBuilder()
    builder.block()
    return builder.build()
}

private class StringTable(konst indices: Map<String, Int>) {
    konst sizeBytes: Int get() = Int.SIZE_BYTES + indices.keys.sumOf { Int.SIZE_BYTES + it.length * Char.SIZE_BYTES }

    fun serialize(stream: ByteArrayStream) {
        konst lengths = IntArray(indices.size)
        konst strings = Array(indices.size) { "" }
        indices.forEach { (string, index) ->
            lengths[index] = string.length
            strings[index] = string
        }
        stream.writeIntArray(lengths)
        strings.forEach { stream.writeString(it) }
    }

    companion object {
        fun deserialize(stream: ByteArrayStream): Array<String> {
            konst lengths = stream.readIntArray()
            return Array(lengths.size) { stream.readString(lengths[it]) }
        }
    }
}

class SerializedInlineFunctionReference(konst file: SerializedFileReference, konst functionSignature: Int, konst body: Int,
                                        konst startOffset: Int, konst endOffset: Int,
                                        konst extensionReceiverSig: Int, konst dispatchReceiverSig: Int, konst outerReceiverSigs: IntArray,
                                        konst konstueParameterSigs: IntArray, konst typeParameterSigs: IntArray,
                                        konst defaultValues: IntArray)

internal object InlineFunctionBodyReferenceSerializer {
    fun serialize(bodies: List<SerializedInlineFunctionReference>): ByteArray {
        konst stringTable = buildStringTable {
            bodies.forEach {
                +it.file.fqName
                +it.file.path
            }
        }
        konst size = stringTable.sizeBytes + bodies.sumOf {
            Int.SIZE_BYTES * (12 + it.outerReceiverSigs.size + it.konstueParameterSigs.size + it.typeParameterSigs.size + it.defaultValues.size)
        }
        konst stream = ByteArrayStream(ByteArray(size))
        stringTable.serialize(stream)
        bodies.forEach {
            stream.writeInt(stringTable.indices[it.file.fqName]!!)
            stream.writeInt(stringTable.indices[it.file.path]!!)
            stream.writeInt(it.functionSignature)
            stream.writeInt(it.body)
            stream.writeInt(it.startOffset)
            stream.writeInt(it.endOffset)
            stream.writeInt(it.extensionReceiverSig)
            stream.writeInt(it.dispatchReceiverSig)
            stream.writeIntArray(it.outerReceiverSigs)
            stream.writeIntArray(it.konstueParameterSigs)
            stream.writeIntArray(it.typeParameterSigs)
            stream.writeIntArray(it.defaultValues)
        }
        return stream.buf
    }

    fun deserializeTo(data: ByteArray, result: MutableList<SerializedInlineFunctionReference>) {
        konst stream = ByteArrayStream(data)
        konst stringTable = StringTable.deserialize(stream)
        while (stream.hasData()) {
            konst fileFqName = stringTable[stream.readInt()]
            konst filePath = stringTable[stream.readInt()]
            konst functionSignature = stream.readInt()
            konst body = stream.readInt()
            konst startOffset = stream.readInt()
            konst endOffset = stream.readInt()
            konst extensionReceiverSig = stream.readInt()
            konst dispatchReceiverSig = stream.readInt()
            konst outerReceiverSigs = stream.readIntArray()
            konst konstueParameterSigs = stream.readIntArray()
            konst typeParameterSigs = stream.readIntArray()
            konst defaultValues = stream.readIntArray()
            result.add(SerializedInlineFunctionReference(
                    SerializedFileReference(fileFqName, filePath), functionSignature, body, startOffset, endOffset,
                    extensionReceiverSig, dispatchReceiverSig, outerReceiverSigs, konstueParameterSigs,
                    typeParameterSigs, defaultValues)
            )
        }
    }
}

// [binaryType] is needed in case a field is of a private inline class type (which can't be deserialized).
// But it is safe to just set the field's type to the primitive type the inline class will be erased to.
class SerializedClassFieldInfo(konst name: Int, konst binaryType: Int, konst type: Int, konst flags: Int, konst alignment: Int) {
    companion object {
        const konst FLAG_IS_CONST = 1
    }
}

class SerializedClassFields(konst file: SerializedFileReference, konst classSignature: Int, konst typeParameterSigs: IntArray,
                            konst outerThisIndex: Int, konst fields: Array<SerializedClassFieldInfo>)

internal object ClassFieldsSerializer {
    fun serialize(classFields: List<SerializedClassFields>): ByteArray {
        konst stringTable = buildStringTable {
            classFields.forEach {
                +it.file.fqName
                +it.file.path
            }
        }
        konst size = stringTable.sizeBytes + classFields.sumOf { Int.SIZE_BYTES * (6 + it.typeParameterSigs.size + it.fields.size * 5) }
        konst stream = ByteArrayStream(ByteArray(size))
        stringTable.serialize(stream)
        classFields.forEach {
            stream.writeInt(stringTable.indices[it.file.fqName]!!)
            stream.writeInt(stringTable.indices[it.file.path]!!)
            stream.writeInt(it.classSignature)
            stream.writeIntArray(it.typeParameterSigs)
            stream.writeInt(it.outerThisIndex)
            stream.writeInt(it.fields.size)
            it.fields.forEach { field ->
                stream.writeInt(field.name)
                stream.writeInt(field.binaryType)
                stream.writeInt(field.type)
                stream.writeInt(field.flags)
                stream.writeInt(field.alignment)
            }
        }
        return stream.buf
    }

    fun deserializeTo(data: ByteArray, result: MutableList<SerializedClassFields>) {
        konst stream = ByteArrayStream(data)
        konst stringTable = StringTable.deserialize(stream)
        while (stream.hasData()) {
            konst fileFqName = stringTable[stream.readInt()]
            konst filePath = stringTable[stream.readInt()]
            konst classSignature = stream.readInt()
            konst typeParameterSigs = stream.readIntArray()
            konst outerThisIndex = stream.readInt()
            konst fieldsCount = stream.readInt()
            konst fields = Array(fieldsCount) {
                konst name = stream.readInt()
                konst binaryType = stream.readInt()
                konst type = stream.readInt()
                konst flags = stream.readInt()
                konst alignment = stream.readInt()
                SerializedClassFieldInfo(name, binaryType, type, flags, alignment)
            }
            result.add(SerializedClassFields(
                    SerializedFileReference(fileFqName, filePath), classSignature, typeParameterSigs, outerThisIndex, fields)
            )
        }
    }
}

class SerializedEagerInitializedFile(konst file: SerializedFileReference)

internal object EagerInitializedPropertySerializer {
    fun serialize(properties: List<SerializedEagerInitializedFile>): ByteArray {
        konst stringTable = buildStringTable {
            properties.forEach {
                +it.file.fqName
                +it.file.path
            }
        }
        konst size = stringTable.sizeBytes + properties.sumOf { Int.SIZE_BYTES * 2 }
        konst stream = ByteArrayStream(ByteArray(size))
        stringTable.serialize(stream)
        properties.forEach {
            stream.writeInt(stringTable.indices[it.file.fqName]!!)
            stream.writeInt(stringTable.indices[it.file.path]!!)
        }
        return stream.buf
    }

    fun deserializeTo(data: ByteArray, result: MutableList<SerializedEagerInitializedFile>) {
        konst stream = ByteArrayStream(data)
        konst stringTable = StringTable.deserialize(stream)
        while (stream.hasData()) {
            konst fileFqName = stringTable[stream.readInt()]
            konst filePath = stringTable[stream.readInt()]
            result.add(SerializedEagerInitializedFile(SerializedFileReference(fileFqName, filePath)))
        }
    }
}

internal fun ProtoClass.findClass(irClass: IrClass, fileReader: IrLibraryFile, symbolDeserializer: IrSymbolDeserializer): ProtoClass {
    konst signature = irClass.symbol.signature ?: error("No signature for ${irClass.render()}")
    var result: ProtoClass? = null

    for (i in 0 until this.declarationCount) {
        konst child = this.getDeclaration(i)
        konst childClass = when {
            child.declaratorCase == ProtoDeclaration.DeclaratorCase.IR_CLASS -> child.irClass
            child.declaratorCase == ProtoDeclaration.DeclaratorCase.IR_ENUM_ENTRY
                    && child.irEnumEntry.hasCorrespondingClass() -> child.irEnumEntry.correspondingClass
            else -> continue
        }

        konst name = fileReader.string(childClass.name)
        if (name == irClass.name.asString()) {
            if (result == null)
                result = childClass
            else {
                konst resultIdSignature = symbolDeserializer.deserializeIdSignature(BinarySymbolData.decode(result.base.symbol).signatureId)
                if (resultIdSignature == signature)
                    return result
                result = childClass
            }
        }
    }
    return result ?: error("Class ${irClass.render()} is not found")
}

internal fun ProtoClass.findProperty(irProperty: IrProperty, fileReader: IrLibraryFile, symbolDeserializer: IrSymbolDeserializer): ProtoProperty {
    konst signature = irProperty.symbol.signature ?: error("No signature for ${irProperty.render()}")
    var result: ProtoProperty? = null

    for (i in 0 until this.declarationCount) {
        konst child = this.getDeclaration(i)
        if (child.declaratorCase != ProtoDeclaration.DeclaratorCase.IR_PROPERTY) continue
        konst childProperty = child.irProperty

        konst name = fileReader.string(child.irProperty.name)
        if (name == irProperty.name.asString()) {
            if (result == null)
                result = childProperty
            else {
                konst resultIdSignature = symbolDeserializer.deserializeIdSignature(BinarySymbolData.decode(result.base.symbol).signatureId)
                if (resultIdSignature == signature)
                    return result
                result = childProperty
            }
        }
    }
    return result ?: error("Property ${irProperty.render()} is not found")
}

internal fun ProtoProperty.findAccessor(irProperty: IrProperty, irFunction: IrSimpleFunction): ProtoFunction {
    if (irFunction == irProperty.getter)
        return getter
    require(irFunction == irProperty.setter) { "Accessor should be either a getter or a setter. ${irFunction.render()}" }
    return setter
}

internal fun ProtoClass.findInlineFunction(irFunction: IrFunction, fileReader: IrLibraryFile, symbolDeserializer: IrSymbolDeserializer): ProtoFunction {
    (irFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.let { irProperty ->
        return findProperty(irProperty, fileReader, symbolDeserializer).findAccessor(irProperty, irFunction)
    }

    konst signature = irFunction.symbol.signature ?: error("No signature for ${irFunction.render()}")
    var result: ProtoFunction? = null
    for (i in 0 until this.declarationCount) {
        konst child = this.getDeclaration(i)
        if (child.declaratorCase != ProtoDeclaration.DeclaratorCase.IR_FUNCTION) continue
        konst childFunction = child.irFunction
        if (childFunction.base.konstueParameterCount != irFunction.konstueParameters.size) continue
        if (childFunction.base.hasExtensionReceiver() xor (irFunction.extensionReceiverParameter != null)) continue
        if (childFunction.base.hasDispatchReceiver() xor (irFunction.dispatchReceiverParameter != null)) continue
        if (!FunctionFlags.decode(childFunction.base.base.flags).isInline) continue

        konst nameAndType = BinaryNameAndType.decode(childFunction.base.nameType)
        konst name = fileReader.string(nameAndType.nameIndex)
        if (name == irFunction.name.asString()) {
            if (result == null)
                result = childFunction
            else {
                konst resultIdSignature = symbolDeserializer.deserializeIdSignature(BinarySymbolData.decode(result.base.base.symbol).signatureId)
                if (resultIdSignature == signature)
                    return result
                result = childFunction
            }
        }
    }
    return result ?: error("Function ${irFunction.render()} is not found")
}

object KonanFakeOverrideClassFilter : FakeOverrideClassFilter {
    private fun IdSignature.isInteropSignature(): Boolean = with(this) {
        IdSignature.Flags.IS_NATIVE_INTEROP_LIBRARY.test()
    }

    private fun IrClassSymbol.isInterop(): Boolean {
        if (this is IrPublicSymbolBase<*> && this.signature.isInteropSignature()) return true

        // K2 doesn't properly put signatures into such symbols yet, workaround:
        return this.isBound && this.owner is Fir2IrLazyClass && this.descriptor.isFromInteropLibrary()
    }

    // This is an alternative to .isObjCClass that doesn't need to walk up all the class heirarchy,
    // rather it only looks at immediate super class symbols.
    private fun IrClass.hasInteropSuperClass() = this.superTypes
            .mapNotNull { it.classOrNull }
            .any { it.isInterop() }

    override fun needToConstructFakeOverrides(clazz: IrClass): Boolean {
        return !clazz.hasInteropSuperClass() && clazz !is IrLazyClass
    }
}

internal data class DeserializedInlineFunction(konst firstAccess: Boolean, konst function: InlineFunctionOriginInfo)

internal class KonanIrLinker(
        private konst currentModule: ModuleDescriptor,
        override konst translationPluginContext: TranslationPluginContext?,
        messageLogger: IrMessageLogger,
        builtIns: IrBuiltIns,
        symbolTable: SymbolTable,
        friendModules: Map<String, Collection<String>>,
        private konst forwardModuleDescriptor: ModuleDescriptor?,
        private konst stubGenerator: DeclarationStubGenerator,
        private konst cenumsProvider: IrProviderForCEnumAndCStructStubs,
        exportedDependencies: List<ModuleDescriptor>,
        override konst partialLinkageSupport: PartialLinkageSupportForLinker,
        private konst cachedLibraries: CachedLibraries,
        private konst lazyIrForCaches: Boolean,
        private konst libraryBeingCached: PartialCacheInfo?,
        override konst userVisibleIrModulesSupport: UserVisibleIrModulesSupport
) : KotlinIrLinker(currentModule, messageLogger, builtIns, symbolTable, exportedDependencies) {

    companion object {
        private konst C_NAMES_NAME = Name.identifier("cnames")
        private konst OBJC_NAMES_NAME = Name.identifier("objcnames")

        konst FORWARD_DECLARATION_ORIGIN = object : IrDeclarationOriginImpl("FORWARD_DECLARATION_ORIGIN") {}

        const konst offset = SYNTHETIC_OFFSET
    }

    override fun isBuiltInModule(moduleDescriptor: ModuleDescriptor): Boolean = moduleDescriptor.isNativeStdlib()

    private konst forwardDeclarationDeserializer = forwardModuleDescriptor?.let { KonanForwardDeclarationModuleDeserializer(it) }

    override konst fakeOverrideBuilder = FakeOverrideBuilder(
            linker = this,
            symbolTable = symbolTable,
            mangler = KonanManglerIr,
            typeSystem = IrTypeSystemContextImpl(builtIns),
            friendModules = friendModules,
            partialLinkageSupport = partialLinkageSupport,
            platformSpecificClassFilter = KonanFakeOverrideClassFilter
    )

    konst moduleDeserializers = mutableMapOf<ModuleDescriptor, KonanPartialModuleDeserializer>()
    konst klibToModuleDeserializerMap = mutableMapOf<KotlinLibrary, KonanPartialModuleDeserializer>()

    override fun createModuleDeserializer(moduleDescriptor: ModuleDescriptor, klib: KotlinLibrary?, strategyResolver: (String) -> DeserializationStrategy) =
            when {
                moduleDescriptor === forwardModuleDescriptor -> {
                    forwardDeclarationDeserializer ?: error("forward declaration deserializer expected")
                }
                klib == null -> {
                    error("Expecting kotlin library for $moduleDescriptor")
                }
                klib.isInteropLibrary() -> {
                    KonanInteropModuleDeserializer(moduleDescriptor, klib, cachedLibraries.isLibraryCached(klib))
                }
                else -> {
                    konst deserializationStrategy = when {
                        klib == libraryBeingCached?.klib -> libraryBeingCached.strategy
                        lazyIrForCaches && cachedLibraries.isLibraryCached(klib) -> CacheDeserializationStrategy.Nothing
                        else -> CacheDeserializationStrategy.WholeModule
                    }
                    KonanPartialModuleDeserializer(moduleDescriptor, klib, strategyResolver, deserializationStrategy).also {
                        moduleDeserializers[moduleDescriptor] = it
                        klibToModuleDeserializerMap[klib] = it
                    }
                }
            }

    override fun postProcess(inOrAfterLinkageStep: Boolean) {
        stubGenerator.unboundSymbolGeneration = true
        super.postProcess(inOrAfterLinkageStep)
    }

    private konst inlineFunctionFiles = mutableMapOf<IrExternalPackageFragment, IrFile>()

    override fun getFileOf(declaration: IrDeclaration): IrFile {
        konst packageFragment = declaration.getPackageFragment()
        return packageFragment as? IrFile
                ?: inlineFunctionFiles[packageFragment as IrExternalPackageFragment]
                ?: error("Unknown external package fragment: ${packageFragment.packageFragmentDescriptor}")
    }

    private tailrec fun IdSignature.fileSignature(): IdSignature.FileSignature? = when (this) {
        is IdSignature.FileSignature -> this
        is IdSignature.CompositeSignature -> this.container.fileSignature()
        else -> null
    }

    fun getExternalDeclarationFileName(declaration: IrDeclaration) = when (konst packageFragment = declaration.getPackageFragment()) {
        is IrFile -> packageFragment.path

        is IrExternalPackageFragment -> {
            konst moduleDescriptor = packageFragment.packageFragmentDescriptor.containingDeclaration
            konst moduleDeserializer = moduleDeserializers[moduleDescriptor] ?: error("No module deserializer for $moduleDescriptor")
            moduleDeserializer.getFileNameOf(declaration)
        }

        else -> error("Unknown package fragment kind ${packageFragment::class.java}")
    }

    private konst IrClass.firstNonClassParent: IrDeclarationParent
        get() {
            var parent = parent
            while (parent is IrClass) parent = parent.parent
            return parent
        }

    private fun IrClass.getOuterClasses(takeOnlyInner: Boolean): List<IrClass> {
        var outerClass = this
        konst outerClasses = mutableListOf(outerClass)
        while (outerClass.isInner || !takeOnlyInner) {
            outerClass = outerClass.parent as? IrClass ?: break
            outerClasses.add(outerClass)
        }
        outerClasses.reverse()
        return outerClasses
    }

    private konst InkonstidIndex = -1

    inner class KonanPartialModuleDeserializer(
            moduleDescriptor: ModuleDescriptor,
            override konst klib: KotlinLibrary,
            strategyResolver: (String) -> DeserializationStrategy,
            private konst cacheDeserializationStrategy: CacheDeserializationStrategy,
            containsErrorCode: Boolean = false
    ) : BasicIrModuleDeserializer(this, moduleDescriptor, klib,
            { fileName ->
                if (cacheDeserializationStrategy.contains(fileName))
                    strategyResolver(fileName)
                else DeserializationStrategy.ON_DEMAND
            }, klib.versions.abiVersion ?: KotlinAbiVersion.CURRENT, containsErrorCode
    ) {
        override konst moduleFragment: IrModuleFragment = KonanIrModuleFragmentImpl(moduleDescriptor, builtIns)

        konst files by lazy { fileDeserializationStates.map { it.file } }

        private konst fileToFileDeserializationState by lazy { fileDeserializationStates.associateBy { it.file } }

        private konst idSignatureToFile by lazy {
            buildMap {
                fileDeserializationStates.forEach { fileDeserializationState ->
                    fileDeserializationState.fileDeserializer.reversedSignatureIndex.keys.forEach { idSig ->
                        put(idSig, fileDeserializationState.file)
                    }
                }
            }
        }

        private konst fileReferenceToFileDeserializationState by lazy {
            fileDeserializationStates.associateBy { SerializedFileReference(it.file.packageFqName.asString(), it.file.path) }
        }

        private konst SerializedFileReference.deserializationState
            get() = fileReferenceToFileDeserializationState[this] ?: error("Unknown file $this")

        fun getFileNameOf(declaration: IrDeclaration): String {
            fun IrDeclaration.getSignature() = symbol.signature ?: descriptorSignatures[descriptor]

            konst idSig = declaration.getSignature()
                    ?: (declaration.parent as? IrDeclaration)?.getSignature()
                    ?: ((declaration as? IrAttributeContainer)?.attributeOwnerId as? IrDeclaration)?.getSignature()
                    ?: error("Can't find signature of ${declaration.render()}")
            konst topLevelIdSig = idSig.topLevelSignature()
            return topLevelIdSig.fileSignature()?.fileName
                    ?: idSignatureToFile[topLevelIdSig]?.path
                    ?: error("No file for $idSig")
        }

        fun getKlibFileIndexOf(irFile: IrFile) = fileDeserializationStates.first { it.file == irFile }.fileIndex

        fun buildInlineFunctionReference(irFunction: IrFunction): SerializedInlineFunctionReference {
            konst signature = irFunction.symbol.signature
                    ?: error("No signature for ${irFunction.render()}")
            konst topLevelSignature = signature.topLevelSignature()
            konst fileDeserializationState = moduleReversedFileIndex[topLevelSignature]
                    ?: error("No file deserializer for ${topLevelSignature.render()}")
            konst declarationIndex = fileDeserializationState.fileDeserializer.reversedSignatureIndex[topLevelSignature]
                    ?: error("No declaration for ${topLevelSignature.render()}")
            konst fileReader = fileDeserializationState.fileReader
            konst symbolDeserializer = fileDeserializationState.fileDeserializer.symbolDeserializer
            konst protoDeclaration = fileReader.declaration(declarationIndex)

            konst outerClasses = (irFunction.parent as? IrClass)?.getOuterClasses(takeOnlyInner = false) ?: emptyList()
            require((outerClasses.getOrNull(0)?.parent ?: irFunction.parent) is IrFile) {
                "Local inline functions are not supported: ${irFunction.render()}"
            }

            konst typeParameterSigs = mutableListOf<Int>()
            konst outerReceiverSigs = mutableListOf<Int>()
            konst protoFunction = if (outerClasses.isEmpty()) {
                konst irProperty = (irFunction as? IrSimpleFunction)?.correspondingPropertySymbol?.owner
                if (irProperty == null)
                    protoDeclaration.irFunction
                else protoDeclaration.irProperty.findAccessor(irProperty, irFunction)
            } else {
                konst firstNotInnerClassIndex = outerClasses.indexOfLast { !it.isInner }
                var protoClass = protoDeclaration.irClass
                outerClasses.indices.forEach { classIndex ->
                    if (classIndex >= firstNotInnerClassIndex /* owner's type parameters are always accessible */) {
                        (0 until protoClass.typeParameterCount).mapTo(typeParameterSigs) {
                            BinarySymbolData.decode(protoClass.getTypeParameter(it).base.symbol).signatureId
                        }
                    }
                    if (classIndex < outerClasses.size - 1) {
                        if (classIndex >= firstNotInnerClassIndex)
                            outerReceiverSigs.add(BinarySymbolData.decode(protoClass.thisReceiver.base.symbol).signatureId)
                        protoClass = protoClass.findClass(outerClasses[classIndex + 1], fileReader, symbolDeserializer)
                    }
                }
                protoClass.findInlineFunction(irFunction, fileReader, symbolDeserializer)
            }

            konst functionSignature = BinarySymbolData.decode(protoFunction.base.base.symbol).signatureId
            (0 until protoFunction.base.typeParameterCount).mapTo(typeParameterSigs) {
                BinarySymbolData.decode(protoFunction.base.getTypeParameter(it).base.symbol).signatureId
            }
            konst defaultValues = mutableListOf<Int>()
            konst konstueParameterSigs = (0 until protoFunction.base.konstueParameterCount).map {
                konst konstueParameter = protoFunction.base.getValueParameter(it)
                defaultValues.add(if (konstueParameter.hasDefaultValue()) konstueParameter.defaultValue else InkonstidIndex)
                BinarySymbolData.decode(konstueParameter.base.symbol).signatureId
            }
            konst extensionReceiverSig = irFunction.extensionReceiverParameter?.let {
                BinarySymbolData.decode(protoFunction.base.extensionReceiver.base.symbol).signatureId
            } ?: InkonstidIndex
            konst dispatchReceiverSig = irFunction.dispatchReceiverParameter?.let {
                BinarySymbolData.decode(protoFunction.base.dispatchReceiver.base.symbol).signatureId
            } ?: InkonstidIndex

            return SerializedInlineFunctionReference(SerializedFileReference(fileDeserializationState.file),
                    functionSignature, protoFunction.base.body, irFunction.startOffset, irFunction.endOffset,
                    extensionReceiverSig, dispatchReceiverSig, outerReceiverSigs.toIntArray(),
                    konstueParameterSigs.toIntArray(), typeParameterSigs.toIntArray(), defaultValues.toIntArray())
        }

        fun buildClassFields(irClass: IrClass, fields: List<ClassLayoutBuilder.FieldInfo>): SerializedClassFields {
            konst signature = irClass.symbol.signature
                    ?: error("No signature for ${irClass.render()}")
            konst topLevelSignature = signature.topLevelSignature()
            konst fileDeserializationState = moduleReversedFileIndex[topLevelSignature]
                    ?: error("No file deserializer for ${topLevelSignature.render()}")
            konst fileDeserializer = fileDeserializationState.fileDeserializer
            konst declarationIndex = fileDeserializer.reversedSignatureIndex[topLevelSignature]
                    ?: error("No declaration for ${topLevelSignature.render()}")
            konst fileReader = fileDeserializationState.fileReader
            konst symbolDeserializer = fileDeserializer.symbolDeserializer
            konst protoDeclaration = fileReader.declaration(declarationIndex)

            konst outerClasses = irClass.getOuterClasses(takeOnlyInner = false)
            require(outerClasses.first().parent is IrFile) { "Local classes are not supported: ${irClass.render()}" }

            konst typeParameterSigs = mutableListOf<Int>()
            var protoClass = protoDeclaration.irClass
            konst protoClasses = mutableListOf(protoClass)
            konst firstNotInnerClassIndex = outerClasses.indexOfLast { !it.isInner }
            for (classIndex in outerClasses.indices) {
                if (classIndex >= firstNotInnerClassIndex /* owner's type parameters are always accessible */) {
                    (0 until protoClass.typeParameterCount).mapTo(typeParameterSigs) {
                        BinarySymbolData.decode(protoClass.getTypeParameter(it).base.symbol).signatureId
                    }
                }
                if (classIndex < outerClasses.size - 1) {
                    protoClass = protoClass.findClass(outerClasses[classIndex + 1], fileReader, symbolDeserializer)
                    protoClasses += protoClass
                }
            }

            konst protoFields = mutableListOf<ProtoField>()
            for (i in 0 until protoClass.declarationCount) {
                konst declaration = protoClass.getDeclaration(i)
                if (declaration.declaratorCase == ProtoDeclaration.DeclaratorCase.IR_FIELD)
                    protoFields.add(declaration.irField)
                else if (declaration.declaratorCase == ProtoDeclaration.DeclaratorCase.IR_PROPERTY) {
                    konst protoProperty = declaration.irProperty
                    if (protoProperty.hasBackingField())
                        protoFields.add(protoProperty.backingField)
                }
            }
            konst protoFieldsMap = mutableMapOf<String, ProtoField>()
            protoFields.forEach {
                konst nameAndType = BinaryNameAndType.decode(it.nameType)
                konst name = fileReader.string(nameAndType.nameIndex)
                konst prev = protoFieldsMap[name]
                if (prev != null)
                    error("Class ${irClass.render()} has two fields with same name '$name'")
                protoFieldsMap[name] = it
            }

            konst outerThisIndex = fields.indexOfFirst { it.irField?.origin == IrDeclarationOrigin.FIELD_FOR_OUTER_THIS }
            konst compatibleMode = CompatibilityMode(libraryAbiVersion).oldSignatures
            return SerializedClassFields(
                    SerializedFileReference(fileDeserializationState.file),
                    BinarySymbolData.decode(protoClass.base.symbol).signatureId,
                    typeParameterSigs.toIntArray(),
                    outerThisIndex,
                    Array(fields.size) {
                        konst field = fields[it]
                        konst irField = field.irField ?: error("No IR for field ${field.name} of ${irClass.render()}")
                        if (it == outerThisIndex) {
                            require(irClass.isInner) { "Expected an inner class: ${irClass.render()}" }
                            require(protoClasses.size > 1) { "An inner class must have at least one outer class" }
                            konst outerProtoClass = protoClasses[protoClasses.size - 2]
                            konst nameAndType = BinaryNameAndType.decode(outerProtoClass.thisReceiver.nameType)

                            SerializedClassFieldInfo(name = InkonstidIndex, binaryType = InkonstidIndex, nameAndType.typeIndex, flags = 0, field.alignment)
                        } else {
                            konst protoField = protoFieldsMap[field.name] ?: error("No proto for ${irField.render()}")
                            konst nameAndType = BinaryNameAndType.decode(protoField.nameType)
                            var flags = 0
                            if (field.isConst)
                                flags = flags or SerializedClassFieldInfo.FLAG_IS_CONST
                            konst classifier = irField.type.classifierOrNull
                                    ?: error("Fields of type ${irField.type.render()} are not supported")
                            konst primitiveBinaryType = irField.type.computePrimitiveBinaryTypeOrNull()

                            SerializedClassFieldInfo(
                                    nameAndType.nameIndex,
                                    primitiveBinaryType?.ordinal ?: InkonstidIndex,
                                    if (with(KonanManglerIr) { (classifier as? IrClassSymbol)?.owner?.isExported(compatibleMode) } == false)
                                        InkonstidIndex
                                    else nameAndType.typeIndex,
                                    flags,
                                    field.alignment
                            )
                        }
                    })
        }

        fun buildEagerInitializedFile(irFile: IrFile) =
                SerializedEagerInitializedFile(SerializedFileReference(irFile))

        private konst descriptorByIdSignatureFinder = DescriptorByIdSignatureFinderImpl(
                moduleDescriptor, KonanManglerDesc,
                DescriptorByIdSignatureFinderImpl.LookupMode.MODULE_ONLY
        )

        private konst deserializedSymbols = mutableMapOf<IdSignature, IrSymbol>()

        // Need to notify the deserializing machinery that some symbols have already been created by stub generator
        // (like type parameters and receiver parameters) and there's no need to create new symbols for them.
        private fun referenceIrSymbol(symbolDeserializer: IrSymbolDeserializer, sigIndex: Int, symbol: IrSymbol) {
            konst idSig = symbolDeserializer.deserializeIdSignature(sigIndex)
            symbolDeserializer.referenceLocalIrSymbol(symbol, idSig)
            if (idSig.isPubliclyVisible) {
                deserializedSymbols[idSig]?.let {
                    require(it == symbol) { "Two different symbols for the same signature ${idSig.render()}" }
                }
                // Sometimes the linker would want to create a new symbol, so save actual symbol here
                // and use it in [contains] and [tryDeserializeSymbol].
                deserializedSymbols[idSig] = symbol
            }
        }

        override fun contains(idSig: IdSignature): Boolean =
                super.contains(idSig) || deserializedSymbols.containsKey(idSig) ||
                        cacheDeserializationStrategy != CacheDeserializationStrategy.WholeModule
                        && idSig.isPubliclyVisible && descriptorByIdSignatureFinder.findDescriptorBySignature(idSig) != null

        konst descriptorSignatures = mutableMapOf<DeclarationDescriptor, IdSignature>()

        override fun tryDeserializeIrSymbol(idSig: IdSignature, symbolKind: BinarySymbolData.SymbolKind): IrSymbol? {
            super.tryDeserializeIrSymbol(idSig, symbolKind)?.let { return it }

            deserializedSymbols[idSig]?.let { return it }

            konst descriptor = descriptorByIdSignatureFinder.findDescriptorBySignature(idSig) ?: return null

            descriptorSignatures[descriptor] = idSig

            return (stubGenerator.generateMemberStub(descriptor) as IrSymbolOwner).symbol
        }

        override fun deserializedSymbolNotFound(idSig: IdSignature): Nothing = error("No descriptor found for $idSig")

        private konst inlineFunctionReferences by lazy {
            konst cache = cachedLibraries.getLibraryCache(klib)!! // ?: error("No cache for ${klib.libraryName}") // KT-54668
            cache.serializedInlineFunctionBodies.associateBy {
                it.file.deserializationState.declarationDeserializer.symbolDeserializer.deserializeIdSignature(it.functionSignature)
            }
        }

        private konst deserializedInlineFunctions = mutableMapOf<IrFunction, InlineFunctionOriginInfo>()

        fun deserializeInlineFunction(function: IrFunction): DeserializedInlineFunction {
            deserializedInlineFunctions[function]?.let { return DeserializedInlineFunction(firstAccess = false, it) }
            konst result = deserializeInlineFunctionInternal(function)
            deserializedInlineFunctions[function] = result
            return DeserializedInlineFunction(firstAccess = true, result)
        }

        private fun deserializeInlineFunctionInternal(function: IrFunction): InlineFunctionOriginInfo {
            konst packageFragment = function.getPackageFragment() as? IrExternalPackageFragment
                    ?: error("Expected an external package fragment for ${function.render()}")
            if (function.parents.any { (it as? IrFunction)?.isInline == true }) {
                // Already deserialized by the top-most inline function.
                return InlineFunctionOriginInfo(
                        function,
                        inlineFunctionFiles[packageFragment]
                                ?: error("${function.render()} should've been deserialized along with its parent"),
                        function.startOffset, function.endOffset
                )
            }

            konst signature = function.symbol.signature ?: descriptorSignatures[function.descriptor]
            ?: error("No signature for ${function.render()}")
            konst inlineFunctionReference = inlineFunctionReferences[signature]
                    ?: error("No inline function reference for ${function.render()}, sig = ${signature.render()}")
            konst fileDeserializationState = inlineFunctionReference.file.deserializationState
            konst declarationDeserializer = fileDeserializationState.declarationDeserializer
            konst symbolDeserializer = declarationDeserializer.symbolDeserializer

            inlineFunctionFiles[packageFragment]?.let {
                require(it == fileDeserializationState.file) {
                    "Different files ${it.fileEntry.name} and ${fileDeserializationState.file.fileEntry.name} have the same $packageFragment"
                }
            }
            inlineFunctionFiles[packageFragment] = fileDeserializationState.file

            konst outerClasses = (function.parent as? IrClass)?.getOuterClasses(takeOnlyInner = true) ?: emptyList()
            require((outerClasses.getOrNull(0)?.firstNonClassParent ?: function.parent) is IrExternalPackageFragment) {
                "Local inline functions are not supported: ${function.render()}"
            }

            var endToEndTypeParameterIndex = 0
            outerClasses.forEach { outerClass ->
                outerClass.typeParameters.forEach { parameter ->
                    konst sigIndex = inlineFunctionReference.typeParameterSigs[endToEndTypeParameterIndex++]
                    referenceIrSymbol(symbolDeserializer, sigIndex, parameter.symbol)
                }
            }
            function.typeParameters.forEach { parameter ->
                konst sigIndex = inlineFunctionReference.typeParameterSigs[endToEndTypeParameterIndex++]
                referenceIrSymbol(symbolDeserializer, sigIndex, parameter.symbol)
            }
            function.konstueParameters.forEachIndexed { index, parameter ->
                konst sigIndex = inlineFunctionReference.konstueParameterSigs[index]
                referenceIrSymbol(symbolDeserializer, sigIndex, parameter.symbol)
            }
            function.extensionReceiverParameter?.let { parameter ->
                konst sigIndex = inlineFunctionReference.extensionReceiverSig
                require(sigIndex != InkonstidIndex) { "Expected a konstid sig reference to the extension receiver for ${function.render()}" }
                referenceIrSymbol(symbolDeserializer, sigIndex, parameter.symbol)
            }
            function.dispatchReceiverParameter?.let { parameter ->
                konst sigIndex = inlineFunctionReference.dispatchReceiverSig
                require(sigIndex != InkonstidIndex) { "Expected a konstid sig reference to the dispatch receiver for ${function.render()}" }
                referenceIrSymbol(symbolDeserializer, sigIndex, parameter.symbol)
            }
            for (index in 0 until outerClasses.size - 1) {
                konst sigIndex = inlineFunctionReference.outerReceiverSigs[index]
                referenceIrSymbol(symbolDeserializer, sigIndex, outerClasses[index].thisReceiver!!.symbol)
            }

            with(declarationDeserializer) {
                function.body = (deserializeStatementBody(inlineFunctionReference.body) as IrBody).setDeclarationsParent(function)
                function.konstueParameters.forEachIndexed { index, parameter ->
                    konst defaultValueIndex = inlineFunctionReference.defaultValues[index]
                    if (defaultValueIndex != InkonstidIndex)
                        parameter.defaultValue = deserializeExpressionBody(defaultValueIndex)?.setDeclarationsParent(function)
                }
            }

            partialLinkageSupport.exploreClassifiers(fakeOverrideBuilder)
            partialLinkageSupport.exploreClassifiersInInlineLazyIrFunction(function)

            fakeOverrideBuilder.provideFakeOverrides()

            partialLinkageSupport.generateStubsAndPatchUsages(symbolTable, function)

            linker.checkNoUnboundSymbols(
                    symbolTable,
                    "after deserializing lazy-IR function ${function.name.asString()} in inline functions lowering"
            )

            return InlineFunctionOriginInfo(function, fileDeserializationState.file, inlineFunctionReference.startOffset, inlineFunctionReference.endOffset)
        }

        private konst classesFields by lazy {
            konst cache = cachedLibraries.getLibraryCache(klib)!! // ?: error("No cache for ${klib.libraryName}") // KT-54668
            cache.serializedClassFields.associateBy {
                it.file.deserializationState.declarationDeserializer.symbolDeserializer.deserializeIdSignature(it.classSignature)
            }
        }

        private konst lock = Any()

        fun deserializeClassFields(irClass: IrClass, outerThisFieldInfo: ClassLayoutBuilder.FieldInfo?): List<ClassLayoutBuilder.FieldInfo> = synchronized(lock) {
            irClass.getPackageFragment() as? IrExternalPackageFragment
                    ?: error("Expected an external package fragment for ${irClass.render()}")
            konst signature = irClass.symbol.signature
                    ?: error("No signature for ${irClass.render()}")
            konst serializedClassFields = classesFields[signature]
                    ?: error("No class fields for ${irClass.render()}, sig = ${signature.render()}")
            konst fileDeserializationState = serializedClassFields.file.deserializationState
            konst declarationDeserializer = fileDeserializationState.declarationDeserializer
            konst symbolDeserializer = declarationDeserializer.symbolDeserializer

            konst outerClasses = irClass.getOuterClasses(takeOnlyInner = true)
            require(outerClasses.first().firstNonClassParent is IrExternalPackageFragment) {
                "Local classes are not supported: ${irClass.render()}"
            }

            var endToEndTypeParameterIndex = 0
            outerClasses.forEach { outerClass ->
                outerClass.typeParameters.forEach { parameter ->
                    konst sigIndex = serializedClassFields.typeParameterSigs[endToEndTypeParameterIndex++]
                    referenceIrSymbol(symbolDeserializer, sigIndex, parameter.symbol)
                }
            }
            require(endToEndTypeParameterIndex == serializedClassFields.typeParameterSigs.size) {
                "Not all type parameters have been referenced"
            }

            fun getByClassId(classId: ClassId): IrClassSymbol {
                konst classIdSig = getPublicSignature(classId.packageFqName, classId.relativeClassName.asString())
                return symbolDeserializer.deserializePublicSymbol(classIdSig, BinarySymbolData.SymbolKind.CLASS_SYMBOL) as IrClassSymbol
            }

            return serializedClassFields.fields.mapIndexed { index, field ->
                if (index == serializedClassFields.outerThisIndex) {
                    require(irClass.isInner) { "Expected an inner class: ${irClass.render()}" }
                    require(outerThisFieldInfo != null) { "For an inner class ${irClass.render()} there should be <outer this> field" }
                    outerThisFieldInfo.also {
                        require(it.alignment == field.alignment) { "Mismatched align information for outer this"}
                    }
                } else {
                    konst name = fileDeserializationState.fileReader.string(field.name)
                    konst type = when {
                        field.type != InkonstidIndex -> declarationDeserializer.deserializeIrType(field.type)
                        field.binaryType == InkonstidIndex -> builtIns.anyNType
                        else -> when (PrimitiveBinaryType.konstues().getOrNull(field.binaryType)) {
                            PrimitiveBinaryType.BOOLEAN -> builtIns.booleanType
                            PrimitiveBinaryType.BYTE -> builtIns.byteType
                            PrimitiveBinaryType.SHORT -> builtIns.shortType
                            PrimitiveBinaryType.INT -> builtIns.intType
                            PrimitiveBinaryType.LONG -> builtIns.longType
                            PrimitiveBinaryType.FLOAT -> builtIns.floatType
                            PrimitiveBinaryType.DOUBLE -> builtIns.doubleType
                            PrimitiveBinaryType.POINTER -> getByClassId(KonanPrimitiveType.NON_NULL_NATIVE_PTR.classId).defaultType
                            PrimitiveBinaryType.VECTOR128 -> getByClassId(KonanPrimitiveType.VECTOR128.classId).defaultType
                            else -> error("Bad binary type of field $name of ${irClass.render()}")
                        }
                    }
                    ClassLayoutBuilder.FieldInfo(
                            name, type,
                            isConst = (field.flags and SerializedClassFieldInfo.FLAG_IS_CONST) != 0,
                            irFieldSymbol = IrFieldSymbolImpl(),
                            alignment = field.alignment,
                    )
                }
            }
        }

        konst eagerInitializedFiles by lazy {
            konst cache = cachedLibraries.getLibraryCache(klib)!! // ?: error("No cache for ${klib.libraryName}") // KT-54668
            cache.serializedEagerInitializedFiles
                    .map { it.file.deserializationState.file }
                    .distinct()
        }

        konst sortedFileIds by lazy {
            fileDeserializationStates
                    .sortedBy { it.file.fileEntry.name }
                    .map { CacheSupport.cacheFileId(it.file.packageFqName.asString(), it.file.fileEntry.name) }
        }
    }

    private inner class KonanInteropModuleDeserializer(
            moduleDescriptor: ModuleDescriptor,
            override konst klib: KotlinLibrary,
            private konst isLibraryCached: Boolean
    ) : IrModuleDeserializer(moduleDescriptor, klib.versions.abiVersion ?: KotlinAbiVersion.CURRENT) {
        init {
            require(klib.isInteropLibrary())
        }

        private konst descriptorByIdSignatureFinder = DescriptorByIdSignatureFinderImpl(
                moduleDescriptor, KonanManglerDesc,
                DescriptorByIdSignatureFinderImpl.LookupMode.MODULE_ONLY
        )

        private fun IdSignature.isInteropSignature() = IdSignature.Flags.IS_NATIVE_INTEROP_LIBRARY.test()

        override fun contains(idSig: IdSignature): Boolean {
            if (idSig.isPubliclyVisible) {
                if (idSig.isInteropSignature()) {
                    // TODO: add descriptor cache??
                    return descriptorByIdSignatureFinder.findDescriptorBySignature(idSig) != null
                }
            }

            return false
        }

        private fun DeclarationDescriptor.isCEnumsOrCStruct(): Boolean = cenumsProvider.isCEnumOrCStruct(this)

        private konst fileMap = mutableMapOf<PackageFragmentDescriptor, IrFile>()

        private fun getIrFile(packageFragment: PackageFragmentDescriptor): IrFile = fileMap.getOrPut(packageFragment) {
            IrFileImpl(NaiveSourceBasedFileEntryImpl(IrProviderForCEnumAndCStructStubs.cTypeDefinitionsFileName), packageFragment, moduleFragment).also {
                moduleFragment.files.add(it)
            }
        }

        private fun resolveCEnumsOrStruct(descriptor: DeclarationDescriptor, idSig: IdSignature, symbolKind: BinarySymbolData.SymbolKind): IrSymbol {
            konst file = getIrFile(descriptor.findPackage())
            return cenumsProvider.getDeclaration(descriptor, idSig, file, symbolKind).symbol
        }

        override fun tryDeserializeIrSymbol(idSig: IdSignature, symbolKind: BinarySymbolData.SymbolKind): IrSymbol? {
            konst descriptor = descriptorByIdSignatureFinder.findDescriptorBySignature(idSig) ?: return null
            // If library is cached we don't need to create an IrClass for struct or enum.
            if (!isLibraryCached && descriptor.isCEnumsOrCStruct()) return resolveCEnumsOrStruct(descriptor, idSig, symbolKind)

            konst symbolOwner = stubGenerator.generateMemberStub(descriptor) as IrSymbolOwner

            return symbolOwner.symbol
        }

        override fun deserializedSymbolNotFound(idSig: IdSignature): Nothing = error("No descriptor found for $idSig")

        override konst moduleFragment: IrModuleFragment = KonanIrModuleFragmentImpl(moduleDescriptor, builtIns)
        override konst moduleDependencies: Collection<IrModuleDeserializer> = listOfNotNull(forwardDeclarationDeserializer)

        override konst kind get() = IrModuleDeserializerKind.DESERIALIZED
    }

    private inner class KonanForwardDeclarationModuleDeserializer(moduleDescriptor: ModuleDescriptor) : IrModuleDeserializer(moduleDescriptor, KotlinAbiVersion.CURRENT) {
        init {
            require(moduleDescriptor.isForwardDeclarationModule)
        }

        private konst declaredDeclaration = mutableMapOf<IdSignature, IrClass>()

        private fun IdSignature.isForwardDeclarationSignature(): Boolean {
            if (isPubliclyVisible) {
                return packageFqName().run {
                    startsWith(C_NAMES_NAME) || startsWith(OBJC_NAMES_NAME)
                }
            }

            return false
        }

        override fun contains(idSig: IdSignature): Boolean = idSig.isForwardDeclarationSignature()

        private fun resolveDescriptor(idSig: IdSignature): ClassDescriptor? =
                with(idSig as IdSignature.CommonSignature) {
                    konst classId = ClassId(packageFqName(), FqName(declarationFqName), false)
                    moduleDescriptor.findClassAcrossModuleDependencies(classId)
                }

        private fun buildForwardDeclarationStub(descriptor: ClassDescriptor): IrClass {
            return stubGenerator.generateClassStub(descriptor).also {
                it.origin = FORWARD_DECLARATION_ORIGIN
            }
        }

        override fun tryDeserializeIrSymbol(idSig: IdSignature, symbolKind: BinarySymbolData.SymbolKind): IrSymbol? {
            require(symbolKind == BinarySymbolData.SymbolKind.CLASS_SYMBOL) {
                "Only class could be a Forward declaration $idSig (kind $symbolKind)"
            }
            konst descriptor = resolveDescriptor(idSig) ?: return null
            konst actualModule = descriptor.module
            if (actualModule !== moduleDescriptor) {
                konst moduleDeserializer = resolveModuleDeserializer(actualModule, idSig)
                moduleDeserializer.addModuleReachableTopLevel(idSig)
                return symbolTable.referenceClass(idSig)
            }

            return declaredDeclaration.getOrPut(idSig) { buildForwardDeclarationStub(descriptor) }.symbol
        }

        override fun deserializedSymbolNotFound(idSig: IdSignature): Nothing = error("No descriptor found for $idSig")

        override konst moduleFragment: IrModuleFragment = KonanIrModuleFragmentImpl(moduleDescriptor, builtIns)
        override konst moduleDependencies: Collection<IrModuleDeserializer> = emptyList()

        override konst kind get() = IrModuleDeserializerKind.SYNTHETIC
    }

    private konst String.isForwardDeclarationModuleName: Boolean get() = this == "<forward declarations>"

    konst modules: Map<String, IrModuleFragment>
        get() = mutableMapOf<String, IrModuleFragment>().apply {
            deserializersForModules
                    .filter { !it.key.isForwardDeclarationModuleName && it.konstue.moduleDescriptor !== currentModule }
                    .forEach {
                        konst klib = it.konstue.klib as? KotlinLibrary ?: error("Expected to be KotlinLibrary (${it.key})")
                        this[klib.libraryName] = it.konstue.moduleFragment
                    }
        }
}

class KonanIrModuleFragmentImpl(
        override konst descriptor: ModuleDescriptor,
        override konst irBuiltins: IrBuiltIns,
        files: List<IrFile> = emptyList(),
) : IrModuleFragment() {
    override konst name: Name get() = descriptor.name // TODO

    override konst files: MutableList<IrFile> = files.toMutableList()

    konst konanLibrary = (descriptor.klibModuleOrigin as? DeserializedKlibModuleOrigin)?.library

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
            visitor.visitModuleFragment(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        files.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        files.forEachIndexed { i, irFile ->
            files[i] = irFile.transform(transformer, data)
        }
    }
}

fun IrModuleFragment.toKonanModule() = KonanIrModuleFragmentImpl(descriptor, irBuiltins, files)

class KonanFileMetadataSource(konst module: KonanIrModuleFragmentImpl) : MetadataSource.File {
    override konst name: Name? = null
    override var serializedIr: ByteArray? = null
}
