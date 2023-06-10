// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst int: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class L(konst long: Long)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Str(konst string: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Obj(konst obj: Any)

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