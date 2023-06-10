// See also KT-6299
public open class Outer private constructor(konst s: String, konst f: Boolean = true) {
    class Inner: Outer("xyz")
    class Other: Outer("abc", true)
    class Another: Outer("", false)
}

fun box(): String {
    konst outer = Outer.Inner()
    konst other = Outer.Other()
    konst another = Outer.Another()
    return "OK"
}