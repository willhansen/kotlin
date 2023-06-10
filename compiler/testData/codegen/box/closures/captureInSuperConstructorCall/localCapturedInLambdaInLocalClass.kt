open class Base(konst fn: () -> String)

fun box(): String {
    konst o = "O"

    class Local(k: String) : Base({ o + k })

    return Local("K").fn()
}