// LANGUAGE: +ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 11
 * NUMBER: 1
 * DESCRIPTION: Non-exhaustive when using nullable boolean konstues.
 */


// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    true -> ""
    false -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    true -> ""
    null -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Boolean?): Int = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!>(konstue_1) { }<!>

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Boolean?): String = <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
    <!CONFUSING_BRANCH_CONDITION_ERROR!>true && false && ((true || false)) || true && !!!false && !!!true<!> -> ""
    <!CONFUSING_BRANCH_CONDITION_ERROR!>true && false && ((true || false)) || true && !!!false<!> -> ""
    null -> ""
}
