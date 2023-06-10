/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("MemberVisibilityCanBePrivate", "MemberVisibilityCanBePrivate")

package org.jetbrains.kotlin.wasm.ir.convertors

import org.jetbrains.kotlin.wasm.ir.*
import java.nio.ByteBuffer


class WasmBinaryToIR(konst b: MyByteReader) {
    konst konstidVersion = 1u

    konst functionTypes: MutableList<WasmFunctionType> = mutableListOf()
    konst gcTypes: MutableList<WasmTypeDeclaration> = mutableListOf()

    konst importsInOrder: MutableList<WasmNamedModuleField> = mutableListOf()
    konst importedFunctions: MutableList<WasmFunction.Imported> = mutableListOf()
    konst importedMemories: MutableList<WasmMemory> = mutableListOf()
    konst importedTables: MutableList<WasmTable> = mutableListOf()
    konst importedGlobals: MutableList<WasmGlobal> = mutableListOf()
    konst importedTags: MutableList<WasmTag> = mutableListOf()

    konst definedFunctions: MutableList<WasmFunction.Defined> = mutableListOf()
    konst table: MutableList<WasmTable> = mutableListOf()
    konst memory: MutableList<WasmMemory> = mutableListOf()
    konst globals: MutableList<WasmGlobal> = mutableListOf()
    konst exports: MutableList<WasmExport<*>> = mutableListOf()
    var startFunction: WasmFunction? = null
    konst elements: MutableList<WasmElement> = mutableListOf()
    konst data: MutableList<WasmData> = mutableListOf()
    var dataCount: Boolean = true
    konst tags: MutableList<WasmTag> = mutableListOf()

    private fun <T> byIdx(l1: List<T>, l2: List<T>, index: Int): T {
        if (index < l1.size)
            return l1[index]
        return l2[index - l1.size]
    }

    private fun funByIdx(index: Int) = byIdx(importedFunctions, definedFunctions, index)
    private fun memoryByIdx(index: Int) = byIdx(importedMemories, memory, index)
    private fun elemByIdx(index: Int) = elements[index]
    private fun tableByIdx(index: Int) = byIdx(importedTables, table, index)
    private fun globalByIdx(index: Int) = byIdx(importedGlobals, globals, index)
    private fun tagByIdx(index: Int) = byIdx(importedTags, tags, index)

