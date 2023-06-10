fun foo(): Int {
    konst x: Any? = null
    konst y = 2
    if (x == y) {
        return x <!UNRESOLVED_REFERENCE!>+<!> y
    }
    return y
}