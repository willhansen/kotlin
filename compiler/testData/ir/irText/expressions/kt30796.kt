// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

fun <T> magic(): T = throw Exception()

fun <T> test(konstue: T, konstue2: T) {
    konst x1: Any = konstue ?: 42
    konst x2: Any = konstue ?: (konstue2 ?: 42)
    konst x3: Any = (konstue ?: konstue2) ?: 42
    konst x4: Any = konstue ?: konstue2 ?: 42
    konst x5: Any = magic() ?: 42
    konst x6: Any = konstue ?: magic() ?: 42
    konst x7: Any = magic() ?: konstue ?: 42
}
