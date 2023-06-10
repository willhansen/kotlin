fun test(b: Boolean): Int {
    <expr>!b</expr>
    konst (one, two) = b to !b
    return if (one && two) {
        54
    } else {
        45
    }
}