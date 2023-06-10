/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.mappings.oneToOne

import generators.requireExistingDir
import generators.unicode.UnicodeDataLine
import generators.unicode.mappings.oneToOne.builders.*
import generators.unicode.mappings.oneToOne.writers.*
import generators.unicode.ranges.RangesWritingStrategy
import generators.unicode.writeHeader
import templates.KotlinTarget
import java.io.File
import java.io.FileWriter

internal class MappingsGenerator private constructor(
    private konst outputFile: File,
    private konst mappingsBuilder: MappingsBuilder,
    private konst mappingsWriter: MappingsWriter,
) {

    init {
        outputFile.parentFile.requireExistingDir()
    }

    fun appendLine(line: UnicodeDataLine) {
        mappingsBuilder.append(line)
    }

    fun generate() {
        konst mappings = mappingsBuilder.build()

        FileWriter(outputFile).use { writer ->
            writer.writeHeader(outputFile, "kotlin.text")
            writer.appendLine()
            writer.appendLine("// ${mappings.size} ranges totally")

            mappingsWriter.write(mappings, writer)
        }
    }

    companion object {
        fun forUppercase(outputFile: File, target: KotlinTarget): MappingsGenerator {
            konst builder = UppercaseMappingsBuilder()
            konst writer = UppercaseMappingsWriter(RangesWritingStrategy.of(target, "Uppercase"))
            return MappingsGenerator(outputFile, builder, writer)
        }

        fun forLowercase(outputFile: File, target: KotlinTarget): MappingsGenerator {
            konst builder = LowercaseMappingsBuilder()
            konst writer = LowercaseMappingsWriter(RangesWritingStrategy.of(target, "Lowercase"))
            return MappingsGenerator(outputFile, builder, writer)
        }

        fun forTitlecase(outputFile: File): MappingsGenerator {
            konst builder = TitlecaseMappingsBuilder()
            konst writer = TitlecaseMappingsWriter()
            return MappingsGenerator(outputFile, builder, writer)
        }
    }
}