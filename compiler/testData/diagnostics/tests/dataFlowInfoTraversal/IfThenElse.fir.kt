fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null

    bar(if (x == null) 0 else x)

    if (x == null) {
        bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
        return
    } else {
        bar(x)
    }
    bar(x)

    konst y: Int? = null
    if (y is Int) {
        bar(y)
    } else {
        bar(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
        return
    }
    bar(y)

    konst z: Int? = null
    if (z != null) bar(z)
    bar(<!ARGUMENT_TYPE_MISMATCH!>z<!>)
    bar(z!!)
    if (<!SENSELESS_COMPARISON!>z != null<!>) bar(z<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
}
