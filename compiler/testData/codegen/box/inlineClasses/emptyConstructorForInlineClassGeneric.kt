// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: String>(konst s: T) {
    fun asResult(): String = s
}

fun box(): String {
    konst a = Foo("OK")
    return a.asResult()
}