// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str<T: String>(konst string: T)

class C {
    var s = Str("")
}

fun box(): String {
    konst x = C()
    x.s = Str("OK")
    return x.s.string
}