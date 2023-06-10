// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Exhaustive when, with bound konstue, with else branch.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int): String = when (konstue_1) {
    0 -> ""
    1 -> ""
    2 -> ""
    3 -> ""
    else -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean): String = when (konstue_1) {
    true -> ""
    else -> ""
}

/*
 * TESTCASE NUMBER: 3
 * NOTE: for a potential bound konstue constant analysis.
 */
fun case_3(): String = when (true) {
    true -> ""
    else -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int): String = when(konstue_1) {
    else -> ""
}