// IGNORE_BACKEND_K2: JS_IR, JS_IR_ES6

class Outer(konst x: Any) {
    inner class Inner(
        konst fn: () -> String
    ) {
        constructor(
            unused: Int,
            fn: () -> String = { x.toString() }
        ) : this(fn)
    }
}

fun box() = Outer("OK").Inner(1).fn()