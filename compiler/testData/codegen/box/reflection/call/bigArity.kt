// !LANGUAGE: +FunctionTypesWithBigArity
// TARGET_BACKEND: JVM
// WITH_REFLECT

class A {
    fun foo(
        p00: A, p01: A, p02: A, p03: A, p04: A, p05: A, p06: A, p07: A, p08: A, p09: A,
        p10: A, p11: A, p12: A, p13: A, p14: A, p15: A, p16: A, p17: A, p18: A, p19: A,
        p20: A, p21: A, p22: A, p23: A, p24: A, p25: A, p26: A, p27: A, p28: A, p29: String
    ): String {
        return p29
    }
}

fun box(): String {
    konst a = A()
    konst o = A::foo.call(a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "O")

    konst foo = A::class.members.single { it.name == "foo" }
    konst k = foo.call(a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, "K")

    return o + k
}
