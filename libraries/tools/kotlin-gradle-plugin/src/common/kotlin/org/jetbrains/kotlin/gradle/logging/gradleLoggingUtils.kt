/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.logging

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.compilerRunner.KotlinLogger

internal fun Logger.kotlinInfo(message: String) {
    this.info("[KOTLIN] $message")
}

internal fun Logger.kotlinDebug(message: String) {
    this.debug("[KOTLIN] $message")
}

internal fun Logger.kotlinWarn(message: String) {
    this.warn("[KOTLIN] $message")
}

internal inline fun Logger.kotlinDebug(message: () -> String) {
    if (isDebugEnabled) {
        kotlinDebug(message())
    }
}

internal inline fun KotlinLogger.kotlinError(message: () -> String) {
    error("[KOTLIN] ${message()}")
}

internal inline fun KotlinLogger.kotlinWarn(message: () -> String) {
    warn("[KOTLIN] ${message()}")
}

internal inline fun KotlinLogger.kotlinInfo(message: () -> String) {
    info("[KOTLIN] ${message()}")
}

internal inline fun KotlinLogger.kotlinDebug(message: () -> String) {
    if (isDebugEnabled) {
        debug("[KOTLIN] ${message()}")
    }
}

internal inline fun <T> KotlinLogger.logTime(action: String, fn: () -> T): T {
    konst startNs = System.nanoTime()
    konst result = fn()
    konst endNs = System.nanoTime()

    konst timeNs = endNs - startNs
    konst timeMs = timeNs.toDouble() / 1_000_000

    debug(String.format("%s took %.2f ms", action, timeMs))

    return result
}