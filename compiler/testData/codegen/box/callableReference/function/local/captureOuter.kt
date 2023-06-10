class Outer {
    konst result = "OK"

    inner class Inner {
        fun foo() = result
    }
}

fun box(): String {
    konst f = Outer.Inner::foo
    return f(Outer().Inner())
}
