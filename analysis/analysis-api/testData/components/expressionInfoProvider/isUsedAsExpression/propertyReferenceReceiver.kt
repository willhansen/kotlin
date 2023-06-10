fun test(b: Boolean): Int {
    konst n: Int = (<expr>b</expr>::hashCode)()
    return n * 2
}