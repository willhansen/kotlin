// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when (konstue_1) {
        1 -> when (konstue_3) {
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 1000<!> -> "1"
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 100<!> -> "2"
            else -> "3"
        }
        2 -> when (konstue_3) {
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 1000<!> -> "1"
                <!CONFUSING_BRANCH_CONDITION_ERROR!>konstue_2 > 100<!> -> "2"
            else -> ""
        }
        3 -> when (konstue_3) {
            else -> ""
        }
        4 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            null -> "3"
            else -> ""
        }
        5 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            else -> ""
        }
        6 -> when (konstue_3) {
            else -> ""
        }
    }
}