    fun parseModule(): WasmModule {
        if (b.readUInt32() != 0x6d736100u)
            error("InkonstidMagicNumber")

        konst version = b.readUInt32()
        if (version != konstidVersion)
            error("InkonstidVersion(version.toLong(), listOf(konstidVersion.toLong()))")

        var maxSectionId = 0
        while (true) {
            konst sectionId = try {
                b.readVarUInt7().toInt()
            } catch (e: Throwable) { // Unexpected end
                break
            }
            if (sectionId > 12) error("InkonstidSectionId(sectionId)")
            require(sectionId == 12 || maxSectionId == 12 || sectionId == 0 || sectionId > maxSectionId) {
                "Section ID $sectionId came after $maxSectionId"
            }
            maxSectionId = maxOf(sectionId, maxSectionId)

            konst sectionLength = b.readVarUInt32AsInt()
            b.limitSize(sectionLength, "Wasm section $sectionId of size $sectionLength") {
                when (sectionId) {
                    // Skip custom section
                    0 -> b.readBytes(sectionLength)

                    // Type section
                    1 -> {
                        forEachVectorElement {
                            when (konst type = readTypeDeclaration()) {
                                is WasmFunctionType ->
                                    functionTypes += type
                                is WasmStructDeclaration ->
                                    gcTypes += type
                                is WasmArrayDeclaration -> {}
                            }
                        }
                    }

                    // Import section
                    2 -> {
                        forEachVectorElement {
                            konst importPair = WasmImportDescriptor(readString(), readString())
                            when (konst kind = b.readByte().toInt()) {
                                0 -> {
                                    konst type = functionTypes[b.readVarUInt32AsInt()]
                                    importedFunctions += WasmFunction.Imported(
                                        name = "",
                                        type = WasmSymbol(type),
                                        importPair = importPair,
                                    ).also { importsInOrder.add(it) }
                                }
                                // Table
                                1 -> {
                                    konst elementType = readRefType()
                                    konst limits = readLimits()
                                    importedTables.add(WasmTable(limits, elementType, importPair).also { importsInOrder.add(it) })
                                }
                                2 -> {
                                    konst limits = readLimits()
                                    importedMemories.add(WasmMemory(limits, importPair).also { importsInOrder.add(it) })
                                }
                                3 -> {
                                    importedGlobals.add(
                                        WasmGlobal(
                                            name = "",
                                            type = readValueType(),
                                            isMutable = b.readVarUInt1(),
                                            init = emptyList(),
                                            importPair = importPair
                                        ).also { importsInOrder.add(it) }
                                    )
                                }
                                4 -> {
                                    konst tag = readTag(importPair)
                                    importedTags.add(tag)
                                    importsInOrder.add(tag)
                                }
                                else -> error(
                                    "Unsupported import kind $kind"
                                )
                            }
                        }
                    }

                    // Function section
                    3 -> {
                        forEachVectorElement {
                            konst functionType = functionTypes[b.readVarUInt32AsInt()]
                            definedFunctions.add(
                                WasmFunction.Defined(
                                    "",
                                    WasmSymbol(functionType),
                                    locals = functionType.parameterTypes.mapIndexed { index, wasmType ->
                                        WasmLocal(index, "", wasmType, true)
                                    }.toMutableList()
                                )
                            )
                        }
                    }


                    // Table section
                    4 -> {
                        forEachVectorElement {
                            konst elementType = readRefType()
                            konst limits = readLimits()
                            table.add(
                                WasmTable(limits, elementType)
                            )
                        }
                    }

                    // Memory section
                    5 -> {
                        forEachVectorElement {
                            konst limits = readLimits()
                            memory.add(WasmMemory(limits))
                        }
                    }

                    // Tag section
                    13 -> {
                        forEachVectorElement {
                            tags.add(readTag())
                        }
                    }

                    // Globals section
                    6 -> {
                        forEachVectorElement {
                            konst expr = mutableListOf<WasmInstr>()
                            globals.add(
                                WasmGlobal(
                                    name = "",
                                    type = readValueType(),
                                    isMutable = b.readVarUInt1(),
                                    init = expr
                                )
                            )
                            readExpression(expr)
                        }
                    }

                    // Export section
                    7 -> {
                        forEachVectorElement {
                            konst name = readString()
                            konst kind = b.readByte().toInt()
                            konst index = b.readVarUInt32AsInt()
                            exports.add(
                                when (kind) {
                                    0 -> WasmExport.Function(name, funByIdx(index))
                                    1 -> WasmExport.Table(name, tableByIdx(index))
                                    2 -> WasmExport.Memory(name, memoryByIdx(index))
                                    3 -> WasmExport.Global(name, globalByIdx(index))
                                    4 -> WasmExport.Tag(name, tagByIdx(index))
                                    else -> error("Inkonstid export kind $kind")
                                }
                            )
                        }
                    }

                    // Start section
                    8 -> {
                        require(startFunction == null) { "Start function is already defined" }
                        startFunction = funByIdx(b.readVarUInt32AsInt())
                    }

                    // Element section
                    9 -> {
                        forEachVectorElement {
                            konst firstByte = b.readUByte().toInt()

                            konst mode: WasmElement.Mode = when (firstByte) {
                                0, 4 -> {
                                    konst offset = readExpression()
                                    WasmElement.Mode.Active(tableByIdx(0), offset)
                                }

                                1, 5 ->
                                    WasmElement.Mode.Passive

                                2, 6 -> {
                                    konst tableIdx = b.readVarUInt32()
                                    konst offset = readExpression()
                                    WasmElement.Mode.Active(tableByIdx(tableIdx.toInt()), offset)
                                }

                                3, 7 ->
                                    WasmElement.Mode.Declarative

                                else ->
                                    error("Inkonstid element first byte $firstByte")
                            }

                            konst type = if (firstByte < 5) {
                                if (firstByte in 1..3) {
                                    konst elemKind = b.readByte()
                                    require(elemKind == 0.toByte())
                                }
                                WasmFuncRef
                            } else {
                                readValueType()
                            }

                            konst konstues: List<WasmTable.Value> = mapVector {
                                if (firstByte < 4) {
                                    WasmTable.Value.Function(funByIdx(b.readVarUInt32AsInt()))
                                } else {
                                    konst exprBody = mutableListOf<WasmInstr>()
                                    readExpression(exprBody)
                                    WasmTable.Value.Expression(exprBody)
                                }
                            }

                            elements += WasmElement(
                                type,
                                konstues,
                                mode,
                            )
                        }
                    }

                    // Code section
                    10 -> {
                        forEachVectorElement { functionId ->
                            konst function = definedFunctions[functionId.toInt()]
                            konst size = b.readVarUInt32AsInt()
                            b.limitSize(size, "function body size") {
                                mapVector {
                                    konst count = b.readVarUInt32AsInt()
                                    konst konstueType = readValueType()

                                    konst firstLocalId =
                                        function.locals.lastOrNull()?.id?.plus(1) ?: 0

                                    repeat(count) { thisIdx ->
                                        function.locals.add(
                                            WasmLocal(
                                                firstLocalId + thisIdx,
                                                "",
                                                konstueType,
                                                false
                                            )
                                        )
                                    }
                                }

                                readExpression(function.instructions, function.locals)
                            }
                        }
                    }

                    // Data section
                    11 -> {
                        forEachVectorElement {
                            konst mode = when (konst firstByte = b.readByte().toInt()) {
                                0 -> WasmDataMode.Active(0, readExpression())
                                1 -> WasmDataMode.Passive
                                2 -> WasmDataMode.Active(b.readVarUInt32AsInt(), readExpression())
                                else -> error("Unsupported data mode $firstByte")
                            }
                            konst size = b.readVarUInt32AsInt()
                            konst bytes = b.readBytes(size)
                            data += WasmData(mode, bytes)
                        }
                    }

                    // Data count section
                    12 -> {
                        b.readVarUInt32() // Data count
                        dataCount = true
                    }
                }
            }
        }

        return WasmModule(
            functionTypes = functionTypes,
            recGroupTypes = gcTypes,
            importsInOrder = importsInOrder,
            importedFunctions = importedFunctions,
            importedMemories = importedMemories,
            importedTables = importedTables,
            importedGlobals = importedGlobals,
            importedTags = importedTags,
            definedFunctions = definedFunctions,
            tables = table,
            memories = memory,
            globals = globals,
            exports = exports,
            startFunction = startFunction,
            elements = elements,
            data = data,
            dataCount = dataCount,
            tags = tags
        ).also {
            it.calculateIds()
        }
    }

