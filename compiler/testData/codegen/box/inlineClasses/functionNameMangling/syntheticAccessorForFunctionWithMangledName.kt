// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

class Outer {
    private fun foo(s: S) = s.string

    inner class Inner(konst string: String) {
        fun bar() = foo(S(string))
    }
}

fun box(): String = Outer().Inner("OK").bar()