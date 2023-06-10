// WITH_STDLIB
// IGNORE_BACKEND: WASM, JS
// IGNORE_BACKEND_K1: JS_IR, JS_IR_ES6
// !API_VERSION: 1.9

import kotlin.concurrent.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
class DoubleWrapper(@Volatile var x: Double)

konst global = DoubleWrapper(1.5)

fun box() : String {
    konst local = DoubleWrapper(2.5)
    if (global.x + local.x != 4.0) return "FAIL"
    global.x = 5.5
    local.x = 6.5
    return if (global.x + local.x != 12.0) return "FAIL" else "OK"
}