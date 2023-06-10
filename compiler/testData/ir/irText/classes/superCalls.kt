// FIR_IDENTICAL
open class Base {
    open fun foo() {}

    open konst bar: String = ""

    override fun hashCode() = super.hashCode()
}

class Derived : Base() {
    override fun foo() {
        super.foo()
    }

    override konst bar: String
        get() = super.bar
}