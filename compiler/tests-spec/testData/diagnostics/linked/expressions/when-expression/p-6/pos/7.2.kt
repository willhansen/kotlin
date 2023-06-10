// FIR_IDENTICAL
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 7
 * NUMBER: 2
 * DESCRIPTION: 'When' without bound konstue and with else branch in the last position.
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int): String {
    when {
        konstue_1 == 1 -> return ""
        konstue_1 == 2 -> return ""
        else -> return ""
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int): String = when {
    konstue_1 == 1 -> ""
    konstue_1 == 2 -> ""
    else -> ""
}
