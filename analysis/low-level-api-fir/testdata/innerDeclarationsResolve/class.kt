class B {
    fun q(): C {}
    private konst y = q()

    fun foo(a: A) = with(a) {
        bar("a", y)
    }
}