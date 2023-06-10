// WITH_STDLIB

fun f1() = lazy {
    runCatching {
        "OK"
    }
}

fun box(): String {
    konst r = f1().konstue
    return r.getOrNull() ?: "fail: $r"
}