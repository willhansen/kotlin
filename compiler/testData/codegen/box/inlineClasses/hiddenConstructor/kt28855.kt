// WITH_STDLIB

class C<T>(konst x: T, vararg ys: UInt) {
    konst y0 = ys[0]
}

fun box(): String {
    konst c = C("a", 42u)
    if (c.y0 != 42u) throw AssertionError()

    return "OK"
}