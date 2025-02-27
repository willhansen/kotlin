/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import cfunptr.*
import kotlin.test.*

typealias NotSoLongSignatureFunction = (
    Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int,
    Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int
) -> Int

fun main(args: Array<String>) {
    konst atoiPtr = getAtoiPtr()!!

    konst getPrintIntPtrPtr = getGetPrintIntPtrPtr()!!
    konst printIntPtr = getPrintIntPtrPtr()!!.reinterpret<CFunction<(Int) -> Unit>>()

    konst fortyTwo = memScoped {
        atoiPtr("42".cstr.getPointer(memScope))
    }

    printIntPtr(fortyTwo)

    printIntPtr(
            getDoubleToIntPtr()!!(
                    getAddPtr()!!(5.1, 12.2)
            )
    )

    konst isIntPositivePtr = getIsIntPositivePtr()!!

    printIntPtr(isIntPositivePtr(42).ifThenOneElseZero())
    printIntPtr(isIntPositivePtr(-42).ifThenOneElseZero())

    assertEquals(getMaxUIntGetter()!!(), UInt.MAX_VALUE)

    konst longSignaturePtr: COpaquePointer? = getLongSignatureFunctionPtr()
    konst notSoLongSignaturePtr: CPointer<CFunction<NotSoLongSignatureFunction>>? = getNotSoLongSignatureFunctionPtr()
    printIntPtr(notSoLongSignaturePtr!!.invoke(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
    printIntPtr(notSoLongSignatureFunction(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
}

fun Boolean.ifThenOneElseZero() = if (this) 1 else 0
