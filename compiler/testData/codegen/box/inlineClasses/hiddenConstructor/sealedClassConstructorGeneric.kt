// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

sealed class Sealed(konst x: S<String>)

class Test(x: S<String>) : Sealed(x)

fun box() = Test(S("OK")).x.string