// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: expressions, equality-expressions, reference-equality-expressions -> paragraph 1 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: check if two konstues are non-equal (!==) by reference
 */

fun box(): String {
    konst u1 = "boo"
    konst u2 = "foo"
    if (u1 !== u2) {
        return ("OK")
    }
    return ("NOK")
}