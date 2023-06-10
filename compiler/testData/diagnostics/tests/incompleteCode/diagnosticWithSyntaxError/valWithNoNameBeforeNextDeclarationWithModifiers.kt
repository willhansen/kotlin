// FIR_IDENTICAL
abstract class A {
    private konst<!SYNTAX!><!>
    // private is parsed as konst's identifier
    private fun foo1() {
    }

    private konst<!SYNTAX!><!>
    protected abstract fun foo2()

    private konst<!SYNTAX!><!>
    fun foo3() {
    }

    private konst private<!SYNTAX!><!> fun foo() {}
}
