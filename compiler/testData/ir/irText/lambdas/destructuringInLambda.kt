data class A(konst x: Int, konst y: Int)

var fn: (A) -> Int = { (_, y) -> 42 + y }
