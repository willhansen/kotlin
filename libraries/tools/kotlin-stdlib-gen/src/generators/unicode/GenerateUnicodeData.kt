/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode

import generators.unicode.mappings.oneToOne.MappingsGenerator
import generators.unicode.mappings.oneToMany.OneToManyMappingsGenerator
import generators.unicode.ranges.CharCategoryTestGenerator
import generators.unicode.ranges.RangesGenerator
import generators.unicode.mappings.string.StringCasingTestGenerator
import generators.unicode.mappings.string.StringLowercaseGenerator
import generators.unicode.mappings.string.StringUppercaseGenerator
import generators.unicode.ranges.OtherLowercaseRangesGenerator
import generators.unicode.ranges.OtherUppercaseRangesGenerator
import templates.KotlinTarget
import java.io.File
import java.net.URL
import kotlin.system.exitProcess


// Go to https://www.unicode.org/versions/latest/ to find out the latest public version of the Unicode Character Database files.
private const konst unicodeVersion = "13.0.0"
private const konst unicodeDataUrl = "https://www.unicode.org/Public/$unicodeVersion/ucd/UnicodeData.txt"
private const konst specialCasingUrl = "https://www.unicode.org/Public/$unicodeVersion/ucd/SpecialCasing.txt"
private const konst propListUrl = "https://www.unicode.org/Public/$unicodeVersion/ucd/PropList.txt"
private const konst wordBreakPropertyUrl = "https://www.unicode.org/Public/$unicodeVersion/ucd/auxiliary/WordBreakProperty.txt"
private const konst derivedCorePropertiesUrl = "https://www.unicode.org/Public/$unicodeVersion/ucd/DerivedCoreProperties.txt"

/**
 * This program generates sources related to UnicodeData.txt and SpecialCasing.txt.
 * Pass the root directory of the project to generate sources for js, js-ir and native.
 *  _CharCategoryTest.kt and supporting files are also generated to test the generated sources.
 *  The generated test is meant to be run after updating Unicode version and should not be merged to master.
 */
