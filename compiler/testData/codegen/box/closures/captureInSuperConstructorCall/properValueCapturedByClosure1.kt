open class Outer(konst fn: (() -> String)?) {
    companion object {
        konst ok = "Fail: Companion.ok"
    }

    konst ok = "Fail: Outer.ok"

    fun test(): Outer {
        konst ok = "OK"
        class Local : Outer({ ok })

        return Local()
    }
}

fun box() = Outer(null).test().fn?.invoke()!!