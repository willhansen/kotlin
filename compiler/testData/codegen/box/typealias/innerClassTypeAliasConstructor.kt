class Outer(konst x: String) {
    inner class Inner(konst y: String) {
        konst z = x + y
    }
}

typealias OI = Outer.Inner

fun box(): String =
        Outer("O").OI("K").z