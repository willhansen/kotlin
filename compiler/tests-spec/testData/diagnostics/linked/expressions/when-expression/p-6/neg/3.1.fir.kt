// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: TypesProvider): String {
    when (konstue_1) {
        <!INCOMPATIBLE_TYPES!>-1000L..100<!> -> return ""
        <!INCOMPATIBLE_TYPES!>konstue_2.getInt()..getLong()<!> -> return ""
    }

    return ""
}
