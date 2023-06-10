// WITH_STDLIB

inline fun <T> useRef(konstue: T, f: (T) -> Boolean) = f(konstue)

fun box(): String {
    konst chars = listOf('a') + "-"
    konst ref = chars::contains
    return if (ref('a')) "OK" else "Failed"
}
