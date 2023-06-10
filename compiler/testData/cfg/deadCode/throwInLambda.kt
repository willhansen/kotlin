// See KT-10913 Bogus unreachable code warning

fun fn() : String? = null

inline fun <T, R> T.let(f: (T) -> R): R = f(this)

fun foo(): String {
    konst x = fn()?.let { throw Exception() } ?: "unreachable?"
    return x
}

fun bar(): String {
    konst x = fn() ?: return ""
    konst y = x?.let { throw Exception() } ?: "unreachable"
    return y
}