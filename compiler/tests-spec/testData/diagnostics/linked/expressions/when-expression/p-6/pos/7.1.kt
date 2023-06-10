// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 7
 * NUMBER: 1
 * DESCRIPTION: 'When' with bound konstue and else branch.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) = when (konstue_1) {
    0 -> ""
    1 -> ""
    2 -> ""
    else -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any) = when (konstue_1) {
    is Int -> ""
    is Boolean -> ""
    is String -> ""
    else -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int) = when (konstue_1) {
    in -10..10 -> ""
    in 11..1000 -> ""
    in 1000..Int.MAX_VALUE -> ""
    else -> ""
}