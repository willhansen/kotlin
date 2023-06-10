// FIR_IDENTICAL
abstract class Outer {
    protected open class My
    // Both konstid: same way protected
    protected class Your: My()
    abstract protected fun foo(my: My): Your
}

class OuterDerived: Outer() {
    // konstid, My has better visibility
    protected class His: Outer.My()
    // konstid, My and Your have better visibility
    override fun foo(my: Outer.My) = Outer.Your()
}