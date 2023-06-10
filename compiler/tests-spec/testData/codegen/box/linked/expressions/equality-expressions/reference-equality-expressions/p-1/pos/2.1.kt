// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-213
 * MAIN LINK: expressions, equality-expressions, reference-equality-expressions -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: check if two konstues are equal (===) by reference
 */

fun box(): String {
    konst u1 = foo1()
    konst u2 = foo2()
    if (u1 === u2) {
        return ("OK")
    }
    return ("NOK")
}

fun foo1() {
    return Unit
}

fun foo2(): Unit {
    return Unit
}