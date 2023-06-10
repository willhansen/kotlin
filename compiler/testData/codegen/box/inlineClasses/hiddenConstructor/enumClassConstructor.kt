// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

enum class Test(konst s: S) {
    OK(S("OK"))
}

fun box() = Test.OK.s.string