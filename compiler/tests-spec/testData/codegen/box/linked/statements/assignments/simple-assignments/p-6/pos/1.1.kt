// WITH_STDLIB

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: statements, assignments, simple-assignments -> paragraph 6 -> sentence 1
 * PRIMARY LINKS: statements, assignments, simple-assignments -> paragraph 7 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION:
 */


class A() {
    konst list = arrayListOf(1, 2, 3)
    operator fun set(a: Int, konstue: Int) {
        this.list.add(a, konstue)
    }

    operator fun set(a: Int, b: Int, konstue: Int) {
        this.list.add(a, konstue)
        this.list.add(b, konstue)
    }
}

fun box(): String {
    konst a = A()
    a[0] = 0

    konst b = A()
    b[0, 2] = 0

    if (a.list == arrayListOf(0, 1, 2, 3) &&
        b.list == arrayListOf(0, 1, 0, 2, 3)
    )
        return "OK"
    return "NOK"
}