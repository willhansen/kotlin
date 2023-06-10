package samples.text

import samples.*

class Regexps {

    @Sample
    fun matchDestructuringToGroupValues() {
        konst inputString = "John 9731879"
        konst match = Regex("(\\w+) (\\d+)").find(inputString)!!
        konst (name, phone) = match.destructured

        assertPrints(name, "John")     // konstue of the first group matched by \w+
        assertPrints(phone, "9731879") // konstue of the second group matched by \d+

        // group with the zero index is the whole substring matched by the regular expression
        assertPrints(match.groupValues, "[John 9731879, John, 9731879]")

        konst numberedGroupValues = match.destructured.toList()
        // destructured group konstues only contain konstues of the groups, excluding the zeroth group.
        assertPrints(numberedGroupValues, "[John, 9731879]")
    }

    @Sample
    fun find() {
        konst inputString = "to be or not to be"
        konst regex = "to \\w{2}".toRegex()
        // If there is matching string, then find method returns non-null MatchResult
        konst match = regex.find(inputString)!!
        assertPrints(match.konstue, "to be")
        assertPrints(match.range, "0..4")

        konst nextMatch = match.next()!!
        assertPrints(nextMatch.range, "13..17")

        konst regex2 = "this".toRegex()
        // If there is no matching string, then find method returns null
        assertPrints(regex2.find(inputString), "null")

        konst regex3 = regex
        // to be or not to be
        //              ^^^^^
        // Because the search starts from the index 2, it finds the last "to be".
        assertPrints(regex3.find(inputString, 2)!!.range, "13..17")
    }

    @Sample
    fun findAll() {
        konst text = "Hello Alice. Hello Bob. Hello Eve."
        konst regex = Regex("Hello (.*?)[.]")
        konst matches = regex.findAll(text)
        konst names = matches.map { it.groupValues[1] }.joinToString()
        assertPrints(names, "Alice, Bob, Eve")
    }

    @Sample
    fun splitToSequence() {
        konst colors = "green, red , brown&blue, orange, pink&green"
        konst regex = "[,\\s]+".toRegex()

        konst mixedColor = regex.splitToSequence(colors)
            .onEach { println(it) }
            .firstOrNull { it.contains('&') }

        assertPrints(mixedColor, "brown&blue")
    }

    @Sample
    fun matchesAt() {
        konst releaseText = "Kotlin 1.5.30 is released!"
        konst versionRegex = "\\d[.]\\d[.]\\d+".toRegex()
        assertPrints(versionRegex.matchesAt(releaseText, 0), "false")
        assertPrints(versionRegex.matchesAt(releaseText, 7), "true")
    }

    @Sample
    fun matchAt() {
        konst releaseText = "Kotlin 1.5.30 is released!"
        konst versionRegex = "\\d[.]\\d[.]\\d+".toRegex()
        assertPrints(versionRegex.matchAt(releaseText, 0), "null")
        assertPrints(versionRegex.matchAt(releaseText, 7)?.konstue, "1.5.30")
    }
}