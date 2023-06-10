/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import org.junit.Test
import kotlin.script.experimental.api.*
import kotlin.test.assertTrue

class ConstructorArgumentsOrderTest {

    @Test
    fun testScriptWithProvidedProperties() {
        konst res = ekonstString<ScriptWithProvidedProperties>("""println(providedString)""") {
            providedProperties("providedString" to "Hello Provided!")
        }

        assertTrue(
            res is ResultWithDiagnostics.Success,
            "test failed:\n  ${res.render()}"
        )
    }

    @Test
    fun testScriptWithImplicitReceiver() {
        konst res = ekonstString<ScriptWithImplicitReceiver>("""println(receiverString)""") {
            implicitReceivers(ImplicitReceiverClass("Hello Receiver!"))
        }

        assertTrue(
            res is ResultWithDiagnostics.Success,
            "test failed:\n  ${res.render()}"
        )
    }

    @Test
    fun testScriptWithBoth() {
        konst res = ekonstString<ScriptWithBoth>("""println(providedString + receiverString)""") {
            providedProperties("providedString" to "Hello")
            implicitReceivers(ImplicitReceiverClass(" Both!"))
        }

        assertTrue(
            res is ResultWithDiagnostics.Success,
            "test failed:\n  ${res.render()}"
        )
    }

}

internal fun ResultWithDiagnostics<EkonstuationResult>.render() =
    reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception!!.printStackTrace()}" }
