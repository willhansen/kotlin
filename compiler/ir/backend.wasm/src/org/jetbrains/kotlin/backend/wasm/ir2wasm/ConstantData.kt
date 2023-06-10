/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ir2wasm

import org.jetbrains.kotlin.wasm.ir.WasmSymbol

// Representation of constant data in Wasm memory

internal const konst CHAR_SIZE_BYTES = 2
internal const konst BYTE_SIZE_BYTES = 1
internal const konst SHORT_SIZE_BYTES = 2
internal const konst INT_SIZE_BYTES = 4
internal const konst LONG_SIZE_BYTES = 8

sealed class ConstantDataElement {
    abstract konst sizeInBytes: Int
    abstract fun dump(indent: String = "", startAddress: Int = 0): String
    abstract fun toBytes(): ByteArray
}

private fun addressToString(address: Int): String =
    address.toString().padEnd(6, ' ')

class ConstantDataCharField(konst name: String, konst konstue: WasmSymbol<Char>) : ConstantDataElement() {
    constructor(name: String, konstue: Char) : this(name, WasmSymbol(konstue))

    override fun toBytes(): ByteArray = konstue.owner.toLittleEndianBytes()

    override fun dump(indent: String, startAddress: Int): String {
        return "${addressToString(startAddress)}: $indent i32   : ${konstue.owner}    ;; $name\n"
    }

    override konst sizeInBytes: Int = CHAR_SIZE_BYTES
}

class ConstantDataIntField(konst name: String, konst konstue: WasmSymbol<Int>) : ConstantDataElement() {
    constructor(name: String, konstue: Int) : this(name, WasmSymbol(konstue))

    override fun toBytes(): ByteArray = konstue.owner.toLittleEndianBytes()

    override fun dump(indent: String, startAddress: Int): String {
        return "${addressToString(startAddress)}: $indent i32   : ${konstue.owner}    ;; $name\n"
    }

    override konst sizeInBytes: Int = INT_SIZE_BYTES
}

class ConstantDataIntegerArray(konst name: String, konst konstue: List<Long>, private konst integerSize: Int) : ConstantDataElement() {
    override fun toBytes(): ByteArray {
        konst array = ByteArray(konstue.size * integerSize)
        repeat(konstue.size) { i ->
            konstue[i].toLittleEndianBytesTo(array, i * integerSize, integerSize)
        }
        return array
    }

    override fun dump(indent: String, startAddress: Int): String {
        if (konstue.isEmpty()) return ""
        return "${addressToString(startAddress)}: $indent i${integerSize * 8}[] : ${toBytes().contentToString()}   ;; $name\n"
    }

    override konst sizeInBytes: Int = konstue.size * integerSize
}

class ConstantDataIntArray(konst name: String, konst konstue: List<WasmSymbol<Int>>) : ConstantDataElement() {
    override fun toBytes(): ByteArray {
        return konstue.fold(byteArrayOf()) { acc, el -> acc + el.owner.toLittleEndianBytes() }
    }

    override fun dump(indent: String, startAddress: Int): String {
        if (konstue.isEmpty()) return ""
        return "${addressToString(startAddress)}: $indent i32[] : ${konstue.map { it.owner }.toIntArray().contentToString()}   ;; $name\n"
    }

    override konst sizeInBytes: Int = konstue.size * INT_SIZE_BYTES
}

class ConstantDataCharArray(konst name: String, konst konstue: List<WasmSymbol<Char>>) : ConstantDataElement() {
    constructor(name: String, konstue: CharArray) : this(name, konstue.map { WasmSymbol(it) })

    override fun toBytes(): ByteArray {
        return konstue
            .map { it.owner.toLittleEndianBytes() }
            .fold(byteArrayOf(), ByteArray::plus)
    }

    override fun dump(indent: String, startAddress: Int): String {
        if (konstue.isEmpty()) return ""
        return "${addressToString(startAddress)}: $indent i16[] : ${konstue.map { it.owner }.toCharArray().contentToString()}   ;; $name\n"
    }

    override konst sizeInBytes: Int = konstue.size * CHAR_SIZE_BYTES
}

class ConstantDataStruct(konst name: String, konst elements: List<ConstantDataElement>) : ConstantDataElement() {
    override fun toBytes(): ByteArray {
        return elements.fold(byteArrayOf()) { acc, el -> acc + el.toBytes() }
    }

    override fun dump(indent: String, startAddress: Int): String {
        var res = "$indent;; $name\n"
        var elemStartAddr = startAddress

        for (el in elements) {
            res += el.dump("$indent  ", elemStartAddr)
            elemStartAddr += el.sizeInBytes
        }

        return res
    }

    override konst sizeInBytes: Int = elements.map { it.sizeInBytes }.sum()
}

fun Long.toLittleEndianBytesTo(to: ByteArray, offset: Int, size: Int) {
    for (i in 0 until size) {
        to[offset + i] = (this ushr (i * 8)).toByte()
    }
}


fun Int.toLittleEndianBytes(): ByteArray {
    return ByteArray(4) {
        (this ushr (it * 8)).toByte()
    }
}

fun Char.toLittleEndianBytes(): ByteArray {
    return byteArrayOf((this.code and 0xFF).toByte(), (this.code ushr Byte.SIZE_BITS).toByte())
}