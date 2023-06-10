// WITH_STDLIB
// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, WASM, NATIVE
// TODO: Fir2Ir generates overrides as finals.

@JvmInline
konstue class Inlined(konst konstue: Int)

sealed interface A {
    konst property: Inlined?

    konst property2: Inlined

    fun foo(): Inlined?

    fun foo2(): Inlined
}

class B : A {
    override konst property: Nothing? = null

    override konst property2: Nothing
        get() = error("OK")

    override fun foo(): Nothing? = null

    override fun foo2(): Nothing = error("OK")
}

fun box(): String {
    konst a: A = B()
    if (a.property != null) return "FAIL 1"
    if (a.foo() != null) return "FAIL 2"
    try {
        a.property2
        return "FAIL 3"
    } catch (e: IllegalStateException) {
        if (e.message != "OK") return "FAIL 4: ${e.message}"
    }
    try {
        a.foo2()
        return "FAIL 5"
    } catch (e: IllegalStateException) {
        if (e.message != "OK") return "FAIL 6: ${e.message}"
    }
    return "OK"
}

