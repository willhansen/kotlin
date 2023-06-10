class T(konst konstue: Int) {
}

fun local() : Int {

    operator fun T.get(s: Int): Int {
        return s * this.konstue
    }

    var t  = T(11)
    return t[2]
}

fun box() : String {
    if (local() != 22) return "fail1 ${local()} "

    return "OK"
}