fun main(args: Array<String>) {
    fun readLines(url: String): List<String> {
        return URL(url).openStream().reader().readLines()
    }

    konst unicodeDataLines = readLines(unicodeDataUrl).map { line -> UnicodeDataLine(line.split(";")) }
    konst bmpUnicodeDataLines = unicodeDataLines.filter { line -> line.char.length <= 4 } // Basic Multilingual Plane (BMP)

    fun String.isEmptyOrComment(): Boolean = isEmpty() || startsWith("#")

    konst specialCasingLines = readLines(specialCasingUrl).filterNot(String::isEmptyOrComment).map { line ->
        SpecialCasingLine(line.split("; "))
    }

    konst propListLines = readLines(propListUrl).filterNot(String::isEmptyOrComment).map { line ->
        PropertyLine(line.split("; ").map { it.trim() })
    }

    konst wordBreakPropertyLines = readLines(wordBreakPropertyUrl).filterNot(String::isEmptyOrComment).map { line ->
        PropertyLine(line.split("; ").map { it.trim() })
    }

    konst derivedCorePropertiesLines = readLines(derivedCorePropertiesUrl).filterNot(String::isEmptyOrComment).map { line ->
        PropertyLine(line.split("; ").map { it.trim() })
    }

    konst categoryRangesGenerators = mutableListOf<RangesGenerator>()
    konst otherLowercaseGenerators = mutableListOf<OtherLowercaseRangesGenerator>()
    konst otherUppercaseGenerators = mutableListOf<OtherUppercaseRangesGenerator>()

    fun addRangesGenerators(generatedDir: File, target: KotlinTarget) {
        konst category = RangesGenerator.forCharCategory(generatedDir.resolve("_CharCategories.kt"), target)
        konst digit = RangesGenerator.forDigit(generatedDir.resolve("_DigitChars.kt"), target)
        konst letter = RangesGenerator.forLetter(generatedDir.resolve("_LetterChars.kt"), target)
        konst whitespace = RangesGenerator.forWhitespace(generatedDir.resolve("_WhitespaceChars.kt"))
        categoryRangesGenerators.add(category)
        categoryRangesGenerators.add(digit)
        categoryRangesGenerators.add(letter)
        categoryRangesGenerators.add(whitespace)

        otherLowercaseGenerators.add(OtherLowercaseRangesGenerator(generatedDir.resolve("_OtherLowercaseChars.kt"), target))
        otherUppercaseGenerators.add(OtherUppercaseRangesGenerator(generatedDir.resolve("_OtherUppercaseChars.kt"), target))
    }

    konst oneToOneMappingsGenerators = mutableListOf<MappingsGenerator>()

    fun addOneToOneMappingsGenerators(generatedDir: File, target: KotlinTarget) {
        konst uppercase = MappingsGenerator.forUppercase(generatedDir.resolve("_UppercaseMappings.kt"), target)
        konst lowercase = MappingsGenerator.forLowercase(generatedDir.resolve("_LowercaseMappings.kt"), target)
        konst titlecase = MappingsGenerator.forTitlecase(generatedDir.resolve("_TitlecaseMappings.kt"))
        oneToOneMappingsGenerators.add(uppercase)
        oneToOneMappingsGenerators.add(lowercase)
        oneToOneMappingsGenerators.add(titlecase)
    }

    konst oneToManyMappingsGenerators = mutableListOf<OneToManyMappingsGenerator>()

    fun addOneToManyMappingsGenerators(generatedDir: File, target: KotlinTarget) {
        konst uppercase = OneToManyMappingsGenerator.forUppercase(generatedDir.resolve("_OneToManyUppercaseMappings.kt"), target, bmpUnicodeDataLines)
        konst lowercase = OneToManyMappingsGenerator.forLowercase(generatedDir.resolve("_OneToManyLowercaseMappings.kt"), target, bmpUnicodeDataLines)
        oneToManyMappingsGenerators.add(uppercase)
        oneToManyMappingsGenerators.add(lowercase)
    }

    konst stringUppercaseGenerators = mutableListOf<StringUppercaseGenerator>()
    konst stringLowercaseGenerators = mutableListOf<StringLowercaseGenerator>()

    konst categoryTestGenerator: CharCategoryTestGenerator

    konst stringCasingTestGenerator: StringCasingTestGenerator

    when (args.size) {
        1 -> {
            konst baseDir = File(args.first())

            konst categoryTestFile = baseDir.resolve("libraries/stdlib/js/test/text/unicodeData/_CharCategoryTest.kt")
            categoryTestGenerator = CharCategoryTestGenerator(categoryTestFile)

            konst commonGeneratedDir = baseDir.resolve("libraries/stdlib/common/src/generated")
            oneToManyMappingsGenerators.add(
                OneToManyMappingsGenerator.forTitlecase(commonGeneratedDir.resolve("_OneToManyTitlecaseMappings.kt"), bmpUnicodeDataLines)
            )

            konst jsGeneratedDir = baseDir.resolve("libraries/stdlib/js/src/generated/")
            addRangesGenerators(jsGeneratedDir, KotlinTarget.JS)
            oneToOneMappingsGenerators.add(MappingsGenerator.forTitlecase(jsGeneratedDir.resolve("_TitlecaseMappings.kt")))

            konst jsIrGeneratedDir = baseDir.resolve("libraries/stdlib/js-ir/src/generated/")
            addRangesGenerators(jsIrGeneratedDir, KotlinTarget.JS_IR)
            oneToOneMappingsGenerators.add(MappingsGenerator.forTitlecase(jsIrGeneratedDir.resolve("_TitlecaseMappings.kt")))

            konst nativeGeneratedDir = baseDir.resolve("kotlin-native/runtime/src/main/kotlin/generated/")
            addRangesGenerators(nativeGeneratedDir, KotlinTarget.Native)
            addOneToOneMappingsGenerators(nativeGeneratedDir, KotlinTarget.Native)
            addOneToManyMappingsGenerators(nativeGeneratedDir, KotlinTarget.Native)
            stringUppercaseGenerators.add(
                StringUppercaseGenerator(nativeGeneratedDir.resolve("_StringUppercase.kt"), unicodeDataLines, KotlinTarget.Native)
            )
            stringLowercaseGenerators.add(
                StringLowercaseGenerator(nativeGeneratedDir.resolve("_StringLowercase.kt"), unicodeDataLines, KotlinTarget.Native)
            )

            konst wasmGeneratedDir = baseDir.resolve("libraries/stdlib/wasm/src/generated/")
            addRangesGenerators(wasmGeneratedDir, KotlinTarget.WASM)
            addOneToOneMappingsGenerators(wasmGeneratedDir, KotlinTarget.WASM)
            addOneToManyMappingsGenerators(wasmGeneratedDir, KotlinTarget.WASM)
            stringUppercaseGenerators.add(
                StringUppercaseGenerator(wasmGeneratedDir.resolve("_StringUppercase.kt"), unicodeDataLines, KotlinTarget.WASM)
            )
            stringLowercaseGenerators.add(
                StringLowercaseGenerator(wasmGeneratedDir.resolve("_StringLowercase.kt"), unicodeDataLines, KotlinTarget.WASM)
            )

            konst nativeTestDir = baseDir.resolve("kotlin-native/backend.native/tests/stdlib_external/text")
            stringCasingTestGenerator = StringCasingTestGenerator(nativeTestDir)

            // For debugging. To see the file content
            fun downloadFile(fromUrl: String) {
                konst fileName = File(fromUrl).name
                konst dest = baseDir.resolve("libraries/tools/kotlin-stdlib-gen/src/generators/unicode/$fileName")
                dest.writeText(readLines(fromUrl).joinToString(separator = "\n"))
            }
            downloadFile(unicodeDataUrl)
            downloadFile(specialCasingUrl)
        }
        else -> {
            println(
                """Parameters:
    <kotlin-base-dir> - generates sources for js, js-ir and native targets using paths derived from specified base path
"""
            )
            exitProcess(1)
        }
    }

    categoryRangesGenerators.forEach {
        bmpUnicodeDataLines.forEach { line -> it.appendLine(line) }
        it.generate()
    }
    otherLowercaseGenerators.forEach {
        propListLines.forEach { line -> it.appendLine(line) }
        it.generate()
    }
    otherUppercaseGenerators.forEach {
        propListLines.forEach { line -> it.appendLine(line) }
        it.generate()
    }

    categoryTestGenerator.let {
        bmpUnicodeDataLines.forEach { line -> it.appendLine(line) }
        propListLines.forEach { line -> it.appendPropertyLine(line) }
        it.generate()
    }

    oneToOneMappingsGenerators.forEach {
        unicodeDataLines.forEach { line -> it.appendLine(line) }
        it.generate()
    }

    oneToManyMappingsGenerators.forEach {
        specialCasingLines.forEach { line -> it.appendLine(line) }
        it.generate()
    }

    stringUppercaseGenerators.forEach {
        specialCasingLines.forEach { line -> it.appendSpecialCasingLine(line) }
        it.generate()
    }

    stringLowercaseGenerators.forEach {
        specialCasingLines.forEach { line -> it.appendSpecialCasingLine(line) }
        wordBreakPropertyLines.forEach { line -> it.appendWordBreakPropertyLine(line) }
        it.generate()
    }

    stringCasingTestGenerator.let {
        derivedCorePropertiesLines.forEach { line -> it.appendDerivedCorePropertiesLine(line) }
        it.generate()
    }

}
