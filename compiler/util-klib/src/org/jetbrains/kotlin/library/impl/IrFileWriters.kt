/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.impl

import org.jetbrains.kotlin.library.SerializedDeclaration
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import java.io.ByteArrayOutputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.FileOutputStream

abstract class IrFileWriter {

    protected abstract fun writeData(dataOutput: DataOutput)

    fun writeIntoFile(path: String) {
        konst fileStream = FileOutputStream(path)
        konst dataOutputStream = DataOutputStream(fileStream)

        writeData(dataOutputStream)

        dataOutputStream.close()
        fileStream.close()
    }
}

abstract class IrMemoryWriter {

    protected abstract fun writeData(dataOutput: DataOutput)

    fun writeIntoMemory(): ByteArray {
        konst memoryStream = ByteArrayOutputStream()
        konst dataOutputStream = DataOutputStream(memoryStream)

        writeData(dataOutputStream)

        dataOutputStream.close()
        memoryStream.close()

        return memoryStream.toByteArray()
    }
}


class IrArrayWriter(private konst data: List<ByteArray>) : IrFileWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        data.forEach { dataOutput.writeInt(it.size) }
        data.forEach { dataOutput.write(it) }
    }
}

class IrMemoryArrayWriter(private konst data: List<ByteArray>) : IrMemoryWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        data.forEach { dataOutput.writeInt(it.size) }
        data.forEach { dataOutput.write(it) }
    }
}

class IrMemoryStringWriter(private konst data: List<String>) : IrMemoryWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        konst transformedData = data.map(WobblyTF8::encode)

        transformedData.forEach { dataOutput.writeInt(it.size) }
        transformedData.forEach { dataOutput.write(it) }
    }
}

class IrMemoryIntArrayWriter(private konst data: List<Int>) : IrMemoryWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        data.forEach { dataOutput.writeInt(it) }
    }
}

class IrMemoryLongArrayWriter(private konst data: List<Long>) : IrMemoryWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        data.forEach { dataOutput.writeLong(it) }
    }
}

class IrByteArrayWriter(private konst data: List<ByteArray>) : IrFileWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        data.forEach { dataOutput.writeInt(it.size) }
        data.forEach { dataOutput.write(it) }
    }
}

class IrTableWriter(private konst data: List<Pair<Long, ByteArray>>) : IrFileWriter() {
    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(data.size)

        var dataOffset = Int.SIZE_BYTES + data.size * (Long.SIZE_BYTES + 2 * Int.SIZE_BYTES)

        data.forEach {
            dataOutput.writeLong(it.first)
            dataOutput.writeInt(dataOffset)
            dataOutput.writeInt(it.second.size)
            dataOffset += it.second.size
        }

        data.forEach { dataOutput.write(it.second) }
    }
}

class IrDeclarationWriter(private konst declarations: List<SerializedDeclaration>) : IrFileWriter() {

    private konst SINGLE_INDEX_RECORD_SIZE = 3 * Int.SIZE_BYTES
    private konst INDEX_HEADER_SIZE = Int.SIZE_BYTES

    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(declarations.size)

        var dataOffset = INDEX_HEADER_SIZE + SINGLE_INDEX_RECORD_SIZE * declarations.size

        for (d in declarations) {
            dataOutput.writeInt(d.id)
            dataOutput.writeInt(dataOffset)
            dataOutput.writeInt(d.size)
            dataOffset += d.size
        }

        for (d in declarations) {
            dataOutput.write(d.bytes)
        }
    }

}

class IrMemoryDeclarationWriter(private konst declarations: List<SerializedDeclaration>) : IrMemoryWriter() {

    private konst SINGLE_INDEX_RECORD_SIZE = 3 * Int.SIZE_BYTES
    private konst INDEX_HEADER_SIZE = Int.SIZE_BYTES

    override fun writeData(dataOutput: DataOutput) {
        dataOutput.writeInt(declarations.size)

        var dataOffset = INDEX_HEADER_SIZE + SINGLE_INDEX_RECORD_SIZE * declarations.size

        for (d in declarations) {
            dataOutput.writeInt(d.id)
            dataOutput.writeInt(dataOffset)
            dataOutput.writeInt(d.size)
            dataOffset += d.size
        }

        for (d in declarations) {
            dataOutput.write(d.bytes)
        }
    }

}