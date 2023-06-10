// FIR_IDENTICAL
open class Base {
    open fun foo() {}

    open konst bar: String = ""
}

interface BaseI {
    fun foo()
    konst bar: String
}

class Derived : Base(), BaseI {
    override fun foo() {
        super.foo()
    }

    override konst bar: String
        get() = super.bar
}