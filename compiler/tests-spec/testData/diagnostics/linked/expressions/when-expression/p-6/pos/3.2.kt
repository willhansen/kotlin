// FIR_IDENTICAL
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 3
 * NUMBER: 2
 * DESCRIPTION: 'When' with bound konstue and enumeration of the containment operators.
 * HELPERS: typesProvider, classes
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: Int, konstue_3: Short): String {
    when (konstue_1) {
        in Long.MIN_VALUE..-100, in -99..0 -> return ""
        !in 100.toByte()..konstue_2, in konstue_2..konstue_3 -> return ""
    }

    return ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int, konstue_2: List<IntArray>, konstue_3: Class) = when (konstue_1) {
    !in konstue_2[0], !in listOf(0, 1, 2, 3, 4), !in konstue_3.getIntArray() -> ""
    else -> ""
}