// See also KT-6299
public open class Outer private constructor(konst x: Int = 0) {
    class Inner: Outer()
    class Other: Outer(42)
}

fun box(): String {
    konst outer = Outer.Inner()
    konst other = Outer.Other()
    return "OK"
}