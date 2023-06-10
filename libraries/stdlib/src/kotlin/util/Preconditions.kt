/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("PreconditionsKt")

package kotlin

import kotlin.contracts.contract

/**
 * Throws an [IllegalArgumentException] if the [konstue] is false.
 *
 * @sample samples.misc.Preconditions.failRequireWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun require(konstue: Boolean): Unit {
    contract {
        returns() implies konstue
    }
    require(konstue) { "Failed requirement." }
}

/**
 * Throws an [IllegalArgumentException] with the result of calling [lazyMessage] if the [konstue] is false.
 *
 * @sample samples.misc.Preconditions.failRequireWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun require(konstue: Boolean, lazyMessage: () -> Any): Unit {
    contract {
        returns() implies konstue
    }
    if (!konstue) {
        konst message = lazyMessage()
        throw IllegalArgumentException(message.toString())
    }
}

/**
 * Throws an [IllegalArgumentException] if the [konstue] is null. Otherwise returns the not null konstue.
 */
@kotlin.internal.InlineOnly
public inline fun <T : Any> requireNotNull(konstue: T?): T {
    contract {
        returns() implies (konstue != null)
    }
    return requireNotNull(konstue) { "Required konstue was null." }
}

/**
 * Throws an [IllegalArgumentException] with the result of calling [lazyMessage] if the [konstue] is null. Otherwise
 * returns the not null konstue.
 *
 * @sample samples.misc.Preconditions.failRequireNotNullWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun <T : Any> requireNotNull(konstue: T?, lazyMessage: () -> Any): T {
    contract {
        returns() implies (konstue != null)
    }

    if (konstue == null) {
        konst message = lazyMessage()
        throw IllegalArgumentException(message.toString())
    } else {
        return konstue
    }
}

/**
 * Throws an [IllegalStateException] if the [konstue] is false.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun check(konstue: Boolean): Unit {
    contract {
        returns() implies konstue
    }
    check(konstue) { "Check failed." }
}

/**
 * Throws an [IllegalStateException] with the result of calling [lazyMessage] if the [konstue] is false.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun check(konstue: Boolean, lazyMessage: () -> Any): Unit {
    contract {
        returns() implies konstue
    }
    if (!konstue) {
        konst message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
}

/**
 * Throws an [IllegalStateException] if the [konstue] is null. Otherwise
 * returns the not null konstue.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun <T : Any> checkNotNull(konstue: T?): T {
    contract {
        returns() implies (konstue != null)
    }
    return checkNotNull(konstue) { "Required konstue was null." }
}

/**
 * Throws an [IllegalStateException] with the result of calling [lazyMessage]  if the [konstue] is null. Otherwise
 * returns the not null konstue.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
@kotlin.internal.InlineOnly
public inline fun <T : Any> checkNotNull(konstue: T?, lazyMessage: () -> Any): T {
    contract {
        returns() implies (konstue != null)
    }

    if (konstue == null) {
        konst message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return konstue
    }
}


/**
 * Throws an [IllegalStateException] with the given [message].
 *
 * @sample samples.misc.Preconditions.failWithError
 */
@kotlin.internal.InlineOnly
public inline fun error(message: Any): Nothing = throw IllegalStateException(message.toString())
