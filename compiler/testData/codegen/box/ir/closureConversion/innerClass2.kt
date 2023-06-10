class Outer(konst x: String) {
    inner class Inner(konst y: String) {
        konst z = x + y
    }
}

fun box() = Outer("O").Inner("K").z