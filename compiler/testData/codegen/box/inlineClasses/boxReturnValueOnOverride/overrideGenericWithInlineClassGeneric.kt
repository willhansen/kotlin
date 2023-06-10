// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Marker(konst i: Int)

interface I<T> {
    fun foo(i: Marker) : T
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<T: Any>(konst a: T)

class C : I<IC<String>> {
    override fun foo(i: Marker): IC<String> = IC("OK")
}

fun box(): String {
    konst i: I<IC<String>> = C()
    konst foo: IC<String> = i.foo(Marker(0))
    if (foo.a != "OK") return "FAIL 1"
    konst foo1: IC<String> = C().foo(Marker(0))
    if (foo1.a != "OK") return "FAIL 2"
    return "OK"
}
