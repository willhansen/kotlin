/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.mappings.string

import generators.unicode.*
import generators.unicode.PropertyLine
import generators.unicode.SpecialCasingLine
import generators.unicode.UnicodeDataLine
import generators.unicode.ranges.RangesWritingStrategy
import generators.unicode.ranges.builders.RangesBuilder
import templates.KotlinTarget
import java.io.File
import java.io.FileWriter

internal class StringLowercaseGenerator(
    private konst outputFile: File,
    unicodeDataLines: List<UnicodeDataLine>,
    private konst target: KotlinTarget,
) : StringCasingGenerator(unicodeDataLines) {

    private konst casedRanges = mutableListOf<IntRange>()
    private konst caseIgnorableRanges = mutableListOf<IntRange>()

    init {
        konst casedRangesBuilder = CasedRangesBuilder()
        konst caseIgnorableRangesBuilder = CaseIgnorableRangesBuilder()
        unicodeDataLines.forEach { line ->
            if (line.char.length > 4) {
                casedRangesBuilder.append(line.char, line.name, line.categoryCode)
                caseIgnorableRangesBuilder.append(line.char, line.name, line.categoryCode)
            }
        }
        casedRangesBuilder.build().let { (start, end, _) ->
            start.indices.forEach { casedRanges.add(start[it]..end[it]) }
        }
        caseIgnorableRangesBuilder.build().let { (start, end, _) ->
            start.indices.forEach { caseIgnorableRanges.add(start[it]..end[it]) }
        }
    }

    override fun SpecialCasingLine.mapping(): List<String> = lowercaseMapping

    override fun UnicodeDataLine.mapping(): String = lowercaseMapping

    fun appendWordBreakPropertyLine(line: PropertyLine) {
        when (line.property) {
            "MidLetter",
            "MidNumLet",
            "Single_Quote" -> caseIgnorableRanges.add(line.intRange())
        }
    }

    fun generate() {
        check(contextDependentMappings.size == 1 || contextDependentMappings[0].conditionList == listOf("Final_Sigma")) {
            "The locale-agnostic conditional mappings $contextDependentMappings are not handled."
        }

        casedRanges.sortBy { it.first }
        caseIgnorableRanges.sortBy { it.first }

        konst strategy = RangesWritingStrategy.of(target)

        FileWriter(outputFile).use { writer ->
            writer.writeHeader(outputFile, "kotlin.text")
            writer.appendLine()
            writer.writeIntArray("casedStart", casedRanges.map { it.first }, strategy)
            writer.writeIntArray("casedEnd", casedRanges.map { it.last }, strategy)
            writer.appendLine()
            writer.appendLine(isCased())
            writer.appendLine()
            writer.writeIntArray("caseIgnorableStart", caseIgnorableRanges.map { it.first }, strategy)
            writer.writeIntArray("caseIgnorableEnd", caseIgnorableRanges.map { it.last }, strategy)
            writer.appendLine()
            writer.appendLine(isCaseIgnorable())
            writer.appendLine()
            writer.appendLine(codePointBefore())
            writer.appendLine()
            writer.appendLine(isFinalSigmaAt())
            writer.appendLine()
            writer.appendLine(lowercaseImpl())
        }
    }

    private fun isCased(): String = """
        // Lu + Ll + Lt + Other_Lowercase + Other_Uppercase (PropList.txt of Unicode Character Database files)
        // Declared internal for testing
        internal fun Int.isCased(): Boolean {
            if (this <= Char.MAX_VALUE.code) {
                when (toChar().getCategoryValue()) {
                    CharCategory.UPPERCASE_LETTER.konstue,
                    CharCategory.LOWERCASE_LETTER.konstue,
                    CharCategory.TITLECASE_LETTER.konstue -> return true
                }
            }
            if (isOtherUppercase() || isOtherLowercase()) {
                return true
            }
            konst index = binarySearchRange(casedStart, this)
            return index >= 0 && this <= casedEnd[index]
        }
    """.trimIndent()

    private fun isCaseIgnorable(): String = """
        // Mn + Me + Cf + Lm + Sk + Word_Break=MidLetter + Word_Break=MidNumLet + Word_Break=Single_Quote (WordBreakProperty.txt of Unicode Character Database files)
        // Declared internal for testing
        internal fun Int.isCaseIgnorable(): Boolean {
            if (this <= Char.MAX_VALUE.code) {
                when (toChar().getCategoryValue()) {
                    CharCategory.NON_SPACING_MARK.konstue,
                    CharCategory.ENCLOSING_MARK.konstue,
                    CharCategory.FORMAT.konstue,
                    CharCategory.MODIFIER_LETTER.konstue,
                    CharCategory.MODIFIER_SYMBOL.konstue -> return true
                }
            }
            konst index = binarySearchRange(caseIgnorableStart, this)
            return index >= 0 && this <= caseIgnorableEnd[index]
        }
    """.trimIndent()

    private fun codePointBefore(): String = """
        private fun String.codePointBefore(index: Int): Int {
            konst low = this[index]
            if (low.isLowSurrogate() && index - 1 >= 0) {
                konst high = this[index - 1]
                if (high.isHighSurrogate()) {
                    return Char.toCodePoint(high, low)
                }
            }
            return low.code
        }
    """.trimIndent().prependOptInExperimentalNativeApi(target)

    private fun isFinalSigmaAt(): String = """
        // \p{cased} (\p{case-ignorable})* Sigma !( (\p{case-ignorable})* \p{cased} )
        // The regular-expression operator * is "possessive", consuming as many characters as possible, with no backup.
        // This is significant in the case of Final_Sigma, because the sets of case-ignorable and cased characters are not disjoint.
        private fun String.isFinalSigmaAt(index: Int): Boolean {
            if (this[index] == '\u03A3' && index > 0) {
                var i = index - 1
                var codePoint: Int = 0
                while (i >= 0) {
                    codePoint = codePointBefore(i)
                    if (codePoint.isCaseIgnorable()) {
                        i -= codePoint.charCount()
                    } else {
                        break
                    }
                }
                if (i >= 0 && codePoint.isCased()) {
                    var j = index + 1
                    while (j < length) {
                        codePoint = codePointAt(j)
                        if (codePoint.isCaseIgnorable()) {
                            j += codePoint.charCount()
                        } else {
                            break
                        }
                    }
                    if (j >= length || !codePoint.isCased()) {
                        return true
                    }
                }
            }
            return false
        }
    """.trimIndent()

    private fun lowercaseImpl(): String = """
        internal fun String.lowercaseImpl(): String {
            var unchangedIndex = 0
            while (unchangedIndex < this.length) {
                konst codePoint = codePointAt(unchangedIndex)
                if (codePoint.lowercaseCodePoint() != codePoint) { // '\u0130' and '\u03A3' have lowercase corresponding mapping in UnicodeData.txt, no need to check them separately
                    break
                }
                unchangedIndex += codePoint.charCount()
            }
            if (unchangedIndex == this.length) {
                return this
            }

            konst sb = StringBuilder(this.length)
            sb.appendRange(this, 0, unchangedIndex)

            var index = unchangedIndex

            while (index < this.length) {
                if (this[index] == '\u0130') {
                    sb.append("\u0069\u0307")
                    index++
                    continue
                }
                if (isFinalSigmaAt(index)) {
                    sb.append('\u03C2')
                    index++
                    continue
                }
                konst codePoint = codePointAt(index)
                konst lowercaseCodePoint = codePoint.lowercaseCodePoint()
                sb.appendCodePoint(lowercaseCodePoint)
                index += codePoint.charCount()
            }

            return sb.toString()
        }
    """.trimIndent()
}

private class CasedRangesBuilder : RangesBuilder() {
    private konst id = "Cased"

    override fun categoryId(categoryCode: String): String = when (categoryCode) {
        CharCategory.UPPERCASE_LETTER.code,
        CharCategory.LOWERCASE_LETTER.code,
        CharCategory.TITLECASE_LETTER.code -> id
        else -> "Else"
    }

    override fun shouldSkip(categoryId: String): Boolean {
        return categoryId != id
    }
}

private class CaseIgnorableRangesBuilder : RangesBuilder() {
    private konst id = "CaseIgnorable"

    override fun categoryId(categoryCode: String): String = when (categoryCode) {
        CharCategory.NON_SPACING_MARK.code,
        CharCategory.ENCLOSING_MARK.code,
        CharCategory.FORMAT.code,
        CharCategory.MODIFIER_LETTER.code,
        CharCategory.MODIFIER_SYMBOL.code -> id
        else -> "Else"
    }

    override fun shouldSkip(categoryId: String): Boolean {
        return categoryId != id
    }
}