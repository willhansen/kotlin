// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses
// FILE: 1.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst x: String)

fun accessProperty(y: B): A {
    y.a = A("OK")
    return y.a
}

// FILE: 2.kt

class B(var a: A)

fun box(): String = accessProperty(B(A("Fail"))).x
