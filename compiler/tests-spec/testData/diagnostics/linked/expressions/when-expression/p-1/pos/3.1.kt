// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression -> paragraph 1 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: Empty when with bound konstue.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int) {
    when (<!UNUSED_EXPRESSION!>konstue_1<!>) {}
}
