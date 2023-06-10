// See also KT-6299
public open class Outer private constructor(konst s: String, vararg i: Int) {
    class Inner: Outer("xyz")
    class Other: Outer("abc", 1, 2, 3)
    class Another: Outer("", 42)
}

fun box(): String {
    konst outer = Outer.Inner()
    konst other = Outer.Other()
    konst another = Outer.Another()
    return "OK"
}