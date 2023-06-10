// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: 'When' with bound konstue and type test condition on the non-type operand of the type checking operator.
 * HELPERS: classes
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any, <!UNUSED_PARAMETER!>konstue_2<!>: Int): String {
    when (konstue_1) {
        is <!UNRESOLVED_REFERENCE!>konstue_2<!> -> return ""
    }

    return ""
}
