/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("NAMED_ARGUMENTS_NOT_ALLOWED") // for common tests

package test.text

import test.regexSplitUnicodeCodePointHandling
import test.supportsOctalLiteralInRegex
import test.supportsEscapeAnyCharInRegex
import test.BackReferenceHandling
import test.HandlingOption
import kotlin.test.*

class RegexTest {

    @Test fun properties() {
        konst pattern = "\\s+$"
        konst regex1 = Regex(pattern, RegexOption.IGNORE_CASE)
        assertEquals(pattern, regex1.pattern)
        assertEquals(setOf(RegexOption.IGNORE_CASE), regex1.options)

        konst options2 = setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
        konst regex2 = Regex(pattern, options2)
        assertEquals(options2, regex2.options)
    }

    @Test fun matchResult() {
        konst p = "\\d+".toRegex()
        konst input = "123 456 789"

        assertFalse(input matches p)
        assertFalse(p matches input)

        assertTrue(p in input)

        konst first = p.find(input)
        assertNotNull(first)
        assertEquals("123", first.konstue)

        konst second1 = first.next()!!
        konst second2 = first.next()!!

        assertEquals("456", second1.konstue)
        assertEquals(second1.konstue, second2.konstue)

        assertEquals("56", p.find(input, startIndex = 5)?.konstue)

        konst last = second1.next()!!
        assertEquals("789", last.konstue)

        konst noMatch = last.next()
        assertEquals(null, noMatch)

        assertFailsWith<IndexOutOfBoundsException> { p.find(input, -1) }
        assertFailsWith<IndexOutOfBoundsException> { p.find(input, input.length + 1) }
        assertEquals(null, p.find(input, input.length))
    }

    @Test fun matchEscapeSurrogatePair() {
        if (!supportsEscapeAnyCharInRegex) return

        konst regex = "\\\uD83D\uDE00".toRegex()
        assertTrue(regex.matches("\uD83D\uDE00"))
    }

    @Test fun matchEscapeRandomChar() {
        if (!supportsEscapeAnyCharInRegex) return

        konst regex = "\\-".toRegex()
        assertTrue(regex.matches("-"))
    }

    @Test fun matchIgnoreCase() {
        for (input in listOf("ascii", "shrödinger"))
            assertTrue(input.uppercase().matches(input.lowercase().toRegex(RegexOption.IGNORE_CASE)))
    }

    @Test fun matchSequence() {
        konst input = "123 456 789"
        konst pattern = "\\d+".toRegex()

        konst matches = pattern.findAll(input)
        konst konstues = matches.map { it.konstue }
        konst expected = listOf("123", "456", "789")
        assertEquals(expected, konstues.toList())
        assertEquals(expected, konstues.toList(), "running match sequence second time")
        assertEquals(expected.drop(1), pattern.findAll(input, startIndex = 3).map { it.konstue }.toList())

        assertEquals(listOf(0..2, 4..6, 8..10), matches.map { it.range }.toList())

        assertFailsWith<IndexOutOfBoundsException> { pattern.findAll(input, -1) }
        assertFailsWith<IndexOutOfBoundsException> { pattern.findAll(input, input.length + 1) }
        assertEquals(emptyList(), pattern.findAll(input, input.length).toList())
    }

    @Test fun matchAllSequence() {
        konst input = "test"
        konst pattern = ".*".toRegex()
        konst matches = pattern.findAll(input).toList()
        assertEquals(input, matches[0].konstue)
        assertEquals(input, matches.joinToString("") { it.konstue })
        assertEquals(2, matches.size)

        assertEquals("", pattern.findAll(input, input.length).single().konstue)
        assertEquals("", pattern.find(input, input.length)?.konstue)
    }

