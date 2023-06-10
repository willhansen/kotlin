// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: String, konstue_3: TypesProvider): String {
    when {
        <!CONDITION_TYPE_MISMATCH, TYPE_MISMATCH!>.012f / konstue_1<!> -> return ""
        <!CONDITION_TYPE_MISMATCH!>"$konstue_2..."<!> -> return ""
        <!CONDITION_TYPE_MISMATCH!>'-'<!> -> return ""
        <!CONDITION_TYPE_MISMATCH!>{}<!> -> return ""
        <!CONDITION_TYPE_MISMATCH, TYPE_MISMATCH!>konstue_3.getAny()<!> -> return ""
        <!CONDITION_TYPE_MISMATCH, TYPE_MISMATCH!>-10..-1<!> -> return ""
    }

    return ""
}
