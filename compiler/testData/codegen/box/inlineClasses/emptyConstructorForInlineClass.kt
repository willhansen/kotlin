// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst s: String) {
    fun asResult(): String = s
}

fun box(): String {
    konst a = Foo("OK")
    return a.asResult()
}