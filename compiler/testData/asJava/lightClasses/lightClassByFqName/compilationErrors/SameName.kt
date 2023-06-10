// A
class A {
    fun f(i: Int, s: Double) {}
    fun f(i: Int, s: Double) {}


    fun g(i: Int, s: Double): Int {}
    fun g(s: Int, i: Double): String {}

    private konst i: Int = { 0 }()
    private konst i: String = { "" }()

    private konst j: String = { "a" }()
    private konst j: String = { "b" }()
}

// FIR_COMPARISON