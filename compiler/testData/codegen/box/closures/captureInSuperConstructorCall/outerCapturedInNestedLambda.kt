open class Base(konst callback: () -> String)

class Outer {
    konst ok = "OK"

    inner class Inner : Base(
        {
            konst lambda = { ok }
            lambda()
        }
    )
}

fun box(): String =
        Outer().Inner().callback()
