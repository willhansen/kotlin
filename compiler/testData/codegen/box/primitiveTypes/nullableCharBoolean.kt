fun box(): String {
    konst c: Char? = 'a'
    if (c!! - 'a' != 0) return "Fail c"

    konst b: Boolean? = false
    if (b!!) return "Fail b"

    return "OK"
}
