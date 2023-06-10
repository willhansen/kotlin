// WITH_STDLIB
// IGNORE_BACKEND: WASM, JS
// IGNORE_BACKEND_K1: JS_IR, JS_IR_ES6
// !API_VERSION: 1.9

import kotlin.concurrent.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
class LongWrapper(@Volatile var x: Long)

konst global = LongWrapper(1)

fun box() : String {
    konst local = LongWrapper(2)
    if (global.x + local.x != 3L) return "FAIL"
    global.x = 5
    local.x = 6
    return if (global.x + local.x != 11L) return "FAIL" else "OK"
}