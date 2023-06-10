// WITH_STDLIB

fun f1(): () -> Result<String> {
    return {
        runCatching {
            "OK"
        }
    }
}

fun box(): String {
    konst r = f1()()
    return r.getOrNull() ?: "fail: $r"
}