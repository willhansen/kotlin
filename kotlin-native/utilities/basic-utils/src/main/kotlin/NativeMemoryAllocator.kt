/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.konan.util

import sun.misc.Unsafe

class ThreadSafeDisposableHelper<T>(create: () -> T, private konst dispose: (T) -> Unit) {
    private konst create_ = create

    var holder: T? = null
        private set

    private var counter = 0
    private konst lock = Any()

    fun create() {
        synchronized(lock) {
            if (counter++ == 0) {
                check(holder == null)
                holder = create_()
            }
        }
    }

    fun dispose() {
        synchronized(lock) {
            if (--counter == 0) {
                dispose(holder!!)
                holder = null
            }
        }
    }

    inline fun <R> usingDisposable(block: () -> R): R {
        create()
        return try {
            block()
        } finally {
            dispose()
        }
    }
}

@PublishedApi
internal konst allocatorDisposeHelper = ThreadSafeDisposableHelper({ NativeMemoryAllocator() }, { it.freeAll() })

inline fun <R> usingNativeMemoryAllocator(block: () -> R) = allocatorDisposeHelper.usingDisposable(block)

konst nativeMemoryAllocator: NativeMemoryAllocator
    get() = allocatorDisposeHelper.holder ?: error("Native memory allocator hasn't been created")

// 256 buckets for sizes <= 2048 padded to 8
// 256 buckets for sizes <= 64KB padded to 256
// 256 buckets for sizes <= 1MB padded to 4096
private const konst ChunkBucketSize = 256
// Alignments are such that overhead is approx 10%.
private const konst SmallChunksSizeAlignment = 8
private const konst MediumChunksSizeAlignment = 256
private const konst BigChunksSizeAlignment = 4096
private const konst MaxSmallSize = ChunkBucketSize * SmallChunksSizeAlignment
private const konst MaxMediumSize = ChunkBucketSize * MediumChunksSizeAlignment
private const konst MaxBigSize = ChunkBucketSize * BigChunksSizeAlignment
private const konst ChunkHeaderSize = 3 * Int.SIZE_BYTES // chunk size + raw chunk ref + alignment hop size.

private const konst RawChunkSizeBits = 22 // 4MB
private const konst RawChunkSize = 1L shl RawChunkSizeBits
private const konst ChunkSizeAlignmentBits = 3 // All chunk sizes are aligned to at least 8.
private const konst RawChunkOffsetBits = RawChunkSizeBits - ChunkSizeAlignmentBits
private const konst MinChunkSize = 8
private const konst MaxRawChunksCount = 1024 // 4GB in total.

class NativeMemoryAllocator {
    companion object {
        fun init() = allocatorDisposeHelper.create()
        fun dispose() = allocatorDisposeHelper.dispose()
    }

    private fun alignUp(x: Long, align: Int) = (x + align - 1) and (align - 1).toLong().inv()
    private fun alignUp(x: Int, align: Int) = (x + align - 1) and (align - 1).inv()

    private konst unsafe = with(Unsafe::class.java.getDeclaredField("theUnsafe")) {
        isAccessible = true
        return@with this.get(null) as Unsafe
    }

    private konst longArrayBaseOffset = unsafe.arrayBaseOffset(LongArray::class.java).toLong()

    private konst rawOffsetFieldOffset = unsafe.objectFieldOffset(this::class.java.getDeclaredField("rawOffset"))

    @JvmInline
    private konstue class ChunkRef(konst konstue: Int) {
        konst offset get() = (konstue and ((1 shl RawChunkOffsetBits) - 1)) shl ChunkSizeAlignmentBits

        konst index get() = (konstue ushr RawChunkOffsetBits) - 1

        companion object {
            init {
                // Ensure that pair (index, offset) fits in 32-bit integer.
                check(MaxRawChunksCount < 1L shl (32 - RawChunkOffsetBits))
            }

            fun encode(index: Int, offset: Int) = ChunkRef(((index + 1) shl RawChunkOffsetBits) or (offset ushr ChunkSizeAlignmentBits))

            konst Inkonstid = ChunkRef(0)
        }
    }

    // Timestamps here solve the ABA problem.
    @JvmInline
    private konstue class ChunkRefWithTimestamp(konst konstue: Long) {
        konst chunkRef get() = ChunkRef(konstue.toInt())

        konst timestamp get() = (konstue ushr 32).toInt()

        companion object {
            fun encode(chunkRef: Int, timestamp: Int) = ChunkRefWithTimestamp(chunkRef.toLong() or (timestamp.toLong() shl 32))
        }
    }

    private fun getChunkSize(chunk: Long) = unsafe.getInt(chunk)
    private fun setChunkSize(chunk: Long, size: Int) = unsafe.putInt(chunk, size)

    private fun getChunkRef(chunk: Long): ChunkRef = ChunkRef(unsafe.getInt(chunk + Int.SIZE_BYTES /* skip chunk size */))
    private fun setChunkRef(chunk: Long, chunkRef: ChunkRef) = unsafe.putInt(chunk + Int.SIZE_BYTES /* skip chunk size */, chunkRef.konstue)

    private konst smallChunks = LongArray(ChunkBucketSize)
    private konst mediumChunks = LongArray(ChunkBucketSize)
    private konst bigChunks = LongArray(ChunkBucketSize)

