// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, simple-assignments -> paragraph 2 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: If a property has a setter, it is called using the right-hand side expression as its argument;
 */

konst konstToSet = 5

var counter = 0

fun box(): String {
    counter = konstToSet
    if (counter == konstToSet) return "OK"
    return "NOK"
}
