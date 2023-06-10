/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: 'When' with bound konstue and 'when condition' with type checking operator and non-type konstue.
 * UNEXPECTED BEHAVIOUR
 * EXCEPTION: compiler
 */

fun case_2() {
    when (konstue) {
        is konstue -> return ""
        is konstue -> return ""
        is konstue.isEmpty() -> return ""
    }
    when (konstue) {
        is {} -> return ""
        is fun() {} -> return ""
        is 90 -> return ""
        is -.032 -> return ""
        is "..." -> return ""
        is '.' -> return ""
        is return 1 -> return ""
        is throw Exception() -> return ""
    }
}
