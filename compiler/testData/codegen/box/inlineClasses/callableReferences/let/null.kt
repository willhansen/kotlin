// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Value(konst konstue: Any)

fun foo(konstue: Value?) = konstue?.konstue as String?

fun box(): String = (null as Value?).let(::foo) ?: "OK"
