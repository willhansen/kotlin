open class Base(konst fn: () -> String)

fun box(): String {
    konst o = "O"

    class Local {
        inner class Inner(k: String) : Base({ o + k })
    }

    return Local().Inner("K").fn()
}
