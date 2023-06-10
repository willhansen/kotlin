// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

abstract class Base(konst x: S)

class Test(x: S) : Base(x)

fun box() = Test(S("OK")).x.string