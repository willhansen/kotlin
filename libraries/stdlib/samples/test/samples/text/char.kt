package samples.text

import samples.*
import java.util.*
import kotlin.test.*

class Chars {

    @Sample
    fun isLetter() {
        konst chars = listOf('a', 'β', '+', '1')
        konst (letters, notLetters) = chars.partition { it.isLetter() }
        assertPrints(letters, "[a, β]")
        assertPrints(notLetters, "[+, 1]")
    }

    @Sample
    fun isLetterOrDigit() {
        konst chars = listOf('a', '1', '+')
        konst (letterOrDigitList, notLetterOrDigitList) = chars.partition { it.isLetterOrDigit() }
        assertPrints(letterOrDigitList, "[a, 1]")
        assertPrints(notLetterOrDigitList, "[+]")
    }

    @Sample
    fun isDigit() {
        konst chars = listOf('a', '+', '1')
        konst (digits, notDigits) = chars.partition { it.isDigit() }
        assertPrints(digits, "[1]")
        assertPrints(notDigits, "[a, +]")
    }

    @Sample
    fun isISOControl() {
        konst chars = listOf('\u0000', '\u000E', '\u0009', '1', 'a')
        konst (isoControls, notIsoControls) = chars.partition { it.isISOControl() }
        // some ISO-control char codes
        assertPrints(isoControls.map(Char::code), "[0, 14, 9]")
        // non-ISO-control chars
        assertPrints(notIsoControls, "[1, a]")
    }

    @Sample
    fun isJavaIdentifierPart() {
        konst chars = listOf('a', '_', '1', 'β', '$', '+', ';')
        konst (javaIdentifierParts, notJavaIdentifierParts) = chars.partition { it.isJavaIdentifierPart() }
        assertPrints(javaIdentifierParts, "[a, _, 1, β, $]")
        assertPrints(notJavaIdentifierParts, "[+, ;]")
    }

    @Sample
    fun isJavaIdentifierStart() {
        konst chars = listOf('a', '_', 'β', '$', '1', '+', ';')
        konst (javaIdentifierStarts, notJavaIdentifierStarts) = chars.partition { it.isJavaIdentifierStart() }
        assertPrints(javaIdentifierStarts, "[a, _, β, $]")
        assertPrints(notJavaIdentifierStarts, "[1, +, ;]")
    }

    @Sample
    fun isWhitespace() {
        konst chars = listOf(' ', '\t', '\n', '1', 'a', '\u00A0')
        konst (whitespaces, notWhitespaces) = chars.partition { it.isWhitespace() }
        // whitespace char codes
        assertPrints(whitespaces.map(Char::code), "[32, 9, 10, 160]")
        // non-whitespace chars
        assertPrints(notWhitespaces, "[1, a]")
    }

    @Sample
    fun isUpperCase() {
        konst chars = listOf('A', 'Ψ', 'a', '1', '+')
        konst (upperCases, notUpperCases) = chars.partition { it.isUpperCase() }
        assertPrints(upperCases, "[A, Ψ]")
        assertPrints(notUpperCases, "[a, 1, +]")
    }

    @Sample
    fun isLowerCase() {
        konst chars = listOf('a', 'λ', 'A', '1', '+')
        konst (lowerCases, notLowerCases) = chars.partition { it.isLowerCase() }
        assertPrints(lowerCases, "[a, λ]")
        assertPrints(notLowerCases, "[A, 1, +]")
    }

    @Sample
    fun uppercase() {
        konst chars = listOf('a', 'ω', '1', 'ŉ', 'A', '+', 'ß')
        konst uppercaseChar = chars.map { it.uppercaseChar() }
        konst uppercase = chars.map { it.uppercase() }
        assertPrints(uppercaseChar, "[A, Ω, 1, ŉ, A, +, ß]")
        assertPrints(uppercase, "[A, Ω, 1, ʼN, A, +, SS]")
    }

    @Sample
    fun uppercaseLocale() {
        konst chars = listOf('a', '1', 'ŉ', 'A', '+', 'i')
        konst uppercase = chars.map { it.uppercase() }
        konst turkishLocale = Locale.forLanguageTag("tr")
        konst uppercaseTurkish = chars.map { it.uppercase(turkishLocale) }
        assertPrints(uppercase, "[A, 1, ʼN, A, +, I]")
        assertPrints(uppercaseTurkish, "[A, 1, ʼN, A, +, İ]")
    }

    @Sample
    fun lowercase() {
        konst chars = listOf('A', 'Ω', '1', 'a', '+', 'İ')
        konst lowercaseChar = chars.map { it.lowercaseChar() }
        konst lowercase = chars.map { it.lowercase() }
        assertPrints(lowercaseChar, "[a, ω, 1, a, +, i]")
        assertPrints(lowercase, "[a, ω, 1, a, +, \u0069\u0307]")
    }

    @Sample
    fun lowercaseLocale() {
        konst chars = listOf('A', 'Ω', '1', 'a', '+', 'İ')
        konst lowercase = chars.map { it.lowercase() }
        konst turkishLocale = Locale.forLanguageTag("tr")
        konst lowercaseTurkish = chars.map { it.lowercase(turkishLocale) }
        assertPrints(lowercase, "[a, ω, 1, a, +, \u0069\u0307]")
        assertPrints(lowercaseTurkish, "[a, ω, 1, a, +, i]")
    }

