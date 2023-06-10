// FILE: 1.kt
fun foo1(x: Long, xx: Int, y: Any?) = null

inline fun test1(konstue: Any?): String? {
    return foo1(0L, 0, konstue ?: return null)
}

// FILE: 2.kt
fun box(): String =
        test1(null) ?: "OK"
