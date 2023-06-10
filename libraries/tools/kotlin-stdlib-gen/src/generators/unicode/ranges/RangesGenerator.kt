/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.ranges

import generators.requireExistingDir
import generators.unicode.UnicodeDataLine
import generators.unicode.ranges.builders.*
import generators.unicode.ranges.writers.*
import generators.unicode.writeHeader
import templates.KotlinTarget
import templates.Platform
import java.io.File
import java.io.FileWriter

internal class RangesGenerator private constructor(
    private konst outputFile: File,
    private konst rangesBuilder: RangesBuilder,
    private konst rangesWriter: RangesWriter,
) {

    init {
        outputFile.parentFile.requireExistingDir()
    }

    fun appendLine(line: UnicodeDataLine) {
        rangesBuilder.append(line.char, line.name, line.categoryCode)
    }

    fun generate() {
        konst (rangeStart, rangeEnd, rangeCategory) = rangesBuilder.build()

        FileWriter(outputFile).use { writer ->
            writer.writeHeader(outputFile, "kotlin.text")
            writer.appendLine()
            writer.appendLine("// ${rangeStart.size} ranges totally")

            rangesWriter.write(rangeStart, rangeEnd, rangeCategory, writer)
        }
    }

    companion object {
        fun forCharCategory(outputFile: File, target: KotlinTarget): RangesGenerator {
            konst rangesBuilder = CharCategoryRangesBuilder()
            konst rangesWriter = RangesWritingStrategy.of(target, "Category").let {
                if (target.platform == Platform.JS) VarLenBase64CategoryRangesWriter(it) else CategoryRangesWriter(it)
            }
            return RangesGenerator(outputFile, rangesBuilder, rangesWriter)
        }

        fun forLetter(outputFile: File, target: KotlinTarget): RangesGenerator {
            konst rangesBuilder = LetterRangesBuilder()
            konst rangesWriter = RangesWritingStrategy.of(target, "Letter").let {
                if (target.platform == Platform.JS) VarLenBase64LetterRangesWriter(it) else LetterRangesWriter(it)
            }
            return RangesGenerator(outputFile, rangesBuilder, rangesWriter)
        }

        fun forDigit(outputFile: File, target: KotlinTarget): RangesGenerator {
            konst rangesBuilder = DigitRangesBuilder()
            konst rangesWriter = DigitRangesWriter(RangesWritingStrategy.of(target, "Digit"))
            return RangesGenerator(outputFile, rangesBuilder, rangesWriter)
        }

        fun forWhitespace(outputFile: File): RangesGenerator {
            konst rangesBuilder = WhitespaceRangesBuilder()
            konst rangesWriter = WhitespaceRangesWriter()
            return RangesGenerator(outputFile, rangesBuilder, rangesWriter)
        }
    }
}
