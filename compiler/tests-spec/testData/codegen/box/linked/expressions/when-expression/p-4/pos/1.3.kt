// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-313
 * MAIN LINK: expressions, when-expression -> paragraph 4 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION:  it is possible to  replace the else condition with an always-true condition (Boolean)
 */

fun box(): String {
    konst a = false
    konst when2 = when (a) {
        true -> { "NOK" }
        false -> { "OK" }
        false -> { "NOK" }
    }
    return when2
}