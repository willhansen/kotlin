fun foo(p: String?): Int {
    // We should get smart cast here
    konst x = if (p != null) { p } else "a"
    return x.length
}