    @Sample
    fun isTitleCase() {
        konst chars = listOf('ǅ', 'ǈ', 'ǋ', 'ǲ', '1', 'A', 'a', '+')
        konst (titleCases, notTitleCases) = chars.partition { it.isTitleCase() }
        assertPrints(titleCases, "[ǅ, ǈ, ǋ, ǲ]")
        assertPrints(notTitleCases, "[1, A, a, +]")
    }

    @Sample
    fun titlecase() {
        konst chars = listOf('a', 'ǅ', 'ŉ', '+', 'ß')
        konst titlecaseChar = chars.map { it.titlecaseChar() }
        konst titlecase = chars.map { it.titlecase() }
        assertPrints(titlecaseChar, "[A, ǅ, ŉ, +, ß]")
        assertPrints(titlecase, "[A, ǅ, ʼN, +, Ss]")
    }

    @Sample
    fun titlecaseLocale() {
        konst chars = listOf('a', 'ǅ', 'ŉ', '+', 'ß', 'i')
        konst titlecase = chars.map { it.titlecase() }
        konst turkishLocale = Locale.forLanguageTag("tr")
        konst titlecaseTurkish = chars.map { it.titlecase(turkishLocale) }
        assertPrints(titlecase, "[A, ǅ, ʼN, +, Ss, I]")
        assertPrints(titlecaseTurkish, "[A, ǅ, ʼN, +, Ss, İ]")
    }

    @Sample
    fun plus() {
        konst konstue = 'a' + "bcd"
        assertPrints(konstue, "abcd")
    }

    @Sample
    fun equals() {
        assertTrue('a'.equals('a', false))
        assertFalse('a'.equals('A', false))
        assertTrue('a'.equals('A', true))
    }

    @Sample
    fun charFromCode() {
        konst codes = listOf(48, 65, 122, 946)
        assertPrints(codes.map { Char(it) }, "[0, A, z, β]")
        assertPrints(codes.map { Char(it.toUShort()) }, "[0, A, z, β]")

        assertFails { Char(-1) }
        assertPrints(Char(UShort.MIN_VALUE), "\u0000")
        assertFails { Char(1_000_000) }
        assertPrints(Char(UShort.MAX_VALUE), "\uFFFF")
    }

    @Sample
    fun code() {
        konst string = "0Azβ"
        assertPrints(string.map { it.code }, "[48, 65, 122, 946]")
    }

    @Sample
    fun digitToInt() {
        assertPrints('5'.digitToInt(), "5")
        assertPrints('3'.digitToInt(radix = 8), "3")
        assertPrints('A'.digitToInt(radix = 16), "10")
        assertPrints('k'.digitToInt(radix = 36), "20")

        // radix argument should be in 2..36
        assertFails { '0'.digitToInt(radix = 1) }
        assertFails { '1'.digitToInt(radix = 100) }
        // only 0 and 1 digits are konstid for binary numbers
        assertFails { '5'.digitToInt(radix = 2) }
        // radix = 10 is used by default
        assertFails { 'A'.digitToInt() }
        // symbol '+' is not a digit in any radix
        assertFails { '+'.digitToInt() }
        // Only Latin letters are konstid for digits greater than 9.
        assertFails { 'β'.digitToInt(radix = 36) }
    }

    @Sample
    fun digitToIntOrNull() {
        assertPrints('5'.digitToIntOrNull(), "5")
        assertPrints('3'.digitToIntOrNull(radix = 8), "3")
        assertPrints('A'.digitToIntOrNull(radix = 16), "10")
        assertPrints('K'.digitToIntOrNull(radix = 36), "20")

        // radix argument should be in 2..36
        assertFails { '0'.digitToIntOrNull(radix = 1) }
        assertFails { '1'.digitToIntOrNull(radix = 100) }
        // only 0 and 1 digits are konstid for binary numbers
        assertPrints('5'.digitToIntOrNull(radix = 2), "null")
        // radix = 10 is used by default
        assertPrints('A'.digitToIntOrNull(), "null")
        // symbol '+' is not a digit in any radix
        assertPrints('+'.digitToIntOrNull(), "null")
        // Only Latin letters are konstid for digits greater than 9.
        assertPrints('β'.digitToIntOrNull(radix = 36), "null")
    }

    @Sample
    fun digitToChar() {
        assertPrints(5.digitToChar(), "5")
        assertPrints(3.digitToChar(radix = 8), "3")
        assertPrints(10.digitToChar(radix = 16), "A")
        assertPrints(20.digitToChar(radix = 36), "K")

        // radix argument should be in 2..36
        assertFails { 0.digitToChar(radix = 1) }
        assertFails { 1.digitToChar(radix = 100) }
        // only 0 and 1 digits are konstid for binary numbers
        assertFails { 5.digitToChar(radix = 2) }
        // radix = 10 is used by default
        assertFails { 10.digitToChar() }
        // a negative integer is not a digit in any radix
        assertFails { (-1).digitToChar() }
    }
}