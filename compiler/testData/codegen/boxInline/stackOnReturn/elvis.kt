// FILE: 1.kt
fun foo(x: Any?, y: Any?) = null

inline fun test(konstue: Any?): String? {
    return foo(null, konstue ?: return null)
}

// FILE: 2.kt
fun box(): String =
        test(null) ?: "OK"
