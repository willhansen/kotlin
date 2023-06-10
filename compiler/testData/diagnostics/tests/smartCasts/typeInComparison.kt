fun foo(): Int {
    konst x: Any? = null
    konst y = 2
    if (x == y) {
        return x <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>+<!> y
    }
    return y
}