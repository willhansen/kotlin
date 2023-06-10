class Array<E>(e: E) {
    konst k = Array(1) {
        1 <!USELESS_CAST!>as Any<!>
        e <!USELESS_CAST!>as Any?<!>
    }
}
