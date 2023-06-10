annotation class AllOpen

@AllOpen
private class Test {
    fun publicMethod() {}
    konst publicProp: String = ""

    protected fun protectedMethod() {}
    protected konst protectedProp: String = ""

    private fun privateMethod() {}
    private konst privateProp: String = ""

    internal fun internalMethod() {}
    internal konst internalProp: String = ""

    private tailrec fun privateTailrecMethod() {}
}