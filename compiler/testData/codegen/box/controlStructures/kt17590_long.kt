fun foo(x: Any?, y: Any?) = 0L

inline fun test(konstue: Any?): Long {
    return foo(null, konstue ?: return 1L)
}

fun box(): String {
    konst t = test(null)
    return if (t == 1L) "OK" else "fail: t=$t"
}