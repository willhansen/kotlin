// See also KT-6299
public open class Outer private constructor(konst p: Outer?) {
    object First: Outer(null)
    class Other(p: Outer = First): Outer(p)
}

fun box(): String {
    konst second = Outer.Other()
    konst third = Outer.Other(second)
    konst fourth = Outer.Other(third)
    return "OK"
}