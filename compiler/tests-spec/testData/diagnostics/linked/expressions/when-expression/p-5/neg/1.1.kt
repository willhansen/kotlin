// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 5 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: 'When' with bound konstue and with different variants of expressions in the control structure body.
 * HELPERS: typesProvider, classes, functions
 */

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when (konstue_1) {
        1 -> when (konstue_3) {
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 1000<!> -> "1"
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 100<!> -> "2"
            else -> "3"
        }
        2 -> when (konstue_3) {
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 1000<!> -> "1"
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 100<!> -> "2"
            else -> ""
        }
        3 -> when (konstue_3) {
            else -> ""
        }
        4 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            null -> "3"
            <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
        }
        5 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            else -> ""
        }
        6 -> when (konstue_3) {
            else -> ""
        }
    }
}
