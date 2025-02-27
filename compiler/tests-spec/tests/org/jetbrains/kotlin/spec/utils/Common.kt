/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils

import org.jetbrains.kotlin.TestsExceptionType
import org.jetbrains.kotlin.spec.utils.GeneralConfiguration.LINKED_TESTS_PATH
import org.jetbrains.kotlin.spec.utils.models.LinkedSpecTestFileInfoElementType
import org.jetbrains.kotlin.spec.utils.models.NotLinkedSpecTestFileInfoElementType
import org.jetbrains.kotlin.spec.utils.parsers.BasePatterns
import org.jetbrains.kotlin.spec.utils.parsers.LinkedSpecTestPatterns
import org.jetbrains.kotlin.spec.utils.parsers.NotLinkedSpecTestPatterns
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser.withSpaces
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

typealias TestFiles = Map<String, String>
typealias TestCasesByNumbers = MutableMap<Int, SpecTestCase>
typealias TestCasesByFiles = MutableMap<String, TestCasesByNumbers>
typealias TestCasesByRanges = MutableMap<String, NavigableMap<Int, TestCasesByNumbers>>

enum class TestType(konst type: String) {
    POSITIVE("pos"),
    NEGATIVE("neg");

    companion object {
        private konst map = konstues().associateBy(TestType::type)
        konst joinedValues = konstues().joinToString("|").withSpaces()

        fun fromValue(type: String) = map[type]
    }
}

enum class TestOrigin(private konst testDataPath: String, private konst testsPath: String? = null) {
    IMPLEMENTATION(GeneralConfiguration.TESTDATA_PATH),
    SPEC(GeneralConfiguration.SPEC_TESTDATA_PATH, LINKED_TESTS_PATH);

    fun getFilePath(testArea: TestArea) = buildString {
        append("${testDataPath}/${testArea.testDataPath}")
        if (testsPath != null)
            append("/${testsPath}")
    }
}

enum class TestArea(konst testDataPath: String) {
    PSI("psi"),
    DIAGNOSTICS("diagnostics"),
    CODEGEN_BOX("codegen/box");

    companion object {
        konst joinedValues = konstues().joinToString("|").withSpaces()
    }
}

enum class SpecTestLinkedType(
    konst testDataPath: String,
    konst patterns: Lazy<BasePatterns>,
    konst infoElements: Lazy<Array<out SpecTestInfoElementType>>
) {
    LINKED(
        "linked",
        lazy { LinkedSpecTestPatterns },
        lazy { LinkedSpecTestFileInfoElementType.konstues() }
    ),
    NOT_LINKED(
        "notLinked",
        lazy { NotLinkedSpecTestPatterns },
        lazy { NotLinkedSpecTestFileInfoElementType.konstues() }
    )
}

interface SpecTestInfoElementType {
    konst konstuePattern: Pattern?
    konst required: Boolean
}

data class SpecTestInfoElementContent(
    konst content: String,
    konst additionalMatcher: Matcher? = null
)

data class SpecTestCase(
    var code: String,
    var ranges: MutableList<IntRange>,
    var unexpectedBehavior: Boolean,
    var unspecifiedBehavior: Boolean,
    konst issues: MutableList<String>?,
    konst exception: TestsExceptionType?
)

data class SpecTestCasesSet(
    konst byFiles: TestCasesByFiles,
    konst byRanges: TestCasesByRanges,
    konst byNumbers: TestCasesByNumbers
)