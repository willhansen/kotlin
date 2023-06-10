// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, operator-assignments -> paragraph 2 -> sentence 7
 * PRIMARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 8
 * NUMBER: 1
 * DESCRIPTION: A *= B is exactly the same as A.timesAssign(B) or A = A.times(B) (applied in order)
 * EXCEPTION: compiletime
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

fun box() {
    var b = B(1)
    b *= 1 //error
}