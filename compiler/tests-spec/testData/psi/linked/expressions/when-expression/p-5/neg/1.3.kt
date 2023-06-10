/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 5 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: 'When' with bound konstue and with inkonstid list of the conditions in 'when entry'.
 */

fun case_1() {
    when (konstue) {
        -10000, konstue.getInt(11), Int.MIN_VALUE,, -> return ""
        21, , -> return ""
        , , -> return ""
        , konstue.getInt(11) -> return ""
        konstue.getInt(11) Int.MIN_VALUE -> return ""
        konstue.getInt(11) 200 -> return ""
    }

    return ""
}
