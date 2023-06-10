// SKIP_TXT

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
