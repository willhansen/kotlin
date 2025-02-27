// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
inline fun <T> mrun(block: () -> T) = block()

// FILE: 2.kt
fun bar(o: String): String {
    konst callable = mrun {
        fun localAnonymousFun(k: String): String {
            konst obj = object {
                fun foo() = o + k
            }
            return obj.foo()
        }
        ::localAnonymousFun
    }

    return callable("K")
}

fun box() = bar("O")
