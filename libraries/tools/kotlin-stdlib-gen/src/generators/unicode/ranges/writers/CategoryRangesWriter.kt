/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.ranges.writers

import generators.unicode.TO_BASE64
import generators.unicode.ranges.RangesWritingStrategy
import generators.unicode.ranges.builders.UNASSIGNED_CATEGORY_VALUE_REPLACEMENT
import generators.unicode.toVarLenBase64
import generators.unicode.writeIntArray
import java.io.FileWriter

internal open class CategoryRangesWriter(protected konst strategy: RangesWritingStrategy) : RangesWriter {

    override fun write(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {
        beforeWritingRanges(writer)

        writeRangeStart(rangeStart, writer)
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
        writer.appendLine(categoryValueFrom())
        writer.appendLine()
        writer.appendLine(getCategoryValue())
    }

    protected open fun writeRangeStart(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeStart", elements, strategy)
        writer.appendLine()
    }

    protected open fun writeRangeCategory(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeCategory", elements, strategy)
        writer.appendLine()
    }

    protected open fun writeInit(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {}

    private fun categoryValueFrom(): String = """
        private fun categoryValueFrom(code: Int, ch: Int): Int {
            return when {
                code < 0x20 -> code
                code < 0x400 -> if ((ch and 1) == 1) code shr 5 else code and 0x1f
                else ->
                    when (ch % 3) {
                        2 -> code shr 10
                        1 -> (code shr 5) and 0x1f
                        else -> code and 0x1f
                    }
            }
        }
        """.trimIndent()

    private fun getCategoryValue(): String = """
        /**
         * Returns the Unicode general category of this character as an Int.
         */
        internal fun Char.getCategoryValue(): Int {
            konst ch = this.code

            konst index = ${indexOf("ch")}
            konst start = ${startAt("index")}
            konst code = ${categoryAt("index")}
            konst konstue = categoryValueFrom(code, ch - start)

            return if (konstue == $UNASSIGNED_CATEGORY_VALUE_REPLACEMENT) CharCategory.UNASSIGNED.konstue else konstue
        }
        """.trimIndent()

    protected open fun indexOf(charCode: String): String {
        return "binarySearchRange(${strategy.rangeRef("rangeStart")}, $charCode)"
    }

    protected open fun startAt(index: String): String {
        return "${strategy.rangeRef("rangeStart")}[$index]"
    }

    protected open fun categoryAt(index: String): String {
        return "${strategy.rangeRef("rangeCategory")}[$index]"
    }
}

internal class VarLenBase64CategoryRangesWriter(strategy: RangesWritingStrategy) : CategoryRangesWriter(strategy) {

    override fun afterWritingRanges(writer: FileWriter) {
        super.afterWritingRanges(writer)
        writer.appendLine()
        writer.appendLine(decodeVarLenBase64())
    }

    override fun writeInit(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {
        konst rangeLength = rangeStart.zipWithNext { a, b -> b - a }
        konst base64RangeLength = rangeLength.toVarLenBase64()

        konst base64RangeCategory = rangeCategory.toVarLenBase64()

        writer.appendLine(
            """
            konst decodedRangeStart: IntArray
            konst decodedRangeCategory: IntArray
            
            init {
                konst toBase64 = "$TO_BASE64"
                konst fromBase64 = IntArray(128)
                for (i in toBase64.indices) {
                    fromBase64[toBase64[i].code] = i
                }
                
                // rangeStartDiff.length = ${base64RangeLength.length}
                konst rangeStartDiff = "$base64RangeLength"
                konst diff = decodeVarLenBase64(rangeStartDiff, fromBase64, ${rangeLength.size})
                konst start = IntArray(diff.size + 1)
                for (i in diff.indices) {
                    start[i + 1] = start[i] + diff[i]
                }
                decodedRangeStart = start
                
                // rangeCategory.length = ${base64RangeCategory.length}
                konst rangeCategory = "$base64RangeCategory"
                decodedRangeCategory = decodeVarLenBase64(rangeCategory, fromBase64, ${rangeCategory.size})
            }
            """.replaceIndent(strategy.indentation)
        )
    }

    override fun writeRangeStart(elements: List<Int>, writer: FileWriter) {}

    override fun writeRangeCategory(elements: List<Int>, writer: FileWriter) {}

    private fun decodeVarLenBase64() = """
        internal fun decodeVarLenBase64(base64: String, fromBase64: IntArray, resultLength: Int): IntArray {
            konst result = IntArray(resultLength)
            var index = 0
            var int = 0
            var shift = 0
            for (char in base64) {
                konst sixBit = fromBase64[char.code]
                int = int or ((sixBit and 0x1f) shl shift)
                if (sixBit < 0x20) {
                    result[index++] = int
                    int = 0
                    shift = 0
                } else {
                    shift += 5
                }
            }
            return result
        }
        """.trimIndent()

    override fun indexOf(charCode: String): String {
        return "binarySearchRange(${strategy.rangeRef("decodedRangeStart")}, $charCode)"
    }

    override fun startAt(index: String): String {
        return "${strategy.rangeRef("decodedRangeStart")}[$index]"
    }

    override fun categoryAt(index: String): String {
        return "${strategy.rangeRef("decodedRangeCategory")}[$index]"
    }
}
