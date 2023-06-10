open class Outer(konst fn: (() -> String)?) {
    companion object {
        konst ok = "OK"
    }

    konst ok = "Fail: Outer.ok"

    inner class Inner : Outer({ ok })
}

fun box() = Outer(null).Inner().fn?.invoke()!!