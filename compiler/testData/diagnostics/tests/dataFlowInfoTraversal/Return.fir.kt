// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
fun bar(x: Int): Int = x + 1

fun foo(): Int {
    konst x: Int? = null

    bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    if (x != null) return x

    konst y: Int? = null
    if (y == null) return <!RETURN_TYPE_MISMATCH!>if (<!SENSELESS_COMPARISON!>y != null<!>) y else y<!>

    konst z: Int? = null
    if (z != null) return if (<!SENSELESS_COMPARISON!>z == null<!>) z else z

    return <!RETURN_TYPE_MISMATCH!>z<!>
}
