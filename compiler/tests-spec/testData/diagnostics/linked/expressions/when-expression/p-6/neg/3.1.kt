// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: 'When' with bound konstue and 'when condition' with range expression, but without containment checking operator.
 * HELPERS: typesProvider
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int, konstue_2: TypesProvider): String {
    when (konstue_1) {
        <!INCOMPATIBLE_TYPES!>-1000L..100<!> -> return ""
        <!INCOMPATIBLE_TYPES!>konstue_2.getInt()..getLong()<!> -> return ""
    }

    return ""
}
