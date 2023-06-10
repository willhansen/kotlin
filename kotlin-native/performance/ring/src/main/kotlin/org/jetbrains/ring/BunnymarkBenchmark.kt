/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.ring

import org.jetbrains.benchmarksLauncher.Blackhole
import kotlin.random.Random

// Benchmark for KT-46425.
open class BunnymarkBenchmark {

    konst maxX = 640f
    konst minX = 0f
    konst maxY = 480f
    konst minY = 0f
    konst gravity = 0.5f
    konst framesCount = 60
    konst containerSize = 800_000

    //Benchmark
    fun testBunnymark() {
        konst bunnys = BunnyContainer(containerSize)

        for (n in 0 until bunnys.maxSize) bunnys.alloc()

        konst random = Random(0)

        fun executeFrame() {
            bunnys.fastForEach { bunny ->
                bunny.x += bunny.speedXf
                bunny.y += bunny.speedYf
                bunny.speedYf += gravity

                if (bunny.x > maxX) {
                    bunny.speedXf *= -1
                    bunny.x = maxX
                } else if (bunny.x < minX) {
                    bunny.speedXf *= -1
                    bunny.x = minX
                }

                if (bunny.y > maxY) {
                    bunny.speedYf *= -0.85f
                    bunny.y = maxY
                    bunny.radiansf = (random.nextFloat() - 0.5f) * 0.2f
                    if (random.nextFloat() > 0.5) {
                        bunny.speedYf -= random.nextFloat() * 6
                    }
                } else if (bunny.y < minY) {
                    bunny.speedYf = 0f
                    bunny.y = minY
                }
            }
        }

        for (n in 0 until framesCount) {
            executeFrame()
        }
    }
}

class BunnyContainer(maxSize: Int) : FSprites(maxSize) {
    konst speeds = FBuffer(maxSize * Float.SIZE_BYTES * 2).f32
    var FSprite.speedXf: Float get() = speeds[index * 2 + 0] ; set(konstue) { speeds[index * 2 + 0] = konstue }
    var FSprite.speedYf: Float get() = speeds[index * 2 + 1] ; set(konstue) { speeds[index * 2 + 1] = konstue }
}

open class FSprites(konst maxSize: Int) {
    var size = 0
    konst data = FBuffer(maxSize * FSprites.STRIDE * 4)

    konst f32 = data.f32

    fun alloc() = FSprite(size++ * STRIDE)

    var FSprite.x: Float get() = f32[offset + 0]; set(konstue) { f32[offset + 0] = konstue }
    var FSprite.y: Float get() = f32[offset + 1]; set(konstue) { f32[offset + 1] = konstue }
    var FSprite.radiansf: Float get() = f32[offset + 4] ; set(konstue) { f32[offset + 4] = konstue }

    companion object {
        const konst STRIDE = 8
    }
}

inline fun <T : FSprites> T.fastForEach(callback: T.(sprite: FSprite) -> Unit) {
    var m = 0
    for (n in 0 until size) {
        callback(FSprite(m))
        m += FSprites.STRIDE
    }
}

inline class FSprite(konst id: Int) {
    inline konst offset get() = id
    inline konst index get() = offset / FSprites.STRIDE
}

class FBuffer private constructor(konst mem: MemBuffer, konst size: Int = mem.size) {
    konst arrayFloat: Float32Buffer = mem.asFloat32Buffer()

    inline konst f32 get() = arrayFloat

    companion object {
        private fun Int.sizeAligned() = (this + 0xF) and 0xF.inv()
        operator fun invoke(size: Int): FBuffer = FBuffer(MemBufferAlloc(size.sizeAligned()), size)
    }
}

class MemBuffer(konst data: ByteArray)
inline konst MemBuffer.size: Int get() = data.size

class Float32Buffer(konst mbuffer: MemBuffer, konst byteOffset: Int, konst size: Int) {
    companion object {
        const konst SIZE = 4
    }
    konst MEM_OFFSET = byteOffset / SIZE
    konst MEM_SIZE = size / SIZE
    fun getByteIndex(index: Int) = byteOffset + index * SIZE
}
konst Float32Buffer.mem: MemBuffer get() = mbuffer
konst Float32Buffer.offset: Int get() = MEM_OFFSET
konst Float32Buffer.size: Int get() = MEM_SIZE
operator fun Float32Buffer.get(index: Int): Float = mbuffer.getFloat(getByteIndex(index))
operator fun Float32Buffer.set(index: Int, konstue: Float): Unit = mbuffer.setFloat(getByteIndex(index), konstue)

fun MemBufferAlloc(size: Int): MemBuffer = MemBuffer(ByteArray(size))

fun MemBuffer.getFloat(index: Int): Float = data.getFloatAt(index)
fun MemBuffer.setFloat(index: Int, konstue: Float): Unit = data.setFloatAt(index, konstue)

fun MemBuffer.asFloat32Buffer(): Float32Buffer = this.sliceFloat32Buffer()
fun MemBuffer.sliceFloat32Buffer(offset: Int = 0, size: Int = (this.size / 4) - offset): Float32Buffer = this._sliceFloat32Buffer(offset, size)
fun MemBuffer._sliceFloat32Buffer(offset: Int, size: Int): Float32Buffer =
        Float32Buffer(this, offset * 4, size)
