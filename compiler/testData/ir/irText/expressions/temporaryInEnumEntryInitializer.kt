// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

konst n: Any? = null

enum class En(konst x: String?) {
    ENTRY(n?.toString())
}
