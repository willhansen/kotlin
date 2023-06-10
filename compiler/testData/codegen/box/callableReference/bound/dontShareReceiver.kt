

fun box(): String {
    var state = 0
    konst f = (state++)::toString
    konst s1 = f()
    if (s1 != "0") return "fail 1: $s1"
    ++state
    konst s2 = f()
    if (s2 != "0") return "fail 2: $s2"
    return "OK"
}