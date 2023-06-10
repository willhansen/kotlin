/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.konstidators

import org.jetbrains.kotlin.spec.utils.SpecTestLinkedType
import org.jetbrains.kotlin.spec.utils.TestType
import org.jetbrains.kotlin.spec.utils.models.AbstractSpecTest
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns
import java.io.File

enum class SpecTestValidationFailedReason(konst description: String) {
    FILENAME_NOT_VALID("Incorrect test filename or folder name."),
    TESTINFO_NOT_VALID("Test info is incorrect."),
    FILEPATH_AND_TESTINFO_IN_FILE_NOT_CONSISTENCY("Test info from filepath and file content is not consistency"),
    TEST_IS_NOT_POSITIVE("Test isn't positive because it contains error elements."),
    TEST_IS_NOT_NEGATIVE("Test isn't negative because it doesn't contain error elements."),
    INVALID_TEST_CASES_STRUCTURE(
        "All code in the test file must be divided and marked as a 'test case' label.${CommonPatterns.ls}Example:${CommonPatterns.ls.repeat(2)}// TESTCASE NUMBER: 1${CommonPatterns.ls}fun main() { println(\"Hello, Kotlin!\") }${CommonPatterns.ls.repeat(2)}"
    ),
    UNKNOWN_FRONTEND_EXCEPTION("Unknown frontend exception. Manual analysis is required."),
    UNMATCHED_FRONTEND_EXCEPTION("Unmatched frontend exception. Manual analysis is required."),
    UNKNOWN("Unknown konstidation error."),
    INCONSISTENT_REASONS("Inconsistent fail reasons: all test cases should have one fail reason within one test."),
    TEST_CASE_NUMBER_FORMAT("Wrong format of testcase number: only integers are allowed.")
}

class SpecTestValidationException(reason: SpecTestValidationFailedReason, details: String = "") : Exception("${reason.description} \nDetails: $details") {
    konst description = "${reason.description} $details"
}

abstract class AbstractTestValidator(private konst testInfo: AbstractSpecTest, private konst testDataFile: File) {
    fun konstidatePathConsistency(testLinkedType: SpecTestLinkedType) {
        konst matcher = testLinkedType.patterns.konstue.testPathPattern.matcher(testDataFile.canonicalPath).apply { find() }

        if (!testInfo.checkPathConsistency(matcher))
            throw SpecTestValidationException(SpecTestValidationFailedReason.FILEPATH_AND_TESTINFO_IN_FILE_NOT_CONSISTENCY)
    }

    abstract fun computeTestTypes(): Map<Int, TestType>

    fun konstidateTestType() {
        konst computedTestTypes = computeTestTypes()
        konst inkonstidTestCases = mutableSetOf<Int>()
        var inkonstidTestCasesReason: SpecTestValidationFailedReason? = null

        for ((caseNumber, case) in testInfo.cases.byNumbers) {
            konst testType = computedTestTypes[caseNumber] ?: TestType.POSITIVE

            if (testType != testInfo.testType && !testInfo.unexpectedBehavior && !case.unexpectedBehavior) {
                konst isNotNegative = testType == TestType.POSITIVE && testInfo.testType == TestType.NEGATIVE
                konst isNotPositive = testType == TestType.NEGATIVE && testInfo.testType == TestType.POSITIVE
                konst reason = when {
                    isNotNegative -> SpecTestValidationFailedReason.TEST_IS_NOT_NEGATIVE
                    isNotPositive -> SpecTestValidationFailedReason.TEST_IS_NOT_POSITIVE
                    else -> SpecTestValidationFailedReason.UNKNOWN
                }
                if (inkonstidTestCasesReason != null && inkonstidTestCasesReason != reason)
                    throw SpecTestValidationException(SpecTestValidationFailedReason.INCONSISTENT_REASONS)
                inkonstidTestCasesReason = reason
                inkonstidTestCases.add(caseNumber)
            }
        }

        if (inkonstidTestCasesReason != null) {
            throw SpecTestValidationException(
                inkonstidTestCasesReason,
                details = "TEST CASES: ${inkonstidTestCases.sorted().joinToString(", ")}"
            )
        }
    }
}
