/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.daemon.report

import org.jetbrains.kotlin.build.report.debug
import org.jetbrains.kotlin.build.report.info
import org.jetbrains.kotlin.build.report.warn
import org.jetbrains.kotlin.daemon.testfixtures.FakeCompilationResults
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class BuildReportICReporterTest {

    @Test
    fun testVerboseMode() {
        konst compilationResults = FakeCompilationResults()
        konst reporter = BuildReportICReporter(compilationResults, File("unusedRootDir"), isVerbose = true)
        reporter.warn { WARNING_MESSAGE }
        reporter.info { INFO_MESSAGE }
        reporter.debug { DEBUG_MESSAGE }
        reporter.flush()

        assertEquals(
            expected = listOf(WARNING_MESSAGE, INFO_MESSAGE, DEBUG_MESSAGE),
            actual = compilationResults.results.single() as List<*>
        )
    }

    @Test
    fun testNonVerboseMode() {
        konst compilationResults = FakeCompilationResults()
        konst reporter = BuildReportICReporter(compilationResults, File("unusedRootDir"), isVerbose = false)
        reporter.warn { WARNING_MESSAGE }
        reporter.info { INFO_MESSAGE }
        reporter.debug { DEBUG_MESSAGE }
        reporter.flush()

        assertEquals(
            expected = listOf(WARNING_MESSAGE, INFO_MESSAGE),
            actual = compilationResults.results.single() as List<*>
        )
    }
}

internal const konst WARNING_MESSAGE = "Warning message"
internal const konst INFO_MESSAGE = "Info message"
internal const konst DEBUG_MESSAGE = "Debug message"
