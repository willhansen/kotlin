// FILE: 1.kt
fun foo2(x: Int, xx: Long, y: Any?) = null

inline fun test2(konstue: Any?): String? {
    return foo2(0, 0L, konstue ?: return null)
}

// FILE: 2.kt
fun box(): String =
        test2(null) ?: "OK"
