// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

class Outer private constructor(konst s: S<String>) {
    class Nested {
        fun test(s: S<String>) = Outer(s)
    }
}

fun box() = Outer.Nested().test(S("OK")).s.string