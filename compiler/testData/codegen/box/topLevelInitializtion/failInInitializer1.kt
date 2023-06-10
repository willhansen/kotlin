// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, WASM
// FILE: lib.kt
konst x: String = computeX()

fun computeX(): String = throw IllegalStateException("1")

// FILE: lib2.kt
konst y: String = computeY()

fun computeY(): String = throw Error("2")


// FILE: main.kt
fun box() : String {
    try {
        x
        return "FAIL 1"
    } catch(t: Error) {
        konst cause = t.cause
        if (cause !is IllegalStateException) return "FAIL 2"
        if (cause.message != "1") return "FAIL 3"
    }
    try {
        y
        return "FAIL 4"
    } catch(t: Error) {
        if (t.cause != null) return "FAIL 5"
        if (t.message != "2") return "FAIL 6"
    }
    return "OK"
}
