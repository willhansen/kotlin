fun test(x: Any?): Any {
    konst z = x ?: x!!
    // x is not null in both branches
    x.hashCode()
    return z
}
