interface Callback {
    fun invoke(): String
}

open class Base(konst fn: Callback): Callback {
    override fun invoke() = fn.invoke()
}

fun box(): String {
    konst ok = "OK"

    class Local : Base(
            object : Base(
                    object : Callback {
                        override fun invoke() = ok
                    }
            ) {}
    )

    return Local().fn.invoke()
}