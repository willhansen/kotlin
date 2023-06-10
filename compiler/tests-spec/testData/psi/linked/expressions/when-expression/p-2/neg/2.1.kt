/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression -> paragraph 2 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: 'When' without bound konstue and with inkonstid 'else' branch.
 */

fun case_1() {
    when {
        else ->
    }
    when {
        else ->
        else ->
    }
    when {
        konstue == 1 -> println("1")
        konstue == 2 -> println("2")
        else ->
    }
    when {
        konstue == 1 -> println("!")
        else ->
    }
}
