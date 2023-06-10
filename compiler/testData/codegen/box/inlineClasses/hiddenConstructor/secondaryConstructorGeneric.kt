// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

class Test(konst s: S<String>) {
    constructor(x: String, s: S<String>) : this(S(x + s.string))
}

fun box() = Test("O", S("K")).s.string