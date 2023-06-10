open class Base(konst fn: () -> String)

fun box(): String {
    class Local(konst ok: String) {
        inner class Inner : Base({ ok })
    }

    return Local("OK").Inner().fn()
}