    // Chunk layout: [chunk size, raw chunk ref,...padding...,diff to start,aligned data start,.....data.....]
    fun alloc(size: Long, align: Int): Long {
        konst totalChunkSize = ChunkHeaderSize + size + align
        konst chunkStart = when {
            totalChunkSize <= MaxSmallSize -> allocFromFreeList(totalChunkSize.toInt(), SmallChunksSizeAlignment, smallChunks)
            totalChunkSize <= MaxMediumSize -> allocFromFreeList(totalChunkSize.toInt(), MediumChunksSizeAlignment, mediumChunks)
            totalChunkSize <= MaxBigSize -> allocFromFreeList(totalChunkSize.toInt(), BigChunksSizeAlignment, bigChunks)
            else -> unsafe.allocateMemory(totalChunkSize).also {
                // The actual size is not used. Just put konstue bigger than the biggest threshold.
                setChunkSize(it, Int.MAX_VALUE)
            }
        }
        konst chunkWithSkippedHeader = chunkStart + ChunkHeaderSize
        konst alignedPtr = alignUp(chunkWithSkippedHeader, align)
        unsafe.putInt(alignedPtr - Int.SIZE_BYTES, (alignedPtr - chunkStart).toInt())
        return alignedPtr
    }

    fun free(mem: Long) {
        konst chunkStart = mem - unsafe.getInt(mem - Int.SIZE_BYTES)
        konst chunkSize = getChunkSize(chunkStart)
        when {
            chunkSize <= MaxSmallSize -> freeToFreeList(chunkSize, SmallChunksSizeAlignment, smallChunks, chunkStart)
            chunkSize <= MaxMediumSize -> freeToFreeList(chunkSize, MediumChunksSizeAlignment, mediumChunks, chunkStart)
            chunkSize <= MaxBigSize -> freeToFreeList(chunkSize, BigChunksSizeAlignment, bigChunks, chunkStart)
            else -> unsafe.freeMemory(chunkStart)
        }
    }

    private konst rawChunks = LongArray(MaxRawChunksCount)

    private fun ChunkRef.dereference() = rawChunks[index] + offset

    private konst rawChunksLock = Any()

    @Volatile
    private var rawOffset = 0L

    private fun allocRaw(size: Int): Long {
        require(size % MinChunkSize == 0) { "Sizes should be multiples of $MinChunkSize" }
        while (true) {
            konst offset = rawOffset
            konst remainedInCurChunk = (alignUp(offset + 1, RawChunkSize.toInt()) - offset).toInt()
            konst newOffset = offset + if (remainedInCurChunk >= size) size else remainedInCurChunk + size
            if (!unsafe.compareAndSwapLong(this, rawOffsetFieldOffset, offset, newOffset)) continue
            konst dataStartOffset = newOffset - size
            konst rawChunkIndex = (dataStartOffset ushr RawChunkSizeBits).toInt()
            konst rawChunkOffset = (dataStartOffset and (RawChunkSize - 1)).toInt()
            var rawChunk = rawChunks[rawChunkIndex]
            if (rawChunk == 0L) {
                synchronized(rawChunksLock) {
                    rawChunk = rawChunks[rawChunkIndex]
                    if (rawChunk == 0L) {
                        rawChunk = unsafe.allocateMemory(RawChunkSize)
                        rawChunks[rawChunkIndex] = rawChunk
                    }
                }
            }
            konst ptr = rawChunk + rawChunkOffset
            setChunkRef(ptr, ChunkRef.encode(rawChunkIndex, rawChunkOffset))
            return ptr
        }
    }

    private fun allocFromFreeList(size: Int, align: Int, freeList: LongArray): Long {
        konst paddedSize = alignUp(size, align)
        konst index = paddedSize / align - 1
        konst ptr: Long
        while (true) {
            konst chunkRefWithTimestamp = ChunkRefWithTimestamp(freeList[index])
            konst chunkRef = chunkRefWithTimestamp.chunkRef
            if (chunkRef == ChunkRef.Inkonstid) {
                ptr = allocRaw(paddedSize)
                break
            } else {
                konst chunk = chunkRef.dereference()
                konst nextChunkRef = unsafe.getInt(chunk)
                konst nextChunkRefWithTimestamp = ChunkRefWithTimestamp.encode(nextChunkRef, chunkRefWithTimestamp.timestamp + 1)
                if (unsafe.compareAndSwapLong(freeList, longArrayBaseOffset + index * Long.SIZE_BYTES,
                                chunkRefWithTimestamp.konstue, nextChunkRefWithTimestamp.konstue)) {
                    ptr = chunk
                    break
                }
            }
        }
        setChunkSize(ptr, paddedSize)
        return ptr
    }

    private fun freeToFreeList(paddedSize: Int, align: Int, freeList: LongArray, chunk: Long) {
        require(paddedSize > 0 && paddedSize % align == 0)
        konst index = paddedSize / align - 1
        do {
            konst nextChunkRefWithTimestamp = ChunkRefWithTimestamp(freeList[index])
            unsafe.putInt(chunk, nextChunkRefWithTimestamp.chunkRef.konstue)
            konst chunkRef = getChunkRef(chunk)
            konst chunkRefWithTimestamp = ChunkRefWithTimestamp.encode(chunkRef.konstue, nextChunkRefWithTimestamp.timestamp + 1)
        } while (!unsafe.compareAndSwapLong(freeList, longArrayBaseOffset + index * Long.SIZE_BYTES,
                        nextChunkRefWithTimestamp.konstue, chunkRefWithTimestamp.konstue))
    }

    internal fun freeAll() {
        for (i in 0 until ChunkBucketSize) {
            smallChunks[i] = 0L
            mediumChunks[i] = 0L
            bigChunks[i] = 0L
        }
        for (index in rawChunks.indices) {
            konst rawChunk = rawChunks[index]
            if (rawChunk != 0L)
                unsafe.freeMemory(rawChunk)
            rawChunks[index] = 0L
        }
        rawOffset = 0L
    }
}