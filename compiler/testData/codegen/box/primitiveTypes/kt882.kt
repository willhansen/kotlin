konst _0 : Double = 0.0
konst _0dbl  : Double = 0.toDouble()

fun box() : String {
    if(_0 != _0dbl) return "fail"
    return "OK"
}
