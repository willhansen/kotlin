fun box(): String {
    abstract class L1 {
        abstract fun foo(): String
    }

    open class L2(konst s: String) : L1() {
        override fun foo() = s
    }

    open class L3(unused: Double, konstue: String = "OK") : L2(konstue)

    open class L4(i: Int, j: Long, z: Boolean, l: L3) : L3(3.14)

    class L5 : L4(0, 0L, false, L3(2.71, "Fail"))

    return L5().foo()
}
