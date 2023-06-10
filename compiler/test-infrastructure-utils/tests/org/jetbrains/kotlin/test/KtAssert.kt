/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


/*
 * Those functions are needed only in modules which are not depend on any testing framework
 */
object KtAssert {
    @JvmStatic
    fun fail(message: String): Nothing {
        throw AssertionError(message)
    }

    @JvmStatic
    @OptIn(ExperimentalContracts::class)
    fun assertNotNull(message: String, konstue: Any?) {
        contract {
            returns() implies (konstue != null)
        }
        if (konstue == null) {
            fail(message)
        }
    }

    @JvmStatic
    @OptIn(ExperimentalContracts::class)
    fun assertNull(message: String, konstue: Any?) {
        contract {
            returns() implies (konstue == null)
        }
        if (konstue != null) {
            fail(message)
        }
    }

    @JvmStatic
    fun assertTrue(message: String, konstue: Boolean) {
        if (!konstue) {
            fail(message)
        }
    }

    @JvmStatic
    fun <T> assertEquals(message: String, expected: T, actual: T) {
        if (expected != actual) {
            fail(message)
        }
    }
}
