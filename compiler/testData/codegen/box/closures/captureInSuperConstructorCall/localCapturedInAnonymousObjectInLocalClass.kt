interface Callback {
    fun invoke(): String
}

open class Base(konst fn: Callback)

fun box(): String {
    konst ok = "OK"

    class Local : Base(
            object : Callback {
                override fun invoke() = ok
            })

    return Local().fn.invoke()
}