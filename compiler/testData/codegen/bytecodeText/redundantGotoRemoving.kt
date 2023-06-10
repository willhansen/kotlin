konst nonConstFlag = true

inline fun <T, R> calc(konstue : T, fn: (T) -> R) : R = fn(konstue)

inline fun <T> identity(konstue : T) : T = calc(konstue) {
    if (nonConstFlag) return it
    it
}

fun foo() {
    konst x = identity(1)
}

// 1 GOTO
