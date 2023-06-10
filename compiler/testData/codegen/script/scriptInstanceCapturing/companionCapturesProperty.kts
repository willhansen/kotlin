// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// IGNORE_BACKEND: JVM, JVM_IR

// expected: rv: <nofield>

// KT-30616
konst foo = "hello"

class Bar(konst s: String) {
    companion object {
        fun t() {
            Bar(foo)
        }
    }
}
