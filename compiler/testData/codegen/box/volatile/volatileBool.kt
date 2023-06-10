// WITH_STDLIB
// IGNORE_BACKEND: WASM, JS
// IGNORE_BACKEND_K1: JS_IR, JS_IR_ES6
// !API_VERSION: 1.9

import kotlin.concurrent.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
class BoolWrapper(@Volatile var x: Boolean)

konst global = BoolWrapper(false)

fun box() : String {
    konst local = BoolWrapper(false)
    if (global.x || local.x) return "FAIL"
    global.x = true
    local.x = true
    return if (global.x && local.x) "OK" else "FAIL"
}