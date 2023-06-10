// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 3
 * NUMBER: 2
 * DESCRIPTION: 'When' with bound konstue and 'when condition' with contains operator and type without defined contains operator.
 * HELPERS: classes
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: EmptyClass, konstue_3: Int, konstue_4: Any): String {
    when (konstue_1) {
        <!TYPE_MISMATCH_IN_RANGE, UNRESOLVED_REFERENCE_WRONG_RECEIVER!>in<!> konstue_2  -> return ""
        <!TYPE_MISMATCH_IN_RANGE, UNRESOLVED_REFERENCE_WRONG_RECEIVER!>in<!> konstue_3  -> return ""
        <!TYPE_MISMATCH_IN_RANGE, UNRESOLVED_REFERENCE_WRONG_RECEIVER!>in<!> konstue_4  -> return ""
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
        <!OVERLOAD_RESOLUTION_AMBIGUITY, TYPE_MISMATCH_IN_RANGE, UNREACHABLE_CODE!>in<!> konstue_3 -> <!UNREACHABLE_CODE!>{}<!>
        <!UNREACHABLE_CODE!><!OVERLOAD_RESOLUTION_AMBIGUITY, TYPE_MISMATCH_IN_RANGE!>in<!> throw Exception() -> {}<!>
        <!UNREACHABLE_CODE!><!OVERLOAD_RESOLUTION_AMBIGUITY, TYPE_MISMATCH_IN_RANGE!>in<!> return -> {}<!>
    }
}
