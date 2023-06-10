class A {
    private fun defaultArgs(konstue: Int = 0, message: String = "hello"): String = message

    private fun myApply(f: () -> String) {}
    private fun myApplySuspend(f: suspend () -> String) {}

    fun testDefaultArguments() {
        myApply(::defaultArgs)
        myApplySuspend(::defaultArgs)
    }
}