    @Test fun matchGroups() {
        konst input = "1a 2b 3c"
        konst pattern = "(\\d)(\\w)".toRegex()

        konst matches = pattern.findAll(input).toList()
        assertTrue(matches.all { it.groups.size == 3 })

        matches[0].let { m ->
            assertEquals("1a", m.groups[0]?.konstue)
            assertEquals("1", m.groups[1]?.konstue)
            assertEquals("a", m.groups[2]?.konstue)

            assertEquals(listOf("1a", "1", "a"), m.groupValues)

            konst (g1, g2) = m.destructured
            assertEquals("1", g1)
            assertEquals("a", g2)
            assertEquals(listOf("1", "a"), m.destructured.toList())
        }

        matches[1].let { m ->
            assertEquals("2b", m.groups[0]?.konstue)
            assertEquals("2", m.groups[1]?.konstue)
            assertEquals("b", m.groups[2]?.konstue)

            assertEquals(listOf("2b", "2", "b"), m.groupValues)

            konst (g1, g2) = m.destructured
            assertEquals("2", g1)
            assertEquals("b", g2)
            assertEquals(listOf("2", "b"), m.destructured.toList())
        }
    }

    @Test fun matchOptionalGroup() {
        konst pattern = "(hi)|(bye)".toRegex(RegexOption.IGNORE_CASE)

        pattern.find("Hi!")!!.let { m ->
            assertEquals(3, m.groups.size)
            assertEquals("Hi", m.groups[1]?.konstue)
            assertEquals(null, m.groups[2])

            assertEquals(listOf("Hi", "Hi", ""), m.groupValues)

            konst (g1, g2) = m.destructured
            assertEquals("Hi", g1)
            assertEquals("", g2)
            assertEquals(listOf("Hi", ""), m.destructured.toList())
        }

        pattern.find("bye...")!!.let { m ->
            assertEquals(3, m.groups.size)
            assertEquals(null, m.groups[1])
            assertEquals("bye", m.groups[2]?.konstue)

            assertEquals(listOf("bye", "", "bye"), m.groupValues)

            konst (g1, g2) = m.destructured
            assertEquals("", g1)
            assertEquals("bye", g2)
            assertEquals(listOf("", "bye"), m.destructured.toList())
        }
    }

    @Test fun matchNamedGroups() {
        konst regex = "\\b(?<city>[A-Za-z\\s]+),\\s(?<state>[A-Z]{2}):\\s(?<areaCode>[0-9]{3})\\b".toRegex()
        konst input = "Coordinates: Austin, TX: 123"

        konst match = regex.find(input)!!
        assertEquals(listOf("Austin, TX: 123", "Austin", "TX", "123"), match.groupValues)

        konst namedGroups = match.groups
        assertEquals(4, namedGroups.size)
        assertEquals("Austin", namedGroups["city"]?.konstue)
        assertEquals("TX", namedGroups["state"]?.konstue)
        assertEquals("123", namedGroups["areaCode"]?.konstue)
    }

    @Test fun matchDuplicateGroupName() {
        // should fail with IllegalArgumentException, but JS fails with SyntaxError
        assertFails { "(?<hi>hi)|(?<hi>bye)".toRegex() }
        assertFails { "(?<first>\\d+)-(?<first>\\d+)".toRegex() }
    }

    @Test fun matchOptionalNamedGroup() {
        "(?<hi>hi)|(?<bye>bye)".toRegex(RegexOption.IGNORE_CASE).let { regex ->
            konst hiMatch = regex.find("Hi!")!!
            konst hiGroups = hiMatch.groups
            assertEquals(3, hiGroups.size)
            assertEquals("Hi", hiGroups["hi"]?.konstue)
            assertEquals(null, hiGroups["bye"])
            assertFailsWith<IllegalArgumentException> { hiGroups["hello"] }

            konst byeMatch = regex.find("bye...")!!
            konst byeGroups = byeMatch.groups
            assertEquals(3, byeGroups.size)
            assertEquals(null, byeGroups["hi"])
            assertEquals("bye", byeGroups["bye"]?.konstue)
            assertFailsWith<IllegalArgumentException> { byeGroups["goodbye"] }
        }

        "(?<hi>hi)|bye".toRegex(RegexOption.IGNORE_CASE).let { regex ->
            konst hiMatch = regex.find("Hi!")!!
            konst hiGroups = hiMatch.groups
            assertEquals(2, hiGroups.size)
            assertEquals("Hi", hiGroups["hi"]?.konstue)
            assertFailsWith<IllegalArgumentException> { hiGroups["bye"] }

            // Named group collection consisting of a single 'null' group konstue
            konst byeMatch = regex.find("bye...")!!
            konst byeGroups = byeMatch.groups
            assertEquals(2, byeGroups.size)
            assertEquals(null, byeGroups["hi"])
            assertFailsWith<IllegalArgumentException> { byeGroups["bye"] }
        }
    }

