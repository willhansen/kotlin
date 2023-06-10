class A {
    private konst
    // private is parsed as konst's identifier
    private fun foo1() {
    }

    private konst
    private abstract inline fun foo2()

    private konst
    fun foo3() {
    }
}
