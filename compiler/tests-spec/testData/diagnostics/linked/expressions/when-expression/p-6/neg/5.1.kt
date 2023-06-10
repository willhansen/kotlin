// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 5
 * NUMBER: 1
 * DESCRIPTION: 'When' with enumeration of the different variants of expressions in 'when condition'.
 * HELPERS: typesProvider, classes, functions
 */

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Boolean, konstue_2: Boolean, konstue_3: Long) {
    <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
        konstue_2 -> {}
        !konstue_2 -> {}
        <!CONFUSING_BRANCH_CONDITION_ERROR!>getBoolean() && konstue_2<!> -> {}
        <!CONFUSING_BRANCH_CONDITION_ERROR!>getChar() != 'a'<!> -> {}
        <!CONFUSING_BRANCH_CONDITION_ERROR!>getList() === getAny()<!> -> {}
        <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_3 <= 11<!> -> {}
    }
}