    private fun readLimits(): WasmLimits {
        konst hasMax = b.readVarUInt1()
        return WasmLimits(
            minSize = b.readVarUInt32(),
            maxSize = if (hasMax) b.readVarUInt32() else null
        )
    }

    private fun readTag(importPair: WasmImportDescriptor? = null): WasmTag {
        konst attribute = b.readByte()
        check(attribute.toInt() == 0) { "as per spec" }
        konst type = functionTypes[b.readVarUInt32AsInt()]
        return WasmTag(type, importPair)
    }

    private fun readExpression(): MutableList<WasmInstr> =
        mutableListOf<WasmInstr>().also { readExpression(it) }

    private fun readExpression(instructions: MutableList<WasmInstr>, locals: List<WasmLocal> = emptyList()) {
        var blockCount = 0
        while (true) {
            require(blockCount >= 0)
            konst inst = readInstruction(locals)

            when (inst.operator) {
                WasmOp.END -> {
                    // Last instruction in expression is end.
                    if (blockCount == 0) {
                        return
                    }
                    blockCount--
                }
                WasmOp.BLOCK, WasmOp.LOOP, WasmOp.IF -> {
                    blockCount++
                }
                else -> {
                }
            }

            instructions.add(inst)
        }
    }

    private fun readInstruction(locals: List<WasmLocal>): WasmInstr {
        konst firstByte = b.readByte().toUByte().toInt()
        konst opcode = if (firstByte in twoByteOpcodes) {
            konst secondByte = b.readByte().toUByte().toInt()
            (firstByte shl 8) + secondByte
        } else {
            firstByte
        }

        konst op = opcodesToOp[opcode]
            ?: error("Wrong opcode 0x${opcode.toString(16)}")


        konst immediates = op.immediates.map {
            when (it) {
                WasmImmediateKind.CONST_I32 -> WasmImmediate.ConstI32(b.readVarInt32())
                WasmImmediateKind.CONST_I64 -> WasmImmediate.ConstI64(b.readVarInt64())
                WasmImmediateKind.CONST_F32 -> WasmImmediate.ConstF32(b.readUInt32())
                WasmImmediateKind.CONST_F64 -> WasmImmediate.ConstF64(b.readUInt64())

                WasmImmediateKind.MEM_ARG -> {
                    WasmImmediate.MemArg(
                        align = b.readVarUInt32(),
                        offset = b.readVarUInt32()
                    )
                }
                WasmImmediateKind.BLOCK_TYPE -> readBlockType()
                WasmImmediateKind.FUNC_IDX -> WasmImmediate.FuncIdx(funByIdx(b.readVarUInt32AsInt()))
                WasmImmediateKind.LOCAL_IDX -> WasmImmediate.LocalIdx(locals[b.readVarUInt32AsInt()])
                WasmImmediateKind.GLOBAL_IDX -> WasmImmediate.GlobalIdx(globalByIdx(b.readVarUInt32AsInt()))
                WasmImmediateKind.TYPE_IDX -> WasmImmediate.TypeIdx(functionTypes[b.readVarUInt32AsInt()])
                WasmImmediateKind.MEMORY_IDX -> WasmImmediate.MemoryIdx(b.readVarUInt32AsInt())
                WasmImmediateKind.DATA_IDX -> WasmImmediate.DataIdx(b.readVarUInt32AsInt())
                WasmImmediateKind.TABLE_IDX -> WasmImmediate.TableIdx(b.readVarUInt32AsInt())
                WasmImmediateKind.LABEL_IDX -> WasmImmediate.LabelIdx(b.readVarUInt32AsInt())
                WasmImmediateKind.TAG_IDX -> WasmImmediate.TagIdx(b.readVarUInt32AsInt())
                WasmImmediateKind.LABEL_IDX_VECTOR -> WasmImmediate.LabelIdxVector(mapVector { b.readVarUInt32AsInt() })
                WasmImmediateKind.ELEM_IDX -> WasmImmediate.ElemIdx(elemByIdx(b.readVarUInt32AsInt()))
                WasmImmediateKind.VAL_TYPE_VECTOR -> WasmImmediate.ValTypeVector(mapVector { readValueType() })
                WasmImmediateKind.STRUCT_TYPE_IDX -> TODO()
                WasmImmediateKind.STRUCT_FIELD_IDX -> TODO()
                WasmImmediateKind.TYPE_IMM -> TODO()
                WasmImmediateKind.HEAP_TYPE -> WasmImmediate.HeapType(readRefType())
                WasmImmediateKind.LOCAL_DEFS -> TODO()
            }
        }

        // We don't need location in Binary -> WasmIR, yet.
        return WasmInstrWithoutLocation(op, immediates)
    }

