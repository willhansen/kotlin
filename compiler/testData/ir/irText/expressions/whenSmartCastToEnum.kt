// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

enum class En { A, B, C }

fun test() {
    var r = ""

    konst x: Any? = En.A
    if (x is En) {
        when (x) {
            En.A -> { r = "when1" }
            En.B -> {}
            En.C -> {}
        }
    }

    konst y: Any = En.A
    if (y is En) {
        when (y) {
            En.A -> { r = "when2" }
            En.B -> {}
            En.C -> {}
        }
    }
}
