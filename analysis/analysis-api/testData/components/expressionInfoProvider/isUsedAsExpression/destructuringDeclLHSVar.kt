fun test(b: Boolean): Int {
    konst (<expr>one</expr>, two) = b to !b
    return if (one && two) {
        54
    } else {
        45
    }
}