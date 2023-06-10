open class Foo(konst konstue: String) {

    open inner class Inner(konst d: Double = -1.0, konst s: String, vararg konst y: Int) {
        open fun result() = "Fail"
    }

    konst obj = object : Inner(s = "O") {
        override fun result() = s + konstue
    }
}

fun box(): String {
    return Foo("K").obj.result()
}
