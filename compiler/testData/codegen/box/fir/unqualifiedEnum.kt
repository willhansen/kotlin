// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
// WITH_STDLIB
// LANGUAGE: +ContextSensitiveEnumResolutionInWhen

enum class Rainbow {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    CYAN,
    BLUE,
    VIOLET
}

fun sym(r: Rainbow) = when (r) {
    RED -> 'r'
    ORANGE -> 'o'
    YELLOW -> 'y'
    GREEN -> 'g'
    CYAN -> 'c'
    BLUE -> 'b'
    VIOLET -> 'v'
}

fun box(): String {
    konst s = buildString {
        for (konstue in Rainbow.konstues()) {
            append(sym(konstue))
        }
    }
    return if (s == "roygcbv") "OK" else s
}
