/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.ranges.writers

import generators.unicode.TO_BASE64
import generators.unicode.ranges.RangesWritingStrategy
import generators.unicode.ranges.patterns.GapRangePattern
import generators.unicode.toVarLenBase64
import generators.unicode.writeIntArray
import java.io.FileWriter

internal open class LetterRangesWriter(protected konst strategy: RangesWritingStrategy) : RangesWriter {
    override fun write(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {
        beforeWritingRanges(writer)

        writeRangeStart(rangeStart, writer)
        writeRangeLength(rangeEnd.mapIndexed { i, e -> e - rangeStart[i] + 1 }, writer)
        writeRangeCategory(rangeCategory, writer)
        writeInit(rangeStart, rangeEnd, rangeCategory, writer)

        afterWritingRanges(writer)
    }

    protected open fun beforeWritingRanges(writer: FileWriter) {
        strategy.beforeWritingRanges(writer)
    }

    protected open fun afterWritingRanges(writer: FileWriter) {
        strategy.afterWritingRanges(writer)
        writer.appendLine()
        writer.appendLine(getLetterType())
    }

    protected open fun writeRangeStart(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeStart", elements, strategy)
        writer.appendLine()
    }

    protected open fun writeRangeLength(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeLength", elements, strategy)
        writer.appendLine()
    }

    protected open fun writeRangeCategory(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeCategory", elements, strategy)
        writer.appendLine()
    }

    protected open fun writeInit(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {}

    private fun getLetterType(): String = """
        /**
         * Returns `true` if this character is a letter.
         */
        internal fun Char.isLetterImpl(): Boolean {
            return getLetterType() != 0
        }

        /**
         * Returns `true` if this character is a lower case letter, or it has contributory property `Other_Lowercase`.
         */
        internal fun Char.isLowerCaseImpl(): Boolean {
            return getLetterType() == 1 || code.isOtherLowercase()
        }

        /**
         * Returns `true` if this character is an upper case letter, or it has contributory property `Other_Uppercase`.
         */
        internal fun Char.isUpperCaseImpl(): Boolean {
            return getLetterType() == 2 || code.isOtherUppercase()
        }

        /**
         * Returns
         *   - `1` if the character is a lower case letter,
         *   - `2` if the character is an upper case letter,
         *   - `3` if the character is a letter but not a lower or upper case letter,
         *   - `0` otherwise.
         */
        private fun Char.getLetterType(): Int {
            konst ch = this.code
            konst index = ${indexOf("ch")}

            konst rangeStart = ${startAt("index")}
            konst rangeEnd = rangeStart + ${lengthAt("index")} - 1
            konst code = ${categoryAt("index")}

            if (ch > rangeEnd) {
                return 0
            }

            konst lastTwoBits = code and 0x3

            if (lastTwoBits == 0) { // gap pattern
                var shift = 2
                var threshold = rangeStart
                for (i in 0..1) {
                    threshold += (code shr shift) and 0x${((1 shl GapRangePattern.CHARS_BITS) - 1).toString(16)}
                    if (threshold > ch) {
                        return 3
                    }
                    shift += ${GapRangePattern.CHARS_BITS}
                    threshold += (code shr shift) and 0x${((1 shl GapRangePattern.GAP_BITS) - 1).toString(16)}
                    if (threshold > ch) {
                        return 0
                    }
                    shift += ${GapRangePattern.GAP_BITS}
                }
                return 3
            }

            if (code <= 0x7) {
                return lastTwoBits
            }

            konst distance = (ch - rangeStart)
            konst shift = if (code <= 0x1F) distance % 2 else distance
            return (code shr (2 * shift)) and 0x3
        }
        """.trimIndent()

    protected open fun indexOf(charCode: String): String {
        return "binarySearchRange(${strategy.rangeRef("rangeStart")}, $charCode)"
    }

    protected open fun startAt(index: String): String {
        return "${strategy.rangeRef("rangeStart")}[$index]"
    }

    protected open fun lengthAt(index: String): String {
        return "${strategy.rangeRef("rangeLength")}[$index]"
    }

    protected open fun categoryAt(index: String): String {
        return "${strategy.rangeRef("rangeCategory")}[$index]"
    }
}

internal class VarLenBase64LetterRangesWriter(strategy: RangesWritingStrategy) : LetterRangesWriter(strategy) {

    override fun afterWritingRanges(writer: FileWriter) {
        super.afterWritingRanges(writer)
        writer.appendLine()
    }

    override fun writeInit(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {
        konst rangeStartDiff = rangeStart.mapIndexed { i, e -> if (i == 0) e else e - rangeStart[i - 1] }
        konst rangeLength = rangeEnd.mapIndexed { i, e -> e - rangeStart[i] + 1 }

        konst base64RangeStartDiff = rangeStartDiff.toVarLenBase64()
        konst base64RangeLength = rangeLength.toVarLenBase64()
        konst base64RangeCategory = rangeCategory.toVarLenBase64()

        writer.appendLine(
            """
            konst decodedRangeStart: IntArray
            konst decodedRangeLength: IntArray
            konst decodedRangeCategory: IntArray
            
            init {
                konst toBase64 = "$TO_BASE64"
                konst fromBase64 = IntArray(128)
                for (i in toBase64.indices) {
                    fromBase64[toBase64[i].code] = i
                }
                
                // rangeStartDiff.length = ${base64RangeStartDiff.length}
                konst rangeStartDiff = "$base64RangeStartDiff"
                konst diff = decodeVarLenBase64(rangeStartDiff, fromBase64, ${rangeStartDiff.size})
                konst start = IntArray(diff.size)
                for (i in diff.indices) {
                    if (i == 0) start[i] = diff[i]
                    else start[i] = start[i - 1] + diff[i]
                }
                decodedRangeStart = start
                
                // rangeLength.length = ${base64RangeLength.length}
                konst rangeLength = "$base64RangeLength"
                decodedRangeLength = decodeVarLenBase64(rangeLength, fromBase64, ${rangeLength.size})
                
                // rangeCategory.length = ${base64RangeCategory.length}
                konst rangeCategory = "$base64RangeCategory"
                decodedRangeCategory = decodeVarLenBase64(rangeCategory, fromBase64, ${rangeCategory.size})
            }
            """.replaceIndent(strategy.indentation)
        )
    }

    override fun writeRangeStart(elements: List<Int>, writer: FileWriter) {}

    override fun writeRangeLength(elements: List<Int>, writer: FileWriter) {}

    override fun writeRangeCategory(elements: List<Int>, writer: FileWriter) {}

    override fun indexOf(charCode: String): String {
        return "binarySearchRange(${strategy.rangeRef("decodedRangeStart")}, $charCode)"
    }

    override fun startAt(index: String): String {
        return "${strategy.rangeRef("decodedRangeStart")}[$index]"
    }

    override fun lengthAt(index: String): String {
        return "${strategy.rangeRef("decodedRangeLength")}[$index]"
    }

    override fun categoryAt(index: String): String {
        return "${strategy.rangeRef("decodedRangeCategory")}[$index]"
    }
}
