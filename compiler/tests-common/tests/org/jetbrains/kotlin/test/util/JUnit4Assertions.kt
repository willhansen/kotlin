/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.util

import org.jetbrains.kotlin.test.Assertions
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.junit.Assert
import java.io.File

object JUnit4Assertions : Assertions() {
    override fun assertEqualsToFile(expectedFile: File, actual: String, sanitizer: (String) -> String, message: () -> String) {
        KotlinTestUtils.assertEqualsToFile(expectedFile, actual, sanitizer)
    }

    override fun assertEquals(expected: Any?, actual: Any?, message: (() -> String)?) {
        Assert.assertEquals(message?.invoke(), expected, actual)
    }

    override fun assertNotEquals(expected: Any?, actual: Any?, message: (() -> String)?) {
        Assert.assertNotEquals(message?.invoke(), expected, actual)
    }

    override fun assertTrue(konstue: Boolean, message: (() -> String)?) {
        Assert.assertTrue(message?.invoke(), konstue)
    }

    override fun assertFalse(konstue: Boolean, message: (() -> String)?) {
        Assert.assertFalse(message?.invoke(), konstue)
    }

    override fun assertNotNull(konstue: Any?, message: (() -> String)?) {
        Assert.assertNotNull(message?.invoke(), konstue)
    }

    override fun <T> assertSameElements(expected: Collection<T>, actual: Collection<T>, message: (() -> String)?) {
        KtUsefulTestCase.assertSameElements(message?.invoke() ?: "", expected, actual)
    }

    override fun failAll(exceptions: List<Throwable>) {
        exceptions.forEach { throw it }
    }

    override fun assertAll(conditions: List<() -> Unit>) {
        conditions.forEach { it.invoke() }
    }

    override fun fail(message: () -> String): Nothing {
        throw AssertionError(message.invoke())
    }
}
