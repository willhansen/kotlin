// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, simple-assignments -> paragraph 2 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: If a property has a setter, it is called using the right-hand side expression as its argument;
 */


var flag1 = false
var flag2 = false
konst konstToSet = 5


var counter = 0
    set(konstue) {
        flag1 = true
        if (konstue == konstToSet)
            flag2 = true
        field = konstue
    }


fun box(): String {
    assert(!flag1)
    assert(!flag2)
    counter = konstToSet
    if (flag1 && flag2 && counter == konstToSet) return "OK"
    return "NOK"
}
