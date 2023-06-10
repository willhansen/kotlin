// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
class Box<T>(konst konstue: T) {
    inline fun run(block: (T) -> Unit) {
        block(konstue)
    }
}

// FILE: 2.kt
fun box(): String {
    var result: String = "fail"
    Box("OK").run { outer ->
        konst block = { result = outer }
        block()
    }
    return result
}