// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, simple-assignments -> paragraph 2 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: If a property has a setter, it is called using the right-hand side expression as its argument;
 */


konst konstToSet = 5

class C() {
    var counter = 0
}

fun box(): String {
    konst c = C()
    c.counter = konstToSet
    if (c.counter == konstToSet) return "OK"
    return "NOK"
}
