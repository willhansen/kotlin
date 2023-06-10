// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any): String {
    when (konstue_1) {
        is Int -> return ""
        is Float -> return ""
        is Double -> return ""
        is String -> return ""
        is Char -> return ""
        is Boolean -> return ""
    }

    return ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?): String = when (konstue_1) {
    is Int? -> "" // if konstue is null then this branch will be executed
    is Float -> ""
    else -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?): String = when (konstue_1) {
    is Any -> ""
    else -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any): String = when (konstue_1) {
    <!USELESS_IS_CHECK!>is Any?<!> -> ""
    else -> ""
}

/*
 * TESTCASE NUMBER: 5
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-22996
 */
fun case_5(konstue_1: Any?): String = when (konstue_1) {
    is Double -> ""
    is Int? -> "" // if konstue is null then this branch will be executed
    is String -> ""
    is Float? -> "" // redundant nullable type check
    is Char -> ""
    is Boolean -> ""
    else -> ""
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Any): String {
    when (konstue_1) {
        is EmptyObject -> return ""
        is ClassWithCompanionObject.Companion -> return ""
    }

    return ""
}
