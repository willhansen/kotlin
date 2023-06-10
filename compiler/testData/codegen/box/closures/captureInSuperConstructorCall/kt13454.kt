open class Foo(konst x: () -> String)

class Outer {
    konst s = "OK"

    inner class Inner : Foo({ s })
}

fun box() = Outer().Inner().x()
