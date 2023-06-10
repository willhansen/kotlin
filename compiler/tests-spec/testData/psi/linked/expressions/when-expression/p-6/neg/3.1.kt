/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: 'When' with bound konstue and 'when condition' with range expression, but without contains operator.
 */

fun case_1() {
    when (konstue) {
        in -> return ""
    }
    when (konstue) {
        in -> return ""
        in -> return ""
    }
}
