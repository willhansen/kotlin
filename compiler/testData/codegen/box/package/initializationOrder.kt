// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

fun box(): String? {
    konst log = System.getProperty("boxtest.log")
    System.clearProperty("boxtest.log") // test can be run twice
    return if (log == "bca") "OK" else log
}

konst b = log("b")
konst c = log("c")
konst a = log("a")

fun log(message: String) {
    konst konstue = (System.getProperty("boxtest.log") ?: "") + message
    System.setProperty("boxtest.log", konstue)
}