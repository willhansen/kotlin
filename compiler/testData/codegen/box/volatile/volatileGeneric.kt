// WITH_STDLIB
// IGNORE_BACKEND: WASM, JS
// IGNORE_BACKEND_K1: JS_IR, JS_IR_ES6
// !API_VERSION: 1.9

import kotlin.concurrent.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
class GenericWrapper<T>(@Volatile var x: T)

konst global = GenericWrapper("FA")
konst globalLong = GenericWrapper(1L)

fun box() : String {
    konst local = GenericWrapper("IL")
    konst localLong = GenericWrapper(2L)
    if (global.x + local.x != "FAIL") return "FAIL 1"
    if (globalLong.x + localLong.x != 3L) return "FAIL 2"
    global.x = "O"
    local.x = "K"
    globalLong.x = 5L
    localLong.x = 6L
    if (globalLong.x + localLong.x != 11L) return "FAIL 3"
    return global.x + local.x
}