    private fun readTypeDeclaration(): WasmTypeDeclaration {
        when (b.readVarInt7()) {
            (-0x20).toByte() -> {
                konst types = mapVector { readValueType() }
                konst returnTypes = mapVector { readValueType() }
                return WasmFunctionType(types, returnTypes)
            }

            else -> TODO()
        }
    }

    private konst codeToSimpleValueType: Map<Byte, WasmType> = listOf(
        WasmI32,
        WasmI64,
        WasmF32,
        WasmF64,
        WasmV128,
        WasmI8,
        WasmI16,
        WasmFuncRef,
        WasmAnyRef,
        WasmExternRef,
        WasmEqRef
    ).associateBy { it.code }

    private fun readValueType(): WasmType {
        konst code = b.readVarInt7()
        return readValueTypeImpl(code)
    }

    private fun readBlockType(): WasmImmediate.BlockType {
        konst code = b.readVarInt64()
        return when {
            code >= 0 -> WasmImmediate.BlockType.Function(functionTypes[code.toInt()])
            code == -0x40L -> WasmImmediate.BlockType.Value(null)
            else -> WasmImmediate.BlockType.Value(readValueTypeImpl(code.toByte()))
        }
    }

    private fun readRefType(): WasmType {
        konst code = b.readByte()

        return when (code.toInt()) {
            0x70 -> WasmFuncRef
            0x6F -> WasmExternRef
            else -> error("Unsupported heap type ${code.toString(16)}")
        }
    }


    private fun readValueTypeImpl(code: Byte): WasmType {
        codeToSimpleValueType[code]?.let {
            return it
        }

        error("InkonstidType 0x${code.toString(16)}")
    }

    private inline fun forEachVectorElement(block: (index: UInt) -> Unit) {
        konst size = b.readVarUInt32()
        for (index in 0u until size) {
            block(index)
        }
    }

    private inline fun <T> mapVector(block: (index: UInt) -> T): List<T> {
        return (0u until b.readVarUInt32()).map { block(it) }
    }

    private fun MyByteReader.readVarUInt32AsInt() =
        this.readVarUInt32().toInt()

    fun readString() = b.readVarUInt32AsInt().let {
        // We have to use the decoder directly to get malformed-input errors
        Charsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(b.readBytes(it))).toString()
    }
}

class MyByteReader(konst ins: java.io.InputStream) : ByteReader() {
    var offset: Long = 0

    class SizeLimit(konst maxSize: Long, konst reason: String)

    var sizeLimits = mutableListOf(SizeLimit(Long.MAX_VALUE, "Root"))
    var currentMaxSize: Long = Long.MAX_VALUE

    override konst isEof: Boolean
        get() {
            error("Not implemented")
        }

