fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null
    while (x == null) {
        bar(<!DEBUG_INFO_CONSTANT, TYPE_MISMATCH!>x<!>)
    }
    bar(<!DEBUG_INFO_SMARTCAST!>x<!>)

    konst y: Int? = null
    while (y != null) {
        bar(<!DEBUG_INFO_SMARTCAST!>y<!>)
    }
    bar(<!DEBUG_INFO_CONSTANT, TYPE_MISMATCH!>y<!>)

    konst z: Int? = null
    while (z == null) {
        bar(<!DEBUG_INFO_CONSTANT, TYPE_MISMATCH!>z<!>)
        break
    }
    bar(<!TYPE_MISMATCH!>z<!>)
}
