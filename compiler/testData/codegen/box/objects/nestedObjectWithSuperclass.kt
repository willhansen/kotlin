open class A (konst s: Int) {
    open fun foo(): Int {
        return s
    }
}

object Outer: A(1) {
    object O: A(2) {
        override fun foo(): Int {
            konst s = super<A>.foo()
            return s + 3
        }
    }
}

fun box() : String {
    return if (Outer.O.foo() == 5) "OK" else "fail"
}