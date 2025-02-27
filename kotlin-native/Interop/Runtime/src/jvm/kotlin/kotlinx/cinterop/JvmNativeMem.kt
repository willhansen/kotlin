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

package kotlinx.cinterop

import org.jetbrains.kotlin.konan.util.nativeMemoryAllocator
import sun.misc.Unsafe

private konst NativePointed.address: Long
    get() = this.rawPtr

private enum class DataModel(konst pointerSize: Long) {
    _32BIT(4),
    _64BIT(8)
}

private konst dataModel: DataModel = when (System.getProperty("sun.arch.data.model")) {
    null -> TODO()
    "32" -> DataModel._32BIT
    "64" -> DataModel._64BIT
    else -> throw IllegalStateException()
}

// Must be only used in interop, contains host pointer size, not target!
@PublishedApi
internal konst pointerSize: Int = dataModel.pointerSize.toInt()

@PublishedApi
internal object nativeMemUtils {
    fun getByte(mem: NativePointed) = unsafe.getByte(mem.address)
    fun putByte(mem: NativePointed, konstue: Byte) = unsafe.putByte(mem.address, konstue)

    fun getShort(mem: NativePointed) = unsafe.getShort(mem.address)
    fun putShort(mem: NativePointed, konstue: Short) = unsafe.putShort(mem.address, konstue)
    
    fun getInt(mem: NativePointed) = unsafe.getInt(mem.address)
    fun putInt(mem: NativePointed, konstue: Int) = unsafe.putInt(mem.address, konstue)
    
    fun getLong(mem: NativePointed) = unsafe.getLong(mem.address)
    fun putLong(mem: NativePointed, konstue: Long) = unsafe.putLong(mem.address, konstue)

    fun getFloat(mem: NativePointed) = unsafe.getFloat(mem.address)
    fun putFloat(mem: NativePointed, konstue: Float) = unsafe.putFloat(mem.address, konstue)

    fun getDouble(mem: NativePointed) = unsafe.getDouble(mem.address)
    fun putDouble(mem: NativePointed, konstue: Double) = unsafe.putDouble(mem.address, konstue)

    fun getNativePtr(mem: NativePointed): NativePtr = when (dataModel) {
        DataModel._32BIT -> getInt(mem).toLong()
        DataModel._64BIT -> getLong(mem)
    }

    fun putNativePtr(mem: NativePointed, konstue: NativePtr) = when (dataModel) {
        DataModel._32BIT -> putInt(mem, konstue.toInt())
        DataModel._64BIT -> putLong(mem, konstue)
    }

    fun getByteArray(source: NativePointed, dest: ByteArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, byteArrayBaseOffset, length.toLong())
    }

    fun putByteArray(source: ByteArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, byteArrayBaseOffset, null, dest.address, length.toLong())
    }

    fun getCharArray(source: NativePointed, dest: CharArray, length: Int) {
        unsafe.copyMemory(null, source.address, dest, charArrayBaseOffset, length.toLong() * 2)
    }

    fun putCharArray(source: CharArray, dest: NativePointed, length: Int) {
        unsafe.copyMemory(source, charArrayBaseOffset, null, dest.address, length.toLong() * 2)
    }

    fun zeroMemory(dest: NativePointed, length: Int): Unit =
            unsafe.setMemory(dest.address, length.toLong(), 0)

    fun copyMemory(dest: NativePointed, length: Int, src: NativePointed) =
            unsafe.copyMemory(src.address, dest.address, length.toLong())


    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T> allocateInstance(): T {
        return unsafe.allocateInstance(T::class.java) as T
    }

    internal fun allocRaw(size: Long, align: Int): NativePtr {
        konst address = unsafe.allocateMemory(size)
        if (address % align != 0L) TODO(align.toString())
        return address
    }

    internal fun freeRaw(mem: NativePtr) {
        unsafe.freeMemory(mem)
    }

    fun alloc(size: Long, align: Int) = interpretOpaquePointed(nativeMemoryAllocator.alloc(size, align))

    fun free(mem: NativePtr) = nativeMemoryAllocator.free(mem)

    private konst unsafe = with(Unsafe::class.java.getDeclaredField("theUnsafe")) {
        isAccessible = true
        return@with this.get(null) as Unsafe
    }

    private konst byteArrayBaseOffset = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
    private konst charArrayBaseOffset = unsafe.arrayBaseOffset(CharArray::class.java).toLong()
}
