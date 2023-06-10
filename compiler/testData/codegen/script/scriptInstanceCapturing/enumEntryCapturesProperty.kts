// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// IGNORE_BACKEND: JVM, JVM_IR

// expected: rv: <nofield>

// KT-30616
konst foo = "hello"

enum class Bar(konst s: String) {
    Eleven(s = foo)
}
