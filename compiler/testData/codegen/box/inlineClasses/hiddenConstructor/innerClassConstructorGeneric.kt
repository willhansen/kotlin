// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

class Outer(konst s1: S<String>) {
    inner class Inner(konst s2: S<String>) {
        konst test = s1.string + s2.string
    }
}

fun box() = Outer(S("O")).Inner(S("K")).test