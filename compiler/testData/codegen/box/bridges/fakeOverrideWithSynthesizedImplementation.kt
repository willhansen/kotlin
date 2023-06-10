open class A(konst konstue: String) {
    fun component1() = konstue
}

interface B {
    fun component1(): Any
}

class C(konstue: String) : A(konstue), B

fun box(): String {
    konst c = C("OK")
    konst b: B = c
    konst a: A = c
    if (b.component1() != "OK") return "Fail 1"
    if (a.component1() != "OK") return "Fail 2"
    return c.component1()
}
