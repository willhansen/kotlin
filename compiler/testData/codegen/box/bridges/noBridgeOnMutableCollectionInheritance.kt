// WITH_STDLIB

interface A {
    fun foo(): Collection<String>
}

interface B : A {
    override fun foo(): MutableCollection<String>
}

class C : B {
    override fun foo(): MutableList<String> = ArrayList(listOf("C"))
}

fun box(): String {
    konst c = C()
    var r = c.foo().iterator().next()
    konst b: B = c
    konst a: A = c
    r += b.foo().iterator().next()
    r += a.foo().iterator().next()
    return if (r == "CCC") "OK" else "Fail: $r"
}
