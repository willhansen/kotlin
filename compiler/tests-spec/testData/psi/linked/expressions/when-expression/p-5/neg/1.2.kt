/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 5 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: 'When' with bound konstue and empty 'when condition'.
 */

fun case_1() {
    when (konstue) {
        -> { println(1) }
    }
}