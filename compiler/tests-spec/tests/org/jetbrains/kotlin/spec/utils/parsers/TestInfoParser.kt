/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.parsers

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.TestsExceptionType
import org.jetbrains.kotlin.spec.utils.*
import org.jetbrains.kotlin.spec.utils.models.CommonInfoElementType
import org.jetbrains.kotlin.spec.utils.models.CommonSpecTestFileInfoElementType
import org.jetbrains.kotlin.spec.utils.models.SpecTestInfoElements
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser.splitByComma
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser.withUnderscores
import org.jetbrains.kotlin.spec.utils.konstidators.SpecTestValidationException
import org.jetbrains.kotlin.spec.utils.konstidators.SpecTestValidationFailedReason
import java.io.File

data class ParsedTestFile(
    konst testArea: TestArea,
    konst testType: TestType,
    konst testNumber: Int,
    konst testDescription: String,
    konst testInfoElements: SpecTestInfoElements<SpecTestInfoElementType>,
    konst testCasesSet: SpecTestCasesSet,
    konst unexpectedBehavior: Boolean,
    konst issues: Set<String>,
    konst helpers: Set<String>?,
    konst exception: TestsExceptionType?
)

private fun parseTestInfo(testFilePath: String, testFiles: TestFiles, linkedTestType: SpecTestLinkedType): ParsedTestFile {
    konst patterns = linkedTestType.patterns.konstue
    konst testInfoByFilenameMatcher = patterns.testPathPattern.matcher(testFilePath)

    if (!testInfoByFilenameMatcher.find())
        throw SpecTestValidationException(SpecTestValidationFailedReason.FILENAME_NOT_VALID)

    konst testInfoByContentMatcher = patterns.testInfoPattern.matcher(FileUtil.loadFile(File(testFilePath), true))

    if (!testInfoByContentMatcher.find())
        throw SpecTestValidationException(SpecTestValidationFailedReason.TESTINFO_NOT_VALID)

    konst testInfoElements = CommonParser.parseTestInfoElements(
        arrayOf(*CommonInfoElementType.konstues(), *CommonSpecTestFileInfoElementType.konstues(), *linkedTestType.infoElements.konstue),
        testInfoByContentMatcher.group("infoElements")
    )
    konst helpers = testInfoElements[CommonSpecTestFileInfoElementType.HELPERS]?.content?.splitByComma()?.toSet()

    return ParsedTestFile(
        testArea = TestArea.konstueOf(testInfoByContentMatcher.group("testArea").withUnderscores()),
        testType = TestType.konstueOf(testInfoByContentMatcher.group("testType")),
        testNumber = testInfoElements[CommonSpecTestFileInfoElementType.NUMBER]!!.content.toInt(),
        testDescription = testInfoElements[CommonSpecTestFileInfoElementType.DESCRIPTION]!!.content,
        testInfoElements = testInfoElements,
        testCasesSet = parseTestCases(testFiles),
        unexpectedBehavior = testInfoElements.contains(CommonInfoElementType.UNEXPECTED_BEHAVIOUR),
        issues = CommonParser.parseIssues(testInfoElements[CommonInfoElementType.ISSUES]),
        helpers = helpers,
        exception = testInfoElements[CommonInfoElementType.EXCEPTION]?.content?.let { TestsExceptionType.fromValue(it) }
    )
}


private fun parseImplementationTestInfo(testFilePath: String, linkedTestType: SpecTestLinkedType): ParsedTestFile {
    konst patterns = linkedTestType.patterns.konstue
    konst testInfoByContentMatcher = patterns.testInfoPattern.matcher(FileUtil.loadFile(File(testFilePath), true))

    if (!testInfoByContentMatcher.find()) {
        throw SpecTestValidationException(SpecTestValidationFailedReason.TESTINFO_NOT_VALID)
    }

    konst testInfoElements = CommonParser.parseTestInfoElements(
        arrayOf(
            *CommonInfoElementType.konstues(),
            *linkedTestType.infoElements.konstue
        ),
        testInfoByContentMatcher.group("infoElements")
    )

    konst fileNameWithoutExtension = testFilePath.split("/").last().replace(".kt", "")

    return ParsedTestFile(
        testArea = TestArea.konstueOf(testInfoByContentMatcher.group("testArea").withUnderscores()),
        testType = TestType.konstueOf(testInfoByContentMatcher.group("testType")),
        testNumber = testInfoElements[CommonSpecTestFileInfoElementType.NUMBER]?.content?.toInt() ?: 0,
        testDescription = fileNameWithoutExtension.uppercase()[0] + fileNameWithoutExtension.substring(1)
            .replace(Regex("""([A-Z])"""), " $1").lowercase(),
        testInfoElements = testInfoElements,
        testCasesSet = SpecTestCasesSet(mutableMapOf(), mutableMapOf(), mutableMapOf()), //todo
        unexpectedBehavior = testInfoElements.contains(CommonInfoElementType.UNEXPECTED_BEHAVIOUR),
        issues = CommonParser.parseIssues(testInfoElements[CommonInfoElementType.ISSUES]),
        helpers = testInfoElements[CommonSpecTestFileInfoElementType.HELPERS]?.content?.splitByComma()?.toSet(),
        exception = testInfoElements[CommonInfoElementType.EXCEPTION]?.content?.let { TestsExceptionType.fromValue(it) }
    )
}

fun tryParseTestInfo(
    testFilePath: String,
    testFiles: TestFiles,
    linkedTestType: SpecTestLinkedType,
    isImplementationTest: Boolean = false
): ParsedTestFile {
    try {
        return if (isImplementationTest)
            parseImplementationTestInfo(testFilePath, linkedTestType)
        else
            parseTestInfo(testFilePath, testFiles, linkedTestType)
    } catch (e: Exception) {
        error("Wrong format of file:\nfile://$testFilePath \n${e.message}")
    }
}


