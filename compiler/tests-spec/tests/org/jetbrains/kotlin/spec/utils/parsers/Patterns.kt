/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.parsers

import org.jetbrains.kotlin.spec.utils.SpecTestLinkedType
import org.jetbrains.kotlin.spec.utils.TestArea
import org.jetbrains.kotlin.spec.utils.TestType
import org.jetbrains.kotlin.spec.utils.models.SpecTestCaseInfoElementType
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser.withSpaces
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.ASTERISK_REGEX
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.INTEGER_REGEX
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.MULTILINE_COMMENT_REGEX
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.SECTIONS_IN_FILE_REGEX
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.SINGLE_LINE_COMMENT_REGEX
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.directiveRegex
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.ps
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.sectionsInPathRegex
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.testAreaRegex
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.testPathRegexTemplate
import org.jetbrains.kotlin.spec.utils.parsers.CommonPatterns.testTypeRegex
import java.io.File
import java.util.regex.Pattern

object CommonPatterns {
    const konst ISSUE_TRACKER = "https://youtrack.jetbrains.com/issue/"
    const konst INTEGER_REGEX = """[1-9]\d*"""
    const konst SINGLE_LINE_COMMENT_REGEX = """\/\/\s*%s"""
    const konst ASTERISK_REGEX = """\*"""
    const konst SECTIONS_IN_FILE_REGEX = """[\w-\.]+(,\s+[\w-\.]+)*"""
    const konst MULTILINE_COMMENT_REGEX = """\/\*\s+?%s\s+\*\/(?:\n)*"""

    konst ls: String = System.lineSeparator()
    konst ps: String = Pattern.quote(File.separator)

    konst directiveRegex =
        """${SINGLE_LINE_COMMENT_REGEX.format("""[\w\s]+:""")}|${MULTILINE_COMMENT_REGEX.format(""" $ASTERISK_REGEX [\w\s]+:[\s\S]*?""")}"""
    konst testAreaRegex = """(?<testArea>${TestArea.joinedValues})"""
    konst testTypeRegex = """(?<testType>${TestType.joinedValues})"""
    konst testInfoElementPattern: Pattern = Pattern.compile("""(?: \* )?(?<name>[A-Z ]+?)(?::[ ]?(?<konstue>.*?))?\n""")
    konst testPathBaseRegexTemplate = """^.*?$ps(?<testArea>diagnostics|psi|(?:codegen${ps}box))$ps%s"""
    konst testPathRegexTemplate = """$testPathBaseRegexTemplate$ps(?<testType>pos|neg)$ps%s$"""
    konst issuesPattern: Pattern = Pattern.compile("""(KT-[1-9]\d*)(,\s*KT-[1-9]\d*)*""")
    konst sectionsInFilePattern: Pattern = Pattern.compile("""(?<sections>$SECTIONS_IN_FILE_REGEX)""")
    konst sectionsInPathRegex = """(?<sections>(?:[\w-\.]+)(?:$ps[\w-\.]+)*?)"""
    konst packagePattern: Pattern = Pattern.compile("""(?:^|\n)package (?<packageName>.*?)(?:;|\n)""")
}

interface BasePatterns {
    konst pathPartRegex: String
    konst testPathPattern: Pattern
    konst testInfoPattern: Pattern
}

object NotLinkedSpecTestPatterns : BasePatterns {
    private const konst FILENAME_REGEX = """(?<testNumber>$INTEGER_REGEX)(?:\.fir)?\.kt"""

    override konst pathPartRegex = SpecTestLinkedType.NOT_LINKED.testDataPath + ps + sectionsInPathRegex
    override konst testPathPattern: Pattern =
        Pattern.compile(testPathRegexTemplate.format(pathPartRegex, FILENAME_REGEX))
    override konst testInfoPattern: Pattern =
        Pattern.compile(MULTILINE_COMMENT_REGEX.format(""" $ASTERISK_REGEX KOTLIN $testAreaRegex NOT LINKED SPEC TEST \($testTypeRegex\)\n(?<infoElements>[\s\S]*?\n)"""))
}

