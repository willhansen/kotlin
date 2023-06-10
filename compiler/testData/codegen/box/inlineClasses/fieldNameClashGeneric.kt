// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: String>(konst s: T) {
    konst Int.s: Int get() = 42
}

fun box(): String {
    if (Z("a").toString() == "Z(s=a)")
        return "OK"
    return "Fail"
}
