// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: Exhaustive when using boolean konstues.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean): String = when (konstue_1) {
    true -> ""
    false -> ""
}

