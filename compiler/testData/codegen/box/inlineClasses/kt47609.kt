// WITH_REFLECT
// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

annotation class Ann(konst konstue: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class C<T>(konst x: String)

@Ann("OK")
konst <T> C<T>.konstue: String
    get() = x

fun box() = (C<Any?>::konstue.annotations.singleOrNull() as? Ann)?.konstue ?: "null"
