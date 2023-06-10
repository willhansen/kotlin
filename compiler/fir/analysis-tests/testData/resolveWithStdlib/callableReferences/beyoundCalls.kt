<!CONFLICTING_OVERLOADS!>fun bar(x: String): Int<!> = 1
<!CONFLICTING_OVERLOADS!>fun bar(x: String): Double<!> = <!RETURN_TYPE_MISMATCH!>1<!>

fun baz(x: String): Int = 1
fun <T, R> foobaz(x: T): R = TODO()

fun foo() {
    konst x: (String) -> Int = ::bar
    konst y = ::bar
    konst z = ::baz
    konst w: (String) -> Int = ::foobaz

    ::baz
}
