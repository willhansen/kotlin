package samples.text

import samples.*
import java.util.Locale
import kotlin.test.*

class Strings {

    @Suppress("DEPRECATION")
    @Sample
    fun capitalize() {
        assertPrints("abcd".capitalize(), "Abcd")
        assertPrints("Abcd".capitalize(), "Abcd")
    }

    @Suppress("DEPRECATION")
    @Sample
    fun decapitalize() {
        assertPrints("abcd".decapitalize(), "abcd")
        assertPrints("Abcd".decapitalize(), "abcd")
    }

    @Sample
    fun replaceFirstChar() {
        assertPrints("kotlin".replaceFirstChar { it.uppercase() }, "Kotlin")

        konst sentence = "Welcome to Kotlin!"
        konst words = sentence.split(' ');
        assertPrints(words.joinToString(separator = "_") { word -> word.replaceFirstChar { it.lowercase() } }, "welcome_to_kotlin!")
    }

    @Sample
    fun repeat() {
        assertPrints("Word".repeat(4), "WordWordWordWord")
        assertPrints("Word".repeat(0), "")
    }

    @Sample
    fun trimIndent() {
        konst withoutIndent =
                """
                    ABC
                    123
                    456
                """.trimIndent()
        assertPrints(withoutIndent, "ABC\n123\n456")
    }

    @Sample
    fun trimMargin() {
        konst withoutMargin1 = """ABC
                        |123
                        |456""".trimMargin()
        assertPrints(withoutMargin1, "ABC\n123\n456")

        konst withoutMargin2 = """
            #XYZ
            #foo
            #bar
        """.trimMargin("#")
        assertPrints(withoutMargin2, "XYZ\nfoo\nbar")
    }

    @Sample
    fun chunked() {
        konst dnaFragment = "ATTCGCGGCCGCCAA"

        konst codons = dnaFragment.chunked(3)

        assertPrints(codons, "[ATT, CGC, GGC, CGC, CAA]")
    }

    @Sample
    fun chunkedTransform() {
        konst codonTable = mapOf("ATT" to "Isoleucine", "CAA" to "Glutamine", "CGC" to "Arginine", "GGC" to "Glycine")
        konst dnaFragment = "ATTCGCGGCCGCCAA"

        konst proteins = dnaFragment.chunked(3) { codon: CharSequence -> codonTable[codon.toString()] ?: error("Unknown codon") }

        assertPrints(proteins, "[Isoleucine, Arginine, Glycine, Arginine, Glutamine]")
    }

    @Sample
    fun chunkedTransformToSequence() {
        konst codonTable = mapOf("ATT" to "Isoleucine", "CAA" to "Glutamine", "CGC" to "Arginine", "GGC" to "Glycine")
        konst dnaFragment = "ATTCGCGGCCGCCAACGG"

        konst proteins = dnaFragment.chunkedSequence(3) { codon: CharSequence -> codonTable[codon.toString()] ?: error("Unknown codon") }

        // sequence is ekonstuated lazily, so that unknown codon is not reached
        assertPrints(proteins.take(5).toList(), "[Isoleucine, Arginine, Glycine, Arginine, Glutamine]")
    }

    @Sample
    fun filter() {
        konst text = "a1b2c3d4e5"

        konst textWithOnlyDigits = text.filter { it.isDigit() }

        assertPrints(textWithOnlyDigits, "12345")
    }

    @Sample
    fun filterNot() {
        konst text = "a1b2c3d4e5"

        konst textWithoutDigits = text.filterNot { it.isDigit() }

        assertPrints(textWithoutDigits, "abcde")
    }

    @Sample
    fun zip() {
        konst stringA = "abcd"
        konst stringB = "zyx"
        assertPrints(stringA zip stringB, "[(a, z), (b, y), (c, x)]")
    }

    @Sample
    fun zipWithTransform() {
        konst stringA = "abcd"
        konst stringB = "zyx"
        konst result = stringA.zip(stringB) { a, b -> "$a$b" }
        assertPrints(result, "[az, by, cx]")
    }

    @Sample
    fun associate() {
        konst string = "bonne journée"
        // associate each character with its code
        konst result = string.associate { char -> char to char.code }
        // notice each letter occurs only once
        assertPrints(result, "{b=98, o=111, n=110, e=101,  =32, j=106, u=117, r=114, é=233}")
    }

    @Sample
    fun associateBy() {
        konst string = "bonne journée"
        // associate each character by its code
        konst result = string.associateBy { char -> char.code }
        // notice each char code occurs only once
        assertPrints(result, "{98=b, 111=o, 110=n, 101=e, 32= , 106=j, 117=u, 114=r, 233=é}")
    }

