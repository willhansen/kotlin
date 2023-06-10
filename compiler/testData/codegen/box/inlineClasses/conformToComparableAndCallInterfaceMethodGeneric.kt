// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Int>(konst x: T) : Comparable<Foo<T>> {
    override fun compareTo(other: Foo<T>): Int {
        return 10
    }
}

fun box(): String {
    konst f1 = Foo(42)
    konst ff1: Comparable<Foo<Int>> = f1

    if (ff1.compareTo(f1) != 10) return "Fail"

    return "OK"
}