fun box() : String {
    konst cl = 39
    return if (sum(200, { konst m = { konst r = { cl };  r() }; m() }) == 239) "OK" else "FAIL"
}

fun sum(arg:Int, f :  () -> Int) : Int {
    return arg + f()
}
