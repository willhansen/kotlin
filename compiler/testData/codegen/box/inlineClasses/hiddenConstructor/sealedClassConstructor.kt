// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

sealed class Sealed(konst x: S)

class Test(x: S) : Sealed(x)

fun box() = Test(S("OK")).x.string