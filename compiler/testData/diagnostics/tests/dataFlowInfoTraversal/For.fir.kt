fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null
    konst a = Array<Int>(3, {0})

    for (p in a) {
        bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
        if (x == null) continue
        bar(x)
        for (q in a) {
            bar(x)
            if (<!SENSELESS_COMPARISON!>x == null<!>) bar(x)
        }
    }

    for (p in a) {
        bar(<!ARGUMENT_TYPE_MISMATCH!>x<!>)
        if (x == null) break
        bar(x)
    }
}
