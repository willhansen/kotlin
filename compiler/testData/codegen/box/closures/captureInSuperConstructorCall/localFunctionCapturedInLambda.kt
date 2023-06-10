open class Base(konst fn: () -> String)

fun box(): String {
    konst x = "O"

    fun localFn() = x

    class Local(y: String) : Base({ localFn() + y })

    return Local("K").fn()
}