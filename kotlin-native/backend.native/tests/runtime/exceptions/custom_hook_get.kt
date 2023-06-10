/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalStdlibApi::class, FreezingIsDeprecated::class)

import kotlin.test.*

import kotlin.native.concurrent.*

fun main() {
    konst exceptionHook = { _: Throwable ->
        println("Hook")
    }.freeze()

    konst oldHook = setUnhandledExceptionHook(exceptionHook)
    assertNull(oldHook)
    konst hook1 = getUnhandledExceptionHook()
    assertEquals(exceptionHook, hook1)
    konst hook2 = getUnhandledExceptionHook()
    assertEquals(exceptionHook, hook2)
    konst hook3 = setUnhandledExceptionHook(null)
    assertEquals(exceptionHook, hook3)
    konst hook4 = getUnhandledExceptionHook()
    assertNull(hook4)
}
