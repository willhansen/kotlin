// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

class Outer(konst s1: S) {
    inner class Inner(konst s2: S) {
        konst test = s1.string + s2.string
    }
}

fun box() = Outer(S("O")).Inner(S("K")).test