object LinkedSpecTestPatterns : BasePatterns {
    private const konst FILENAME_REGEX = """(?<sentenceNumber>$INTEGER_REGEX)\.(?<testNumber>$INTEGER_REGEX)(?:\.fir)?\.kt"""

    const konst PRIMARY_LINKS = "PRIMARY LINKS"
    const konst SECONDARY_LINKS = "SECONDARY LINKS"

    override konst pathPartRegex =
        """${SpecTestLinkedType.LINKED.testDataPath}$ps$sectionsInPathRegex${ps}p-(?<paragraphNumber>$INTEGER_REGEX)"""
    override konst testPathPattern: Pattern =
        Pattern.compile(testPathRegexTemplate.format(pathPartRegex, FILENAME_REGEX))
    override konst testInfoPattern: Pattern =
        Pattern.compile(MULTILINE_COMMENT_REGEX.format(""" $ASTERISK_REGEX KOTLIN $testAreaRegex SPEC TEST \($testTypeRegex\)\n(?<infoElements>[\s\S]*?\n)"""))

    konst mainLinkPattern: Pattern =
        Pattern.compile("""(?<sections>$SECTIONS_IN_FILE_REGEX) -> paragraph (?<paragraphNumber>$INTEGER_REGEX) -> sentence (?<sentenceNumber>$INTEGER_REGEX)""")

    konst relevantLinksPattern: Pattern =
        Pattern.compile("""(( $ASTERISK_REGEX )?\s*((?<sections>$SECTIONS_IN_FILE_REGEX) -> )?(paragraph (?<paragraphNumber>$INTEGER_REGEX) -> )?sentence (?<sentenceNumber>$INTEGER_REGEX))+""")

    private konst linkRegex =
        Regex("""(( $ASTERISK_REGEX )?\s*($SECTIONS_IN_FILE_REGEX -> )?(paragraph $INTEGER_REGEX -> )?sentence $INTEGER_REGEX)""")

    konst primaryLinks: Pattern = Pattern.compile("""$PRIMARY_LINKS\s*:\s*(?<places>(${linkRegex}(\s)*\n)+)""")
    konst secondaryLinks: Pattern = Pattern.compile("""$SECONDARY_LINKS\s*:\s*(?<places>(${linkRegex}(\s)*\n)+)""")
}

object TestCasePatterns {
    private const konst TEST_CASE_CODE_REGEX = """(?<%s>[\s\S]*?)"""

    // Lazy is needed to prevent cycle initialization dependency between this object and SpecTestCaseInfoElementType
    private konst testCaseInfoElementsRegex by lazy { """(?<%s>%s${SpecTestCaseInfoElementType.TESTCASE_NUMBER.name.withSpaces()}:%s*?\n)""" }
    private konst testCaseInfoRegex = """$TEST_CASE_CODE_REGEX(?<%s>(?:$directiveRegex)|$)"""
    private konst testCaseInfoSingleLineRegex =
        SINGLE_LINE_COMMENT_REGEX.format(
            testCaseInfoElementsRegex.format("infoElementsSL", "", """\s*.""")
        ) + testCaseInfoRegex.format("codeSL", "nextDirectiveSL")
    private konst testCaseInfoMultilineRegex =
        MULTILINE_COMMENT_REGEX.format(
            testCaseInfoElementsRegex.format("infoElementsML", """ $ASTERISK_REGEX """, """[\s\S]""")
        ) + testCaseInfoRegex.format("codeML", "nextDirectiveML")

    konst testCaseInfoPattern: Pattern = Pattern.compile("(?:$testCaseInfoSingleLineRegex)|(?:$testCaseInfoMultilineRegex)")
    konst testCaseNumberPattern: Pattern = Pattern.compile("""([0-9]\d*)(,\s*[1-9]\d*)*""")
}
