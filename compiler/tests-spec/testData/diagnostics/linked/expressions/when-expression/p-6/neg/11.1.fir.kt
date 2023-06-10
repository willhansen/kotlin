// !DIAGNOSTICS: -UNUSED_VALUE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: List<Int>): String {
    when (konstue_1) {
        <!EXPRESSION_REQUIRED!>while (false) {}<!> -> return ""
        <!EXPRESSION_REQUIRED!>do {} while (false)<!> -> return ""
        for (konstue in konstue_2) {} -> return ""
    }

    return ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int): String {
    var konstue_2: Int
    var konstue_3 = 10

    when (konstue_1) {
        <!EXPRESSION_REQUIRED!>konstue_2 = 10<!> -> return ""
        <!EXPRESSION_REQUIRED!>konstue_3 %= 10<!> -> return ""
    }

    return ""
}
