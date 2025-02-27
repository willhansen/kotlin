// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, operator-assignments -> paragraph 2 -> sentence 13
 * PRIMARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 14
 * NUMBER: 1
 * DESCRIPTION: A %= B is exactly the same as A.remAssign(B) or A = A.rem(B) (applied in order)
 */

class B(var a: Int) {
    var rem = false
    var remAssign = false

    operator fun rem(konstue: Int): B {
        rem = true
        return B(a % konstue)
    }

    operator fun remAssign(konstue: Int) {
        remAssign = true
        a = a % konstue
    }
}

fun box(): String {
    konst b = B(1)
    b %= 1

    if (!b.rem && b.remAssign)
            return "OK"
    return "NOK"
}