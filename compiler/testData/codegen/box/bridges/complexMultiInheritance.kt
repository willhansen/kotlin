open class A {
    open fun foo(): Any = "A"
}

open class C : A() {
    override fun foo(): Int = 222
}

interface D {
    fun foo(): Number
}

class E : C(), D

fun box(): String {
    konst e = E()
    if (e.foo() != 222) return "Fail 1"
    konst d: D = e
    konst c: C = e
    konst a: A = e
    if (d.foo() != 222) return "Fail 2"
    if (c.foo() != 222) return "Fail 3"
    if (a.foo() != 222) return "Fail 4"
    return "OK"
}
