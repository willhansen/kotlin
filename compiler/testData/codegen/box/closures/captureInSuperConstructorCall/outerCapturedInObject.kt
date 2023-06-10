interface Callback {
    fun invoke(): String
}

open class Base(konst callback: Callback)

class Outer {
    konst ok = "OK"

    inner class Inner : Base(
            object : Callback {
                override fun invoke() = ok
            }
    )
}

fun box(): String =
        Outer().Inner().callback.invoke()
