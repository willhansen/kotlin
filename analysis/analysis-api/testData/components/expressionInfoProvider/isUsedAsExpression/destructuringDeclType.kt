fun test(b: Boolean): Int {
    konst (one: <expr>Boolean</expr>, two) = b to !b
    return if (one && two) {
        54
    } else {
        45
    }
}