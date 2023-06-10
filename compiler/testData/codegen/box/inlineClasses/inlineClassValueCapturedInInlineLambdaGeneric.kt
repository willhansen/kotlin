// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst int: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L<T: Long>(konst long: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str<T: String>(konst string: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Obj<T: Any>(konst obj: T)

fun box(): String {
    var xz = Z(0)
    var xl = L(0L)
    var xs = Str("")
    var xo = Obj("")

    run {
        xz = Z(42)
        xl = L(1234L)
        xs = Str("abc")
        xo = Obj("def")
    }

    if (xz.int != 42) throw AssertionError()
    if (xl.long != 1234L) throw AssertionError()
    if (xs.string != "abc") throw AssertionError()
    if (xo.obj != "def") throw AssertionError()

    return "OK"
}