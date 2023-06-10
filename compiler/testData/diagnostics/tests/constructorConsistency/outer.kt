// FIR_IDENTICAL
class Outer {

    fun foo() = 1

    inner class Inner {

        konst x = this@Outer.foo()

        konst y = foo()
    }
}