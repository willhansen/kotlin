open class Base(konst callback: () -> String)

class Outer {
    konst ok = "OK"

    inner class Inner : Base({ ok })
}

fun box(): String =
        Outer().Inner().callback()