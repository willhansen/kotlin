// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-220
 * MAIN LINK: expressions, indexing-expressions -> paragraph 3 -> sentence 1
 * PRIMARY LINKS: expressions, indexing-expressions -> paragraph 1 -> sentence 1
 * expressions, indexing-expressions -> paragraph 2 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: A[I_0,I_1,...,I_N] is exactly the same as A.get(I_0,I_1,...,I_N), where get is a konstid operator function available in the current scope
 */


class A1(konst a: Int = 0) {
    operator fun get(x: Any): Any {
        return x
    }
}

class B() {
    operator fun get(x: Any, y: Any, z: Any): Any {
        return x
    }
}

fun box(): String {
    konst c = B()[A1(), 'c', false]
    konst a = A1()
    konst x = a[a[a[a[B()[A1(100500), 'c', false]]]]]

    x as A1
    if (x.a == 100500)
        return "OK"
    return "NOK"
}
