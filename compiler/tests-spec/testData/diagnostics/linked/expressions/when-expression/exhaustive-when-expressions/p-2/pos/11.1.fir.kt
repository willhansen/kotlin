// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 11
 * NUMBER: 1
 * DESCRIPTION: Exhaustive when using nullable boolean konstues.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean?): String = when (konstue_1) {
    true -> ""
    false -> ""
    null -> ""
}

