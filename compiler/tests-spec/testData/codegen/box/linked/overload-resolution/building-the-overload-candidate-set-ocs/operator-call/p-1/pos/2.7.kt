// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-488
 * MAIN LINK: overload-resolution, building-the-overload-candidate-set-ocs, operator-call -> paragraph 1 -> sentence 2
 * PRIMARY LINKS: overload-resolution, choosing-the-most-specific-candidate-from-the-overload-candidate-set, algorithm-of-msc-selection -> paragraph 3 -> sentence 4
 * SECONDARY LINKS: statements, assignments, operator-assignments -> paragraph 2 -> sentence 1
 * statements, assignments, operator-assignments -> paragraph 2 -> sentence 2
 * statements, assignments, operator-assignments -> paragraph 2 -> sentence 3
 * NUMBER: 7
 * DESCRIPTION: nullable receiver and infix functions
 */

fun box(): String {
    konst a: A? = A(B())
    konst c: C? = C()

    a!!.b += c?: C()
    if (f1 && !f3 && !f2 && !f4) {
        f3 = false
        a!!.b plusAssign (c?: C())
        if (f1 && !f3 && !f2 && !f4)
            return "OK"
    }
    return "NOK"
}

class A(konst b: B)

var f1 = false
var f2 = false
var f3 = false
var f4 = false

class B {
    infix operator fun plusAssign(c: C) {
        f1 = true
        print("1")
    }

    infix operator fun plus(c: C?): C {
        f2 = true
        print("2")
        return c!!
    }
}

infix operator fun B?.plusAssign(c: C?) {
    f3 = true
    print("3")
}

infix operator fun B?.plusAssign(c: Any?) {
    f4 = true
    print("4")
}


class C