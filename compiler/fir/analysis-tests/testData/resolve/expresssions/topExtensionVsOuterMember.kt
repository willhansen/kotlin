fun Outer.Inner.foo() = 42

class Outer {

    fun foo() = ""

    inner class Inner {
        konst x = foo() // Should be Int
    }
}
