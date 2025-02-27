// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst x: Int) : Comparable<Foo> {
    override fun compareTo(other: Foo): Int {
        return 10
    }
}

fun box(): String {
    konst f1 = Foo(42)
    konst ff1: Comparable<Foo> = f1

    if (ff1.compareTo(f1) != 10) return "Fail"

    return "OK"
}