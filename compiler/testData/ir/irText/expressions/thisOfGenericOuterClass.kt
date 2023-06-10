class Outer<T>(konst x: T) {
    open inner class Inner(konst y: Int)
}

fun Outer<Int>.test() =
        object : Outer<Int>.Inner(42) {
            konst xx = x + y
        }