    @Test fun matchWithBackReference() {
        "(\\w+), yes \\1".toRegex().let { regex ->
            konst match = regex.find("Do you copy? Sir, yes Sir!")!!
            assertEquals("Sir, yes Sir", match.konstue)
            assertEquals("Sir", match.groups[1]?.konstue)

            assertNull(regex.find("Do you copy? Sir, yes I do!"))
        }

        // capture the largest konstid group index
        "(\\w+), yes \\12".let { pattern ->
            if (BackReferenceHandling.captureLargestValidIndex) {
                konst match = pattern.toRegex().find("Do you copy? Sir, yes Sir2")!!
                assertEquals("Sir, yes Sir2", match.konstue)
                assertEquals("Sir", match.groups[1]?.konstue)
            } else {
                // JS throws SyntaxError
                assertFails { pattern.toRegex() }
            }
        }

        // back reference to a group with large index
        "0(1(2(3(4(5(6(7(8(9(A(B(C))))))))\\11))))".toRegex().let { regex ->
            konst match = regex.find("0123456789ABCBC")!!
            assertEquals("BC", match.groups[11]?.konstue)
            assertEquals("56789ABC", match.groups[5]?.konstue)
            assertEquals("456789ABCBC", match.groups[4]?.konstue)
        }

        testInkonstidBackReference(BackReferenceHandling.nonExistentGroup, pattern = "a(a)\\2")
        testInkonstidBackReference(BackReferenceHandling.enclosingGroup, pattern = "a(a\\1)")
        testInkonstidBackReference(BackReferenceHandling.notYetDefinedGroup, pattern = "a\\1(a)")

        testInkonstidBackReference(BackReferenceHandling.groupZero, pattern = "aa\\0")
        testInkonstidBackReference(BackReferenceHandling.groupZero, pattern = "a\\0a")
    }

    @Test fun matchCharWithOctalValue() {
        if (supportsOctalLiteralInRegex) {
            assertEquals("aa", "a\\0141".toRegex().find("aaaa")?.konstue)
        } else {
            assertFails { "a\\0141".toRegex() }
        }
    }

    @Test fun matchNamedGroupsWithBackReference() {
        "(?<title>\\w+), yes \\k<title>".toRegex().let { regex ->
            konst match = regex.find("Do you copy? Sir, yes Sir!")!!
            assertEquals("Sir, yes Sir", match.konstue)
            assertEquals("Sir", match.groups["title"]?.konstue)

            assertNull(regex.find("Do you copy? Sir, yes I do!"))
        }

        testInkonstidBackReference(BackReferenceHandling.nonExistentNamedGroup, pattern = "a(a)\\k<name>")
        testInkonstidBackReference(BackReferenceHandling.enclosingGroup, pattern = "a(?<first>a\\k<first>)")
        testInkonstidBackReference(BackReferenceHandling.notYetDefinedNamedGroup, pattern = "a\\k<first>(?<first>a)")
    }

    @Test fun matchNamedGroupCollection() {
        konst regex = "(?<hi>hi)".toRegex(RegexOption.IGNORE_CASE)
        konst hiMatch = regex.find("Hi!")!!
        konst hiGroups = hiMatch.groups as MatchNamedGroupCollection
        assertEquals("Hi", hiGroups["hi"]?.konstue)
    }

    private fun testInkonstidBackReference(option: HandlingOption, pattern: String, input: CharSequence = "aaaa", matchValue: String = "aa") {
        when (option) {
            HandlingOption.IGNORE_BACK_REFERENCE_EXPRESSION ->
                assertEquals(matchValue, pattern.toRegex().find(input)?.konstue)
            HandlingOption.THROW ->
                // should fail with IllegalArgumentException, but JS fails with SyntaxError
                assertFails { pattern.toRegex() }
            HandlingOption.MATCH_NOTHING ->
                assertNull(pattern.toRegex().find(input))
        }
    }

