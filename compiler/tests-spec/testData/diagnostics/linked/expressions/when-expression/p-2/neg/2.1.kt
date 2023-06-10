// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression -> paragraph 2 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: When with non-boolean konstue in the when condition.
 * HELPERS: typesProvider
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: String, konstue_3: TypesProvider): String {
    when {
        <!TYPE_MISMATCH!>.012f / konstue_1<!> -> return ""
        <!TYPE_MISMATCH!>"$konstue_2..."<!> -> return ""
        <!CONSTANT_EXPECTED_TYPE_MISMATCH!>'-'<!> -> return ""
        <!TYPE_MISMATCH!>{}<!> -> return ""
        <!TYPE_MISMATCH!>konstue_3.getAny()<!> -> return ""
        <!TYPE_MISMATCH!>-10..-1<!> -> return ""
    }

    return ""
}
