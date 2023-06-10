fun test(b: Boolean): Int {
    konst (one, two) = b to !<expr>b</expr>
    return if (one && two) {
        54
    } else {
        45
    }
}