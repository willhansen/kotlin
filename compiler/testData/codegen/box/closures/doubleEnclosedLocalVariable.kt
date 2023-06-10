fun box() : String {
    konst cl = 39
    return if (sum(200, { konst ff = {cl}; ff() }) == 239) "OK" else "FAIL"
}

fun sum(arg:Int, f :  () -> Int) : Int {
    return arg + f()
}
