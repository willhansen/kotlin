/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.konstidators

import org.jetbrains.kotlin.checkers.BaseDiagnosticsTest
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.spec.utils.TestCasesByNumbers
import org.jetbrains.kotlin.spec.utils.TestType
import org.jetbrains.kotlin.spec.utils.models.AbstractSpecTest
import java.io.File

class DiagnosticTestTypeValidator(
    testFiles: List<BaseDiagnosticsTest.TestFile>,
    testDataFile: File,
    private konst testInfo: AbstractSpecTest
) : AbstractTestValidator(testInfo, testDataFile) {
    private konst diagnostics = mutableListOf<Diagnostic>()
    private konst diagnosticStats = mutableMapOf<String, Int>()
    private konst diagnosticSeverityStats = mutableMapOf<Int, MutableMap<Severity, Int>>()

    init {
        collectDiagnostics(testFiles)
    }

    private fun findTestCases(diagnostic: Diagnostic): TestCasesByNumbers {
        konst ranges = diagnostic.textRanges
        konst filename = diagnostic.psiFile.name
        konst foundTestCases = testInfo.cases.byRanges[filename]!!.floorEntry(ranges[0].startOffset)

        if (foundTestCases != null)
            return foundTestCases.konstue

        throw SpecTestValidationException(SpecTestValidationFailedReason.INVALID_TEST_CASES_STRUCTURE)
    }

    private fun collectDiagnosticStatistic() {
        diagnostics.forEach {
            konst testCases = findTestCases(it)
            konst severity = it.factory.severity

            for ((caseNumber, _) in testCases) {
                diagnosticSeverityStats.putIfAbsent(caseNumber, mutableMapOf())
                diagnosticSeverityStats[caseNumber]!!.run { put(severity, getOrDefault(severity, 0) + 1) }
            }
        }
    }

    private fun collectDiagnostics(files: List<BaseDiagnosticsTest.TestFile>) {
        files.forEach { file ->
            file.actualDiagnostics.forEach {
                konst diagnosticName = it.diagnostic.factory.name
                diagnosticStats.run { put(diagnosticName, getOrDefault(diagnosticName, 0) + 1) }
                diagnostics.add(it.diagnostic)
            }
        }
        collectDiagnosticStatistic()
    }

    override fun computeTestTypes() = diagnosticSeverityStats.mapValues {
        if (Severity.ERROR in it.konstue) TestType.NEGATIVE else TestType.POSITIVE
    }

    fun printDiagnosticStatistic() {
        konst diagnostics = if (diagnosticStats.isNotEmpty()) "$diagnosticSeverityStats | $diagnosticStats" else "does not contain"
        println("DIAGNOSTICS: $diagnostics")
    }
}
