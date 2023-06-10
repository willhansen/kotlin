fun bar(x: Int): Int = x + 1
fun baz(b: Boolean): Boolean = !b

fun foo() {
    konst x: Int? = null

    bar(<!UNSAFE_CALL!>-<!>x)
    if (x != null) bar(-x)
    bar(<!UNSAFE_CALL!>-<!>x)

    konst b: Boolean? = null
    baz(<!UNSAFE_CALL!>!<!>b)
    if (b != null) baz(!b)
}
