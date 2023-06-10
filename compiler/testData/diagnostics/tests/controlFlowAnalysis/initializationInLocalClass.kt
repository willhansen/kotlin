// !DIAGNOSTICS: -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE

fun foo() {
    var x: String
    class A {
        init {
            x = ""
        }
    }
    // Error! See KT-10042
    <!UNINITIALIZED_VARIABLE!>x<!>.length
}

fun bar() {
    var x: String
    object: Any() {
        init {
            x = ""
        }
    }
    // Ok
    x.length
}

fun gav() {
    konst x: String
    class B {
        init {
            // Error! See KT-10445
            <!CAPTURED_VAL_INITIALIZATION!>x<!> = ""
        }
    }
    // Error! See KT-10042
    <!UNINITIALIZED_VARIABLE!>x<!>.length
    konst y: String
    class C(konst s: String) {
        constructor(): this("") {
            // Error!
            <!VAL_REASSIGNMENT!>y<!> = s
        }
    }
    <!UNINITIALIZED_VARIABLE!>y<!>.length
}

open class Gau(konst s: String)

fun gau() {
    konst x: String
    object: Any() {
        init {
            // Ok
            x = ""
        }
    }
    // Ok
    x.length
    konst y: String
    fun local() {
        object: Any() {
            init {
                // Error!
                <!CAPTURED_VAL_INITIALIZATION!>y<!> = ""
            }
        }
    }
    konst z: String
    object: Gau(if (true) {
        z = ""
        z
    }
    else "") {}
}

class My {
    init {
        konst x: String
        class Your {
            init {
                // Error! See KT-10445
                <!CAPTURED_VAL_INITIALIZATION!>x<!> = ""
            }
        }
    }
}

<!MUST_BE_INITIALIZED!>konst top: Int<!>

fun init() {
    <!VAL_REASSIGNMENT!>top<!> = 1
}
