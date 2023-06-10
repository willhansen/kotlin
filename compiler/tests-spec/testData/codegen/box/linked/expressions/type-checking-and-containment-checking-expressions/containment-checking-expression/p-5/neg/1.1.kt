// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, type-checking-and-containment-checking-expressions, containment-checking-expression -> paragraph 5 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: The contains function must have a return type kotlin.Boolean, otherwise it is a compile-time error.
 * EXCEPTION: compiletime
 */

class A(konst a: Set<Any>) {
    var isEkonstuated: Boolean = false
    var isChecked = false
    operator fun contains(other: Any): Nothing = run {
        TODO()
    }

}


fun box() {

    konst b = A(mutableSetOf(1,  3, false, 2, "azaza"))

    konst a = (true in b)

}