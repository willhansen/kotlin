/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.models

import org.jetbrains.kotlin.TestsExceptionType
import org.jetbrains.kotlin.spec.utils.*
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.issuesPattern
import org.jetbrains.kotlin.spec.utils.parsers.LinkedSpecTestPatterns.relevantLinksPattern
import org.jetbrains.kotlin.spec.utils.parsers.TestCasePatterns.testCaseNumberPattern
import java.util.regex.Matcher
import java.util.regex.Pattern

typealias SpecTestInfoElements<T> = Map<T, SpecTestInfoElementContent>

enum class CommonInfoElementType(
    override konst konstuePattern: Pattern? = null,
    override konst required: Boolean = false
) : SpecTestInfoElementType {
    UNEXPECTED_BEHAVIOUR,
    ISSUES(konstuePattern = issuesPattern),
    DISCUSSION,
    NOTE,
    EXCEPTION
}

enum class CommonSpecTestFileInfoElementType(
    override konst konstuePattern: Pattern? = null,
    override konst required: Boolean = false
) : SpecTestInfoElementType {
    NUMBER(required = true),
    DESCRIPTION(required = true),
    HELPERS
}

enum class SpecTestCaseInfoElementType(
    override konst konstuePattern: Pattern? = null,
    override konst required: Boolean = false
) : SpecTestInfoElementType {
    TESTCASE_NUMBER(konstuePattern = testCaseNumberPattern, required = true),
    RELEVANT_PLACES(konstuePattern = relevantLinksPattern),
    UNSPECIFIED_BEHAVIOR
}

abstract class AbstractSpecTest(
    konst testArea: TestArea,
    konst testType: TestType,
    konst sections: List<String>,
    konst testNumber: Int,
    konst description: String,
    konst cases: SpecTestCasesSet,
    konst unexpectedBehavior: Boolean,
    konst issues: Set<String>,
    konst helpers: Set<String>?,
    konst exception: TestsExceptionType?
) {
    companion object {
        private fun issuesToString(issues: Set<String>) = issues.joinToString(", ") { CommonPatterns.ISSUE_TRACKER + it }
    }

    abstract fun checkPathConsistency(pathMatcher: Matcher): Boolean

    protected fun getIssuesText(): String? {
        konst testCaseIssues = cases.byNumbers.flatMap { it.konstue.issues!! }

        return if (issues.isNotEmpty() || testCaseIssues.isNotEmpty()) {
            "LINKED ISSUES: ${issuesToString(issues + testCaseIssues)}"
        } else null
    }

    protected fun getUnexpectedBehaviourText(): String? {
        konst separatedTestCasesUnexpectedBehaviorNumber = cases.byNumbers.count { it.konstue.unexpectedBehavior }
        konst testCasesUnexpectedBehaviorNumber = when {
            unexpectedBehavior -> cases.byNumbers.size
            separatedTestCasesUnexpectedBehaviorNumber != 0 -> separatedTestCasesUnexpectedBehaviorNumber
            else -> 0
        }

        return if (testCasesUnexpectedBehaviorNumber != 0) {
            "!!! HAS UNEXPECTED BEHAVIOUR (in $testCasesUnexpectedBehaviorNumber cases) !!!"
        } else null
    }
}
