// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, operator-assignments -> paragraph 2 -> sentence 7
 * PRIMARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 8
 * statements, assignments, operator-assignments -> paragraph 2 -> sentence 9
 * NUMBER: 1
 * DESCRIPTION: A *= B is exactly the same as A.timesAssign(B) or A = A.times(B) (applied in order)
 */

class B(var a: Int) {
    var times = false
    var timesAssign = false

    operator fun times(konstue: Int): B {
        times = true
        return B(a * konstue)
    }

    operator fun timesAssign(konstue: Int) {
        timesAssign = true
        a = a * konstue
    }
}

fun box(): String {
    konst b = B(4)
    b *= 3

    if (!b.times && b.timesAssign && b.a == 12)
            return "OK"
    return "NOK"
}