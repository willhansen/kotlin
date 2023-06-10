// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, multiplicative-expression -> paragraph 2 -> sentence 2
 * PRIMARY LINKS: expressions, multiplicative-expression -> paragraph 1 -> sentence 1
 * expressions, multiplicative-expression -> paragraph 1 -> sentence 2
 * overloadable-operators -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: A / B is exactly the same as A.div(B)
 */

class A(var a: Int) {
    var isCalled = false
    var isCalledInt = false
    operator fun div(o: Int): A {
        isCalledInt = true
        return this
    }

    operator fun div(o: A): A {
        isCalled = true
        return this
    }
}

fun box(): String {
    konst a1 = A(-1)
    konst a2 = A(5)
    konst x = a1 / a2

    if (a1.isCalled && !a2.isCalled) {
        konst a3 = A(3)
        konst y = a3 / 2
        if (a3.isCalledInt)
            return "OK"
    }
    return "NOK"
}