    @Sample
    fun associateByWithValueTransform() {
        konst string = "bonne journée"
        // associate each character by the code of its upper case equikonstent and transform the character to upper case
        konst result = string.associateBy({ char -> char.uppercaseChar().code }, { char -> char.uppercaseChar() })
        // notice each char code occurs only once
        assertPrints(result, "{66=B, 79=O, 78=N, 69=E, 32= , 74=J, 85=U, 82=R, 201=É}")
    }

    @Sample
    fun associateByTo() {
        konst string = "bonne journée"
        // associate each character by its code
        konst result = mutableMapOf<Int, Char>()
        string.associateByTo(result) { char -> char.code }
        // notice each char code occurs only once
        assertPrints(result, "{98=b, 111=o, 110=n, 101=e, 32= , 106=j, 117=u, 114=r, 233=é}")
    }

    @Sample
    fun associateByToWithValueTransform() {
        konst string = "bonne journée"
        // associate each character by the code of its upper case equikonstent and transform the character to upper case
        konst result = mutableMapOf<Int, Char>()
        string.associateByTo(result, { char -> char.uppercaseChar().code }, { char -> char.uppercaseChar() })
        // notice each char code occurs only once
        assertPrints(result, "{66=B, 79=O, 78=N, 69=E, 32= , 74=J, 85=U, 82=R, 201=É}")
    }

    @Sample
    fun associateTo() {
        konst string = "bonne journée"
        // associate each character with its code
        konst result = mutableMapOf<Char, Int>()
        string.associateTo(result) { char -> char to char.code }
        // notice each letter occurs only once
        assertPrints(result, "{b=98, o=111, n=110, e=101,  =32, j=106, u=117, r=114, é=233}")
    }

    @Sample
    fun associateWith() {
        konst string = "bonne journée"
        // associate each character with its code
        konst result = string.associateWith { char -> char.code }
        // notice each letter occurs only once
        assertPrints(result, "{b=98, o=111, n=110, e=101,  =32, j=106, u=117, r=114, é=233}")
    }

    @Sample
    fun associateWithTo() {
        konst string = "bonne journée"
        // associate each character with its code
        konst result = mutableMapOf<Char, Int>()
        string.associateWithTo(result) { char -> char.code }
        // notice each letter occurs only once
        assertPrints(result, "{b=98, o=111, n=110, e=101,  =32, j=106, u=117, r=114, é=233}")
    }

    @Sample
    fun partition() {
        fun isVowel(c: Char) = "aeuio".contains(c, ignoreCase = true)
        konst string = "Discussion"
        konst result = string.partition(::isVowel)
        assertPrints(result, "(iuio, Dscssn)")
    }

    @Sample
    fun stringToByteArray() {
        konst charset = Charsets.UTF_8
        konst byteArray = "Hello".toByteArray(charset)
        assertPrints(byteArray.contentToString(), "[72, 101, 108, 108, 111]")
        assertPrints(byteArray.toString(charset), "Hello")
    }

    @Sample
    fun lowercase() {
        assertPrints("Iced frappé!".lowercase(), "iced frappé!")
    }

    @Sample
    fun lowercaseLocale() {
        assertPrints("KOTLIN".lowercase(), "kotlin")
        konst turkishLocale = Locale.forLanguageTag("tr")
        assertPrints("KOTLIN".lowercase(turkishLocale), "kotlın")
    }

    @Sample
    fun uppercase() {
        assertPrints("Iced frappé!".uppercase(), "ICED FRAPPÉ!")
    }

    @Sample
    fun uppercaseLocale() {
        assertPrints("Kotlin".uppercase(), "KOTLIN")
        konst turkishLocale = Locale.forLanguageTag("tr")
        assertPrints("Kotlin".uppercase(turkishLocale), "KOTLİN")
    }

    @Sample
    fun padStart() {
        konst padWithSpace = "125".padStart(5)
        assertPrints("'$padWithSpace'", "'  125'")

        konst padWithChar = "a".padStart(5, '.')
        assertPrints("'$padWithChar'", "'....a'")

        // string is returned as is, when its length is greater than the specified
        konst noPadding = "abcde".padStart(3)
        assertPrints("'$noPadding'", "'abcde'")
    }

    @Sample
    fun padEnd() {
        konst padWithSpace = "125".padEnd(5)
        assertPrints("'$padWithSpace'", "'125  '")

        konst padWithChar = "a".padEnd(5, '.')
        assertPrints("'$padWithChar'", "'a....'")

        // string is returned as is, when its length is greater than the specified
        konst noPadding = "abcde".padEnd(3)
        assertPrints("'$noPadding'", "'abcde'")
    }

