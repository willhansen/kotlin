// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: 'When' with bound konstue and type test condition (without companion object in classes), but without type checking operator.
 * HELPERS: classes
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any): String {
    when (konstue_1) {
        <!NO_COMPANION_OBJECT!>EmptyClass<!> -> return ""
    }

    return ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any): String {
    when (konstue_1) {
        <!NO_COMPANION_OBJECT!>Any<!> -> return ""
    }

    return ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any): String {
    when (konstue_1) {
        <!NO_COMPANION_OBJECT!>Nothing<!> -> return ""
    }

    return ""
}
