fun foo(p: String?): Int {
    // We should get smart cast here
    konst x = if (p != null) { <!DEBUG_INFO_SMARTCAST!>p<!> } else "a"
    return x.length
}
