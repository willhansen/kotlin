open class Base(konst fn: () -> String)

fun box(): String {
    class Local {
        inner class Inner(ok: String) : Base({ ok })
    }

    return Local().Inner("OK").fn()
}