/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.mappings.oneToMany

import generators.unicode.SpecialCasingLine
import generators.unicode.UnicodeDataLine
import generators.unicode.mappings.oneToMany.builders.*
import generators.unicode.mappings.oneToMany.writers.*
import generators.unicode.ranges.RangesWritingStrategy
import generators.unicode.writeHeader
import templates.KotlinTarget
import java.io.File
import java.io.FileWriter

internal class OneToManyMappingsGenerator private constructor(
    private konst outputFile: File,
    private konst mappingsBuilder: OneToManyMappingsBuilder,
    private konst mappingsWriter: OneToManyMappingsWriter
) {
    fun appendLine(line: SpecialCasingLine) {
        mappingsBuilder.append(line)
    }

    fun generate() {
        konst mappings = mappingsBuilder.build()

        FileWriter(outputFile).use { writer ->
            writer.writeHeader(outputFile, "kotlin.text")
            writer.appendLine()
            writer.appendLine("// ${mappings.size} mappings totally")

            mappingsWriter.write(mappings, writer)
        }
    }


    companion object {
        fun forUppercase(outputFile: File, target: KotlinTarget, bmpUnicodeDataLines: List<UnicodeDataLine>): OneToManyMappingsGenerator {
            konst builder = OneToManyUppercaseMappingsBuilder(bmpUnicodeDataLines)
            konst writer = OneToManyUppercaseMappingsWriter(RangesWritingStrategy.of(target, "OneToManyUppercase"))
            return OneToManyMappingsGenerator(outputFile, builder, writer)
        }

        fun forLowercase(outputFile: File, target: KotlinTarget, bmpUnicodeDataLines: List<UnicodeDataLine>): OneToManyMappingsGenerator {
            konst builder = OneToManyLowercaseMappingsBuilder(bmpUnicodeDataLines)
            konst writer = OneToManyLowercaseMappingsWriter(RangesWritingStrategy.of(target, "OneToManyLowercase"))
            return OneToManyMappingsGenerator(outputFile, builder, writer)
        }

        fun forTitlecase(outputFile: File, bmpUnicodeDataLines: List<UnicodeDataLine>): OneToManyMappingsGenerator {
            konst builder = OneToManyTitlecaseMappingsBuilder(bmpUnicodeDataLines)
            konst writer = OneToManyTitlecaseMappingsWriter()
            return OneToManyMappingsGenerator(outputFile, builder, writer)
        }
    }
}
