// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-253
 * MAIN LINK: statements, assignments, operator-assignments -> paragraph 2 -> sentence 4
 * PRIMARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 5
 * NUMBER: 1
 * DESCRIPTION: A -= B is exactly the same as A.minusAssign(B) or A = A.minus(B) (applied in order)
 */

class B(var a: Int) {
    var minus = false
    var minusAssign = false

    operator fun minus(konstue: Int): B {
        minus = true
        return B(a - konstue)
    }

    operator fun minusAssign(konstue: Int) {
        minusAssign = true
        a = a - konstue
    }
}

fun box(): String {
    konst b = B(1)
    b -= 1

    if (!b.minus && b.minusAssign && b.a == 0)
        return "OK"
    return "NOK"
}