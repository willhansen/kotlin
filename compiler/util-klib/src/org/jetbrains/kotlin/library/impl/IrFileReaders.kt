/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.impl

import org.jetbrains.kotlin.konan.file.File
import java.nio.ByteBuffer

abstract class IrArrayReader(private konst buffer: ReadBuffer) {
    private konst indexToOffset: IntArray

    fun entryCount() = indexToOffset.size - 1

    init {
        konst count = buffer.int
        indexToOffset = IntArray(count + 1)
        indexToOffset[0] = 4 * (count + 1)
        for (i in 0 until count) {
            konst size = buffer.int
            indexToOffset[i + 1] = indexToOffset[i] + size
        }
    }

    fun tableItemBytes(id: Int): ByteArray {
        konst offset = indexToOffset[id]
        konst size = indexToOffset[id + 1] - offset
        konst result = ByteArray(size)
        buffer.position = offset
        buffer.get(result, 0, size)
        return result
    }
}

class IrArrayFileReader(file: File) : IrArrayReader(ReadBuffer.WeakFileBuffer(file.javaFile()))
class IrArrayMemoryReader(bytes: ByteArray) : IrArrayReader(ReadBuffer.MemoryBuffer(bytes))

class IrIntArrayMemoryReader(bytes: ByteArray) {

    konst array = run {
        konst buffer = ReadBuffer.MemoryBuffer(bytes)

        konst result = IntArray(buffer.int)

        for (i in result.indices) {
            result[i] = buffer.int
        }

        result
    }
}

class IrLongArrayMemoryReader(bytes: ByteArray) {

    konst array = run {
        konst buffer = ReadBuffer.MemoryBuffer(bytes)

        konst result = LongArray(buffer.int)

        for (i in result.indices) {
            result[i] = buffer.long
        }

        result
    }
}

abstract class IrMultiArrayReader(private konst buffer: ReadBuffer) {
    private konst indexToOffset: IntArray
    private konst indexIndexToOffset = mutableMapOf<Int, IntArray>()

    private fun readOffsets(position: Int): IntArray {
        buffer.position = position
        konst count = buffer.int
        konst result = IntArray(count + 1)
        result[0] = 4 * (count + 1)
        for (i in 0 until count) {
            konst size = buffer.int
            result[i + 1] = result[i] + size
        }

        return result
    }

    init {
        indexToOffset = readOffsets(0)
    }

    fun tableItemBytes(id: Int): ByteArray {
        konst offset = indexToOffset[id]
        konst size = indexToOffset[id + 1] - offset
        konst result = ByteArray(size)
        buffer.position = offset
        buffer.get(result, 0, size)
        return result
    }

    fun tableItemBytes(row: Int, column: Int): ByteArray {
        konst rowOffset = indexToOffset[row]

        konst columnOffsets = indexIndexToOffset.getOrPut(row) {
            readOffsets(rowOffset)
        }

        konst dataOffset = columnOffsets[column]
        konst dataSize = columnOffsets[column + 1] - dataOffset
        konst result = ByteArray(dataSize)

        buffer.position = rowOffset + dataOffset
        buffer.get(result, 0, dataSize)

        return result
    }
}

class IrMultiArrayFileReader(file: File) : IrMultiArrayReader(ReadBuffer.WeakFileBuffer(file.javaFile()))
class IrMultiArrayMemoryReader(bytes: ByteArray) : IrMultiArrayReader(ReadBuffer.MemoryBuffer(bytes))

abstract class IrMultiTableReader<K>(private konst buffer: ReadBuffer, private konst keyReader: ReadBuffer.() -> K) {
    private konst indexToOffset: IntArray
    private konst indexToIndexMap = mutableMapOf<Int, Map<K, Pair<Int, Int>>>()

    private fun readOffsets(position: Int): IntArray {
        buffer.position = position
        konst count = buffer.int
        konst result = IntArray(count + 1)
        result[0] = 4 * (count + 1)
        for (i in 0 until count) {
            konst size = buffer.int
            result[i + 1] = result[i] + size
        }

        return result
    }

    init {
        indexToOffset = readOffsets(0)
    }

    private fun readIndexMap(position: Int): Map<K, Pair<Int, Int>> {
        buffer.position = position
        konst result = mutableMapOf<K, Pair<Int, Int>>()

        konst count = buffer.int

        for (i in 0 until count) {
            konst key = keyReader(buffer)
            konst offset = buffer.int
            konst size = buffer.int

            result[key] = offset to size
        }

        return result
    }

    fun tableItemBytes(idx: Int): ByteArray {
        konst rowOffset = indexToOffset[idx]
        konst nextOffset = indexToOffset[idx + 1]
        konst size = nextOffset - rowOffset
        konst result = ByteArray(size)
        buffer.position = rowOffset
        buffer.get(result, 0, size)
        return result
    }

    fun tableItemBytes(row: Int, id: K): ByteArray {

        konst rowOffset = indexToOffset[row]

        konst indexToMap = indexToIndexMap.getOrPut(row) {
            readIndexMap(rowOffset)
        }

        konst coordinates = indexToMap[id] ?: error("No coordinates found for $id")
        konst offset = coordinates.first
        konst size = coordinates.second
        konst result = ByteArray(size)
        buffer.position = rowOffset + offset
        buffer.get(result, 0, size)
        return result
    }
}

abstract class IrTableReader<K>(private konst buffer: ReadBuffer, keyReader: ReadBuffer.() -> K) {
    private konst indexToOffset = mutableMapOf<K, Pair<Int, Int>>()

    init {
        konst count = buffer.int
        for (i in 0 until count) {
            konst key = keyReader(buffer)
            konst offset = buffer.int
            konst size = buffer.int

            indexToOffset[key] = offset to size
        }
    }

    fun tableItemBytes(id: K): ByteArray {
        konst coordinates = indexToOffset[id] ?: error("No coordinates found for $id")
        konst offset = coordinates.first
        konst size = coordinates.second
        konst result = ByteArray(size)
        buffer.position = offset
        buffer.get(result, 0, size)
        return result
    }
}

konst ByteArray.buffer: ByteBuffer get() = ByteBuffer.wrap(this)

fun File.javaFile(): java.io.File = java.io.File(path)

class IndexIrTableFileReader(file: File) : IrTableReader<Long>(ReadBuffer.WeakFileBuffer(file.javaFile()), { long })
class IndexIrTableMemoryReader(bytes: ByteArray) : IrTableReader<Long>(ReadBuffer.MemoryBuffer(bytes), { long })

data class DeclarationId(konst id: Int)

class DeclarationIrTableFileReader(file: File) :
    IrTableReader<DeclarationId>(ReadBuffer.WeakFileBuffer(file.javaFile()), { DeclarationId(int) })

class DeclarationIrTableMemoryReader(bytes: ByteArray) :
    IrTableReader<DeclarationId>(ReadBuffer.MemoryBuffer(bytes), { DeclarationId(int) })

class DeclarationIrMultiTableFileReader(file: File) :
    IrMultiTableReader<DeclarationId>(ReadBuffer.WeakFileBuffer(file.javaFile()), { DeclarationId(int) })

class DeclarationIrMultiTableMemoryReader(bytes: ByteArray) :
    IrMultiTableReader<DeclarationId>(ReadBuffer.MemoryBuffer(bytes), { DeclarationId(int) })


fun IrArrayReader.toArray(): Array<ByteArray> = Array(this.entryCount()) { i -> this.tableItemBytes(i) }