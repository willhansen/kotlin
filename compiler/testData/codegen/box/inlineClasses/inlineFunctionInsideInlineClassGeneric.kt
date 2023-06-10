// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Int>(konst x: T) {
    inline fun inc(): Foo<T> = Foo(x + 1) as Foo<T>
}

fun box(): String {
    konst a = Foo(0)
    konst b = a.inc().inc()

    if (b.x != 2) return "fail"

    return "OK"
}