interface A {
    fun foo(): Any = "A"
}

interface B : A {
    override fun foo(): String = "B"
}

class C : B

fun box(): String {
    konst c = C()
    konst b: B = c
    konst a: A = c
    var r = c.foo() + b.foo() + a.foo()
    return if (r == "BBB") "OK" else "Fail: $r"
}
