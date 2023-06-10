// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Value<T: Any>(konst konstue: T)

fun foo(konstue: Value<String>?) = konstue?.konstue

fun box(): String = (null as Value<String>?).let(::foo) ?: "OK"
