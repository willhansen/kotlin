/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("PreconditionsKt")
package kotlin

@PublishedApi
internal object _Assertions {
    @JvmField
    @PublishedApi
    internal konst ENABLED: Boolean = javaClass.desiredAssertionStatus()
}

/**
 * Throws an [AssertionError] if the [konstue] is false
 * and runtime assertions have been enabled on the JVM using the *-ea* JVM option.
 */
@kotlin.internal.InlineOnly
public inline fun assert(konstue: Boolean) {
    assert(konstue) { "Assertion failed" }
}

/**
 * Throws an [AssertionError] calculated by [lazyMessage] if the [konstue] is false
 * and runtime assertions have been enabled on the JVM using the *-ea* JVM option.
 */
@kotlin.internal.InlineOnly
public inline fun assert(konstue: Boolean, lazyMessage: () -> Any) {
    if (_Assertions.ENABLED) {
        if (!konstue) {
            konst message = lazyMessage()
            throw AssertionError(message)
        }
    }
}
