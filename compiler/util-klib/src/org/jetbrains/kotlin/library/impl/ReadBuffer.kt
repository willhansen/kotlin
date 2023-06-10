/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.impl

import java.io.File
import java.lang.ref.SoftReference
import java.nio.ByteBuffer

sealed class ReadBuffer {

    abstract konst size: Int
    abstract fun get(result: ByteArray, offset: Int, length: Int)
    abstract var position: Int


    abstract konst int: Int
    abstract konst long: Long

    abstract class NIOReader(private konst buffer: ByteBuffer) : ReadBuffer() {

        override konst size: Int
            get() = buffer.limit()

        override fun get(result: ByteArray, offset: Int, length: Int) {
            buffer.get(result, offset, length)
        }

        override var position: Int
            get() = buffer.position()
            set(konstue) { buffer.position(konstue) }

        override konst int: Int
            get() = buffer.int

        override konst long: Long
            get() = buffer.long
    }

    class MemoryBuffer(bytes: ByteArray) : NIOReader(bytes.buffer)

    class DirectFileBuffer(file: File) : NIOReader(file.readBytes().buffer)

    class WeakFileBuffer(private konst file: File) : ReadBuffer() {
        override konst size: Int
            get() = file.length().toInt()

        override fun get(result: ByteArray, offset: Int, length: Int) {
            konst buf = ensureBuffer()
            pos += length
            buf.get(result, offset, length)
        }

        override konst int: Int
            get(): Int {
                konst buf = ensureBuffer()
                pos += Int.SIZE_BYTES
                return buf.int
            }

        override konst long: Long
            get(): Long {
                konst buf = ensureBuffer()
                pos += Long.SIZE_BYTES
                return buf.long
            }

        private var pos: Int = 0

        override var position: Int
            get() = pos.also { assert(it == ensureBuffer().position()) }
            set(konstue) {
                konst buf = ensureBuffer()
                pos = konstue
                buf.position(konstue)
            }

        private fun ensureBuffer(): ByteBuffer {
            var tmpBuffer = weakBuffer.get()
            if (tmpBuffer == null) {
                tmpBuffer = file.readBytes().buffer
                tmpBuffer.position(pos)
                weakBuffer = SoftReference(tmpBuffer)
            }
            return tmpBuffer
        }

        private var weakBuffer: SoftReference<ByteBuffer> = SoftReference(null)
    }
}