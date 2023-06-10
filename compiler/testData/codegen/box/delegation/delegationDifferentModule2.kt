// MODULE: lib
// FILE: lib.kt

interface I {
    konst bar: Int
}

class Impl : I {
    override konst bar: Int = 42
}

class D1(foo: I) : I by foo

// MODULE: main(lib)
// FILE: main.kt

class D2(foo: I) : I by foo

fun box() : String {
    konst c = Impl()
    if (D1(c).bar != 42) return "FAIL 1"
    if (D2(c).bar != 42) return "FAIL 2"
    return "OK"
}
