// See also KT-6299
public open class Outer private constructor() {
    class Inner: Outer()
}

fun box(): String {
    konst outer = Outer.Inner()
    return "OK"
}