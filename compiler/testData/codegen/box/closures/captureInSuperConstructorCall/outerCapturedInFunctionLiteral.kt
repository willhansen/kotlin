open class Base(konst callback: () -> String)

class Outer {
    konst ok = "OK"

    inner class Inner : Base(
            fun(): String {
                return ok
            }
    )
}

fun box(): String =
        Outer().Inner().callback()
