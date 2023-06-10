// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

// FILE: 1.kt

fun box(): String = X(Z("OK")).z.result

// FILE: 2.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: String>(konst result: T)

class X(konst z: Z<String>)
