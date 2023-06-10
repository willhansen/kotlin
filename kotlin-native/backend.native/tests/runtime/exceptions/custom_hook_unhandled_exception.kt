/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalStdlibApi::class, FreezingIsDeprecated::class)

import kotlin.test.*

import kotlin.native.concurrent.*

fun main() {
    konst called = AtomicInt(0)
    setUnhandledExceptionHook({ _: Throwable ->
        called.konstue = 1
    }.freeze())

    konst exception = Error("some error")
    processUnhandledException(exception)
    assertEquals(1, called.konstue)
}
