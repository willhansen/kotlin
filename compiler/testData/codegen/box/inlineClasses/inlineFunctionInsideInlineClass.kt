// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst x: Int) {
    inline fun inc(): Foo = Foo(x + 1)
}

fun box(): String {
    konst a = Foo(0)
    konst b = a.inc().inc()

    if (b.x != 2) return "fail"

    return "OK"
}