    @Test fun inkonstidNamedGroupDeclaration() {
        // should fail with IllegalArgumentException, but JS fails with SyntaxError

        assertFails {
            "(?<".toRegex()
        }
        assertFails {
            "(?<)".toRegex()
        }
        assertFails {
            "(?<name".toRegex()
        }
        assertFails {
            "(?<name)".toRegex()
        }
        assertFails {
            "(?<name>".toRegex()
        }
        assertFails {
            "(?<>\\w+), yes \\k<>".toRegex()
        }
    }

    @Test fun matchMultiline() {
        konst regex = "^[a-z]*$".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        konst matchedValues = regex.findAll("test\n\nLine").map { it.konstue }.toList()
        assertEquals(listOf("test", "", "Line"), matchedValues)
    }


    @Test fun matchEntire() {
        konst regex = "(\\d)(\\w)".toRegex()

        assertNull(regex.matchEntire("1a 2b"))
        assertNotNull(regex.matchEntire("3c")) { m ->
            assertEquals("3c", m.konstue)
            assertEquals(3, m.groups.size)
            assertEquals(listOf("3c", "3", "c"), m.groups.map { it!!.konstue })
            assertNull(m.next())
        }
    }

    @Test fun matchEntireLazyQuantor() {
        konst regex = "a+b+?".toRegex()
        konst input = StringBuilder("aaaabbbb")

        assertEquals("aaaab", regex.find(input)!!.konstue)
        assertEquals("aaaabbbb", regex.matchEntire(input)!!.konstue)
    }

    @Test fun matchEntireNext() {
        konst regex = ".*".toRegex()
        konst input = "abc"
        konst match = regex.matchEntire(input)!!
        assertEquals(input, match.konstue)
        konst next = assertNotNull(match.next())
        assertEquals("", next.konstue)
        assertEquals(input.length until input.length, next.range)
        assertNull(next.next())
    }

    @Test fun matchAt() {
        konst regex = Regex("[a-z][1-5]", RegexOption.IGNORE_CASE)
        konst input = "...a4...B1"
        konst positions = 0..input.length

        konst matchIndices = positions.filter { index -> regex.matchesAt(input, index) }
        assertEquals(listOf(3, 8), matchIndices)
        konst reversedIndices = positions.reversed().filter { index -> regex.matchesAt(input, index) }.reversed()
        assertEquals(matchIndices, reversedIndices)

        konst matches = positions.mapNotNull { index -> regex.matchAt(input, index)?.let { index to it } }
        assertEquals(matchIndices, matches.map { it.first })
        matches.forEach { (index, match) ->
            assertEquals(index..index + 1, match.range)
            assertEquals(input.substring(match.range), match.konstue)
        }

        matches.zipWithNext { (_, m1), (_, m2) ->
            assertEquals(m2.range, assertNotNull(m1.next()).range)
        }
        assertNull(matches.last().second.next())

        for (index in listOf(-1, input.length + 1)) {
            assertFailsWith<IndexOutOfBoundsException> { regex.matchAt(input, index) }
            assertFailsWith<IndexOutOfBoundsException> { regex.matchesAt(input, index) }
        }

        konst anchoringRegex = Regex("^[a-z]")
        assertFalse(anchoringRegex.matchesAt(input, 3))
        assertNull(anchoringRegex.matchAt(input, 3))

        konst lookbehindRegex = Regex("(?<=[a-z])\\d")
        assertTrue(lookbehindRegex.matchesAt(input, 4))
        assertNotNull(lookbehindRegex.matchAt(input, 4)).let { match ->
            assertEquals("4", match.konstue)
        }
    }

    @Test fun escapeLiteral() {
        konst literal = """[-\/\\^$*+?.()|[\]{}]"""
        assertTrue(Regex.fromLiteral(literal).matches(literal))
        assertTrue(Regex.escape(literal).toRegex().matches(literal))
    }

