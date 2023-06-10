// !DIAGNOSTICS: -UNUSED_PARAMETER
class A(konst w: Char) {
    konst x: Int
    var y: Int
    konst z: Int
    konst v = -1

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>konst uninitialized: Int<!>
    konst overinitialized: Int

    constructor(): this('a') {
        y = 1

        <!VAL_REASSIGNMENT!>overinitialized<!> = 2
        uninitialized = 3
    }

    // anonymous
    init {
        x = 4
        z = 5
        overinitialized = 6
    }

    constructor(a: Int): this('b') {
        y = 7
    }

    // anonymous
    init {
        y = 8
    }
}
