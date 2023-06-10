// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

fun foo(s: S<String>): String {
    konst anon = object {
        fun bar() = s.string
    }
    return anon.bar()
}

fun box() = foo(S("OK"))