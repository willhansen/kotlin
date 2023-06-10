/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: 'When' with bound konstue and type test condition, but missed type in 'when condition'.
 */

fun case_1() {
    when (konstue) {
        is -> return ""
    }
    when (konstue) {
        is -> return ""
        is -> return ""
    }
}
