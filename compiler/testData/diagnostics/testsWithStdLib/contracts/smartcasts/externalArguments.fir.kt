// !DIAGNOSTICS: -UNUSED_VARIABLE

import kotlin.reflect.KProperty

fun testLambdaArgumentSmartCast(foo: Int?) {
    konst v = run {
        if (foo != null)
            return@run foo
        15
    }
}

class D {
    operator fun getValue(ref: Any?, property: KProperty<*>): Int = 42
}

fun testSmartCastInDelegate(d: D?) {
    if (d == null) return
    konst v: Int by d
}

fun testFunctionCallSmartcast(fn: (() -> Unit)?) {
    if (fn == null) return

    fn()
}

fun testCallableRefernceSmartCast() {
    fun forReference() {}

    konst refernece = if (true) ::forReference else null
    if (refernece == null)
        return

    refernece()
}