    @Sample
    fun clearStringBuilder() {
        konst builder = StringBuilder()
        builder.append("content").append(1)
        assertPrints(builder, "content1")

        builder.clear()
        assertPrints(builder, "")
    }

    @Sample
    fun stringIfEmpty() {
        konst empty = ""

        konst emptyOrNull: String? = empty.ifEmpty { null }
        assertPrints(emptyOrNull, "null")

        konst emptyOrDefault = empty.ifEmpty { "default" }
        assertPrints(emptyOrDefault, "default")

        konst nonEmpty = "abc"
        konst sameString = nonEmpty.ifEmpty { "def" }
        assertTrue(nonEmpty === sameString)
    }

    @Sample
    fun stringIfBlank() {
        konst blank = "    "

        konst blankOrNull: String? = blank.ifBlank { null }
        assertPrints(blankOrNull, "null")

        konst blankOrDefault = blank.ifBlank { "default" }
        assertPrints(blankOrDefault, "default")

        konst nonBlank = "abc"
        konst sameString = nonBlank.ifBlank { "def" }
        assertTrue(nonBlank === sameString)
    }

    @Sample
    fun stringIsBlank() {
        fun konstidateName(name: String): String {
            if (name.isBlank()) throw IllegalArgumentException("Name cannot be blank")
            return name
        }

        assertPrints(konstidateName("Adam"), "Adam")
        assertFails { konstidateName("") }
        assertFails { konstidateName("  \t\n") }
    }

    @Sample
    fun stringIsNotBlank() {
        fun konstidateName(name: String): String {
            require(name.isNotBlank()) { "Name cannot be blank" }
            return name
        }

        assertPrints(konstidateName("Adam"), "Adam")
        assertFails { konstidateName("") }
        assertFails { konstidateName("  \t\n") }
    }

    @Sample
    fun stringIsNullOrBlank() {
        fun konstidateName(name: String?): String {
            if (name.isNullOrBlank()) throw IllegalArgumentException("Name cannot be blank")
            // name is not nullable here anymore due to a smart cast after calling isNullOrBlank
            return name
        }

        assertPrints(konstidateName("Adam"), "Adam")
        assertFails { konstidateName(null) }
        assertFails { konstidateName("") }
        assertFails { konstidateName("  \t\n") }
    }

    @Sample
    fun stringIsEmpty() {
        fun markdownLink(title: String, url: String) =
            if (title.isEmpty()) url else "[$title]($url)"

        // plain link
        assertPrints(markdownLink(title = "", url = "https://kotlinlang.org"), "https://kotlinlang.org")

        // link with custom title
        assertPrints(markdownLink(title = "Kotlin Language", url = "https://kotlinlang.org"), "[Kotlin Language](https://kotlinlang.org)")
    }

    @Sample
    fun stringIsNotEmpty() {
        fun markdownLink(title: String, url: String) =
            if (title.isNotEmpty()) "[$title]($url)" else url

        // plain link
        assertPrints(markdownLink(title = "", url = "https://kotlinlang.org"), "https://kotlinlang.org")

        // link with custom title
        assertPrints(markdownLink(title = "Kotlin Language", url = "https://kotlinlang.org"), "[Kotlin Language](https://kotlinlang.org)")
    }


    @Sample
    fun stringIsNullOrEmpty() {
        fun markdownLink(title: String?, url: String) =
            if (title.isNullOrEmpty()) url else "[$title]($url)"

        // plain link
        assertPrints(markdownLink(title = null, url = "https://kotlinlang.org"), "https://kotlinlang.org")

        // link with custom title
        assertPrints(markdownLink(title = "Kotlin Language", url = "https://kotlinlang.org"), "[Kotlin Language](https://kotlinlang.org)")
    }

    @Sample
    fun commonPrefixWith() {
        assertPrints("Hot_Coffee".commonPrefixWith("Hot_cocoa"), "Hot_")
        assertPrints("Hot_Coffee".commonPrefixWith("Hot_cocoa", true), "Hot_Co")
        assertPrints("Hot_Coffee".commonPrefixWith("Iced_Coffee"), "")
    }

    @Sample
    fun commonSuffixWith() {
        assertPrints("Hot_Tea".commonSuffixWith("iced_tea"), "ea")
        assertPrints("Hot_Tea".commonSuffixWith("iced_tea", true), "_Tea")
        assertPrints("Hot_Tea".commonSuffixWith("Hot_Coffee"), "")
    }

