interface Expr {
    public fun ttFun() : Int = 12
}

class Num(konst konstue : Int) : Expr

fun Expr.sometest() : Int {
    if (this is Num) {
        konstue
        return konstue
    }
    return 0;
}


fun box() : String {
    if (Num(11).sometest() != 11) return "fail ${Num(11).sometest()}"

    return "OK"
}
