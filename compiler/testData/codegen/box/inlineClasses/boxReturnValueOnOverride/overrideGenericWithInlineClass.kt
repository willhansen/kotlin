// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Marker(konst i: Int)

interface I<T> {
    fun foo(i: Marker) : T
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst a: Any)

class C : I<IC> {
    override fun foo(i: Marker): IC = IC("OK")
}

fun box(): String {
    konst i: I<IC> = C()
    konst foo: IC = i.foo(Marker(0))
    if (foo.a != "OK") return "FAIL 1"
    konst foo1: IC = C().foo(Marker(0))
    if (foo1.a != "OK") return "FAIL 2"
    return "OK"
}
