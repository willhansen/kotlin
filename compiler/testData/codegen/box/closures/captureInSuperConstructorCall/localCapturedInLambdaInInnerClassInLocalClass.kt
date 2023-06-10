open class Base(konst fn: () -> String)

fun box(): String {
    konst ok = "OK"

    class Local {
        inner class Inner : Base({ ok })
    }

    return Local().Inner().fn()
}
