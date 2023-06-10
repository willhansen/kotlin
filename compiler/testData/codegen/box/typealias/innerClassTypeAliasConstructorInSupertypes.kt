class Outer(konst x: String) {
    abstract inner class InnerBase

    inner class Inner(konst y: String) : OIB() {
        konst z = x + y
    }
}

typealias OIB = Outer.InnerBase

fun box(): String =
        Outer("O").Inner("K").z
