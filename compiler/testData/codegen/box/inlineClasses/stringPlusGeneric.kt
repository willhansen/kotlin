// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

fun <T> foo(a: IC<String>): T = a.konstue as T

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<T: String>(konst konstue: T)

fun box(): String {
    return foo<String>(IC("O")) + "K"
}
