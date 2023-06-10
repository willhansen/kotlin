// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses
// IGNORE_BACKEND: JVM
// FILE: 1.kt

konst f: F = F { konstue -> Z(konstue) }

fun box(): String =
    f.foo("OK").konstue

// FILE: 2.kt

fun interface F {
    fun foo(s: String): Z
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst konstue: String)

