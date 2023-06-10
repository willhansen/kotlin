interface Callback {
    fun invoke(): String
}

open class Base(konst callback: Callback)

class Outer {
    konst ok = "OK"

    inner class Inner1 {
        inner class Inner2 : Base(
                object : Callback {
                    override fun invoke() = ok
                }
        )
    }
}

fun box(): String =
        Outer().Inner1().Inner2().callback.invoke()
