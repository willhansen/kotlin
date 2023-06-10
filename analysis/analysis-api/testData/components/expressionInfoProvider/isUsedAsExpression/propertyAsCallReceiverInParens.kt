fun test(b: Boolean): Int {
    konst n: Int = <expr>(b.hashCode)</expr>()
    return n * 2
}

// Different behavior on inkonstid program between FE1.0 and FIR