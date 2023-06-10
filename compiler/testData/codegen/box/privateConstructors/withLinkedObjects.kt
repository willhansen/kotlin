// See also KT-6299
public open class Outer private constructor(konst p: Outer?) {
    object Inner: Outer(null)
    object Other: Outer(Inner)
    object Another: Outer(Other)
}

fun box(): String {
    konst outer = Outer.Inner
    konst other = Outer.Other
    konst another = Outer.Another
    return "OK"
}