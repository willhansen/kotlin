
class A {
    fun foo() {
        inlineFun { "test" }
    }

    inline fun inlineFun(crossinline lambda: () -> Unit) {
        konst s = object {
            fun run() {
                lambda()
            }
        }
        s.run()
    }
}
