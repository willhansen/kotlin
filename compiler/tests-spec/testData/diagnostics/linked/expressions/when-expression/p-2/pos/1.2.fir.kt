// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int): String {
    while (true) {
        when {
            konstue_1 == 1 -> break
        }
    }

    return ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int): String {
    while (true) {
        when {
            konstue_1 == 1 -> continue
        }
    }

    return ""
}
