// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any, konstue_2: Int): String {
    when (konstue_1) {
        is <!UNRESOLVED_REFERENCE!>konstue_2<!> -> return ""
    }

    return ""
}
