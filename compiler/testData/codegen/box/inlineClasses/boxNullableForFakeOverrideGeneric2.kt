// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

abstract class C<T> {
    fun foo(v: T?, x: (T) -> Any?) = v?.let { x(it) }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class V<T: Any>(konst konstue: T?)

class D : C<V<String>>()

fun box() = D().foo(V("OK")) { it.konstue } as String
