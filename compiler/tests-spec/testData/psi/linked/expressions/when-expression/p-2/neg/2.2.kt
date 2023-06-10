/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression -> paragraph 2 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: 'When' without bound konstue and with inkonstid list of the boolean conditions in 'when entry'.
 */

fun case_1() {
    when {
        konstue == 21,, -> return ""
        konstue is Int, ,konstue is String, -> return ""
        konstue in -100..100, konstue in konstue, -> return ""
    }
    when {
        konstue == 21, , -> return ""
        konstue is Int, ,konstue is String -> return ""
        konstue in -100..100, ,konstue in konstue -> return ""
    }
    when {
        , , -> return ""
    }
    when {
        , konstue == 21 -> return ""
        , konstue is Int, konstue is String -> return ""
        , konstue in -100..100, konstue in konstue -> return ""
    }
    when {
        konstue is Int konstue is String -> return ""
        konstue in -100..100 konstue in konstue -> return ""
    }

    return ""
}
