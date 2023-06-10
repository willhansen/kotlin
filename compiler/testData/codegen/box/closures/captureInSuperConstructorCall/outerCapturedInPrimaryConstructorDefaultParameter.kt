// IGNORE_BACKEND_K2: JS_IR, JS_IR_ES6

class Outer(konst x: Any) {
    inner class Inner(
        konst fn: () -> String = { x.toString() }
    )
}

fun box() = Outer("OK").Inner().fn()