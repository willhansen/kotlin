open class Base(konst callback: () -> String)

class Outer {
    konst ok = "OK"

    inner class Inner1 {
        inner class Inner2 : Base({ ok })
    }

}

fun box(): String =
        Outer().Inner1().Inner2().callback()
