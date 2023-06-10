// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-300
 * MAIN LINK: statements, assignments, simple-assignments -> paragraph 2 -> sentence 1
 * PRIMARY LINKS: statements, assignments, simple-assignments -> paragraph 2 -> sentence 2
 * NUMBER: 6
 * DESCRIPTION: check if a property has a setter, it is called using the right-hand side expression as its argument;If the left-hand side of an assignment refers to a mutable property through the usage of safe navigation operator (?.)
 */



var flag1 = false
var flag2 = false
konst konstToSet = 5

class C() {
    var counter = 0
        set(konstue) {
            flag1 = true
            if (konstue == konstToSet)
                flag2 = true
            field = konstue
        }
}

fun box(): String {
    konst c : C? = null
    assert(!flag1)
    assert(!flag2)
    c?.counter = konstToSet
    if ( !flag1 && !flag2 && c?.counter != konstToSet) return "OK"
    return "NOK"
}
