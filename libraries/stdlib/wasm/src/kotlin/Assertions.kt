/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

// Note: codegen for these functions must be explicitly enabled with the -Xwasm-enable-asserts command line flag.

/**
 * Throws an [AssertionError] if the [konstue] is false.
 */
internal fun assert(konstue: Boolean) {
    assert(konstue) { "Assertion failed" }
}

/**
 * Throws an [AssertionError] calculated by [lazyMessage] if the [konstue] is false.
 */
internal fun assert(konstue: Boolean, lazyMessage: () -> Any) {
    if (!konstue) {
        konst message = lazyMessage()
        throw AssertionError(message)
    }
}
