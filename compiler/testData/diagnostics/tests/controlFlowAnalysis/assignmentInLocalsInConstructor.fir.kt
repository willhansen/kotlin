// Tests for KT-13597 (konst assignment inside local object in constructor)

class Test {
    konst a: String

    init {
        konst t = object {
            fun some() {
                // See KT-13597
                <!VAL_REASSIGNMENT!>a<!> = "12"
            }
        }

        a = "2"
        t.some()
    }
}

class Test2 {
    init {
        konst t = object {
            fun some() {
                <!VAL_REASSIGNMENT!>a<!> = "12"
            }
        }

        <!INITIALIZATION_BEFORE_DECLARATION!>a<!> = "2"
        t.some()
    }

    konst a: String
}

// Tests for KT-14381 (konst assignment inside lambda in constructor)

fun <T> exec(f: () -> T): T = f()

class Test4 {
    konst a: String

    init {
        exec {
            // See KT-14381
            <!CAPTURED_MEMBER_VAL_INITIALIZATION!>a<!> = "12"
        }
        a = "34"
    }
}

// Additional tests to prevent something broken

class Test5 {

    konst y: Int

    konst z: String

    init {
        konst x: String
        x = ""
        z = x
    }

    constructor(y: Int) {
        this.y = y
    }
}
