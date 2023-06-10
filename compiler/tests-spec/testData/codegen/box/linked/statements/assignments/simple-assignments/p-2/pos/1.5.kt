// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-300
 * MAIN LINK: statements, assignments, simple-assignments -> paragraph 2 -> sentence 1
 * PRIMARY LINKS: statements, assignments, simple-assignments -> paragraph 2 -> sentence 2
 * NUMBER: 5
 * DESCRIPTION: check if a property has a setter, it is called using the right-hand side expression as its argument;If the left-hand side of an assignment refers to a mutable property through the usage of safe navigation operator (?.)
 */

konst konstToSet = 5

class C() {
    var counter = 0
}

fun box(): String {
    konst c :C?= C()
    c?.counter = konstToSet
    if (c?.counter == konstToSet) return "OK"
    return "NOK"
}