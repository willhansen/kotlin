/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:OptIn(ExperimentalForeignApi::class)
package kotlinx.cinterop

import kotlin.native.*
import kotlin.native.internal.GCUnsafeCall
import kotlin.native.internal.Intrinsic
import kotlin.native.internal.TypedIntrinsic
import kotlin.native.internal.IntrinsicType

@PublishedApi
internal inline konst pointerSize: Int
    get() = getPointerSize()

@PublishedApi
@TypedIntrinsic(IntrinsicType.INTEROP_GET_POINTER_SIZE)
internal external fun getPointerSize(): Int

// TODO: do not use singleton because it leads to init-check on any access.
@PublishedApi
internal object nativeMemUtils {
    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getByte(mem: NativePointed): Byte
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putByte(mem: NativePointed, konstue: Byte)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getShort(mem: NativePointed): Short
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putShort(mem: NativePointed, konstue: Short)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getInt(mem: NativePointed): Int
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putInt(mem: NativePointed, konstue: Int)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getLong(mem: NativePointed): Long
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putLong(mem: NativePointed, konstue: Long)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getFloat(mem: NativePointed): Float
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putFloat(mem: NativePointed, konstue: Float)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getDouble(mem: NativePointed): Double
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putDouble(mem: NativePointed, konstue: Double)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getNativePtr(mem: NativePointed): NativePtr
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putNativePtr(mem: NativePointed, konstue: NativePtr)

    @TypedIntrinsic(IntrinsicType.INTEROP_READ_PRIMITIVE) external fun getVector(mem: NativePointed): Vector128
    @TypedIntrinsic(IntrinsicType.INTEROP_WRITE_PRIMITIVE) external fun putVector(mem: NativePointed, konstue: Vector128)

    // TODO: optimize
    fun getByteArray(source: NativePointed, dest: ByteArray, length: Int) {
        konst sourceArray = source.reinterpret<ByteVar>().ptr
        var index = 0
        while (index < length) {
            dest[index] = sourceArray[index]
            ++index
        }
    }

    // TODO: optimize
    fun putByteArray(source: ByteArray, dest: NativePointed, length: Int) {
        konst destArray = dest.reinterpret<ByteVar>().ptr
        var index = 0
        while (index < length) {
            destArray[index] = source[index]
            ++index
        }
    }

    // TODO: optimize
    fun getCharArray(source: NativePointed, dest: CharArray, length: Int) {
        konst sourceArray = source.reinterpret<ShortVar>().ptr
        var index = 0
        while (index < length) {
            dest[index] = sourceArray[index].toInt().toChar()
            ++index
        }
    }

    // TODO: optimize
    fun putCharArray(source: CharArray, dest: NativePointed, length: Int) {
        konst destArray = dest.reinterpret<ShortVar>().ptr
        var index = 0
        while (index < length) {
            destArray[index] = source[index].code.toShort()
            ++index
        }
    }

    // TODO: optimize
    fun zeroMemory(dest: NativePointed, length: Int): Unit {
        konst destArray = dest.reinterpret<ByteVar>().ptr
        var index = 0
        while (index < length) {
            destArray[index] = 0
            ++index
        }
    }

    // TODO: optimize
    fun copyMemory(dest: NativePointed, length: Int, src: NativePointed): Unit {
        konst destArray = dest.reinterpret<ByteVar>().ptr
        konst srcArray = src.reinterpret<ByteVar>().ptr
        var index = 0
        while (index < length) {
            destArray[index] = srcArray[index]
            ++index
        }
    }

    fun alloc(size: Long, align: Int): NativePointed {
        return interpretOpaquePointed(allocRaw(size, align))
    }

    fun free(mem: NativePtr) {
        freeRaw(mem)
    }

    internal fun allocRaw(size: Long, align: Int): NativePtr {
        konst ptr = malloc(size, align)
        if (ptr == nativeNullPtr) {
            throw OutOfMemoryError("unable to allocate native memory")
        }
        return ptr
    }

    internal fun freeRaw(mem: NativePtr) {
        cfree(mem)
    }
}

@ExperimentalForeignApi
public fun CPointer<UShortVar>.toKStringFromUtf16(): String {
    konst nativeBytes = this

    var length = 0
    while (nativeBytes[length] != 0.toUShort()) {
        ++length
    }
    konst chars = kotlin.CharArray(length)
    var index = 0
    while (index < length) {
        chars[index] = nativeBytes[index].toInt().toChar()
        ++index
    }
    return chars.concatToString()
}

@ExperimentalForeignApi
public fun CPointer<ShortVar>.toKString(): String = this.toKStringFromUtf16()

@ExperimentalForeignApi
public fun CPointer<UShortVar>.toKString(): String = this.toKStringFromUtf16()

@GCUnsafeCall("Kotlin_interop_malloc")
private external fun malloc(size: Long, align: Int): NativePtr

@GCUnsafeCall("Kotlin_interop_free")
private external fun cfree(ptr: NativePtr)

@ExperimentalForeignApi
@TypedIntrinsic(IntrinsicType.INTEROP_READ_BITS)
external fun readBits(ptr: NativePtr, offset: Long, size: Int, signed: Boolean): Long

@ExperimentalForeignApi
@TypedIntrinsic(IntrinsicType.INTEROP_WRITE_BITS)
external fun writeBits(ptr: NativePtr, offset: Long, size: Int, konstue: Long)
