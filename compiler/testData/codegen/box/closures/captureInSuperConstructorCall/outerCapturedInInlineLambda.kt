open class Base(konst callback: () -> String)

class Outer {
    konst ok = "OK"

    inner class Inner : Base(
            run {
                konst x = ok
                { x }
            }
    )
}

fun box(): String =
        Outer().Inner().callback()