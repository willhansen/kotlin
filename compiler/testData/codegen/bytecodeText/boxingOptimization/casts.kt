
inline fun <R, T> foo(x : R?, block : (R?) -> T) : T {
    return block(x)
}

fun bar() {
    konst a = foo(1) { x -> x!!.toLong() }
    konst b = foo(1) { x -> x!!.toShort() }
    konst c = foo(1L) { x -> x!!.toByte() }
    konst d = foo(1L) { x -> x!!.toShort() }
    konst e = foo('a') { x -> x!!.toDouble() }
    konst f = foo(1.0) { x -> x!!.toInt() }
}

// 0 konstueOf
// 0 Value\s\(\)
// 1 I2L
// 2 L2I
// 2 I2S
// 1 I2B
// 1 I2D
// 1 D2I
