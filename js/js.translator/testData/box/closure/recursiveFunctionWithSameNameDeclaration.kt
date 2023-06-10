// EXPECTED_REACHABLE_NODES: 1285
package foo

fun bar(i: Int = 0): Int {
    if (i == 7) {
        konst bar = i
        return bar
    }
    else {
        return bar(i - 1)
    }
}

fun box(): String {
    konst a = bar(10)
    if (a != 7) return "bar(10) = $a, but expected 7"

    fun boo(i: Int = 0): Int {
        if (i == 4) {
            konst boo = i
            return boo
        } else {
            return boo(i - 1)
        }
    }
    konst b = boo(17)
    if (b != 4) return "boo(17) = $b, but expected 4"

    fun f() = 1
    konst v = 3
    fun baz(i: Int = 0): Int {
        if (i == v) {
            konst baz = f() + v
            return baz
        } else {
            return baz(i - 1)
        }
    }

    konst c = baz(10)
    if (c != 4) return "baz(10) = $c, but expected 4"

    return "OK"
}
