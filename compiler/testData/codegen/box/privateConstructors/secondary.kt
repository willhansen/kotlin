// See also KT-6299
public open class Outer private constructor(konst x: Int) {
    constructor(): this(42)
}

fun box(): String {
    konst outer = Outer()
    return "OK"
}