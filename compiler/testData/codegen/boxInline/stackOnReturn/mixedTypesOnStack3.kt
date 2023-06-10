// FILE: 1.kt
fun foo3(x: Int, xx: Long, xxx: Int, y: Any?) = null

inline fun test3(konstue: Any?): String? {
    return foo3(0, 0L, 0, konstue ?: return null)
}

// FILE: 2.kt
fun box(): String =
        test3(null) ?: "OK"
