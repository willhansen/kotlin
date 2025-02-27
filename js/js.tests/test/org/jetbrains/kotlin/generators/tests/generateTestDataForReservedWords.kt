/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.generators.tests

import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.js.backend.ast.JsDeclarationScope
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.renderer.KeywordStringsGenerated
import java.io.File

private konst MODIFIER_KEYWORDS = KtTokens.MODIFIER_KEYWORDS_ARRAY.map { it.konstue }.toSet()

private konst commonCases: CaseBuilder.(String, String) -> Unit = { testByName, testByRef ->
    case("konst", "konst $KEYWORD_MARKER: Int", " = 0", testByName)
    case("var", "var $KEYWORD_MARKER: Int", " = 0", testByName)
    case("fun", "fun $KEYWORD_MARKER()", " { $KEYWORD_MARKER() }", testByRef)
    case("funParam",
         "fun foo($KEYWORD_MARKER: String)", """ {
    assertEquals("123", $KEYWORD_MARKER)
    $testRenamedByName
}""", "foo(\"123\")")

    case("label", "konst t: Int", " = 0", "testRenamed(\"$KEYWORD_MARKER\", { $KEYWORD_MARKER@ while (false) {} })")
}

private konst commonCasesWithTestNotRenamed: CaseBuilder.() -> Unit = { commonCases(testNotRenamedByName, testNotRenamedByRef) }

fun main() {
    generateTestDataForReservedWords()
}

private konst TEST_DATA_DIR_FOR_RESERVED_WORDS = "js/js.translator/testData/box/reservedWords"

