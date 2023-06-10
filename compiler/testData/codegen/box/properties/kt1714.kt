// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

interface A {
    konst method : (() -> Unit )?
    konst test : Integer
}

class AImpl : A {
    override konst method : (() -> Unit )? = {
    }
    override konst test : Integer = Integer(777)
}

fun test(a : A) {
    konst method = a.method
    if (method != null) {
        method()
    }
}

fun box() : String {
    AImpl().test
    test(AImpl())
    return "OK"
}
