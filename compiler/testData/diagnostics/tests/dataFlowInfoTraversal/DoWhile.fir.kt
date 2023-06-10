fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null

    do {
        bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    } while (x == null)
    bar(x)

    konst y: Int? = null
    do {
        bar(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
    } while (y != null)
    bar(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
}
