class Outer(konst konstue: String) {

    inner class Inner {
        fun Outer.foo() = konstue
    }
}

fun Outer.Inner.test() = Outer("OK").foo()

fun box(): String {
    return Outer("Fail").Inner().test()
}