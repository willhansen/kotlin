fun <T> nullableK(m: () -> T?) = m()

fun box(): String {
    // no coercion to Unit for T?
    konst nullableK = (nullableK<Unit> { null }).toString()
    return if (nullableK == "null") "OK" else  "fail: $nullableK"
}