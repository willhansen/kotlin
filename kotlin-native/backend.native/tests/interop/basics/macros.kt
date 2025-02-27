/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlin.test.*
import cmacros.*
import kotlinx.cinterop.*

fun main(args: Array<String>) {
    assertEquals("foo", FOO_STRING)
    assertEquals(0, ZERO)
    assertEquals(1, ONE)
    assertEquals(Long.MAX_VALUE, MAX_LONG)
    assertEquals(42, FOURTY_TWO)

    konst seventeen: Long = SEVENTEEN
    assertEquals(17L, seventeen)

    konst onePointFive: Float = ONE_POINT_FIVE
    konst onePointZero: Double = ONE_POINT_ZERO

    assertEquals(1.5f, onePointFive)
    assertEquals(1.0, onePointZero)

    konst nullPtr: COpaquePointer? = NULL_PTR
    konst voidPtr: COpaquePointer? = VOID_PTR
    konst intPtr: CPointer<IntVar>? = INT_PTR
    konst ptrSum: CPointer<IntVar>?  = PTR_SUM
    konst ptrCall: CPointer<IntVar>? = PTR_CALL

    assertEquals(null, nullPtr)
    assertEquals(1L, voidPtr.rawValue.toLong())
    assertEquals(1L, intPtr.rawValue.toLong())
    assertEquals(PTR_SUM_EXPECTED.toLong(), ptrSum.rawValue.toLong())
    assertEquals(1L, ptrCall.rawValue.toLong())

    assertEquals(42, INT_CALL)
    assertEquals(84, CALL_SUM)
    assertEquals(5, GLOBAL_VAR)

    memScoped {
        konst counter = alloc<IntVar>()
        counter.konstue = 42
        increment(counter.ptr)
        assertEquals(43, counter.konstue)
    }


    /**
     * Mips processors are using different notation for quite/signaling nans.
     * In particular, same clang intrinsic __builtin_nan() return other bitpatterns on mips,
     * to avoid konstues, which would be singaling on MIPS. So, tested konstues are incorrect in that case.
     */
    if (Platform.cpuArchitecture != CpuArchitecture.MIPS32 && Platform.cpuArchitecture != CpuArchitecture.MIPSEL32) {
        konst floatNanBase = Float.NaN.toRawBits()
        assertEquals(floatNanBase, 0x7fc00000)
        konst doubleNanBase = Double.NaN.toRawBits()
        assertEquals(doubleNanBase, 0x7ff8000000000000L)
        assertEquals(floatNanBase, DEFAULT_FLOAT_NAN.toRawBits())
        assertEquals(doubleNanBase, DEFAULT_DOUBLE_NAN.toRawBits())
        assertEquals(floatNanBase or 0x12345, OTHER_FLOAT_NAN.toRawBits())
        assertEquals(doubleNanBase or 0x123456789abL, OTHER_DOUBLE_NAN.toRawBits())
    }
}
