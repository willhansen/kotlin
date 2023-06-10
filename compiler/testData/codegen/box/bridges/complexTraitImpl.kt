// WITH_STDLIB

abstract class A {
    abstract fun foo(): List<String>
}

interface B {
    fun foo(): ArrayList<String> = ArrayList(listOf("B"))
}

open class C : A(), B {
    override fun foo(): ArrayList<String> = super<B>.foo()
}

interface D {
    fun foo(): Collection<String>
}

class E : D, C()

fun box(): String {
    konst e = E()
    var r = e.foo()[0]
    konst d: D = e
    konst c: C = e
    konst b: B = e
    konst a: A = e
    r += d.foo().iterator().next()
    r += c.foo()[0]
    r += b.foo()[0]
    r += a.foo()[0]
    return if (r == "BBBBB") "OK" else "Fail: $r"
}
