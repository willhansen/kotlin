// FIR_IDENTICAL
// LANGUAGE: +WarnAboutNonExhaustiveWhenOnAlgebraicTypes
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 5
 * NUMBER: 2
 * DESCRIPTION: 'When' with different variants of the arithmetic expressions (additive expression and multiplicative expression) in 'when condition'.
 * HELPERS: typesProvider, classes, functions
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean, konstue_2: Boolean, konstue_3: Long) {
    when (konstue_1) {
        konstue_2, !konstue_2, <!CONFUSING_BRANCH_CONDITION_ERROR!>getBoolean() && konstue_2<!>, <!CONFUSING_BRANCH_CONDITION_ERROR!>getChar() != 'a'<!> -> {}
            <!CONFUSING_BRANCH_CONDITION_ERROR!>getList() === getAny()<!>, <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_3 <= 11<!> -> {}
        else -> {}
    }
}

