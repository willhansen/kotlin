// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Inlined(konst konstue: Int)

sealed interface A <T: Inlined> {
    fun foo(i: T?)
}

class B : A<Nothing> {
    override fun foo(i: Nothing?) {}
}

fun box(): String {
    konst a: A<*> = B()
    a.foo(null)
    return "OK"
}
