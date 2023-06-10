fun test(b: Boolean): <expr>Int</expr> {
    konst (one, two) = b to !b
    return if (one && two) {
        54
    } else {
        45
    }
}