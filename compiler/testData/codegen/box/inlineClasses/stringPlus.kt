// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

fun <T> foo(a: IC): T = a.konstue as T

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC(konst konstue: String)

fun box(): String {
    return foo<String>(IC("O")) + "K"
}
