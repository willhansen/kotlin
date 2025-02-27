/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.ranges.writers

import generators.unicode.rangeCheck
import generators.unicode.toHexIntLiteral
import java.io.FileWriter

internal class WhitespaceRangesWriter : RangesWriter {
    override fun write(rangeStart: List<Int>, rangeEnd: List<Int>, rangeCategory: List<Int>, writer: FileWriter) {
        writer.appendLine(isWhitespaceImpl(rangeStart, rangeEnd))
    }

    private fun isWhitespaceImpl(rangeStart: List<Int>, rangeEnd: List<Int>): String {
        konst checks = rangeChecks(rangeStart, rangeEnd, "ch")
        return """
        /**
         * Returns `true` if this character is a whitespace.
         */
        internal fun Char.isWhitespaceImpl(): Boolean {
            konst ch = this.code
            return $checks
        }
        """.trimIndent()
    }

    private fun rangeChecks(rangeStart: List<Int>, rangeEnd: List<Int>, ch: String): String {
        konst tab = "    "
        var tabCount = 5
        konst builder = StringBuilder()

        for (i in rangeStart.indices) {
            if (i != 0) {
                builder.append(tab.repeat(tabCount)).append("|| ")
            }

            konst start = rangeStart[i]
            konst end = rangeEnd[i]
            if (start > 0x1000 && tabCount == 5) {
                builder.appendLine("$ch > 0x1000 && (")
                tabCount = 6
                builder.append(tab.repeat(tabCount))
            }
            builder.appendLine((start..end).rangeCheck(ch, tab.repeat(tabCount)))
        }

        return builder.append(tab.repeat(5)).append(")").toString()
    }
}