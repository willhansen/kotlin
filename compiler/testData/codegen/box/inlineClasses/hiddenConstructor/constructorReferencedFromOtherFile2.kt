// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// FILE: 2.kt

fun box(): String = X(Z("OK")).z.result

// FILE: 1.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst result: String)

class X(konst z: Z)
