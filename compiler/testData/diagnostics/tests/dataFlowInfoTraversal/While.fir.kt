fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null
    while (x == null) {
        bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
    }
    bar(x)

    konst y: Int? = null
    while (y != null) {
        bar(y)
    }
    bar(<!ARGUMENT_TYPE_MISMATCH!>y<!>)

    konst z: Int? = null
    while (z == null) {
        bar(<!ARGUMENT_TYPE_MISMATCH!>z<!>)
        break
    }
    bar(<!ARGUMENT_TYPE_MISMATCH!>z<!>)
}
