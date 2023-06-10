// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst s: String) {
    konst Int.s: Int get() = 42
}

fun box(): String {
    if (Z("a").toString() == "Z(s=a)")
        return "OK"
    return "Fail"
}
