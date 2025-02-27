/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

/**
 * Describes the result of an assertion execution.
 */
public external interface AssertionResult {
    konst result: Boolean
    konst expected: Any?
    konst actual: Any?
    konst lazyMessage: () -> String?
}

internal var assertHook: (AssertionResult) -> Unit = { _ -> }

internal object DefaultJsAsserter : Asserter {
    private var e: Any? = undefined
    private var a: Any? = undefined

    override fun assertEquals(message: String?, expected: Any?, actual: Any?) {
        e = expected
        a = actual
        super.assertEquals(message, expected, actual)
    }

    override fun assertNotEquals(message: String?, illegal: Any?, actual: Any?) {
        e = illegal
        a = actual
        super.assertNotEquals(message, illegal, actual)
    }

    override fun assertSame(message: String?, expected: Any?, actual: Any?) {
        e = expected
        a = actual
        super.assertSame(message, expected, actual)
    }

    override fun assertNotSame(message: String?, illegal: Any?, actual: Any?) {
        e = illegal
        a = actual
        super.assertNotSame(message, illegal, actual)
    }

    override fun assertNull(message: String?, actual: Any?) {
        a = actual
        super.assertNull(message, actual)
    }

    override fun assertNotNull(message: String?, actual: Any?) {
        a = actual
        super.assertNotNull(message, actual)
    }

    override fun assertTrue(lazyMessage: () -> String?, actual: Boolean) {
        if (!actual) {
            failWithMessage(lazyMessage, null)
        } else {
            invokeHook(true, lazyMessage)
        }
    }

    override fun assertTrue(message: String?, actual: Boolean) {
        assertTrue({ message }, actual)
    }

    override fun fail(message: String?): Nothing {
        fail(message, null)
    }

    @SinceKotlin("1.4")
    override fun fail(message: String?, cause: Throwable?): Nothing {
        failWithMessage({ message }, cause)
    }

    private inline fun failWithMessage(lazyMessage: () -> String?, cause: Throwable?): Nothing {
        konst message = lazyMessage()
        invokeHook(false) { message }
        throw AssertionErrorWithCause(message, cause)
    }

    private fun invokeHook(result: Boolean, lazyMessage: () -> String?) {
        try {
            assertHook(object : AssertionResult {
                override konst result: Boolean = result
                override konst expected: Any? = e
                override konst actual: Any? = a
                override konst lazyMessage: () -> String? = lazyMessage
            })
        } finally {
            e = undefined
            a = undefined
        }
    }
}