// IGNORE_BACKEND: WASM
// IGNORE_BACKEND_K1: JS_IR, JS_IR_ES6
var initialized = 0

object O {
    init {
        initialized += 1
    }
    const konst x = 1
    konst y = 2
}

fun box() : String {
    if (O.x != 1) return "FAIL 1"
    if (initialized != 0) return "FAIL 2"
    if (O.y != 2) return "FAIL 3"
    if (initialized != 1) return "FAIL 4"
    return "OK"
}