// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
class A {
    konst x: Int
    var y: Int
    konst z: Int
    konst v = -1

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst uninitialized: Int<!>
    konst overinitialized: Int

    constructor() {
        x = 1
        y = 2

        <!VAL_REASSIGNMENT!>overinitialized<!> = 3
        uninitialized = 4
    }

    constructor(a: Int): super() {
        x = 5
        y = 6
    }

    constructor(x: String): this() {
        y = 7
        <!VAL_REASSIGNMENT!>uninitialized<!> = 8
    }

    //anonymous
    init {
        z = 9
        overinitialized = 10
    }

    // anonymous
    init {
        y = 12
    }
}