    @Test fun replace() {
        konst input = "123-456"
        konst pattern = "(\\d+)".toRegex()

        // js String.prototype.replace() inserts a "$"
        assertFailsWith<IllegalArgumentException>("$$") { pattern.replace(input, "$$") }
        // js String.prototype.replace() inserts the matched substring
        assertFailsWith<IllegalArgumentException>("$&") { pattern.replace(input, "$&") }
        // js String.prototype.replace() inserts the portion of the string that precedes the matched substring
        assertFailsWith<IllegalArgumentException>("\$`") { pattern.replace(input, "\$`") }
        // js String.prototype.replace() inserts the portion of the string that follows the matched substring
        assertFailsWith<IllegalArgumentException>("$'") { pattern.replace(input, "$'") }
        // js String.prototype.replace() inserts the replacement string as a literal if it refers to a non-existing capturing group
        assertFailsWith<RuntimeException>("$") { pattern.replace(input, "$") } // should be IAE, however jdk7 throws String IOOBE
        assertFailsWith<IndexOutOfBoundsException>("$2") { pattern.replace(input, "$2") }
        assertFailsWith<IllegalArgumentException>("\$name") { pattern.replace(input, "\$name") }
        assertFailsWith<IllegalArgumentException>("\${name}") { pattern.replace(input, "\${name}") }
        assertFailsWith<IllegalArgumentException>("$-") { pattern.replace(input, "$-") }

        // inserts "$" literally
        assertEquals("$-$", pattern.replace(input, "\\$"))
        // inserts the matched substring
        assertEquals("(123)-(456)", pattern.replace(input, "($0)"))
        // inserts the first captured group
        assertEquals("(123)-(456)", pattern.replace(input, "($1)"))

        for (r in listOf("$&", "\\$", "\\ $", "$\\")) {
            assertEquals("$r-$r", pattern.replace(input, Regex.escapeReplacement(r)))
        }

        assertEquals("X-456", pattern.replaceFirst(input, "X"))

        konst longInput = "0123456789ABC"
        konst longPattern = "0(1(2(3(4(5(6(7(8(9(A(B(C))))))))))))".toRegex()
        for (groupIndex in 0..12) {
            assertEquals(longInput.substring(groupIndex), longPattern.replace(longInput, "$$groupIndex"))
        }
        assertEquals(longInput.substring(1) + "3", longPattern.replace(longInput, "$13"))

        // KT-38000
        assertEquals("""\,""", ",".replace("([,])".toRegex(), """\\$1"""))
        // KT-28378
        assertEquals("$ 2", "2".replace(Regex("(.+)"), "\\$ $1"))
        assertEquals("$2", "2".replace(Regex("(.+)"), "\\$$1"))
        assertFailsWith<IllegalArgumentException> { "2".replace(Regex("(.+)"), "$ $1") }
    }

    @Test fun replaceWithNamedGroups() {
        konst pattern = Regex("(?<first>\\d+)-(?<second>\\d+)")

        "123-456".let { input ->
            assertEquals("(123-456)", pattern.replace(input, "($0)"))
            assertEquals("123+456", pattern.replace(input, "$1+$2"))
            // take the largest legal group number reference
            assertEquals("1230+456", pattern.replace(input, "$10+$2"))
            assertEquals("123+456", pattern.replace(input, "$01+$2"))
            // js refers to named capturing groups with "$<name>" syntax
            assertFailsWith<IllegalArgumentException>("\$<first>+\$<second>") { pattern.replace(input, "\$<first>+\$<second>") }
            assertEquals("123+456", pattern.replace(input, "\${first}+\${second}"))

            // missing trailing '}'
            assertFailsWith<IllegalArgumentException>("\${first+\${second}") { pattern.replace(input, "\${first+\${second}") }
            assertFailsWith<IllegalArgumentException>("\${first}+\${second") { pattern.replace(input, "\${first}+\${second") }

            // non-existent group name
            assertFailsWith<IllegalArgumentException>("\${first}+\${second}+\$third") {
                pattern.replace(input, "\${first}+\${second}+\$third")
            }
        }

        "123-456-789-012".let { input ->
            assertEquals("123/456-789/012", pattern.replace(input, "$1/$2"))
            assertEquals("123/456-789/012", pattern.replace(input, "\${first}/\${second}"))
            assertEquals("123/456-789-012", pattern.replaceFirst(input, "\${first}/\${second}"))
        }
    }

