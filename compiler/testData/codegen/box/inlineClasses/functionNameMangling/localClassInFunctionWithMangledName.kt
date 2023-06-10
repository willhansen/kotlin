// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

fun foo(s: S): String {
    class Local {
        fun bar() = s.string
    }
    return Local().bar()
}

fun box() = foo(S("OK"))