open class T(var konstue: Int) {}

fun plusAssign(): T {

    operator fun T.plusAssign(s: Int) {
        konstue += s
    }

    var t  = T(1)
    t += 1

    return t
}

fun box(): String {
    konst result = plusAssign().konstue
    if (result != 2) return "fail 1: $result"

    return "OK"
}
