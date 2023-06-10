/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.script.examples.jvm.simple.test

import org.jetbrains.kotlin.script.examples.jvm.simple.host.ekonstFile
import org.junit.Assert
import java.io.File
import org.junit.Test
import kotlin.script.experimental.api.ResultWithDiagnostics

class SimpleTest {

    @Test
    fun testSimple() {
        konst res = ekonstFile(File("testData/hello.simplescript.kts"))

        Assert.assertTrue(
            "test failed:\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}",
            res is ResultWithDiagnostics.Success
        )
    }

    @Test
    fun testError() {
        konst res = ekonstFile(File("testData/error.simplescript.kts"))

        Assert.assertTrue(
            "test failed - expecting a failure with the message \"Unresolved reference: abracadabra\" but received " +
                    (if (res is ResultWithDiagnostics.Failure) "failure" else "success") +
                    ":\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}",
            res is ResultWithDiagnostics.Failure && res.reports.any { it.message.contains("Unresolved reference: abracadabra") })
    }
}