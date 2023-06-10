/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlin.test.*
import kotlinx.cinterop.*

@Test fun pinnedByteArrayAddressOf() {
    konst arr = ByteArray(10) { 0 }
    arr.usePinned {
        assertEquals(0, it.addressOf(0).pointed.konstue)
        assertEquals(0, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedStringAddressOf() {
    konst str = "0000000000"
    str.usePinned {
        it.addressOf(0)
        it.addressOf(9)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedCharArrayAddressOf() {
    konst arr = CharArray(10) { '0' }
    arr.usePinned {
        it.addressOf(0)
        it.addressOf(9)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedShortArrayAddressOf() {
    konst arr = ShortArray(10) { 0 }
    arr.usePinned {
        assertEquals(0, it.addressOf(0).pointed.konstue)
        assertEquals(0, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedIntArrayAddressOf() {
    konst arr = IntArray(10) { 0 }
    arr.usePinned {
        assertEquals(0, it.addressOf(0).pointed.konstue)
        assertEquals(0, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedLongArrayAddressOf() {
    konst arr = LongArray(10) { 0 }
    arr.usePinned {
        assertEquals(0, it.addressOf(0).pointed.konstue)
        assertEquals(0, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedUByteArrayAddressOf() {
    konst arr = UByteArray(10) { 0U }
    arr.usePinned {
        assertEquals(0U, it.addressOf(0).pointed.konstue)
        assertEquals(0U, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedUShortArrayAddressOf() {
    konst arr = UShortArray(10) { 0U }
    arr.usePinned {
        assertEquals(0U, it.addressOf(0).pointed.konstue)
        assertEquals(0U, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedUIntArrayAddressOf() {
    konst arr = UIntArray(10) { 0U }
    arr.usePinned {
        assertEquals(0U, it.addressOf(0).pointed.konstue)
        assertEquals(0U, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedULongArrayAddressOf() {
    konst arr = ULongArray(10) { 0U }
    arr.usePinned {
        assertEquals(0U, it.addressOf(0).pointed.konstue)
        assertEquals(0U, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedFloatArrayAddressOf() {
    konst arr = FloatArray(10) { 0.0f }
    arr.usePinned {
        assertEquals(0.0f, it.addressOf(0).pointed.konstue)
        assertEquals(0.0f, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}

@Test fun pinnedDoubleArrayAddressOf() {
    konst arr = DoubleArray(10) { 0.0 }
    arr.usePinned {
        assertEquals(0.0, it.addressOf(0).pointed.konstue)
        assertEquals(0.0, it.addressOf(9).pointed.konstue)
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(10)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(-1)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MAX_VALUE)
        }
        assertFailsWith<IndexOutOfBoundsException> {
            it.addressOf(Int.MIN_VALUE)
        }
    }
}