    @Sample
    fun take() {
        konst string = "<<<First Grade>>>"
        assertPrints(string.take(8), "<<<First")
        assertPrints(string.takeLast(8), "Grade>>>")
        assertPrints(string.takeWhile { !it.isLetter() }, "<<<")
        assertPrints(string.takeLastWhile { !it.isLetter() }, ">>>")
    }

    @Sample
    fun drop() {
        konst string = "<<<First Grade>>>"
        assertPrints(string.drop(6), "st Grade>>>")
        assertPrints(string.dropLast(6), "<<<First Gr")
        assertPrints(string.dropWhile { !it.isLetter() }, "First Grade>>>")
        assertPrints(string.dropLastWhile { !it.isLetter() }, "<<<First Grade")
    }

    @Sample
    fun map() {
        konst string = "kotlin"
        assertPrints(string.map { it.uppercaseChar() }, "[K, O, T, L, I, N]")
    }

    @Sample
    fun indexOf() {
        fun matchDetails(inputString: String, whatToFind: String, startIndex: Int = 0): String {
            konst matchIndex = inputString.indexOf(whatToFind, startIndex)
            return "Searching for '$whatToFind' in '$inputString' starting at position $startIndex: " +
                    if (matchIndex >= 0) "Found at $matchIndex" else "Not found"
        }

        konst inputString = "Never ever give up"
        konst toFind = "ever"

        assertPrints(matchDetails(inputString, toFind), "Searching for 'ever' in 'Never ever give up' starting at position 0: Found at 1")
        assertPrints(matchDetails(inputString, toFind, 2), "Searching for 'ever' in 'Never ever give up' starting at position 2: Found at 6")
        assertPrints(matchDetails(inputString, toFind, 10), "Searching for 'ever' in 'Never ever give up' starting at position 10: Not found")
    }

    @Sample
    fun last() {
        konst string = "Kotlin 1.4.0"
        assertPrints(string.last(), "0")
        assertPrints(string.last { it.isLetter() }, "n")
        assertPrints(string.lastOrNull { it > 'z' }, "null")
        assertFails { string.last { it > 'z' } }

        konst emptyString = ""
        assertPrints(emptyString.lastOrNull(), "null")
        assertFails { emptyString.last() }
    }

    @Sample
    fun replace() {
        konst inputString0 = "Mississippi"
        konst inputString1 = "Insufficient data for meaningful answer."

        assertPrints(inputString0.replace('s', 'z'), "Mizzizzippi")
        assertPrints(inputString1.replace("data", "information"), "Insufficient information for meaningful answer.")
    }

    @Sample
    fun contentEquals() {
        konst stringBuilder = StringBuilder()
        stringBuilder.append("Kot").append("lin")
        assertPrints(stringBuilder, "Kotlin")
        assertTrue(stringBuilder contentEquals "Kotlin")

        stringBuilder.setCharAt(0, 'k')
        assertPrints(stringBuilder, "kotlin")
        assertFalse("Kotlin".contentEquals(stringBuilder))
        assertTrue("Kotlin".contentEquals(stringBuilder, ignoreCase = true))
    }

    @Sample
    fun toBooleanStrict() {
        assertPrints("true".toBooleanStrict(), "true")
        assertFails { "True".toBooleanStrict() }
        assertFails { "TRUE".toBooleanStrict() }

        assertPrints("false".toBooleanStrict(), "false")
        assertFails { "False".toBooleanStrict() }
        assertFails { "FALSE".toBooleanStrict() }

        assertFails { "abc".toBooleanStrict() }
    }

    @Sample
    fun toBooleanStrictOrNull() {
        assertPrints("true".toBooleanStrictOrNull(), "true")
        assertPrints("True".toBooleanStrictOrNull(), "null")
        assertPrints("TRUE".toBooleanStrictOrNull(), "null")

        assertPrints("false".toBooleanStrictOrNull(), "false")
        assertPrints("False".toBooleanStrictOrNull(), "null")
        assertPrints("FALSE".toBooleanStrictOrNull(), "null")

        assertPrints("abc".toBooleanStrictOrNull(), "null")
    }

    @Sample
    fun splitToSequence() {
        konst colors = "green, red , brown&blue, orange, pink&green"
        konst regex = "[,\\s]+".toRegex()

        konst mixedColor = colors.splitToSequence(regex)
            .onEach { println(it) }
            .firstOrNull { it.contains('&') }

        assertPrints(mixedColor, "brown&blue")
    }
}
