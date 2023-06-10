// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst s: Any) {
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