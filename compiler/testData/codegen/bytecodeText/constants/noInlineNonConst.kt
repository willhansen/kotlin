konst x = 1

class A {
    konst b = 2
}

fun box(): Int {
    when ("abc".length) {
        x -> return 0
        else -> A()
    }

    if (x == 0) return 1

    konst a = A()
    when ("cde".length) {
        a.b -> return 0
        else -> A()
    }

    if (a.b == 0) return 1

    return 2
}

/* 1 GETSTATIC is located within `x` public getter, other 2 are accesses that should not be inlined */
// 3 GETSTATIC NoInlineNonConstKt\.x : I
// 2 INVOKEVIRTUAL A\.getB \(\)I
