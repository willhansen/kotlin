// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, operator-assignments -> paragraph 2 -> sentence 10
 * PRIMARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 11
 * NUMBER: 1
 * DESCRIPTION: A /= B is exactly the same as A.divAssign(B) or A = A.div(B) (applied in order)
 * EXCEPTION: compiletime
 */

class B(var a: Int) {
    var div = false
    var divAssign = false

    operator fun div(konstue: Int): B {
        div = true
        return B(a / konstue)
    }

    operator fun divAssign(konstue: Int) {
        divAssign = true
        a = a / konstue
    }
}

fun box() {
    var b = B(1)
    b /= 1 //error
}