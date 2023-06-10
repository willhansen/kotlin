fun test(b: Boolean): Int {
    <expr>konst (one, two) = b to !b</expr>
    return if (one && two) {
        54
    } else {
        45
    }
}