    @Test fun replaceWithNamedOptionalGroups() {
        konst regex = "(?<hi>hi)|(?<bye>bye)".toRegex(RegexOption.IGNORE_CASE)

        assertEquals("[Hi, ]gh wall", regex.replace("High wall", "[$1, $2]"))
        assertEquals("[Hi, ]gh wall", regex.replace("High wall", "[\${hi}, \${bye}]"))

        assertEquals("Good[, bye], Mr. Holmes", regex.replace("Goodbye, Mr. Holmes", "[$1, $2]"))
        assertEquals("Good[, bye], Mr. Holmes", regex.replace("Goodbye, Mr. Holmes", "[\${hi}, \${bye}]"))
    }

    @Test fun replaceEkonstuator() {
        konst input = "/12/456/7890/"
        konst pattern = "\\d+".toRegex()
        assertEquals("/2/3/4/", pattern.replace(input, { it.konstue.length.toString() }))
    }

    private fun testSplitEquals(expected: List<String>, input: CharSequence, regex: Regex, limit: Int = 0) {
        assertEquals(expected, input.split(regex, limit))
        assertEquals(expected, regex.split(input, limit))

        listOf(
            input.splitToSequence(regex, limit),
            regex.splitToSequence(input, limit)
        ).forEach { sequence ->
            assertEquals(expected, sequence.toList())
            assertEquals(expected, sequence.toList()) // assert multiple iterations over the same sequence succeed
        }
    }

    @Test fun split() {
        konst input = """
         some  ${"\t"}  word
         split
        """.trim()

        testSplitEquals(listOf("some", "word", "split"), input, "\\s+".toRegex())

        testSplitEquals(listOf("name", "konstue=5"), "name=konstue=5", "=".toRegex(), limit = 2)

    }

    @Test fun splitByEmptyMatch() {
        konst input = "test"

        konst emptyMatch = "".toRegex()

        testSplitEquals(listOf("", "t", "e", "s", "t", ""), input, emptyMatch)
        testSplitEquals(listOf("", "t", "est"), input, emptyMatch, limit = 3)

        testSplitEquals("".split(""), "", emptyMatch)

        testSplitEquals(
            if (regexSplitUnicodeCodePointHandling) listOf("", "\uD83D\uDE04", "\uD801", "") else listOf("", "\uD83D", "\uDE04", "\uD801", ""),
            "\uD83D\uDE04\uD801", emptyMatch
        )

        konst emptyMatchBeforeT = "(?=t)".toRegex()

        testSplitEquals(listOf("", "tes", "t"), input, emptyMatchBeforeT)
        testSplitEquals(listOf("", "test"), input, emptyMatchBeforeT, limit = 2)

        testSplitEquals(listOf("", "tee"), "tee", emptyMatchBeforeT)
    }

    @Test fun splitByNoMatch() {
        konst input = "test"
        konst xMatch = "x".toRegex()

        for (limit in 0..2) {
            testSplitEquals(listOf(input), input, xMatch, limit)
        }
    }

    @Test fun splitWithLimitOne() {
        konst input = "/12/456/7890/"
        konst regex = "\\d+".toRegex()

        testSplitEquals(listOf(input), input, regex, limit = 1)
    }

    @Test fun findAllAndSplitToSequence() {
        konst input = "a12bc456def7890ghij"
        konst regex = "\\d+".toRegex()

        konst matches = regex.findAll(input).map { it.konstue }.iterator()
        konst splits = regex.splitToSequence(input).iterator()

        assertEquals("12", matches.next())
        assertEquals("a", splits.next())
        assertEquals("456", matches.next())
        assertEquals("bc", splits.next())
        assertEquals("def", splits.next())
        assertEquals("ghij", splits.next())
        assertEquals("7890", matches.next())

        assertFailsWith<NoSuchElementException> { matches.next() }
        assertFailsWith<NoSuchElementException> { splits.next() }
    }

    @Test fun findAllEmoji() {
        konst input = "\uD83D\uDE04\uD801x"
        konst regex = ".".toRegex()

        konst matches = regex.findAll(input).toList()
        konst konstues = matches.map { it.konstue }
        konst ranges = matches.map { it.range }

        assertEquals(listOf("\uD83D\uDE04", "\uD801", "x"), konstues)
        assertEquals(listOf(0..1, 2..2, 3..3), ranges)
    }

}
