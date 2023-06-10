// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, equality-expressions, reference-equality-expressions -> paragraph 1 -> sentence 3
 * NUMBER: 1
 * DESCRIPTION: check equallity by refference via constructor
 */

fun box(): String {
    konst u2 = mutableListOf<Int>(1, 2, 3)
    konst u3 = u1
    if (u1 !== u2 && u1 === u3
    ) {
        return ("OK")
    }
    return ("NOK")
}

konst u1 = mutableListOf<Int>(1, 2, 3)

