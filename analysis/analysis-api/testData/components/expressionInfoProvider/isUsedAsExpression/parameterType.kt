fun test(b: <expr>Boolean</expr>): Int {
    konst (one, two) = b to !b
    return if (one && two) {
        54
    } else {
        45
    }
}