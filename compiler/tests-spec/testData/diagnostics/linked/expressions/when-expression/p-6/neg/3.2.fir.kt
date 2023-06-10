// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: EmptyClass, konstue_3: Int, konstue_4: Any): String {
    when (konstue_1) {
        <!NONE_APPLICABLE!>in<!> konstue_2  -> return ""
        <!NONE_APPLICABLE!>in<!> konstue_3  -> return ""
        <!NONE_APPLICABLE!>in<!> konstue_4  -> return ""
    }

    return ""
}

/*
 * TESTCASE NUMBER: 2
 * DISCUSSION
 * ISSUES: KT-25948
 */
fun case_2(konstue_1: Int, konstue_3: Nothing) {
    when (konstue_1) {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>in<!> konstue_3 -> {}
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>in<!> throw Exception() -> {}
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>in<!> return -> {}
    }
}
