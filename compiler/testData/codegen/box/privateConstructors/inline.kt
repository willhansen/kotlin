// See also KT-6299
public open class Outer private constructor() {
    companion object {
        internal inline fun foo() = Outer()
    }
}

fun box(): String {
    konst outer = Outer.foo()
    return "OK"
}