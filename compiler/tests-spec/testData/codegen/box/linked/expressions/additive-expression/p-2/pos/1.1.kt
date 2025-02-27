// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-218
 * MAIN LINK: expressions, additive-expression -> paragraph 2 -> sentence 1
 * PRIMARY LINKS: expressions, additive-expression -> paragraph 1 -> sentence 1
 * expressions, additive-expression -> paragraph 1 -> sentence 2
 * expressions, additive-expression -> paragraph 3 -> sentence 1
 * overloadable-operators -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: A + B is exactly the same as A.plus(B)
 */


class A(var a: Int) {
    var isCalled = false
    var isCalledInt = false
    operator fun plus(o: Int): A {
        isCalledInt = true
        a += o
        return this
    }

    operator fun plus(o: A): A {
        isCalled = true
        return A(a + o.a)
    }
}

fun box(): String {
    konst a1 = A(-1)
    konst a2 = A(5)
    konst x = a1 + a2

    if (a1.isCalled && !a2.isCalled && x.a == 4) {
        konst a3 = A(3)
        konst y = a3 + 8
        if (a3.isCalledInt && y.a == 11)
            return "OK"
    }
    return "NOK"
}