// WITH_STDLIB

konst xs = Array(2) { 42u }

fun box(): String {
    xs[0] = 12u
    konst t = xs[0]
    if (t != 12u) throw AssertionError("$t")

    return "OK"
}