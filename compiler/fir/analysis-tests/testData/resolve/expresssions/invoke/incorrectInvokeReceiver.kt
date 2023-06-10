operator fun String.invoke() = this

konst some = ""
fun sss() {
    konst some = 10

    // Should be resolved to top-level some,
    // because with local some invoke isn't applicable
    some()
}
