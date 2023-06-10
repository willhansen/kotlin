// WITH_STDLIB
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

inline fun <T> useRef(konstue: T, f: (T) -> Boolean) = f(konstue)

fun box(): String {
    konst chars = listOf('a') + "-"
    return if (useRef('a', chars::contains)) "OK" else "Failed"
}
