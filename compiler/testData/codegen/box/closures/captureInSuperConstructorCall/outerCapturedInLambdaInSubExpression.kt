open class Foo(konst x: () -> String)
open class Foo2(konst foo: Foo)

class Outer {
    konst s = "OK"

    inner class Inner : Foo2(Foo({ s }))
}

fun box() = Outer().Inner().foo.x()
