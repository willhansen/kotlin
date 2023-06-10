// FIR_IDENTICAL
open class B {
    private fun getX() = 1
}

class C : B() {
    konst x: Int
        get() = 1
}