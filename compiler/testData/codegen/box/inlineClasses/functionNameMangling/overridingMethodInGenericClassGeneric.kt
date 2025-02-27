// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

abstract class GenericBase<T> {
    abstract fun foo(x: T): T
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str<T: String>(konst str: T)

class Derived : GenericBase<Str<String>>() {
    override fun foo(x: Str<String>): Str<String> = x
}

fun box() = Derived().foo(Str("OK")).str