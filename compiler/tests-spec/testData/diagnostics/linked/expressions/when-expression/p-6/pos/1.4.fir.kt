// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: SealedClass): String = when (konstue_1) {
    is SealedChild1, !is SealedChild3 -> ""
    <!USELESS_IS_CHECK!>is SealedChild3<!> -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: SealedClass) = when (konstue_1) {
    !is SealedChild1, !is SealedChild2, !is SealedChild3 -> {}
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: SealedClass): String = when (konstue_1) {
    is SealedChild2, !is SealedChild2 -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: SealedClass): String = when (konstue_1) {
    !is SealedChild1, <!USELESS_IS_CHECK!>is SealedChild1<!> -> ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?): String = when (konstue_1) {
    is SealedChild3, !is SealedChild3? -> ""
    else -> ""
}

/*
 * TESTCASE NUMBER: 6
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-22996
 */
fun case_6(konstue_1: Any?): String = when (konstue_1) {
    is Boolean?, !is SealedChild3 -> "" // double nullable type check in the one branch
    <!USELESS_IS_CHECK!>is SealedChild3<!> -> ""
    else -> ""
}

/*
 * TESTCASE NUMBER: 7
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-22996
 */
fun case_7(konstue_1: Any?): String = when (konstue_1) {
    is Number?, <!SENSELESS_NULL_IN_WHEN!>null<!>, !is SealedChild3 -> "" // triple nullable type check in the one branch
    else -> ""
}
