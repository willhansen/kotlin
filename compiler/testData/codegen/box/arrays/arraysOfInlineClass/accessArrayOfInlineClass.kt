// !LANGUAGE: +InlineClasses

inline class Z(konst data: Int)

konst xs = Array(2) { Z(42) }

fun box(): String {
    xs[0] = Z(12)
    konst t = xs[0]
    if (t.data != 12) throw AssertionError("$t")

    return "OK"
}