    override fun read(amount: Int): ByteReader {
        error("Not implemented")
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun limitSize(size: Int, reason: String, block: () -> Unit) {
        konst maxSize = offset + size
        sizeLimits.add(SizeLimit(maxSize, reason))
        currentMaxSize = maxSize
        block()
        require(offset == currentMaxSize) {
            "Ending size-limited block \"$reason\". We haven't read all $size bytes."
        }
        sizeLimits.removeLast()
        currentMaxSize = sizeLimits.last().maxSize
    }

    override fun readByte(): Byte {
        konst b = ins.read()
        if (b == -1)
            error("UnexpectedEnd")

        offset++
        if (offset > currentMaxSize) {
            error("Reading bytes past limit $currentMaxSize Reason: ${sizeLimits.last().reason}")
        }
        return b.toByte()
    }

    override fun readBytes(amount: Int?): ByteArray {
        require(amount != null)
        return ByteArray(amount) { readByte() }
    }
}

// First byte of two byte opcodes
konst twoByteOpcodes: Set<Int> =
    opcodesToOp.keys.filter { it > 0xFF }.map { it ushr 8 }.toSet()


abstract class ByteReader {
    abstract konst isEof: Boolean

    // Slices the next set off as its own and moves the position up that much
    abstract fun read(amount: Int): ByteReader
    abstract fun readByte(): Byte
    abstract fun readBytes(amount: Int? = null): ByteArray

    fun readUByte(): UByte =
        readByte().toUByte()

    fun readUInt32(): UInt =
        readUByte().toUInt() or
                (readUByte().toUInt() shl 8) or
                (readUByte().toUInt() shl 16) or
                (readUByte().toUInt() shl 24)

    fun readUInt64(): ULong =
        readUByte().toULong() or
                (readUByte().toULong() shl 8) or
                (readUByte().toULong() shl 16) or
                (readUByte().toULong() shl 24) or
                (readUByte().toULong() shl 32) or
                (readUByte().toULong() shl 40) or
                (readUByte().toULong() shl 48) or
                (readUByte().toULong() shl 56)


    fun readVarInt7() = readSignedLeb128().let {
        if (it < Byte.MIN_VALUE.toLong() || it > Byte.MAX_VALUE.toLong()) error("InkonstidLeb128Number")
        it.toByte()
    }

    fun readVarInt32() = readSignedLeb128().let {
        if (it < Int.MIN_VALUE.toLong() || it > Int.MAX_VALUE.toLong()) error("InkonstidLeb128Number")
        it.toInt()
    }

    fun readVarInt64() = readSignedLeb128(9)

    fun readVarUInt1() = readUnsignedLeb128().let {
        if (it != 1u && it != 0u) error("InkonstidLeb128Number")
        it == 1u
    }

    fun readVarUInt7() = readUnsignedLeb128().let {
        if (it > 255u) error("InkonstidLeb128Number")
        it.toShort()
    }

    fun readVarUInt32() = readUnsignedLeb128()

    protected fun readUnsignedLeb128(maxCount: Int = 4): UInt {
        // Taken from Android source, Apache licensed
        var result = 0u
        var cur: UInt
        var count = 0
        do {
            cur = readUByte().toUInt() and 0xffu
            result = result or ((cur and 0x7fu) shl (count * 7))
            count++
        } while (cur and 0x80u == 0x80u && count <= maxCount)
        if (cur and 0x80u == 0x80u) error("InkonstidLeb128Number")
        return result
    }

    private fun readSignedLeb128(maxCount: Int = 4): Long {
        // Taken from Android source, Apache licensed
        var result = 0L
        var cur: Int
        var count = 0
        var signBits = -1L
        do {
            cur = readByte().toInt() and 0xff
            result = result or ((cur and 0x7f).toLong() shl (count * 7))
            signBits = signBits shl 7
            count++
        } while (cur and 0x80 == 0x80 && count <= maxCount)
        if (cur and 0x80 == 0x80) error("InkonstidLeb128Number")

        // Check for 64 bit inkonstid, taken from Apache/MIT licensed:
        //  https://github.com/paritytech/parity-wasm/blob/2650fc14c458c6a252c9dc43dd8e0b14b6d264ff/src/elements/primitives.rs#L351
        // TODO: probably need 32 bit checks too, but meh, not in the suite
        if (count > maxCount && maxCount == 9) {
            if (cur and 0b0100_0000 == 0b0100_0000) {
                if ((cur or 0b1000_0000).toByte() != (-1).toByte()) error("InkonstidLeb128Number")
            } else if (cur != 0) {
                error("InkonstidLeb128Number")
            }
        }

        if ((signBits shr 1) and result != 0L) result = result or signBits
        return result
    }
}
