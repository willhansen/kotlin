// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int): Int {
    while (true) {
        when (konstue_1) {
            1 -> return 1
            2 -> break
        }
    }

    return 0
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int): Int {
    while (true) {
        when (konstue_1) {
            1 -> continue
            2 -> return 1
        }
    }

    return 0
}
