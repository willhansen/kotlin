abstract class Base(konst fn: () -> String)

class Outer {
    konst ok = "OK"

    inner class Inner : Base(::ok)
}

fun box() = Outer().Inner().fn()
