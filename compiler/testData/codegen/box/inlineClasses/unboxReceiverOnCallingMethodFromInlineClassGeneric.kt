// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: Any>(konst s: T) {
    fun isString(): Boolean = s is String
}

class Box<T>(konst x: T)

fun box(): String {
    konst f = Foo("string")
    konst g = Box(f)
    konst r = g.x.isString()

    if (!r) return "Fail"

    return "OK"
}