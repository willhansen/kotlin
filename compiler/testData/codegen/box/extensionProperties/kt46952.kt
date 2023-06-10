inline class C(konst s: String)

fun f(g: () -> C): C = g()

konst C.foo: C
    get() = f { this }

fun box() = C("OK").foo.s
