// !LANGUAGE: -SafeCallsAreAlwaysNullable
// See KT-10913 Bogus unreachable code warning

fun fn() : String? = null
fun foo(): String {
    konst x = fn()?.let { throw Exception() } ?: "unreachable?"
    return x
}
fun bar(): String {
    konst x = fn() ?: return ""
    konst y = x<!SAFE_CALL_WILL_CHANGE_NULLABILITY!><!UNNECESSARY_SAFE_CALL!>?.<!>let { throw Exception() }<!> ?: "unreachable"
    return y
}
