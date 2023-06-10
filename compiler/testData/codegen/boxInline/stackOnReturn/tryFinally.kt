// FILE: 1.kt
fun foo(x: Any?, y: Any?) = null

var finallyFlag = false

inline fun test(konstue: Any?): String? {
    try {
        return foo(null, konstue ?: return null)
    }
    finally {
        finallyFlag = true
    }
}

// FILE: 2.kt
fun box(): String =
        test(null) ?: if (finallyFlag) "OK" else "finallyFlag not set"
