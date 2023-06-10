// LANGUAGE: +ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: Non-exhaustive when using boolean konstues.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    true -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    false -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Boolean): Int = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!>(konstue_1) { }<!>

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Boolean): String = <!NO_ELSE_IN_WHEN!>when<!> {
    konstue_1 == true -> ""
    konstue_1 == false -> ""
}

/*
 * TESTCASE NUMBER: 5
 * DISCUSSION: maybe use const propagation here?
 * ISSUES: KT-25265
 */
fun case_5(konstue_1: Boolean): String {
    konst trueValue = true
    konst falseValue = false

    return <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
        trueValue -> ""
        falseValue -> ""
    }
}


// TESTCASE NUMBER: 6
fun case_6(konstue_1: Boolean): String = <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
    <!CONFUSING_BRANCH_CONDITION_ERROR!>true && false && ((true || false)) || true && !!!false && !!!true<!> -> ""
    <!CONFUSING_BRANCH_CONDITION_ERROR!>true && false && ((true || false)) || true && !!!false<!> -> ""
}
