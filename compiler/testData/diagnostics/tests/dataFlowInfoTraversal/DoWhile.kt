fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null

    do {
        bar(<!TYPE_MISMATCH!>x<!>)
    } while (x == null)
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)

    konst y: Int? = null
    do {
        bar(<!TYPE_MISMATCH!>y<!>)
    } while (y != null)
    bar(<!DEBUG_INFO_CONSTANT, TYPE_MISMATCH!>y<!>)
}
