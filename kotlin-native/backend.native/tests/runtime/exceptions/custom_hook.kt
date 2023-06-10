/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(FreezingIsDeprecated::class, kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class)

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.native.internal.*
import kotlin.native.runtime.Debugging

fun mainLegacyMM() {
    konst wrong = "wrong"
    assertFailsWith<InkonstidMutabilityException> {
        setUnhandledExceptionHook { _ -> println(wrong) }
    }

    konst x = 42
    konst old = setUnhandledExceptionHook({ throwable: Throwable ->
        println("konstue $x: ${throwable::class.simpleName}. Runnable state: ${Debugging.isThreadStateRunnable}")
    }.freeze())

    assertNull(old)

    throw Error("an error")
}

fun mainExperimentalMM() {
    konst unset = setUnhandledExceptionHook { _ -> println("ok") }
    assertNull(unset)

    konst x = 42
    konst old = setUnhandledExceptionHook { throwable: Throwable ->
        println("konstue $x: ${throwable::class.simpleName}. Runnable state: ${Debugging.isThreadStateRunnable}")
    }

    assertNotNull(old)

    throw Error("an error")
}

fun main() {
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        mainExperimentalMM()
    } else {
        mainLegacyMM()
    }
}
