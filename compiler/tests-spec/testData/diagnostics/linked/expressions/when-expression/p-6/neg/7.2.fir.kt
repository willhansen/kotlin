// SKIP_TXT


// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int): String = when {
    <!ELSE_MISPLACED_IN_WHEN!>else<!> -> ""
    konstue_1 == 1 -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int): String = when {
    konstue_1 == 1 -> ""
    <!ELSE_MISPLACED_IN_WHEN!>else<!> -> ""
    konstue_1 == 2 -> ""
}

// TESTCASE NUMBER: 3
fun case_3(): String {
    when {
        <!ELSE_MISPLACED_IN_WHEN!>else<!> -> return ""
        else -> return ""
    }

    return ""
}