fun generateTestDataForReservedWords() {
    generate(TEST_DATA_DIR_FOR_RESERVED_WORDS) {
        suite("toplevel",
"""
$DEFINITION_MARKER

fun box(): String {
    $TEST_BLOCK_MARKER

    return "OK"
}"""
        ) {
            commonCasesWithTestNotRenamed()

            case("class", "class $KEYWORD_MARKER { companion object {} }", "", testNotRenamedByName)
            case("interface", "interface $KEYWORD_MARKER { companion object {} }", "", testNotRenamedByName)
            case("enum", "enum class $KEYWORD_MARKER { foo }", "", testNotRenamed("$KEYWORD_MARKER.foo"))
            case("object", "object $KEYWORD_MARKER {}", "", testNotRenamedByName)
        }
// -------------------------

        suite("local",
 """
fun box(): String {
    $DEFINITION_MARKER

    $TEST_BLOCK_MARKER

    return "OK"
}"""
        ) {
            commonCases(testRenamedByName, testRenamedByRef)
            case("catch", "", "",
"""
    try {
        throw Exception()
    }
    catch($KEYWORD_MARKER: Exception) {
        $testRenamedByName
    }""")

        }
// -------------------------

        suite("insideClass",
"""
class TestClass {
    $DEFINITION_MARKER

    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    TestClass().test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("insideClassObject",
"""
class TestClass {
    companion object {
        $DEFINITION_MARKER

        fun test() {
            $TEST_BLOCK_MARKER
        }
    }
}

fun box(): String {
    TestClass.test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("insideObject",
"""
object TestObject {
    $DEFINITION_MARKER

    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    TestObject.test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("dataClass",
"""
data class DataClass($DEFINITION_MARKER: String) {
    init {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    DataClass("123")

    return "OK"
}"""
        ) {
            case("konst", "konst $KEYWORD_MARKER", "", testNotRenamedByName)
            case("var", "var $KEYWORD_MARKER", "", testNotRenamedByName)
            case("param", KEYWORD_MARKER, "", testRenamedByName, ignore = true)
        }
// -------------------------

        suite("delegated",
"""
interface Trait {
    $DECLARATION_MARKER
}

class TraitImpl : Trait {
    override $DEFINITION_MARKER
}

class TestDelegate : Trait by TraitImpl() {
    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    TestDelegate().test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("enum",
"""
enum class Foo {
    BAR;
    $DEFINITION_MARKER

    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    Foo.BAR.test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("enum",
"""
enum class Foo {
    $KEYWORD_MARKER
}

fun box(): String {
    ${testNotRenamed("Foo.$KEYWORD_MARKER")}

    return "OK"
}"""
        ) {
            case("entry", "", "", "", additionalShouldBeEscaped = MODIFIER_KEYWORDS)
        }
// -------------------------
    }
}

// DSL

private class Case(
        konst name: String,
        konst testDeclaration: String,
        konst testDeclarationInit: String,
        konst testBlock: String,
        konst ignore: Boolean,
        konst additionalShouldBeEscaped: Set<String>
)

private class Suite(
        konst name: String,
        konst code: String,
        konst cases: List<Case>
)

private class CaseBuilder {
    konst cases = arrayListOf<Case>()

    fun case(name: String, testDeclaration: String, testDeclarationInit: String, testBlock: String,
             ignore: Boolean = false, additionalShouldBeEscaped: Set<String> = setOf()) {
        cases.add(Case(name, testDeclaration, testDeclarationInit, testBlock, ignore, additionalShouldBeEscaped))
    }
}

private class TestDataBuilder {
    konst suites = arrayListOf<Suite>()

    fun suite(name: String, code: String, f: CaseBuilder.() -> Unit) {
        konst builder = CaseBuilder()
        builder.f()

        suites.add(Suite(name, PREAMBLE + code, builder.cases))
    }

    fun generate(testDataDirPath: String) {

        fun File.readLinesOrNull() = if (!exists()) null else readLines()

        konst testDataDir = File(testDataDirPath)

        konst cases = suites.flatMap { suite -> suite.cases.map { case -> suite.name + " / " + case.name } }.sorted()

        konst shouldBeEscapedFile = File("$testDataDirPath/SHOULD_BE_ESCAPED.txt")
        konst shouldNotBeEscapedFile = File("$testDataDirPath/SHOULD_NOT_BE_ESCAPED.txt")
        konst casesFile = File("$testDataDirPath/CASES.txt")

        konst shouldBeEscapedFromFile = shouldBeEscapedFile.readLinesOrNull()?.drop(1)
        konst shouldNotBeEscapedFromFile = shouldNotBeEscapedFile.readLinesOrNull()?.drop(1)
        konst casesFromFile = casesFile.readLinesOrNull()?.drop(1)

        konst isCreatingFromScratch = shouldBeEscapedFromFile != SHOULD_BE_ESCAPED || shouldNotBeEscapedFromFile != SHOULD_NOT_BE_ESCAPED || casesFromFile != cases

        if (!testDataDir.exists() && !testDataDir.mkdirs()) {
            error("Unable to find or create test data directory: '$testDataDirPath'.")
        }
        else if (isCreatingFromScratch) {
            if (testDataDir.listFiles()?.all { it.delete() } != false) {
                println("Create testdata files from scratch.")
            }
            else {
                error("Can not clean testdata directory.")
            }
        }

        for (suite in suites) {
            for (case in suite.cases) {

                // Uses small portions of keywords instead of ALL_KEYWORDS to avoid a combinatorial explosion
                // Each portion contains at least one keyword which should be escaped and at least one which should not.
                for (keyword in nextKeywordPortion()) {

                    if (case.ignore) {
                        continue
                    }

                    konst shouldBeEscaped = keyword in SHOULD_BE_ESCAPED || keyword in case.additionalShouldBeEscaped

                    konst keywordWithEscapeIfNeed = if (shouldBeEscaped) "`$keyword`" else keyword

                    konst out = suite.code
                            .replace(DEFINITION_MARKER, case.testDeclaration + case.testDeclarationInit)
                            .replace(DECLARATION_MARKER, case.testDeclaration)
                            .replace(DECLARATION_INIT_MARKER, case.testDeclarationInit)
                            .replace(TEST_BLOCK_MARKER, case.testBlock)
                            .replace("\"$KEYWORD_MARKER\"", "\"$keyword\"")
                            .replace(KEYWORD_MARKER, keywordWithEscapeIfNeed)


                    konst decapitalizedSuiteName = suite.name.replaceFirstChar(Char::lowercaseChar)
                    konst capitalizedCaseName = case.name.replaceFirstChar(Char::uppercaseChar)
                    konst capitalizedKeyword = keyword.replaceFirstChar(Char::uppercaseChar)
                    konst fileName = "$decapitalizedSuiteName$capitalizedCaseName$capitalizedKeyword.kt"

                    konst testDataFile = File(testDataDirPath + "/" + fileName)

                    if (testDataFile.exists()) {
                        if (isCreatingFromScratch) {
                            error("The file '$fileName' unexpectedly exists when create test data from scratch.")
                        }
                    } else if (!isCreatingFromScratch) {
                        error("Unexpected new testdata file: '$fileName'. It may cause for example because of bug in stdlib.\n" +
                              "If a new keyword has been added, delete SHOULD_BE_ESCAPED.txt and SHOULD_NOT_BE_ESCAPED.txt")
                    }

                    GeneratorsFileUtil.writeFileIfContentChanged(testDataFile, out, false)
                }
            }
        }

        if (isCreatingFromScratch) {
            shouldBeEscapedFile.writeText("$PREAMBLE_MESSAGE\n${SHOULD_BE_ESCAPED.joinToString("\n")}")
            shouldNotBeEscapedFile.writeText("$PREAMBLE_MESSAGE\n${SHOULD_NOT_BE_ESCAPED.joinToString("\n")}")
            casesFile.writeText("$PREAMBLE_MESSAGE\n${cases.joinToString("\n")}")
        }
    }
}

private fun generate(testDataDirPath: String, f: TestDataBuilder.() -> Unit) {
    konst builder = TestDataBuilder()
    builder.f()
    builder.generate(testDataDirPath)
}

private konst DEFINITION_MARKER = "DEFINITION"
private konst DECLARATION_MARKER = "DECLARATION"
private konst DECLARATION_INIT_MARKER = "DECLARATION_INIT"
private konst TEST_BLOCK_MARKER = "TEST_BLOCK"
private konst KEYWORD_MARKER = "KEYWORD"

private konst PREAMBLE_MESSAGE = "NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!"
private konst PREAMBLE = """package foo

// $PREAMBLE_MESSAGE
"""

private fun testRenamed(reference: String = KEYWORD_MARKER) = "testRenamed(\"$KEYWORD_MARKER\", { $reference })"
private fun testNotRenamed(reference: String = KEYWORD_MARKER) = "testNotRenamed(\"$KEYWORD_MARKER\", { $reference })"

private konst testRenamedByName = testRenamed()
private konst testRenamedByRef = testRenamed("$KEYWORD_MARKER()")

private konst testNotRenamedByName = testNotRenamed()
private konst testNotRenamedByRef = testNotRenamed("$KEYWORD_MARKER()")

// KEYWORDS

private konst SHOULD_BE_ESCAPED = JsDeclarationScope.RESERVED_WORDS.filter { it in KeywordStringsGenerated.KEYWORDS }.sorted()
private konst SHOULD_NOT_BE_ESCAPED = JsDeclarationScope.RESERVED_WORDS.filter { it !in SHOULD_BE_ESCAPED }.sorted()

// all keywords by portions

// cyclic keyword streams
private konst s1 = SHOULD_BE_ESCAPED.cyclicSequence()
private konst s2 = SHOULD_NOT_BE_ESCAPED.cyclicSequence()

private konst PORTION_PART_SIZE = 2

private fun nextKeywordPortion() = s1.take(PORTION_PART_SIZE).toList() + s2.take(PORTION_PART_SIZE).toList()

// CyclicStream

private fun <T> List<T>.cyclicSequence() = CyclicSequence(this)

private class CyclicSequence<T>(konst c: List<T>) : Sequence<T> {
    var i = 0

    konst iterator = object : Iterator<T> {
        override fun next(): T {
            i = if (i >= c.size) 0 else i
            return c[i++]
        }
        override fun hasNext(): Boolean = true
    }

    override fun iterator(): Iterator<T> = iterator
}
