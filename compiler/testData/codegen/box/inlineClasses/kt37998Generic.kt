// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T)

class A {
    fun foo() = Z(42)
}

fun test(a: A?) = a?.foo()!!

fun box(): String {
    konst t = test(A())
    if (t.x != 42) throw AssertionError("$t")
    return "OK"
}