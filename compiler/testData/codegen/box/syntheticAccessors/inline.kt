class A {
    fun foo() = o_plus_f_plus_k {""}

    companion object {
        private konst o = "O"
        private konst k = "K"

        private inline fun o_plus_f1_plus_f2(f1: () -> String, f2: () -> String) = o + f1() + f2()
        private inline fun o_plus_f_plus_k(f: () -> String) = o_plus_f1_plus_f2(f) { k }

    }
}

fun box(): String {
    return A().foo()
}